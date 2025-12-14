package com.pasteleria.Controller;

import com.pasteleria.Entity.Boleta;
import com.pasteleria.dto.CheckoutRequest;
import com.pasteleria.service.BoletaService;
import com.pasteleria.service.UsuarioService;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Cristóbal Pérez
 */
@RestController
@RequestMapping("/api/v1/boletas")
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
@Tag(name = "Boletas", description = "Gestión de boletas (JWT + roles)")
public class BoletaController {

    private final BoletaService service;
    private final UsuarioService usuarioService;

    public BoletaController(BoletaService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    // ========================= ADMIN (CRUD GENERAL) =========================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Boleta> addBoleta(@RequestBody Boleta b) {
        b.setId(null);
        Boleta creada = service.saveBoleta(b);
        return ResponseEntity.ok(creada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lote")
    public ResponseEntity<List<Boleta>> addBoletas(@RequestBody List<Boleta> boletas) {
        boletas.forEach(boleta -> boleta.setId(null));
        List<Boleta> creadas = service.saveBoletas(boletas);
        return ResponseEntity.ok(creadas);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping
    public ResponseEntity<List<Boleta>> findAllBoletas() {
        return ResponseEntity.ok(service.getBoletas());
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Boleta> findBoletaById(@PathVariable Integer id) {
        Boleta b = service.getBoletaById(id);
        if (b == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(b);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Boleta>> findBoletasByUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(service.getBoletasByIdUsuario(idUsuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoleta(@PathVariable Integer id) {
        service.deleteBoleta(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Boleta> updateBoleta(@PathVariable Integer id, @RequestBody Boleta b) {
        Boleta actualizada = service.updateBoleta(id, b);
        if (actualizada == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(actualizada);
    }

    // ========================= CLIENTE (MIS BOLETAS) =========================

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/mias")
    public ResponseEntity<List<Boleta>> misBoletas(Authentication auth) {
        String email = auth.getName();
        var u = usuarioService.getUsuarioByEmail(email);
        if (u == null || u.getId() == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(service.getBoletasByIdUsuario(u.getId()));
    }

    // ========================= CHECKOUT (CLIENTE) =========================
    // Invitado NO puede pagar; solo logueado con JWT.

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/checkout")
    public ResponseEntity<Boleta> checkout(@RequestBody CheckoutRequest req, Authentication auth) {
        String email = auth.getName(); // viene del JWT
        Boleta boleta = service.procesarCheckout(req, email);
        return ResponseEntity.ok(boleta);
    }
}
