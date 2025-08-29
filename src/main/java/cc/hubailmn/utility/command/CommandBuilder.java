package cc.hubailmn.utility.command;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.command.annotation.Command;
import cc.hubailmn.utility.command.annotation.SubCommand;
import cc.hubailmn.utility.interaction.CSend;
import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.interaction.player.PlayerUtil;
import cc.hubailmn.utility.registry.ClasspathScanner;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
        Set<Class<?>> subCommandClasses = ClasspathScanner.getTypesAnnotatedWith(
                SubCommand.class,
                BasePlugin.getInstance().getPackageName() + ".command"
        );

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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            if (PlayerUtil.hasPermission(sender, permission)) {
                return perform(sender, command, label, args);
            }
            sender.sendMessage(BasePlugin.getPrefix() + "§c You don't have permission to execute this command.");
            if (sender instanceof Player player) SoundUtil.play(player, SoundUtil.SoundType.DENY);
            return true;
        }

        String subCommandName = args[0];
        SubCommandBuilder baseSubCommand = subCommands.get(subCommandName);

        if (baseSubCommand == null) {
            if (PlayerUtil.hasPermission(sender, permission)) {
                return perform(sender, command, label, args);
            }
            sender.sendMessage(BasePlugin.getPrefix() + "§c You don't have permission to execute this command.");
            if (sender instanceof Player player) SoundUtil.play(player, SoundUtil.SoundType.DENY);
            return true;
        }

        if (baseSubCommand.isRequiresPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(BasePlugin.getPrefix() + "§c Only players can execute this command.");
            return true;
        }

        if (!PlayerUtil.hasPermission(sender, baseSubCommand.getPermission())) {
            sender.sendMessage(BasePlugin.getPrefix() + "§c You don't have permission to execute this command.");
            if (sender instanceof Player player) SoundUtil.play(player, SoundUtil.SoundType.DENY);
            return true;
        }

        return baseSubCommand.execute(sender, command, label, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        TabComplete tab = new TabComplete(sender, command, label, args);

        if (args.length == 1) {
            List<String> completions = subCommands.entrySet().stream()
                    .filter(entry -> {
                        SubCommandBuilder sub = entry.getValue();
                        return sub.hasPermission(sender) && (!sub.isRequiresPlayer() || sender instanceof Player);
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            tab.add(1, completions);

        } else if (args.length >= 2) {
            String subCommandName = args[0].toLowerCase();
            SubCommandBuilder sub = subCommands.values().stream()
                    .filter(subCmd -> subCmd.matches(subCommandName))
                    .findFirst()
                    .orElse(null);

            if (sub != null && sub.hasPermission(sender) &&
                    (!sub.isRequiresPlayer() || sender instanceof Player)) {

                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return sub.onTabComplete(sender, command, label, subArgs);
            }
        }

        if (sender.hasPermission(permission)) {
            performTabComplete(tab, sender, command, label, args);
        }

        return tab.build();
    }

    protected void performTabComplete(TabComplete builder, CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

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
