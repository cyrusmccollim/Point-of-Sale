package pos.model;

/**
 * Represents a product that has been selected but not yet confirmed to the cart.
 * Used in the new workflow where user selects a product, enters weight/quantity,
 * then confirms to add to cart.
 */
public class PendingCartItem {
    private final Product product;
    private double quantity;
    private double weight;

    public PendingCartItem(Product product) {
        this.product = product;
        this.quantity = 1;
        this.weight = 0;
    }

    public PendingCartItem(Product product, double quantity, double weight) {
        this.product = product;
        this.quantity = quantity;
        this.weight = weight;
    }

    public Product getProduct() {
        return product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = Math.max(0, weight);
    }

    /**
     * Calculates the total price for this pending item.
     * For weighed goods: price per lb * weight * quantity
     *
     * @return The total price
     */
    public double getTotalPrice() {
        if (weight <= 0) {
            return 0;
        }
        return product.getPrice() * weight * quantity;
    }

    /**
     * Gets the unit price (per pound) of the product.
     *
     * @return The price per pound
     */
    public double getUnitPrice() {
        return product.getPrice();
    }

    /**
     * Checks if this pending item has valid weight for confirmation.
     *
     * @return true if weight is greater than 0
     */
    public boolean canConfirm() {
        return weight > 0;
    }

    /**
     * Creates a CartItem from this pending item.
     *
     * @return A new CartItem with the product, quantity, and weight
     */
    public CartItem toCartItem() {
        return new CartItem(product, quantity, weight);
    }

    /**
     * Resets this pending item to default values.
     */
    public void reset() {
        this.quantity = 1;
        this.weight = 0;
    }

    @Override
    public String toString() {
        return String.format("Pending: %s x %.0f @ %.2f lb = $%.2f",
                product.getName(), quantity, weight, getTotalPrice());
    }
}
