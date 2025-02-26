package BackendEcommerce.mundoMagico.Repository;

import BackendEcommerce.mundoMagico.User.Producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Búsqueda por nombre del producto
    List<Producto> findByName(String name);

    // Verifica si existe un producto por nombre
    boolean existsByName(String name);

    // Buscar productos por categoría y limitar resultados
    List<Producto> findByCategoria(String categoria, Pageable pageable);

    // Buscar por ID
    Optional<Producto> findById(Long id);

    // Buscar por Stripe Product ID y Price ID
    //Optional<Producto> findByStripeProductId(String stripeProductId);
    //Optional<Producto> findByStripePriceId(String stripePriceId);


}
