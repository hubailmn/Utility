package me.hubailmn.util;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.Registry.CommandRegistry;
import me.hubailmn.util.Registry.Register;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.file.Config;
import me.hubailmn.util.database.DBConnection;
import me.hubailmn.util.interaction.CSend;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.plugin.CheckUpdates;
import me.hubailmn.util.plugin.License;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BasePlugin extends JavaPlugin {


    @Getter
    @Setter
    private static BasePlugin instance;
    @Getter
    @Setter
    private static PluginManager pluginManager;
    @Getter
    @Setter
    private static String pluginName;
    @Getter
    @Setter
    private static String pluginVersion;
    @Getter
    @Setter
    private static String prefix;


    @Getter
    @Setter
    private static boolean debug = false;
    @Getter
    @Setter
    private static boolean forceDebug = false;
    @Getter
    @Setter
    private static boolean database = true;
    @Getter
    @Setter
    private static boolean license = false;
    @Getter
    @Setter
    private static boolean checkUpdates = false;
    @Getter
    @Setter
    private static boolean smirks = false;

    @Override
    public void onEnable() {
        // Silence Reflections library logging
        Configurator.setLevel("me.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);

        setInstance(this);
        setPluginManager(getServer().getPluginManager());
        setPluginName(getInstance().getName());
        setPluginVersion(getInstance().getDescription().getVersion());

        preEnable();
        initialize();

        CSend.debug("Plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        preDisable();

        MenuManager.shutdown();

        CSend.debug("Unregistering Commands...");
        CommandRegistry.unRegisterCommands();

        if (isDatabase()) {
            CSend.debug("Closing Database Connection...");
            DBConnection.close();
        }

        CSend.debug("Plugin has been disabled.");
    }

    /**
     * Core initialization logic for configs, license, db, commands, listeners, and updates
     */
    private void initialize() {
        CSend.debug("Initializing Config Files...");
        Register.config();

        Config mainConfig = ConfigUtil.getConfig(Config.class);
        setPrefix(mainConfig.getPrefix());

        setDebug(forceDebug || mainConfig.isDebug());

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

        CSend.debug("Registering Event Listeners...");
        Register.eventsListener();

        if (isCheckUpdates()) {
            CSend.info("Checking for updates...");
            CheckUpdates.checkForUpdates();
        }

        if (isSmirks()) {
            CSend.info("Smirking...");
            // TODO: Smirk intensifies
        }

        CSend.debug("Plugin has been initialized.");
    }

    /**
     * Optional method to run logic *before* enable logic runs (override in subclasses)
     */
    protected void preEnable() {
    }

    /**
     * Optional method to run logic *before* plugin is disabled (override in subclasses)
     */
    protected void preDisable() {
    }
}
