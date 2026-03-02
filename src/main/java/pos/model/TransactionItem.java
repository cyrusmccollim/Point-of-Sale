package pos.model;

/**
 * Represents an item in a completed transaction.
 * Immutable snapshot of a cart item at transaction time.
 */
public class TransactionItem {
    private final String productName;
    private final String productCpu;
    private final int quantity;
    private final double weight;
    private final double unitPrice;
    private final double subtotal;

    public TransactionItem(String productName, String productCpu, int quantity, double weight, double unitPrice) {
        this.productName = productName;
        this.productCpu = productCpu;
        this.quantity = quantity;
        this.weight = weight;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * weight * unitPrice;
    }

    public TransactionItem(CartItem cartItem) {
        this.productName = cartItem.getProduct().getName();
        this.productCpu = cartItem.getProduct().getCpu();
        this.quantity = (int) cartItem.getQuantity();
        this.weight = cartItem.getWeight();
        this.unitPrice = cartItem.getUnitPrice();
        this.subtotal = cartItem.getTotalPrice();
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCpu() {
        return productCpu;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getWeight() {
        return weight;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    /**
     * Checks if this is a weighed item.
     *
     * @return true if weight > 0
     */
    public boolean isWeighedItem() {
        return weight > 0;
    }

    @Override
    public String toString() {
        if (weight > 0) {
            return String.format("%s x %d @ %.2f lb = $%.2f",
                    productName, quantity, weight, subtotal);
        }
        return String.format("%s x %d @ $%.2f = $%.2f",
                productName, quantity, unitPrice, subtotal);
    }
}