package BackendEcommerce.mundoMagico.Auth;

import BackendEcommerce.mundoMagico.Service.ProductoService;
import BackendEcommerce.mundoMagico.User.Producto.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public List<Producto> obtenerProductos() {
        return productoService.obtenerTodosProductos();
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) { // Cambiar Integer a Long
        Producto producto = productoService.getProductoById(id);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/productos/por-ids")
    public ResponseEntity<List<Producto>> obtenerProductosPorIds(@RequestParam List<Long> ids) { // Cambiar Integer a Long
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requieren los IDs de los productos");
        }
        return ResponseEntity.ok(productoService.obtenerProductosPorIds(ids));
    }
}

