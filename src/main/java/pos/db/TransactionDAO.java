package pos.db;

import pos.model.Transaction;
import pos.model.TransactionItem;
import pos.util.Config;
import pos.util.Logger;
import pos.util.PdfReceiptGenerator;
import pos.util.Utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Transaction entities.
 */
public class TransactionDAO {
    private static final TransactionDAO INSTANCE = new TransactionDAO();
    private final DatabaseManager dbManager;

    private TransactionDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public static TransactionDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Saves a transaction to the database.
     *
     * @param transaction The transaction to save
     * @return The generated transaction ID, or -1 on failure
     */
    public int saveTransaction(Transaction transaction) {
        String insertTransaction = "INSERT INTO transactions (timestamp, total, receipt_path) VALUES (?, ?, ?)";
        String insertItem = "INSERT INTO transaction_items (transaction_id, product_name, product_cpu, quantity, weight, unit_price) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Insert transaction
                try (PreparedStatement pstmt = conn.prepareStatement(insertTransaction, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, transaction.getTimestamp().toString());
                    pstmt.setDouble(2, transaction.getTotal());
                    pstmt.setString(3, transaction.getReceiptPath());

                    pstmt.executeUpdate();

                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (!generatedKeys.next()) {
                        throw new SQLException("Failed to get generated transaction ID");
                    }

                    int transactionId = generatedKeys.getInt(1);

                    // Insert transaction items
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

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            Logger.error("Failed to save transaction", e);
        }

        return -1;
    }

    /**
     * Loads all transactions from the database.
     *
     * @return List of all transactions
     */
    public List<Transaction> loadAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, timestamp, total, receipt_path FROM transactions ORDER BY timestamp DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));
                double total = rs.getDouble("total");
                String receiptPath = rs.getString("receipt_path");

                List<TransactionItem> items = loadTransactionItems(conn, id);

                Transaction transaction = new Transaction(id, timestamp, items, total);
                transaction.setReceiptPath(receiptPath);
                transactions.add(transaction);
            }

            Logger.info("Loaded " + transactions.size() + " transactions from database");

        } catch (SQLException e) {
            Logger.error("Failed to load transactions", e);
        }

        return transactions;
    }

    /**
     * Loads a transaction by ID.
     *
     * @param id The transaction ID
     * @return The transaction if found, null otherwise
     */
    public Transaction loadTransaction(int id) {
        String sql = "SELECT id, timestamp, total, receipt_path FROM transactions WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));
                double total = rs.getDouble("total");
                String receiptPath = rs.getString("receipt_path");

                List<TransactionItem> items = loadTransactionItems(conn, id);

                Transaction transaction = new Transaction(id, timestamp, items, total);
                transaction.setReceiptPath(receiptPath);
                return transaction;
            }

        } catch (SQLException e) {
            Logger.error("Failed to load transaction #" + id, e);
        }

        return null;
    }

    /**
     * Loads items for a specific transaction.
     */
    private List<TransactionItem> loadTransactionItems(Connection conn, int transactionId) throws SQLException {
        List<TransactionItem> items = new ArrayList<>();
        String sql = "SELECT product_name, product_cpu, quantity, weight, unit_price FROM transaction_items WHERE transaction_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TransactionItem item = new TransactionItem(
                        rs.getString("product_name"),
                        rs.getString("product_cpu"),
                        rs.getInt("quantity"),
                        rs.getDouble("weight"),
                        rs.getDouble("unit_price")
                );
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Saves a receipt to a file.
     *
     * @param transaction The transaction
     * @param receiptContent The receipt content
     * @return The file path of the saved receipt
     */
    public String saveReceipt(Transaction transaction, String receiptContent) {
        String receiptFolder = Config.getInstance().getReceiptFolder();
        dbManager.ensureReceiptsDirectory();

        String filename = "receipt_" + transaction.getId() + "_" + Utility.getTimestampFilename() + ".txt";
        String filepath = receiptFolder + File.separator + filename;

        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(receiptContent);
            Logger.info("Saved receipt to: " + filepath);
            return filepath;
        } catch (IOException e) {
            Logger.error("Failed to save receipt file", e);
            return null;
        }
    }

    /**
     * Saves a receipt as PDF.
     *
     * @param transaction The transaction
     * @param storeName The store name
     * @param storeAddress The store address
     * @return The file path of the saved PDF receipt
     */
    public String saveReceiptPdf(Transaction transaction, String storeName, String storeAddress) {
        String receiptFolder = Config.getInstance().getReceiptFolder();
        dbManager.ensureReceiptsDirectory();

        String filename = "receipt_" + transaction.getId() + "_" + Utility.getTimestampFilename() + ".pdf";
        String filepath = receiptFolder + File.separator + filename;

        if (PdfReceiptGenerator.generateReceipt(transaction, storeName, storeAddress, filepath)) {
            return filepath;
        }
        return null;
    }

    /**
     * Gets the transaction count for today.
     *
     * @return Number of transactions today
     */
    public int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE date(timestamp) = date('now')";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            Logger.error("Failed to get today's transaction count", e);
        }

        return 0;
    }

    /**
     * Gets the total sales for today.
     *
     * @return Total sales amount today
     */
    public double getTodayTotalSales() {
        String sql = "SELECT SUM(total) FROM transactions WHERE date(timestamp) = date('now')";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            Logger.error("Failed to get today's total sales", e);
        }

        return 0;
    }
}