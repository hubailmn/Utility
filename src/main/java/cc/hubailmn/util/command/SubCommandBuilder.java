package cc.hubailmn.util.command;

import cc.hubailmn.util.command.annotation.SubCommand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

@Getter
@Setter
public abstract class SubCommandBuilder {

    private String name;
    private String permission;
    private boolean requiresPlayer;

    public SubCommandBuilder() {
        SubCommand annotation = this.getClass().getAnnotation(SubCommand.class);

        if (annotation == null) {
            this.name = this.getClass().getSimpleName().replaceAll("Command$", "").toLowerCase();
            return;
        }

        this.name = annotation.name();
        this.permission = annotation.permission();
        this.requiresPlayer = annotation.requiresPlayer();
    }

    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        TabComplete tabComplBuilder = new TabComplete(sender, command, label, args);
        return tabComplBuilder.build();
    }
}
