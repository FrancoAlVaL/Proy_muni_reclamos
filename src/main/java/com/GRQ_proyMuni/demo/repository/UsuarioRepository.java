package com.GRQ_proyMuni.demo.repository;

import com.GRQ_proyMuni.demo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Buscar usuario por correo (útil para el login)
    Optional<Usuario> findByCorreo(String correo);
    
    // Buscar usuario por DNI
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    boolean existsByAreaIdArea(Integer idArea);

    boolean existsByRolIdRol(Integer idRol);
}