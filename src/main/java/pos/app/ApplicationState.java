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

    public enum InputMode { QUANTITY, WEIGHT, NONE }

    private ApplicationState() {
        this.cart              = new Cart();
        this.currentInput      = "";
        this.inputMode         = InputMode.NONE;
        this.currentDepartment = Config.getInstance().getDepartment();
    }

    public static ApplicationState getInstance() { return INSTANCE; }

    public void initialize() {
        this.currentDepartment = Config.getInstance().getDepartment();
        loadProducts();
    }

    private void loadProducts() {
        String csvPath = findFile(currentDepartment.getCatalogPath(), "src/main/resources/" + currentDepartment.getCatalogPath(), "../" + currentDepartment.getCatalogPath());
        if (csvPath != null) {
            products = ProductDAO.getInstance().loadFromCsv(csvPath);
            if (!products.isEmpty()) {
                Logger.info("Loaded " + products.size() + " products for department: " + currentDepartment);
                notifyProductsChanged();
                return;
            }
        }

        products = ProductDAO.getInstance().loadAllProducts();

        if (products.isEmpty()) {
            String genericPath = findFile("data/mock_database.csv", "../data/mock_database.csv", "src/main/resources/data/mock_database.csv");
            if (genericPath != null) {
                ProductDAO.getInstance().importFromCsv(genericPath);
                products = ProductDAO.getInstance().loadAllProducts();
                if (products.isEmpty()) products = ProductDAO.getInstance().loadFromCsv(genericPath);
            }
        }

        Logger.info("Loaded " + products.size() + " products");
        notifyProductsChanged();
    }

    private String findFile(String... paths) {
        for (String path : paths) {
            if (new java.io.File(path).exists()) return path;
        }
        return null;
    }

    public Cart getCart()                   { return cart; }
    public List<Product> getProducts()      { return products; }
    public Product getSelectedProduct()     { return selectedProduct; }
    public void setSelectedProduct(Product p){ this.selectedProduct = p; }
    public Department getCurrentDepartment(){ return currentDepartment; }

    public void clearCart() { cart.clear(); currentInput = ""; }

    public List<Product> searchProducts(String query) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) || p.getCpu().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public String getCurrentInput()          { return currentInput; }
    public void appendInput(String digit)    { this.currentInput += digit; }
    public void clearInput()                 { this.currentInput = ""; }
    public double getCurrentInputAsDouble()  { try { return Double.parseDouble(currentInput); } catch (NumberFormatException e) { return 0; } }

    public void deleteLastInputChar() {
        if (!currentInput.isEmpty()) currentInput = currentInput.substring(0, currentInput.length() - 1);
    }

    public InputMode getInputMode() { return inputMode; }

    public void setInputMode(InputMode mode) {
        this.inputMode    = mode;
        this.currentInput = "";
        for (StateChangeListener l : listeners) l.onInputModeChanged(mode);
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

    public PendingCartItem getPendingItem() { return pendingItem; }
    public boolean hasPendingItem()         { return pendingItem != null; }

    public void setPendingProduct(Product product) {
        this.pendingItem    = new PendingCartItem(product);
        this.selectedProduct = product;
        clearInput();
        notifyPendingItemChanged();
    }

    public void updatePendingQuantity(double quantity) {
        if (pendingItem != null) { pendingItem.setQuantity(quantity); notifyPendingItemChanged(); }
    }

    public void updatePendingWeight(double weight) {
        if (pendingItem != null) { pendingItem.setWeight(weight); notifyPendingItemChanged(); }
    }

    public boolean confirmPendingItem() {
        if (pendingItem != null && pendingItem.canConfirm()) {
            cart.addItem(pendingItem.getProduct(), pendingItem.getQuantity(), pendingItem.getWeight());
            clearPendingItem();
            notifyCartChanged();
            return true;
        }
        return false;
    }

    public void removeCartItem(int index) { cart.removeItem(index); notifyCartChanged(); }

    public void clearPendingItem() {
        this.pendingItem     = null;
        this.selectedProduct = null;
        clearInput();
        notifyPendingItemChanged();
    }

    public void addStateChangeListener(StateChangeListener l)    { listeners.add(l); }
    public void removeStateChangeListener(StateChangeListener l) { listeners.remove(l); }

    private void notifyPendingItemChanged() { for (StateChangeListener l : listeners) l.onPendingItemChanged(pendingItem); }
    private void notifyDepartmentChanged()  { for (StateChangeListener l : listeners) l.onDepartmentChanged(currentDepartment); }
    private void notifyProductsChanged()    { for (StateChangeListener l : listeners) l.onProductsChanged(products); }
    private void notifyCartChanged()        { for (StateChangeListener l : listeners) l.onCartChanged(); }
    public void notifyTransactionCompleted(){ for (StateChangeListener l : listeners) l.onTransactionCompleted(); }

    public interface StateChangeListener {
        default void onPendingItemChanged(PendingCartItem item) {}
        default void onDepartmentChanged(Department department) {}
        default void onProductsChanged(List<Product> products)  {}
        default void onInputModeChanged(InputMode mode)         {}
        default void onCartChanged()                            {}
        default void onTransactionCompleted()                   {}
    }
}
