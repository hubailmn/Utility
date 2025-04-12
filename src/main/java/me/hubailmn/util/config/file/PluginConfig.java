package me.hubailmn.util.config.file;

import me.hubailmn.util.annotation.LoadConfig;
import me.hubailmn.util.config.ConfigBuilder;

@LoadConfig(path = "PluginConfig.yml")
public class PluginConfig extends ConfigBuilder {

    private static final String path = "plugin.";

    public String getPrefix() {
        return getConfig().getString(path + "prefix");
    }

    public boolean isDebug() {
        return getConfig().getBoolean(path + "debug");
    }

    public String getConfigVersion() {
        return getConfig().getString(path + "version");
    }

}
