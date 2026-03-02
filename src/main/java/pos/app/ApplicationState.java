package pos.app;

import pos.db.ProductDAO;
import pos.model.Cart;
import pos.model.Department;
import pos.model.PendingCartItem;
import pos.model.Product;
import pos.util.Config;
import pos.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the current cart, products list, and UI state.
 */
public class ApplicationState {
    private static final ApplicationState INSTANCE = new ApplicationState();

    private Cart cart;
    private List<Product> products;
    private Product selectedProduct;
    private PendingCartItem pendingItem;
    private Department currentDepartment;
    private String currentInput;
    private InputMode inputMode;
    private final List<StateChangeListener> listeners = new ArrayList<>();

    public enum InputMode {
        QUANTITY,
        WEIGHT,
        SEARCH,
        NONE
    }

    private ApplicationState() {
        this.cart = new Cart();
        this.currentInput = "";
        this.inputMode = InputMode.NONE;
        this.currentDepartment = Config.getInstance().getDepartment();
    }

    public static ApplicationState getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the application state with products.
     */
    public void initialize() {
        this.currentDepartment = Config.getInstance().getDepartment();
        loadProducts();
    }

    /**
     * Loads products from the database or CSV fallback for the current department.
     */
    private void loadProducts() {
        // Try to load from department-specific CSV first
        String csvPath = getDepartmentCsvPath();
        if (csvPath != null) {
            products = ProductDAO.getInstance().loadFromCsv(csvPath);
            if (!products.isEmpty()) {
                Logger.info("Loaded " + products.size() + " products for department: " + currentDepartment);
                notifyProductsChanged();
                return;
            }
        }

        // Fallback to database
        products = ProductDAO.getInstance().loadAllProducts();

        // If database is empty, try generic CSV
        if (products.isEmpty()) {
            String genericPath = getProductsCsvPath();
            if (genericPath != null) {
                ProductDAO.getInstance().importFromCsv(genericPath);
                products = ProductDAO.getInstance().loadAllProducts();
            }

            if (products.isEmpty() && genericPath != null) {
                products = ProductDAO.getInstance().loadFromCsv(genericPath);
            }
        }

        Logger.info("Loaded " + products.size() + " products");
        notifyProductsChanged();
    }

    private String getDepartmentCsvPath() {
        String catalogPath = currentDepartment.getCatalogPath();
        String[] paths = {
                catalogPath,
                "src/main/resources/" + catalogPath,
                "../" + catalogPath
        };

        for (String path : paths) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                return path;
            }
        }
        return null;
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

    public void addToCart(Product product, double quantity, double weight) {
        cart.addItem(product, quantity, weight);
    }

    // Department management
    public Department getCurrentDepartment() {
        return currentDepartment;
    }

    public void setCurrentDepartment(Department department) {
        if (department != this.currentDepartment) {
            this.currentDepartment = department;
            Config.getInstance().setDepartment(department);
            Config.getInstance().save();
            clearPendingItem();
            loadProducts();
            notifyDepartmentChanged();
        }
    }

    // Pending item management (new workflow)
    public PendingCartItem getPendingItem() {
        return pendingItem;
    }

    public boolean hasPendingItem() {
        return pendingItem != null;
    }

    public void setPendingProduct(Product product) {
        this.pendingItem = new PendingCartItem(product);
        this.selectedProduct = product;
        clearInput();
        notifyPendingItemChanged();
    }

    public void updatePendingQuantity(double quantity) {
        if (pendingItem != null) {
            pendingItem.setQuantity(quantity);
            notifyPendingItemChanged();
        }
    }

    public void updatePendingWeight(double weight) {
        if (pendingItem != null) {
            pendingItem.setWeight(weight);
            notifyPendingItemChanged();
        }
    }

    public boolean confirmPendingItem() {
        if (pendingItem != null && pendingItem.canConfirm()) {
            cart.addItem(pendingItem.getProduct(), pendingItem.getQuantity(), pendingItem.getWeight());
            clearPendingItem();
            return true;
        }
        return false;
    }

    public void clearPendingItem() {
        this.pendingItem = null;
        this.selectedProduct = null;
        clearInput();
        notifyPendingItemChanged();
    }

    // State change listeners
    public void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    public void removeStateChangeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyPendingItemChanged() {
        for (StateChangeListener listener : listeners) {
            listener.onPendingItemChanged(pendingItem);
        }
    }

    private void notifyDepartmentChanged() {
        for (StateChangeListener listener : listeners) {
            listener.onDepartmentChanged(currentDepartment);
        }
    }

    private void notifyProductsChanged() {
        for (StateChangeListener listener : listeners) {
            listener.onProductsChanged(products);
        }
    }

    /**
     * Listener interface for state changes.
     */
    public interface StateChangeListener {
        default void onPendingItemChanged(PendingCartItem item) {}
        default void onDepartmentChanged(Department department) {}
        default void onProductsChanged(List<Product> products) {}
    }
}