package pos.ui.panels;

import pos.app.ThemeManager;
import pos.db.TransactionDAO;
import pos.model.Transaction;
import pos.util.Config;
import pos.util.UIFactory;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {
    private final String[] columns = {"ID", "Date/Time", "Items", "Total"};
    private final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable transactionTable = new JTable(tableModel);
    private JLabel transactionCountLabel;
    private JLabel totalSumLabel;

    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(),  BorderLayout.CENTER);
        add(createSummaryBar(),  BorderLayout.SOUTH);

        configureTable();
        loadTransactions();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = UIFactory.createButton("Refresh",
                ThemeManager.getInstance().getAccentColor(), Color.WHITE, 8);
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.addActionListener(e -> loadTransactions());
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        // View receipt button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JButton viewButton = UIFactory.createButton("View Receipt",
                ThemeManager.getInstance().getAccentColor(), Color.WHITE, 8);
        viewButton.addActionListener(e -> viewSelectedReceipt());
        buttonPanel.add(viewButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(8, 14, 8, 14));

        transactionCountLabel = new JLabel("0 transactions");
        transactionCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionCountLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(transactionCountLabel, BorderLayout.WEST);

        totalSumLabel = new JLabel("$0.00 total");
        totalSumLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalSumLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        panel.add(totalSumLabel, BorderLayout.EAST);

        return panel;
    }

    private void configureTable() {
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        transactionTable.setRowHeight(36);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 0));

        // Header styling
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        transactionTable.getTableHeader().setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        transactionTable.getTableHeader().setForeground(ThemeManager.getInstance().getTextColor());
        transactionTable.getTableHeader().setBorder(
                new javax.swing.border.MatteBorder(0, 0, 1, 0, ThemeManager.getInstance().getBorderColor()));

        // Column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Custom cell renderer
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                ThemeManager tm = ThemeManager.getInstance();

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? tm.getPanelBackgroundColor() : tm.getSurfaceColor());
                    setForeground(tm.getTextColor());
                }

                // ID column: secondary color, centered
                if (col == 0) {
                    setHorizontalAlignment(CENTER);
                    if (!isSelected) setForeground(tm.getTextSecondaryColor());
                } else if (col == 3) {
                    // Total column: orange, right-aligned
                    setHorizontalAlignment(RIGHT);
                    if (!isSelected) setForeground(tm.getOrangeColor());
                } else {
                    setHorizontalAlignment(LEFT);
                }

                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = TransactionDAO.getInstance().loadAllTransactions();

        double totalSum = 0;
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getFormattedTimestamp(),
                    t.getTotalQuantity(),
                    Utility.formatPrice(t.getTotal())
            });
            totalSum += t.getTotal();
        }

        if (transactionCountLabel != null) {
            int n = transactions.size();
            transactionCountLabel.setText(n + " transaction" + (n != 1 ? "s" : ""));
            totalSumLabel.setText(Utility.formatPrice(totalSum) + " total");
        }
    }

    private void viewSelectedReceipt() {
        int row = transactionTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to view.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Transaction t = TransactionDAO.getInstance().loadTransaction(id);
        if (t != null) {
            String receipt = t.generateReceipt(Config.getInstance().getStoreName(),
                    Config.getInstance().getStoreAddress());
            JTextArea textArea = new JTextArea(receipt);
            textArea.setEditable(false);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            textArea.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 500));
            JOptionPane.showMessageDialog(this, scrollPane, "Receipt #" + id, JOptionPane.PLAIN_MESSAGE);
        }
    }

    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        SwingUtilities.updateComponentTreeUI(transactionTable);
        revalidate();
        repaint();
    }
}
