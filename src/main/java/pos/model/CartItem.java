package pos.model;

/**
 * Represents an item in the shopping cart.
 * Associates a product with a quantity.
 */
public class CartItem {
    private final Product product;
    private double quantity;
    private double unitPrice;

    public CartItem(Product product) {
        this(product, 1.0);
    }

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
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

    public double getTotalPrice() {
        return quantity * unitPrice;
    }

    public String getDisplayName() {
        return product.getName();
    }

    public String getCpu() {
        return product.getCpu();
    }

    @Override
    public String toString() {
        return String.format("%s x %.2f @ $%.2f = $%.2f",
                product.getName(), quantity, unitPrice, getTotalPrice());
    }
}