package com.GRQ_proyMuni.demo.config;

import com.GRQ_proyMuni.demo.entity.Area;
import com.GRQ_proyMuni.demo.entity.Categoria;
import com.GRQ_proyMuni.demo.entity.Estado;
import com.GRQ_proyMuni.demo.entity.Rol;
import com.GRQ_proyMuni.demo.entity.TipoDocumento;
import com.GRQ_proyMuni.demo.entity.Usuario;
import com.GRQ_proyMuni.demo.service.AreaService;
import com.GRQ_proyMuni.demo.service.CategoriaService;
import com.GRQ_proyMuni.demo.service.EstadoService;
import com.GRQ_proyMuni.demo.service.PasswordService;
import com.GRQ_proyMuni.demo.service.RolService;
import com.GRQ_proyMuni.demo.service.TipoDocumentoService;
import com.GRQ_proyMuni.demo.service.UsuarioService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class CatalogInitializer implements ApplicationRunner {

    private final AreaService areaService;
    private final CategoriaService categoriaService;
    private final EstadoService estadoService;
    private final RolService rolService;
    private final TipoDocumentoService tipoDocumentoService;
    private final UsuarioService usuarioService;
    private final PasswordService passwordService;
    private final String adminEmail;
    private final String adminPassword;

    public CatalogInitializer(AreaService areaService, CategoriaService categoriaService,
                              EstadoService estadoService, RolService rolService,
                              TipoDocumentoService tipoDocumentoService, UsuarioService usuarioService,
                              PasswordService passwordService,
                              @Value("${app.admin.email:}") String adminEmail,
                              @Value("${app.admin.password:}") String adminPassword) {
        this.areaService = areaService;
        this.categoriaService = categoriaService;
        this.estadoService = estadoService;
        this.rolService = rolService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.usuarioService = usuarioService;
        this.passwordService = passwordService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureArea("Mesa de Partes Virtual");
        ensureArea("Ciudadanía");
        ensureCategory("Reclamo");
        ensureCategory("Queja");
        ensureCategory("Sugerencia");
        ensureState("Pendiente", 1);
        ensureState("En proceso", 2);
        ensureState("Resuelto", 3);
        ensureState("Anulado", 4);
        ensureType("Solicitud");
        ensureType("Carta");
        ensureRole("Ciudadano");
        ensureRole("Operador");
        ensureRole("Administrador");
        ensureInitialAdministrator();
    }

    private void ensureArea(String name) {
        if (areaService.listarTodos().stream().noneMatch(item -> same(item.getNombreArea(), name))) {
            Area area = new Area();
            area.setNombreArea(name);
            area.setEstadoActivo(true);
            areaService.guardar(area);
        }
    }

    private void ensureCategory(String name) {
        if (categoriaService.listarTodos().stream().noneMatch(item -> same(item.getNombreCategoria(), name))) {
            Categoria category = new Categoria();
            category.setNombreCategoria(name);
            category.setEstadoActivo(true);
            categoriaService.guardar(category);
        }
    }

    private void ensureState(String name, int order) {
        if (estadoService.listarTodos().stream().noneMatch(item -> same(item.getNombreEstado(), name))) {
            Estado state = new Estado();
            state.setNombreEstado(name);
            state.setOrdenSecuencia(order);
            estadoService.guardar(state);
        }
    }

    private void ensureType(String name) {
        if (tipoDocumentoService.listarTodos().stream().noneMatch(item -> same(item.getNombreTipo(), name))) {
            TipoDocumento type = new TipoDocumento();
            type.setNombreTipo(name);
            tipoDocumentoService.guardar(type);
        }
    }

    private void ensureRole(String name) {
        if (rolService.listarTodos().stream().noneMatch(item -> same(item.getNombreRol(), name))) {
            Rol role = new Rol();
            role.setNombreRol(name);
            rolService.guardar(role);
        }
    }

    private void ensureInitialAdministrator() {
        if (adminEmail.isBlank() && adminPassword.isBlank()) return;
        if (adminEmail.isBlank() || adminPassword.length() < 8) {
            throw new IllegalStateException("Configura APP_ADMIN_EMAIL y una APP_ADMIN_PASSWORD de al menos 8 caracteres.");
        }
        if (usuarioService.buscarPorCorreo(adminEmail).isPresent()) return;

        Usuario admin = new Usuario();
        admin.setTipoDocumento("N/A");
        admin.setNumeroDocumento("ADMIN-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        admin.setNombre("Administrador");
        admin.setApellidos("Sistema");
        admin.setCorreo(adminEmail);
        admin.setPassword(passwordService.hash(adminPassword));
        admin.setFechaRegistro(LocalDateTime.now());
        admin.setEstadoActivo(true);
        admin.setRol(rolService.listarTodos().stream()
                .filter(role -> same(role.getNombreRol(), "Administrador"))
                .findFirst().orElseThrow());
        admin.setArea(areaService.listarTodos().stream()
                .filter(area -> same(area.getNombreArea(), "Mesa de Partes Virtual"))
                .findFirst().orElseThrow());
        usuarioService.guardar(admin);
    }

    private boolean same(String left, String right) {
        return left != null && left.toLowerCase(Locale.ROOT).equals(right.toLowerCase(Locale.ROOT));
    }
}
