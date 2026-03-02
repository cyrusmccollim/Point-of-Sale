package pos.app;

import pos.db.DatabaseManager;
import pos.db.TransactionDAO;
import pos.model.Cart;
import pos.model.PendingCartItem;
import pos.model.Product;
import pos.model.Transaction;
import pos.model.TransactionItem;
import pos.ui.components.CartSummaryPanel;
import pos.ui.components.CurrentItemPanel;
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
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for the POS System.
 * Fixed window size at half of screen dimensions.
 */
public class POSApplication extends JFrame implements ApplicationState.StateChangeListener {
    private static POSApplication instance;

    private ProductsPanel productsPanel;
    private CartSummaryPanel cartSummaryPanel;
    private CheckoutPanel checkoutPanel;
    private HistoryPanel historyPanel;
    private SettingsPanel settingsPanel;
    private CurrentItemPanel currentItemPanel;
    private NumberPad numberPad;

    private JPanel mainContent;
    private CardLayout cardLayout;
    private static final String PRODUCTS_VIEW = "PRODUCTS";
    private static final String CHECKOUT_VIEW = "CHECKOUT";
    private static final String HISTORY_VIEW = "HISTORY";
    private static final String SETTINGS_VIEW = "SETTINGS";

    private String currentView = PRODUCTS_VIEW;

    public static POSApplication getInstance() {
        return instance;
    }

    public POSApplication() {
        instance = this;
        initialize();
    }

    private void initialize() {
        DatabaseManager.getInstance().initializeSchema();
        DatabaseManager.getInstance().ensureReceiptsDirectory();
        ApplicationState.getInstance().initialize();
        ThemeManager.getInstance().applyTheme();

        configureWindow();
        createComponents();
        layoutComponents();
        registerListeners();
        ApplicationState.getInstance().addStateChangeListener(this);
        showProductsView();

        Logger.info("POS Application initialized successfully");
    }

