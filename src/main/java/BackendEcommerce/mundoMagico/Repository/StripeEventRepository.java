package BackendEcommerce.mundoMagico.Repository;

import BackendEcommerce.mundoMagico.Stripe.StripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StripeEventRepository extends JpaRepository<StripeEvent, Long> {
    Optional<StripeEvent> findByEventId(String eventId);
    Optional<StripeEvent> findById(Long id);
    List<StripeEvent> findByCustomerEmail(String customerEmail);
}