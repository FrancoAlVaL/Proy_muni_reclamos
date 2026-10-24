package com.GRQ_proyMuni.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "tipo_documento", length = 50)
    @NotBlank(message = "Selecciona un tipo de documento.")
    private String tipoDocumento;

    @Column(name = "numero_documento", length = 30)
    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(max = 30, message = "El documento no puede superar 30 caracteres.")
    private String numeroDocumento;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres.")
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 150)
    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 150, message = "Los apellidos no pueden superar 150 caracteres.")
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 30)
    private String genero;

    @Column(name = "correo", nullable = false, length = 150)
    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
    private String correo;

    @Column(name = "celular", length = 20)
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres.")
    private String celular;

    @Column(name = "direccion", length = 255)
    @Size(max = 255, message = "La dirección no puede superar 255 caracteres.")
    private String direccion;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String password;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "estado_activo", nullable = false)
    private Boolean estadoActivo = true;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "id_area", nullable = false)
    private Area area;

    public Usuario() {
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}