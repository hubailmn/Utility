package me.hubailmn.util.config.file;

import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.LoadConfig;

@LoadConfig(path = "Config.yml")
public class Config extends ConfigBuilder {

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
