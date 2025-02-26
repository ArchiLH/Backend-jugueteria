package BackendEcommerce.mundoMagico.Auth;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);


    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.success.url:http://localhost:5173/success}")
    private String successUrl;

    @Value("${stripe.cancel.url:http://localhost:5173/cancel}")
    private String cancelUrl;

    // Inicializa la clave secreta de Stripe
    @PostConstruct
    public void init() {
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            logger.error("❌ Clave secreta de Stripe no configurada. Verifica tu archivo application.properties.");
        } else {
            Stripe.apiKey = stripeApiKey;
            logger.info("✅ Clave secreta de Stripe configurada correctamente.");
        }
    }

    // Endpoint para crear una sesión de pago
    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Object> payload) {
        try {
            // Extraer productos del cuerpo de la solicitud
            List<Map<String, Object>> products = (List<Map<String, Object>>) payload.get("products");

            if (products == null || products.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La lista de productos no puede estar vacía."));
            }

            // Construcción de los ítems de la sesión de Stripe
            SessionCreateParams.LineItem[] lineItems = products.stream().map(product -> {
                String priceId = (String) product.get("stripePriceId");
                Integer quantity = (Integer) product.get("quantity");

                if (priceId == null || priceId.trim().isEmpty()) {
                    throw new IllegalArgumentException("❌ El ID de precio (stripePriceId) es obligatorio.");
                }

                if (quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException("❌ La cantidad debe ser mayor a cero.");
                }

                return SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(Long.valueOf(quantity))
                        .build();
            }).toArray(SessionCreateParams.LineItem[]::new);

            // Configuración de la sesión de pago
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addAllLineItem(List.of(lineItems))
                    .build();

            // Creación de la sesión
            Session session = Session.create(params);

            logger.info("✅ Sesión de pago creada exitosamente: {}", session.getId());
            return ResponseEntity.ok(Map.of("url", session.getUrl()));
        } catch (StripeException e) {
            logger.error("❌ Error de Stripe: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Error inesperado: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno del servidor."));
        }
    }
}
