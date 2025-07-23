package cc.hubailmn.jdautility;

import cc.hubailmn.jdautility.register.BotRegister;
import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.ConfigUtil;
import cc.hubailmn.utility.config.file.BotSettingsConfig;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.bukkit.Bukkit;

public class BaseBot extends ListenerAdapter {

    @Getter
    private static BotSettingsConfig config;

    @Getter
    private static ShardManager shardManager;

    public static void init() {
        config = ConfigUtil.getConfig(BotSettingsConfig.class);
        if (!config.isDiscordEnabled()) return;

        String token = config.getBotToken();
        if (token == null || token.isBlank() || token.equals("XXXX.XXXX.XXXX")) {
            CSend.warn("Discord bot token is invalid or not set. Skipping initialization.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            try {
                DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
                builder.setStatus(config.getBotStatus());
                Activity activity;
                if (config.getBotActivityType() == Activity.ActivityType.STREAMING) {
                    activity = Activity.of(config.getBotActivityType(), config.getBotActivityName(), config.getBotActivityUrl());
                } else {
                    activity = Activity.of(config.getBotActivityType(), config.getBotActivityName());
                }

                builder.setActivity(activity);
                shardManager = builder.build();
                CSend.info("Discord bot initialized successfully.");
            } catch (InvalidTokenException e) {
                CSend.error("Invalid Discord bot token provided in config!");
                CSend.error(e);
            } catch (Exception e) {
                CSend.error("Failed to initialize Discord bot!");
                CSend.error(e);
            }

            BotRegister.commands();
            BotRegister.listeners();
            BotRegister.modals();
        });
    }

    public static void shutdown() {
        if (shardManager != null) {
            shardManager.shutdown();
            CSend.info("Discord bot shutdown successfully.");
        }

        shardManager = null;
    }

}
