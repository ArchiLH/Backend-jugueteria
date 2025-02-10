package BackendEcommerce.mundoMagico.Auth;

import BackendEcommerce.mundoMagico.Exception.StripeException;
import BackendEcommerce.mundoMagico.Service.ProductoService;
import BackendEcommerce.mundoMagico.User.Producto.Producto;
import BackendEcommerce.mundoMagico.User.Stripe.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);
    private final ProductoService productoService;

    static {
        // Configura tu clave secreta de Stripe desde una variable de entorno
        String stripeApiKey = System.getenv("sk_test_51QTBiWGVrTx2ekztpDBHi32qfXdiehHPLpme6ldSCzd9VHaHVpSzegDOubQsj1l2P5gIzeF5NIaeWKsO7c4w79dn00Dlprg5uX");
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            logger.error("Clave secreta de Stripe no configurada. Verifica tu entorno.");
        } else {
            Stripe.apiKey = stripeApiKey;
            logger.info("Clave secreta de Stripe configurada correctamente.");
        }
    }




    // Constructor
    public StripeController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Crear el PaymentIntent (como ya lo tenías)
    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> products = (List<Map<String, Object>>) body.get("products");
        if (products == null || products.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Los productos son requeridos"));
        }

        double totalPriceInSoles = 0;

        // Procesar cada producto y su cantidad
        for (Map<String, Object> productData : products) {
            Integer productId = (Integer) productData.get("id");
            Integer quantity = (Integer) productData.get("quantity");

            Producto producto = productoService.getProductoById(productId);
            if (producto == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Producto no encontrado"));
            }

            totalPriceInSoles += producto.getPrice() * (quantity != null ? quantity : 1);
        }

        long amountInCents = (long) (totalPriceInSoles * 100);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("PEN")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al procesar el pago con Stripe"));
        }
    }
}