package com.pasteleria.Controller;

import com.pasteleria.Entity.Categoria;
import com.pasteleria.service.CategoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author Cristóbal Pérez
 */
@RestController
@RequestMapping("/api/v1/categorias")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@Tag(name = "Categorias", description = "CRUD de categorías de productos (JWT + roles)")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    // ================== SOLO ADMIN ==================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Categoria> addCategoria(@RequestBody Categoria c) {
        c.setId(null);
        Categoria creada = service.saveCategoria(c);
        return ResponseEntity.ok(creada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lote")
    public ResponseEntity<List<Categoria>> addCategorias(@RequestBody List<Categoria> categorias) {
        categorias.forEach(cat -> cat.setId(null));
        List<Categoria> creadas = service.saveCategorias(categorias);
        return ResponseEntity.ok(creadas);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Integer id) {
        service.deleteCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> updateCategoria(@PathVariable Integer id, @RequestBody Categoria c) {
        Categoria actualizada = service.updateCategoria(id, c);
        return (actualizada == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(actualizada);
    }

    // ================== TIENDA (PÚBLICO) ==================

    // Invitado puede ver categorías
    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorias() {
        return ResponseEntity.ok(service.getCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable Integer id) {
        Categoria c = service.getCategoriaById(id);
        return (c == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Categoria> getCategoriaByNombre(@PathVariable String nombre) {
        Categoria c = service.getCategoriaByNombre(nombre);
        return (c == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }
}
