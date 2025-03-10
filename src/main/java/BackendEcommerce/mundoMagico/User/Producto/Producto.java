package BackendEcommerce.mundoMagico.User.Producto;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;
    private Double price;
    private Long stock;
    private String image;
    private String categoria;
    private Boolean favoritos;
    private String descripcion;
    private String  stripe_product_id;
    private String  stripe_price_id;

}