package com.GRQ_proyMuni.demo;

import com.GRQ_proyMuni.demo.entity.Usuario;
import com.GRQ_proyMuni.demo.web.ReclamoForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidUserRegistrationFields() {
        Usuario usuario = new Usuario();
        usuario.setNombre(" ");
        usuario.setApellidos("Pérez");
        usuario.setTipoDocumento("DNI");
        usuario.setNumeroDocumento("12345678");
        usuario.setCorreo("correo-invalido");
        usuario.setCelular("987654321");
        usuario.setDireccion("Calle 1");
        usuario.setPassword("corta");

        var violations = validator.validate(usuario);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(error -> error.getPropertyPath().toString().equals("nombre")));
        assertTrue(violations.stream().anyMatch(error -> error.getPropertyPath().toString().equals("correo")));
        assertTrue(violations.stream().anyMatch(error -> error.getPropertyPath().toString().equals("password")));
    }

    @Test
    void rejectsFutureDocumentDatesAndInvalidEmailInComplaintForm() {
        ReclamoForm form = new ReclamoForm();
        form.setTipoDocumentoRemitente("DNI");
        form.setNumeroDocumento("12345678");
        form.setNombre("Ana");
        form.setApellidos("Pérez");
        form.setCelular("987654321");
        form.setCorreo("correo-invalido");
        form.setTipoDocumentoId(1);
        form.setCategoriaId(1);
        form.setFechaDocumento(LocalDate.now().plusDays(1));
        form.setAsunto("Asunto");
        form.setDescripcion("Descripción");

        var violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(error -> error.getPropertyPath().toString().equals("correo")));
        assertTrue(violations.stream().anyMatch(error -> error.getPropertyPath().toString().equals("fechaDocumento")));
    }

    @Test
    void acceptsAutomaticallyCreatedAdministratorWithoutPhoneOrAddress() {
        Usuario administrador = new Usuario();
        administrador.setTipoDocumento("N/A");
        administrador.setNumeroDocumento("ADMIN-12345678");
        administrador.setNombre("Administrador");
        administrador.setApellidos("Sistema");
        administrador.setCorreo("admin@muni.gob.pe");
        administrador.setPassword("pbkdf2$hash");
        administrador.setFechaRegistro(java.time.LocalDateTime.now());
        administrador.setEstadoActivo(true);

        var violations = validator.validate(administrador);

        assertTrue(violations.isEmpty());
    }
}
