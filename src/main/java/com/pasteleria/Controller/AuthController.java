package com.pasteleria.Controller;

import com.pasteleria.Entity.RolUsuario;
import com.pasteleria.Entity.Usuario;
import com.pasteleria.dto.AuthResponse;
import com.pasteleria.dto.LoginRequest;
import com.pasteleria.repository.UsuarioRepository;
import com.pasteleria.security.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
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
@Tag(name = "Auth", description = "Autenticación (JWT) y registro público de clientes")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    // ========================= REGISTER (PÚBLICO) =========================
    // Registro público SIEMPRE crea CLIENTE. La password debe venir YA hasheada desde el frontend.
    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario payload) {

        if (payload == null) return ResponseEntity.badRequest().build();

        if (payload.getRun() == null || payload.getRun().trim().isEmpty()) return ResponseEntity.badRequest().build();
        if (payload.getNombre() == null || payload.getNombre().trim().isEmpty()) return ResponseEntity.badRequest().build();
        if (payload.getEmail() == null || payload.getEmail().trim().isEmpty()) return ResponseEntity.badRequest().build();
        if (payload.getPassword() == null || payload.getPassword().trim().isEmpty()) return ResponseEntity.badRequest().build();

        payload.setId(null);
        payload.setRol(RolUsuario.CLIENTE);

        // Normalizar email
        payload.setEmail(payload.getEmail().trim().toLowerCase());

        // IMPORTANTE: NO modificar el hash (solo trim por si acaso)
        payload.setPassword(payload.getPassword().trim());

        // Duplicados
        if (usuarioRepository.findByEmail(payload.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (usuarioRepository.findByRun(payload.getRun().trim()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Usuario creado = usuarioRepository.save(payload);

        // No devolver password
        creado.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========================= LOGIN (PÚBLICO) =========================
    // Login recibe email + password HASHEADA (hex) y compara literal con lo guardado en BD.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        if (request == null) return ResponseEntity.badRequest().build();
        if (request.getEmail() == null || request.getPassword() == null) return ResponseEntity.badRequest().build();

        String email = request.getEmail().trim().toLowerCase();
        String passHash = request.getPassword().trim();

        Usuario u = usuarioRepository.findByEmail(email);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String passDb = (u.getPassword() == null) ? "" : u.getPassword().trim();

        // Comparación literal hash vs hash
        if (!passHash.equals(passDb)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(u);

        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setId(u.getId());
        resp.setEmail(u.getEmail());
        resp.setRol(u.getRol().name());
        resp.setNombre(u.getNombre());

        return ResponseEntity.ok(resp);
    }
}
