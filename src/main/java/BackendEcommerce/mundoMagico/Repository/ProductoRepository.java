package BackendEcommerce.mundoMagico.Repository;

import BackendEcommerce.mundoMagico.User.Producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Búsqueda por nombre del producto
    List<Producto> findByName(String name);

    // Verifica si existe un producto por nombre
    boolean existsByName(String name);

    // Nuevo método para buscar productos por categoría y limitar resultados
    List<Producto> findByCategoria(String categoria, org.springframework.data.domain.Pageable pageable);





    // Puedes agregar otros métodos según necesites
}
