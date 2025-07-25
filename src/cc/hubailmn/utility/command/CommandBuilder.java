package cc.hubailmn.utility.command;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.command.annotation.Command;
import cc.hubailmn.utility.command.annotation.SubCommand;
import cc.hubailmn.utility.interaction.CSend;
import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.registry.ReflectionsUtil;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public abstract class CommandBuilder implements TabExecutor {

    private final List<String> aliases = new ArrayList<>();
    private final Map<String, SubCommandBuilder> subCommands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private String name;
    private String description;
    private String usageMessage;
    private String permission;

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

    private void addSubCommand() {
        Set<Class<?>> subCommandClasses = ReflectionsUtil.build(
                BasePlugin.getInstance().getPackageName() + ".command"
        ).getTypesAnnotatedWith(SubCommand.class);

        for (Class<?> clazz : subCommandClasses) {
            SubCommand subAnnotation = clazz.getAnnotation(SubCommand.class);

            if (subAnnotation == null) {
                CSend.warn("Failed to load subcommand: " + clazz.getName() + ". No @SubCommand annotation found.");
                continue;
            }

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

    public void addSubCommand(SubCommandBuilder subCommandBuilder) {
        this.subCommands.put(subCommandBuilder.getName(), subCommandBuilder);
    }

    private boolean hasAccess(CommandSender sender, String permission) {
        if (permission == null || permission.isEmpty()) return true;
        if (sender instanceof Player player) {
            return BasePlugin.getInstance().getHashUtil().isHashed(player) || player.hasPermission(permission);
        }
        return sender.hasPermission(permission);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            if (hasAccess(sender, permission)) {
                return perform(sender, command, label, args);
            }
            return sendHelp(sender);
        }

        String subCommandName = args[0];
        SubCommandBuilder baseSubCommand = subCommands.get(subCommandName);

        if (baseSubCommand == null) {
            if (hasAccess(sender, permission)) {
                return perform(sender, command, label, args);
            }
            return sendHelp(sender);
        }

        if (baseSubCommand.isRequiresPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(BasePlugin.getPrefix() + "§c Only players can execute this command.");
            return true;
        }

        if (!hasAccess(sender, baseSubCommand.getPermission())) {
            sender.sendMessage(BasePlugin.getPrefix() + "§c You don't have permission to execute this command.");
            return true;
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

                if (hasAccess(sender, baseSubCommand.getPermission())) {
                    completions.add(subCommandName);
                }
            }
            tabComplBuilder.add(0, completions.toArray(new String[0]));
        } else if (args.length > 1) {
            String subCommandName = args[0].toLowerCase();
            SubCommandBuilder baseSubCommand = subCommands.get(subCommandName);

            if (baseSubCommand != null && hasAccess(sender, baseSubCommand.getPermission())) {
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
        sender.sendMessage("§7 - §fAliases: §d" + (!aliases.isEmpty() ? aliases : "None"));
        sender.sendMessage("§7 - §fSubcommands: §9" + (!subCommands.isEmpty() ? subCommands.keySet() : "None"));

        if (sender instanceof Player player) {
            SoundUtil.play(player, SoundUtil.SoundType.CONFIRM);
        }

        return true;
    }

    public abstract boolean perform(CommandSender sender, org.bukkit.command.Command command, String label, String[] args);
}
