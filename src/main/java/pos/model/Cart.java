package pos.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<CartItem> items;
    private double total;

    public Cart() {
        this.items = new ArrayList<>();
        this.total = 0;
    }

    public void addItem(Product product, double quantity, double weight) {
        items.add(new CartItem(product, quantity, weight));
        recalculateTotal();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalculateTotal();
        }
    }

    public void clear() {
        items.clear();
        total = 0;
    }

    public List<CartItem> getItems()  { return new ArrayList<>(items); }
    public int getItemCount()         { return items.size(); }
    public double getTotal()          { return total; }
    public boolean isEmpty()          { return items.isEmpty(); }

    private void recalculateTotal() {
        total = items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cart contents:\n");
        for (CartItem item : items) sb.append("  ").append(item).append("\n");
        sb.append(String.format("Total: $%.2f", total));
        return sb.toString();
    }
}
