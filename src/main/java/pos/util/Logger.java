package pos.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Logger INSTANCE = new Logger();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {}

    public static void info(String message)    { INSTANCE.log("INFO",    message); }
    public static void warning(String message) { INSTANCE.log("WARNING", message); }

    public static void error(String message, Exception e) {
        INSTANCE.log("ERROR", message + (e != null ? " - " + e.getMessage() : ""));
    }

    private void log(String level, String message) {
        System.out.printf("[%s] [%s] %s%n", LocalDateTime.now().format(TIMESTAMP_FORMAT), level, message);
    }
}
