package cc.hubailmn.utility.jda;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.config.ConfigUtil;
import cc.hubailmn.utility.config.file.BotSettingsConfig;
import cc.hubailmn.utility.jda.commands.BotCommandUtil;
import cc.hubailmn.utility.jda.register.BotRegister;
import cc.hubailmn.utility.jda.register.InstanceManager;
import cc.hubailmn.utility.plugin.CSend;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@Getter
public class BaseBot extends ListenerAdapter {

    @Getter
    private static BaseBot instance;
    private final InstanceManager instanceManager;
    private ShardManager shardManager;
    private BotSettingsConfig config;

    public BaseBot() {
        instance = this;
        instanceManager = new InstanceManager();
        init();
    }

    public void init() {
        config = ConfigUtil.getConfig(BotSettingsConfig.class);
        if (!config.isDiscordEnabled()) return;

        String token = config.getBotToken();
        if (token == null || token.isBlank() || token.equals("XXXX.XXXX.XXXX")) {
            CSend.warn("Discord bot token is invalid or not set. Skipping initialization.");
            return;
        }

        shutdownAsync().thenRun(() -> {
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

                    shardManager.addEventListener(this);
                    BotRegister.commands();

                    BotCommandUtil.reloadAllCommandsAsync()
                            .thenRun(() -> {
                                BotRegister.listeners();
                                BotRegister.modals();
                                CSend.info("Commands reloaded and listeners/modals registered.");
                            })
                            .exceptionally(ex -> {
                                CSend.error("Failed to reload commands or register listeners/modals: " + ex.getMessage());
                                return null;
                            });

                } catch (InvalidTokenException e) {
                    CSend.error("Invalid Discord bot token provided in config!");
                    CSend.error(e);
                } catch (Exception e) {
                    CSend.error("Failed to initialize Discord bot!");
                    CSend.error(e);
                }
            });
        });
    }

    public CompletableFuture<Void> shutdownAsync() {
        return CompletableFuture.runAsync(() -> {
            if (shardManager != null) {
                try {
                    CompletableFuture<Void> clearGlobal = BotCommandUtil.clearGlobalCommandsAsync();
                    CompletableFuture<Void> clearGuild = BotCommandUtil.clearGuildCommandsAsync();

                    CompletableFuture.allOf(clearGlobal, clearGuild).join();

                    shardManager.shutdown();
                    shardManager = null;

                    CSend.info("Discord bot shutdown successfully.");
                } catch (Exception e) {
                    CSend.error("Error while shutting down Discord bot");
                    CSend.error(e);
                }
            }

            BotCommandUtil.clearCommands();
            getInstanceManager().reload();
            CSend.debug("Discord bot instance manager cleared successfully.");
        });
    }

    public CompletableFuture<Void> reloadCommandsAsync() {
        if (shardManager == null) {
            return CompletableFuture.completedFuture(null);
        }
        return BotCommandUtil.reloadAllCommandsAsync();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent e) {
        Guild guild = e.getGuild();
        BotCommandUtil.register(guild);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent e) {
        Guild guild = e.getGuild();
        BotCommandUtil.register(guild);
    }
}
