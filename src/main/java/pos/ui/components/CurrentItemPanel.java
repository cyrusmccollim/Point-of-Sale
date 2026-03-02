package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.Department;
import pos.model.PendingCartItem;
import pos.model.Product;
import pos.util.UIFactory;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CurrentItemPanel extends JPanel implements ApplicationState.StateChangeListener {
    private JLabel pricePerLbInlineLabel;
    private JLabel qtyValueLabel;
    private JLabel weightValueLabel;
    private JLabel itemTotalLabel;
    private JLabel productNameLabel;
    private JLabel departmentBadge;

    private JPanel qtyPanel;
    private JPanel weightPanel;
    private boolean qtySelected = false;
    private boolean weightSelected = false;

    public CurrentItemPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(6, 4));
        setBorder(new EmptyBorder(8, 14, 8, 14));
        setBackground(ThemeManager.getInstance().getOrangeColor());

        // Top row: department badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        departmentBadge = UIFactory.createBadge("Deli", new Color(0, 0, 0, 60), Color.WHITE);
        topRow.add(departmentBadge, BorderLayout.WEST);
        add(topRow, BorderLayout.NORTH);

        // Center: product name + price/lb inline
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        centerPanel.setOpaque(false);

        productNameLabel = new JLabel("Select a product", SwingConstants.CENTER);
        productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        productNameLabel.setForeground(Color.WHITE);
        centerPanel.add(productNameLabel);

        pricePerLbInlineLabel = new JLabel("", SwingConstants.CENTER);
        pricePerLbInlineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        pricePerLbInlineLabel.setForeground(new Color(255, 255, 255, 200));
        centerPanel.add(pricePerLbInlineLabel);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom: 3-metric row (QTY, WT, TOTAL)
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        metricsPanel.setOpaque(false);

        qtyValueLabel    = createMetricValue();
        weightValueLabel = createMetricValue();
        itemTotalLabel   = createMetricValue();

        qtyPanel    = createMetricPanel("QTY", qtyValueLabel, true);
        weightPanel = createMetricPanel("WT (lb)", weightValueLabel, true);
        JPanel totalPanel = createMetricPanel("TOTAL", itemTotalLabel, false);

        metricsPanel.add(qtyPanel);
        metricsPanel.add(weightPanel);
        metricsPanel.add(totalPanel);
        add(metricsPanel, BorderLayout.SOUTH);

        ApplicationState.getInstance().addStateChangeListener(this);
        updateDepartment();
    }

    private JLabel createMetricValue() {
        JLabel label = new JLabel("--", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createMetricPanel(String name, JLabel valueLabel, boolean clickable) {
        JPanel panel = new JPanel(new BorderLayout(0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = isSelectedPanel(this);
                float alpha = sel ? 0.25f : 0.15f;
                g2.setColor(new Color(1f, 1f, 1f, alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (sel) {
                    g2.setColor(new Color(1f, 1f, 1f, 0.65f));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(3, 8, 3, 8));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", clickable ? Font.BOLD : Font.PLAIN, 10));
        nameLabel.setForeground(new Color(255, 255, 255, 153));

        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        if (clickable) {
            panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (panel == qtyPanel) selectQty();
                    else if (panel == weightPanel) selectWeight();
                }
                @Override public void mouseEntered(MouseEvent e) { panel.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { panel.repaint(); }
            });
        }
        return panel;
    }

    private boolean isSelectedPanel(JPanel p) {
        return (p == qtyPanel && qtySelected) || (p == weightPanel && weightSelected);
    }

    private void selectQty() {
        qtySelected = true;
        weightSelected = false;
        qtyPanel.repaint();
        weightPanel.repaint();
        ApplicationState.getInstance().setInputMode(ApplicationState.InputMode.QUANTITY);
    }

    private void selectWeight() {
        qtySelected = false;
        weightSelected = true;
        qtyPanel.repaint();
        weightPanel.repaint();
        ApplicationState.getInstance().setInputMode(ApplicationState.InputMode.WEIGHT);
    }

    public void updatePendingItem(PendingCartItem item) {
        if (item == null) {
            qtyValueLabel.setText("--");
            weightValueLabel.setText("--");
            itemTotalLabel.setText("$0.00");
            productNameLabel.setText("Select a product");
            productNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            pricePerLbInlineLabel.setText("");
            qtySelected = false;
            weightSelected = false;
            if (qtyPanel != null) { qtyPanel.repaint(); weightPanel.repaint(); }
        } else {
            Product product = item.getProduct();
            qtyValueLabel.setText(String.format("%.0f", item.getQuantity()));
            weightValueLabel.setText(String.format("%.2f", item.getWeight()));
            itemTotalLabel.setText(Utility.formatPrice(item.getTotalPrice()));
            productNameLabel.setText(product.getName());
            productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            pricePerLbInlineLabel.setText("@ " + Utility.formatPrice(product.getPrice()) + " /lb");

            if (item.getWeight() == 0) selectWeight();
        }
        revalidate();
        repaint();
    }

    public void updateQuantity(double quantity) {
        qtyValueLabel.setText(String.format("%.0f", quantity));
    }

    public void updateWeight(double weight) {
        weightValueLabel.setText(String.format("%.2f", weight));
        PendingCartItem item = ApplicationState.getInstance().getPendingItem();
        if (item != null) itemTotalLabel.setText(Utility.formatPrice(item.getTotalPrice()));
    }

    public void updateDepartment() {
        Department dept = ApplicationState.getInstance().getCurrentDepartment();
        departmentBadge.setText(dept.getDisplayName());
    }

    public void clearDisplay() {
        updatePendingItem(null);
    }

    public boolean isQtySelected()    { return qtySelected; }
    public boolean isWeightSelected() { return weightSelected; }

    @Override public void onPendingItemChanged(PendingCartItem item) {
        SwingUtilities.invokeLater(() -> updatePendingItem(item));
    }

    @Override public void onDepartmentChanged(Department department) {
        SwingUtilities.invokeLater(() -> { updateDepartment(); clearDisplay(); });
    }

    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getOrangeColor());
        revalidate();
        repaint();
    }
}
