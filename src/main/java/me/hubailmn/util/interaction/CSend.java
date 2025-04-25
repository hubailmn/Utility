package me.hubailmn.util.interaction;

import me.hubailmn.util.BasePlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CSend {

    private static final File DEBUG_LOG = new File(BasePlugin.getInstance().getDataFolder(), "debug/debug.log");
    private static final File ERROR_LOG = new File(BasePlugin.getInstance().getDataFolder(), "debug/error.log");
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CSend() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    private static String getPrefix() {
        return BasePlugin.getPrefix() != null ? BasePlugin.getPrefix() : "";
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
            logToFile(ERROR_LOG, "  at " + ste.toString());
        }
    }

    private static void logToFile(File file, String message) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write("[" + FORMAT.format(new Date()) + "] " + message + System.lineSeparator());
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§c[Logger Error] Failed to write to log file: " + file.getName());
        }
    }
}
