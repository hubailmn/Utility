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

    private static File DEBUG_LOG;
    private static File ERROR_LOG;
    private static File PLUGIN_LOG;

    private static Thread logThread;
    private static volatile boolean running = true;

    private CSend() {
        throw new UnsupportedOperationException("CSend is a utility class and should not be instantiated.");
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

        startLoggerThread();
    }

    public static void shutdown() {
        running = false;
        if (logThread != null) {
            logThread.interrupt();
        }
    }

    private static void startLoggerThread() {
        logThread = new Thread(() -> {
            while (running || !logQueue.isEmpty()) {
                try {
                    LogEntry entry = logQueue.poll(1, TimeUnit.SECONDS);
                    if (entry != null) {
                        writeToFile(entry.file, entry.message);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }, "Async-Logger-Thread");
        logThread.setDaemon(true);
        logThread.start();
    }

    private static String getPrefix() {
        if (BasePlugin.getInstance() == null || BasePlugin.getPluginName() == null) {
            return "§7[§aPlugin§7] §b>>§r";
        }

        String prefix = BasePlugin.getPrefix();
        return (prefix == null || prefix.isEmpty())
               ? "§7[§a" + BasePlugin.getPluginName() + "§7] §b>>§r"
               : prefix;
    }

    public static void plain(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static void prefixed(String message) {
        plain(getPrefix() + " " + message);
    }

    public static void info(String message) {
        prefixed("§e[INFO] §r" + message);
    }

    public static void warn(String message) {
        prefixed("§c[WARNING] §r" + message);
    }

    public static void debug(String message) {
        String fullMessage = "§b[DEBUG] §r" + message;
        if (BasePlugin.isDebug()) {
            prefixed(fullMessage);
            logAsync(DEBUG_LOG, fullMessage);
        } else {
            logAsync(DEBUG_LOG, fullMessage);
        }
    }

    public static void log(String message) {
        logAsync(PLUGIN_LOG, message);
    }

    public static void error(String message) {
        String fullMessage = "§4[ERROR] §r" + message;
        prefixed(fullMessage);
        logAsync(ERROR_LOG, fullMessage);
    }

    public static void error(Throwable throwable) {
        if (throwable == null) {
            error("Unknown error (null throwable).");
            return;
        }

        error("Exception: " + throwable.getClass().getSimpleName() + " - " + throwable.getMessage());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            logAsync(ERROR_LOG, "  at " + ste);
        }
    }

    private static void logAsync(File file, String message) {
        boolean blockingQueue = logQueue.offer(new LogEntry(file, "[" + FORMAT.format(new Date()) + "] " + stripColor(message)));
    }

    private static void writeToFile(File file, String message) {
        try {
            ensureDirectoryExists(file.getParentFile());

            if (!file.exists() && !file.createNewFile()) {
                plain("§c[Logger Error]§r Failed to create log file: " + file.getName());
                return;
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(message + System.lineSeparator());
            }
        } catch (IOException e) {
            plain("§c[Logger Error]§r Failed to write to log file: " + file.getName());
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
            plain("§c[Logger Error]§r Failed to create directory: " + dir.getPath());
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

    private static class LogEntry {
        final File file;
        final String message;

        LogEntry(File file, String message) {
            this.file = file;
            this.message = message;
        }
    }
}
