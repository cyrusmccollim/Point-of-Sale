package pos.db;

import pos.model.Transaction;
import pos.model.TransactionItem;
import pos.util.Config;
import pos.util.Logger;
import pos.util.PdfReceiptGenerator;
import pos.util.Utility;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static final TransactionDAO INSTANCE = new TransactionDAO();
    private final DatabaseManager dbManager;

    private TransactionDAO() { this.dbManager = DatabaseManager.getInstance(); }

    public static TransactionDAO getInstance() { return INSTANCE; }

    public int saveTransaction(Transaction transaction) {
        String insertTransaction = "INSERT INTO transactions (timestamp, total, receipt_path) VALUES (?, ?, ?)";
        String insertItem = "INSERT INTO transaction_items (transaction_id, product_name, product_cpu, quantity, weight, unit_price) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(insertTransaction, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, transaction.getTimestamp().toString());
                    pstmt.setDouble(2, transaction.getTotal());
                    pstmt.setString(3, transaction.getReceiptPath());
                    pstmt.executeUpdate();

                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (!generatedKeys.next()) throw new SQLException("Failed to get generated transaction ID");
                    int transactionId = generatedKeys.getInt(1);

                    try (PreparedStatement itemStmt = conn.prepareStatement(insertItem)) {
                        for (TransactionItem item : transaction.getItems()) {
                            itemStmt.setInt(1, transactionId);
                            itemStmt.setString(2, item.getProductName());
                            itemStmt.setString(3, item.getProductCpu());
                            itemStmt.setInt(4, item.getQuantity());
                            itemStmt.setDouble(5, item.getWeight());
                            itemStmt.setDouble(6, item.getUnitPrice());
                            itemStmt.addBatch();
                        }
                        itemStmt.executeBatch();
                    }
                    conn.commit();
                    Logger.info("Saved transaction #" + transactionId + " with " + transaction.getItemCount() + " items");
                    return transactionId;
                }
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) {
            Logger.error("Failed to save transaction", e);
        }
        return -1;
    }

    public List<Transaction> loadAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, timestamp, total, receipt_path FROM transactions ORDER BY timestamp DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Transaction t = new Transaction(id, LocalDateTime.parse(rs.getString("timestamp")), loadTransactionItems(conn, id), rs.getDouble("total"));
                t.setReceiptPath(rs.getString("receipt_path"));
                transactions.add(t);
            }
            Logger.info("Loaded " + transactions.size() + " transactions");
        } catch (SQLException e) {
            Logger.error("Failed to load transactions", e);
        }
        return transactions;
    }

    public Transaction loadTransaction(int id) {
        String sql = "SELECT id, timestamp, total, receipt_path FROM transactions WHERE id = ?";

        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Transaction t = new Transaction(id, LocalDateTime.parse(rs.getString("timestamp")), loadTransactionItems(conn, id), rs.getDouble("total"));
                t.setReceiptPath(rs.getString("receipt_path"));
                return t;
            }
        } catch (SQLException e) {
            Logger.error("Failed to load transaction #" + id, e);
        }
        return null;
    }

    private List<TransactionItem> loadTransactionItems(Connection conn, int transactionId) throws SQLException {
        List<TransactionItem> items = new ArrayList<>();
        String sql = "SELECT product_name, product_cpu, quantity, weight, unit_price FROM transaction_items WHERE transaction_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(new TransactionItem(rs.getString("product_name"), rs.getString("product_cpu"), rs.getInt("quantity"), rs.getDouble("weight"), rs.getDouble("unit_price")));
            }
        }
        return items;
    }

    public String saveReceiptPdf(Transaction transaction, String storeName, String storeAddress) {
        dbManager.ensureReceiptsDirectory();
        String filepath = Config.getInstance().getReceiptFolder() + File.separator + "receipt_" + transaction.getId() + "_" + Utility.getTimestampFilename() + ".pdf";
        return PdfReceiptGenerator.generateReceipt(transaction, storeName, storeAddress, filepath) ? filepath : null;
    }
}
