package pos.ui.panels;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.Department;
import pos.util.Config;
import pos.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final JTextField storeNameField    = new JTextField(30);
    private final JTextField storeAddressField = new JTextField(30);
    private final JTextField receiptFolderField= new JTextField(30);
    private final JComboBox<Department> departmentComboBox = new JComboBox<>(Department.values());

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(ThemeManager.getInstance().getBackgroundColor());
        formWrapper.setBorder(new EmptyBorder(10, 0, 10, 0));

        formWrapper.add(createStoreInfoCard());
        formWrapper.add(Box.createVerticalStrut(12));
        formWrapper.add(createAppearanceCard());
        formWrapper.add(Box.createVerticalGlue());

        add(formWrapper, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadSettings();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createStoreInfoCard() {
        JPanel card = UIFactory.createCard(12);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel title = UIFactory.createSectionHeader("Store Information");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        card.add(createFieldRow("Store Name", storeNameField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Store Address", storeAddressField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Receipt Folder", receiptFolderField));

        return card;
    }

    private JPanel createAppearanceCard() {
        JPanel card = UIFactory.createCard(12);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel title = UIFactory.createSectionHeader("Appearance");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        card.add(createFieldRow("Department", buildDeptCombo()));
        return card;
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(ThemeManager.getInstance().getTextColor());
        label.setPreferredSize(new Dimension(120, 36));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JComboBox<Department> buildDeptCombo() {
        departmentComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        departmentComboBox.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        departmentComboBox.setForeground(ThemeManager.getInstance().getTextColor());
        departmentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                if (value instanceof Department d) setText(d.getDisplayName());
                return this;
            }
        });
        return departmentComboBox;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JButton resetButton = UIFactory.createButton("Reset to Defaults",
                ThemeManager.getInstance().getSurfaceColor(),
                ThemeManager.getInstance().getTextColor(), 8);
        resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resetButton.addActionListener(e -> resetSettings());
        panel.add(resetButton);

        JButton saveButton = UIFactory.createButton("Save Settings",
                ThemeManager.getInstance().getAccentColor(), Color.WHITE, 8);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.addActionListener(e -> saveSettings());
        panel.add(saveButton);

        return panel;
    }

    private void loadSettings() {
        Config cfg = Config.getInstance();
        storeNameField.setText(cfg.getStoreName());
        storeAddressField.setText(cfg.getStoreAddress());
        receiptFolderField.setText(cfg.getReceiptFolder());
        departmentComboBox.setSelectedItem(cfg.getDepartment());

        // Style fields
        styleField(storeNameField);
        styleField(storeAddressField);
        styleField(receiptFolderField);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(ThemeManager.getInstance().getBackgroundColor());
        field.setForeground(ThemeManager.getInstance().getTextColor());
        field.setPreferredSize(new Dimension(0, 36));
    }

    private void saveSettings() {
        Config cfg = Config.getInstance();
        cfg.setStoreName(storeNameField.getText().trim());
        cfg.setStoreAddress(storeAddressField.getText().trim());
        cfg.setReceiptFolder(receiptFolderField.getText().trim());

        Department dept = (Department) departmentComboBox.getSelectedItem();
        if (dept != null) {
            cfg.setDepartment(dept);
            ApplicationState.getInstance().setCurrentDepartment(dept);
        }
        cfg.save();

        JOptionPane.showMessageDialog(this, "Settings saved successfully.",
                "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettings() {
        storeNameField.setText("Store Name");
        storeAddressField.setText("123 Main Street");
        receiptFolderField.setText("receipts");
        departmentComboBox.setSelectedItem(Department.DELI);
    }

    public void updateTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        setBackground(tm.getBackgroundColor());
        departmentComboBox.setBackground(tm.getPanelBackgroundColor());
        revalidate();
        repaint();
    }
}
