package BackendEcommerce.mundoMagico.Service;

import BackendEcommerce.mundoMagico.Repository.StripeEventRepository;
import BackendEcommerce.mundoMagico.Stripe.StripeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private StripeEventRepository stripeEventRepository;

    // Obtener todos los pedidos
    public List<StripeEvent> obtenerTodosLosPedidos() {
        return stripeEventRepository.findAll();
    }

    // Obtener pedido por ID
    public Optional<StripeEvent> obtenerPedidoPorId(Long id) {
        return stripeEventRepository.findById(id);
    }

    // Obtener pedidos por email del cliente
    public List<StripeEvent> obtenerPedidosPorEmail(String email) {
        return stripeEventRepository.findByCustomerEmail(email);
    }
}
