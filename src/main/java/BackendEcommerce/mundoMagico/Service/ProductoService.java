package BackendEcommerce.mundoMagico.Service;

import BackendEcommerce.mundoMagico.Repository.ProductoRepository;
import BackendEcommerce.mundoMagico.User.Producto.Producto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // método para obtener productos por categoría con límite
    public List<Producto> obtenerProductosPorCategoria(String categoria, int limit) {
        Pageable pageable = PageRequest.of(0, limit); // Crea un objeto Pageable para limitar a 'limit' resultados, empezando desde la página 0
        return productoRepository.findByCategoria(categoria, pageable);
    }

    public List<Producto> obtenerTodosProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) { // Cambiado a Long
        Optional<Producto> producto = productoRepository.findById(id);
        return producto.orElse(null); // o lanzar excepción si no se encuentra el producto
    }

    public List<Producto> getProductosByIds(List<Long> productIds) { // Cambiado a List<Long>
        return productoRepository.findAllById(productIds);
    }

    public List<Producto> obtenerProductosPorIds(List<Long> ids) { // Cambiado a List<Long>
        return productoRepository.findAllById(ids);
    }

    // Si aún recibes una lista de Integer, conviértela antes de usar findAllById
    public List<Producto> getProductosByIdsFromInteger(List<Integer> productIds) {
        List<Long> longIds = productIds.stream()
                .map(Integer::longValue) // Convertir Integer a Long
                .collect(Collectors.toList());
        return productoRepository.findAllById(longIds);
    }

    @Transactional
    public void reducirStock(Long productoId, Long cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para ID: " + productoId));

        if (producto.getStock() >= cantidad) {
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
            System.out.println("📉 Stock reducido en " + cantidad + " para el producto con ID: " + productoId);
        } else {
            throw new RuntimeException("Stock insuficiente para el producto con ID: " + productoId);
        }
    }

}
