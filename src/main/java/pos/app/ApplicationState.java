package pos.app;

import pos.db.ProductDAO;
import pos.model.Cart;
import pos.model.Product;

import java.util.List;

/**
 * Holds the current cart, products list, and UI state.
 */
public class ApplicationState {
    private static final ApplicationState INSTANCE = new ApplicationState();

    private Cart cart;
    private List<Product> products;
    private Product selectedProduct;
    private String currentInput;
    private InputMode inputMode;

    public enum InputMode {
        QUANTITY,
        PRICE,
        SEARCH,
        NONE
    }

    private ApplicationState() {
        this.cart = new Cart();
        this.currentInput = "";
        this.inputMode = InputMode.NONE;
    }

    public static ApplicationState getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the application state with products.
     */
    public void initialize() {
        loadProducts();
    }

    /**
     * Loads products from the database or CSV fallback.
     */
    private void loadProducts() {
        // Try to load from database first
        products = ProductDAO.getInstance().loadAllProducts();

        // If database is empty, try to import from CSV
        if (products.isEmpty()) {
            String csvPath = getProductsCsvPath();
            if (csvPath != null) {
                ProductDAO.getInstance().importFromCsv(csvPath);
                products = ProductDAO.getInstance().loadAllProducts();
            }

            // If still empty, load directly from CSV without database
            if (products.isEmpty() && csvPath != null) {
                products = ProductDAO.getInstance().loadFromCsv(csvPath);
            }
        }
    }

    private String getProductsCsvPath() {
        // Check common locations for the CSV file
        String[] paths = {
                "data/mock_database.csv",
                "../data/mock_database.csv",
                "src/main/resources/data/mock_database.csv"
        };

        for (String path : paths) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                return path;
            }
        }
        return null;
    }

    // Cart management
    public Cart getCart() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
        currentInput = "";
    }

    // Products management
    public List<Product> getProducts() {
        return products;
    }

    public Product findProductByCpu(String cpu) {
        for (Product product : products) {
            if (product.getCpu().equals(cpu)) {
                return product;
            }
        }
        return null;
    }

    public List<Product> searchProducts(String query) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                        p.getCpu().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    // Selection management
    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Product product) {
        this.selectedProduct = product;
    }

    // Input management
    public String getCurrentInput() {
        return currentInput;
    }

    public void appendInput(String digit) {
        this.currentInput += digit;
    }

    public void deleteLastInputChar() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }
    }

    public void clearInput() {
        this.currentInput = "";
    }

    public double getCurrentInputAsDouble() {
        try {
            return Double.parseDouble(currentInput);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getCurrentInputAsInt() {
        try {
            return Integer.parseInt(currentInput);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Input mode management
    public InputMode getInputMode() {
        return inputMode;
    }

    public void setInputMode(InputMode mode) {
        this.inputMode = mode;
        this.currentInput = "";
    }

    // Convenience methods
    public void addToCart(Product product) {
        if (currentInput.isEmpty()) {
            cart.addItem(product);
        } else {
            cart.addItem(product, getCurrentInputAsDouble());
            clearInput();
        }
    }

    public void addToCart(Product product, double quantity) {
        cart.addItem(product, quantity);
    }
}