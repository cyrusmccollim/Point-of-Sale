package pos.model;

/**
 * Represents a product in the POS system.
 */
public class Product {
    private final int id;
    private final String name;
    private final double price;
    private final String cpu;
    private final String category;
    private boolean active;

    public Product(int id, String name, double price, String cpu) {
        this(id, name, price, cpu, null, true);
    }

    public Product(int id, String name, double price, String cpu, String category) {
        this(id, name, price, cpu, category, true);
    }

    public Product(int id, String name, double price, String cpu, String category, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.cpu = cpu;
        this.category = category;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCpu() {
        return cpu;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", cpu='" + cpu + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id && (cpu != null ? cpu.equals(product.cpu) : product.cpu == null);
    }

    @Override
    public int hashCode() {
        return 31 * id + cpu.hashCode();
    }
}