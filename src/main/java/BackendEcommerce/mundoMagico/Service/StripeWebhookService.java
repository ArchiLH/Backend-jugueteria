package BackendEcommerce.mundoMagico.Service;

import BackendEcommerce.mundoMagico.Repository.StripeEventRepository;
import BackendEcommerce.mundoMagico.Stripe.StripeEvent;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.LineItem;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.param.ChargeListParams;
import com.stripe.param.checkout.SessionListLineItemsParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StripeWebhookService {

    @Autowired
    private StripeEventRepository stripeEventRepository;

    public void handleEvent(Event event) {
        String eventType = event.getType();

        switch (eventType) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;

            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            default:
                System.out.println("⚠️ Evento no manejado: " + eventType);
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        if (session != null && session.getPaymentIntent() != null) {
            String paymentIntentId = session.getPaymentIntent();

            if (stripeEventRepository.findByEventId(event.getId()).isPresent()) {
                System.out.println("✅ Evento duplicado ignorado: " + event.getId());
                return;
            }

            // Obtener el email del cliente
            String customerEmail = (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null)
                    ? session.getCustomerDetails().getEmail()
                    : session.getCustomerEmail();

            if (customerEmail == null || customerEmail.isEmpty()) {
                System.out.println("⚠️ Email no encontrado en la sesión.");
            } else {
                System.out.println("✅ Email encontrado: " + customerEmail);
            }

            // Capturar las cantidades compradas
            try {
                SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
                List<LineItem> lineItems = session.listLineItems(params).getData();

                for (LineItem item : lineItems) {
                    System.out.println("🛒 Producto: " + item.getDescription() + ", Cantidad: " + item.getQuantity());

                    // Crear el objeto StripeEvent para cada producto
                    StripeEvent stripeEvent = new StripeEvent();
                    stripeEvent.setEventId(event.getId());
                    stripeEvent.setEventType(event.getType());
                    stripeEvent.setCustomerEmail(customerEmail != null ? customerEmail : "Desconocido");
                    stripeEvent.setAmount(item.getAmountTotal());
                    stripeEvent.setCurrency(item.getCurrency());
                    stripeEvent.setPaymentIntent(paymentIntentId);
                    stripeEvent.setSessionUrl(session.getUrl());
                    stripeEvent.setStatus(session.getPaymentStatus());
                    stripeEvent.setProductDescription(item.getDescription());
                    stripeEvent.setProductQuantity(item.getQuantity());     // Conversión segura a Integer

                    // Logs detallados antes de guardar
                    System.out.println("📄 Guardando evento con los siguientes detalles:");
                    System.out.println("  - ID del Evento: " + stripeEvent.getEventId());
                    System.out.println("  - Email del Cliente: " + stripeEvent.getCustomerEmail());
                    System.out.println("  - Descripción del Producto: " + stripeEvent.getProductDescription());
                    System.out.println("  - Cantidad: " + stripeEvent.getProductQuantity());
                    System.out.println("  - Monto: " + stripeEvent.getAmount());
                    System.out.println("  - Moneda: " + stripeEvent.getCurrency());
                    System.out.println("  - URL de Sesión: " + stripeEvent.getSessionUrl());

                    // Guardar el evento
                    stripeEventRepository.save(stripeEvent);
                    System.out.println("✅ Evento guardado exitosamente: " + stripeEvent.getEventId());
                }
            } catch (StripeException e) {
                System.err.println("❌ Error al obtener los productos de la sesión: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Sesión incompleta o sin PaymentIntent.");
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) ApiResource.GSON.fromJson(event.getDataObjectDeserializer().getRawJson(), PaymentIntent.class);

        if (stripeEventRepository.findByEventId(event.getId()).isPresent()) {
            System.out.println("✅ Evento duplicado ignorado: " + event.getId());
            return;
        }

        StripeEvent stripeEvent = new StripeEvent();
        stripeEvent.setEventId(event.getId());
        stripeEvent.setEventType(event.getType());
        stripeEvent.setAmount(paymentIntent.getAmount());
        stripeEvent.setCurrency(paymentIntent.getCurrency());
        stripeEvent.setPaymentIntent(paymentIntent.getId());
        stripeEvent.setStatus(paymentIntent.getStatus());

        try {
            for (Charge charge : getChargesForPaymentIntent(paymentIntent.getId())) {
                if (charge.getBillingDetails() != null && charge.getBillingDetails().getEmail() != null) {
                    stripeEvent.setCustomerEmail(charge.getBillingDetails().getEmail());
                }
                if (charge.getReceiptUrl() != null) {
                    stripeEvent.setReceiptUrl(charge.getReceiptUrl());
                    break;
                }
            }
        } catch (StripeException e) {
            System.err.println("❌ Error al obtener la receipt_url y el email: " + e.getMessage());
        }

        stripeEventRepository.save(stripeEvent);
        System.out.println("✅ Evento de pago guardado con éxito: " + stripeEvent);
    }

    private Iterable<Charge> getChargesForPaymentIntent(String paymentIntentId) throws StripeException {
        ChargeListParams params = ChargeListParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();
        return Charge.list(params).autoPagingIterable();
    }
}