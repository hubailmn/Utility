package cc.hubailmn.util.command;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.Registry.ReflectionsUtil;
import cc.hubailmn.util.command.annotation.Command;
import cc.hubailmn.util.command.annotation.SubCommand;
import cc.hubailmn.util.interaction.SoundPreset;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public abstract class CommandBuilder implements TabExecutor {

    String name;
    String description;
    String usageMessage;
    String permission;
    List<String> aliases = new ArrayList<>();

    private Map<String, SubCommandBuilder> subCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public CommandBuilder() {
        Command annotation = this.getClass().getAnnotation(Command.class);

        if (annotation == null) {
            this.name = this.getClass().getSimpleName().replaceAll("Command$", "").toLowerCase();
            return;
        }

        this.name = annotation.name();
        this.description = annotation.description();
        this.usageMessage = annotation.usage();
        this.permission = annotation.permission();

        this.aliases.addAll(Arrays.asList(annotation.aliases()));
        this.aliases.removeIf(String::isEmpty);

        addSubCommand();
    }

    public void addSubCommand() {
        Set<Class<?>> subCommandClasses = ReflectionsUtil.build(
                BasePlugin.getPackageName() + ".command"
        ).getTypesAnnotatedWith(SubCommand.class);

        for (Class<?> clazz : subCommandClasses) {
            SubCommand subAnnotation = clazz.getAnnotation(SubCommand.class);

            if (subAnnotation.command().equals(this.getClass())) {
                try {
                    SubCommandBuilder baseSubCommand = (SubCommandBuilder) clazz.getDeclaredConstructor().newInstance();
                    this.subCommands.put(baseSubCommand.getName(), baseSubCommand);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load subcommand: " + clazz.getName(), e);
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            if (sender.hasPermission(Objects.toString(getPermission(), "")) || getPermission().isEmpty()) {
                return perform(sender);
            }

            return sendHelp(sender);
        }

        String subCommandName = args[0];
        SubCommandBuilder baseSubCommand = subCommands.get(subCommandName);

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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        TabComplete tabComplBuilder = new TabComplete(sender, command, label, args);

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, SubCommandBuilder> entry : subCommands.entrySet()) {
                String subCommandName = entry.getKey();
                SubCommandBuilder baseSubCommand = entry.getValue();
                if (baseSubCommand.getPermission().isEmpty() || sender.hasPermission(baseSubCommand.getPermission())) {
                    completions.add(subCommandName);
                }
            }
            tabComplBuilder.add(0, completions.toArray(new String[0]));
        } else if (args.length > 1) {
            String subCommandName = args[0].toLowerCase();
            SubCommandBuilder baseSubCommand = subCommands.get(subCommandName);
            if (baseSubCommand != null && (baseSubCommand.getPermission().isEmpty() || sender.hasPermission(baseSubCommand.getPermission()))) {
                return baseSubCommand.onTabComplete(sender, command, label, args);
            }
        }

        return tabComplBuilder.build();
    }

    public boolean sendHelp(CommandSender sender) {
        sender.sendMessage("§eCommand Info:");
        sender.sendMessage("§7 - §fName: §b" + name);

        if (!description.isEmpty()) {
            sender.sendMessage("§7 - §fDescription: §a" + description);
        }

        if (!usageMessage.isEmpty()) {
            sender.sendMessage("§7 - §fUsage: §6" + usageMessage);
        }

        sender.sendMessage("§7 - §fPermission: §c" + (permission != null ? permission : "None"));
        sender.sendMessage("§7 - §fAliases: §d" + (aliases != null && !aliases.isEmpty() ? aliases : "None"));
        sender.sendMessage("§7 - §fSubcommands: §9" + (subCommands != null && !subCommands.isEmpty() ? subCommands.keySet() : "None"));

        if (sender instanceof Player player) {
            SoundPreset.play(player, SoundPreset.SoundType.CONFIRM);
        }

        return true;
    }

    public abstract boolean perform(CommandSender sender);
}
