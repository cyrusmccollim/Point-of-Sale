package pos.util;

public class Utility {
    private static final double LBS_PER_KG = 2.20462;
    private static final double KGS_PER_LB = 0.453592;

    public static String formatPrice(double price) { return String.format("$%.2f", price); }

    public static String getWeightUnit() { return Config.getInstance().isUseKg() ? "kg" : "lb"; }

    /** Convert stored lbs value to display string in the current unit. */
    public static String formatWeight(double lbs) {
        return Config.getInstance().isUseKg()
                ? String.format("%.3f kg", lbs * KGS_PER_LB)
                : String.format("%.2f lb", lbs);
    }

    /** Format a per-lb price as the current unit price ($/lb or $/kg). */
    public static String formatUnitPrice(double pricePerLb) {
        return Config.getInstance().isUseKg()
                ? String.format("$%.2f/kg", pricePerLb * LBS_PER_KG)
                : String.format("$%.2f/lb", pricePerLb);
    }

    /** Convert a user-entered value (in the current unit) to lbs for internal storage. */
    public static double inputToLbs(double input) {
        return Config.getInstance().isUseKg() ? input * LBS_PER_KG : input;
    }

    /** Convert a stored lbs value to the display unit. */
    public static double lbsToDisplayUnit(double lbs) {
        return Config.getInstance().isUseKg() ? lbs * KGS_PER_LB : lbs;
    }

    public static String getTimestampFilename() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }
}
