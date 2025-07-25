package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.annotation.IgnoreFile;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

@LoadConfig(
        path = "discord/BotSettings.yml"
)
@IgnoreFile(discord = true)
@Getter
public class BotSettingsConfig extends ConfigBuilder {

    private final String PREFIX = "discord.";
    private final String BOT_PREFIX = PREFIX + "bot.";
    private final String ACTIVITY_PREFIX = BOT_PREFIX + "activity.";

    private boolean discordEnabled;
    private String botToken;
    private OnlineStatus botStatus;
    private Activity.ActivityType botActivityType;
    private String botActivityName;
    private String botActivityUrl;

    public BotSettingsConfig() {
        super();
        reloadCache();
    }

    public void reloadCache() {
        this.discordEnabled = getConfig().getBoolean(PREFIX + "enable", false);
        this.botToken = getConfig().getString(BOT_PREFIX + "token", "XXXX.XXXX.XXXX");

        String statusRaw = getConfig().getString(BOT_PREFIX + "status", "ONLINE").toUpperCase();
        try {
            this.botStatus = OnlineStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException ex) {
            this.botStatus = OnlineStatus.ONLINE;
        }

        String activityTypeRaw = getConfig().getString(ACTIVITY_PREFIX + "type", "WATCHING").toUpperCase();
        try {
            this.botActivityType = Activity.ActivityType.valueOf(activityTypeRaw);
        } catch (IllegalArgumentException ex) {
            this.botActivityType = Activity.ActivityType.WATCHING;
        }

        this.botActivityName = getConfig().getString(ACTIVITY_PREFIX + "name", "Over You!");
        this.botActivityUrl = getConfig().getString(ACTIVITY_PREFIX + "url", "https://twitch.tv/yourchannel");
    }

}
