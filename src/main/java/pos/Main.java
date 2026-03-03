package pos;

import pos.app.POSApplication;
import pos.app.ThemeManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ThemeManager.getInstance().applyTheme();
        SwingUtilities.invokeLater(() -> {
            try {
                new POSApplication().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start application: " + e.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
