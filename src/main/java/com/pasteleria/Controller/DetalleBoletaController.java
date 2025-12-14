package com.pasteleria.Controller;

import com.pasteleria.Entity.DetalleBoleta;
import com.pasteleria.service.DetalleBoletaService;

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
@RequestMapping("/api/v1/detalle-boleta")
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
@Tag(name = "Detalle Boleta", description = "Gestión de ítems de boletas (JWT + roles)")
public class DetalleBoletaController {

    private final DetalleBoletaService service;

    public DetalleBoletaController(DetalleBoletaService service) {
        this.service = service;
    }

    // ================== ADMIN (CRUD) ==================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DetalleBoleta> addDetalleBoleta(@RequestBody DetalleBoleta d) {
        d.setId(null);
        DetalleBoleta creado = service.saveDetalleBoleta(d);
        return ResponseEntity.ok(creado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lote")
    public ResponseEntity<List<DetalleBoleta>> addDetallesBoleta(@RequestBody List<DetalleBoleta> detalles) {
        detalles.forEach(det -> det.setId(null));
        List<DetalleBoleta> creados = service.saveDetallesBoleta(detalles);
        return ResponseEntity.ok(creados);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetalleBoleta(@PathVariable Integer id) {
        service.deleteDetalleBoleta(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DetalleBoleta> updateDetalleBoleta(@PathVariable Integer id, @RequestBody DetalleBoleta d) {
        DetalleBoleta updated = service.updateDetalleBoleta(id, d);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // ================== VENDEDOR + ADMIN (SOLO LECTURA) ==================
    // Vendedor puede visualizar órdenes y detalle. :contentReference[oaicite:4]{index=4}

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping
    public ResponseEntity<List<DetalleBoleta>> findAllDetalles() {
        return ResponseEntity.ok(service.getDetallesBoleta());
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<DetalleBoleta> findDetalleById(@PathVariable Integer id) {
        DetalleBoleta d = service.getDetalleBoletaById(id);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping("/boleta/{idBoleta}")
    public ResponseEntity<List<DetalleBoleta>> findDetallesByBoleta(@PathVariable Integer idBoleta) {
        return ResponseEntity.ok(service.getDetallesByBoleta(idBoleta));
    }
}
