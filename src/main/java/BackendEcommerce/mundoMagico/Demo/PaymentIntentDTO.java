package BackendEcommerce.mundoMagico.Demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentIntentDTO {
    private String id;
    private Long amount;
    private String currency;
    private String status;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("latest_charge")
    private String latestCharge;
}
