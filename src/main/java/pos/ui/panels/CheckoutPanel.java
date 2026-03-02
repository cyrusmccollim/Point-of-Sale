package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.CartItem;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel that displays checkout summary and receipt preview.
 */
public class CheckoutPanel extends JPanel {
    private final JTextArea receiptArea = new JTextArea();
    private final JLabel totalLabel = new JLabel("$0.00");

    public CheckoutPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Receipt preview
        JPanel receiptPanel = createReceiptPanel();
        add(receiptPanel, BorderLayout.CENTER);

        // Total panel
        JPanel totalPanel = createTotalPanel();
        add(totalPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLabel = new JLabel("Checkout");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JLabel previewLabel = new JLabel("Receipt Preview");
        previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        previewLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(previewLabel, BorderLayout.NORTH);

        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        receiptArea.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        receiptArea.setForeground(ThemeManager.getInstance().getTextColor());
        receiptArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                ThemeManager.getInstance().getSecondaryColor(), 1, true));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel totalTextLabel = new JLabel("TOTAL:");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTextLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(totalTextLabel, BorderLayout.WEST);

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        panel.add(totalLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Updates the checkout panel with current cart contents.
     */
    public void updateCheckout() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("================================\n");
        receipt.append("         RECEIPT PREVIEW        \n");
        receipt.append("================================\n\n");

        List<CartItem> items = ApplicationState.getInstance().getCart().getItems();

        if (items.isEmpty()) {
            receipt.append("    Cart is empty.\n");
            receipt.append("    Add products to checkout.\n\n");
        } else {
            for (CartItem item : items) {
                String line = String.format("%-20s\n", item.getDisplayName());
                receipt.append(line);
                receipt.append(String.format("  %s x %s = %s\n",
                        Utility.formatQuantity(item.getQuantity()),
                        Utility.formatPrice(item.getUnitPrice()),
                        Utility.formatPrice(item.getTotalPrice())));
            }
        }

        receipt.append("\n--------------------------------\n");
        receipt.append(String.format("%-12s%20s\n", "TOTAL:", Utility.formatPrice(
                ApplicationState.getInstance().getCart().getTotal())));
        receipt.append("================================\n");

        receiptArea.setText(receipt.toString());
        totalLabel.setText(Utility.formatPrice(ApplicationState.getInstance().getCart().getTotal()));
    }

    /**
     * Clears the checkout panel.
     */
    public void clearCheckout() {
        receiptArea.setText("");
        totalLabel.setText("$0.00");
    }

    /**
     * Gets the receipt preview text.
     *
     * @return The receipt text
     */
    public String getReceiptText() {
        return receiptArea.getText();
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        receiptArea.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        receiptArea.setForeground(ThemeManager.getInstance().getTextColor());
        revalidate();
        repaint();
    }
}