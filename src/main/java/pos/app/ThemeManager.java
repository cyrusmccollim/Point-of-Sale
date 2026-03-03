package pos.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import pos.util.Config;
import pos.util.Logger;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();

    // Light theme
    public static final Color LIGHT_BG             = new Color(0xF0F0F5);
    public static final Color LIGHT_PANEL_BG       = new Color(0xFFFFFF);
    public static final Color LIGHT_SURFACE        = new Color(0xFAFAFA);
    public static final Color LIGHT_ACCENT         = new Color(0x5B5BD6);
    public static final Color LIGHT_ACCENT_HOVER   = new Color(0x4747C2);
    public static final Color LIGHT_ORANGE         = new Color(0xF07820);
    public static final Color LIGHT_TEXT           = new Color(0x18181B);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(0x71717A);
    public static final Color LIGHT_BORDER         = new Color(0xE4E4E7);
    public static final Color LIGHT_SEPARATOR      = new Color(0xB8B8C4);

    // Dark theme
    public static final Color DARK_BG              = new Color(0x18181B);
    public static final Color DARK_PANEL_BG        = new Color(0x27272A);
    public static final Color DARK_SURFACE         = new Color(0x1E1E21);
    public static final Color DARK_ACCENT          = new Color(0x7C7CE8);
    public static final Color DARK_ACCENT_HOVER    = new Color(0x9595F0);
    public static final Color DARK_ORANGE          = new Color(0xFF9940);
    public static final Color DARK_TEXT            = new Color(0xFAFAFA);
    public static final Color DARK_TEXT_SECONDARY  = new Color(0xA1A1AA);
    public static final Color DARK_BORDER          = new Color(0x3F3F46);

    private boolean darkMode = false;

    private ThemeManager() {
        this.darkMode = "dark".equalsIgnoreCase(Config.getInstance().getTheme());
    }

    public static ThemeManager getInstance() { return INSTANCE; }

    public void applyTheme() {
        try {
            FlatLaf laf = darkMode ? new FlatDarkLaf() : new FlatLightLaf();
            UIManager.setLookAndFeel(laf);
            applyCustomColors();
            for (Window w : Window.getWindows()) SwingUtilities.updateComponentTreeUI(w);
            Logger.info("Applied " + (darkMode ? "dark" : "light") + " theme");
        } catch (Exception e) {
            Logger.error("Failed to apply theme", e);
        }
    }

    private void applyCustomColors() {
        Color accent   = darkMode ? DARK_ACCENT : LIGHT_ACCENT;
        Color text     = darkMode ? DARK_TEXT   : LIGHT_TEXT;

        if (darkMode) {
            UIManager.put("Panel.background",      DARK_PANEL_BG);
            UIManager.put("Button.background",     DARK_SURFACE);
            UIManager.put("Button.foreground",     DARK_TEXT);
            UIManager.put("Label.foreground",      DARK_TEXT);
            UIManager.put("TextField.background",  DARK_BG);
            UIManager.put("TextField.foreground",  DARK_TEXT);
            UIManager.put("List.background",       DARK_BG);
            UIManager.put("List.foreground",       DARK_TEXT);
            UIManager.put("ScrollPane.background", DARK_BG);
        } else {
            UIManager.put("Panel.background",      LIGHT_PANEL_BG);
            UIManager.put("Button.background",     LIGHT_SURFACE);
            UIManager.put("Button.foreground",     LIGHT_TEXT);
            UIManager.put("Label.foreground",      LIGHT_TEXT);
            UIManager.put("TextField.background",  LIGHT_PANEL_BG);
            UIManager.put("TextField.foreground",  LIGHT_TEXT);
            UIManager.put("List.background",       LIGHT_PANEL_BG);
            UIManager.put("List.foreground",       LIGHT_TEXT);
            UIManager.put("ScrollPane.background", LIGHT_PANEL_BG);
        }

        UIManager.put("Component.arc",        10);
        UIManager.put("Button.arc",           10);
        UIManager.put("TextComponent.arc",    8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("ScrollBar.thumbArc",   999);
        UIManager.put("ScrollBar.width",      8);
        UIManager.put("Table.selectionBackground",
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
        UIManager.put("Table.selectionForeground", text);
    }

    public boolean isDarkMode() { return darkMode; }

    public Color getAccentColor()         { return darkMode ? DARK_ACCENT        : LIGHT_ACCENT; }
    public Color getAccentHoverColor()    { return darkMode ? DARK_ACCENT_HOVER   : LIGHT_ACCENT_HOVER; }
    public Color getSurfaceColor()        { return darkMode ? DARK_SURFACE        : LIGHT_SURFACE; }
    public Color getBorderColor()         { return darkMode ? DARK_BORDER         : LIGHT_BORDER; }
    public Color getSeparatorColor()      { return darkMode ? DARK_BORDER         : LIGHT_SEPARATOR; }
    public Color getSecondaryColor()      { return getBorderColor(); }   // backward compat
    public Color getBackgroundColor()     { return darkMode ? DARK_BG             : LIGHT_BG; }
    public Color getPanelBackgroundColor(){ return darkMode ? DARK_PANEL_BG       : LIGHT_PANEL_BG; }
    public Color getTextColor()           { return darkMode ? DARK_TEXT           : LIGHT_TEXT; }
    public Color getTextSecondaryColor()  { return darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY; }
    public Color getOrangeColor()         { return darkMode ? DARK_ORANGE         : LIGHT_ORANGE; }
}
