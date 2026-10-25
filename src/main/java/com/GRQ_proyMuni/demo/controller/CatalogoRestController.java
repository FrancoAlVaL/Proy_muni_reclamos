package com.GRQ_proyMuni.demo.controller;

import com.GRQ_proyMuni.demo.entity.Area;
import com.GRQ_proyMuni.demo.entity.Categoria;
import com.GRQ_proyMuni.demo.entity.Estado;
import com.GRQ_proyMuni.demo.entity.Rol;
import com.GRQ_proyMuni.demo.entity.TipoDocumento;
import com.GRQ_proyMuni.demo.repository.HistorialReclamoRepository;
import com.GRQ_proyMuni.demo.repository.ReclamoRepository;
import com.GRQ_proyMuni.demo.repository.UsuarioRepository;
import com.GRQ_proyMuni.demo.service.AreaService;
import com.GRQ_proyMuni.demo.service.CategoriaService;
import com.GRQ_proyMuni.demo.service.EstadoService;
import com.GRQ_proyMuni.demo.service.RolService;
import com.GRQ_proyMuni.demo.service.TipoDocumentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoRestController {

    private final AreaService areaService;
    private final CategoriaService categoriaService;
    private final EstadoService estadoService;
    private final RolService rolService;
    private final TipoDocumentoService tipoDocumentoService;
    private final UsuarioRepository usuarioRepository;
    private final ReclamoRepository reclamoRepository;
    private final HistorialReclamoRepository historialReclamoRepository;

    public CatalogoRestController(AreaService areaService, CategoriaService categoriaService,
                                  EstadoService estadoService, RolService rolService,
                                  TipoDocumentoService tipoDocumentoService,
                                  UsuarioRepository usuarioRepository,
                                  ReclamoRepository reclamoRepository,
                                  HistorialReclamoRepository historialReclamoRepository) {
        this.areaService = areaService;
        this.categoriaService = categoriaService;
        this.estadoService = estadoService;
        this.rolService = rolService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.usuarioRepository = usuarioRepository;
        this.reclamoRepository = reclamoRepository;
        this.historialReclamoRepository = historialReclamoRepository;
    }

    @GetMapping("/areas")
    public List<Area> listarAreas() {
        return areaService.listarTodos();
    }

    @GetMapping("/areas/{id}")
    public Area obtenerArea(@PathVariable Integer id) {
        return areaService.buscarPorId(id).orElseThrow(() -> noEncontrado("Área"));
    }

    @PostMapping("/areas")
    public ResponseEntity<Area> crearArea(@Valid @RequestBody AreaPayload payload) {
        Area area = new Area();
        aplicar(area, payload);
        Area guardada = areaService.guardar(area);
        return creado("areas", guardada.getIdArea(), guardada);
    }

    @PutMapping("/areas/{id}")
    public Area actualizarArea(@PathVariable Integer id, @Valid @RequestBody AreaPayload payload) {
        Area area = areaService.buscarPorId(id).orElseThrow(() -> noEncontrado("Área"));
        aplicar(area, payload);
        return areaService.guardar(area);
    }

    @DeleteMapping("/areas/{id}")
    public ResponseEntity<Void> eliminarArea(@PathVariable Integer id) {
        areaService.buscarPorId(id).orElseThrow(() -> noEncontrado("Área"));
        if (usuarioRepository.existsByAreaIdArea(id) || reclamoRepository.existsByAreaIdArea(id)) {
            throw enUso("El área está asignada a usuarios o reclamos; puedes desactivarla.");
        }
        areaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return categoriaService.listarTodos();
    }

    @GetMapping("/categorias/{id}")
    public Categoria obtenerCategoria(@PathVariable Integer id) {
        return categoriaService.buscarPorId(id).orElseThrow(() -> noEncontrado("Categoría"));
    }

    @PostMapping("/categorias")
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody CategoriaPayload payload) {
        Categoria categoria = new Categoria();
        aplicar(categoria, payload);
        Categoria guardada = categoriaService.guardar(categoria);
        return creado("categorias", guardada.getIdCategoria(), guardada);
    }

    @PutMapping("/categorias/{id}")
    public Categoria actualizarCategoria(@PathVariable Integer id,
                                         @Valid @RequestBody CategoriaPayload payload) {
        Categoria categoria = categoriaService.buscarPorId(id)
                .orElseThrow(() -> noEncontrado("Categoría"));
        aplicar(categoria, payload);
        return categoriaService.guardar(categoria);
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.buscarPorId(id).orElseThrow(() -> noEncontrado("Categoría"));
        if (reclamoRepository.existsByCategoriaIdCategoria(id)) {
            throw enUso("La categoría está asignada a reclamos.");
        }
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estados")
    public List<Estado> listarEstados() {
        return estadoService.listarTodos();
    }

    @GetMapping("/estados/{id}")
    public Estado obtenerEstado(@PathVariable Integer id) {
        return estadoService.buscarPorId(id).orElseThrow(() -> noEncontrado("Estado"));
    }

    @PostMapping("/estados")
    public ResponseEntity<Estado> crearEstado(@Valid @RequestBody EstadoPayload payload) {
        Estado estado = new Estado();
        aplicar(estado, payload);
        Estado guardado = estadoService.guardar(estado);
        return creado("estados", guardado.getIdEstado(), guardado);
    }

    @PutMapping("/estados/{id}")
    public Estado actualizarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoPayload payload) {
        Estado estado = estadoService.buscarPorId(id).orElseThrow(() -> noEncontrado("Estado"));
        aplicar(estado, payload);
        return estadoService.guardar(estado);
    }

    @DeleteMapping("/estados/{id}")
    public ResponseEntity<Void> eliminarEstado(@PathVariable Integer id) {
        estadoService.buscarPorId(id).orElseThrow(() -> noEncontrado("Estado"));
        if (reclamoRepository.existsByEstadoIdEstado(id)
                || historialReclamoRepository.existsByEstadoIdEstado(id)) {
            throw enUso("El estado está asociado a reclamos o a su historial.");
        }
        estadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return rolService.listarTodos();
    }

    @GetMapping("/roles/{id}")
    public Rol obtenerRol(@PathVariable Integer id) {
        return rolService.buscarPorId(id).orElseThrow(() -> noEncontrado("Rol"));
    }

    @PostMapping("/roles")
    public ResponseEntity<Rol> crearRol(@Valid @RequestBody RolPayload payload) {
        Rol rol = new Rol();
        aplicar(rol, payload);
        Rol guardado = rolService.guardar(rol);
        return creado("roles", guardado.getIdRol(), guardado);
    }

    @PutMapping("/roles/{id}")
    public Rol actualizarRol(@PathVariable Integer id, @Valid @RequestBody RolPayload payload) {
        Rol rol = rolService.buscarPorId(id).orElseThrow(() -> noEncontrado("Rol"));
        aplicar(rol, payload);
        return rolService.guardar(rol);
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Integer id) {
        rolService.buscarPorId(id).orElseThrow(() -> noEncontrado("Rol"));
        if (usuarioRepository.existsByRolIdRol(id)) {
            throw enUso("El rol está asignado a usuarios.");
        }
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipos-documento")
    public List<TipoDocumento> listarTiposDocumento() {
        return tipoDocumentoService.listarTodos();
    }

    @GetMapping("/tipos-documento/{id}")
    public TipoDocumento obtenerTipoDocumento(@PathVariable Integer id) {
        return tipoDocumentoService.buscarPorId(id)
                .orElseThrow(() -> noEncontrado("Tipo de documento"));
    }

    @PostMapping("/tipos-documento")
    public ResponseEntity<TipoDocumento> crearTipoDocumento(
            @Valid @RequestBody TipoDocumentoPayload payload) {
        TipoDocumento tipoDocumento = new TipoDocumento();
        aplicar(tipoDocumento, payload);
        TipoDocumento guardado = tipoDocumentoService.guardar(tipoDocumento);
        return creado("tipos-documento", guardado.getIdTipoDocumento(), guardado);
    }

    @PutMapping("/tipos-documento/{id}")
    public TipoDocumento actualizarTipoDocumento(@PathVariable Integer id,
                                                 @Valid @RequestBody TipoDocumentoPayload payload) {
        TipoDocumento tipoDocumento = tipoDocumentoService.buscarPorId(id)
                .orElseThrow(() -> noEncontrado("Tipo de documento"));
        aplicar(tipoDocumento, payload);
        return tipoDocumentoService.guardar(tipoDocumento);
    }

    @DeleteMapping("/tipos-documento/{id}")
    public ResponseEntity<Void> eliminarTipoDocumento(@PathVariable Integer id) {
        tipoDocumentoService.buscarPorId(id).orElseThrow(() -> noEncontrado("Tipo de documento"));
        if (reclamoRepository.existsByTipoDocumentoIdTipoDocumento(id)) {
            throw enUso("El tipo de documento está asignado a reclamos.");
        }
        tipoDocumentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private static void aplicar(Area area, AreaPayload payload) {
        area.setNombreArea(payload.nombreArea().trim());
        area.setDescripcion(normalizarDescripcion(payload.descripcion()));
        area.setEstadoActivo(payload.estadoActivo());
    }

    private static void aplicar(Categoria categoria, CategoriaPayload payload) {
        categoria.setNombreCategoria(payload.nombreCategoria().trim());
        categoria.setDescripcion(normalizarDescripcion(payload.descripcion()));
        categoria.setEstadoActivo(payload.estadoActivo());
    }

    private static void aplicar(Estado estado, EstadoPayload payload) {
        estado.setNombreEstado(payload.nombreEstado().trim());
        estado.setDescripcion(normalizarDescripcion(payload.descripcion()));
        estado.setOrdenSecuencia(payload.ordenSecuencia());
    }

    private static void aplicar(Rol rol, RolPayload payload) {
        rol.setNombreRol(payload.nombreRol().trim());
        rol.setDescripcion(normalizarDescripcion(payload.descripcion()));
    }

    private static void aplicar(TipoDocumento tipoDocumento, TipoDocumentoPayload payload) {
        tipoDocumento.setNombreTipo(payload.nombreTipo().trim());
        tipoDocumento.setDescripcion(normalizarDescripcion(payload.descripcion()));
    }

    private static String normalizarDescripcion(String descripcion) {
        if (descripcion == null) return null;
        String valor = descripcion.trim();
        return valor.isEmpty() ? null : valor;
    }

    private static <T> ResponseEntity<T> creado(String recurso, Integer id, T cuerpo) {
        return ResponseEntity.created(URI.create("/api/v1/catalogos/" + recurso + "/" + id)).body(cuerpo);
    }

    private static ResponseStatusException noEncontrado(String recurso) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " no encontrado.");
    }

    private static ResponseStatusException enUso(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }

    public record AreaPayload(
            @NotBlank @Size(max = 100) String nombreArea,
            @Size(max = 255) String descripcion,
            @NotNull Boolean estadoActivo) {
    }

    public record CategoriaPayload(
            @NotBlank @Size(max = 100) String nombreCategoria,
            @Size(max = 255) String descripcion,
            @NotNull Boolean estadoActivo) {
    }

    public record EstadoPayload(
            @NotBlank @Size(max = 100) String nombreEstado,
            @Size(max = 255) String descripcion,
            @Min(1) Integer ordenSecuencia) {
    }

    public record RolPayload(
            @NotBlank @Size(max = 100) String nombreRol,
            @Size(max = 255) String descripcion) {
    }

    public record TipoDocumentoPayload(
            @NotBlank @Size(max = 100) String nombreTipo,
            @Size(max = 255) String descripcion) {
    }
}
