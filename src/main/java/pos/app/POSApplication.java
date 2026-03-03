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
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class POSApplication extends JFrame implements ApplicationState.StateChangeListener {
    private static POSApplication instance;

    private ProductsPanel   productsPanel;
    private CartSummaryPanel cartSummaryPanel;
    private CheckoutPanel   checkoutPanel;
    private HistoryPanel    historyPanel;
    private SettingsPanel   settingsPanel;
    private CurrentItemPanel currentItemPanel;
    private NumberPad       numberPad;

    private JPanel mainContent;
    private CardLayout cardLayout;
    private JToggleButton[] navButtons;

    private static final String PRODUCTS_VIEW  = "PRODUCTS";
    private static final String CHECKOUT_VIEW  = "CHECKOUT";
    private static final String HISTORY_VIEW   = "HISTORY";
    private static final String SETTINGS_VIEW  = "SETTINGS";

    private String currentView = PRODUCTS_VIEW;

    public static POSApplication getInstance() { return instance; }

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

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screen.width * 0.75), (int)(screen.height * 0.75));
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
        productsPanel    = new ProductsPanel();
        cartSummaryPanel = new CartSummaryPanel();
        checkoutPanel    = new CheckoutPanel();
        historyPanel     = new HistoryPanel();
        settingsPanel    = new SettingsPanel();
        numberPad        = new NumberPad();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(0, 0));

        add(currentItemPanel, BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        mainContent  = new JPanel(cardLayout);
        mainContent.setBackground(ThemeManager.getInstance().getBackgroundColor());

        mainContent.add(createSimpleView(productsPanel), PRODUCTS_VIEW);
        mainContent.add(createSimpleView(checkoutPanel), CHECKOUT_VIEW);
        mainContent.add(historyPanel,  HISTORY_VIEW);
        mainContent.add(settingsPanel, SETTINGS_VIEW);
        add(mainContent, BorderLayout.CENTER);

        add(createRightPanel(),      BorderLayout.EAST);
        add(createNavigationPanel(), BorderLayout.SOUTH);
    }

    private JPanel createSimpleView(JPanel inner) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.setBorder(new javax.swing.border.CompoundBorder(
                new MatteBorder(0, 2, 0, 0, ThemeManager.getInstance().getSeparatorColor()),
                new EmptyBorder(8, 0, 8, 0)));
        panel.setPreferredSize(new Dimension(320, 0));
        panel.add(cartSummaryPanel, BorderLayout.NORTH);
        panel.add(numberPad,        BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNavigationPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        outer.setBorder(new MatteBorder(2, 0, 0, 0, ThemeManager.getInstance().getSeparatorColor()));
        outer.setPreferredSize(new Dimension(0, 52));

        String[] labels = {"Products", "Checkout", "History", "Settings"};
        String[] views  = {PRODUCTS_VIEW, CHECKOUT_VIEW, HISTORY_VIEW, SETTINGS_VIEW};

        JPanel tabsPanel = new JPanel(new GridLayout(1, 4, 0, 0));
        tabsPanel.setOpaque(false);

        navButtons = new JToggleButton[labels.length];
        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < labels.length; i++) {
            final String view = views[i];
            JToggleButton tab = buildNavTab(labels[i]);
            navButtons[i] = tab;
            group.add(tab);
            tab.addActionListener(e -> switchView(view));
            tabsPanel.add(tab);
        }
        navButtons[0].setSelected(true);
        syncTabColors();

        outer.add(tabsPanel, BorderLayout.CENTER);
        return outer;
    }

    private JToggleButton buildNavTab(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle hover fill
                if (getModel().isRollover() && !isSelected()) {
                    Color border = ThemeManager.getInstance().getBorderColor();
                    g2.setColor(new Color(border.getRed(), border.getGreen(), border.getBlue(), 80));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();

                super.paintComponent(g);

                // Accent underline for selected state
                if (isSelected()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g3.setColor(ThemeManager.getInstance().getAccentColor());
                    g3.setStroke(new BasicStroke(2.5f));
                    int pad = 16;
                    g3.drawLine(pad, getHeight() - 3, getWidth() - pad, getHeight() - 3);
                    g3.dispose();
                }
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 52));
        btn.setMinimumSize(new Dimension(110, 52));

        btn.getModel().addChangeListener(e -> {
            ThemeManager tm = ThemeManager.getInstance();
            btn.setForeground(btn.isSelected() ? tm.getAccentColor() : tm.getTextSecondaryColor());
            btn.setFont(new Font("Segoe UI", btn.isSelected() ? Font.BOLD : Font.PLAIN, 17));
            btn.repaint();
        });

        ThemeManager tm = ThemeManager.getInstance();
        btn.setForeground(tm.getTextSecondaryColor());
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        return btn;
    }

    private void syncTabColors() {
        if (navButtons == null) return;
        ThemeManager tm = ThemeManager.getInstance();
        for (JToggleButton btn : navButtons) {
            btn.setForeground(btn.isSelected() ? tm.getAccentColor() : tm.getTextSecondaryColor());
            btn.setFont(new Font("Segoe UI", btn.isSelected() ? Font.BOLD : Font.PLAIN, 17));
        }
    }

    private void registerListeners() {
        numberPad.addNumberPadListener(this::handleNumberPadAction);
        registerKeyboardShortcuts();
    }

    private void registerKeyboardShortcuts() {
        getRootPane().registerKeyboardAction(e -> productsPanel.focusSearch(),
                KeyStroke.getKeyStroke("ctrl F"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> clearInput(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> handleConfirm(),
                KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void handleNumberPadAction(String key) {
        switch (key) {
            case NumberPad.CONFIRM -> handleConfirm();
            case NumberPad.DELETE  -> handleDelete();
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

    public void handleConfirm() {
        ApplicationState state = ApplicationState.getInstance();

        if (state.hasPendingItem()) {
            PendingCartItem pending = state.getPendingItem();

            if (pending.getWeight() <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please click on 'WT (lb)' and enter a weight before adding to cart.",
                        "Weight Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Product product = pending.getProduct();
            String msg = String.format(
                    "<html><div style='text-align: center;'>" +
                    "<b>%s</b><br><br>Quantity: %.0f<br>Weight: %.2f lb<br>" +
                    "Price: %s/lb<br><br><b>Item Total: %s</b></div></html>",
                    product.getName(), pending.getQuantity(), pending.getWeight(),
                    Utility.formatPrice(product.getPrice()),
                    Utility.formatPrice(pending.getTotalPrice()));

            if (ConfirmationDialog.confirm(this, "Add to Cart?", msg)) {
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

    public void processCheckout() {
        Cart cart = ApplicationState.getInstance().getCart();
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot checkout with an empty cart.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ConfirmationDialog.confirm(this, "Confirm Sale",
                String.format("Process this transaction and print receipt?<br><br><b>Total: %s</b>",
                        Utility.formatPrice(cart.getTotal())))) return;

        List<TransactionItem> items = new ArrayList<>();
        cart.getItems().forEach(item -> items.add(new TransactionItem(item)));

        Transaction transaction = new Transaction(0, LocalDateTime.now(), items, cart.getTotal());
        int transactionId = TransactionDAO.getInstance().saveTransaction(transaction);

        if (transactionId > 0) {
            transaction = new Transaction(transactionId, transaction.getTimestamp(),
                    transaction.getItems(), transaction.getTotal());

            String pdfPath = TransactionDAO.getInstance().saveReceiptPdf(
                    transaction, Config.getInstance().getStoreName(),
                    Config.getInstance().getStoreAddress());
            if (pdfPath != null) {
                transaction.setReceiptPath(pdfPath);
                Logger.info("PDF receipt saved to: " + pdfPath);
            }

            ReceiptDialog.showReceipt(this, transaction);

            ApplicationState.getInstance().clearCart();
            cartSummaryPanel.clear();
            checkoutPanel.clearCheckout();
            currentItemPanel.clearDisplay();

            Logger.info("Transaction #" + transactionId + " completed successfully");
            switchView(PRODUCTS_VIEW);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to process transaction. Please try again.",
                    "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchView(String view) {
        currentView = view;
        cardLayout.show(mainContent, view);

        // Sync nav button selection
        if (navButtons != null) {
            String[] views = {PRODUCTS_VIEW, CHECKOUT_VIEW, HISTORY_VIEW, SETTINGS_VIEW};
            for (int i = 0; i < views.length; i++) {
                navButtons[i].setSelected(views[i].equals(view));
            }
            syncTabColors();
        }

        if (view.equals(CHECKOUT_VIEW)) checkoutPanel.updateCheckout();
        else if (view.equals(PRODUCTS_VIEW)) productsPanel.clearSearch();

        setTitle(Config.getInstance().getStoreName() + " - " +
                view.substring(0, 1).toUpperCase() + view.substring(1).toLowerCase());
    }

    private void showProductsView() { switchView(PRODUCTS_VIEW); }

    private void clearInput() {
        ApplicationState state = ApplicationState.getInstance();
        if (state.hasPendingItem()) {
            state.clearPendingItem();
            currentItemPanel.clearDisplay();
        }
        state.clearInput();
        numberPad.clearDisplay();
    }

    @Override public void onPendingItemChanged(PendingCartItem item) {}

    public void updateTheme() {
        ThemeManager.getInstance().applyTheme();
        productsPanel.updateTheme();
        cartSummaryPanel.updateTheme();
        checkoutPanel.updateTheme();
        historyPanel.updateTheme();
        settingsPanel.updateTheme();
        numberPad.updateTheme();
        currentItemPanel.updateTheme();
        syncTabColors();
        SwingUtilities.updateComponentTreeUI(this);
    }
}
