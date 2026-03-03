package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.UIFactory;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class CheckoutPanel extends JPanel implements ApplicationState.StateChangeListener {
    private final JLabel totalLabel = new JLabel("$0.00");
    private JPanel itemsContainer;

    public CheckoutPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(UIFactory.createPageHeader("Checkout"), BorderLayout.NORTH);
        add(createItemsPanel(),  BorderLayout.CENTER);
        add(createTotalPanel(),  BorderLayout.SOUTH);

        ApplicationState.getInstance().addStateChangeListener(this);
    }

    @Override public void onCartChanged() { SwingUtilities.invokeLater(this::updateCheckout); }

    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel totalStack = new JPanel(new GridLayout(2, 1, 0, 2));
        totalStack.setOpaque(false);
        JLabel totalTextLabel = new JLabel("TOTAL");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalTextLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        totalStack.add(totalTextLabel);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        totalStack.add(totalLabel);
        panel.add(totalStack, BorderLayout.WEST);

        JButton processButton = UIFactory.createButton("Print Receipt", ThemeManager.getInstance().getOrangeColor(), Color.WHITE, 10);
        processButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        processButton.setPreferredSize(new Dimension(160, 50));
        processButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        processButton.setIconTextGap(8);
        processButton.addActionListener(e -> pos.app.POSApplication.getInstance().processCheckout());

        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(processButton, BorderLayout.SOUTH);
        panel.add(buttonWrapper, BorderLayout.EAST);

        return panel;
    }

    public void updateCheckout() {
        itemsContainer.removeAll();
        List<CartItem> items = ApplicationState.getInstance().getCart().getItems();

        if (items.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());
            emptyPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

            JLabel emptyLabel = new JLabel("No items in cart", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            emptyLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(emptyLabel);

            JLabel hintLabel = new JLabel("Add products from the Products page", SwingConstants.CENTER);
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            hintLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(hintLabel);

            itemsContainer.add(emptyPanel);
        } else {
            for (int i = 0; i < items.size(); i++) itemsContainer.add(createItemPanel(items.get(i), i));
        }

        itemsContainer.add(Box.createVerticalGlue());
        itemsContainer.revalidate();
        itemsContainer.repaint();
        totalLabel.setText(Utility.formatPrice(ApplicationState.getInstance().getCart().getTotal()));
    }

    private JPanel createItemPanel(CartItem item, int index) {
        ThemeManager tm = ThemeManager.getInstance();

        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBackground(tm.getPanelBackgroundColor());
        panel.setOpaque(true);
        panel.setBorder(new javax.swing.border.CompoundBorder(new MatteBorder(0, 0, 1, 0, tm.getBorderColor()), new EmptyBorder(12, 14, 12, 14)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getDisplayName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        nameLabel.setForeground(tm.getTextColor());
        infoPanel.add(nameLabel);

        String details = String.format("%.0f x %.2f lb @ %s/lb", item.getQuantity(), item.getWeight(), Utility.formatPrice(item.getUnitPrice()));
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        detailsLabel.setForeground(tm.getTextSecondaryColor());
        infoPanel.add(detailsLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 0));
        rightPanel.setOpaque(false);

        JLabel priceLabel = new JLabel(Utility.formatPrice(item.getTotalPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        priceLabel.setForeground(tm.getOrangeColor());
        rightPanel.add(priceLabel, BorderLayout.CENTER);

        JButton removeBtn = makeRemoveButton();
        removeBtn.addActionListener(e -> ApplicationState.getInstance().removeCartItem(index));
        rightPanel.add(removeBtn, BorderLayout.EAST);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton makeRemoveButton() {
        JButton btn = new JButton("✕");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(0xDC2626));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(2, 2, 2, 2));
        btn.setOpaque(true);
        btn.getModel().addChangeListener(e -> btn.setBackground(btn.getModel().isRollover() ? new Color(0xB91C1C) : new Color(0xDC2626)));
        return btn;
    }

    public void clearCheckout() { itemsContainer.removeAll(); totalLabel.setText("$0.00"); }

    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());
        revalidate();
        repaint();
    }
}
