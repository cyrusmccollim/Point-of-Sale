package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.IconManager;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CartPanel extends JPanel {
    private final JPanel itemsContainer;
    private JLabel totalLabel;
    private JLabel itemCountLabel;
    private final List<CartUpdateListener> listeners = new ArrayList<>();
    private final List<CartItemCard> itemCards = new ArrayList<>();

    public CartPanel() {
        setLayout(new BorderLayout(3, 3));
        setBorder(new EmptyBorder(3, 3, 3, 3));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createHeaderPanel(), BorderLayout.NORTH);

        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel("Cart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        itemCountLabel = new JLabel("0 items");
        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        itemCountLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(itemCountLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel totalTextLabel = new JLabel("Total:");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalTextLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(totalTextLabel, BorderLayout.WEST);

        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        panel.add(totalLabel, BorderLayout.EAST);

        return panel;
    }

    public void updateCart() {
        itemsContainer.removeAll();
        itemCards.clear();

        List<CartItem> items = ApplicationState.getInstance().getCart().getItems();
        for (int i = 0; i < items.size(); i++) {
            CartItemCard card = new CartItemCard(items.get(i), i);
            itemCards.add(card);
            itemsContainer.add(card);
        }

        itemsContainer.add(Box.createVerticalGlue());
        updateTotal();
        notifyListeners();
        itemsContainer.revalidate();
        itemsContainer.repaint();
    }

    private void updateTotal() {
        double total = ApplicationState.getInstance().getCart().getTotal();
        int count    = ApplicationState.getInstance().getCart().getItemCount();
        totalLabel.setText(Utility.formatPrice(total));
        itemCountLabel.setText(count + " item" + (count != 1 ? "s" : ""));
    }

    public void clearCart() {
        itemsContainer.removeAll();
        itemCards.clear();
        totalLabel.setText("$0.00");
        itemCountLabel.setText("0 items");
    }

    public void addCartUpdateListener(CartUpdateListener l) { listeners.add(l); }
    private void notifyListeners() { listeners.forEach(CartUpdateListener::onCartUpdated); }

    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());
        for (CartItemCard c : itemCards) c.updateTheme();
        revalidate();
        repaint();
    }

    public interface CartUpdateListener { void onCartUpdated(); }

    private class CartItemCard extends JPanel {
        private final CartItem item;
        private final int index;

        CartItemCard(CartItem item, int index) {
            this.item  = item;
            this.index = index;
            initialize();
        }

        private void initialize() {
            setLayout(new BorderLayout(8, 0));
            setBorder(new CompoundBorderNoLine(
                    new MatteBorder(0, 0, 1, 0, ThemeManager.getInstance().getBorderColor()),
                    new EmptyBorder(12, 14, 12, 14)
            ));
            setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
            setOpaque(true);

            // Left: product info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(item.getDisplayName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameLabel.setForeground(ThemeManager.getInstance().getTextColor());
            infoPanel.add(nameLabel);

            String details = item.isWeighedItem()
                    ? String.format("%.0f x %.2f lb @ %s/lb", item.getQuantity(), item.getWeight(),
                                    Utility.formatPrice(item.getUnitPrice()))
                    : String.format("%.0f @ %s", item.getQuantity(), Utility.formatPrice(item.getUnitPrice()));
            JLabel detailsLabel = new JLabel(details);
            detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            detailsLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            infoPanel.add(detailsLabel);

            add(infoPanel, BorderLayout.CENTER);

            // Right: price + remove
            JPanel rightPanel = new JPanel(new BorderLayout(6, 0));
            rightPanel.setOpaque(false);

            JLabel priceLabel = new JLabel(Utility.formatPrice(item.getTotalPrice()));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            priceLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
            rightPanel.add(priceLabel, BorderLayout.CENTER);

            JButton removeBtn = new JButton("✕");
            removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            removeBtn.setPreferredSize(new Dimension(20, 20));
            removeBtn.setFocusable(false);
            removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeBtn.setBackground(new Color(0xDC2626));
            removeBtn.setForeground(Color.WHITE);
            removeBtn.setBorder(new EmptyBorder(2, 2, 2, 2));
            removeBtn.setOpaque(true);
            removeBtn.addActionListener(e -> {
                ApplicationState.getInstance().getCart().removeItem(index);
                updateCart();
            });
            removeBtn.getModel().addChangeListener(e ->
                    removeBtn.setBackground(removeBtn.getModel().isRollover()
                            ? new Color(0xB91C1C) : new Color(0xDC2626)));
            rightPanel.add(removeBtn, BorderLayout.EAST);

            add(rightPanel, BorderLayout.EAST);
        }

        public void updateTheme() {
            setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
            setBorder(new CompoundBorderNoLine(
                    new MatteBorder(0, 0, 1, 0, ThemeManager.getInstance().getBorderColor()),
                    new EmptyBorder(12, 14, 12, 14)
            ));
            revalidate();
            repaint();
        }
    }

    // Simple compound border (no lineBorder)
    private static class CompoundBorderNoLine extends javax.swing.border.AbstractBorder {
        private final javax.swing.border.Border outside, inside;
        CompoundBorderNoLine(javax.swing.border.Border outside, javax.swing.border.Border inside) {
            this.outside = outside; this.inside = inside;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outside.paintBorder(c, g, x, y, w, h);
            Insets oi = outside.getBorderInsets(c);
            inside.paintBorder(c, g, x + oi.left, y + oi.top,
                    w - oi.left - oi.right, h - oi.top - oi.bottom);
        }
        @Override public Insets getBorderInsets(Component c) {
            Insets oi = outside.getBorderInsets(c);
            Insets ii = inside.getBorderInsets(c);
            return new Insets(oi.top+ii.top, oi.left+ii.left, oi.bottom+ii.bottom, oi.right+ii.right);
        }
    }
}
