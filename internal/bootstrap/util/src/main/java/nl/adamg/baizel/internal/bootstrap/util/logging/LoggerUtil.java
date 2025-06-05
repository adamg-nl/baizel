package nl.adamg.baizel.internal.bootstrap.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {
    public static void configureLogger() {
        if ("true".equals(System.getenv("BAIZEL_VERBOSE"))) {
            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS.%1$tL %4$-4s [%3$s] %5$s%n");
            System.setProperty("java.util.logging.ConsoleHandler.level", Level.FINEST.getName());
            var formatter = new SourceInfoEnhancer();
            for (var handler : Logger.getLogger("").getHandlers()) {
                handler.setFormatter(formatter);
            }
        } else {
            System.setProperty("java.util.logging.SimpleFormatter.format", "");
            System.setProperty("java.util.logging.ConsoleHandler.level", Level.OFF.getName());
        }
    }
}
