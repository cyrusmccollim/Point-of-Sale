package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.POSApplication;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentItemPanel extends JPanel implements ApplicationState.StateChangeListener {
    private JLabel pricePerLbInlineLabel;
    private JLabel qtyValueLabel;
    private JLabel weightValueLabel;
    private JLabel itemTotalLabel;
    private JLabel productNameLabel;
    private JLabel departmentBadge;
    // The name label inside the weight metric panel, so we can update it when the unit changes
    private JLabel weightNameLabel;

    private JPanel qtyPanel;
    private JPanel weightPanel;
    private boolean qtySelected    = false;
    private boolean weightSelected = false;

    private JButton removeBtn;
    private JButton confirmBtn;

    public CurrentItemPanel() {
        setLayout(new BorderLayout(6, 4));
        setBorder(new EmptyBorder(8, 14, 14, 14));
        setBackground(ThemeManager.getInstance().getOrangeColor());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        departmentBadge = UIFactory.createBadge("Deli", new Color(0, 0, 0, 60), Color.WHITE);
        departmentBadge.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        departmentBadge.setPreferredSize(new Dimension(80, 30));
        topRow.add(departmentBadge, BorderLayout.WEST);

        JLabel dateTimeLabel = new JLabel("", SwingConstants.CENTER);
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dateTimeLabel.setForeground(new Color(255, 255, 255, 200));
        topRow.add(dateTimeLabel, BorderLayout.EAST);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM YYYY \u2014 h:mm a");
        dateTimeLabel.setText(LocalDateTime.now().format(dtf));
        new Timer(1000, e -> dateTimeLabel.setText(LocalDateTime.now().format(dtf))).start();

        add(topRow, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(10, 0));
        centerContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
        centerContainer.setOpaque(false);

        removeBtn = createIconButton("✕ Cancel");
        removeBtn.setPreferredSize(new Dimension(120, 45));
        removeBtn.addActionListener(e -> ApplicationState.getInstance().clearPendingItem());

        confirmBtn = createIconButton("✓ Add");
        confirmBtn.setPreferredSize(new Dimension(120, 45));
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        confirmBtn.addActionListener(e -> POSApplication.getInstance().handleConfirm());

        JPanel namePanel = new JPanel(new GridBagLayout());
        namePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        productNameLabel = new JLabel("[No Product Selected]", SwingConstants.CENTER);
        productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        productNameLabel.setForeground(Color.WHITE);
        namePanel.add(productNameLabel, gbc);

        pricePerLbInlineLabel = new JLabel(" ", SwingConstants.CENTER);
        pricePerLbInlineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        pricePerLbInlineLabel.setForeground(new Color(255, 255, 255, 200));
        namePanel.add(pricePerLbInlineLabel, gbc);

        centerContainer.add(removeBtn,  BorderLayout.WEST);
        centerContainer.add(namePanel,   BorderLayout.CENTER);
        centerContainer.add(confirmBtn,  BorderLayout.EAST);

        removeBtn.setVisible(false);
        confirmBtn.setVisible(false);

        add(centerContainer, BorderLayout.CENTER);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        metricsPanel.setOpaque(false);

        qtyValueLabel    = createMetricValue();
        weightValueLabel = createMetricValue();
        itemTotalLabel   = createMetricValue();

        qtyValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        weightValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        itemTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        qtyPanel    = createMetricPanel("QUANTITY", qtyValueLabel, true);
        weightPanel = createMetricPanel("WEIGHT (" + Utility.getWeightUnit() + ")", weightValueLabel, true);
        JPanel totalPanel = createMetricPanel("ITEM TOTAL", itemTotalLabel, false);

        metricsPanel.add(qtyPanel);
        metricsPanel.add(weightPanel);
        metricsPanel.add(totalPanel);
        add(metricsPanel, BorderLayout.SOUTH);

        ApplicationState.getInstance().addStateChangeListener(this);
        updateDepartment();
    }

    private JButton createIconButton(String symbol) {
        JButton btn = new JButton(symbol) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, getModel().isRollover() ? 80 : 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(44, 44));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
        return btn;
    }

    private JLabel createMetricValue() {
        JLabel label = new JLabel("--", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createMetricPanel(String name, JLabel valueLabel, boolean clickable) {
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", clickable ? Font.BOLD : Font.PLAIN, 15));
        nameLabel.setForeground(new Color(255, 255, 255, 153));

        // Store the weight panel's name label so we can update it live
        if (name.startsWith("WEIGHT")) weightNameLabel = nameLabel;

        JPanel panel = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = isSelectedPanel(this);
                g2.setColor(new Color(1f, 1f, 1f, sel ? 0.25f : 0.15f));
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

        panel.add(nameLabel,  BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        if (clickable) {
            panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (panel == qtyPanel) selectQty(); else if (panel == weightPanel) selectWeight(); }
                @Override public void mouseEntered(MouseEvent e) { panel.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { panel.repaint(); }
            });
        }
        return panel;
    }

    private boolean isSelectedPanel(JPanel p) { return (p == qtyPanel && qtySelected) || (p == weightPanel && weightSelected); }

    private void selectQty() {
        if (!ApplicationState.getInstance().hasPendingItem()) return;
        qtySelected = true; weightSelected = false;
        qtyPanel.repaint(); weightPanel.repaint();
        ApplicationState.getInstance().setInputMode(ApplicationState.InputMode.QUANTITY);
    }

    private void selectWeight() {
        if (!ApplicationState.getInstance().hasPendingItem()) return;
        qtySelected = false; weightSelected = true;
        qtyPanel.repaint(); weightPanel.repaint();
        ApplicationState.getInstance().setInputMode(ApplicationState.InputMode.WEIGHT);
    }

    public void deselectMetrics() {
        qtySelected = false; weightSelected = false;
        if (qtyPanel != null) { qtyPanel.repaint(); weightPanel.repaint(); }
    }

    public void updatePendingItem(PendingCartItem item) {
        if (item == null) {
            qtyValueLabel.setText("--");
            weightValueLabel.setText("--");
            itemTotalLabel.setText("$0.00");
            productNameLabel.setText("[No Product Selected]");
            productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
            pricePerLbInlineLabel.setText(" ");
            qtySelected = false; weightSelected = false;
            if (qtyPanel != null) { qtyPanel.repaint(); weightPanel.repaint(); }
            removeBtn.setVisible(false);
            confirmBtn.setVisible(false);
        } else {
            Product product = item.getProduct();
            qtyValueLabel.setText(String.format("%.0f", item.getQuantity()));
            weightValueLabel.setText(String.format("%.2f", Utility.lbsToDisplayUnit(item.getWeight())));
            itemTotalLabel.setText(Utility.formatPrice(item.getTotalPrice()));
            productNameLabel.setText(product.getName());
            productNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
            pricePerLbInlineLabel.setText("@ " + Utility.formatUnitPrice(product.getPrice()));
            removeBtn.setVisible(true);
            confirmBtn.setVisible(true);
            if (item.getWeight() == 0 && !weightSelected && !qtySelected) selectWeight();
        }
        revalidate();
        repaint();
    }

    /** Called when the weight unit setting changes so the panel label stays accurate. */
    public void refreshWeightUnitLabel() {
        if (weightNameLabel != null) {
            weightNameLabel.setText("WEIGHT (" + Utility.getWeightUnit() + ")");
        }
    }

    public void updateDepartment() { departmentBadge.setText(ApplicationState.getInstance().getCurrentDepartment().getDisplayName()); }
    public void clearDisplay()     { updatePendingItem(null); }

    public boolean isQtySelected()    { return qtySelected; }
    public boolean isWeightSelected() { return weightSelected; }

    @Override public void onPendingItemChanged(PendingCartItem item) { SwingUtilities.invokeLater(() -> updatePendingItem(item)); }
    @Override public void onDepartmentChanged(Department department) { SwingUtilities.invokeLater(() -> { updateDepartment(); clearDisplay(); }); }
    @Override public void onInputModeChanged(ApplicationState.InputMode mode) {
        if (mode == ApplicationState.InputMode.NONE) SwingUtilities.invokeLater(this::deselectMetrics);
    }

    public void updateTheme() { setBackground(ThemeManager.getInstance().getOrangeColor()); revalidate(); repaint(); }
}
