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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ProductsPanel extends JPanel implements ApplicationState.StateChangeListener {
    private final JPanel productsGrid;
    private final JTextField searchField = new JTextField();
    private List<ProductCard> productCards;
    private JScrollPane scrollPane;

    public ProductsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createSearchPanel(), BorderLayout.NORTH);

        productsGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        productsGrid.setBackground(ThemeManager.getInstance().getBackgroundColor());
        productsGrid.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(ThemeManager.getInstance().getBackgroundColor());
        gridWrapper.add(productsGrid, BorderLayout.NORTH);

        scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Keep scrollbar functional but invisible (zero width)
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.getInstance().getBackgroundColor());
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        scrollPane.setWheelScrollingEnabled(true);

        // Drag-to-scroll: installed on the viewport itself so it fires regardless of
        // which child component the cursor is over. We dispatch wheel events from
        // product cards up to the scroll pane so everything works uniformly.
        final int[] dragStartY = {0};
        final boolean[] dragging = {false};
        MouseAdapter dragScroller = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStartY[0] = e.getYOnScreen();
                dragging[0] = false;
            }
            @Override public void mouseDragged(MouseEvent e) {
                dragging[0] = true;
                int delta = dragStartY[0] - e.getYOnScreen();
                dragStartY[0] = e.getYOnScreen();
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() + delta * 2);
            }
        };
        scrollPane.getViewport().addMouseListener(dragScroller);
        scrollPane.getViewport().addMouseMotionListener(dragScroller);

        // Forward mouse wheel events from every product card to the scroll pane
        // so scrolling works even when the cursor is directly over a card.
        installWheelForwarding(productsGrid, scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        ApplicationState.getInstance().addStateChangeListener(this);
        loadProducts();
    }

    /**
     * Recursively installs a mouse-wheel listener on {@code root} and all its
     * descendants that forwards wheel events to {@code target}.
     */
    private void installWheelForwarding(JComponent root, JScrollPane target) {
        root.addMouseWheelListener(e -> {
            // Re-dispatch to the scroll pane so its normal wheel handler fires
            target.dispatchEvent(SwingUtilities.convertMouseEvent(root, e, target));
        });
        for (Component c : root.getComponents()) {
            if (c instanceof JComponent jc) installWheelForwarding(jc, target);
        }
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new MatteBorder(0, 0, 2, 0, ThemeManager.getInstance().getSeparatorColor()));

        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 10, 8, 10));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        searchField.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        searchField.setForeground(ThemeManager.getInstance().getTextColor());
        searchField.setBorder(new EmptyBorder(4, 10, 4, 4));
        searchField.setPreferredSize(new Dimension(0, 48));
        searchField.setToolTipText("Search products by name or CPU code (Ctrl+F)");
        searchField.putClientProperty("JTextField.placeholderText", "Search products…");
        searchField.putClientProperty("JTextField.leadingIcon", IconManager.getInstance().getIcon(IconManager.SEARCH, 25, 25));

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
        productCards = new ArrayList<>();
        for (Product p : ApplicationState.getInstance().getProducts()) {
            ProductCard card = new ProductCard(p);
            // Forward wheel events from each newly added card
            card.addMouseWheelListener(e ->
                scrollPane.dispatchEvent(SwingUtilities.convertMouseEvent(card, e, scrollPane)));
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
            if (q.isEmpty() || p.getName().toLowerCase().contains(q) || p.getCpu().toLowerCase().contains(q))
                productsGrid.add(card);
        }
        productsGrid.revalidate();
        productsGrid.repaint();
    }

    public void reloadProducts() { searchField.setText(""); loadProducts(); }

    public void clearSearch() {
        searchField.setText("");
        productsGrid.removeAll();
        for (ProductCard card : productCards) productsGrid.add(card);
        productsGrid.revalidate();
        productsGrid.repaint();
    }

    public void focusSearch() { searchField.requestFocusInWindow(); }

    @Override public void onDepartmentChanged(Department department) { SwingUtilities.invokeLater(this::reloadProducts); }
    @Override public void onProductsChanged(List<Product> products)  { SwingUtilities.invokeLater(this::reloadProducts); }

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
