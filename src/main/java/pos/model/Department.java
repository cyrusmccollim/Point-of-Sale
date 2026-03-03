package pos.model;

public enum Department {
    DELI("Deli", "data/deli.csv"),
    BAKERY("Bakery", "data/bakery.csv"),
    SEAFOOD("Seafood", "data/seafood.csv"),
    PRODUCE("Produce", "data/produce.csv");

    private final String displayName;
    private final String catalogPath;

    Department(String displayName, String catalogPath) {
        this.displayName = displayName;
        this.catalogPath = catalogPath;
    }

    public String getDisplayName() { return displayName; }
    public String getCatalogPath() { return catalogPath; }
}
