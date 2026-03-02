package pos.app;

import pos.db.DatabaseManager;
import pos.db.TransactionDAO;
import pos.model.Cart;
import pos.model.Transaction;
import pos.model.TransactionItem;
import pos.ui.components.CartPanel;
import pos.ui.components.MetricPanel;
import pos.ui.components.NumberPad;
import pos.ui.dialogs.ConfirmationDialog;
import pos.ui.dialogs.ReceiptDialog;
import pos.ui.panels.CheckoutPanel;
import pos.ui.panels.HistoryPanel;
import pos.ui.panels.ProductsPanel;
import pos.ui.panels.SettingsPanel;
import pos.util.Config;
import pos.util.IconManager;
import pos.util.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for the POS System.
 */
public class POSApplication extends JFrame {
    private static POSApplication instance;

    // Panels
    private ProductsPanel productsPanel;
    private CartPanel cartPanel;
    private CheckoutPanel checkoutPanel;
    private HistoryPanel historyPanel;
    private SettingsPanel settingsPanel;
    private MetricPanel[] metricPanels;
    private NumberPad numberPad;

    // Navigation
    private JPanel mainContent;
    private CardLayout cardLayout;
    private static final String PRODUCTS_VIEW = "PRODUCTS";
    private static final String CHECKOUT_VIEW = "CHECKOUT";
    private static final String HISTORY_VIEW = "HISTORY";
    private static final String SETTINGS_VIEW = "SETTINGS";

    // Current view
    private String currentView = PRODUCTS_VIEW;

    /**
     * Gets the singleton instance of the application.
     */
    public static POSApplication getInstance() {
        return instance;
    }

    public POSApplication() {
        instance = this;
        initialize();
    }

    private void initialize() {
        // Initialize database
        DatabaseManager.getInstance().initializeSchema();
        DatabaseManager.getInstance().ensureReceiptsDirectory();

        // Initialize application state
        ApplicationState.getInstance().initialize();

        // Apply theme
        ThemeManager.getInstance().applyTheme();

        // Configure window
        configureWindow();

        // Create components
        createComponents();

        // Layout
        layoutComponents();

        // Register listeners
        registerListeners();

        // Set default view
        showProductsView();

        Logger.info("POS Application initialized successfully");
    }

    private void configureWindow() {
        setTitle(Config.getInstance().getStoreName() + " - POS System");

        // Set window size to 75% of screen at 16:9 aspect ratio
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height * 0.75);
        int width = (int) (height * 16.0 / 9.0);

