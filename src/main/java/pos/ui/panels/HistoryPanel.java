package pos.ui.panels;

import pos.app.ApplicationState;
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

public class HistoryPanel extends JPanel implements ApplicationState.StateChangeListener {
    private static final Color SELECTION_BG = new Color(0xD4D4D8);

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

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createSummaryBar(), BorderLayout.SOUTH);

        configureTable();
        loadTransactions();
        ApplicationState.getInstance().addStateChangeListener(this);
    }

    @Override public void onTransactionCompleted() { SwingUtilities.invokeLater(this::loadTransactions); }

    // Custom header that integrates the "View Receipt" button on the right
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel label = new JLabel("Transaction History");
        label.setFont(ThemeManager.getInstance().getTitleFont());
        label.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(label, BorderLayout.WEST);

        JButton viewButton = UIFactory.createButton("View Receipt", ThemeManager.getInstance().getAccentColor(), Color.WHITE, 8);
        viewButton.addActionListener(e -> viewSelectedReceipt());
        panel.add(viewButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Give the scrollpane a subtle border to frame the table
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInstance().getBorderColor(), 1));
        scrollPane.getViewport().setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel createSummaryBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        transactionCountLabel = new JLabel("0 transactions");
        transactionCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionCountLabel.setForeground(ThemeManager.getInstance().getTextSecondaryColor());
        panel.add(transactionCountLabel, BorderLayout.WEST);

        totalSumLabel = new JLabel("$0.00 total");
        totalSumLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalSumLabel.setForeground(ThemeManager.getInstance().getOrangeColor());
        panel.add(totalSumLabel, BorderLayout.EAST);

        return panel;
    }

    private void configureTable() {
        ThemeManager tm = ThemeManager.getInstance();

        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        transactionTable.setRowHeight(44);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 0));
        transactionTable.setBackground(tm.getPanelBackgroundColor());
        transactionTable.setSelectionBackground(SELECTION_BG);
        transactionTable.setSelectionForeground(tm.getTextColor());

        transactionTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBackground(tm.getPanelBackgroundColor());
                setForeground(tm.getTextSecondaryColor());
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, tm.getBorderColor()),
                        new EmptyBorder(0, 10, 0, 10)));
                setHorizontalAlignment(col == 0 ? CENTER : col == 3 ? RIGHT : LEFT);
                return this;
            }
        });

        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(130);

        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    setBackground(SELECTION_BG);
                    setForeground(tm.getTextColor());
                } else {
                    setBackground(row % 2 == 0 ? tm.getPanelBackgroundColor() : tm.getSurfaceColor());
                    setForeground(col == 0 ? tm.getTextSecondaryColor() : col == 3 ? tm.getOrangeColor() : tm.getTextColor());
                }
                setFont(col == 3 ? new Font("Segoe UI", Font.BOLD, 15) : new Font("Segoe UI", Font.PLAIN, 15));
                setHorizontalAlignment(col == 0 ? CENTER : col == 3 ? RIGHT : LEFT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = TransactionDAO.getInstance().loadAllTransactions();

        double totalSum = 0;
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{t.getId(), t.getFormattedTimestamp(), t.getTotalQuantity(), Utility.formatPrice(t.getTotal())});
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
            JOptionPane.showMessageDialog(this, "Please select a transaction to view.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Transaction t = TransactionDAO.getInstance().loadTransaction(id);
        if (t != null) {
            String receipt = t.generateReceipt(Config.getInstance().getStoreName(), Config.getInstance().getStoreAddress());
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
        transactionTable.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        SwingUtilities.updateComponentTreeUI(transactionTable);
        revalidate();
        repaint();
    }
}
