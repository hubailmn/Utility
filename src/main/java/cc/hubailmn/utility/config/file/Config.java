package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;

@LoadConfig(path = "Settings.yml")
public class Config extends ConfigBuilder {

    private static final String PREFIX = "plugin.";

    public Config() {
        super();
        getConfig().set(PREFIX + "prefix", getPrefix().replace("%plugin_name%", BasePlugin.getPluginName()));
        getConfig().set(PREFIX + "version", BasePlugin.getPluginVersion());
        save();
    }

    public String getPrefix() {
        return getConfig().getString(PREFIX + "prefix");
    }

    public String getConfigVersion() {
        return getConfig().getString(PREFIX + "version");
    }

    public boolean isDebug() {
        return getConfig().getBoolean(PREFIX + "debug");
    }
}
