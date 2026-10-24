package com.GRQ_proyMuni.demo.controller;

import com.GRQ_proyMuni.demo.entity.Usuario;
import com.GRQ_proyMuni.demo.service.UsuarioService;
import com.GRQ_proyMuni.demo.service.RolService;
import com.GRQ_proyMuni.demo.service.AreaService;
import com.GRQ_proyMuni.demo.service.PasswordService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private Validator validator;

    // Mostrar formulario de registro 
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolService.listarTodos());
        return "registro";
    }

    // Procesar el registro de un nuevo usuario
    @PostMapping("/registro")
    public String guardarUsuario(@Valid @ModelAttribute Usuario usuario,
                                 BindingResult bindingResult,
                                 @RequestParam String passwordConfirm,
                                 @RequestParam(required = false) String terms,
                                 HttpSession session,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Revisa los datos ingresados y corrige los campos inválidos.");
            return "registro";
        }
        if (usuario.getCelular() == null || usuario.getCelular().isBlank()
                || usuario.getDireccion() == null || usuario.getDireccion().isBlank()) {
            model.addAttribute("error", "El teléfono y la dirección son obligatorios.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }
        if (usuario.getFechaNacimiento() == null) {
            model.addAttribute("error", "La fecha de nacimiento es obligatoria.");
            return "registro";
        }
        if (!"on".equals(terms)) {
            model.addAttribute("error", "Debes aceptar los términos y condiciones.");
            return "registro";
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 8
                || !usuario.getPassword().equals(passwordConfirm)) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres y coincidir con la confirmación.");
            return "registro";
        }
        var usuarioPorDni = usuarioService.buscarPorDni(usuario.getNumeroDocumento());
        var usuarioPorCorreo = usuarioService.buscarPorCorreo(usuario.getCorreo());
        Object sesionUsuario = session.getAttribute("authenticatedUserId");
        if (usuarioPorDni.isPresent()
                && (!(sesionUsuario instanceof Integer usuarioId)
                    || !usuarioPorDni.get().getIdUsuario().equals(usuarioId))) {
            model.addAttribute("error", "Ese documento ya tiene una cuenta. Inicia sesión para modificarla.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }
        if (usuarioPorDni.isEmpty() && sesionUsuario instanceof Integer) {
            model.addAttribute("error", "Cierra sesión antes de registrar otra cuenta.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }
        if (usuarioPorCorreo.isPresent() && (usuarioPorDni.isEmpty()
                || !usuarioPorCorreo.get().getIdUsuario().equals(usuarioPorDni.get().getIdUsuario()))) {
            model.addAttribute("error", "Ese correo ya está registrado. Inicia sesión o utiliza otro correo.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }
        if (usuarioPorDni.isPresent()
                && !usuarioPorDni.get().getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
            model.addAttribute("error", "El correo debe coincidir con el registrado para ese documento.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }
        String passwordHash = passwordService.hash(usuario.getPassword());
        if (usuarioPorDni.isPresent()) {
            Usuario registrado = usuarioPorDni.get();
            registrado.setTipoDocumento(usuario.getTipoDocumento());
            registrado.setNumeroDocumento(usuario.getNumeroDocumento());
            registrado.setNombre(usuario.getNombre());
            registrado.setApellidos(usuario.getApellidos());
            registrado.setFechaNacimiento(usuario.getFechaNacimiento());
            registrado.setGenero(usuario.getGenero());
            registrado.setCorreo(usuario.getCorreo());
            registrado.setCelular(usuario.getCelular());
            registrado.setDireccion(usuario.getDireccion());
            usuario = registrado;
        }
        usuario.setPassword(passwordHash);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstadoActivo(true);
        if (usuario.getRol() == null) {
            usuario.setRol(rolService.listarTodos().stream()
                    .filter(rol -> "Ciudadano".equalsIgnoreCase(rol.getNombreRol()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("No existe el rol Ciudadano.")));
        }
        if (usuario.getArea() == null) {
            usuario.setArea(areaService.listarTodos().stream()
                    .filter(area -> "Ciudadanía".equalsIgnoreCase(area.getNombreArea()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("No existe el área Ciudadanía.")));
        }
        usuarioService.guardar(usuario);
        return "redirect:/login";
    }

    // Lista de usuarios para el administrador
    @GetMapping("/lista")
    public String listarUsuarios(Model model) {
        var usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", rolService.listarTodos());
        model.addAttribute("areas", areaService.listarTodos());
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("activos", usuarios.stream().filter(u -> Boolean.TRUE.equals(u.getEstadoActivo())).count());
        model.addAttribute("inactivos", usuarios.stream().filter(u -> !Boolean.TRUE.equals(u.getEstadoActivo())).count());
        model.addAttribute("online", 0);
        return "usuarios";
    }

    @GetMapping("/{id}/editar")
    public String mostrarEdicionUsuario(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.listarTodos());
        model.addAttribute("areas", areaService.listarTodos());
        return "editar-usuario";
    }

    @PostMapping("/{id}/editar")
    public String actualizarUsuario(@PathVariable Integer id,
                                    @RequestParam String tipoDocumento,
                                    @RequestParam String numeroDocumento,
                                    @RequestParam String nombre,
                                    @RequestParam String apellidos,
                                    @RequestParam String correo,
                                    @RequestParam(required = false) String celular,
                                    @RequestParam Integer rolId,
                                    @RequestParam Integer areaId,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        String correoNormalizado = correo.trim();
        boolean correoDuplicado = usuarioService.buscarPorCorreo(correoNormalizado)
                .filter(otro -> !otro.getIdUsuario().equals(id)).isPresent();
        boolean documentoDuplicado = usuarioService.buscarPorDni(numeroDocumento.trim())
                .filter(otro -> !otro.getIdUsuario().equals(id)).isPresent();
        if (correoDuplicado || documentoDuplicado) {
            redirectAttributes.addFlashAttribute("error", "El correo o documento ya está registrado.");
            return "redirect:/usuarios/" + id + "/editar";
        }

        usuario.setTipoDocumento(tipoDocumento.trim());
        usuario.setNumeroDocumento(numeroDocumento.trim());
        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setCelular(celular == null || celular.isBlank() ? null : celular.trim());
        usuario.setRol(rolService.buscarPorId(rolId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Selecciona un rol válido.")));
        usuario.setArea(areaService.buscarPorId(areaId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Selecciona un área válida.")));

        if (!validator.validate(usuario).isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Revisa los datos ingresados. Verifica los campos obligatorios, el correo y sus longitudes.");
            return "redirect:/usuarios/" + id + "/editar";
        }

        usuarioService.guardar(usuario);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        return "redirect:/usuarios/lista";
    }

    @PostMapping
    public String crearUsuarioAdministrativo(@Valid @ModelAttribute Usuario usuario,
                                             BindingResult bindingResult,
                                             @RequestParam String passwordConfirm,
                                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    "Revisa los datos ingresados y corrige los campos inválidos.");
            return "redirect:/usuarios/lista";
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 8
                || !usuario.getPassword().equals(passwordConfirm)) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y coincidir.");
        }
        if (usuarioService.buscarPorCorreo(usuario.getCorreo()).isPresent()
                || usuarioService.buscarPorDni(usuario.getNumeroDocumento()).isPresent()) {
            throw new IllegalArgumentException("El correo o documento ya está registrado.");
        }
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstadoActivo(true);
        usuario.setPassword(passwordService.hash(usuario.getPassword()));
        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null
                || usuario.getArea() == null || usuario.getArea().getIdArea() == null) {
            throw new IllegalArgumentException("Selecciona un rol y un área válidos.");
        }
        usuario.setRol(rolService.listarTodos().stream()
                .filter(rol -> rol.getIdRol().equals(usuario.getRol().getIdRol()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no existe.")));
        usuario.setArea(areaService.listarTodos().stream()
                .filter(area -> area.getIdArea().equals(usuario.getArea().getIdArea()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("El área seleccionada no existe.")));
        usuarioService.guardar(usuario);
        return "redirect:/usuarios/lista";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstadoUsuario(@PathVariable Integer id) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario."));
        usuario.setEstadoActivo(!Boolean.TRUE.equals(usuario.getEstadoActivo()));
        usuarioService.guardar(usuario);
        return "redirect:/usuarios/lista";
    }
}