        setMinimumSize(new Dimension(1200, 800));
        setPreferredSize(new Dimension(width, height));
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Try to set window icon
        try {
            setIconImage(IconManager.getInstance().getImage(IconManager.POS, 64, 64));
        } catch (Exception e) {
            Logger.warning("Could not load window icon: " + e.getMessage());
        }
    }

    private void createComponents() {
        // Create metric panels
        metricPanels = new MetricPanel[4];
        metricPanels[0] = new MetricPanel("Total", "$0.00");
        metricPanels[1] = new MetricPanel("Items", "0");
        metricPanels[2] = new MetricPanel("Weight", "0.0");
        metricPanels[3] = new MetricPanel("Selected", "--");

        // Create main panels
        productsPanel = new ProductsPanel();
        cartPanel = new CartPanel();
        checkoutPanel = new CheckoutPanel();
        historyPanel = new HistoryPanel();
        settingsPanel = new SettingsPanel();

        // Create number pad
        numberPad = new NumberPad();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top: Metrics panel
        JPanel metricsPanel = createMetricsPanel();
        add(metricsPanel, BorderLayout.NORTH);

        // Center: Main content with card layout
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(ThemeManager.getInstance().getBackgroundColor());

        mainContent.add(createProductsView(), PRODUCTS_VIEW);
        mainContent.add(createCheckoutView(), CHECKOUT_VIEW);
        mainContent.add(historyPanel, HISTORY_VIEW);
        mainContent.add(settingsPanel, SETTINGS_VIEW);

        add(mainContent, BorderLayout.CENTER);

        // Right: Number pad and cart
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Bottom: Navigation
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.SOUTH);
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBackground(ThemeManager.getInstance().getOrangeColor());
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        for (MetricPanel metric : metricPanels) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(ThemeManager.getInstance().getOrangeColor());
            wrapper.add(metric, BorderLayout.CENTER);
            panel.add(wrapper);
        }

        return panel;
    }

    private JPanel createProductsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.add(productsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCheckoutView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.add(checkoutPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(350, 0));

        // Cart panel
        panel.add(cartPanel, BorderLayout.CENTER);

        // Number pad
        panel.add(numberPad, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] buttonLabels = {"Products", "Checkout", "History", "Settings"};
        String[] views = {PRODUCTS_VIEW, CHECKOUT_VIEW, HISTORY_VIEW, SETTINGS_VIEW};

        for (int i = 0; i < buttonLabels.length; i++) {
            JButton button = new JButton(buttonLabels[i]);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setBackground(ThemeManager.getInstance().getSecondaryColor());
            button.setForeground(ThemeManager.getInstance().getTextColor());
            button.setFocusable(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(120, 40));

            final String view = views[i];
            button.addActionListener(e -> switchView(view));
            panel.add(button);
        }

        return panel;
    }

    private void registerListeners() {
        // Number pad listener
        numberPad.addNumberPadListener(this::handleNumberPadAction);

        // Cart update listener
        cartPanel.addCartUpdateListener(this::handleCartUpdate);

        // Keyboard shortcuts
        registerKeyboardShortcut();
    }

    private void registerKeyboardShortcut() {
        // Ctrl+F for search
        getRootPane().registerKeyboardAction(e -> productsPanel.focusSearch(),
                KeyStroke.getKeyStroke("ctrl F"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Escape to clear input
        getRootPane().registerKeyboardAction(e -> clearInput(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void handleNumberPadAction(String key) {
        switch (key) {
            case NumberPad.ENTER -> handleEnter();
            case NumberPad.DELETE -> handleDelete();
            case NumberPad.SETTINGS -> switchView(SETTINGS_VIEW);
            case NumberPad.SEARCH -> productsPanel.focusSearch();
            case NumberPad.PRINT -> handlePrint();
            default -> {
                // Numeric input handled by ApplicationState
                updateMetricsFromInput();
            }
        }
    }

    private void handleEnter() {
        if (currentView.equals(CHECKOUT_VIEW)) {
            processCheckout();
        } else {
            // Switch to checkout view
            switchView(CHECKOUT_VIEW);
        }
    }

    private void handleDelete() {
        // Delete from cart if item selected
        int selectedIndex = cartPanel.getSelectedIndex();
        if (selectedIndex >= 0) {
            ApplicationState.getInstance().getCart().removeItem(selectedIndex);
            cartPanel.updateCart();
            updateMetrics();
        }
    }

    private void handlePrint() {
        // Print current cart as receipt
        if (!ApplicationState.getInstance().getCart().isEmpty()) {
            processCheckout();
        }
    }

    private void handleCartUpdate() {
        updateMetrics();
        if (currentView.equals(CHECKOUT_VIEW)) {
            checkoutPanel.updateCheckout();
        }
    }

    private void updateMetricsFromInput() {
        String input = ApplicationState.getInstance().getCurrentInput();
        if (!input.isEmpty()) {
            metricPanels[3].setValue("Qty: " + input);
        }
    }

    private void processCheckout() {
        Cart cart = ApplicationState.getInstance().getCart();

        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot checkout with an empty cart.",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm checkout
        boolean confirmed = ConfirmationDialog.confirm(this,
                "Confirm Checkout",
                "Process this transaction?<br>Total: " +
                        String.format("$%.2f", cart.getTotal()));

        if (!confirmed) {
            return;
        }

        // Create transaction
        List<TransactionItem> items = new ArrayList<>();
        cart.getItems().forEach(item -> items.add(new TransactionItem(item)));

        Transaction transaction = new Transaction(
                0,
                LocalDateTime.now(),
                items,
                cart.getTotal()
        );

        // Save transaction
        int transactionId = TransactionDAO.getInstance().saveTransaction(transaction);
        if (transactionId > 0) {
            // Generate and save receipt
            String receiptContent = transaction.generateReceipt(
                    Config.getInstance().getStoreName(),
                    Config.getInstance().getStoreAddress()
            );
            String receiptPath = TransactionDAO.getInstance().saveReceipt(transaction, receiptContent);
            transaction.setReceiptPath(receiptPath);

            // Update transaction with ID
            transaction = new Transaction(transactionId, transaction.getTimestamp(),
                    transaction.getItems(), transaction.getTotal());

            // Show receipt
            ReceiptDialog.showReceipt(this, transaction);

            // Clear cart
            ApplicationState.getInstance().clearCart();
            cartPanel.clearCart();
            checkoutPanel.clearCheckout();
            updateMetrics();

            Logger.info("Transaction #" + transactionId + " completed successfully");

            // Switch back to products view
            switchView(PRODUCTS_VIEW);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to process transaction. Please try again.",
                    "Transaction Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchView(String view) {
        currentView = view;
        cardLayout.show(mainContent, view);

        // Update view-specific content
        if (view.equals(CHECKOUT_VIEW)) {
            checkoutPanel.updateCheckout();
        } else if (view.equals(HISTORY_VIEW)) {
            // History panel loads data on creation
        } else if (view.equals(PRODUCTS_VIEW)) {
            productsPanel.clearSearch();
        }

        // Update window title
        setTitle(Config.getInstance().getStoreName() + " - " +
                view.substring(0, 1).toUpperCase() + view.substring(1).toLowerCase());
    }

    private void showProductsView() {
        switchView(PRODUCTS_VIEW);
    }

    private void clearInput() {
        ApplicationState.getInstance().clearInput();
        numberPad.clearDisplay();
        updateMetrics();
    }

    private void updateMetrics() {
        Cart cart = ApplicationState.getInstance().getCart();

        // Total
        metricPanels[0].setPriceValue(cart.getTotal());

        // Items count
        metricPanels[1].setValue(String.valueOf(cart.getItemCount()));

        // Total quantity
        metricPanels[2].setValue(String.format("%.1f", cart.getTotalQuantity()));

        // Selected product (from input)
        String input = ApplicationState.getInstance().getCurrentInput();
        if (!input.isEmpty()) {
            metricPanels[3].setValue("Qty: " + input);
        } else {
            metricPanels[3].setValue("--");
        }
    }

    /**
     * Updates all theme-dependent components.
     */
    public void updateTheme() {
        ThemeManager.getInstance().applyTheme();

        // Update all panels
        productsPanel.updateTheme();
        cartPanel.updateTheme();
        checkoutPanel.updateTheme();
        historyPanel.updateTheme();
        settingsPanel.updateTheme();
        numberPad.updateTheme();

        for (MetricPanel panel : metricPanels) {
            panel.updateTheme();
        }

        SwingUtilities.updateComponentTreeUI(this);
    }
}