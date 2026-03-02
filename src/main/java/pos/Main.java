package pos;

import pos.app.POSApplication;
import pos.app.ThemeManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            ThemeManager.getInstance().applyTheme();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Run application on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                POSApplication app = new POSApplication();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}