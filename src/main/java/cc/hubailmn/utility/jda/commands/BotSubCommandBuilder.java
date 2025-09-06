package cc.hubailmn.utility.jda.commands;

import cc.hubailmn.utility.jda.commands.annotation.BotCommand;
import cc.hubailmn.utility.jda.commands.annotation.BotSubCommand;
import cc.hubailmn.utility.plugin.CSend;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BotSubCommandBuilder extends ListenerAdapter {

    private final Map<String, List<String>> autoCompletion = new HashMap<>();
    private String name;
    private String description;
    private SubcommandData subcommandData;
    private Class<?> parent;
    private List<Permission> requiredPermission = new ArrayList<>();

    public BotSubCommandBuilder() {
        BotSubCommand annotation = this.getClass().getAnnotation(BotSubCommand.class);

        if (annotation == null) {
            CSend.error("Failed to load subcommand: " + this.getClass().getSimpleName() +
                    ". Subcommand class must be annotated with @BotSubCommand.");
            return;
        }

        this.name = annotation.name();
        this.description = annotation.description();
        this.requiredPermission = Arrays.asList(annotation.permission());
        this.parent = annotation.parent();

        register();
    }

    private void register() {
        this.subcommandData = new SubcommandData(name, description);
        addOptions();
    }

    public void addOption(OptionType optionType, String optionName, String description, boolean required) {
        subcommandData.addOptions(new OptionData(optionType, optionName, description, required));
    }

    public void addOption(OptionType optionType, String optionName, String description, boolean required, List<String> suggestions) {
        autoCompletion.put(optionName, suggestions);
        subcommandData.addOptions(new OptionData(optionType, optionName, description, required).setAutoComplete(true));
    }

    public void updateSuggestions(String optionName, List<String> suggestions) {
        boolean optionExists = subcommandData.getOptions().stream().anyMatch(opt -> opt.getName().equals(optionName));
        if (!optionExists) {
            CSend.warn("Tried to update auto-completion for nonexistent option: " + optionName);
            return;
        }

        autoCompletion.put(optionName, suggestions);
    }

    public void addOptions() {
    }

    public void autoComplete() {
    }

    public void handleSubCommand(@NotNull SlashCommandInteractionEvent e) {
        BotCommand annotation = parent.getAnnotation(BotCommand.class);
        if (annotation == null) {
            CSend.error("There was an error on subcommand " + getName() + ". Parent command must be annotated with @BotCommand.");
            return;
        }

        String subcommandName = e.getSubcommandName();
        if (subcommandName == null) {
            CSend.error("There was an error on subcommand " + getName() + ". Subcommand name must be specified.");
            return;
        }

        if (!e.getName().equalsIgnoreCase(annotation.name())) return;
        if (!e.getSubcommandName().equalsIgnoreCase(getName())) return;

        Member user = e.getMember();
        if (user == null) return;

        if (!requiredPermission.isEmpty() && !user.hasPermission(requiredPermission)) {
            e.reply("❌ You don't have permission to use this subcommand.").setEphemeral(true).queue();
            CSend.warn("User " + user.getEffectiveName() + " tried to use subcommand " + getName() + " without required permissions.");
            return;
        }

        try {
            execute(e);
        } catch (Exception ex) {
            e.reply("❌ An error occurred while processing the command.").setEphemeral(true).queue();
            CSend.error("Error executing command: " + getName());
            CSend.error(ex);
        }
    }

    public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent e) {
        BotCommand annotation = parent.getAnnotation(BotCommand.class);

        if (annotation == null) {
            CSend.error("There was an error on subcommand " + getName() + ". Parent command must be annotated with @BotCommand.");
            return;
        }

        String subcommandName = e.getSubcommandName();
        if (subcommandName == null) {
            CSend.error("There was an error on subcommand " + getName() + ". Subcommand name must be specified.");
            return;
        }

        if (!e.getName().equalsIgnoreCase(annotation.name())) return;
        if (!e.getSubcommandName().equalsIgnoreCase(getName())) return;
        if (e.isAcknowledged()) return;

        autoComplete();

        String focused = e.getFocusedOption().getName();
        List<String> choices = autoCompletion.getOrDefault(focused, Collections.emptyList());

        List<Command.Choice> filtered = choices.stream()
                .filter(word -> word.toLowerCase().startsWith(e.getFocusedOption().getValue().toLowerCase()))
                .limit(25)
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());

        e.replyChoices(filtered).queue();
    }

    public abstract void execute(SlashCommandInteractionEvent e);
}
