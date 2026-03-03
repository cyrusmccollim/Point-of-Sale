package pos.app;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import pos.util.Logger;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();

    public static final Color LIGHT_BG             = new Color(0xF0F0F5);
    public static final Color LIGHT_PANEL_BG       = new Color(0xFFFFFF);
    public static final Color LIGHT_SURFACE        = new Color(0xFAFAFA);
    public static final Color LIGHT_ACCENT         = new Color(0x5B5BD6);
    public static final Color LIGHT_ACCENT_HOVER   = new Color(0x4747C2);
    public static final Color LIGHT_ORANGE         = new Color(0xF07820);
    public static final Color LIGHT_TEXT           = new Color(0x18181B);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(0x71717A);
    public static final Color LIGHT_BORDER         = new Color(0xE4E4E7);
    public static final Color LIGHT_SEPARATOR      = new Color(0xd9d9df);

    public static ThemeManager getInstance() { return INSTANCE; }

    public void applyTheme() {
        try {
            FlatLaf laf = new FlatLightLaf();
            UIManager.setLookAndFeel(laf);
            applyCustomColors();
            for (Window w : Window.getWindows()) SwingUtilities.updateComponentTreeUI(w);
        } catch (Exception e) {
            Logger.error("Failed to apply theme", e);
        }
    }

    private void applyCustomColors() {
        Color accent = LIGHT_ACCENT;
        Color text = LIGHT_TEXT;

        UIManager.put("Panel.background",      LIGHT_PANEL_BG);
        UIManager.put("Button.background",     LIGHT_SURFACE);
        UIManager.put("Button.foreground",     LIGHT_TEXT);
        UIManager.put("Label.foreground",      LIGHT_TEXT);
        UIManager.put("TextField.background",  LIGHT_PANEL_BG);
        UIManager.put("TextField.foreground",  LIGHT_TEXT);
        UIManager.put("List.background",       LIGHT_PANEL_BG);
        UIManager.put("List.foreground",       LIGHT_TEXT);
        UIManager.put("ScrollPane.background", LIGHT_PANEL_BG);

        UIManager.put("Component.arc",        10);
        UIManager.put("Button.arc",           10);
        UIManager.put("TextComponent.arc",    8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("ScrollBar.thumbArc",   999);
        UIManager.put("ScrollBar.width",      8);
        UIManager.put("Table.selectionBackground", new Color(0xD4D4D8));
        UIManager.put("Table.selectionForeground", text);
    }

    public Color getAccentColor()         { return LIGHT_ACCENT; }
    public Color getAccentHoverColor()    { return LIGHT_ACCENT_HOVER; }
    public Color getSurfaceColor()        { return LIGHT_SURFACE; }
    public Color getBorderColor()         { return LIGHT_BORDER; }
    public Color getSeparatorColor()      { return LIGHT_SEPARATOR; }
    public Color getSecondaryColor()      { return getBorderColor(); }
    public Color getBackgroundColor()     { return LIGHT_BG; }
    public Color getPanelBackgroundColor(){ return LIGHT_PANEL_BG; }
    public Color getTextColor()           { return LIGHT_TEXT; }
    public Color getTextSecondaryColor()  { return LIGHT_TEXT_SECONDARY; }
    public Color getOrangeColor()         { return LIGHT_ORANGE; }

    public Font getTitleFont()   { return new Font("Segoe UI", Font.PLAIN,  25); }
    public Font getSectionFont() { return new Font("Segoe UI", Font.BOLD,   15); }
    public Font getBodyFont()    { return new Font("Segoe UI", Font.PLAIN,  15); }
    public Font getBoldFont()    { return new Font("Segoe UI", Font.BOLD,   15); }
    public Font getSmallFont()   { return new Font("Segoe UI", Font.PLAIN,  13); }
    public Font getLargeFont()   { return new Font("Segoe UI", Font.BOLD,   22); }
}
