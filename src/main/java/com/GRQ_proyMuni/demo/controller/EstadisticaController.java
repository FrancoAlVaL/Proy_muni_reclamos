package com.GRQ_proyMuni.demo.controller;

import com.GRQ_proyMuni.demo.entity.Reclamo;
import com.GRQ_proyMuni.demo.service.ReclamoService;
import com.GRQ_proyMuni.demo.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class EstadisticaController {

    @Autowired
    private ReclamoService reclamoService;

    @Autowired
    private UsuarioService usuarioService;

    // 1. VISTA PRINCIPAL (Carga inicial de la página)
    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        List<Reclamo> reclamos = reclamoService.listarTodos();
        var estados = reclamos.stream().collect(Collectors.groupingBy(
                reclamo -> reclamo.getEstado().getNombreEstado(), Collectors.counting()));
        var tipos = reclamos.stream().collect(Collectors.groupingBy(
                reclamo -> reclamo.getCategoria().getNombreCategoria(), Collectors.counting()));
        var areas = reclamos.stream().collect(Collectors.groupingBy(
                reclamo -> reclamo.getArea().getNombreArea(), Collectors.counting()));
        
        long resueltos = reclamos.stream()
                .filter(reclamo -> "Resuelto".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())).count();
        long pendientes = reclamos.stream()
                .filter(reclamo -> "Pendiente".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())).count();
        double promedio = reclamos.stream().filter(reclamo -> reclamo.getFechaCierre() != null)
                .mapToLong(reclamo -> Duration.between(reclamo.getFechaRegistro(), reclamo.getFechaCierre()).toDays())
                .average().orElse(0);
                
        model.addAttribute("totalReclamos", reclamos.size());
        model.addAttribute("totalUsuarios", usuarioService.listarTodos().size());
        model.addAttribute("resueltos", resueltos);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("promedio", String.format(java.util.Locale.ROOT, "%.1f", promedio));
        model.addAttribute("datosMeses", monthlyData(reclamos));
        model.addAttribute("datosTipos", chartData(tipos));
        model.addAttribute("datosEstados", chartData(estados));
        model.addAttribute("datosAreas", chartData(areas));
        
        return "estadisticas";
    }

    // 2. ENDPOINT JSON (Para que el filtro de años funcione con JavaScript)
    @GetMapping("/estadisticas/json")
    @ResponseBody
    public Map<String, Object> estadisticasJson(@RequestParam(required = false, defaultValue = "2026") Integer año) {
        List<Reclamo> todosReclamos = reclamoService.listarTodos();
        
        // Usar variable final explícita para el lambda
        final int finalYear = año;
        
        List<Reclamo> reclamos = todosReclamos.stream()
                .filter(r -> r.getFechaRegistro() != null && r.getFechaRegistro().getYear() == finalYear)
                .collect(Collectors.toList());
        
        var estados = reclamos.stream().collect(Collectors.groupingBy(
                reclamo -> reclamo.getEstado().getNombreEstado(), Collectors.counting()));
        var tipos = reclamos.stream().collect(Collectors.groupingBy(
                reclamo -> reclamo.getCategoria().getNombreCategoria(), Collectors.counting()));
        var areas = reclamos.stream().collect(Collectors.groupingBy(
                reclamo -> reclamo.getArea().getNombreArea(), Collectors.counting()));
        
        long resueltos = reclamos.stream()
                .filter(reclamo -> "Resuelto".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())).count();
        long pendientes = reclamos.stream()
                .filter(reclamo -> "Pendiente".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())).count();
        
        double promedio = reclamos.stream().filter(reclamo -> reclamo.getFechaCierre() != null)
                .mapToLong(reclamo -> Duration.between(reclamo.getFechaRegistro(), reclamo.getFechaCierre()).toDays())
                .average().orElse(0);
        
        // Usar HashMap en lugar de Map.of() para evitar límite de 10 pares
        Map<String, Object> response = new HashMap<>();
        response.put("totalReclamos", reclamos.size());
        response.put("resueltos", resueltos);
        response.put("pendientes", pendientes);
        response.put("promedio", String.format(java.util.Locale.ROOT, "%.1f", promedio));
        response.put("meses", getMonthlyDataList(reclamos, finalYear));
        response.put("tiposLabels", new ArrayList<>(tipos.keySet()));
        response.put("tiposData", new ArrayList<>(tipos.values()));
        response.put("estadosLabels", new ArrayList<>(estados.keySet()));
        response.put("estadosData", new ArrayList<>(estados.values()));
        response.put("areasLabels", new ArrayList<>(areas.keySet()));
        response.put("areasData", new ArrayList<>(areas.values()));
        
        return response;
    }

    // 3. ENDPOINT PARA EXPORTAR A CSV
    @GetMapping("/estadisticas/exportar")
    public void exportarEstadisticas(@RequestParam(required = false, defaultValue = "2026") Integer año, 
                                      HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=estadisticas_" + año + ".csv");
        
        PrintWriter writer = response.getWriter();
        writer.println("Año,Mes,Cantidad");
        
        final int finalYear = año;
        List<Reclamo> reclamos = reclamoService.listarTodos().stream()
                .filter(r -> r.getFechaRegistro() != null && r.getFechaRegistro().getYear() == finalYear)
                .collect(Collectors.toList());
        
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        for (int i = 0; i < 12; i++) {
            final int mesActual = i + 1;
            long count = reclamos.stream()
                    .filter(r -> r.getFechaRegistro().getMonthValue() == mesActual)
                    .count();
            writer.println(año + "," + meses[i] + "," + count);
        }
        writer.flush();
    }

    // --- Métodos auxiliares ---
    private Map<String, Object> chartData(Map<String, Long> grouped) {
        List<String> labels = new ArrayList<>(grouped.keySet());
        List<Long> values = labels.stream().map(grouped::get).toList();
        return Map.of("labels", labels, "data", values);
    }

    private Map<String, Object> monthlyData(List<Reclamo> reclamos) {
        final int year = LocalDateTime.now().getYear();
        final List<Reclamo> finalReclamos = reclamos;
        
        List<String> labels = List.of("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
        List<Long> values = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> finalReclamos.stream()
                        .filter(r -> r.getFechaRegistro() != null 
                                && r.getFechaRegistro().getYear() == year
                                && r.getFechaRegistro().getMonthValue() == month)
                        .count())
                .toList();
        return Map.of("labels", labels, "data", values);
    }

    private List<Long> getMonthlyDataList(List<Reclamo> reclamos, int year) {
        final int finalYear = year;
        final List<Reclamo> finalReclamos = reclamos; 
        
        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> finalReclamos.stream()
                        .filter(r -> r.getFechaRegistro() != null 
                                && r.getFechaRegistro().getYear() == finalYear
                                && r.getFechaRegistro().getMonthValue() == month)
                        .count())
                .toList();
    }
}