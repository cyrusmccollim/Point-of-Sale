package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.PendingCartItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class NumberPad extends JPanel implements ApplicationState.StateChangeListener {
    private JLabel displayField;
    private JLabel modeLabel;
    private JButton doneButton;

    public static final String CLEAR   = "CLEAR";
    public static final String DECIMAL = ".";

    private static final Color DONE_ACTIVE   = new Color(0xF07820);
    private static final Color DONE_DISABLED = new Color(0xD4D4D4);

    public NumberPad() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        add(createDisplayPanel(), BorderLayout.NORTH);
        add(createPadPanel(),     BorderLayout.CENTER);
        add(createDonePanel(),    BorderLayout.SOUTH);
        ApplicationState.getInstance().addStateChangeListener(this);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        modeLabel = new JLabel("SELECT PRODUCT", SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modeLabel.setForeground(ThemeManager.getInstance().getAccentColor());
        panel.add(modeLabel, BorderLayout.NORTH);

        displayField = new JLabel(" ", SwingConstants.RIGHT);
        displayField.setOpaque(true);
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 30));
        displayField.setForeground(ThemeManager.getInstance().getTextColor());
        displayField.setBorder(new CompoundBorder(new LineBorder(ThemeManager.getInstance().getSeparatorColor(), 2), new EmptyBorder(2, 6, 2, 8)));
        panel.add(displayField, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPadPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 3, 6, 6));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        for (String key : new String[]{"1","2","3","4","5","6","7","8","9",DECIMAL,"0",CLEAR}) panel.add(createNumericButton(key));
        return panel;
    }

    private JPanel createDonePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 1, 0, 0));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        panel.setBorder(new EmptyBorder(4, 0, 0, 0));

        doneButton = new JButton("Enter");
        doneButton.setFocusable(false);
        doneButton.setFont(new Font("Segoe UI", Font.BOLD, 25));
        doneButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        doneButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        doneButton.setIconTextGap(6);
        doneButton.setBorder(new EmptyBorder(12, 14, 12, 14));
        doneButton.setMinimumSize(new Dimension(0, 52));
        doneButton.setPreferredSize(new Dimension(0, 52));
        doneButton.setEnabled(false);
        doneButton.setBackground(DONE_DISABLED);
        doneButton.setForeground(Color.WHITE);
        doneButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        doneButton.addActionListener(e -> handleDone());
        panel.add(doneButton);
        return panel;
    }

    private JButton createNumericButton(String key) {
        boolean isClear = key.equals(CLEAR);
        Color bg = ThemeManager.getInstance().getPanelBackgroundColor();
        Color fg = ThemeManager.getInstance().getTextColor();

        JButton button = new JButton(isClear ? "C" : key);
        button.setFocusable(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, key.equals(DECIMAL) ? 40 : 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));

        Color hoverBg = isClear ? new Color(0xB91C1C) : ThemeManager.getInstance().getBorderColor();
        button.getModel().addChangeListener(e -> button.setBackground(button.getModel().isRollover() ? hoverBg : bg));
        button.addActionListener(e -> handleButtonClick(key));
        return button;
    }

    private void handleButtonClick(String key) {
        String current = ApplicationState.getInstance().getCurrentInput();
        switch (key) {
            case CLEAR  -> { ApplicationState.getInstance().clearInput(); updateDisplay(); }
            case DECIMAL -> { if (!current.contains(".")) { ApplicationState.getInstance().appendInput("."); updateDisplay(); } }
            default     -> { if (current.length() < 8) { ApplicationState.getInstance().appendInput(key); updateDisplay(); applyInputToPendingItem(); } }
        }
    }

    private void handleDone() {
        applyInputToPendingItem();
        ApplicationState state = ApplicationState.getInstance();
        state.setInputMode(ApplicationState.InputMode.NONE);
        state.clearInput();
        updateDisplay();
    }

    private void applyInputToPendingItem() {
        ApplicationState state = ApplicationState.getInstance();
        double val = state.getCurrentInputAsDouble();
        if (state.hasPendingItem()) {
            if (state.getInputMode() == ApplicationState.InputMode.QUANTITY) state.updatePendingQuantity(val);
            else if (state.getInputMode() == ApplicationState.InputMode.WEIGHT) state.updatePendingWeight(val);
        }
    }

    private void updateDisplay() {
        String input = ApplicationState.getInstance().getCurrentInput();
        displayField.setText(input.isEmpty() ? " " : input);
        updateDoneState(input);
    }

    private void updateDoneState(String input) {
        boolean hasInput = !input.isEmpty();
        doneButton.setEnabled(hasInput);
        doneButton.setBackground(hasInput ? DONE_ACTIVE : DONE_DISABLED);
        doneButton.setCursor(new Cursor(hasInput ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
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
        displayField.setText(" ");
        updateDoneState("");
        updateModeFromState();
    }

    private void updateModeFromState() {
        ApplicationState state = ApplicationState.getInstance();
        if (state.hasPendingItem()) {
            ApplicationState.InputMode mode = state.getInputMode();
            if (mode == ApplicationState.InputMode.QUANTITY)       setModeText("Enter Quantity");
            else if (mode == ApplicationState.InputMode.WEIGHT)    setModeText("Enter Weight");
            else                                                    setModeText("Select Field");
        } else {
            setModeText("No Product Selected");
        }
    }

    @Override public void onPendingItemChanged(PendingCartItem item) {
        SwingUtilities.invokeLater(() -> {
            if (item != null) updateModeFromState();
            else { setModeText("No Product Selected"); displayField.setText(" "); updateDoneState(""); }
        });
    }

    @Override public void onInputModeChanged(ApplicationState.InputMode mode) {
        SwingUtilities.invokeLater(() -> {
            if (mode == ApplicationState.InputMode.QUANTITY)    setModeText("Enter Quantity");
            else if (mode == ApplicationState.InputMode.WEIGHT) setModeText("Enter Weight");
            else if (mode == ApplicationState.InputMode.NONE)   { setModeText("Select Field"); displayField.setText(" "); updateDoneState(""); }
        });
    }

    public void updateTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        setBackground(tm.getBackgroundColor());
        displayField.setForeground(tm.getTextColor());
        modeLabel.setForeground(tm.getAccentColor());
        SwingUtilities.updateComponentTreeUI(this);
    }
}
