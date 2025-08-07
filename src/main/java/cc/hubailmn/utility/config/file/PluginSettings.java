package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import cc.hubailmn.utility.interaction.CSend;
import cc.hubailmn.utility.util.CodeGenerator;
import lombok.Getter;

import java.util.Arrays;
import java.util.TimeZone;

@LoadConfig(path = "Settings.yml")
@Getter
public class PluginSettings extends ConfigBuilder {

    private static final String PREFIX = "plugin.";

    public String prefix;
    public String version;
    public String serverId;
    public boolean checkForUpdates;
    public boolean debug;
    public TimeZone timeZone;

    public PluginSettings() {
        super();

        this.prefix = getConfig().getString(PREFIX + "prefix");
        getConfig().set(PREFIX + "prefix", getPrefix().replace("%plugin_name%", BasePlugin.getInstance().getPluginName()));
        getConfig().set(PREFIX + "version", BasePlugin.getInstance().getPluginVersion());

        this.serverId = getConfig().getString(PREFIX + "server-id");
        String code = new CodeGenerator(5, 0, 5).generate();
        getConfig().set(PREFIX + "server-id", getServerId().replace("%generate_id%", code));
        save();

        reloadCache();
    }

    public void reloadCache() {
        this.prefix = getConfig().getString(PREFIX + "prefix");
        this.version = getConfig().getString(PREFIX + "version");
        this.serverId = getConfig().getString(PREFIX + "server-id");
        this.checkForUpdates = getConfig().getBoolean(PREFIX + "update-check");
        this.debug = getConfig().getBoolean(PREFIX + "debug");
        this.timeZone = getTimeZone();
    }

    public TimeZone getTimeZone() {
        String id = getConfig().getString(PREFIX + "timezone", "Asia/Riyadh");

        if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(id)) {
            CSend.warn("Invalid timezone ID '" + id + "' in config. Falling back to Asia/Riyadh.");
            id = "Asia/Riyadh";
        }

        return TimeZone.getTimeZone(id);
    }

}
