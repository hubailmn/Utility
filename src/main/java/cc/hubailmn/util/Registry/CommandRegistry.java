package cc.hubailmn.util.Registry;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.interaction.CSend;
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
            if (registeredCommands.contains(commandName)) {
                CSend.debug("Command '" + commandName + "' already registered. Skipping.");
                return;
            }

            DynamicCommand dynamicCommand = new DynamicCommand(commandName, executor, aliases);
            commandMap.register(BasePlugin.getPluginName(), dynamicCommand);
            registeredCommands.add(commandName);
        } catch (Exception e) {
            CSend.error("Error registering command '" + commandName + "': " + e.getMessage());
            CSend.error(e);
        }
    }

    public static void unRegisterCommands() {
        CSend.debug("Unregistering all registered commands...");
        for (String commandName : new HashSet<>(registeredCommands)) {
            unRegisterCommand(commandName);
        }
    }

    public static void unRegisterCommand(String commandName) {
        CSend.debug("Attempting to unregister command: " + commandName);

        try {
            String pluginPrefix = BasePlugin.getPluginName().toLowerCase();
            String key = commandName.toLowerCase();
            String namespacedKey = pluginPrefix + ":" + key;

            Command command = knownCommands.getOrDefault(key, knownCommands.get(namespacedKey));

            if (command != null) {
                knownCommands.remove(key);
                knownCommands.remove(namespacedKey);
                registeredCommands.remove(commandName);

                CSend.debug("Successfully unregistered command: " + commandName);

                for (String alias : command.getAliases()) {
                    if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().toLowerCase().contains(pluginPrefix)) {
                        knownCommands.remove(alias);
                        CSend.debug("Removed alias '" + alias + "' for command '" + commandName + "'");
                    }
                }
            } else {
                CSend.warn("Command '" + commandName + "' not found in knownCommands.");
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
            return executor.onCommand(sender, this, label, args);
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
            return Objects.requireNonNull(executor.onTabComplete(sender, this, alias, args));
        }
    }

}
