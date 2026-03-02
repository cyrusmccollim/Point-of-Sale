package pos.model;

/**
 * Represents an item in the shopping cart.
 * Associates a product with a quantity and weight (for weighed goods).
 */
public class CartItem {
    private final Product product;
    private double quantity;
    private double weight;
    private double unitPrice;

    public CartItem(Product product) {
        this(product, 1.0, 0.0);
    }

    public CartItem(Product product, double quantity) {
        this(product, quantity, 0.0);
    }

    public CartItem(Product product, double quantity, double weight) {
        this.product = product;
        this.quantity = quantity;
        this.weight = weight;
        this.unitPrice = product.getPrice();
    }

    public Product getProduct() {
        return product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public void incrementQuantity() {
        this.quantity += 1;
    }

    public void incrementQuantity(double amount) {
        this.quantity += amount;
    }

    public void decrementQuantity() {
        if (this.quantity > 0) {
            this.quantity -= 1;
        }
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = Math.max(0, weight);
    }

    /**
     * Calculates the total price for this cart item.
     * For weighed goods: quantity * weight * unitPrice
     * For non-weighed goods (weight=0): quantity * unitPrice
     *
     * @return The total price
     */
    public double getTotalPrice() {
        if (weight > 0) {
            return quantity * weight * unitPrice;
        }
        return quantity * unitPrice;
    }

    /**
     * Checks if this is a weighed item.
     *
     * @return true if weight > 0
     */
    public boolean isWeighedItem() {
        return weight > 0;
    }

    public String getDisplayName() {
        return product.getName();
    }

    public String getCpu() {
        return product.getCpu();
    }

    @Override
    public String toString() {
        if (weight > 0) {
            return String.format("%s x %.0f @ %.2f lb = $%.2f",
                    product.getName(), quantity, weight, getTotalPrice());
        }
        return String.format("%s x %.2f @ $%.2f = $%.2f",
                product.getName(), quantity, unitPrice, getTotalPrice());
    }
}