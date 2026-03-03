package pos.db;

import pos.util.Config;
import pos.util.Logger;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private static final DatabaseManager INSTANCE = new DatabaseManager();
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";

    private Connection connection;
    private final String dbPath;

    private DatabaseManager() {
        this.dbPath = Config.getInstance().getDbPath();
        try { Class.forName("org.sqlite.JDBC"); Logger.info("SQLite JDBC driver loaded"); }
        catch (ClassNotFoundException e) { Logger.error("Failed to load SQLite JDBC driver", e); }
    }

    public static DatabaseManager getInstance() { return INSTANCE; }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) connection = DriverManager.getConnection(DB_URL_PREFIX + dbPath);
        return connection;
    }

    public void initializeSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL, price REAL NOT NULL, cpu TEXT UNIQUE NOT NULL,
                    category TEXT, active INTEGER DEFAULT 1)""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL, total REAL NOT NULL, receipt_path TEXT)""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transaction_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER NOT NULL, product_name TEXT NOT NULL,
                    product_cpu TEXT NOT NULL, quantity INTEGER NOT NULL,
                    weight REAL DEFAULT 0, unit_price REAL NOT NULL,
                    FOREIGN KEY (transaction_id) REFERENCES transactions(id))""");
            try { conn.createStatement().execute("ALTER TABLE transaction_items ADD COLUMN weight REAL DEFAULT 0"); }
            catch (SQLException ignored) {}
            Logger.info("Database schema initialized");
        } catch (SQLException e) {
            Logger.error("Failed to initialize database schema", e);
        }
    }

    public void ensureReceiptsDirectory() {
        File dir = new File(Config.getInstance().getReceiptFolder());
        if (!dir.exists()) {
            if (dir.mkdirs()) Logger.info("Created receipts directory: " + dir.getPath());
            else Logger.error("Failed to create receipts directory: " + dir.getPath(), null);
        }
    }
}
