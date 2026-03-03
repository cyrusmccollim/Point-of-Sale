package pos.ui.dialogs;

import pos.app.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ConfirmationDialog extends JDialog {
    private boolean confirmed = false;

    public ConfirmationDialog(Frame parent, String title, String message) {
        super(parent, title, true);

        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        messagePanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(ThemeManager.getInstance().getTextColor());
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setBackground(ThemeManager.getInstance().getAccentColor());
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusable(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> { confirmed = true; dispose(); });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(ThemeManager.getInstance().getSecondaryColor());
        cancelButton.setForeground(ThemeManager.getInstance().getTextColor());
        cancelButton.setFocusable(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> { confirmed = false; dispose(); });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
    }

    public boolean showConfirmation() { setVisible(true); return confirmed; }

    public static boolean confirm(Frame parent, String title, String message) {
        return new ConfirmationDialog(parent, title, message).showConfirmation();
    }
}
