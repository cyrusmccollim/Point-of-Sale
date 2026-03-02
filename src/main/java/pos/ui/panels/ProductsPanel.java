package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.Department;
import pos.model.Product;
import pos.ui.components.ProductCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * Panel that displays the product catalog in a scrollable grid.
 */
public class ProductsPanel extends JPanel implements ApplicationState.StateChangeListener {
    private final JPanel productsGrid;
    private final JTextField searchField = new JTextField();
    private List<ProductCard> productCards;

    public ProductsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Search panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Products grid
        productsGrid = new JPanel(new GridLayout(0, 4, 15, 15));
        productsGrid.setBackground(ThemeManager.getInstance().getBackgroundColor());
        productsGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(productsGrid);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Register for state changes
        ApplicationState.getInstance().addStateChangeListener(this);

        // Load products
        loadProducts();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(searchLabel, BorderLayout.WEST);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(ThemeManager.getInstance().getBackgroundColor());
        searchField.setForeground(ThemeManager.getInstance().getTextColor());
        searchField.setBorder(new EmptyBorder(5, 10, 5, 10));
        searchField.setToolTipText("Search products by name or CPU code");

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterProducts();
            }
        });

        panel.add(searchField, BorderLayout.CENTER);

        // Keyboard shortcut hint
        JLabel hintLabel = new JLabel("(Ctrl+F)");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(hintLabel, BorderLayout.EAST);

        return panel;
    }

    private void loadProducts() {
        productsGrid.removeAll();
        productCards = new java.util.ArrayList<>();

        List<Product> products = ApplicationState.getInstance().getProducts();

        for (Product product : products) {
            ProductCard card = new ProductCard(product);
            productsGrid.add(card);
            productCards.add(card);
        }

        productsGrid.revalidate();
        productsGrid.repaint();
    }

    private void filterProducts() {
        String query = searchField.getText().trim().toLowerCase();

        for (ProductCard card : productCards) {
            if (query.isEmpty()) {
                card.setVisible(true);
            } else {
                Product product = card.getProduct();
                boolean matches = product.getName().toLowerCase().contains(query) ||
                        product.getCpu().toLowerCase().contains(query);
                card.setVisible(matches);
            }
        }

        productsGrid.revalidate();
        productsGrid.repaint();
    }

    /**
     * Reloads products from the current department.
     */
    public void reloadProducts() {
        clearSearch();
        loadProducts();
    }

    /**
     * Refreshes the products from the database.
     */
    public void refreshProducts() {
        ApplicationState.getInstance().initialize();
        loadProducts();
    }

    /**
     * Clears the search field and shows all products.
     */
    public void clearSearch() {
        searchField.setText("");
        filterProducts();
    }

    /**
     * Focuses the search field.
     */
    public void focusSearch() {
        searchField.requestFocusInWindow();
    }

    @Override
    public void onDepartmentChanged(Department department) {
        SwingUtilities.invokeLater(() -> {
            reloadProducts();
        });
    }

    @Override
    public void onProductsChanged(List<Product> products) {
        SwingUtilities.invokeLater(() -> {
            reloadProducts();
        });
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        productsGrid.setBackground(ThemeManager.getInstance().getBackgroundColor());
        searchField.setBackground(ThemeManager.getInstance().getBackgroundColor());
        searchField.setForeground(ThemeManager.getInstance().getTextColor());

        for (ProductCard card : productCards) {
            card.updateTheme();
        }

        revalidate();
        repaint();
    }
}