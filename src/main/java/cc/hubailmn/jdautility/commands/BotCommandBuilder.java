package cc.hubailmn.jdautility.commands;

import cc.hubailmn.jdautility.BaseBot;
import cc.hubailmn.jdautility.commands.annotation.BotCommand;
import cc.hubailmn.jdautility.register.InstanceManager;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BotCommandBuilder extends ListenerAdapter {

    private final Map<String, BotSubCommandBuilder> subCommands = new HashMap<>();
    private final Map<String, List<String>> autoCompletion = new HashMap<>();
    private String name;
    private String description;
    private SlashCommandData commandData;

    private List<Permission> requiredPermission = new ArrayList<>();

    public BotCommandBuilder() {
        BotCommand annotation = this.getClass().getAnnotation(BotCommand.class);

        if (annotation == null) {
            CSend.error("Failed to load command: " + this.getClass().getSimpleName() + ". Command class must be annotated with @BotCommand.");
            return;
        }

        this.name = annotation.name();
        this.description = annotation.description();
        this.requiredPermission = Arrays.asList(annotation.permission());

        register();
        setPermissions();
        addSubCommands();
    }

    private void register() {
        setCommandData(Commands.slash(getName(), getDescription()));
        addOptions();
    }

    private void setPermissions() {
        if (requiredPermission != null && !requiredPermission.isEmpty()) {
            EnumSet<Permission> perms = EnumSet.copyOf(requiredPermission);
            getCommandData().setDefaultPermissions(DefaultMemberPermissions.enabledFor(perms));
        }
    }

    private void addSubCommands() {
        var subCommandsInstance =  BaseBot.getInstance().getInstanceManager().getAllSubCommands();
        for (BotSubCommandBuilder subCommand : subCommandsInstance) {
            if (subCommand.getParent().equals(this.getClass())) {
                getCommandData().addSubcommands(subCommand.getSubcommandData());
                subCommands.put(subCommand.getName(), subCommand);
            }
        }
    }

    public void execute(SlashCommandInteractionEvent e) {
        if (!subCommands.isEmpty()) {
            String subcommandName = e.getSubcommandName();
            if (subcommandName == null) {
                e.reply("❌ Please specify a valid subcommand.").setEphemeral(true).queue();
                return;
            }

            BotSubCommandBuilder subCommand = subCommands.get(subcommandName);
            if (subCommand == null) {
                e.reply("❌ Unknown subcommand `" + subcommandName + "`").setEphemeral(true).queue();
                return;
            }

            subCommand.handleSubCommand(e);
        } else {
            handleCommand(e);
        }
    }

    public void addOption(OptionType optionType, String optionName, String description, boolean required) {
        getCommandData().addOptions(new OptionData(optionType, optionName, description, required));
    }

    public void addOption(OptionType optionType, String optionName, String description, boolean required, List<String> suggestions) {
        autoCompletion.put(optionName, suggestions);
        getCommandData().addOptions(new OptionData(optionType, optionName, description, required).setAutoComplete(true));
    }

    public void updateSuggestions(String optionName, List<String> suggestions) {
        if (!autoCompletion.containsKey(optionName)) return;
        autoCompletion.put(optionName, suggestions);
        CSend.debug("Updated auto-completion for option '%s' with %d entries.".formatted(optionName, suggestions.size()));
    }

    public void addOptions() {

    }

    public void autoComplete() {

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (!e.getName().equals(getName())) return;

        Member user = e.getMember();

        if (user == null) return;
        if (requiredPermission != null && !requiredPermission.isEmpty()) {
            if (!user.hasPermission(requiredPermission)) {
                e.reply("❌ You don't have permission to use this command.").setEphemeral(true).queue();

                CSend.warn("User " + user.getEffectiveName() + " tried to use " + getName() + " without required permissions.");
                return;
            }
        }

        try {
            execute(e);
        } catch (Exception ex) {
            e.reply("❌ An error occurred while processing the command.").setEphemeral(true).queue();
            CSend.error("Error executing command: " + getName());
            CSend.error(ex);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if (!e.getName().equalsIgnoreCase(getName())) return;

        String subcommand = e.getSubcommandName();

        if (subcommand != null) {
            for (BotSubCommandBuilder sub : getSubCommands().values()) {
                if (subcommand.equalsIgnoreCase(sub.getName())) {
                    sub.handleAutoComplete(e);
                    return;
                }
            }
        }

        if (e.isAcknowledged()) return;
        autoComplete();

        String focused = e.getFocusedOption().getName();
        List<String> choices = autoCompletion.getOrDefault(focused, Collections.emptyList());

        List<Command.Choice> filtered = choices.stream().filter(word -> word.toLowerCase().startsWith(e.getFocusedOption().getValue().toLowerCase())).limit(25).map(word -> new Command.Choice(word, word)).collect(Collectors.toList());

        e.replyChoices(filtered).queue();
    }

    public void logUsage(SlashCommandInteractionEvent e) {
        CSend.debug("Command used: " + getName() + " by " + e.getUser().getName());
    }

    public abstract void handleCommand(SlashCommandInteractionEvent e);
}