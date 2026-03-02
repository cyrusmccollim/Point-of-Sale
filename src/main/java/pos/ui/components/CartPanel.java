package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.IconManager;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that displays the current shopping cart with item cards.
 * Each item card shows the product name, weight, quantity, and total.
 */
public class CartPanel extends JPanel {
    private final JPanel itemsContainer;
    private final JLabel totalLabel;
    private final JLabel itemCountLabel;
    private final List<CartUpdateListener> listeners = new ArrayList<>();
    private final List<CartItemCard> itemCards = new ArrayList<>();

    public CartPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Items container with scroll
        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

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

        JLabel titleLabel = new JLabel("Cart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        itemCountLabel = new JLabel("0 items");
        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemCountLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(itemCountLabel, BorderLayout.EAST);

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

        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        panel.add(totalLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Updates the cart display.
     */
    public void updateCart() {
        itemsContainer.removeAll();
        itemCards.clear();

        List<CartItem> items = ApplicationState.getInstance().getCart().getItems();

        for (int i = 0; i < items.size(); i++) {
            CartItemCard card = new CartItemCard(items.get(i), i);
            itemCards.add(card);
            itemsContainer.add(card);
            itemsContainer.add(Box.createVerticalStrut(5));
        }

        // Add glue to push items to top
        itemsContainer.add(Box.createVerticalGlue());

        updateTotal();
        notifyListeners();

        itemsContainer.revalidate();
        itemsContainer.repaint();
    }

    private void updateTotal() {
        double total = ApplicationState.getInstance().getCart().getTotal();
        int itemCount = ApplicationState.getInstance().getCart().getItemCount();

        totalLabel.setText(Utility.formatPrice(total));
        itemCountLabel.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));
    }

    /**
     * Clears the cart display.
     */
    public void clearCart() {
        itemsContainer.removeAll();
        itemCards.clear();
        totalLabel.setText("$0.00");
        itemCountLabel.setText("0 items");
    }

    /**
     * Gets the selected cart item index (for compatibility, returns -1).
     */
    public int getSelectedIndex() {
        return -1; // No single selection in new design
    }

    /**
     * Adds a listener for cart updates.
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
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());

        for (CartItemCard card : itemCards) {
            card.updateTheme();
        }

        revalidate();
        repaint();
    }

    /**
     * Listener interface for cart updates.
     */
    public interface CartUpdateListener {
        void onCartUpdated();
    }

    /**
     * A card component representing a single cart item.
     */
    private class CartItemCard extends JPanel {
        private final CartItem item;
        private final int index;

        public CartItemCard(CartItem item, int index) {
            this.item = item;
            this.index = index;
            initialize();
        }

        private void initialize() {
            setLayout(new BorderLayout(10, 5));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                    new EmptyBorder(10, 12, 10, 8)
            ));
            setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            // Left: Product info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(item.getDisplayName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setForeground(ThemeManager.getInstance().getTextColor());
            infoPanel.add(nameLabel);

            if (item.isWeighedItem()) {
                JLabel detailsLabel = new JLabel(String.format("%.0f x %.2f lb @ %s/lb",
                        item.getQuantity(),
                        item.getWeight(),
                        Utility.formatPrice(item.getUnitPrice())));
                detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                detailsLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
                infoPanel.add(detailsLabel);
            } else {
                JLabel detailsLabel = new JLabel(String.format("%.0f @ %s",
                        item.getQuantity(),
                        Utility.formatPrice(item.getUnitPrice())));
                detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                detailsLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
                infoPanel.add(detailsLabel);
            }

            add(infoPanel, BorderLayout.CENTER);

            // Right: Total and remove button
            JPanel rightPanel = new JPanel(new BorderLayout(5, 0));
            rightPanel.setOpaque(false);

            JLabel totalLabel = new JLabel(Utility.formatPrice(item.getTotalPrice()));
            totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
            rightPanel.add(totalLabel, BorderLayout.CENTER);

            JButton removeButton = new JButton();
            removeButton.setIcon(IconManager.getInstance().getIcon(IconManager.DELETE, 16, 16));
            removeButton.setToolTipText("Remove item");
            removeButton.setPreferredSize(new Dimension(28, 28));
            removeButton.setFocusable(false);
            removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeButton.setBackground(ThemeManager.getInstance().getOrangeColor());
            removeButton.setOpaque(true);
            removeButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            removeButton.addActionListener(e -> removeItem());

            // Style the button
            removeButton.getModel().addChangeListener(e -> {
                if (removeButton.getModel().isRollover()) {
                    removeButton.setBackground(new Color(220, 80, 60));
                } else {
                    removeButton.setBackground(ThemeManager.getInstance().getOrangeColor());
                }
            });

            rightPanel.add(removeButton, BorderLayout.EAST);

            add(rightPanel, BorderLayout.EAST);
        }

        private void removeItem() {
            ApplicationState.getInstance().getCart().removeItem(index);
            updateCart();
        }

        public void updateTheme() {
            setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                    new EmptyBorder(10, 12, 10, 8)
            ));
            revalidate();
            repaint();
        }
    }
}
