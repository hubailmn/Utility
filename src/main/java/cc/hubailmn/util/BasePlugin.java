package cc.hubailmn.util;

import lombok.Getter;
import lombok.Setter;
import cc.hubailmn.util.Registry.CommandRegistry;
import cc.hubailmn.util.Registry.Register;
import cc.hubailmn.util.config.ConfigUtil;
import cc.hubailmn.util.config.file.Config;
import cc.hubailmn.util.database.DataBaseConnection;
import cc.hubailmn.util.interaction.CSend;
import cc.hubailmn.util.menu.MenuManager;
import cc.hubailmn.util.plugin.CheckUpdates;
import cc.hubailmn.util.plugin.License;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        Configurator.setLevel("me.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);

        rotateLogs();

        setInstance(this);
        setPluginManager(getServer().getPluginManager());
        setPluginName(getInstance().getName());
        setPluginVersion(getInstance().getDescription().getVersion());

        preEnable();

        setDebug(forceDebug);

        initialize();

        CSend.debug("Plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        preDisable();

        MenuManager.shutdown();

        CommandRegistry.unRegisterCommands();

        if (isDatabase()) {
            CSend.debug("Closing Database Connection...");
            DataBaseConnection.close();
        }

        if (isLicense()) {
            License.endLicenseSession();
        }

        CSend.debug("Plugin has been disabled.");
    }

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
            // TODO: not implemented yet!
        }

        CSend.debug("Plugin has been initialized.");
    }

    private void rotateLogs() {
        File dataFolder = getDataFolder();
        File debugLog = new File(dataFolder, "debug.log");
        File errorLog = new File(dataFolder, "error.log");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = sdf.format(new Date());

        if (debugLog.exists()) {
            debugLog.renameTo(new File(dataFolder, "debug_" + timestamp + ".log"));
        }

        if (errorLog.exists()) {
            errorLog.renameTo(new File(dataFolder, "error_" + timestamp + ".log"));
        }
    }

    protected void preEnable() {
    }

    protected void preDisable() {
    }
}
