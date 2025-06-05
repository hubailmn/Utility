package cc.hubailmn.util.interaction;

import cc.hubailmn.util.BasePlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CSend {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File DEBUG_LOG;
    private static File ERROR_LOG;

    private CSend() {
        throw new UnsupportedOperationException("CSend is a utility class and should not be instantiated directly. Use the static init method.");
    }

    public static void init(File dataFolder) {
        DEBUG_LOG = new File(dataFolder, "debug/debug.log");
        ERROR_LOG = new File(dataFolder, "debug/error.log");

        if (!DEBUG_LOG.getParentFile().exists()) {
            DEBUG_LOG.getParentFile().mkdirs();
        }
        if (!ERROR_LOG.getParentFile().exists()) {
            ERROR_LOG.getParentFile().mkdirs();
        }

        rotateLogs(dataFolder);
    }

    private static String getPrefix() {

        if (BasePlugin.getInstance() == null || BasePlugin.getPluginName() == null) {
            return "§7[§aPlugin§7] §b>>§r";
        }

        String prefix = BasePlugin.getPrefix();
        return (prefix == null || prefix.isEmpty()) ? "§7[§a" + BasePlugin.getPluginName() + "§7] §b>>§r" : prefix;
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

    public static void error(String message) {
        String fullMessage = "[ERROR] " + message;
        prefixed("§4" + fullMessage);
        logToFile(ERROR_LOG, fullMessage);
    }

    public static void debug(String message) {
        if (BasePlugin.isDebug()) {
            String fullMessage = "[DEBUG] " + message;
            prefixed("§b" + fullMessage);
            logToFile(DEBUG_LOG, fullMessage);
        }
    }

    public static void error(Throwable throwable) {
        if (throwable == null) {
            error("Unknown error (null throwable).");
            return;
        }

        error("Exception: " + throwable.getMessage());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            debug("  at " + ste.toString());
            logToFile(ERROR_LOG, "  at " + ste);
        }
    }

    private static void logToFile(File file, String message) {
        if (file == null) {
            Bukkit.getConsoleSender().sendMessage("§c[Logger Error] Log file path is null. Cannot write log.");
            return;
        }

        try {

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write("[" + FORMAT.format(new Date()) + "] " + message + System.lineSeparator());
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§c[Logger Error] Failed to write to log file: " + file.getName());
            e.printStackTrace();
        }
    }

    private static void rotateLogs(File dataFolder) {

        File debugLogDir = new File(dataFolder, "debug");

        if (!debugLogDir.exists()) {
            debugLogDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = sdf.format(new Date());

        File currentDebugLog = new File(debugLogDir, "debug.log");
        if (currentDebugLog.exists()) {
            File rotatedDebugLog = new File(debugLogDir, "debug_" + timestamp + ".log");

            if (!currentDebugLog.renameTo(rotatedDebugLog)) {

                if (BasePlugin.getInstance() != null) {
                    BasePlugin.getInstance().getLogger().warning("Failed to rotate debug.log to " + rotatedDebugLog.getName());
                } else {
                    Bukkit.getLogger().warning("Failed to rotate debug.log to " + rotatedDebugLog.getName());
                }
            } else {
                if (BasePlugin.getInstance() != null) {
                    BasePlugin.getInstance().getLogger().info("Rotated debug.log to " + rotatedDebugLog.getName());
                } else {
                    Bukkit.getLogger().info("Rotated debug.log to " + rotatedDebugLog.getName());
                }
            }
        }

        File currentErrorLog = new File(debugLogDir, "error.log");
        if (currentErrorLog.exists()) {
            File rotatedErrorLog = new File(debugLogDir, "error_" + timestamp + ".log");
            if (!currentErrorLog.renameTo(rotatedErrorLog)) {
                if (BasePlugin.getInstance() != null) {
                    BasePlugin.getInstance().getLogger().warning("Failed to rotate error.log to " + rotatedErrorLog.getName());
                } else {
                    Bukkit.getLogger().warning("Failed to rotate error.log to " + rotatedErrorLog.getName());
                }
            } else {
                if (BasePlugin.getInstance() != null) {
                    BasePlugin.getInstance().getLogger().info("Rotated error.log to " + rotatedErrorLog.getName());
                } else {
                    Bukkit.getLogger().info("Rotated error.log to " + rotatedErrorLog.getName());
                }
            }
        }
    }
}