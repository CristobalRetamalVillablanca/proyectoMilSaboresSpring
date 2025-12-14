package com.pasteleria.Controller;

import com.pasteleria.Entity.Usuario;
import com.pasteleria.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
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
@Tag(name = "Usuarios", description = "Operaciones CRUD de usuarios (protegido por JWT + roles)")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // ========================= CRUD (SOLO ADMIN) =========================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Usuario> addUsuario(@RequestBody Usuario u) {
        Usuario creado = service.saveUsuario(u);
        return ResponseEntity.ok(creado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batch")
    public ResponseEntity<List<Usuario>> addUsuarios(@RequestBody List<Usuario> usuarios) {
        List<Usuario> creados = service.saveUsuarios(usuarios);
        return ResponseEntity.ok(creados);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Usuario>> findAllUsuarios() {
        return ResponseEntity.ok(service.getUsuarios());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> findUsuarioById(@PathVariable Integer id) {
        Usuario u = service.getUsuarioById(id);
        return (u == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/run/{run}")
    public ResponseEntity<Usuario> findUsuarioByRun(@PathVariable String run) {
        Usuario u = service.getUsuarioByRun(run);
        return (u == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> findUsuarioByEmail(@PathVariable String email) {
        Usuario u = service.getUsuarioByEmail(email);
        return (u == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        service.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Integer id, @RequestBody Usuario u) {
        Usuario updated = service.updateUsuario(id, u);
        return ResponseEntity.ok(updated);
    }
}
