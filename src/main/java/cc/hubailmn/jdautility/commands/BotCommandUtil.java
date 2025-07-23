package cc.hubailmn.jdautility.commands;

import cc.hubailmn.jdautility.BaseBot;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        for (Guild guild : BaseBot.getShardManager().getGuilds()) {
            guild.updateCommands().addCommands(botCommandsList).queue(
                    success -> CSend.debug("Updated commands for guild: " + guild.getName()),
                    failure -> CSend.error("Failed to update commands for guild: " + guild.getName() + " - " + failure.getMessage())
            );
            count++;
        }

        CSend.info("Updated commands for " + count + " guild(s).");
    }

    public static void updateGuildCommands(Guild guild) {
        guild.updateCommands().addCommands(botCommandsList).queue(
                success -> CSend.debug("Updated commands for guild: " + guild.getName()),
                failure -> CSend.error("Failed to update commands for guild: " + guild + " - " + failure.getMessage())
        );
    }

    public static void updateGlobalCommands() {
        BaseBot.getShardManager().getShards().forEach(jda ->
                jda.updateCommands().addCommands().queue(
                        success -> CSend.debug("Updated global commands on shard: " + jda.getShardInfo().getShardId()),
                        failure -> CSend.error("Failed to update global commands on shard: " + failure.getMessage())
                )
        );
        CSend.info("Triggered global command update across all shards.");
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

        BaseBot.getShardManager().getShards().forEach(jda ->
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

        for (Guild guild : BaseBot.getShardManager().getGuilds()) {
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

    public static void registerAllGuild() {
        updateAllGuildCommands();
    }
}
