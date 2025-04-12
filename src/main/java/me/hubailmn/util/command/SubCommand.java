package me.hubailmn.util.command;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class SubCommand {

    private String name;
    private String permission;
    private boolean requiresPlayer;

    public SubCommand(Map<String, SubCommand> subCommands, String name) {
        this(subCommands, name, "", true);
    }

    public SubCommand(Map<String, SubCommand> subCommands, String name, String permission) {
        this(subCommands, name, permission, true);
    }

    public SubCommand(Map<String, SubCommand> subCommands, String name, boolean requiresPlayer) {
        this(subCommands, name, "", requiresPlayer);
    }

    public SubCommand(Map<String, SubCommand> subCommands, String name, String permission, boolean requiresPlayer) {
        this.name = name;
        this.permission = permission;
        this.requiresPlayer = requiresPlayer;
        subCommands.put(getName(), this);
    }

    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        TabComplete tabComplBuilder = new TabComplete(sender, command, label, args);
        return tabComplBuilder.build();
    }
}
