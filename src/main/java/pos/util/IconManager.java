package pos.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class IconManager {
    private static final IconManager INSTANCE = new IconManager();
    private static final String ICONS_PATH = "/icons/";

    private final Map<String, Icon> iconCache = new HashMap<>();

    public static final String SEARCH = "search";
    public static final String POS    = "pos";
    public static final String PRINT  = "print";

    private IconManager() {}

    public static IconManager getInstance() { return INSTANCE; }

    public Icon getIcon(String name, int width, int height) {
        return iconCache.computeIfAbsent(name + "_" + width + "x" + height, k -> loadIcon(name, width, height));
    }

    private Icon loadIcon(String name, int width, int height) {
        try {
            java.net.URL resource = IconManager.class.getResource(ICONS_PATH + name + ".svg");
            if (resource != null) return new FlatSVGIcon(resource).derive(width, height);
            Logger.warning("Icon not found: " + name);
        } catch (Exception e) {
            Logger.error("Failed to load icon: " + name, e);
        }
        return UIManager.getIcon("Tree.leafIcon");
    }

    public java.awt.Image getImage(String name, int width, int height) {
        try {
            java.net.URL resource = IconManager.class.getResource(ICONS_PATH + name + ".svg");
            if (resource != null) return new FlatSVGIcon(resource).derive(width, height).getImage();
            Logger.warning("Icon not found for image: " + name);
        } catch (Exception e) {
            Logger.error("Failed to load icon as image: " + name, e);
        }
        return null;
    }
}
