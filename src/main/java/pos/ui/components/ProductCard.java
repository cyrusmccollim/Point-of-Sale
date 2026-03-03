package pos.ui.components;

import pos.app.ApplicationState;
import pos.app.ThemeManager;
import pos.model.PendingCartItem;
import pos.model.Product;
import pos.util.UIFactory;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProductCard extends JPanel implements ApplicationState.StateChangeListener {
    private static final int ARC = 12;

    private final Product product;
    private boolean selected = false;
    private boolean hovered  = false;

    private final JLabel cpuBadge;
    private final JLabel nameLabel;
    private final JLabel priceLabel;

    public ProductCard(Product product) {
        this.product = product;

        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(155, 105));
        setBorder(new EmptyBorder(16, 8, 8, 16));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        cpuBadge = UIFactory.createBadge(product.getCpu(), ThemeManager.getInstance().getAccentColor(), Color.WHITE);
        JPanel north = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        north.setOpaque(false);
        north.add(cpuBadge);
        add(north, BorderLayout.NORTH);

        nameLabel = new JLabel("<html><div style='text-align:center;'>" + product.getName() + "</div></html>", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(ThemeManager.getInstance().getTextColor());
        add(nameLabel, BorderLayout.CENTER);

        priceLabel = new JLabel(Utility.formatPrice(product.getPrice()) + "/lb", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        add(priceLabel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { ApplicationState.getInstance().setPendingProduct(product); selected = true; repaint(); }
            @Override public void mouseEntered(MouseEvent e) { if (!selected) { hovered = true; repaint(); } }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });

        ApplicationState.getInstance().addStateChangeListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ThemeManager tm = ThemeManager.getInstance();

        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 3, getWidth() - 3, getHeight() - 3, ARC * 2, ARC * 2);

        Color bg;
        if (selected) {
            bg = tm.getAccentColor();
        } else if (hovered) {
            Color s = tm.getSurfaceColor(), a = tm.getAccentColor();
            bg = new Color((int)(s.getRed() * 0.92 + a.getRed() * 0.08), (int)(s.getGreen() * 0.92 + a.getGreen() * 0.08), (int)(s.getBlue() * 0.92 + a.getBlue() * 0.08));
        } else {
            bg = tm.getPanelBackgroundColor();
        }
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, ARC * 2, ARC * 2);

        if (!selected) {
            g2.setColor(new Color(0, 0, 0, 35));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, ARC * 2, ARC * 2);
        }
        if (hovered && !selected) {
            g2.setColor(tm.getAccentColor());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 4, ARC * 2, ARC * 2);
        }

        g2.dispose();
        updateChildColors(selected);
    }

    private void updateChildColors(boolean sel) {
        ThemeManager tm = ThemeManager.getInstance();
        if (sel) {
            cpuBadge.setBackground(new Color(255, 255, 255, 60));
            cpuBadge.setForeground(Color.WHITE);
            nameLabel.setForeground(Color.WHITE);
            priceLabel.setForeground(new Color(255, 255, 200));
        } else {
            cpuBadge.setBackground(tm.getAccentColor());
            cpuBadge.setForeground(Color.WHITE);
            nameLabel.setForeground(tm.getTextColor());
            priceLabel.setForeground(tm.getOrangeColor());
        }
    }

    public void clearSelection() { selected = false; hovered = false; repaint(); }
    public Product getProduct()  { return product; }

    @Override public void onPendingItemChanged(PendingCartItem item) {
        boolean should = item != null && item.getProduct().getCpu().equals(product.getCpu());
        if (should != selected) { selected = should; repaint(); }
    }

    public void updateTheme() { repaint(); }
}
