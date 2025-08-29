package cc.hubailmn.utility.command;

import cc.hubailmn.utility.command.annotation.SubCommand;
import cc.hubailmn.utility.interaction.player.PlayerUtil;
import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@Data
public abstract class SubCommandBuilder {

    private final String name;
    private final String permission;
    private final boolean requiresPlayer;
    private final List<String> aliases;

    public SubCommandBuilder() {
        SubCommand annotation = this.getClass().getAnnotation(SubCommand.class);

        if (annotation == null) {
            this.name = this.getClass().getSimpleName().replaceAll("Command$", "").toLowerCase();
            this.permission = null;
            this.requiresPlayer = false;
            this.aliases = List.of();
            return;
        }

        this.name = annotation.name().toLowerCase();
        this.permission = annotation.permission().isEmpty() ? null : annotation.permission();
        this.requiresPlayer = annotation.requiresPlayer();
        this.aliases = List.copyOf(
                Arrays.stream(annotation.aliases())
                        .map(String::toLowerCase)
                        .toList()
        );
    }

    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        TabComplete tabCompleteBuilder = new TabComplete(sender, command, label, args);
        performTabComplete(tabCompleteBuilder, sender, command, label, args);
        return tabCompleteBuilder.build();
    }

    protected void performTabComplete(TabComplete builder, CommandSender sender, Command command, String label, String[] args) {
    }

    public boolean matches(String input) {
        String lower = input.toLowerCase();
        return name.equals(lower) || (!aliases.isEmpty() && aliases.contains(lower));
    }

    public boolean hasPermission(CommandSender sender) {
        return permission == null || PlayerUtil.hasPermission(sender, permission);
    }

    public boolean canUse(CommandSender sender) {
        return hasPermission(sender) && (!requiresPlayer || sender instanceof org.bukkit.entity.Player);
    }
}