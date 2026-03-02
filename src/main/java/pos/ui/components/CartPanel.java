package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that displays the current shopping cart.
 */
public class CartPanel extends JPanel {
    private final DefaultListModel<String> cartModel = new DefaultListModel<>();
    private final JList<String> cartList = new JList<>(cartModel);
    private final JLabel totalLabel = new JLabel("$0.00");
    private final JLabel itemCountLabel = new JLabel("0 items");
    private final List<CartUpdateListener> listeners = new ArrayList<>();

    public CartPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Cart list
        JPanel listPanel = createListPanel();
        add(listPanel, BorderLayout.CENTER);

        // Footer with total
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLabel = new JLabel("Shopping Cart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemCountLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(itemCountLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        cartList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartList.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        cartList.setForeground(ThemeManager.getInstance().getTextColor());
        cartList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartList.setFixedCellHeight(30);

        JScrollPane scrollPane = new JScrollPane(cartList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                ThemeManager.getInstance().getSecondaryColor(), 1, true));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Remove button
        JButton removeButton = new JButton("Remove Selected");
        removeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeButton.setBackground(ThemeManager.getInstance().getOrangeColor());
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusable(false);
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener(e -> removeSelectedItem());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel totalTextLabel = new JLabel("TOTAL:");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTextLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(totalTextLabel, BorderLayout.WEST);

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        panel.add(totalLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Updates the cart display.
     */
    public void updateCart() {
        cartModel.clear();
        List<CartItem> items = ApplicationState.getInstance().getCart().getItems();

        for (CartItem item : items) {
            String display = String.format("%s x %s @ %s = %s",
                    item.getDisplayName(),
                    Utility.formatQuantity(item.getQuantity()),
                    Utility.formatPrice(item.getUnitPrice()),
                    Utility.formatPrice(item.getTotalPrice()));
            cartModel.addElement(display);
        }

        updateTotal();
        notifyListeners();
    }

    private void updateTotal() {
        double total = ApplicationState.getInstance().getCart().getTotal();
        int itemCount = ApplicationState.getInstance().getCart().getItemCount();

        totalLabel.setText(Utility.formatPrice(total));
        itemCountLabel.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));
    }

    private void removeSelectedItem() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex >= 0) {
            ApplicationState.getInstance().getCart().removeItem(selectedIndex);
            updateCart();
        }
    }

    /**
     * Clears the cart display.
     */
    public void clearCart() {
        cartModel.clear();
        totalLabel.setText("$0.00");
        itemCountLabel.setText("0 items");
    }

    /**
     * Gets the selected cart item index.
     *
     * @return The selected index, or -1 if none selected
     */
    public int getSelectedIndex() {
        return cartList.getSelectedIndex();
    }

    /**
     * Adds a listener for cart updates.
     *
     * @param listener The listener to add
     */
    public void addCartUpdateListener(CartUpdateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (CartUpdateListener listener : listeners) {
            listener.onCartUpdated();
        }
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        cartList.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        cartList.setForeground(ThemeManager.getInstance().getTextColor());

        // Update all components
        for (Component comp : getComponents()) {
            SwingUtilities.updateComponentTreeUI(comp);
        }
    }

    /**
     * Listener interface for cart updates.
     */
    public interface CartUpdateListener {
        void onCartUpdated();
    }
}