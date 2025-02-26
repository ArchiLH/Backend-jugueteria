package BackendEcommerce.mundoMagico.Stripe;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "stripe_eventos")
public class StripeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;   // ID del evento de Stripe

    @Column(nullable = false)
    private String eventType; // Tipo de evento (payment_intent.succeeded, etc.)

    @Column(nullable = true)
    private String customerEmail;

    @Column(name = "stripe_product_id")
    private String stripeProductId;// Correo del cliente

    private Long amount;          // Monto total pagado

    private String currency;      // Moneda utilizada

    private String paymentIntent; // ID del PaymentIntent

    private String sessionUrl;    // URL de la boleta

    private String status;        // Estado del pago (succeeded, failed, etc.)

    @Column
    private String receiptUrl;    // URL del recibo

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;  // Fecha de creación automática

    @Column
    private Long productQuantity; // Cantidad de productos comprados

    @Column
    private String productDescription; // Descripción del producto
}
