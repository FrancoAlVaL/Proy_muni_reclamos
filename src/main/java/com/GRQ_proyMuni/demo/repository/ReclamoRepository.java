package com.GRQ_proyMuni.demo.repository;

import com.GRQ_proyMuni.demo.entity.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {
    // Buscar reclamos por el usuario que lo creó
    List<Reclamo> findByUsuarioIdUsuario(Integer idUsuario);
    
    // Buscar reclamos por su estado actual
    List<Reclamo> findByEstadoIdEstado(Integer idEstado);

    boolean existsByAreaIdArea(Integer idArea);

    boolean existsByCategoriaIdCategoria(Integer idCategoria);

    boolean existsByEstadoIdEstado(Integer idEstado);

    boolean existsByTipoDocumentoIdTipoDocumento(Integer idTipoDocumento);

    Optional<Reclamo> findByCodigoSeguimientoAndUsuarioNumeroDocumento(String codigoSeguimiento,
                                                                        String numeroDocumento);
}