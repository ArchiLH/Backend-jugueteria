package BackendEcommerce.mundoMagico.Auth;

import BackendEcommerce.mundoMagico.Repository.ProductoRepository;
import BackendEcommerce.mundoMagico.User.Producto.Producto;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class PayPalController {

    private final APIContext apiContext;
    private final ProductoRepository productoRepository;

    public PayPalController(ProductoRepository productoRepository, APIContext apiContext) {
        this.productoRepository = productoRepository;
        this.apiContext = apiContext;
    }

    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody List<Map<String, Object>> productosReq) {
        log.info("Iniciando la creación de pago");

        if (productosReq == null || productosReq.isEmpty()) {
            log.warn("No se proporcionaron productos en la solicitud");
            return ResponseEntity.badRequest().body(Map.of("error", "No se proporcionaron productos."));
        }

        log.info("Productos recibidos en la solicitud: {}", productosReq);

        // Validar y convertir los productos
        List<Long> productIds = new ArrayList<>();
        Map<Long, Integer> cantidades = new HashMap<>();

        for (Map<String, Object> item : productosReq) {
            Object idObj = item.get("productId");
            Object cantidadObj = item.get("quantity");

            if (idObj instanceof Number idNumber && cantidadObj instanceof Number cantidadNumber) {
                Long id = idNumber.longValue();
                Integer cantidad = cantidadNumber.intValue();

                productIds.add(id);
                cantidades.put(id, cantidad);
                log.debug("Producto ID: {}, Cantidad: {}", id, cantidad);
            } else {
                log.error("Formato incorrecto en los productos: {}", item);
                return ResponseEntity.badRequest().body(Map.of("error", "Formato incorrecto en los productos."));
            }
        }

        log.info("IDs recibidos para la búsqueda: {}", productIds);

        // Buscar productos en la base de datos
        List<Producto> productos = productoRepository.findAllById(productIds);

        log.info("Productos encontrados en la BD: {}", productos);
        log.info("Cantidad de productos recibidos: {}, Cantidad encontrados en BD: {}", productosReq.size(), productos.size());

        if (productos.size() != productIds.size()) {
            log.warn("Algunos productos no existen en la base de datos.");
            return ResponseEntity.badRequest().body(Map.of("error", "Algunos productos no existen en la base de datos."));
        }

        List<Transaction> transactions = productos.stream().map(producto -> {
            int quantity = cantidades.getOrDefault(producto.getId(), 1);
            String amount = String.format("%.2f", producto.getPrice() * quantity);

            Amount payAmount = new Amount();
            payAmount.setCurrency("USD");
            payAmount.setTotal(amount);

            Transaction transaction = new Transaction();
            transaction.setDescription("Pago de " + quantity + "x " + producto.getName());
            transaction.setAmount(payAmount);

            log.debug("Transacción creada para producto: {}, Cantidad: {}, Monto: {}", producto.getName(), quantity, amount);

            return transaction;
        }).collect(Collectors.toList());

        log.info("Procesando pago con {} transacciones.", transactions.size());
        return procesarPago(transactions);
    }

    private ResponseEntity<Map<String, String>> procesarPago(List<Transaction> transactions) {
        log.info("Iniciando procesamiento de pago con PayPal");

        // Crear el pagador (Payer)
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Crear el pago (Payment)
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Definir las URLs de redirección
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:5173/cancel");
        redirectUrls.setReturnUrl("http://localhost:5173/success");
        payment.setRedirectUrls(redirectUrls);

        try {
            // Crear el pago en PayPal
            Payment createdPayment = payment.create(apiContext);
            log.info("Pago creado exitosamente con PayPal. Enlace de aprobación: {}", createdPayment.getLinks());

            // Buscar el enlace de aprobación y devolver la URL
            return createdPayment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .map(link -> ResponseEntity.ok(Map.of("url", link.getHref())))
                    .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "No se pudo obtener la URL de pago.")));
        } catch (PayPalRESTException e) {
            log.error("Error al crear el pago con PayPal: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el pago con PayPal."));
        }
    }
}
