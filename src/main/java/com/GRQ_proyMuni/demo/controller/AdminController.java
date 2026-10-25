package com.GRQ_proyMuni.demo.controller;

import com.GRQ_proyMuni.demo.entity.Area;
import com.GRQ_proyMuni.demo.entity.Categoria;
import com.GRQ_proyMuni.demo.entity.Estado;
import com.GRQ_proyMuni.demo.entity.Rol;
import com.GRQ_proyMuni.demo.entity.TipoDocumento;
import com.GRQ_proyMuni.demo.service.AreaService;
import com.GRQ_proyMuni.demo.service.CategoriaService;
import com.GRQ_proyMuni.demo.service.EstadoService;
import com.GRQ_proyMuni.demo.service.RolService;
import com.GRQ_proyMuni.demo.service.TipoDocumentoService;
import com.GRQ_proyMuni.demo.service.UsuarioService;
import com.GRQ_proyMuni.demo.service.ReclamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/catalogos")
public class AdminController {

    @Autowired private AreaService areaService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private EstadoService estadoService;
    @Autowired private RolService rolService;
    @Autowired private TipoDocumentoService tipoDocumentoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ReclamoService reclamoService;

    // Vista única para gestionar todos los catálogos
    @GetMapping
    public String gestionCatalogos(Model model, HttpSession session) {
        // Obtener usuario logueado
        Integer usuarioId = (Integer) session.getAttribute("authenticatedUserId");
        if (usuarioId != null) {
            var usuario = usuarioService.buscarPorId(usuarioId).orElse(null);
            model.addAttribute("usuario", usuario);
        }
        
        model.addAttribute("areas", areaService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodos());
        model.addAttribute("estados", estadoService.listarTodos());
        model.addAttribute("roles", rolService.listarTodos());
        model.addAttribute("tiposDocumento", tipoDocumentoService.listarTodos());
        return "catalogos";
    }

    // Guardar Área
    @PostMapping("/area")
    public String guardarArea(@ModelAttribute Area area) {
        areaService.guardar(area);
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/area/{id}")
    public String actualizarArea(@PathVariable Integer id,
                                 @RequestParam String nombreArea,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam Boolean estadoActivo) {
        Area area = areaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        area.setNombreArea(validarNombre(nombreArea));
        area.setDescripcion(validarDescripcion(descripcion));
        area.setEstadoActivo(estadoActivo);
        areaService.guardar(area);
        return "redirect:/admin/catalogos";
    }

    // Guardar Categoría
    @PostMapping("/categoria")
    public String guardarCategoria(@ModelAttribute Categoria categoria) {
        categoriaService.guardar(categoria);
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/categoria/{id}")
    public String actualizarCategoria(@PathVariable Integer id,
                                      @RequestParam String nombreCategoria,
                                      @RequestParam(required = false) String descripcion,
                                      @RequestParam Boolean estadoActivo) {
        Categoria categoria = categoriaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        categoria.setNombreCategoria(validarNombre(nombreCategoria));
        categoria.setDescripcion(validarDescripcion(descripcion));
        categoria.setEstadoActivo(estadoActivo);
        categoriaService.guardar(categoria);
        return "redirect:/admin/catalogos";
    }

    // Guardar Estado
    @PostMapping("/estado")
    public String guardarEstado(@ModelAttribute Estado estado) {
        estadoService.guardar(estado);
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/estado/{id}")
    public String actualizarEstado(@PathVariable Integer id,
                                   @RequestParam String nombreEstado,
                                   @RequestParam(required = false) String descripcion,
                                   @RequestParam(required = false) Integer ordenSecuencia) {
        Estado estado = estadoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        estado.setNombreEstado(validarNombre(nombreEstado));
        estado.setDescripcion(validarDescripcion(descripcion));
        if (ordenSecuencia != null && ordenSecuencia < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El orden debe ser mayor que cero.");
        }
        estado.setOrdenSecuencia(ordenSecuencia);
        estadoService.guardar(estado);
        return "redirect:/admin/catalogos";
    }

    // Guardar Rol
    @PostMapping("/rol")
    public String guardarRol(@ModelAttribute Rol rol) {
        rolService.guardar(rol);
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/rol/{id}")
    public String actualizarRol(@PathVariable Integer id,
                                @RequestParam String nombreRol,
                                @RequestParam(required = false) String descripcion) {
        Rol rol = rolService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        rol.setNombreRol(validarNombre(nombreRol));
        rol.setDescripcion(validarDescripcion(descripcion));
        rolService.guardar(rol);
        return "redirect:/admin/catalogos";
    }

    // Guardar Tipo de Documento
    @PostMapping("/tipo-documento")
    public String guardarTipoDocumento(@ModelAttribute TipoDocumento tipoDocumento) {
        tipoDocumentoService.guardar(tipoDocumento);
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/tipo-documento/{id}")
    public String actualizarTipoDocumento(@PathVariable Integer id,
                                          @RequestParam String nombreTipo,
                                          @RequestParam(required = false) String descripcion) {
        TipoDocumento tipoDocumento = tipoDocumentoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        tipoDocumento.setNombreTipo(validarNombre(nombreTipo));
        tipoDocumento.setDescripcion(validarDescripcion(descripcion));
        tipoDocumentoService.guardar(tipoDocumento);
        return "redirect:/admin/catalogos";
    }

    // Eliminar Área
    @PostMapping("/area/{id}/eliminar")
    public String eliminarArea(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        boolean asignadaAUsuario = usuarioService.listarTodos().stream()
                .anyMatch(usuario -> usuario.getArea() != null
                        && id.equals(usuario.getArea().getIdArea()));
        boolean asignadaAReclamo = reclamoService.listarTodos().stream()
                .anyMatch(reclamo -> reclamo.getArea() != null
                        && id.equals(reclamo.getArea().getIdArea()));
        if (asignadaAUsuario || asignadaAReclamo) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar el área porque está asignada a usuarios o reclamos. Puedes desactivarla.");
            return "redirect:/admin/catalogos";
        }
        areaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Área eliminada correctamente.");
        return "redirect:/admin/catalogos";
    }

    // Eliminar Categoría
    @PostMapping("/categoria/{id}/eliminar")
    public String eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminar(id);
        return "redirect:/admin/catalogos";
    }

    // Eliminar Estado
    @PostMapping("/estado/{id}/eliminar")
    public String eliminarEstado(@PathVariable Integer id) {
        estadoService.eliminar(id);
        return "redirect:/admin/catalogos";
    }

    // Eliminar Rol
    @PostMapping("/rol/{id}/eliminar")
    public String eliminarRol(@PathVariable Integer id) {
        rolService.eliminar(id);
        return "redirect:/admin/catalogos";
    }

    // Eliminar Tipo de Documento
    @PostMapping("/tipo-documento/{id}/eliminar")
    public String eliminarTipoDocumento(@PathVariable Integer id) {
        tipoDocumentoService.eliminar(id);
        return "redirect:/admin/catalogos";
    }

    private String validarNombre(String nombre) {
        String valor = nombre == null ? "" : nombre.trim();
        if (valor.isEmpty() || valor.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio y no puede superar 100 caracteres.");
        }
        return valor;
    }

    private String validarDescripcion(String descripcion) {
        if (descripcion == null) return null;
        String valor = descripcion.trim();
        if (valor.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripción no puede superar 255 caracteres.");
        }
        return valor.isEmpty() ? null : valor;
    }
}