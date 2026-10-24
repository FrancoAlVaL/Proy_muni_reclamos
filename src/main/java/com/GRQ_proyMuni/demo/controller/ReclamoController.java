package com.GRQ_proyMuni.demo.controller;

import com.GRQ_proyMuni.demo.entity.Reclamo;
import com.GRQ_proyMuni.demo.entity.Area;
import com.GRQ_proyMuni.demo.entity.Estado;
import com.GRQ_proyMuni.demo.entity.Respuesta;
import com.GRQ_proyMuni.demo.entity.Usuario;
import com.GRQ_proyMuni.demo.web.ReclamoForm;
import com.GRQ_proyMuni.demo.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/reclamos")
public class ReclamoController {

    @Autowired private ReclamoService reclamoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private TipoDocumentoService tipoDocumentoService;
    @Autowired private EstadoService estadoService;
    @Autowired private AreaService areaService;
    @Autowired private RespuestaService respuestaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;
    @Autowired private PasswordService passwordService;

    // Formulario para nuevo reclamo 
    @GetMapping("/nuevo")
    public String mostrarNuevoReclamo(Model model) {
        model.addAttribute("reclamoForm", new ReclamoForm());
        model.addAttribute("categorias", categoriaService.listarTodos());
        model.addAttribute("tipos", tipoDocumentoService.listarTodos());
        return "index"; 
    }

