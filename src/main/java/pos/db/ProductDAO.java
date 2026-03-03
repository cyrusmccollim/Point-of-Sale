package pos.db;

import pos.model.Product;
import pos.util.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product entities.
 */
public class ProductDAO {
    private static final ProductDAO INSTANCE = new ProductDAO();
    private final DatabaseManager dbManager;

    private ProductDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public static ProductDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Loads products from the database.
     *
     * @return List of all active products
     */
    public List<Product> loadAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, cpu, category, active FROM products WHERE active = 1";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("cpu"),
                        rs.getString("category"),
                        rs.getBoolean("active")
                );
                products.add(product);
            }

            Logger.info("Loaded " + products.size() + " products from database");

        } catch (SQLException e) {
            Logger.error("Failed to load products from database", e);
        }

        return products;
    }

    /**
     * Finds a product by its name (partial match).
     */
    public List<Product> findByName(String name) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, cpu, category, active FROM products WHERE name LIKE ? AND active = 1";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("cpu"),
                        rs.getString("category"),
                        rs.getBoolean("active")
                );
                products.add(product);
            }

        } catch (SQLException e) {
            Logger.error("Failed to find products by name: " + name, e);
        }

        return products;
    }

    /**
     * Inserts a new product into the database.
     *
     * @param product The product to insert
     * @return The generated ID of the inserted product, or -1 on failure
     */
    public int insert(Product product) {
        String sql = "INSERT INTO products (name, price, cpu, category, active) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getCpu());
            pstmt.setString(4, product.getCategory());
            pstmt.setBoolean(5, product.isActive());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    Logger.info("Inserted product: " + product.getName() + " with ID: " + id);
                    return id;
                }
            }

        } catch (SQLException e) {
            Logger.error("Failed to insert product: " + product.getName(), e);
        }

        return -1;
    }

    /**
     * Updates an existing product.
     *
     * @param product The product to update
     * @return true if successful
     */
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, cpu = ?, category = ?, active = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getCpu());
            pstmt.setString(4, product.getCategory());
            pstmt.setBoolean(5, product.isActive());
            pstmt.setInt(6, product.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            Logger.error("Failed to update product: " + product.getId(), e);
        }

        return false;
    }

    /**
     * Imports products from a CSV file into the database.
     *
     * @param csvPath Path to the CSV file
     * @return Number of products imported
     */
    public int importFromCsv(String csvPath) {
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] parts = line.strip().split(",");

                if (parts.length >= 3) {
                    try {
                        String name = parts[0].strip();
                        double price = Double.parseDouble(parts[1].strip());
                        String cpu = parts[2].strip();

                        Product product = new Product(0, name, price, cpu);
                        int id = insert(product);

                        if (id > 0) {
                            imported++;
                        }
                    } catch (NumberFormatException e) {
                        Logger.warning("Skipping invalid price on line " + lineNumber + ": " + line);
                    }
                }
            }

            Logger.info("Imported " + imported + " products from CSV: " + csvPath);

        } catch (Exception e) {
            Logger.error("Failed to import products from CSV: " + csvPath, e);
        }

        return imported;
    }

    /**
     * Loads products from CSV file (fallback method when database is unavailable).
     *
     * @param csvPath Path to the CSV file
     * @return List of products
     */
    public List<Product> loadFromCsv(String csvPath) {
        List<Product> products = new ArrayList<>();
        int id = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.strip().split(",");

                if (parts.length >= 3) {
                    try {
                        String name = parts[0].strip();
                        double price = Double.parseDouble(parts[1].strip());
                        String cpu = parts[2].strip();

                        products.add(new Product(id++, name, price, cpu));
                    } catch (NumberFormatException e) {
                        Logger.warning("Skipping invalid product line: " + line);
                    }
                }
            }

            Logger.info("Loaded " + products.size() + " products from CSV: " + csvPath);

        } catch (Exception e) {
            Logger.error("Failed to load products from CSV: " + csvPath, e);
        }

        return products;
    }
}