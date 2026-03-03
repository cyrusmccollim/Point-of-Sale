package pos.model;

public class PendingCartItem {
    private final Product product;
    private double quantity;
    private double weight;

    public PendingCartItem(Product product) {
        this.product  = product;
        this.quantity = 1;
        this.weight   = 0;
    }

    public Product getProduct()  { return product; }
    public double getQuantity()  { return quantity; }
    public double getWeight()    { return weight; }
    public double getUnitPrice() { return product.getPrice(); }

    public void setQuantity(double quantity) { this.quantity = Math.max(1, quantity); }
    public void setWeight(double weight)     { this.weight   = Math.max(0, weight); }

    public double getTotalPrice() { return weight <= 0 ? 0 : product.getPrice() * weight * quantity; }
    public boolean canConfirm()   { return weight > 0; }

    @Override
    public String toString() {
        return String.format("Pending: %s x %.0f @ %.2f lb = $%.2f", product.getName(), quantity, weight, getTotalPrice());
    }
}
