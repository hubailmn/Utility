package me.hubailmn.util;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.Registry.Register;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.file.Config;
import me.hubailmn.util.database.DBConnection;
import me.hubailmn.util.interaction.CSend;
import me.hubailmn.util.plugin.CheckUpdates;
import me.hubailmn.util.plugin.License;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;


public abstract class BasePlugin extends JavaPlugin {

    @Getter
    @Setter
    private static String pluginName;

    @Getter
    @Setter
    private static BasePlugin instance;

    @Getter
    @Setter
    private static PluginManager pluginManager;

    @Getter
    @Setter
    private static String prefix;

    @Getter
    @Setter
    private static String pluginVersion;

    @Getter
    @Setter
    private static boolean debug;

    @Getter
    @Setter
    private static boolean forceDebug;

    @Getter
    @Setter
    private static boolean database = true;

    @Getter
    @Setter
    private static boolean license;

    @Getter
    @Setter
    private static boolean checkUpdates;

    @Getter
    @Setter
    private static boolean smirks;


    @Override
    public void onEnable() {
        Configurator.setLevel("me.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);

        getInstance();

        pluginManager = getServer().getPluginManager();
        pluginName = getInstance().getName();
        pluginVersion = getInstance().getDescription().getVersion();

        preEnable();
        init();

        CSend.debug("Plugin has been enabled.");
    }

    @Override
    public void onDisable() {

        if (isDatabase()) {
            CSend.debug("Closing Database Connection...");
            DBConnection.close();
        }

        preDisable();

        CSend.debug("Plugin has been disabled.");
    }

    private void init() {

        CSend.debug("Initializing Config Files...");
        Register.config();

        setPrefix(ConfigUtil.getConfig(Config.class).getPrefix());

        if (!isForceDebug()) {
            setDebug(ConfigUtil.getConfig(Config.class).isDebug());
        } else {
            setDebug(true);
        }

        if (isLicense()) {
            CSend.debug("Checking plugin license...");
            License.checkLicense();
        }

        if (isDatabase()) {
            CSend.debug("Initializing Database Connection...");
            Register.database();
        }

        CSend.debug("Registering Commands...");
        Register.commands();

        CSend.debug("Registering Listeners...");
        Register.eventsListener();

        if (isCheckUpdates()) {
            CSend.info("Checking for updates...");
            CheckUpdates.checkForUpdates();
        }

        if (isSmirks()) {
            CSend.info("Smirking...");
            //TODO: :smirk:
        }

        CSend.debug("Plugin has been initialized.");
    }

    protected void preEnable() {
    }

    protected void preDisable() {
    }

    public static BasePlugin getInstance() {
        if (instance == null) {
            try {
                instance = JavaPlugin.getPlugin(BasePlugin.class);

            } catch (final IllegalStateException ex) {
                if (Bukkit.getPluginManager().getPlugin("PlugMan") != null)
                    Bukkit.getLogger().severe("Failed to get instance of the plugin, if you reloaded using PlugMan you need to do a clean restart instead.");

                throw ex;
            }

            Objects.requireNonNull(instance, "Cannot get a new instance! Have you reloaded?");
        }

        return instance;
    }
}
