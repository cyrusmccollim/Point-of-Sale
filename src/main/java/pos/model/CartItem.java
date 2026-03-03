package pos.model;

public class CartItem {
    private final Product product;
    private double quantity;
    private double weight;
    private double unitPrice;

    public CartItem(Product product, double quantity, double weight) {
        this.product   = product;
        this.quantity  = quantity;
        this.weight    = weight;
        this.unitPrice = product.getPrice();
    }

    public Product getProduct()   { return product; }
    public double getQuantity()   { return quantity; }
    public double getWeight()     { return weight; }
    public double getUnitPrice()  { return unitPrice; }
    public String getDisplayName(){ return product.getName(); }

    public void setQuantity(double quantity) { this.quantity = Math.max(0, quantity); }
    public void setWeight(double weight)     { this.weight   = Math.max(0, weight); }

    public double getTotalPrice() { return quantity * weight * unitPrice; }
    public boolean isWeighedItem(){ return weight > 0; }

    @Override
    public String toString() {
        return String.format("%s x %.0f @ %.2f lb = $%.2f", product.getName(), quantity, weight, getTotalPrice());
    }
}
