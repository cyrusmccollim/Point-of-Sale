package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.IconManager;
import pos.util.UIFactory;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class CheckoutPanel extends JPanel {
    private final JLabel totalLabel     = new JLabel("$0.00");
    //private final JLabel itemCountLabel = new JLabel("0 items");
    private JPanel itemsContainer;

    public CheckoutPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createTotalPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel("Checkout");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        headerPanel.setBorder(new EmptyBorder(0, 0, 6, 0));

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

        // Left: total label + amount stacked
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
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(emptyLabel);

            JLabel hintLabel = new JLabel("Add products from the Products page", SwingConstants.CENTER);
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hintLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(hintLabel);

            itemsContainer.add(emptyPanel);
        } else {
            for (int i = 0; i < items.size(); i++) {
                itemsContainer.add(createCheckoutItemPanel(items.get(i), i));
            }
        }

        itemsContainer.add(Box.createVerticalGlue());
        itemsContainer.revalidate();
        itemsContainer.repaint();

        totalLabel.setText(Utility.formatPrice(ApplicationState.getInstance().getCart().getTotal()));
    }

    private JPanel createCheckoutItemPanel(CartItem item, int index) {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new MatteBorder(0, 0, 1, 0, ThemeManager.getInstance().getBorderColor()));
        panel.setOpaque(true);
        Insets inner = new Insets(12, 14, 12, 14);
        panel.setBorder(new javax.swing.border.CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ThemeManager.getInstance().getBorderColor()),
                new EmptyBorder(inner.top, inner.left, inner.bottom, inner.right)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

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

        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 0));
        rightPanel.setOpaque(false);

        JLabel priceLabel = new JLabel(Utility.formatPrice(item.getTotalPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        priceLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        rightPanel.add(priceLabel, BorderLayout.CENTER);

        JButton removeBtn = new JButton("✕");
        removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        removeBtn.setPreferredSize(new Dimension(22, 22));
        removeBtn.setFocusable(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.setBackground(new Color(0xDC2626));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setBorder(new EmptyBorder(2, 2, 2, 2));
        removeBtn.setOpaque(true);
        removeBtn.addActionListener(e -> {
            ApplicationState.getInstance().getCart().removeItem(index);
            updateCheckout();
        });
        removeBtn.getModel().addChangeListener(e ->
                removeBtn.setBackground(removeBtn.getModel().isRollover()
                        ? new Color(0xB91C1C) : new Color(0xDC2626)));
        rightPanel.add(removeBtn, BorderLayout.EAST);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    public void clearCheckout() {
        itemsContainer.removeAll();
        totalLabel.setText("$0.00");
    }

    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());
        revalidate();
        repaint();
    }
}
