package com.pasteleria.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 *
 * @author Cristóbal Pérez
 */
@Entity
@Table(name = "PRODUCTO")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    // 🔗 Relación MANY-TO-ONE con Categoria
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    @Column(name = "precio_clp")
    private Integer precio_clp;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    // 🔥 BASE64 largo → CLOB (OBLIGATORIO)
    @Lob
    @Column(name = "imagen", columnDefinition = "CLOB")
    private String imagen;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "activo")
    private Boolean activo = true;

    // ===== Constructor vacío requerido por JPA =====
    public Producto() {}

    // ===== Getters y Setters =====
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Integer getPrecio_clp() {
        return precio_clp;
    }

    public void setPrecio_clp(Integer precio_clp) {
        this.precio_clp = precio_clp;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", categoria=" + (categoria != null ? categoria.getNombre() : "null") +
                ", precio_clp=" + precio_clp +
                ", descripcion='" + descripcion + '\'' +
                ", imagen='" + (imagen != null ? "[BASE64]" : "null") + '\'' +
                ", stock=" + stock +
                ", activo=" + activo +
                '}';
    }
}
