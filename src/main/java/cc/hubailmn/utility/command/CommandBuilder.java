package cc.hubailmn.utility.command;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.command.annotation.Command;
import cc.hubailmn.utility.command.annotation.SubCommand;
import cc.hubailmn.utility.plugin.CSend;
import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.interaction.player.MessageUtil;
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
    private final List<String> subCommandNames = new ArrayList<>();
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
        this.aliases.removeIf(String::isBlank);

        loadSubcommands();
    }

    private void loadSubcommands() {
        Set<Class<?>> subCommandClasses = ClasspathScanner.getTypesAnnotatedWith(
                SubCommand.class,
                BasePlugin.getInstance().getPackageName() + ".command"
        );

        for (Class<?> clazz : subCommandClasses) {
            SubCommand subAnnotation = clazz.getAnnotation(SubCommand.class);

            if (subAnnotation == null || !subAnnotation.command().equals(this.getClass())) {
                continue;
            }

            try {
                SubCommandBuilder subCommand = (SubCommandBuilder) clazz.getDeclaredConstructor().newInstance();
                this.addSubCommand(subCommand);
            } catch (Exception e) {
                CSend.warn("Failed to load subcommand '" + clazz.getName() + "'. Reason: " + e.getMessage());
            }
        }
    }

    public void addSubCommand(SubCommandBuilder subCommandBuilder) {
        this.subCommandNames.add(subCommandBuilder.getName());
        this.subCommands.put(subCommandBuilder.getName(), subCommandBuilder);
        subCommandBuilder.getAliases().forEach(alias -> this.subCommands.put(alias, subCommandBuilder));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (!PlayerUtil.hasPermission(sender, permission)) {
            deny(sender);
            return true;
        }

        if (args.length < 1) {
            return perform(sender, command, label, args);
        }

        SubCommandBuilder sub = subCommands.get(args[0]);

        if (sub != null) {
            if (!sub.canUse(sender)) {
                deny(sender, "§cYou cannot use this command.");
                return true;
            }

            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return sub.execute(sender, command, label, subArgs);
        }

        return perform(sender, command, label, args);
    }

    private void deny(CommandSender sender) {
        deny(sender, "§cYou don't have permission to execute this command.");
    }

    private void deny(CommandSender sender, String message) {
        MessageUtil.prefixed(sender, message);
        if (sender instanceof Player player) SoundUtil.play(player, SoundUtil.SoundType.DENY);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        TabComplete tab = new TabComplete(sender, command, label, args);

        if (args.length == 1) {
            tab.add(1, subCommandNames.stream()
                    .filter(name -> subCommands.get(name).canUse(sender))
                    .collect(Collectors.toList()));
        } else if (args.length > 1) {
            SubCommandBuilder sub = subCommands.get(args[0]);
            if (sub != null && sub.canUse(sender)) {
                tab.add(args.length, sub.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length)));
            }
        }

        performTabComplete(tab, sender, command, label, args);
        return tab.build();
    }

    protected void performTabComplete(TabComplete builder, CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
    }

    public boolean sendHelp(CommandSender sender) {
        sender.sendMessage("§eCommand Info:");
        sender.sendMessage("§7 - §fName: §b" + name);
        if (!description.isBlank()) sender.sendMessage("§7 - §fDescription: §a" + description);
        if (!usageMessage.isBlank()) sender.sendMessage("§7 - §fUsage: §6" + usageMessage);
        sender.sendMessage("§7 - §fPermission: §c" + (permission != null ? permission : "None"));
        sender.sendMessage("§7 - §fAliases: §d" + (aliases.isEmpty() ? "None" : String.join(", ", aliases)));
        sender.sendMessage("§7 - §fSubcommands: §9" + (subCommandNames.isEmpty() ? "None" : String.join(", ", subCommandNames)));
        if (sender instanceof Player player) SoundUtil.play(player, SoundUtil.SoundType.CONFIRM);
        return true;
    }

    public abstract boolean perform(CommandSender sender, org.bukkit.command.Command command, String label, String[] args);
}