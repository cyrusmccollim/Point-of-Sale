package pos.ui.components;

import pos.app.ThemeManager;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A panel that displays a single metric (e.g., Total, Weight, etc.).
 */
public class MetricPanel extends JPanel {
    private final JLabel valueLabel;
    private final JLabel nameLabel;
    private final String metricName;

    public MetricPanel(String name) {
        this(name, "--");
    }

    public MetricPanel(String name, String initialValue) {
        this.metricName = name;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Value label (large)
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        valueLabel = new JLabel(initialValue, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        valueLabel.setForeground(ThemeManager.getInstance().getTextColor());
        add(valueLabel, gbc);

        // Name label (smaller)
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        add(nameLabel, gbc);

        applyThemeColors();
    }

    /**
     * Updates the metric value.
     *
     * @param value The new value to display
     */
    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /**
     * Updates the metric value as a formatted price.
     *
     * @param price The price to format and display
     */
    public void setPriceValue(double price) {
        valueLabel.setText(Utility.formatPrice(price));
    }

    /**
     * Gets the current value.
     *
     * @return The displayed value
     */
    public String getValue() {
        return valueLabel.getText();
    }

    /**
     * Gets the metric name.
     *
     * @return The metric name
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * Clears the metric to its default state.
     */
    public void clear() {
        valueLabel.setText("--");
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        applyThemeColors();
        valueLabel.setForeground(ThemeManager.getInstance().getTextColor());
        nameLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        repaint();
    }

    private void applyThemeColors() {
        setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));
    }
}