package pos.db;

import pos.util.Config;
import pos.util.Logger;

import java.io.File;
import java.sql.*;

/**
 * Manages SQLite database connections and schema initialization.
 */
public class DatabaseManager {
    private static final DatabaseManager INSTANCE = new DatabaseManager();
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";

    private Connection connection;
    private final String dbPath;

    private DatabaseManager() {
        this.dbPath = Config.getInstance().getDbPath();
        initialize();
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    private void initialize() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            Logger.info("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            Logger.error("Failed to load SQLite JDBC driver", e);
        }
    }

    /**
     * Gets a database connection. Creates a new connection if none exists.
     *
     * @return Database connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL_PREFIX + dbPath);
        }
        return connection;
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                Logger.info("Database connection closed");
            } catch (SQLException e) {
                Logger.error("Failed to close database connection", e);
            }
        }
    }

    /**
     * Initializes the database schema.
     * Creates tables if they don't exist.
     */
    public void initializeSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create products table
            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    cpu TEXT UNIQUE NOT NULL,
                    category TEXT,
                    active INTEGER DEFAULT 1
                )
                """;
            stmt.execute(createProductsTable);

            // Create transactions table
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    total REAL NOT NULL,
                    receipt_path TEXT
                )
                """;
            stmt.execute(createTransactionsTable);

            // Create transaction_items table
            String createTransactionItemsTable = """
                CREATE TABLE IF NOT EXISTS transaction_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER NOT NULL,
                    product_name TEXT NOT NULL,
                    product_cpu TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price REAL NOT NULL,
                    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
                )
                """;
            stmt.execute(createTransactionItemsTable);

            Logger.info("Database schema initialized successfully");

        } catch (SQLException e) {
            Logger.error("Failed to initialize database schema", e);
        }
    }

    /**
     * Checks if the products table is empty.
     *
     * @return true if no products exist
     */
    public boolean isProductsEmpty() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            Logger.error("Failed to check if products table is empty", e);
        }
        return true;
    }

    /**
     * Creates the receipts directory if it doesn't exist.
     */
    public void ensureReceiptsDirectory() {
        String receiptFolder = Config.getInstance().getReceiptFolder();
        File dir = new File(receiptFolder);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Logger.info("Created receipts directory: " + receiptFolder);
            } else {
                Logger.error("Failed to create receipts directory: " + receiptFolder, null);
            }
        }
    }
}