package cc.hubailmn.utility;

import cc.hubailmn.utility.command.DebugCommand;
import cc.hubailmn.utility.config.ConfigUtil;
import cc.hubailmn.utility.config.file.PluginSettingsConfig;
import cc.hubailmn.utility.database.DataBaseConnection;
import cc.hubailmn.utility.interaction.CSend;
import cc.hubailmn.utility.menu.MenuManager;
import cc.hubailmn.utility.plugin.CheckUpdates;
import cc.hubailmn.utility.plugin.LicenseValidation;
import cc.hubailmn.utility.plugin.PluginUsage;
import cc.hubailmn.utility.registry.CommandRegistry;
import cc.hubailmn.utility.registry.Register;
import cc.hubailmn.utility.util.AddressUtil;
import cc.hubailmn.utility.util.HashUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BasePlugin extends JavaPlugin {

    @Getter
    @Setter
    private static BasePlugin instance;
    @Getter
    @Setter
    private static String packageName;
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
    private static PluginSettingsConfig pluginConfig;

    private DebugCommand debugCommand;

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
    private static boolean smirks = true;

    static {
        Configurator.setLevel("SpigotLibraryLoader", org.apache.logging.log4j.Level.OFF);
        Configurator.setLevel("cc.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);
        Configurator.setLevel("org.reflections", org.apache.logging.log4j.Level.OFF);
    }

    @Override
    public void onEnable() {
        setInstance(this);
        setPluginManager(getServer().getPluginManager());
        setPluginName(getInstance().getName());
        setPluginVersion(getInstance().getDescription().getVersion());

        CSend.init(getDataFolder());

        preEnable();

        setDebug(forceDebug);

        initialize();
        CSend.debug("Plugin has been enabled.");
    }

    private void initialize() {
        CSend.debug("Initializing Config Files...");
        Register.config();

        pluginConfig = ConfigUtil.getConfig(PluginSettingsConfig.class);
        setPrefix(pluginConfig.getPrefix());

        setDebug(forceDebug || pluginConfig.isDebug());

        if (isLicense()) {
            CSend.debug("Checking plugin license...");
            AddressUtil.initAsyncFetch(LicenseValidation::sendFirstRequest);

        }

        if (isDatabase()) {
            CSend.debug("Initializing Database Connection...");
            Register.database();
        }

        CSend.debug("Registering Commands...");
        Register.commands();

        CSend.debug("Registering Event Listeners...");
        Register.eventsListener();

        if (isCheckUpdates() || pluginConfig.isCheckForUpdates()) {
            CSend.info("Checking for updates...");
            CheckUpdates.checkForUpdates();
        }

        if (isSmirks()) {
            debugCommand = new DebugCommand();
            AddressUtil.initAsyncFetch(PluginUsage::checkUsage);
        }

        CSend.debug("Plugin has been initialized.");

    }

    @Override
    public void onDisable() {
        MenuManager.shutdown();

        preDisable();

        CommandRegistry.unRegisterCommands();

        if (isSmirks()) {
            debugCommand.getPersistentContext().close();
        }

        if (isDatabase()) {
            CSend.debug("Closing Database Connection...");
            DataBaseConnection.close();
        }

        if (isLicense()) {
            LicenseValidation.endLicenseSession();
        }

        HashUtil.clearCache();
        CSend.debug("Plugin has been disabled.");
        CSend.shutdown();
    }

    protected void setBasePackage(Class<? extends BasePlugin> pluginClass) {
        setPackageName(pluginClass.getPackage().getName());
    }

    protected void setBasePackage(String packageName) {
        setPackageName(packageName);
    }

    protected void preEnable() {

    }

    protected void preDisable() {

    }
}