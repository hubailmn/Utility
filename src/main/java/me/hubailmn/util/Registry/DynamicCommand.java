package me.hubailmn.util.Registry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class DynamicCommand extends Command {
    private final TabExecutor executor;

    public DynamicCommand(String name, TabExecutor executor) {
        super(name);
        this.executor = executor;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return executor.onCommand(sender, this, label, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return executor.onTabComplete(sender, this, alias, args);
    }
}
