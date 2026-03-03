package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.UIFactory;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(ThemeManager.getInstance().getBackgroundColor());
        itemsContainer.setBorder(new EmptyBorder(4, 2, 4, 2));

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

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
            emptyPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

            JLabel emptyLabel = new JLabel("No items in cart", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            emptyLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(emptyLabel);

            emptyPanel.add(Box.createVerticalStrut(6));

            JLabel hintLabel = new JLabel("Add products from the Products page", SwingConstants.CENTER);
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            hintLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
            hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(hintLabel);

            itemsContainer.add(emptyPanel);
        } else {
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) itemsContainer.add(Box.createVerticalStrut(8));
                itemsContainer.add(createItemPanel(items.get(i), i));
            }
        }

        itemsContainer.add(Box.createVerticalGlue());
        itemsContainer.revalidate();
        itemsContainer.repaint();
        totalLabel.setText(Utility.formatPrice(ApplicationState.getInstance().getCart().getTotal()));
    }

    private JPanel createItemPanel(CartItem item, int index) {
        ThemeManager tm = ThemeManager.getInstance();

        // Card panel with painted rounded background + shadow
        JPanel panel = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 1, 14, 14);
                g2.setColor(tm.getPanelBackgroundColor());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        // Left: name + details stacked vertically
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getDisplayName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(tm.getTextColor());
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));

        String details = String.format("%.0f x %s @ %s",
                item.getQuantity(), Utility.formatWeight(item.getWeight()), Utility.formatUnitPrice(item.getUnitPrice()));
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detailsLabel.setForeground(tm.getTextSecondaryColor());
        infoPanel.add(detailsLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Right: price label + delete button stacked
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        JLabel priceLabel = new JLabel(Utility.formatPrice(item.getTotalPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        priceLabel.setForeground(tm.getOrangeColor());
        priceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(priceLabel);

        rightPanel.add(Box.createVerticalStrut(4));

        JButton removeBtn = makeRemoveButton(item, index);
        removeBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(removeBtn);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton makeRemoveButton(CartItem item, int index) {
        JButton btn = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setMaximumSize(new Dimension(28, 28));
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(0xDC2626));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(2, 2, 2, 2));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.getModel().addChangeListener(e -> {
            btn.setBackground(btn.getModel().isRollover() ? new Color(0xB91C1C) : new Color(0xDC2626));
            btn.repaint();
        });
        btn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    CheckoutPanel.this,
                    "Remove \"" + item.getDisplayName() + "\" from cart?",
                    "Remove Item",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                ApplicationState.getInstance().removeCartItem(index);
            }
        });
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
