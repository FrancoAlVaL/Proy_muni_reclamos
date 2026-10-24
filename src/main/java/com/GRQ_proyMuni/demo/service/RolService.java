package com.GRQ_proyMuni.demo.service;

import com.GRQ_proyMuni.demo.entity.Rol;
import com.GRQ_proyMuni.demo.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    public Optional<Rol> buscarPorId(Integer id) {
        return rolRepository.findById(id);
    }

    public Rol guardar(Rol rol) {
        return rolRepository.save(rol);
    }

    public void eliminar(Integer id) {
        rolRepository.deleteById(id);
    }
}