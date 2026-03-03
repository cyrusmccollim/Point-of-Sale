package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class CartSummaryPanel extends JPanel {
    private JLabel totalLabel;
    private JLabel itemCountLabel;

    public CartSummaryPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, ThemeManager.getInstance().getSeparatorColor()),
                new EmptyBorder(16, 16, 12, 16)
        ));

        // Top: item count
        itemCountLabel = new JLabel("0 items");
        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemCountLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        itemCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(itemCountLabel);

        add(Box.createVerticalStrut(4));

        // Bottom: Total label + value side by side
        JPanel bottomRow = new JPanel(new BorderLayout(8, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel totalTextLabel = new JLabel("Total");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalTextLabel.setForeground(ThemeManager.getInstance().getTextColor());
        bottomRow.add(totalTextLabel, BorderLayout.WEST);

        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        bottomRow.add(totalLabel, BorderLayout.EAST);

        add(bottomRow);
    }

    public void updateSummary() {
        double total = ApplicationState.getInstance().getCart().getTotal();
        int count = ApplicationState.getInstance().getCart().getItemCount();
        totalLabel.setText(Utility.formatPrice(total));
        itemCountLabel.setText(count + " item" + (count != 1 ? "s" : ""));
    }

    public void clear() {
        totalLabel.setText("$0.00");
        itemCountLabel.setText("0 items");
    }

    public void updateTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        setBackground(tm.getPanelBackgroundColor());
        setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, tm.getSeparatorColor()),
                new EmptyBorder(12, 16, 12, 16)
        ));
        itemCountLabel.setForeground(tm.getTextSecondaryColor());
        totalLabel.setForeground(tm.getOrangeColor());
        revalidate();
        repaint();
    }
}
