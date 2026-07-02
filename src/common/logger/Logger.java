package common.logger;

import common.inject.InjectorService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger class
 */
public class Logger {
    private static LogLevel logLevel = LogLevel.DEBUG;
    private final Class<?> clazz;
    private final boolean onlyLevel;
    private static boolean enableLoggingToDatabase = false;
    private static final LogService logService = InjectorService.getInstance().getInstance(LogService.class);

    public Logger(Class<?> clazz) {
        this(clazz, false);
    }

    public Logger(Class<?> clazz, boolean onlyLevel) {
        this.clazz = clazz;
        this.onlyLevel = onlyLevel;
    }

    public static void setEnableLoggingToDatabase(boolean enableLoggingToDatabase) {
        Logger.enableLoggingToDatabase = enableLoggingToDatabase;
    }

    /**
     * Set the maximum log level
     * @param logLevel The maximum log level
     */
    public static void setMaxLogLevel(LogLevel logLevel) {
        Logger.logLevel = logLevel;
    }

    /**
     * Log a message at the info level
     * @param message The message to log
     */
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Log a message at the warn level
     * @param message The message to log
     */
    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    /**
     * Log a message at the error level
     * @param message The message to log
     */
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    /**
     * Log a message at the error level
     * @param e The exception to log
     */
    public void trace(Exception e) {
        trace((Throwable) e);
    }

    /**
     * Log a message at the error level
     * @param e The exception to log
     */
    public void trace(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();

        String stackTrace = writer.toString();
        log(LogLevel.ERROR, stackTrace);
    }

    /**
     * Log a message at the debug level
     * @param message The message to log
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    private void log(LogLevel level, String message) {
        if (level.ordinal() <= logLevel.ordinal()) {
            if (onlyLevel) {
                System.out.printf("[%s] %s%n", level.getColorized(), message);
            } else {
                System.out.printf("%s [%s] %s %s%n", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS Z").format(new Date()),
                        clazz.getCanonicalName(), level.getColorized(), message);
            }
        }

        if (!enableLoggingToDatabase) {
            return;
        }
        if (level.ordinal() < LogLevel.DEBUG.ordinal()) {
            logService.save(new LogEvent(message, level.name()));
        }
    }
}