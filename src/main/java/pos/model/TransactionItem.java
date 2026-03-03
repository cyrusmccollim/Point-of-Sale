package pos.model;

public class TransactionItem {
    private final String productName;
    private final String productCpu;
    private final int    quantity;
    private final double weight;
    private final double unitPrice;
    private final double subtotal;

    public TransactionItem(String productName, String productCpu, int quantity, double weight, double unitPrice) {
        this.productName = productName;
        this.productCpu  = productCpu;
        this.quantity    = quantity;
        this.weight      = weight;
        this.unitPrice   = unitPrice;
        this.subtotal    = quantity * weight * unitPrice;
    }

    public TransactionItem(CartItem cartItem) {
        this.productName = cartItem.getProduct().getName();
        this.productCpu  = cartItem.getProduct().getCpu();
        this.quantity    = (int) cartItem.getQuantity();
        this.weight      = cartItem.getWeight();
        this.unitPrice   = cartItem.getUnitPrice();
        this.subtotal    = cartItem.getTotalPrice();
    }

    public String getProductName() { return productName; }
    public String getProductCpu()  { return productCpu; }
    public int    getQuantity()    { return quantity; }
    public double getWeight()      { return weight; }
    public double getUnitPrice()   { return unitPrice; }
    public double getSubtotal()    { return subtotal; }
    public boolean isWeighedItem() { return weight > 0; }

    @Override
    public String toString() {
        return weight > 0
                ? String.format("%s x %d @ %.2f lb = $%.2f", productName, quantity, weight, subtotal)
                : String.format("%s x %d @ $%.2f = $%.2f",  productName, quantity, unitPrice, subtotal);
    }
}
