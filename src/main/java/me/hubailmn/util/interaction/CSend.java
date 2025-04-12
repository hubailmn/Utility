package me.hubailmn.util.interaction;

import me.hubailmn.util.BasePlugin;
import org.bukkit.Bukkit;

public final class CSend {

    private static final String PREFIX = BasePlugin.getPrefix();


    private CSend() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void plain(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static void prefixed(String message) {
        plain(PREFIX + " " + message);
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
}
