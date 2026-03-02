package pos.util;

import pos.app.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIFactory {

    private UIFactory() {}

    /** Modern flat button with rounded background. */
    public static JButton createButton(String text, Color bg, Color fg, int radius) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        final Color hoverBg = bg.darker();
        button.getModel().addChangeListener(e -> {
            button.setBackground(button.getModel().isRollover() ? hoverBg : bg);
            button.repaint();
        });
        return button;
    }

    /** Pill badge label with rounded background drawn at paint time. */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel label = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setBackground(bg);
        label.setForeground(fg);
        label.setFont(new Font("Segoe UI", Font.BOLD, 9));
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(2, 6, 2, 6));
        return label;
    }

    /** Card panel with rounded corners and drop shadow, paints bg at paint time. */
    public static JPanel createCard(int arcRadius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 1, arcRadius * 2, arcRadius * 2);
                // Background
                g2.setColor(ThemeManager.getInstance().getPanelBackgroundColor());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, arcRadius * 2, arcRadius * 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    /** Section header label. */
    public static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        return label;
    }
}
