package pos.ui.panels;

import pos.app.ThemeManager;
import pos.util.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel for application settings.
 */
public class SettingsPanel extends JPanel {
    private final JTextField storeNameField = new JTextField(30);
    private final JTextField storeAddressField = new JTextField(30);
    private final JTextField receiptFolderField = new JTextField(30);
    private final JCheckBox darkModeCheckBox = new JCheckBox("Dark Mode");

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Settings form
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // Save button
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // Load current settings
        loadSettings();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Store Name
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel storeNameLabel = new JLabel("Store Name:");
        storeNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        storeNameLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(storeNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        storeNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        storeNameField.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        storeNameField.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(storeNameField, gbc);

        // Store Address
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel storeAddressLabel = new JLabel("Store Address:");
        storeAddressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        storeAddressLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(storeAddressLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        storeAddressField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        storeAddressField.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        storeAddressField.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(storeAddressField, gbc);

        // Receipt Folder
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel receiptFolderLabel = new JLabel("Receipt Folder:");
        receiptFolderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        receiptFolderLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(receiptFolderLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        receiptFolderField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        receiptFolderField.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        receiptFolderField.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(receiptFolderField, gbc);

        // Theme
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        themeLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(themeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        darkModeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        darkModeCheckBox.setBackground(ThemeManager.getInstance().getBackgroundColor());
        darkModeCheckBox.setForeground(ThemeManager.getInstance().getTextColor());
        darkModeCheckBox.addActionListener(e -> {
            ThemeManager.getInstance().setDarkMode(darkModeCheckBox.isSelected());
        });
        panel.add(darkModeCheckBox, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JButton saveButton = new JButton("Save Settings");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(ThemeManager.getInstance().getAccentColor());
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusable(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveSettings());
        panel.add(saveButton);

        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resetButton.setBackground(ThemeManager.getInstance().getSecondaryColor());
        resetButton.setForeground(ThemeManager.getInstance().getTextColor());
        resetButton.setFocusable(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> resetSettings());
        panel.add(resetButton);

        return panel;
    }

    private void loadSettings() {
        Config config = Config.getInstance();
        storeNameField.setText(config.getStoreName());
        storeAddressField.setText(config.getStoreAddress());
        receiptFolderField.setText(config.getReceiptFolder());
        darkModeCheckBox.setSelected(ThemeManager.getInstance().isDarkMode());
    }

    private void saveSettings() {
        Config config = Config.getInstance();
        config.setStoreName(storeNameField.getText().trim());
        config.setStoreAddress(storeAddressField.getText().trim());
        config.setReceiptFolder(receiptFolderField.getText().trim());
        config.setTheme(darkModeCheckBox.isSelected() ? "dark" : "light");
        config.save();

        JOptionPane.showMessageDialog(this,
                "Settings saved successfully.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettings() {
        storeNameField.setText("POS Store");
        storeAddressField.setText("123 Main Street");
        receiptFolderField.setText("receipts");
        darkModeCheckBox.setSelected(false);
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        darkModeCheckBox.setBackground(ThemeManager.getInstance().getBackgroundColor());
        darkModeCheckBox.setForeground(ThemeManager.getInstance().getTextColor());
        revalidate();
        repaint();
    }
}