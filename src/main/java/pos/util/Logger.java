package pos.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logging utility for the POS system.
 * Provides logging with levels and timestamps.
 */
public class Logger {
    private static final Logger INSTANCE = new Logger();
    private final StringBuilder logBuilder = new StringBuilder();
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
        // Private constructor for singleton
    }

    public static void info(String message) {
        INSTANCE.log("INFO", message);
    }

    public static void warning(String message) {
        INSTANCE.log("WARNING", message);
    }

    public static void error(String message) {
        INSTANCE.log("ERROR", message);
    }

    public static void error(String message, Exception e) {
        INSTANCE.log("ERROR", message + " - " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            INSTANCE.logBuilder.append("\n\tat ").append(element.toString());
        }
    }

    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        logBuilder.append(logEntry).append("\n");
        System.out.println(logEntry);
    }

    public static void addLog(String message) {
        INSTANCE.log("INFO", message);
    }

    public static void printLog() {
        System.out.println(INSTANCE.logBuilder.toString());
    }

    public static void saveToFile(String path) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(INSTANCE.logBuilder.toString());
        } catch (IOException e) {
            error("Failed to save log file", e);
        }
    }

    public static String getLog() {
        return INSTANCE.logBuilder.toString();
    }
}