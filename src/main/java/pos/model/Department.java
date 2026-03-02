package pos.model;

/**
 * Represents a department in the store with its own product catalog.
 * Each department has specific weighed goods products.
 */
public enum Department {
    DELI("Deli", "data/deli.csv", "DL"),
    BAKERY("Bakery", "data/bakery.csv", "BK"),
    SEAFOOD("Seafood", "data/seafood.csv", "SF"),
    PRODUCE("Produce", "data/produce.csv", "PR");

    private final String displayName;
    private final String catalogPath;
    private final String codePrefix;

    Department(String displayName, String catalogPath, String codePrefix) {
        this.displayName = displayName;
        this.catalogPath = catalogPath;
        this.codePrefix = codePrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCatalogPath() {
        return catalogPath;
    }

    public String getCodePrefix() {
        return codePrefix;
    }

    /**
     * Finds a department by its display name (case-insensitive).
     *
     * @param name The department name
     * @return The matching department, or DELI as default
     */
    public static Department fromDisplayName(String name) {
        if (name == null) {
            return DELI;
        }
        for (Department dept : values()) {
            if (dept.displayName.equalsIgnoreCase(name)) {
                return dept;
            }
        }
        return DELI;
    }
}
