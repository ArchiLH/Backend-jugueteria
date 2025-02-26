package BackendEcommerce.mundoMagico.Auth;

import BackendEcommerce.mundoMagico.Service.StripeWebhookService;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/webhook/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );

            // Procesar el evento
            stripeWebhookService.handleEvent(event);

            return ResponseEntity.ok("Evento procesado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al procesar el webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error procesando el webhook.");
        }
    }
}
