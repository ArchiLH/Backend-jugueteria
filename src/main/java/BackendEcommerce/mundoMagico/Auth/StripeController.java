package BackendEcommerce.mundoMagico.Auth;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);

    static {
        // Clave secreta de Stripe
        String stripeApiKey = System.getenv("sk_test_51QTBiWGVrTx2ekztpDBHi32qfXdiehHPLpme6ldSCzd9VHaHVpSzegDOubQsj1l2P5gIzeF5NIaeWKsO7c4w79dn00Dlprg5uX"); // Usa una variable de entorno
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            logger.error("Clave secreta de Stripe no configurada. Verifica tu entorno.");
        } else {
            Stripe.apiKey = stripeApiKey;
            logger.info("Clave secreta de Stripe configurada correctamente.");
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Object> payload) {
        try {
            List<Map<String, Object>> products = (List<Map<String, Object>>) payload.get("products");

            // Construcción de los elementos para la sesión de Stripe
            SessionCreateParams.LineItem[] lineItems = products.stream().map(product -> {
                String priceId = (String) product.get("stripePriceId");
                Integer quantity = (Integer) product.get("quantity");

                return SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(Long.valueOf(quantity))
                        .build();
            }).toArray(SessionCreateParams.LineItem[]::new);

            // Parámetros de la sesión
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:5173/success")
                    .setCancelUrl("http://localhost:5173/cancel")
                    .addAllLineItem(List.of(lineItems))
                    .build();

            // Creación de la sesión de Stripe
            Session session = Session.create(params);
            return ResponseEntity.ok(Map.of("url", session.getUrl()));

        } catch (StripeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "Error procesando la solicitud: " + e.getMessage()));
        }
    }
}