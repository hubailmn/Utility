package me.hubailmn.util.config.file;

import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.LoadConfig;

@LoadConfig(path = "Config.yml")
public class Config extends ConfigBuilder {

    private static final String PREFIX = "plugin.";

    public String getPrefix() {
        return getConfig().getString(PREFIX + "prefix");
    }

    public boolean isDebug() {
        return getConfig().getBoolean(PREFIX + "debug");
    }

    public String getConfigVersion() {
        return getConfig().getString(PREFIX + "version");
    }

}
