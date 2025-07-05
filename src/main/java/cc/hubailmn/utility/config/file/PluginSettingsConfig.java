package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import lombok.Getter;

@LoadConfig(path = "Settings.yml")
@Getter
public class PluginSettingsConfig extends ConfigBuilder {

    private static final String PREFIX = "plugin.";

    private String prefix;
    private String version;
    private boolean checkForUpdates;
    private boolean debug;

    public PluginSettingsConfig() {
        super();
        getConfig().set(PREFIX + "prefix", getPrefix().replace("%plugin_name%", BasePlugin.getPluginName()));
        getConfig().set(PREFIX + "version", BasePlugin.getPluginVersion());
        save();

        load();
    }

    public void load() {
        prefix = getConfig().getString(PREFIX + "prefix");
        version = getConfig().getString(PREFIX + "version");
        checkForUpdates = getConfig().getBoolean(PREFIX + "check-updates");
        debug = getConfig().getBoolean(PREFIX + "debug");
    }

}
