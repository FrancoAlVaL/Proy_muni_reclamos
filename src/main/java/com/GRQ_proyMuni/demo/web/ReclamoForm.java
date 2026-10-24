package com.GRQ_proyMuni.demo.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class ReclamoForm {

    @NotBlank(message = "Selecciona un tipo de documento.")
    private String tipoDocumentoRemitente;
    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(max = 30, message = "El documento no puede superar 30 caracteres.")
    private String numeroDocumento;
    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres.")
    private String nombre;
    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 150, message = "Los apellidos no pueden superar 150 caracteres.")
    private String apellidos;
    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres.")
    private String celular;
    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
    private String correo;
    @NotNull(message = "Selecciona un tipo de documento.")
    private Integer tipoDocumentoId;
    @NotNull(message = "Selecciona una categoría.")
    private Integer categoriaId;
    @Size(max = 30, message = "El número de documento no puede superar 30 caracteres.")
    private String numeroDoc;
    @NotNull(message = "La fecha del documento es obligatoria.")
    @PastOrPresent(message = "La fecha del documento no puede ser futura.")
    private LocalDate fechaDocumento;
    @NotBlank(message = "El asunto es obligatorio.")
    @Size(max = 200, message = "El asunto no puede superar 200 caracteres.")
    private String asunto;
    @NotBlank(message = "La descripción es obligatoria.")
    private String descripcion;
    @Size(max = 120, message = "Las observaciones no pueden superar 120 caracteres.")
    private String observaciones;
    private MultipartFile archivoEvidencia;

    public String getTipoDocumentoRemitente() { return tipoDocumentoRemitente; }
    public void setTipoDocumentoRemitente(String tipoDocumentoRemitente) { this.tipoDocumentoRemitente = tipoDocumentoRemitente; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public Integer getTipoDocumentoId() { return tipoDocumentoId; }
    public void setTipoDocumentoId(Integer tipoDocumentoId) { this.tipoDocumentoId = tipoDocumentoId; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public String getNumeroDoc() { return numeroDoc; }
    public void setNumeroDoc(String numeroDoc) { this.numeroDoc = numeroDoc; }
    public LocalDate getFechaDocumento() { return fechaDocumento; }
    public void setFechaDocumento(LocalDate fechaDocumento) { this.fechaDocumento = fechaDocumento; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public MultipartFile getArchivoEvidencia() { return archivoEvidencia; }
    public void setArchivoEvidencia(MultipartFile archivoEvidencia) { this.archivoEvidencia = archivoEvidencia; }
}
