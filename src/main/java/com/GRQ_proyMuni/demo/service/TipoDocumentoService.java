package com.GRQ_proyMuni.demo.service;

import com.GRQ_proyMuni.demo.entity.TipoDocumento;
import com.GRQ_proyMuni.demo.repository.TipoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TipoDocumentoService {

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    public List<TipoDocumento> listarTodos() {
        return tipoDocumentoRepository.findAll();
    }

    public Optional<TipoDocumento> buscarPorId(Integer id) {
        return tipoDocumentoRepository.findById(id);
    }

    public TipoDocumento guardar(TipoDocumento tipoDocumento) {
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public void eliminar(Integer id) {
        tipoDocumentoRepository.deleteById(id);
    }
}