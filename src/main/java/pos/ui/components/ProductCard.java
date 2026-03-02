package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.Product;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A clickable card component that displays product information.
 */
public class ProductCard extends JPanel {
    private final Product product;
    private boolean selected = false;

    public ProductCard(Product product) {
        this.product = product;
        initialize();
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

        // Price (smaller, bottom)
        gbc.gridy = 2;
        gbc.weighty = 0.3;
        JLabel priceLabel = new JLabel(Utility.formatPrice(product.getPrice()), SwingConstants.CENTER);
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
        // Add product to cart
        ApplicationState.getInstance().addToCart(product);

        // Visual feedback
        selected = true;
        applySelectedStyle();

        // Reset after animation
        Timer timer = new Timer(150, e -> {
            selected = false;
            applyNormalStyle();
        });
        timer.setRepeats(false);
        timer.start();
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
                BorderFactory.createLineBorder(ThemeManager.getInstance().getOrangeColor(), 2, true),
                new EmptyBorder(10, 10, 10, 10)
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

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public Product getProduct() {
        return product;
    }

    /**
     * Updates the card's theme colors.
     */
    public void updateTheme() {
        applyThemeColors();
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