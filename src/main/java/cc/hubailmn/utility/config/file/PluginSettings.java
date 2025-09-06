package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import cc.hubailmn.utility.plugin.CSend;
import cc.hubailmn.utility.util.CodeGenerator;
import cc.hubailmn.utility.util.TextParserUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.TimeZone;

@LoadConfig(path = "Settings.yml")
@Getter
public class PluginSettings extends ConfigBuilder {

    private static final String PREFIX = "plugin.";

    public Component prefix;
    public String version;
    public String serverId;
    public boolean checkForUpdates;
    public boolean debug;
    public TimeZone timeZone;

    public PluginSettings() {
        super();

        String prefixString = getConfig().getString(PREFIX + "prefix");
        if (prefixString != null) {
            getConfig().set(PREFIX + "prefix", prefixString.replace("%plugin_name%", BasePlugin.getInstance().getPluginName()));
        }

        getConfig().set(PREFIX + "version", BasePlugin.getInstance().getPluginVersion());

        this.serverId = getConfig().getString(PREFIX + "server-id");
        String code = new CodeGenerator(5, 0, 5).generate();
        getConfig().set(PREFIX + "server-id", getServerId().replace("%generate_id%", code));
        save();

        reloadCache();
    }

    public void reloadCache() {
        this.prefix = getComponent(PREFIX + "prefix", TextParserUtil.parse("<gradient:#00CFFF:#0099FF>" + BasePlugin.getInstance().getPluginName() + " </gradient>"));
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
