package BackendEcommerce.mundoMagico.Exception;

public class StockInsuficienteException extends RuntimeException {
  public StockInsuficienteException(String mensaje) {
    super(mensaje);
  }
}