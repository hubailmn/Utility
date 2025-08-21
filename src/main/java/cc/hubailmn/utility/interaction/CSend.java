package cc.hubailmn.utility.interaction;

import cc.hubailmn.utility.BasePlugin;
import org.bukkit.Bukkit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public final class CSend {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long MAX_LOG_SIZE = 5 * 1024 * 1024;
    private static final int MAX_LOG_FILES = 20;
    private static final int RETENTION_DAYS = 14;

    private static final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<LogEntry> consoleQueue = new LinkedBlockingQueue<>();

    private static File DEBUG_LOG;
    private static File ERROR_LOG;
    private static File PLUGIN_LOG;

    private static Thread logThread;
    private static Thread consoleThread;
    private static volatile boolean running = true;

    private CSend() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void init(File dataFolder) {
        File logDir = new File(dataFolder, "debug");
        ensureDirectoryExists(logDir);

        DEBUG_LOG = new File(logDir, "debug.log");
        ERROR_LOG = new File(logDir, "error.log");
        PLUGIN_LOG = new File(logDir, "plugin.log");

        limitLogFileCount(logDir, MAX_LOG_FILES);
        rotateLogs(logDir);
        deleteOldLogs(logDir, RETENTION_DAYS);

        startLoggerThreads();
    }

    public static void shutdown() {
        try {
            running = false;
            if (logThread != null) {
                logThread.interrupt();
            }

            while (!logQueue.isEmpty()) {
                LogEntry entry = logQueue.poll();
                if (entry != null) {
                    String formattedMessage = formatMessage(entry.messagePattern(), entry.arguments());
                    writeToFile(entry.file(), "[" + FORMAT.format(new Date()) + "] " + stripColor(formattedMessage));
                }
            }

            if (consoleThread != null) {
                consoleThread.interrupt();
            }

            if (logThread != null) {
                logThread.interrupt();
            }
        } catch (Exception ignored) {

        }
    }

    private static void startLoggerThreads() {
        logThread = new Thread(() -> {
            while (running || !logQueue.isEmpty()) {
                try {
                    LogEntry entry = logQueue.poll(1, TimeUnit.SECONDS);
                    if (entry != null) {
                        String formattedMessage = formatMessage(entry.messagePattern(), entry.arguments());
                        writeToFile(entry.file(), "[" + FORMAT.format(new Date()) + "] " + stripColor(formattedMessage));
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "CSend-Logger-Thread");
        logThread.setDaemon(true);
        logThread.start();

        consoleThread = new Thread(() -> {
            while (running || !consoleQueue.isEmpty()) {
                try {
                    LogEntry entry = consoleQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        String prefixedMessage = getPrefix() + " " + formatMessage(entry.messagePattern(), entry.arguments());
                        Bukkit.getConsoleSender().sendMessage(prefixedMessage);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "CSend-Console-Thread");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private static String getPrefix() {
        if (BasePlugin.getInstance() == null || BasePlugin.getInstance().getPluginName() == null) {
            return "§7[§aPlugin§7] §b>>§r";
        }

        String prefix = BasePlugin.getPrefix();
        return (prefix == null || prefix.isEmpty())
               ? "§7[§a" + BasePlugin.getInstance().getPluginName() + "§7] §b>>§r"
               : prefix;
    }

    // ===== SLF4J-style parameterized methods =====

    /**
     * Formats a message with SLF4J-style {} placeholders
     *
     * @param messagePattern The message pattern with {} placeholders
     * @param arguments      The arguments to replace placeholders with
     * @return The formatted message
     */
    private static String formatMessage(String messagePattern, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return messagePattern;
        }

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int lastIndex = 0;

        while (true) {
            int placeholderIndex = messagePattern.indexOf("{}", lastIndex);
            if (placeholderIndex == -1) {
                result.append(messagePattern.substring(lastIndex));
                break;
            }

            result.append(messagePattern, lastIndex, placeholderIndex);

            if (argIndex < arguments.length) {
                Object arg = arguments[argIndex++];
                result.append(formatArgument(arg));
            } else {
                result.append("{}");
            }

            lastIndex = placeholderIndex + 2;
        }

        return result.toString();
    }

    private static String formatArgument(Object arg) {
        if (arg == null) {
            return "null";
        }

        if (arg instanceof Throwable t) {
            return t.getClass().getSimpleName() + ": " + t.getMessage();
        }

        if (arg.getClass().isArray()) {
            if (arg instanceof Object[]) {
                return Arrays.toString((Object[]) arg);
            } else if (arg instanceof int[]) {
                return Arrays.toString((int[]) arg);
            } else if (arg instanceof long[]) {
                return Arrays.toString((long[]) arg);
            } else if (arg instanceof boolean[]) {
                return Arrays.toString((boolean[]) arg);
            } else if (arg instanceof byte[]) {
                return Arrays.toString((byte[]) arg);
            } else if (arg instanceof char[]) {
                return Arrays.toString((char[]) arg);
            } else if (arg instanceof double[]) {
                return Arrays.toString((double[]) arg);
            } else if (arg instanceof float[]) {
                return Arrays.toString((float[]) arg);
            } else if (arg instanceof short[]) {
                return Arrays.toString((short[]) arg);
            }
        }

        return arg.toString();
    }

    // ===== Public logging methods with SLF4J-style support =====

    public static void plain(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static void plain(String messagePattern, Object... arguments) {
        Bukkit.getConsoleSender().sendMessage(formatMessage(messagePattern, arguments));
    }

    public static void prefixed(String message) {
        plain(getPrefix() + " " + message);
    }

    public static void prefixed(String messagePattern, Object... arguments) {
        prefixed(formatMessage(messagePattern, arguments));
    }

    public static void info(String message) {
        consoleQueue.offer(new LogEntry(null, "§e[INFO] §r" + message));
        logQueue.offer(new LogEntry(DEBUG_LOG, "§e[INFO] §r" + message));
    }

    public static void info(String messagePattern, Object... arguments) {
        consoleQueue.offer(new LogEntry(null, "§e[INFO] §r" + messagePattern, arguments));
        logQueue.offer(new LogEntry(DEBUG_LOG, "§e[INFO] §r" + messagePattern, arguments));
    }

    public static void warn(String message) {
        consoleQueue.offer(new LogEntry(null, "§c[WARNING] §r" + message));
        logQueue.offer(new LogEntry(DEBUG_LOG, "§c[WARNING] §r" + message));
    }

    public static void warn(String messagePattern, Object... arguments) {
        consoleQueue.offer(new LogEntry(null, "§c[WARNING] §r" + messagePattern, arguments));
        logQueue.offer(new LogEntry(DEBUG_LOG, "§c[WARNING] §r" + messagePattern, arguments));
    }

    public static void debug(String message) {
        if (BasePlugin.getInstance().isDebug()) {
            consoleQueue.offer(new LogEntry(null, "§b[DEBUG] §r" + message));
        }
        logQueue.offer(new LogEntry(DEBUG_LOG, "§b[DEBUG] §r" + message));
    }

    public static void debug(String messagePattern, Object... arguments) {
        if (BasePlugin.getInstance().isDebug()) {
            consoleQueue.offer(new LogEntry(null, "§b[DEBUG] §r" + messagePattern, arguments));
        }
        logQueue.offer(new LogEntry(DEBUG_LOG, "§b[DEBUG] §r" + messagePattern, arguments));
    }

    public static void log(String message) {
        logQueue.offer(new LogEntry(PLUGIN_LOG, message));
    }

    public static void log(String messagePattern, Object... arguments) {
        logQueue.offer(new LogEntry(PLUGIN_LOG, messagePattern, arguments));
    }

    public static void error(String message) {
        consoleQueue.offer(new LogEntry(null, "§4[ERROR] §r" + message));
        logQueue.offer(new LogEntry(ERROR_LOG, "§4[ERROR] §r" + message));
    }

    public static void error(String messagePattern, Object... arguments) {
        consoleQueue.offer(new LogEntry(null, "§4[ERROR] §r" + messagePattern, arguments));
        logQueue.offer(new LogEntry(ERROR_LOG, "§4[ERROR] §r" + messagePattern, arguments));
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        if (throwable != null) {
            logThrowable(throwable);
        }
    }

    public static void error(String messagePattern, Throwable throwable, Object... arguments) {
        error(formatMessage(messagePattern, arguments), throwable);
    }

    public static void error(Throwable throwable) {
        if (throwable == null) {
            error("Unknown error (null throwable).");
            return;
        }

        error("Exception: {} - {}", throwable.getClass().getSimpleName(), throwable.getMessage());
        logThrowable(throwable);
    }

    private static void logThrowable(Throwable throwable) {
        for (StackTraceElement ste : throwable.getStackTrace()) {
            log(ERROR_LOG, "  at " + ste);
        }

        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            log(ERROR_LOG, "Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            logThrowable(cause);
        }
    }

    // ===== Conditional logging methods =====

    public static boolean isDebugEnabled() {
        return BasePlugin.getInstance() != null && BasePlugin.getInstance().isDebug();
    }

    public static void debugIf(boolean condition, String message) {
        if (condition) {
            debug(message);
        }
    }

    public static void debugIf(boolean condition, String messagePattern, Object... arguments) {
        if (condition) {
            debug(messagePattern, arguments);
        }
    }

    // ===== Trace level logging (only to file, not console) =====

    public static void trace(String message) {
        logQueue.offer(new LogEntry(DEBUG_LOG, "§8[TRACE] §r" + message));
    }

    public static void trace(String messagePattern, Object... arguments) {
        logQueue.offer(new LogEntry(DEBUG_LOG, "§8[TRACE] §r" + messagePattern, arguments));
    }

    // ===== Performance-aware logging =====

    /**
     * Logs a message only if debug is enabled, avoiding string concatenation costs
     */
    public static void debugLazy(java.util.function.Supplier<String> messageSupplier) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get());
        }
    }

    /**
     * Times a block of code and logs the execution time
     */
    public static <T> T timed(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long duration = System.nanoTime() - start;
            debug("Operation '{}' took {}ms", operation, duration / 1_000_000.0);
        }
    }

    private static void logAsync(File file, String message) {
        logQueue.offer(new LogEntry(file, message));
    }

    private static void log(File file, String message) {
        logQueue.offer(new LogEntry(file, message));
    }

    private static void writeToFile(File file, String message) {
        try {
            ensureDirectoryExists(file.getParentFile());

            if (!file.exists() && !file.createNewFile()) {
                plain("§c[Logger Error]§r Failed to create log file: {}", file.getName());
                return;
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(message + System.lineSeparator());
            }
        } catch (IOException e) {
            plain("§c[Logger Error]§r Failed to write to log file: {}", file.getName());
            getFallbackLogger().warning("File write error: " + e.getMessage());
        }
    }

    private static void rotateLogs(File logDir) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = sdf.format(new Date());

        rotateSingleLog(DEBUG_LOG, new File(logDir, "debug_" + timestamp + ".log"), "debug.log");
        rotateSingleLog(ERROR_LOG, new File(logDir, "error_" + timestamp + ".log"), "error.log");
        rotateSingleLog(PLUGIN_LOG, new File(logDir, "plugin_" + timestamp + ".log"), "plugin.log");
    }

    private static void rotateSingleLog(File current, File rotated, String label) {
        if (!current.exists()) return;

        if (current.length() < MAX_LOG_SIZE) return;

        if (!current.renameTo(rotated)) {
            getFallbackLogger().warning("Failed to rotate " + label + " to " + rotated.getName());
        } else {
            getFallbackLogger().info("Rotated " + label + " to " + rotated.getName());
            compressFile(rotated);
        }
    }

    private static void compressFile(File source) {
        File compressed = new File(source.getAbsolutePath() + ".gz");
        try (
                FileInputStream fis = new FileInputStream(source);
                GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(compressed))
        ) {
            fis.transferTo(gzos);
            if (!source.delete()) {
                getFallbackLogger().warning("Failed to delete uncompressed log: " + source.getName());
            }
        } catch (IOException e) {
            getFallbackLogger().warning("Failed to compress log: " + source.getName());
        }
    }

    private static void deleteOldLogs(File logDir, int days) {
        File[] files = logDir.listFiles((dir, name) -> name.matches(".*_(debug|error|plugin)_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.log(\\.gz)?"));
        if (files == null) return;

        long now = System.currentTimeMillis();
        long maxAge = days * 24L * 60 * 60 * 1000;

        for (File file : files) {
            if (now - file.lastModified() > maxAge && !file.delete()) {
                getFallbackLogger().warning("Failed to delete old log: " + file.getName());
            }
        }
    }

    private static void limitLogFileCount(File logDir, int maxFiles) {
        File[] files = logDir.listFiles((dir, name) -> name.matches(".*_(debug|error|plugin)_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.log(\\.gz)?"));
        if (files == null || files.length <= maxFiles) return;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        for (int i = 0; i < files.length - maxFiles; i++) {
            if (!files[i].delete()) {
                getFallbackLogger().warning("Failed to delete old log (count cleanup): " + files[i].getName());
            }
        }
    }

    private static void ensureDirectoryExists(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            plain("§c[Logger Error]§r Failed to create directory: {}", dir.getPath());
        }
    }

    private static Logger getFallbackLogger() {
        return (BasePlugin.getInstance() != null)
               ? BasePlugin.getInstance().getLogger()
               : Bukkit.getLogger();
    }

    private static String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    private record LogEntry(File file, String messagePattern, Object... arguments) {
        public LogEntry(File file, String message) {
            this(file, message, new Object[0]);
        }
    }
}