    private void configureWindow() {
        setTitle(Config.getInstance().getStoreName() + " - POS System");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Fixed size: 3/4 of screen width and height
        int windowWidth = (int) (screenSize.width * 0.75);
        int windowHeight = (int) (screenSize.height * 0.75);

        setSize(windowWidth, windowHeight);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setIconImage(IconManager.getInstance().getImage(IconManager.POS, 64, 64));
        } catch (Exception e) {
            Logger.warning("Could not load window icon: " + e.getMessage());
        }
    }

    private void createComponents() {
        currentItemPanel = new CurrentItemPanel();
        productsPanel = new ProductsPanel();
        cartSummaryPanel = new CartSummaryPanel();
        checkoutPanel = new CheckoutPanel();
        historyPanel = new HistoryPanel();
        settingsPanel = new SettingsPanel();
        numberPad = new NumberPad();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(0, 0));

        // Top: Current item panel
        add(currentItemPanel, BorderLayout.NORTH);

        // Center: Main content with card layout
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(ThemeManager.getInstance().getBackgroundColor());

        mainContent.add(createProductsView(), PRODUCTS_VIEW);
        mainContent.add(createCheckoutView(), CHECKOUT_VIEW);
        mainContent.add(historyPanel, HISTORY_VIEW);
        mainContent.add(settingsPanel, SETTINGS_VIEW);

        add(mainContent, BorderLayout.CENTER);

        // Right: Number pad and cart (25% of width)
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Bottom: Navigation
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.SOUTH);
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
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(320, 0));

        // Cart summary at top
        panel.add(cartSummaryPanel, BorderLayout.NORTH);

        // Number pad takes the rest
        panel.add(numberPad, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] buttonLabels = {"Products", "Checkout", "History", "Settings"};
        String[] views = {PRODUCTS_VIEW, CHECKOUT_VIEW, HISTORY_VIEW, SETTINGS_VIEW};

        for (int i = 0; i < buttonLabels.length; i++) {
            JButton button = new JButton(buttonLabels[i]);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setBackground(ThemeManager.getInstance().getSecondaryColor());
            button.setForeground(ThemeManager.getInstance().getTextColor());
            button.setFocusable(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(120, 36));

            final String view = views[i];
            button.addActionListener(e -> switchView(view));
            panel.add(button);
        }

        return panel;
    }

    private void registerListeners() {
        numberPad.addNumberPadListener(this::handleNumberPadAction);
        registerKeyboardShortcut();
    }

    private void registerKeyboardShortcut() {
        getRootPane().registerKeyboardAction(e -> productsPanel.focusSearch(),
                KeyStroke.getKeyStroke("ctrl F"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> clearInput(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> handleConfirm(),
                KeyStroke.getKeyStroke("ENTER"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void handleNumberPadAction(String key) {
        switch (key) {
            case NumberPad.CONFIRM -> handleConfirm();
            case NumberPad.DELETE -> handleDelete();
            default -> updateCurrentItemDisplay();
        }
    }

    private void updateCurrentItemDisplay() {
        ApplicationState state = ApplicationState.getInstance();
        if (state.hasPendingItem()) {
            PendingCartItem item = state.getPendingItem();
            currentItemPanel.updateQuantity(item.getQuantity());
            currentItemPanel.updateWeight(item.getWeight());
        }
    }

    private void handleConfirm() {
        ApplicationState state = ApplicationState.getInstance();

        if (state.hasPendingItem()) {
            PendingCartItem pendingItem = state.getPendingItem();

            if (pendingItem.getWeight() <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please click on 'WT (lb)' and enter a weight before adding to cart.",
                        "Weight Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Product product = pendingItem.getProduct();
            String message = String.format(
                    "<html><div style='text-align: center;'>" +
                            "<b>%s</b><br><br>" +
                            "Quantity: %.0f<br>" +
                            "Weight: %.2f lb<br>" +
                            "Price: %s/lb<br><br>" +
                            "<b>Item Total: %s</b>" +
                            "</div></html>",
                    product.getName(),
                    pendingItem.getQuantity(),
                    pendingItem.getWeight(),
                    Utility.formatPrice(product.getPrice()),
                    Utility.formatPrice(pendingItem.getTotalPrice())
            );

            boolean confirmed = ConfirmationDialog.confirm(this, "Add to Cart?", message);

            if (confirmed) {
                state.confirmPendingItem();
                cartSummaryPanel.updateSummary();
                currentItemPanel.clearDisplay();
                numberPad.clearDisplay();
                Logger.info("Added " + product.getName() + " to cart");
            }
        } else {
            if (currentView.equals(CHECKOUT_VIEW)) {
                processCheckout();
            } else if (!state.getCart().isEmpty()) {
                switchView(CHECKOUT_VIEW);
            }
        }
    }

    private void handleDelete() {
        ApplicationState state = ApplicationState.getInstance();

        if (!state.getCurrentInput().isEmpty()) {
            state.clearInput();
            numberPad.clearDisplay();
            return;
        }

        if (state.hasPendingItem()) {
            state.clearPendingItem();
            currentItemPanel.clearDisplay();
            numberPad.clearDisplay();
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

        boolean confirmed = ConfirmationDialog.confirm(this,
                "Confirm Sale",
                String.format("Process this transaction and print receipt?<br><br><b>Total: %s</b>",
                        Utility.formatPrice(cart.getTotal())));

        if (!confirmed) {
            return;
        }

        List<TransactionItem> items = new ArrayList<>();
        cart.getItems().forEach(item -> items.add(new TransactionItem(item)));

        Transaction transaction = new Transaction(0, LocalDateTime.now(), items, cart.getTotal());

        int transactionId = TransactionDAO.getInstance().saveTransaction(transaction);
        if (transactionId > 0) {
            // Create a proper transaction object with the ID
            transaction = new Transaction(transactionId, transaction.getTimestamp(),
                    transaction.getItems(), transaction.getTotal());

            // Generate PDF receipt
            String pdfPath = TransactionDAO.getInstance().saveReceiptPdf(
                    transaction,
                    Config.getInstance().getStoreName(),
                    Config.getInstance().getStoreAddress()
            );

            if (pdfPath != null) {
                transaction.setReceiptPath(pdfPath);
                Logger.info("PDF receipt saved to: " + pdfPath);
            }

            // Also generate text receipt for preview
            String receiptContent = transaction.generateReceipt(
                    Config.getInstance().getStoreName(),
                    Config.getInstance().getStoreAddress()
            );

            ReceiptDialog.showReceipt(this, transaction);

            ApplicationState.getInstance().clearCart();
            cartSummaryPanel.clear();
            checkoutPanel.clearCheckout();
            currentItemPanel.clearDisplay();

            Logger.info("Transaction #" + transactionId + " completed successfully");
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

        if (view.equals(CHECKOUT_VIEW)) {
            checkoutPanel.updateCheckout();
        } else if (view.equals(PRODUCTS_VIEW)) {
            productsPanel.clearSearch();
        }

        setTitle(Config.getInstance().getStoreName() + " - " +
                view.substring(0, 1).toUpperCase() + view.substring(1).toLowerCase());
    }

    private void showProductsView() {
        switchView(PRODUCTS_VIEW);
    }

    private void clearInput() {
        ApplicationState state = ApplicationState.getInstance();

        if (state.hasPendingItem()) {
            state.clearPendingItem();
            currentItemPanel.clearDisplay();
        }

        state.clearInput();
        numberPad.clearDisplay();
    }

    @Override
    public void onPendingItemChanged(PendingCartItem item) {
        // NumberPad handles its own mode updates
    }

    public void updateTheme() {
        ThemeManager.getInstance().applyTheme();
        productsPanel.updateTheme();
        cartSummaryPanel.updateTheme();
        checkoutPanel.updateTheme();
        historyPanel.updateTheme();
        settingsPanel.updateTheme();
        numberPad.updateTheme();
        currentItemPanel.updateTheme();
        SwingUtilities.updateComponentTreeUI(this);
    }
}
