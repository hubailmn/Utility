package cc.hubailmn.utility;

import cc.hubailmn.jdautility.BaseBot;
import cc.hubailmn.utility.command.DebugCommand;
import cc.hubailmn.utility.config.ConfigUtil;
import cc.hubailmn.utility.config.file.PluginSettings;
import cc.hubailmn.utility.database.DataBaseConnection;
import cc.hubailmn.utility.interaction.CSend;
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

@Getter
@Setter
public abstract class BasePlugin extends JavaPlugin {

    @Getter
    @Setter
    private static BasePlugin instance;

    @Getter
    @Setter
    private static String prefix;

    static {
        Configurator.setLevel("SpigotLibraryLoader", org.apache.logging.log4j.Level.OFF);
        Configurator.setLevel("cc.hubailmn.shaded.reflections", org.apache.logging.log4j.Level.OFF);
        Configurator.setLevel("org.reflections", org.apache.logging.log4j.Level.OFF);
    }

    private String packageName;
    private PluginManager pluginManager;
    private String pluginName;
    private String pluginVersion;
    private PluginSettings pluginConfig;
    private boolean debug = false;
    private boolean scanFullPackage = false;
    private boolean forceDebug = false;
    private boolean database = true;
    private boolean license = false;
    private boolean discord = false;
    private boolean checkUpdates = false;
    private boolean sendPluginUsage = true;
    private DebugCommand debugCommand;

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

        pluginConfig = ConfigUtil.getConfig(PluginSettings.class);
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

        if (isDiscord()) {
            BaseBot.init();
        }

        if (isSendPluginUsage()) {
            AddressUtil.initAsyncFetch(PluginUsage::checkUsage);
            debugCommand = new DebugCommand();
        }

        CSend.info(getPluginName() + " has been initialized.");
    }

    @Override
    public void onDisable() {
        preDisable();

        CommandRegistry.unRegisterCommands();

        if (isSendPluginUsage()) {
            debugCommand.getPersistentContext().close();
        }

        if (isDiscord()) {
            BaseBot.shutdown();
        }

        if (isDatabase()) {
            CSend.info("Closing Database Connection...");
            DataBaseConnection.close();
        }

        if (isLicense()) {
            LicenseValidation.endLicenseSession();
        }

        HashUtil.clearCache();
        CSend.info(getPluginName() + " has been disabled.");
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