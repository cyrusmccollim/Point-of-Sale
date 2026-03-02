package pos.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import pos.util.Config;
import pos.util.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Manages application themes (light/dark modes).
 */
public class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();

    public static final Color LIGHT_BG = new Color(250, 250, 250);
    public static final Color LIGHT_PANEL_BG = new Color(255, 255, 255);
    public static final Color LIGHT_ACCENT = new Color(52, 106, 179);
    public static final Color LIGHT_SECONDARY = new Color(116, 170, 242);
    public static final Color LIGHT_ORANGE = new Color(235, 144, 75);
    public static final Color LIGHT_TEXT = new Color(30, 30, 30);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(100, 100, 100);

    public static final Color DARK_BG = new Color(40, 40, 44);
    public static final Color DARK_PANEL_BG = new Color(50, 50, 54);
    public static final Color DARK_ACCENT = new Color(100, 149, 237);
    public static final Color DARK_SECONDARY = new Color(70, 130, 200);
    public static final Color DARK_ORANGE = new Color(255, 160, 90);
    public static final Color DARK_TEXT = new Color(240, 240, 240);
    public static final Color DARK_TEXT_SECONDARY = new Color(180, 180, 180);

    private boolean darkMode = false;

    private ThemeManager() {
        String savedTheme = Config.getInstance().getTheme();
        this.darkMode = "dark".equalsIgnoreCase(savedTheme);
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    /**
     * Applies the current theme to the application.
     */
    public void applyTheme() {
        try {
            FlatLaf laf = darkMode ? new FlatDarkLaf() : new FlatLightLaf();
            UIManager.setLookAndFeel(laf);

            // Apply custom colors
            applyCustomColors();

            // Update all existing windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }

            Logger.info("Applied " + (darkMode ? "dark" : "light") + " theme");
        } catch (Exception e) {
            Logger.error("Failed to apply theme", e);
        }
    }

    private void applyCustomColors() {
        if (darkMode) {
            // Dark theme colors
            UIManager.put("Panel.background", DARK_PANEL_BG);
            UIManager.put("Button.background", DARK_SECONDARY);
            UIManager.put("Button.foreground", DARK_TEXT);
            UIManager.put("Label.foreground", DARK_TEXT);
            UIManager.put("TextField.background", DARK_BG);
            UIManager.put("TextField.foreground", DARK_TEXT);
            UIManager.put("List.background", DARK_BG);
            UIManager.put("List.foreground", DARK_TEXT);
            UIManager.put("ScrollPane.background", DARK_BG);
        } else {
            // Light theme colors
            UIManager.put("Panel.background", LIGHT_PANEL_BG);
            UIManager.put("Button.background", LIGHT_SECONDARY);
            UIManager.put("Button.foreground", LIGHT_TEXT);
            UIManager.put("Label.foreground", LIGHT_TEXT);
            UIManager.put("TextField.background", LIGHT_PANEL_BG);
            UIManager.put("TextField.foreground", LIGHT_TEXT);
            UIManager.put("List.background", LIGHT_PANEL_BG);
            UIManager.put("List.foreground", LIGHT_TEXT);
            UIManager.put("ScrollPane.background", LIGHT_PANEL_BG);
        }
    }

    /**
     * Toggles between light and dark themes.
     */
    public void toggleTheme() {
        darkMode = !darkMode;
        Config.getInstance().setTheme(darkMode ? "dark" : "light");
        Config.getInstance().save();
        applyTheme();
    }

    /**
     * Sets the theme to light or dark.
     *
     * @param dark true for dark theme, false for light theme
     */
    public void setDarkMode(boolean dark) {
        if (this.darkMode != dark) {
            this.darkMode = dark;
            Config.getInstance().setTheme(dark ? "dark" : "light");
            Config.getInstance().save();
            applyTheme();
        }
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Gets the current accent color based on theme.
     *
     * @return The accent color
     */
    public Color getAccentColor() {
        return darkMode ? DARK_ACCENT : LIGHT_ACCENT;
    }

    /**
     * Gets the current secondary color based on theme.
     *
     * @return The secondary color
     */
    public Color getSecondaryColor() {
        return darkMode ? DARK_SECONDARY : LIGHT_SECONDARY;
    }

    /**
     * Gets the current background color.
     *
     * @return The background color
     */
    public Color getBackgroundColor() {
        return darkMode ? DARK_BG : LIGHT_BG;
    }

    /**
     * Gets the current panel background color.
     *
     * @return The panel background color
     */
    public Color getPanelBackgroundColor() {
        return darkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG;
    }

    /**
     * Gets the current text color.
     *
     * @return The text color
     */
    public Color getTextColor() {
        return darkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    /**
     * Gets the current secondary text color.
     *
     * @return The secondary text color
     */
    public Color getTextSecondaryColor() {
        return darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }

    /**
     * Gets the orange accent color (always visible).
     *
     * @return The orange color
     */
    public Color getOrangeColor() {
        return darkMode ? DARK_ORANGE : LIGHT_ORANGE;
    }
}