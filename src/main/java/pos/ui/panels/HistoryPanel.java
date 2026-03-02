package pos.ui.panels;

import pos.app.ThemeManager;
import pos.db.TransactionDAO;
import pos.util.Config;
import pos.model.Transaction;
import pos.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel that displays transaction history.
 */
public class HistoryPanel extends JPanel {
    private final String[] columns = {"ID", "Date/Time", "Items", "Total"};
    private final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable transactionTable = new JTable(tableModel);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Load data
        loadTransactions();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getSecondaryColor(), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getInstance().getTextColor());
        panel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(ThemeManager.getInstance().getSecondaryColor());
        refreshButton.setForeground(ThemeManager.getInstance().getTextColor());
        refreshButton.setFocusable(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadTransactions());
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTable.setRowHeight(30);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                ThemeManager.getInstance().getSecondaryColor(), 1, true));

        panel.add(scrollPane, BorderLayout.CENTER);

        // View receipt button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        JButton viewButton = new JButton("View Receipt");
        viewButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewButton.setBackground(ThemeManager.getInstance().getAccentColor());
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusable(false);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> viewSelectedReceipt());
        buttonPanel.add(viewButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadTransactions() {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Load from database
        List<Transaction> transactions = TransactionDAO.getInstance().loadAllTransactions();

        for (Transaction transaction : transactions) {
            Object[] row = {
                    transaction.getId(),
                    transaction.getFormattedTimestamp(),
                    transaction.getTotalQuantity(),
                    Utility.formatPrice(transaction.getTotal())
            };
            tableModel.addRow(row);
        }
    }

    private void viewSelectedReceipt() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a transaction to view.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int transactionId = (int) tableModel.getValueAt(selectedRow, 0);
        Transaction transaction = TransactionDAO.getInstance().loadTransaction(transactionId);

        if (transaction != null) {
            // Show receipt in dialog
            String receipt = transaction.generateReceipt(
                    Config.getInstance().getStoreName(),
                    Config.getInstance().getStoreAddress());

            JTextArea textArea = new JTextArea(receipt);
            textArea.setEditable(false);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            textArea.setBackground(ThemeManager.getInstance().getPanelBackgroundColor());

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 500));

            JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Receipt #" + transactionId,
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Updates the panel's theme colors.
     */
    public void updateTheme() {
        setBackground(ThemeManager.getInstance().getBackgroundColor());
        SwingUtilities.updateComponentTreeUI(transactionTable);
        revalidate();
        repaint();
    }
}