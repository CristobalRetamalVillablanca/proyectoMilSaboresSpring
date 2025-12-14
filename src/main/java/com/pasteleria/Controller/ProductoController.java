package com.pasteleria.Controller;

import com.pasteleria.Entity.Producto;
import com.pasteleria.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Cristóbal Pérez
 */
@RestController
@RequestMapping("/api/v1/productos")
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
@Tag(name = "Productos", description = "Operaciones CRUD de productos (JWT + roles)")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    // ================== SOLO ADMIN ==================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Producto> addProducto(@RequestBody Producto p) {
        p.setId(null);
        Producto creado = service.saveProducto(p);
        return ResponseEntity.ok(creado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lote")
    public ResponseEntity<List<Producto>> addProductos(@RequestBody List<Producto> productos) {
        productos.forEach(prod -> prod.setId(null));
        List<Producto> creados = service.saveProductos(productos);
        return ResponseEntity.ok(creados);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Integer id) {
        service.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Integer id, @RequestBody Producto p) {
        p.setId(id);
        Producto actualizado = service.updateProducto(id, p);
        if (actualizado == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(actualizado);
    }

    // ================== TIENDA (PÚBLICO) ==================

    // Invitado puede ver productos
    @GetMapping
    public ResponseEntity<List<Producto>> findAllProductos() {
        return ResponseEntity.ok(service.getProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> findProductoById(@PathVariable Integer id) {
        Producto p = service.getProductoById(id);
        return (p == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Producto> findProductoByCodigo(@PathVariable String codigo) {
        Producto p = service.getProductoByCodigo(codigo);
        return (p == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }
}
