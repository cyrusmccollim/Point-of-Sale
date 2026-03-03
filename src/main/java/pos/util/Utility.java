package pos.util;

public class Utility {

    public static String formatPrice(double price) { return String.format("$%.2f", price); }

    public static String getTimestampFilename() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }
}
