package cc.hubailmn.utility.registry;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

public class CommandRegistry {

    private static final Set<String> registeredCommands = new HashSet<>();
    private static final CommandMap commandMap;
    private static final Map<String, Command> knownCommands;

    static {
        try {
            commandMap = getCommandMap();
            knownCommands = getKnownCommands((SimpleCommandMap) commandMap);
        } catch (Exception e) {
            CSend.error("Failed to initialize CommandRegistry: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void registerCommand(String commandName, TabExecutor executor, List<String> aliases) {
        try {
            String key = commandName.toLowerCase();
            String pluginPrefix = BasePlugin.getInstance().getPluginName().toLowerCase();
            String namespacedKey = pluginPrefix + ":" + key;

            if (registeredCommands.contains(commandName)) {
                Command existingCommand = knownCommands.get(key);
                Command namespacedCommand = knownCommands.get(namespacedKey);

                if (existingCommand == null && namespacedCommand == null) {
                    CSend.debug("Command '" + commandName + "' was tracked as registered but not found in knownCommands. Removing from tracking.");
                    registeredCommands.remove(commandName);
                } else {
                    CSend.debug("Command '" + commandName + "' already registered. Skipping.");
                    return;
                }
            }

            DynamicCommand dynamicCommand = new DynamicCommand(commandName, executor, aliases);
            commandMap.register(BasePlugin.getInstance().getPluginName(), dynamicCommand);
            registeredCommands.add(commandName);
            CSend.debug("Registered command: " + commandName);
        } catch (Exception e) {
            CSend.error("Error registering command '" + commandName + "': " + e.getMessage());
            CSend.error(e);
        }
    }

    public static void unRegisterCommands() {
        for (String commandName : new HashSet<>(registeredCommands)) {
            unRegisterCommand(commandName);
        }
        registeredCommands.clear();
    }

    public static void resetRegistry() {
        registeredCommands.clear();
    }

    public static Set<String> getRegisteredCommands() {
        return new HashSet<>(registeredCommands);
    }

    public static void unRegisterCommand(String commandName) {
        CSend.debug("Attempting to unregister command: " + commandName);

        try {
            String pluginPrefix = BasePlugin.getInstance().getPluginName().toLowerCase();
            String key = commandName.toLowerCase();
            String namespacedKey = pluginPrefix + ":" + key;

            Command command = knownCommands.get(key);
            if (command == null) {
                command = knownCommands.get(namespacedKey);
            }

            if (command != null) {

                List<String> aliasesToRemove = new ArrayList<>(command.getAliases());

                knownCommands.remove(key);
                knownCommands.remove(namespacedKey);
                registeredCommands.remove(commandName);

                for (String alias : aliasesToRemove) {
                    String aliasLower = alias.toLowerCase();
                    String namespacedAlias = pluginPrefix + ":" + aliasLower;

                    Command aliasCommand = knownCommands.get(aliasLower);
                    if (aliasCommand != null && aliasCommand.equals(command)) {
                        knownCommands.remove(aliasLower);
                    }

                    Command namespacedAliasCommand = knownCommands.get(namespacedAlias);
                    if (namespacedAliasCommand != null && namespacedAliasCommand.equals(command)) {
                        knownCommands.remove(namespacedAlias);
                    }
                }

                Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Command> entry = iterator.next();
                    if (entry.getValue().equals(command)) {
                        iterator.remove();
                    }
                }

            } else {
                knownCommands.remove(key);
                knownCommands.remove(namespacedKey);
                registeredCommands.remove(commandName);
            }
        } catch (Exception e) {
            CSend.error("Failed to unregister command '" + commandName + "': " + e.getMessage());
            CSend.error(e);
        }
    }

    private static CommandMap getCommandMap() throws Exception {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> getKnownCommands(SimpleCommandMap commandMap) throws Exception {
        Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        return (Map<String, Command>) knownCommandsField.get(commandMap);
    }

    private static class DynamicCommand extends Command {
        private final TabExecutor executor;

        public DynamicCommand(String name, TabExecutor executor, List<String> aliases) {
            super(name);
            this.executor = executor;
            this.setAliases(aliases);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
            if (!BasePlugin.getInstance().isEnabled()) {
                sender.sendMessage("§cThis plugin is currently disabled.");
                return true;
            }
            return executor.onCommand(sender, this, label, args);
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
            List<String> completions = executor.onTabComplete(sender, this, alias, args);
            return completions != null ? completions : new ArrayList<>();
        }
    }
}