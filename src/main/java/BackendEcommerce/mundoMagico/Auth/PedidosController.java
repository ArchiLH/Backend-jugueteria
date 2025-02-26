package BackendEcommerce.mundoMagico.Auth;

import BackendEcommerce.mundoMagico.Service.PedidoService;
import BackendEcommerce.mundoMagico.Stripe.StripeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {

    @Autowired
    private PedidoService pedidoService;

    // 1. Obtener todos los pedidos
    @GetMapping
    public ResponseEntity<List<StripeEvent>> obtenerTodosLosPedidos() {
        List<StripeEvent> pedidos = pedidoService.obtenerTodosLosPedidos();
        return ResponseEntity.ok(pedidos);
    }

    // 2. Obtener pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<StripeEvent> obtenerPedidoPorId(@PathVariable Long id) {
        return pedidoService.obtenerPedidoPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }


    // 3. Obtener pedidos por email
    @GetMapping("/cliente/{email}")
    public ResponseEntity<List<StripeEvent>> obtenerPedidosPorEmail(@PathVariable String email) {
        List<StripeEvent> pedidos = pedidoService.obtenerPedidosPorEmail(email);
        if (pedidos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pedidos);
    }
}
