package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.util.IconManager;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A number pad component with numeric buttons and action buttons.
 */
public class NumberPad extends JPanel {
    private final List<NumberPadListener> listeners = new ArrayList<>();
    private JTextField displayField;
    private JLabel modeLabel;

    public static final String DELETE = "DELETE";
    public static final String ENTER = "ENTER";
    public static final String SETTINGS = "SETTINGS";
    public static final String SEARCH = "SEARCH";
    public static final String PRINT = "PRINT";
    public static final String CLEAR = "CLEAR";

    public NumberPad() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Top panel with display
        JPanel topPanel = createDisplayPanel();
        add(topPanel, BorderLayout.NORTH);

        // Number pad grid
        JPanel padPanel = createPadPanel();
        add(padPanel, BorderLayout.CENTER);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Mode label
        modeLabel = new JLabel(" ", SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modeLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(modeLabel, BorderLayout.NORTH);

        // Display field
        displayField = new JTextField();
        displayField.setEditable(false);
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 28));
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setBackground(ThemeManager.getInstance().getBackgroundColor());
        displayField.setForeground(ThemeManager.getInstance().getTextColor());
        displayField.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(displayField, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPadPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 3, 10, 10));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Button layout: 1-9, delete/0/enter, settings/search/print
        String[] layout = {
                "7", "8", "9",
                "4", "5", "6",
                "1", "2", "3",
                DELETE, "0", ENTER,
                SETTINGS, SEARCH, PRINT
        };

        for (String key : layout) {
            JButton button = createButton(key);
            panel.add(button);
        }

        return panel;
    }

    private JButton createButton(String key) {
        JButton button = new JButton();
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(80, 80));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (Utility.isInteger(key)) {
            // Numeric button
            button.setText(key);
            button.setBackground(ThemeManager.getInstance().getSecondaryColor());
            button.setForeground(ThemeManager.getInstance().getTextColor());
        } else {
            // Action button with icon
            button.setBackground(ThemeManager.getInstance().getAccentColor());
            button.setForeground(Color.WHITE);

            switch (key) {
                case DELETE -> {
                    button.setIcon(IconManager.getInstance().getIcon(IconManager.DELETE, 32, 32));
                    button.setToolTipText("Delete");
                }
                case ENTER -> {
                    button.setIcon(IconManager.getInstance().getIcon(IconManager.CHECK, 32, 32));
                    button.setToolTipText("Enter/Checkout");
                }
                case SETTINGS -> {
                    button.setIcon(IconManager.getInstance().getIcon(IconManager.SETTINGS, 32, 32));
                    button.setToolTipText("Settings");
                }
                case SEARCH -> {
                    button.setIcon(IconManager.getInstance().getIcon(IconManager.SEARCH, 32, 32));
                    button.setToolTipText("Search");
                }
                case PRINT -> {
                    button.setIcon(IconManager.getInstance().getIcon(IconManager.PRINT, 32, 32));
                    button.setToolTipText("Print Receipt");
                }
            }
        }

        button.setBorder(BorderFactory.createLineBorder(
                ThemeManager.getInstance().getAccentColor(), 1, true));

        button.addActionListener(e -> handleButtonClick(key));

        return button;
    }

    private void handleButtonClick(String key) {
        switch (key) {
            case DELETE -> {
                ApplicationState.getInstance().deleteLastInputChar();
                updateDisplay();
                notifyListeners(DELETE);
            }
            case ENTER -> {
                notifyListeners(ENTER);
                ApplicationState.getInstance().clearInput();
                updateDisplay();
            }
            case SETTINGS -> notifyListeners(SETTINGS);
            case SEARCH -> notifyListeners(SEARCH);
            case PRINT -> notifyListeners(PRINT);
            default -> {
                // Numeric input
                ApplicationState.getInstance().appendInput(key);
                updateDisplay();
                notifyListeners(key);
            }
        }
    }

    private void updateDisplay() {
        String input = ApplicationState.getInstance().getCurrentInput();
        displayField.setText(input.isEmpty() ? "" : input);
    }

    /**
     * Sets the mode label text.
     *
     * @param mode The mode description
     */
    public void setModeText(String mode) {
        modeLabel.setText(mode);
    }

    /**
     * Clears the display.
     */
    public void clearDisplay() {
        displayField.setText("");
        modeLabel.setText(" ");
    }

    /**
     * Adds a listener for number pad events.
     *
     * @param listener The listener to add
     */
    public void addNumberPadListener(NumberPadListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener The listener to remove
     */
    public void removeNumberPadListener(NumberPadListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String key) {
        for (NumberPadListener listener : listeners) {
            listener.onNumberPadAction(key);
        }
    }

    /**
     * Updates the component's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        displayField.setBackground(ThemeManager.getInstance().getBackgroundColor());
        displayField.setForeground(ThemeManager.getInstance().getTextColor());
        modeLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        // Reapply button colors
        for (Component comp : getComponents()) {
            SwingUtilities.updateComponentTreeUI(comp);
        }
    }

    /**
     * Listener interface for number pad events.
     */
    public interface NumberPadListener {
        void onNumberPadAction(String key);
    }
}