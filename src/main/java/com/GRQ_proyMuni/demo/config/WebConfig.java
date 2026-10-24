package com.GRQ_proyMuni.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns(
                "/admin/**", "/reclamos/bandeja", "/reclamos/atencion/**", "/reclamos/*/anular",
                "/reclamos/mis-reclamos", "/reclamos/detalle/**",
                "/reclamos/respuestas/**",
                "/usuarios/lista", "/usuarios", "/usuarios/*/estado",
                "/usuarios/*/editar");
    }
}
