package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.PendingCartItem;
import pos.model.Product;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A clickable card component that displays product information.
 * Clicking selects the product for the pending item workflow.
 */
public class ProductCard extends JPanel implements ApplicationState.StateChangeListener {
    private final Product product;
    private boolean selected = false;

    public ProductCard(Product product) {
        this.product = product;
        initialize();
        ApplicationState.getInstance().addStateChangeListener(this);
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(150, 120));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // CPU label (smaller, top)
        gbc.gridy = 0;
        gbc.weighty = 0.2;
        JLabel cpuLabel = new JLabel(product.getCpu(), SwingConstants.CENTER);
        cpuLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cpuLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        add(cpuLabel, gbc);

        // Product name (medium, center)
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        JLabel nameLabel = new JLabel(truncate(product.getName(), 20), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(ThemeManager.getInstance().getTextColor());
        add(nameLabel, gbc);

        // Price (smaller, bottom) - shows per-lb price for weighed goods
        gbc.gridy = 2;
        gbc.weighty = 0.3;
        JLabel priceLabel = new JLabel(Utility.formatPrice(product.getPrice()) + "/lb", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        add(priceLabel, gbc);

        // Mouse listeners for interaction
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClicked();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                handleHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                handleHover(false);
            }
        });

        applyThemeColors();
    }

    private void handleClicked() {
        // Set this product as the pending item (new workflow)
        ApplicationState.getInstance().setPendingProduct(product);

        // Visual feedback - persistent selection
        selected = true;
        applySelectedStyle();
    }

    private void handleHover(boolean isHovering) {
        if (!selected) {
            if (isHovering) {
                setBackground(ThemeManager.getInstance().getSecondaryColor());
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeManager.getInstance().getAccentColor(), 2, true),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            } else {
                applyNormalStyle();
            }
        }
    }

    private void applySelectedStyle() {
        setBackground(ThemeManager.getInstance().getOrangeColor());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getOrangeColor(), 3, true),
                new EmptyBorder(9, 9, 9, 9)
        ));
    }

    private void applyNormalStyle() {
        applyThemeColors();
    }

    private void applyThemeColors() {
        setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
    }

    /**
     * Clears the selection state.
     */
    public void clearSelection() {
        selected = false;
        applyNormalStyle();
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public void onPendingItemChanged(PendingCartItem item) {
        // Check if this card should be selected
        boolean shouldSelect = item != null && item.getProduct().getCpu().equals(product.getCpu());
        if (shouldSelect != selected) {
            selected = shouldSelect;
            if (selected) {
                applySelectedStyle();
            } else {
                applyNormalStyle();
            }
        }
    }

    /**
     * Updates the card's theme colors.
     */
    public void updateTheme() {
        applyThemeColors();
        if (selected) {
            applySelectedStyle();
        }
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel label) {
                if (comp == getComponent(0)) { // CPU label
                    label.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
                } else if (comp == getComponent(1)) { // Name label
                    label.setForeground(ThemeManager.getInstance().getTextColor());
                } else if (comp == getComponent(2)) { // Price label
                    label.setForeground(ThemeManager.getInstance().getOrangeColor());
                }
            }
        }
        repaint();
    }
}