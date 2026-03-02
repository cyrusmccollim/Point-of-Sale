package pos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart that holds CartItems.
 * Manages the current transaction being built.
 */
public class Cart {
    private final List<CartItem> items;
    private double total;

    public Cart() {
        this.items = new ArrayList<>();
        this.total = 0;
    }

    public void addItem(Product product) {
        // Check if product already in cart
        for (CartItem item : items) {
            if (item.getProduct().getCpu().equals(product.getCpu())) {
                item.incrementQuantity();
                recalculateTotal();
                return;
            }
        }
        // Add new item
        items.add(new CartItem(product));
        recalculateTotal();
    }

    public void addItem(Product product, double quantity) {
        // Check if product already in cart
        for (CartItem item : items) {
            if (item.getProduct().getCpu().equals(product.getCpu())) {
                item.incrementQuantity(quantity);
                recalculateTotal();
                return;
            }
        }
        // Add new item
        items.add(new CartItem(product, quantity));
        recalculateTotal();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalculateTotal();
        }
    }

    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getCpu().equals(product.getCpu()));
        recalculateTotal();
    }

    public void updateQuantity(int index, double quantity) {
        if (index >= 0 && index < items.size()) {
            if (quantity <= 0) {
                items.remove(index);
            } else {
                items.get(index).setQuantity(quantity);
            }
            recalculateTotal();
        }
    }

    public void clear() {
        items.clear();
        total = 0;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getItemCount() {
        return items.size();
    }

    public double getTotalQuantity() {
        return items.stream().mapToDouble(CartItem::getQuantity).sum();
    }

    public double getTotal() {
        return total;
    }

    private void recalculateTotal() {
        total = items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public CartItem getItemAt(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    /**
     * Finds a cart item by product CPU code.
     *
     * @param cpu The product CPU code
     * @return The CartItem if found, null otherwise
     */
    public CartItem findByCpu(String cpu) {
        for (CartItem item : items) {
            if (item.getProduct().getCpu().equals(cpu)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cart contents:\n");
        for (CartItem item : items) {
            sb.append("  ").append(item.toString()).append("\n");
        }
        sb.append(String.format("Total: $%.2f", total));
        return sb.toString();
    }
}