    // Guardar el reclamo enviado desde el formulario
    @PostMapping("/guardar")
    @Transactional
    public String guardarReclamo(@Valid @ModelAttribute ReclamoForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 HttpSession session) {
        if (bindingResult.hasErrors()) {
            return mostrarFormularioConError(model, form,
                    "Revisa los datos ingresados y corrige los campos inválidos.");
        }
        if (esVacio(form.getNumeroDocumento()) || esVacio(form.getNombre()) || esVacio(form.getApellidos())
                || esVacio(form.getCelular()) || esVacio(form.getCorreo()) || form.getFechaDocumento() == null
                || form.getFechaDocumento().isAfter(java.time.LocalDate.now())
                || esVacio(form.getAsunto()) || esVacio(form.getDescripcion())) {
            return mostrarFormularioConError(model, form, "Completa los campos obligatorios con datos válidos.");
        }
        if ("DNI".equalsIgnoreCase(form.getTipoDocumentoRemitente())
                && !form.getNumeroDocumento().matches("[0-9]{8}")) {
            return mostrarFormularioConError(model, form, "El DNI debe contener exactamente 8 dígitos.");
        }
        if (form.getArchivoEvidencia() == null || form.getArchivoEvidencia().isEmpty()
                || form.getArchivoEvidencia().getSize() > 10 * 1024 * 1024
                || !esPdf(form.getArchivoEvidencia())) {
            return mostrarFormularioConError(model, form,
                    "Adjunta un archivo PDF válido de máximo 10 MB.");
        }

        var categoria = form.getCategoriaId() == null ? java.util.Optional.<com.GRQ_proyMuni.demo.entity.Categoria>empty()
                : categoriaService.buscarPorId(form.getCategoriaId());
        var tipo = form.getTipoDocumentoId() == null ? java.util.Optional.<com.GRQ_proyMuni.demo.entity.TipoDocumento>empty()
                : tipoDocumentoService.buscarPorId(form.getTipoDocumentoId());
        if (categoria.isEmpty() || tipo.isEmpty()) {
            return mostrarFormularioConError(model, form, "Selecciona un tipo de documento y una categoría válidos.");
        }

        var usuarioExistente = usuarioService.buscarPorDni(form.getNumeroDocumento());
        Object sesionUsuario = session.getAttribute("authenticatedUserId");
        if (usuarioExistente.isPresent()
                && (!(sesionUsuario instanceof Integer usuarioId)
                    || !usuarioExistente.get().getIdUsuario().equals(usuarioId))) {
            return mostrarFormularioConError(model, form,
                    "Ese documento ya tiene una cuenta. Inicia sesión antes de presentar otro reclamo.");
        }
        if (usuarioExistente.isEmpty() && sesionUsuario instanceof Integer) {
            return mostrarFormularioConError(model, form,
                    "El documento del remitente debe coincidir con la cuenta que inició sesión.");
        }
        var usuarioPorCorreo = usuarioService.buscarPorCorreo(form.getCorreo());
        if (usuarioPorCorreo.isPresent() && (usuarioExistente.isEmpty()
                || !usuarioPorCorreo.get().getIdUsuario().equals(usuarioExistente.get().getIdUsuario()))) {
            return mostrarFormularioConError(model, form, "El correo ya está registrado con otro documento.");
        }

        Estado pendiente = estadoService.listarTodos().stream()
                .filter(estado -> "Pendiente".equalsIgnoreCase(estado.getNombreEstado()))
                .findFirst().orElseThrow(() -> new IllegalStateException("No existe el estado Pendiente."));
        Area areaAsignada = areaService.listarTodos().stream()
                .filter(area -> "Mesa de Partes Virtual".equalsIgnoreCase(area.getNombreArea()))
                .findFirst().orElseThrow(() -> new IllegalStateException("No existe el área Mesa de Partes Virtual."));
        String rutaEvidencia;
        try {
            Path directorio = Path.of("uploads", "reclamos");
            Files.createDirectories(directorio);
            Path archivo = directorio.resolve(UUID.randomUUID() + ".pdf");
            Files.write(archivo, form.getArchivoEvidencia().getBytes());
            rutaEvidencia = archivo.toString();
        } catch (IOException exception) {
            return mostrarFormularioConError(model, form, "No se pudo guardar el archivo adjunto. Inténtalo nuevamente.");
        }

        Usuario usuario = usuarioExistente.orElseGet(Usuario::new);
        usuario.setTipoDocumento(form.getTipoDocumentoRemitente());
        usuario.setNumeroDocumento(form.getNumeroDocumento());
        usuario.setNombre(form.getNombre());
        usuario.setApellidos(form.getApellidos());
        usuario.setCelular(form.getCelular());
        usuario.setCorreo(form.getCorreo());
        if (usuario.getIdUsuario() == null) {
            usuario.setPassword(passwordService.hash(UUID.randomUUID().toString()));
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setEstadoActivo(true);
            usuario.setRol(rolService.listarTodos().stream()
                    .filter(rol -> "Ciudadano".equalsIgnoreCase(rol.getNombreRol()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("No existe el rol Ciudadano.")));
            usuario.setArea(areaService.listarTodos().stream()
                    .filter(area -> "Ciudadanía".equalsIgnoreCase(area.getNombreArea()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("No existe el área Ciudadanía.")));
        }
        usuario = usuarioService.guardar(usuario);

        Reclamo reclamo = new Reclamo();
        reclamo.setUsuario(usuario);
        reclamo.setTipoDocumento(tipo.get());
        reclamo.setCategoria(categoria.get());
        reclamo.setEstado(pendiente);
        reclamo.setArea(areaAsignada);
        reclamo.setFechaRegistro(LocalDateTime.now());
        reclamo.setFechaDocumento(form.getFechaDocumento());
        reclamo.setNumeroDocumento(form.getNumeroDoc());
        reclamo.setAsunto(form.getAsunto());
        reclamo.setDescripcion(form.getDescripcion());
        reclamo.setObservaciones(form.getObservaciones());
        reclamo.setPrioridad("MEDIA");
        reclamo.setCodigoSeguimiento("EXP-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reclamo.setArchivoEvidencia(rutaEvidencia);
        reclamoService.guardar(reclamo);
        session.setAttribute("authenticatedUserId", usuario.getIdUsuario());
        return "redirect:/reclamos/detalle/" + reclamo.getIdReclamo();
    }

    // Ver mis reclamos 
    @GetMapping("/mis-reclamos")
    public String misReclamos(Model model, HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("authenticatedUserId");
        model.addAttribute("reclamos", reclamoService.buscarPorUsuario(usuarioId));
        return "mis-reclamos";
    }

    // Ver detalle de un reclamo 
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Integer id, Model model) {
        Reclamo reclamo = reclamoService.buscarPorId(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        model.addAttribute("reclamo", reclamo);
        model.addAttribute("respuestas", respuestaService.buscarPorReclamo(id));
        return "detalle-reclamo";
    }

    // Bandeja de entrada para el personal 
    @GetMapping("/bandeja")
    public String bandejaPersonal(Model model) {
        var reclamos = reclamoService.listarTodos();
        model.addAttribute("reclamos", reclamos);
        model.addAttribute("estados", estadoService.listarTodos());
        var activos = reclamos.stream()
                .filter(r -> !"Anulado".equalsIgnoreCase(r.getEstado().getNombreEstado())).toList();
        model.addAttribute("pendientes", activos.stream().filter(r -> "Pendiente".equalsIgnoreCase(r.getEstado().getNombreEstado())).count());
        model.addAttribute("enProceso", activos.stream().filter(r -> "En proceso".equalsIgnoreCase(r.getEstado().getNombreEstado())).count());
        model.addAttribute("resueltos", activos.stream().filter(r -> "Resuelto".equalsIgnoreCase(r.getEstado().getNombreEstado())).count());
        model.addAttribute("vencidos", activos.stream().filter(r -> r.getFechaVencimiento() != null
                && r.getFechaVencimiento().isBefore(LocalDateTime.now())
                && !"Resuelto".equalsIgnoreCase(r.getEstado().getNombreEstado())).count());
        return "bandeja";
    }

    @PostMapping("/{id}/anular")
    @Transactional
    public String anularReclamo(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Reclamo reclamo = reclamoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el reclamo."));
        Estado anulado = estadoService.listarTodos().stream()
                .filter(estado -> "Anulado".equalsIgnoreCase(estado.getNombreEstado()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el estado Anulado."));
        if ("Anulado".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())) {
            redirectAttributes.addFlashAttribute("mensaje", "El reclamo ya estaba anulado.");
            return "redirect:/reclamos/bandeja";
        }
        reclamo.setEstado(anulado);
        reclamoService.guardar(reclamo);
        redirectAttributes.addFlashAttribute("mensaje",
                "Reclamo anulado. El expediente y sus respuestas se conservaron.");
        return "redirect:/reclamos/bandeja";
    }

    // Pantalla de atención para el personal
    @GetMapping("/atencion/{id}")
    public String atenderReclamo(@PathVariable Integer id, Model model) {
        Reclamo reclamo = reclamoService.buscarPorId(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        if ("Anulado".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede atender un reclamo anulado.");
        }
        model.addAttribute("reclamo", reclamo);
        model.addAttribute("estados", estadoService.listarTodos());
        model.addAttribute("areas", areaService.listarTodos());
        return "atencion";
    }

    // Procesar la atención del reclamo (Guardar respuesta y cambiar estado)
    @PostMapping("/atencion/{id}")
    @Transactional
    public String guardarAtencion(@PathVariable Integer id,
                                  @RequestParam Integer estado,
                                  @RequestParam Integer area,
                                  @RequestParam String prioridad,
                                  @RequestParam String observacion,
                                  @RequestParam(required = false) MultipartFile archivo,
                                  HttpSession session) throws IOException {
        if (observacion.isBlank() || !java.util.List.of("ALTA", "MEDIA", "BAJA").contains(prioridad)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa una respuesta y prioridad válidas.");
        }
        Reclamo reclamo = reclamoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el reclamo."));
        if ("Anulado".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar un reclamo anulado.");
        }
        reclamo.setEstado(estadoService.buscarPorId(estado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un estado válido.")));
        reclamo.setArea(areaService.buscarPorId(area)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un área válida.")));
        reclamo.setPrioridad(prioridad);
        reclamo.setObservaciones(observacion);
        if ("Resuelto".equalsIgnoreCase(reclamo.getEstado().getNombreEstado())) {
            reclamo.setFechaCierre(LocalDateTime.now());
        } else {
            reclamo.setFechaCierre(null);
        }
        reclamoService.guardar(reclamo);
        Respuesta respuesta = new Respuesta();
        respuesta.setContenido(observacion);
        respuesta.setFechaRespuesta(LocalDateTime.now());
        respuesta.setEstadoRespuesta(reclamo.getEstado().getNombreEstado());
        respuesta.setArchivoAdjunto(guardarArchivoAtencion(archivo));
        respuesta.setReclamo(reclamo);
        Integer usuarioId = (Integer) session.getAttribute("authenticatedUserId");
        respuesta.setUsuario(usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
        respuestaService.guardar(respuesta);
        return "redirect:/reclamos/bandeja";
    }

    @GetMapping("/respuestas/{id}/archivo")
    public ResponseEntity<Resource> descargarAdjunto(@PathVariable Integer id) {
        Respuesta respuesta = respuestaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (respuesta.getArchivoAdjunto() == null || respuesta.getArchivoAdjunto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path archivo = Path.of(respuesta.getArchivoAdjunto());
        if (!Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El archivo adjunto ya no está disponible.");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.getFileName().toString()).build().toString())
                .body(new FileSystemResource(archivo));
    }

    private String mostrarFormularioConError(Model model, ReclamoForm form, String mensaje) {
        model.addAttribute("reclamoForm", form);
        model.addAttribute("categorias", categoriaService.listarTodos());
        model.addAttribute("tipos", tipoDocumentoService.listarTodos());
        model.addAttribute("error", mensaje);
        return "index";
    }

    private boolean esPdf(MultipartFile archivo) {
        try {
            byte[] bytes = archivo.getBytes();
            return bytes.length >= 5 && bytes[0] == '%'
                    && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-';
        } catch (IOException exception) {
            return false;
        }
    }

    private String guardarArchivoAtencion(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) return null;
        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no puede superar 10 MB.");
        }
        byte[] bytes = archivo.getBytes();
        String extension;
        if (bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D'
                && bytes[3] == 'F' && bytes[4] == '-') {
            extension = ".pdf";
        } else if (bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G') {
            extension = ".png";
        } else if (bytes.length >= 3 && bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8
                && bytes[2] == (byte) 0xff) {
            extension = ".jpg";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjunta un PDF o una imagen JPG/PNG válida.");
        }
        Path directory = Path.of("uploads", "respuestas");
        Files.createDirectories(directory);
        Path destination = directory.resolve(UUID.randomUUID() + extension);
        Files.write(destination, bytes);
        return destination.toString();
    }

    private boolean esVacio(String value) {
        return value == null || value.isBlank();
    }
}