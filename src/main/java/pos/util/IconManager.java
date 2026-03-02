package pos.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages SVG icons for the application.
 * Uses FlatLaf's FlatSVGIcon for rendering.
 */
public class IconManager {
    private static final IconManager INSTANCE = new IconManager();
    private static final String ICONS_PATH = "/icons/";

    private final Map<String, Icon> iconCache = new HashMap<>();

    // Icon names
    public static final String DELETE = "delete";
    public static final String CHECK = "check";
    public static final String SETTINGS = "settings";
    public static final String SEARCH = "search";
    public static final String PRINT = "print";
    public static final String POS = "pos";
    public static final String CART = "cart";
    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String CLEAR = "clear";
    public static final String HISTORY = "history";
    public static final String HOME = "home";
    public static final String DARK_MODE = "dark_mode";
    public static final String LIGHT_MODE = "light_mode";

    private IconManager() {
        // Private constructor for singleton
    }

    public static IconManager getInstance() {
        return INSTANCE;
    }

    /**
     * Gets an icon by name. Uses caching for performance.
     *
     * @param name The icon name (without .svg extension)
     * @return The icon, or null if not found
     */
    public Icon getIcon(String name) {
        return iconCache.computeIfAbsent(name, this::loadIcon);
    }

    /**
     * Gets an icon scaled to a specific size.
     *
     * @param name The icon name
     * @param width Desired width
     * @param height Desired height
     * @return The scaled icon
     */
    public Icon getIcon(String name, int width, int height) {
        String cacheKey = name + "_" + width + "x" + height;
        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }

        Icon icon = loadIcon(name, width, height);
        if (icon != null) {
            iconCache.put(cacheKey, icon);
        }
        return icon;
    }

    private Icon loadIcon(String name) {
        return loadIcon(name, 24, 24);
    }

    private Icon loadIcon(String name, int width, int height) {
        try {
            String path = ICONS_PATH + name + ".svg";
            java.net.URL resource = IconManager.class.getResource(path);

            if (resource != null) {
                FlatSVGIcon icon = new FlatSVGIcon(resource);
                return icon.derive(width, height);
            }

            Logger.warning("Icon not found: " + name);
            return createPlaceholderIcon(width, height);
        } catch (Exception e) {
            Logger.error("Failed to load icon: " + name, e);
            return createPlaceholderIcon(width, height);
        }
    }

    private Icon createPlaceholderIcon(int width, int height) {
        // Create a simple placeholder icon if SVG loading fails
        return UIManager.getIcon("Tree.leafIcon");
    }

    /**
     * Gets an icon as an Image.
     *
     * @param name The icon name
     * @param width Desired width
     * @param height Desired height
     * @return The image, or null if not found
     */
    public java.awt.Image getImage(String name, int width, int height) {
        try {
            String path = ICONS_PATH + name + ".svg";
            java.net.URL resource = IconManager.class.getResource(path);

            if (resource != null) {
                FlatSVGIcon icon = new FlatSVGIcon(resource);
                FlatSVGIcon scaledIcon = icon.derive(width, height);
                return scaledIcon.getImage();
            }

            Logger.warning("Icon not found for image: " + name);
            return null;
        } catch (Exception e) {
            Logger.error("Failed to load icon as image: " + name, e);
            return null;
        }
    }

    /**
     * Clears the icon cache.
     */
    public void clearCache() {
        iconCache.clear();
    }
}