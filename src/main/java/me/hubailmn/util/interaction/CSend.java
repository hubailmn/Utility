package me.hubailmn.util.interaction;

import me.hubailmn.util.BasePlugin;
import org.bukkit.Bukkit;

public final class CSend {

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
        prefixed("§4[ERROR] §r" + message);
    }

    public static void debug(String message) {
        if (BasePlugin.isDebug()) {
            prefixed("§b[DEBUG] §r" + message);
        }
    }

    public static void error(Throwable throwable) {
        if (throwable == null) {
            CSend.error("Unknown error (null throwable).");
            return;
        }

        CSend.error("Exception: " + throwable.getMessage());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            CSend.debug("  at " + ste.toString());
        }
    }

}
