package me.hubailmn.util.command;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public abstract class BaseCommand implements TabExecutor {

    String name;
    String description;
    String usageMessage;
    String permission;

    private Map<String, SubCommand> subCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public BaseCommand(String name) {
        this(name, "", "/" + name);
    }

    public BaseCommand(String name, String description, String usageMessage) {
        this.name = name;
        this.description = description;
        this.usageMessage = usageMessage;

        PluginCommand command = BasePlugin.getInstance().getCommand(getName());
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    public void addSubCommand(SubCommand... subCommands) {
        for (SubCommand subCommand : subCommands) {
            this.subCommands.put(subCommand.getName(), subCommand);
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
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            return sendHelp(sender);
        }

        if (subCommand.isRequiresPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(BasePlugin.getPrefix() + "§c Only players can execute this command.");
            return true;
        }

        if (!subCommand.getPermission().isEmpty()) {
            if (!sender.hasPermission(subCommand.getPermission())) {
                sender.sendMessage(BasePlugin.getPrefix() + "§c You don't have permission to execute this command.");
                return true;
            }
        }

        return subCommand.execute(sender, command, label, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        TabComplete tabComplBuilder = new TabComplete(sender, command, label, args);

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String subCommandName = entry.getKey();
                SubCommand subCommand = entry.getValue();
                if (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())) {
                    completions.add(subCommandName);
                }
            }
            tabComplBuilder.add(0, completions.toArray(new String[0]));
        } else if (args.length > 1) {
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand != null && (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission()))) {
                return subCommand.onTabComplete(sender, command, label, args);
            }
        }

        return tabComplBuilder.build();
    }

    public abstract boolean sendHelp(CommandSender sender);

    public abstract boolean preform(CommandSender sender);
}
