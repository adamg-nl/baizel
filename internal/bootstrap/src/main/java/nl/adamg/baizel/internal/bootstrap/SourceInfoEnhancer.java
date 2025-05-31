package nl.adamg.baizel.internal.bootstrap;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

/**
 * In log lines, changes {@code [package.name.ClassName]} into {@code .(ClassName.java:NN)},
 * which in IntelliJ's console becomes a clickable hyperlink.
 */
public final class SourceInfoEnhancer extends SimpleFormatter {
    private static final Pattern LOG_CLASS_PATTERN = Pattern.compile("^[0-9:.]+ [A-Z]+ \\[(?<CLASS>[a-zA-Z0-9.$]+)\\] .*");

    public SourceInfoEnhancer() {
        this.toString();
    }

    @Override
    public synchronized String format(LogRecord record) {
        var base = super.format(record);
        var originClass = getOriginClass(base);
        if (originClass == null) {
            return base;
        }
        var sourceLocation = getSourceLocation(originClass);
        if (sourceLocation == null) {
            return base;
        }
        var sourceLocationIdeaLink = ".(" + sourceLocation + ")";
        return base.replace("[" + originClass + "]", sourceLocationIdeaLink);
    }

    /*@CheckForNull*/
    private String getOriginClass(String logLine) {
        var matcher = LOG_CLASS_PATTERN.matcher(logLine.trim());
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group("CLASS");
    }

    /*@CheckForNull*/
    private String getSourceLocation(String originClass) {
        var stackFrame = (StackTraceElement) null;
        var stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, 20); i++) {
            StackTraceElement frame = stackTrace[i];
            if (frame.getClassName().equals(originClass)) {
                stackFrame = frame;
                break;
            }
        }
        if (stackFrame == null) {
            return null;
        }
        return getFileName(stackFrame) + ":" + stackFrame.getLineNumber();
    }

    private static String getFileName(StackTraceElement stackFrame) {
        var fileName = stackFrame.getFileName();
        if (fileName != null) {
            return fileName;
        }
        return stackFrame.getClassName().replaceAll(".*\\.", "") + ".java";
    }
}
