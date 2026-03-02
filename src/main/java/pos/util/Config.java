package pos.util;

import pos.model.Department;

import java.io.*;
import java.util.Properties;

/**
 * Application configuration manager.
 * Handles persistent settings like store info, theme preference, etc.
 */
public class Config {
    private static final Config INSTANCE = new Config();
    private static final String CONFIG_FILE = "config.properties";

    private final Properties properties;

    // Default values
    private static final String DEFAULT_STORE_NAME = "POS Store";
    private static final String DEFAULT_STORE_ADDRESS = "123 Main Street";
    private static final String DEFAULT_RECEIPT_FOLDER = "receipts";
    private static final String DEFAULT_THEME = "light";
    private static final String DEFAULT_DB_PATH = "pos_database.db";
    private static final String DEFAULT_DEPARTMENT = "DELI";

    private Config() {
        properties = new Properties();
        load();
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    private void load() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                Logger.error("Failed to load config file", e);
            }
        }
        setDefaults();
    }

    private void setDefaults() {
        if (!properties.containsKey("store.name")) {
            properties.setProperty("store.name", DEFAULT_STORE_NAME);
        }
        if (!properties.containsKey("store.address")) {
            properties.setProperty("store.address", DEFAULT_STORE_ADDRESS);
        }
        if (!properties.containsKey("receipt.folder")) {
            properties.setProperty("receipt.folder", DEFAULT_RECEIPT_FOLDER);
        }
        if (!properties.containsKey("theme")) {
            properties.setProperty("theme", DEFAULT_THEME);
        }
        if (!properties.containsKey("db.path")) {
            properties.setProperty("db.path", DEFAULT_DB_PATH);
        }
        if (!properties.containsKey("department")) {
            properties.setProperty("department", DEFAULT_DEPARTMENT);
        }
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "POS System Configuration");
        } catch (IOException e) {
            Logger.error("Failed to save config file", e);
        }
    }

    // Store configuration
    public String getStoreName() {
        return properties.getProperty("store.name", DEFAULT_STORE_NAME);
    }

    public void setStoreName(String name) {
        properties.setProperty("store.name", name);
    }

    public String getStoreAddress() {
        return properties.getProperty("store.address", DEFAULT_STORE_ADDRESS);
    }

    public void setStoreAddress(String address) {
        properties.setProperty("store.address", address);
    }

    // Receipt configuration
    public String getReceiptFolder() {
        return properties.getProperty("receipt.folder", DEFAULT_RECEIPT_FOLDER);
    }

    public void setReceiptFolder(String folder) {
        properties.setProperty("receipt.folder", folder);
    }

    // Theme configuration
    public String getTheme() {
        return properties.getProperty("theme", DEFAULT_THEME);
    }

    public void setTheme(String theme) {
        properties.setProperty("theme", theme);
    }

    // Database configuration
    public String getDbPath() {
        return properties.getProperty("db.path", DEFAULT_DB_PATH);
    }

    public void setDbPath(String path) {
        properties.setProperty("db.path", path);
    }

    // Department configuration
    public Department getDepartment() {
        String deptName = properties.getProperty("department", DEFAULT_DEPARTMENT);
        return Department.fromDisplayName(deptName);
    }

    public void setDepartment(Department department) {
        properties.setProperty("department", department.name());
    }

    public void setDepartment(String departmentName) {
        properties.setProperty("department", departmentName);
    }
}