package me.hubailmn.util.command;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.RegisterCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.util.*;

@Getter
@Setter
public abstract class BaseCommand implements TabExecutor {

    String name;
    String description;
    String usageMessage;
    String permission;

    private Map<String, BaseSubCommand> subCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public BaseCommand() {
        RegisterCommand annotation = this.getClass().getAnnotation(RegisterCommand.class);
        this.name = annotation.name();
        this.description = annotation.description();
        this.usageMessage = annotation.usage();
        this.permission = annotation.permission();



        addSubCommand();
    }

    public void addSubCommand() {
        Reflections reflections = new Reflections(
                "me.hubailmn." + BasePlugin.getPluginName().toLowerCase() + ".command",
                "me.hubailmn." + BasePlugin.getPluginName().toLowerCase() + ".subcommand"
        );

        Set<Class<?>> subCommandClasses = reflections.getTypesAnnotatedWith(me.hubailmn.util.annotation.SubCommand.class);

        for (Class<?> clazz : subCommandClasses) {
            me.hubailmn.util.annotation.SubCommand subAnnotation = clazz.getAnnotation(me.hubailmn.util.annotation.SubCommand.class);

            if (subAnnotation.baseCommand().equals(this.getClass())) {
                try {
                    BaseSubCommand baseSubCommand = (BaseSubCommand) clazz.getDeclaredConstructor().newInstance();
                    this.subCommands.put(baseSubCommand.getName(), baseSubCommand);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load subcommand: " + clazz.getName(), e);
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            if (sender.hasPermission(getPermission()) || getPermission().isEmpty()) {
                return preform(sender);
            }
            return sendHelp(sender);
        }

        String subCommandName = args[0];
        BaseSubCommand baseSubCommand = subCommands.get(subCommandName);

        if (baseSubCommand == null) {
            return sendHelp(sender);
        }

        if (baseSubCommand.isRequiresPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(BasePlugin.getPrefix() + "§c Only players can execute this command.");
            return true;
        }

        if (!baseSubCommand.getPermission().isEmpty()) {
            if (!sender.hasPermission(baseSubCommand.getPermission())) {
                sender.sendMessage(BasePlugin.getPrefix() + "§c You don't have permission to execute this command.");
                return true;
            }
        }

        return baseSubCommand.execute(sender, command, label, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        TabComplete tabComplBuilder = new TabComplete(sender, command, label, args);

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, BaseSubCommand> entry : subCommands.entrySet()) {
                String subCommandName = entry.getKey();
                BaseSubCommand baseSubCommand = entry.getValue();
                if (baseSubCommand.getPermission().isEmpty() || sender.hasPermission(baseSubCommand.getPermission())) {
                    completions.add(subCommandName);
                }
            }
            tabComplBuilder.add(0, completions.toArray(new String[0]));
        } else if (args.length > 1) {
            String subCommandName = args[0].toLowerCase();
            BaseSubCommand baseSubCommand = subCommands.get(subCommandName);
            if (baseSubCommand != null && (baseSubCommand.getPermission().isEmpty() || sender.hasPermission(baseSubCommand.getPermission()))) {
                return baseSubCommand.onTabComplete(sender, command, label, args);
            }
        }

        return tabComplBuilder.build();
    }

    public abstract boolean sendHelp(CommandSender sender);

    public abstract boolean preform(CommandSender sender);
}
