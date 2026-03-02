package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.Department;
import pos.model.PendingCartItem;
import pos.model.Product;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel displaying the current item being worked on and cart total.
 * Shows price per unit, quantity, weight, and total for the pending item.
 * QTY and WT fields are clickable to select input mode.
 */
public class CurrentItemPanel extends JPanel implements ApplicationState.StateChangeListener {
    private JLabel pricePerLbLabel;
    private JLabel qtyValueLabel;
    private JLabel weightValueLabel;
    private JLabel itemTotalLabel;
    private JLabel productNameLabel;
    private JLabel cartTotalLabel;
    private JLabel departmentLabel;

    private JPanel qtyPanel;
    private JPanel weightPanel;
    private boolean qtySelected = false;
    private boolean weightSelected = false;

    public CurrentItemPanel() {
        setLayout(new BorderLayout(10, 5));
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setBackground(ThemeManager.getInstance().getOrangeColor());

        // Top row: Department and Cart Total
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        departmentLabel = new JLabel("Deli");
        departmentLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        departmentLabel.setForeground(Color.WHITE);
        topRow.add(departmentLabel, BorderLayout.WEST);

        cartTotalLabel = new JLabel("Cart: $0.00");
        cartTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        cartTotalLabel.setForeground(Color.WHITE);
        topRow.add(cartTotalLabel, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // Center: Product name (prominent)
        productNameLabel = new JLabel("Select a product to begin", SwingConstants.CENTER);
        productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        productNameLabel.setForeground(Color.WHITE);
        add(productNameLabel, BorderLayout.CENTER);

        // Bottom: Metrics grid with clickable QTY and WT
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);

        pricePerLbLabel = createMetricValue();
        qtyValueLabel = createMetricValue();
        weightValueLabel = createMetricValue();
        itemTotalLabel = createMetricValue();

        metricsPanel.add(createStaticMetricPanel("Price/lb", pricePerLbLabel));
        metricsPanel.add(createClickableMetricPanel("QTY", qtyValueLabel, () -> selectQty()));
        metricsPanel.add(createClickableMetricPanel("WT (lb)", weightValueLabel, () -> selectWeight()));
        metricsPanel.add(createStaticMetricPanel("TOTAL", itemTotalLabel));

        add(metricsPanel, BorderLayout.SOUTH);

        // Register for state changes
        ApplicationState.getInstance().addStateChangeListener(this);

        // Initial update
        updateDepartment();
        updateCartTotal();
    }

    private JLabel createMetricValue() {
        JLabel label = new JLabel("--", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createStaticMetricPanel(String name, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(5, 3));
        panel.setOpaque(false);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(new Color(255, 255, 255, 180));

        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createClickableMetricPanel(String name, JLabel valueLabel, Runnable onClick) {
        JPanel panel = new JPanel(new BorderLayout(5, 3));
        panel.setOpaque(false);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(255, 255, 255, 200));

        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);

        // Store reference for selection state
        if (name.equals("QTY")) {
            qtyPanel = panel;
        } else if (name.equals("WT (lb)")) {
            weightPanel = panel;
        }

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(255, 255, 255, 30));
                panel.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if ((name.equals("QTY") && !qtySelected) || (name.equals("WT (lb)") && !weightSelected)) {
                    panel.setOpaque(false);
                }
            }
        });

        return panel;
    }

    private void selectQty() {
        qtySelected = true;
        weightSelected = false;
        updateSelectionVisuals();
        ApplicationState.getInstance().setInputMode(ApplicationState.InputMode.QUANTITY);
        notifyModeSelected("QUANTITY");
    }

    private void selectWeight() {
        qtySelected = false;
        weightSelected = true;
        updateSelectionVisuals();
        ApplicationState.getInstance().setInputMode(ApplicationState.InputMode.WEIGHT);
        notifyModeSelected("WEIGHT");
    }

    private void updateSelectionVisuals() {
        // QTY panel
        if (qtyPanel != null) {
            if (qtySelected) {
                qtyPanel.setBackground(new Color(255, 255, 255, 60));
                qtyPanel.setOpaque(true);
            } else {
                qtyPanel.setOpaque(false);
            }
        }

        // Weight panel
        if (weightPanel != null) {
            if (weightSelected) {
                weightPanel.setBackground(new Color(255, 255, 255, 60));
                weightPanel.setOpaque(true);
            } else {
                weightPanel.setOpaque(false);
            }
        }
    }

    private void notifyModeSelected(String mode) {
        // This could be used to update the number pad display
    }

    /**
     * Updates the display with the current pending item.
     */
    public void updatePendingItem(PendingCartItem item) {
        if (item == null) {
            pricePerLbLabel.setText("--");
            qtyValueLabel.setText("--");
            weightValueLabel.setText("--");
            itemTotalLabel.setText("$0.00");
            productNameLabel.setText("Select a product to begin");
            productNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            qtySelected = false;
            weightSelected = false;
            updateSelectionVisuals();
        } else {
            Product product = item.getProduct();
            pricePerLbLabel.setText(Utility.formatPrice(product.getPrice()));
            qtyValueLabel.setText(String.format("%.0f", item.getQuantity()));
            weightValueLabel.setText(String.format("%.2f", item.getWeight()));
            itemTotalLabel.setText(Utility.formatPrice(item.getTotalPrice()));
            productNameLabel.setText(product.getName());
            productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

            // Auto-select weight mode when a new product is selected
            if (item.getWeight() == 0) {
                selectWeight();
            }
        }
    }

    /**
     * Updates the quantity display.
     */
    public void updateQuantity(double quantity) {
        qtyValueLabel.setText(String.format("%.0f", quantity));
    }

    /**
     * Updates the weight display.
     */
    public void updateWeight(double weight) {
        weightValueLabel.setText(String.format("%.2f", weight));

        // Also update item total if we have a pending item
        PendingCartItem item = ApplicationState.getInstance().getPendingItem();
        if (item != null) {
            itemTotalLabel.setText(Utility.formatPrice(item.getTotalPrice()));
        }
    }

    /**
     * Updates the cart total display.
     */
    public void updateCartTotal() {
        double total = ApplicationState.getInstance().getCart().getTotal();
        cartTotalLabel.setText("Cart: " + Utility.formatPrice(total));
    }

    /**
     * Updates the department display.
     */
    public void updateDepartment() {
        Department dept = ApplicationState.getInstance().getCurrentDepartment();
        departmentLabel.setText(dept.getDisplayName());
    }

    /**
     * Clears the display.
     */
    public void clearDisplay() {
        pricePerLbLabel.setText("--");
        qtyValueLabel.setText("--");
        weightValueLabel.setText("--");
        itemTotalLabel.setText("$0.00");
        productNameLabel.setText("Select a product to begin");
        productNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        qtySelected = false;
        weightSelected = false;
        updateSelectionVisuals();
    }

    /**
     * Gets whether quantity mode is selected.
     */
    public boolean isQtySelected() {
        return qtySelected;
    }

    /**
     * Gets whether weight mode is selected.
     */
    public boolean isWeightSelected() {
        return weightSelected;
    }

    @Override
    public void onPendingItemChanged(PendingCartItem item) {
        SwingUtilities.invokeLater(() -> updatePendingItem(item));
    }

    @Override
    public void onDepartmentChanged(Department department) {
        SwingUtilities.invokeLater(() -> {
            updateDepartment();
            clearDisplay();
        });
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getOrangeColor());
        revalidate();
        repaint();
    }
}
