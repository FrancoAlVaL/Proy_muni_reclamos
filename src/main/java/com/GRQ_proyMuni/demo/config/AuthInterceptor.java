package com.GRQ_proyMuni.demo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.GRQ_proyMuni.demo.entity.Usuario;
import com.GRQ_proyMuni.demo.service.ReclamoService;
import com.GRQ_proyMuni.demo.service.RespuestaService;
import com.GRQ_proyMuni.demo.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UsuarioService usuarioService;
    private final ReclamoService reclamoService;
    private final RespuestaService respuestaService;

    public AuthInterceptor(UsuarioService usuarioService, ReclamoService reclamoService,
                           RespuestaService respuestaService) {
        this.usuarioService = usuarioService;
        this.reclamoService = reclamoService;
        this.respuestaService = respuestaService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean apiRequest = path.startsWith("/api/");
        boolean editarUsuario = path.matches("/usuarios/\\d+/editar");
        
        boolean personalOnly = path.startsWith("/admin/")
                || path.equals("/reclamos/bandeja")
                || path.startsWith("/reclamos/atencion/")
                || (path.matches("/reclamos/\\d+/anular")
                    && "POST".equalsIgnoreCase(request.getMethod()))
                || path.equals("/usuarios/lista")
                || (editarUsuario && ("GET".equalsIgnoreCase(request.getMethod())
                    || "POST".equalsIgnoreCase(request.getMethod())))
                || ((path.equals("/usuarios") || path.startsWith("/usuarios/"))
                    && "POST".equalsIgnoreCase(request.getMethod())
                    && !path.equals("/usuarios/registro"));
                    
        boolean adminOnly = path.equals("/usuarios/lista")
                || (editarUsuario && ("GET".equalsIgnoreCase(request.getMethod())
                    || "POST".equalsIgnoreCase(request.getMethod())))
                || ((path.equals("/usuarios") || path.startsWith("/usuarios/"))
                    && "POST".equalsIgnoreCase(request.getMethod())
                    && !path.equals("/usuarios/registro"))
                || apiRequest;
                    
        boolean loginRequired = personalOnly || path.equals("/reclamos/mis-reclamos")
                || path.startsWith("/reclamos/detalle/")
                || path.startsWith("/reclamos/respuestas/")
                || apiRequest;

        if (!loginRequired) return true;

        HttpSession session = request.getSession(false);
        Object sessionUserId = session == null ? null : session.getAttribute("authenticatedUserId");
        
        if (!(sessionUserId instanceof Integer userId)) {
            if (apiRequest) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }

        Usuario usuario = usuarioService.buscarPorId(userId).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getEstadoActivo())) {
            if (session != null) session.invalidate();
            if (apiRequest) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }

        boolean personal = esPersonal(usuario);
        if (personalOnly && !personal) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (adminOnly && !esAdministrador(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        if (!personal && path.startsWith("/reclamos/detalle/")) {
            String rawId = path.substring("/reclamos/detalle/".length());
            try {
                Integer reclamoId = Integer.valueOf(rawId);
                boolean esPropietario = reclamoService.buscarPorId(reclamoId)
                        .map(reclamo -> reclamo.getUsuario().getIdUsuario().equals(userId)).orElse(false);
                if (!esPropietario) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return false;
                }
            } catch (NumberFormatException exception) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
        }


        if (!personal && path.startsWith("/reclamos/respuestas/")) {
            String rawRespuestaId = path.substring("/reclamos/respuestas/".length()).split("/", 2)[0];
            try {
                Integer respuestaId = Integer.valueOf(rawRespuestaId);
                boolean esPropietarioRespuesta = respuestaService.buscarPorId(respuestaId)
                        .map(respuesta -> respuesta.getReclamo().getUsuario().getIdUsuario().equals(userId))
                        .orElse(false);
                if (!esPropietarioRespuesta) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return false;
                }
            } catch (NumberFormatException exception) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
        }

        return true;
    }

    private boolean esPersonal(Usuario usuario) {
        String nombreRol = usuario.getRol() == null ? "" : usuario.getRol().getNombreRol();
        return "Administrador".equalsIgnoreCase(nombreRol) || "Operador".equalsIgnoreCase(nombreRol)
                || "Revisor".equalsIgnoreCase(nombreRol);
    }

    private boolean esAdministrador(Usuario usuario) {
        String nombreRol = usuario.getRol() == null ? "" : usuario.getRol().getNombreRol();
        return "Administrador".equalsIgnoreCase(nombreRol);
    }
}