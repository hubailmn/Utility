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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


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
    private static boolean database = true;

    @Getter
    @Setter
    private static boolean license;

    @Getter
    @Setter
    private static boolean checkUpdates;


    @Override
    public void onEnable() {
        Configurator.setLevel("me.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);

        instance = this;

        pluginManager = getServer().getPluginManager();
        pluginName = getName();
        pluginVersion = getDescription().getVersion();

        preInit();
        init();

        CSend.debug("Plugin has been enabled.");
    }

    @Override
    public void onDisable() {

        if (isDatabase()) {
            CSend.debug("Closing Database Connection...");
            DBConnection.close();
        }

        CSend.debug("Plugin has been disabled.");
    }

    private void init() {

        CSend.debug("Initializing Config Files...");
        Register.config();

        setPrefix(ConfigUtil.getConfig(Config.class).getPrefix());
        setDebug(ConfigUtil.getConfig(Config.class).isDebug());

        if (isDatabase()) {
            CSend.debug("Initializing Database Connection...");
            Register.database();
        }

        if (isLicense()) {
            CSend.debug("Checking plugin license...");
            License.checkLicense();
        }

        CSend.debug("Registering Commands...");
        Register.commands();

        CSend.debug("Registering Listeners...");
        Register.eventsListener();

        CSend.debug("Plugin has been initialized.");

        if (isCheckUpdates()) {
            CSend.info("Checking for updates...");
            CheckUpdates.checkForUpdates();
        }
    }

    protected void preInit() {
    }
}
