package cc.hubailmn.jdautility.commands;

import cc.hubailmn.jdautility.BaseBot;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BotCommandUtil {

    @Getter
    private final static Set<CommandData> botCommandsList = Collections.synchronizedSet(new HashSet<>());

    public static void addCommand(CommandData commandData) {
        botCommandsList.add(commandData);
    }

    public static void removeCommand(CommandData commandData) {
        botCommandsList.remove(commandData);
    }

    public static void clearCommands() {
        botCommandsList.clear();
    }

    public static void updateAllGuildCommands() {
        int count = 0;
        for (Guild guild : BaseBot.getInstance().getShardManager().getGuilds()) {
            guild.updateCommands().addCommands(botCommandsList).queue(
                    success -> CSend.debug("Updated commands for guild: " + guild.getName()),
                    failure -> CSend.error("Failed to update commands for guild: " + guild.getName() + " - " + failure.getMessage())
            );
            count++;
        }
        CSend.info("Updated commands for " + count + " guild(s).");
    }

    public static CompletableFuture<Void> updateAllGuildCommandsAsync() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Guild guild : BaseBot.getInstance().getShardManager().getGuilds()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            guild.updateCommands().addCommands(botCommandsList).queue(
                    success -> {
                        CSend.debug("Updated commands for guild: " + guild.getName());
                        future.complete(null);
                    },
                    failure -> {
                        CSend.error("Failed to update commands for guild: " + guild.getName() + " - " + failure.getMessage());
                        future.complete(null);
                    }
            );
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> CSend.info("Updated commands for all guilds."));
    }

    public static void updateGuildCommands(Guild guild) {
        guild.updateCommands().addCommands(botCommandsList).queue(
                success -> CSend.debug("Updated commands for guild: " + guild.getName()),
                failure -> CSend.error("Failed to update commands for guild: " + guild + " - " + failure.getMessage())
        );
    }

    public static void updateGlobalCommands() {
        BaseBot.getInstance().getShardManager().getShards().forEach(jda ->
                jda.updateCommands().addCommands().queue(
                        success -> CSend.debug("Updated global commands on shard: " + jda.getShardInfo().getShardId()),
                        failure -> CSend.error("Failed to update global commands on shard: " + failure.getMessage())
                )
        );
        CSend.info("Triggered global command update across all shards.");
    }

    public static CompletableFuture<Void> updateGlobalCommandsAsync() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        BaseBot.getInstance().getShardManager().getShards().forEach(jda -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            jda.updateCommands().addCommands(botCommandsList).queue(
                    success -> {
                        CSend.debug("Updated global commands on shard: " + jda.getShardInfo().getShardId());
                        future.complete(null);
                    },
                    failure -> {
                        CSend.error("Failed to update global commands on shard: " + failure.getMessage());
                        future.complete(null);
                    }
            );
            futures.add(future);
        });

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> CSend.info("Triggered global command update across all shards."));
    }

    public static void register(Guild guild) {
        if (guild == null) {
            CSend.warn("Tried to register commands for null guild.");
            return;
        }

        guild.updateCommands().addCommands(botCommandsList).queue(
                success -> CSend.debug("Registered commands for guild: " + guild.getName()),
                failure -> CSend.error("Failed to register commands for guild: " + guild.getName() + " - " + failure.getMessage())
        );
    }

    public static void clearGlobalCommands() {
        Set<String> botCommandNames = botCommandsList.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        BaseBot.getInstance().getShardManager().getShards().forEach(jda ->
                jda.retrieveCommands().queue(commands -> {
                    for (var command : commands) {
                        if (botCommandNames.contains(command.getName())) {
                            jda.deleteCommandById(command.getId()).queue(
                                    success -> CSend.debug("Deleted global command: " + command.getName()),
                                    failure -> CSend.error("Failed to delete global command: " + failure.getMessage())
                            );
                        }
                    }
                })
        );
        CSend.info("Triggered global command clearing across all shards.");
    }

    public static void clearGuildCommands() {
        Set<String> botCommandNames = botCommandsList.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        for (Guild guild : BaseBot.getInstance().getShardManager().getGuilds()) {
            guild.retrieveCommands().queue(commands -> {
                for (var command : commands) {
                    if (botCommandNames.contains(command.getName())) {
                        guild.deleteCommandById(command.getId()).queue(
                                success -> CSend.debug("Cleared command from guild: " + guild.getName() + " - " + command.getName()),
                                failure -> CSend.error("Failed to delete command from guild: " + guild.getName() + " - " + failure.getMessage())
                        );
                    }
                }
            });
        }
    }

    public static CompletableFuture<Void> clearGlobalCommandsAsync() {
        Set<String> botCommandNames = botCommandsList.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        List<CompletableFuture<Void>> shardFutures = new ArrayList<>();

        BaseBot.getInstance().getShardManager().getShards().forEach(jda -> {
            CompletableFuture<Void> shardFuture = new CompletableFuture<>();

            jda.retrieveCommands().queue(commands -> {
                List<CompletableFuture<Void>> deleteFutures = new ArrayList<>();

                for (var command : commands) {
                    if (botCommandNames.contains(command.getName())) {
                        CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
                        jda.deleteCommandById(command.getId()).queue(
                                success -> {
                                    CSend.debug("Deleted global command: " + command.getName());
                                    deleteFuture.complete(null);
                                },
                                failure -> {
                                    CSend.error("Failed to delete global command: " + failure.getMessage());
                                    deleteFuture.complete(null);
                                }
                        );
                        deleteFutures.add(deleteFuture);
                    }
                }

                CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> shardFuture.complete(null))
                        .exceptionally(ex -> {
                            shardFuture.completeExceptionally(ex);
                            return null;
                        });
            }, failure -> {
                CSend.error("Failed to retrieve global commands: " + failure.getMessage());
                shardFuture.completeExceptionally(failure);
            });

            shardFutures.add(shardFuture);
        });

        return CompletableFuture.allOf(shardFutures.toArray(new CompletableFuture[0]));
    }

    public static CompletableFuture<Void> clearGuildCommandsAsync() {
        Set<String> botCommandNames = botCommandsList.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        List<CompletableFuture<Void>> guildFutures = new ArrayList<>();

        for (Guild guild : BaseBot.getInstance().getShardManager().getGuilds()) {
            CompletableFuture<Void> guildFuture = new CompletableFuture<>();

            guild.retrieveCommands().queue(commands -> {
                List<CompletableFuture<Void>> deleteFutures = new ArrayList<>();

                for (var command : commands) {
                    if (botCommandNames.contains(command.getName())) {
                        CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
                        guild.deleteCommandById(command.getId()).queue(
                                success -> {
                                    CSend.debug("Cleared command from guild: " + guild.getName() + " - " + command.getName());
                                    deleteFuture.complete(null);
                                },
                                failure -> {
                                    CSend.error("Failed to delete command from guild: " + guild.getName() + " - " + failure.getMessage());
                                    deleteFuture.complete(null);
                                }
                        );
                        deleteFutures.add(deleteFuture);
                    }
                }

                CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> guildFuture.complete(null))
                        .exceptionally(ex -> {
                            guildFuture.completeExceptionally(ex);
                            return null;
                        });
            }, failure -> {
                CSend.error("Failed to retrieve commands for guild: " + guild.getName() + " - " + failure.getMessage());
                guildFuture.completeExceptionally(failure);
            });

            guildFutures.add(guildFuture);
        }

        return CompletableFuture.allOf(guildFutures.toArray(new CompletableFuture[0]));
    }

    public static CompletableFuture<Void> reloadAllCommandsAsync() {
        return clearGlobalCommandsAsync()
                .thenCompose(v -> clearGuildCommandsAsync())
                .thenCompose(v -> updateGlobalCommandsAsync())
                .thenCompose(v -> updateAllGuildCommandsAsync())
                .thenRun(() -> CSend.info("Reloaded all commands successfully."));
    }

    public static void registerAllGuild() {
        updateAllGuildCommands();
    }
}
