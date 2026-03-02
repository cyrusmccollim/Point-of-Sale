package pos.ui.dialogs;

import pos.app.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A dialog for confirming actions.
 */
public class ConfirmationDialog extends JDialog {
    private boolean confirmed = false;

    public ConfirmationDialog(Frame parent, String title, String message) {
        super(parent, title, true);
        initialize(message);
    }

    public ConfirmationDialog(Dialog parent, String title, String message) {
        super(parent, title, true);
        initialize(message);
    }

    private void initialize(String message) {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        // Message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        messagePanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(ThemeManager.getInstance().getTextColor());
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        add(messagePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setBackground(ThemeManager.getInstance().getAccentColor());
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusable(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(ThemeManager.getInstance().getSecondaryColor());
        cancelButton.setForeground(ThemeManager.getInstance().getTextColor());
        cancelButton.setFocusable(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Dialog settings
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * Shows the dialog and returns whether the user confirmed.
     *
     * @return true if confirmed, false otherwise
     */
    public boolean showConfirmation() {
        setVisible(true);
        return confirmed;
    }

    /**
     * Static method to quickly show a confirmation dialog.
     *
     * @param parent The parent frame
     * @param title The dialog title
     * @param message The message to display
     * @return true if confirmed, false otherwise
     */
    public static boolean confirm(Frame parent, String title, String message) {
        ConfirmationDialog dialog = new ConfirmationDialog(parent, title, message);
        return dialog.showConfirmation();
    }

    /**
     * Static method to quickly show a confirmation dialog.
     *
     * @param parent The parent dialog
     * @param title The dialog title
     * @param message The message to display
     * @return true if confirmed, false otherwise
     */
    public static boolean confirm(Dialog parent, String title, String message) {
        ConfirmationDialog dialog = new ConfirmationDialog(parent, title, message);
        return dialog.showConfirmation();
    }
}