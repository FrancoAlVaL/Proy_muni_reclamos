package com.GRQ_proyMuni.demo.repository;

import com.GRQ_proyMuni.demo.entity.HistorialReclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialReclamoRepository extends JpaRepository<HistorialReclamo, Integer> {
    // Método personalizado para obtener el historial de un reclamo ordenado por fecha
    List<HistorialReclamo> findByReclamoIdReclamoOrderByFechaCambioDesc(Integer idReclamo);

    boolean existsByEstadoIdEstado(Integer idEstado);
}