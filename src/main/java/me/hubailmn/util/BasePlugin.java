package me.hubailmn.util;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.Registry.Register;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.file.PluginConfig;
import me.hubailmn.util.database.DBConnection;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public abstract class BasePlugin extends JavaPlugin {

    @Getter
    @Setter
    private static String pluginName;

    @Getter
    @Setter
    private static JavaPlugin instance;

    @Getter
    @Setter
    private static PluginManager pluginManager;

    @Getter
    @Setter
    private static String prefix;

    @Getter
    @Setter
    private static boolean debug;

    @Getter
    @Setter
    private static String pluginVersion;

    @Override
    public void onEnable() {
        Configurator.setLevel("me.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);

        instance = this;

        pluginManager = getServer().getPluginManager();
        pluginName = getName();
        pluginVersion = getDescription().getVersion();

        init();

    }

    @Override
    public void onDisable() {
        DBConnection.close();

    }

    private void init() {
        Register.config();

        setPrefix(ConfigUtil.getConfig(PluginConfig.class).getPrefix());
        setDebug(ConfigUtil.getConfig(PluginConfig.class).isDebug());

        Register.database();
        Register.commands();
        Register.eventsListener();

    }
}
