package com.pasteleria.service;

import com.pasteleria.Entity.Boleta;
import com.pasteleria.Entity.DetalleBoleta;
import com.pasteleria.Entity.Producto;
import com.pasteleria.Entity.Usuario;
import com.pasteleria.Entity.RolUsuario;
import com.pasteleria.dto.CheckoutItemDTO;
import com.pasteleria.dto.CheckoutRequest;
import com.pasteleria.repository.BoletaRepository;
import com.pasteleria.repository.DetalleBoletaRepository;
import com.pasteleria.repository.ProductoRepository;
import com.pasteleria.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Cristóbal Pérez
 */
@Service
public class BoletaService {

    @Autowired
    private BoletaRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleBoletaRepository detalleBoletaRepository;

    // Crear una boleta normal
    public Boleta saveBoleta(Boleta b) {
        return repository.save(b);
    }

    public List<Boleta> saveBoletas(List<Boleta> boletas) {
        return repository.saveAll(boletas);
    }

    public List<Boleta> getBoletas() {
        return repository.findAll();
    }

    public Boleta getBoletaById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<Boleta> getBoletasByIdUsuario(Integer idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public void deleteBoleta(Integer id) {
        Boleta boleta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada con id: " + id));

        boleta.setEstado("anulada");
        repository.save(boleta);
    }

    public Boleta updateBoleta(Integer id, Boleta b) {
        Boleta existing = repository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setFecha(b.getFecha());
        existing.setTotal(b.getTotal());
        existing.setEstado(b.getEstado());
        existing.setMedio_pago(b.getMedio_pago());
        existing.setIdUsuario(b.getIdUsuario());

        return repository.save(existing);
    }

    // ===================== CHECKOUT (JWT) =====================
    @Transactional
    public Boleta procesarCheckout(CheckoutRequest req, String emailFromJwt) {

        if (emailFromJwt == null || emailFromJwt.isBlank()) {
            throw new RuntimeException("No autenticado");
        }
        if (req == null) {
            throw new RuntimeException("Request inválido");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }
        if (req.getMedioPago() == null || req.getMedioPago().isBlank()) {
            throw new RuntimeException("Medio de pago requerido");
        }

        // 1) Usuario desde JWT (NO desde el body)
        Usuario u = usuarioRepository.findByEmail(emailFromJwt.trim().toLowerCase());
        if (u == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Solo permitir CLIENTE
        if (u.getRol() != RolUsuario.CLIENTE) {
            throw new RuntimeException("Solo los clientes pueden realizar compras");
        }

        // 2) Crear boleta base
        Boleta boleta = new Boleta();
        boleta.setFecha(LocalDateTime.now().toString());
        boleta.setEstado("pagado");
        boleta.setMedio_pago(req.getMedioPago());
        boleta.setIdUsuario(u.getId());
        boleta.setTotal(0);

        boleta = repository.save(boleta);

        int total = 0;

        // 3) Ítems
        for (CheckoutItemDTO item : req.getItems()) {

            if (item == null || item.getIdProducto() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new RuntimeException("Ítem inválido en carrito");
            }

            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getIdProducto()));

            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para producto: " + producto.getNombre());
            }

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            int precioUnitario = producto.getPrecio_clp();
            int subtotal = precioUnitario * item.getCantidad();

            DetalleBoleta det = new DetalleBoleta();
            det.setIdBoleta(boleta.getId());
            det.setIdProducto(producto.getId());
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precioUnitario);
            det.setSubtotal(subtotal);

            detalleBoletaRepository.save(det);

            total += subtotal;
        }

        // 4) Total
        boleta.setTotal(total);
        return repository.save(boleta);
    }
}
