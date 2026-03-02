package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.PendingCartItem;
import pos.util.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NumberPad extends JPanel implements ApplicationState.StateChangeListener {
    private final List<NumberPadListener> listeners = new ArrayList<>();
    private JTextField displayField;
    private JLabel modeLabel;

    public static final String DELETE  = "DELETE";
    public static final String CONFIRM = "CONFIRM";
    public static final String CLEAR   = "CLEAR";
    public static final String DECIMAL = ".";

    public NumberPad() {
        initialize();
        ApplicationState.getInstance().addStateChangeListener(this);
    }

    private void initialize() {
        setLayout(new BorderLayout(5, 6));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createDisplayPanel(), BorderLayout.NORTH);
        add(createPadPanel(),     BorderLayout.CENTER);
        add(createActionPanel(),  BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setBackground(ThemeManager.getInstance().getSurfaceColor());
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        modeLabel = new JLabel("Enter Weight", SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modeLabel.setForeground(ThemeManager.getInstance().getAccentColor());
        panel.add(modeLabel, BorderLayout.NORTH);

        displayField = new JTextField();
        displayField.setEditable(false);
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 30));
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setBackground(ThemeManager.getInstance().getSurfaceColor());
        displayField.setForeground(ThemeManager.getInstance().getTextColor());
        displayField.setBorder(new EmptyBorder(2, 6, 2, 6));
        displayField.setFocusable(false);
        panel.add(displayField, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPadPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 3, 6, 6));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        String[] layout = {"1","2","3","4","5","6","7","8","9",DECIMAL,"0",CLEAR};
        for (String key : layout) panel.add(createNumericButton(key));
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JButton deleteBtn = createActionButton("Del", IconManager.DELETE,
                ThemeManager.getInstance().getSurfaceColor(),
                ThemeManager.getInstance().getTextSecondaryColor(), 14, false);
        deleteBtn.addActionListener(e -> handleDelete());
        panel.add(deleteBtn);

        JButton confirmBtn = createActionButton("Add", IconManager.CHECK,
                ThemeManager.getInstance().getOrangeColor(), Color.WHITE, 14, true);
        confirmBtn.addActionListener(e -> handleConfirm());
        panel.add(confirmBtn);

        return panel;
    }

    private JButton createNumericButton(String key) {
        boolean isClear   = key.equals(CLEAR);
        boolean isDecimal = key.equals(DECIMAL);

        String  label   = isClear ? "C" : key;
        Color   bg      = isClear ? new Color(0xDC2626) : ThemeManager.getInstance().getPanelBackgroundColor();
        Color   fg      = isClear ? Color.WHITE : ThemeManager.getInstance().getTextColor();
        int     fontSize = (isDecimal || isClear) ? 16 : 22;

        JButton button = new JButton(label);
        button.setFocusable(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));

        Color hoverBg = isClear ? new Color(0xB91C1C) : ThemeManager.getInstance().getBorderColor();
        button.getModel().addChangeListener(e -> {
            boolean roll = button.getModel().isRollover();
            button.setBackground(roll ? hoverBg : bg);
        });

        button.addActionListener(e -> handleButtonClick(key));
        return button;
    }

    private JButton createActionButton(String text, String iconName,
                                       Color bg, Color fg, int fontSize, boolean isBold) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, fontSize));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setIcon(IconManager.getInstance().getIcon(iconName, 16, 16));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(6);
        button.setBorder(new EmptyBorder(12, 14, 12, 14));
        button.setMinimumSize(new Dimension(0, 52));
        button.setPreferredSize(new Dimension(0, 52));

        Color hoverBg = bg.darker();
        button.getModel().addChangeListener(e -> {
            button.setBackground(button.getModel().isRollover() ? hoverBg : bg);
        });
        return button;
    }

    private void handleButtonClick(String key) {
        String current = ApplicationState.getInstance().getCurrentInput();
        switch (key) {
            case CLEAR -> { ApplicationState.getInstance().clearInput(); updateDisplay(); }
            case DECIMAL -> {
                if (!current.contains(".")) {
                    ApplicationState.getInstance().appendInput(".");
                    updateDisplay();
                }
            }
            default -> {
                if (current.length() < 8) {
                    ApplicationState.getInstance().appendInput(key);
                    updateDisplay();
                    applyInputToPendingItem();
                }
            }
        }
    }

    private void handleDelete() {
        if (!ApplicationState.getInstance().getCurrentInput().isEmpty()) {
            ApplicationState.getInstance().deleteLastInputChar();
            updateDisplay();
            applyInputToPendingItem();
        } else {
            ApplicationState.getInstance().clearPendingItem();
            notifyListeners(DELETE);
        }
    }

    private void handleConfirm() {
        notifyListeners(CONFIRM);
        ApplicationState.getInstance().clearInput();
        updateDisplay();
    }

    private void applyInputToPendingItem() {
        ApplicationState state = ApplicationState.getInstance();
        double val = state.getCurrentInputAsDouble();
        if (state.hasPendingItem()) {
            if (state.getInputMode() == ApplicationState.InputMode.QUANTITY)
                state.updatePendingQuantity(val);
            else if (state.getInputMode() == ApplicationState.InputMode.WEIGHT)
                state.updatePendingWeight(val);
        }
    }

    private void updateDisplay() {
        String input = ApplicationState.getInstance().getCurrentInput();
        displayField.setText(input.isEmpty() ? "" : input);
    }

    public void setModeText(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            modeLabel.setText(" ");
            modeLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        } else {
            modeLabel.setText(mode.toUpperCase());
            modeLabel.setForeground(ThemeManager.getInstance().getAccentColor());
        }
    }

    public void clearDisplay() {
        displayField.setText("");
        updateModeFromState();
    }

    public double getCurrentInputValue() {
        return ApplicationState.getInstance().getCurrentInputAsDouble();
    }

    private void updateModeFromState() {
        ApplicationState state = ApplicationState.getInstance();
        if (state.hasPendingItem()) {
            setModeText(state.getInputMode() == ApplicationState.InputMode.QUANTITY
                    ? "Enter Quantity" : "Enter Weight");
        } else {
            setModeText("Select Product");
        }
    }

    public void addNumberPadListener(NumberPadListener l)    { listeners.add(l); }
    public void removeNumberPadListener(NumberPadListener l) { listeners.remove(l); }
    private void notifyListeners(String key) { listeners.forEach(l -> l.onNumberPadAction(key)); }

    @Override public void onPendingItemChanged(PendingCartItem item) {
        SwingUtilities.invokeLater(() -> {
            if (item != null) updateModeFromState();
            else setModeText("Select Product");
        });
    }

    public void updateTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        setBackground(tm.getBackgroundColor());
        displayField.setBackground(tm.getSurfaceColor());
        displayField.setForeground(tm.getTextColor());
        modeLabel.setForeground(tm.getAccentColor());
        SwingUtilities.updateComponentTreeUI(this);
    }

    public interface NumberPadListener {
        void onNumberPadAction(String key);
    }
}
