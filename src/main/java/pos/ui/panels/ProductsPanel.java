package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.Department;
import pos.model.Product;
import pos.ui.components.ProductCard;
import pos.util.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class ProductsPanel extends JPanel implements ApplicationState.StateChangeListener {
    private final JPanel productsGrid;
    private final JTextField searchField = new JTextField();
    private List<ProductCard> productCards;

    public ProductsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createSearchPanel(), BorderLayout.NORTH);

        productsGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        productsGrid.setBackground(ThemeManager.getInstance().getBackgroundColor());
        productsGrid.setBorder(new EmptyBorder(8, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(productsGrid);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        ApplicationState.getInstance().addStateChangeListener(this);
        loadProducts();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new MatteBorder(0, 0, 1, 0, ThemeManager.getInstance().getBorderColor()));

        // Inner padding panel
        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(6, 8, 6, 8));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        searchField.setForeground(ThemeManager.getInstance().getTextColor());
        searchField.setBorder(new EmptyBorder(4, 4, 4, 4));
        searchField.setPreferredSize(new Dimension(0, 40));
        searchField.setToolTipText("Search products by name or CPU code (Ctrl+F)");
        searchField.putClientProperty("JTextField.placeholderText", "Search products…");
        searchField.putClientProperty("JTextField.leadingIcon",
                IconManager.getInstance().getIcon(IconManager.SEARCH, 16, 16));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filterProducts(); }
            @Override public void removeUpdate(DocumentEvent e)  { filterProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { filterProducts(); }
        });

        inner.add(searchField, BorderLayout.CENTER);
        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    private void loadProducts() {
        productsGrid.removeAll();
        productCards = new java.util.ArrayList<>();
        for (Product p : ApplicationState.getInstance().getProducts()) {
            ProductCard card = new ProductCard(p);
            productsGrid.add(card);
            productCards.add(card);
        }
        productsGrid.revalidate();
        productsGrid.repaint();
    }

    private void filterProducts() {
        String q = searchField.getText().trim().toLowerCase();
        productsGrid.removeAll();
        for (ProductCard card : productCards) {
            Product p = card.getProduct();
            if (q.isEmpty() || p.getName().toLowerCase().contains(q)
                    || p.getCpu().toLowerCase().contains(q)) {
                productsGrid.add(card);
            }
        }
        productsGrid.revalidate();
        productsGrid.repaint();
    }

    public void reloadProducts() {
        searchField.setText("");
        loadProducts();
    }

    public void refreshProducts() {
        ApplicationState.getInstance().initialize();
        loadProducts();
    }

    public void clearSearch() {
        searchField.setText("");
        productsGrid.removeAll();
        for (ProductCard card : productCards) productsGrid.add(card);
        productsGrid.revalidate();
        productsGrid.repaint();
    }

    public void focusSearch() { searchField.requestFocusInWindow(); }

    @Override public void onDepartmentChanged(Department department) {
        SwingUtilities.invokeLater(this::reloadProducts);
    }
    @Override public void onProductsChanged(List<Product> products) {
        SwingUtilities.invokeLater(this::reloadProducts);
    }

    public void updateTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        setBackground(tm.getBackgroundColor());
        productsGrid.setBackground(tm.getBackgroundColor());
        searchField.setBackground(tm.getPanelBackgroundColor());
        searchField.setForeground(tm.getTextColor());
        for (ProductCard card : productCards) card.updateTheme();
        revalidate();
        repaint();
    }
}
