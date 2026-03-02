package pos.util;

/**
 * Utility class with helper methods.
 */
public class Utility {

    /**
     * Checks if a string can be parsed as an integer.
     *
     * @param input The string to check
     * @return true if the string can be parsed as an integer, false otherwise
     */
    public static boolean isInteger(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string can be parsed as a double.
     *
     * @param input The string to check
     * @return true if the string can be parsed as a double, false otherwise
     */
    public static boolean isDouble(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Formats a price value to a string with 2 decimal places.
     *
     * @param price The price value
     * @return Formatted price string
     */
    public static String formatPrice(double price) {
        return String.format("$%.2f", price);
    }

    /**
     * Formats a quantity with appropriate decimal places.
     *
     * @param quantity The quantity value
     * @return Formatted quantity string
     */
    public static String formatQuantity(double quantity) {
        if (quantity == (int) quantity) {
            return String.valueOf((int) quantity);
        }
        return String.format("%.2f", quantity);
    }

    /**
     * Creates a receipt-safe filename from a timestamp.
     *
     * @return A filename-safe timestamp string
     */
    public static String getTimestampFilename() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }
}