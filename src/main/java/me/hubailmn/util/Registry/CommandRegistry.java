package me.hubailmn.util.Registry;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandRegistry {

    private static final Set<String> registeredCommands = new HashSet<>();

    public static void registerCommand(String commandName, TabExecutor executor) {
        try {
            CommandMap commandMap = getCommandMap();
            DynamicCommand dynamicCommand = new DynamicCommand(commandName, executor);
            commandMap.register(BasePlugin.getPluginName(), dynamicCommand);
            registeredCommands.add(commandName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unRegisterCommands() {
        for (String commandName : registeredCommands) {
            unRegisterCommand(commandName);
        }
    }

    public static void unRegisterCommand(String commandName) {
        CSend.debug("UnRegistering Command: " + commandName);

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) getCommandMap();
            Map<String, Command> knownCommands = getKnownCommands(commandMap);

            String pluginPrefix = BasePlugin.getPluginName().toLowerCase();
            String namespacedKey = pluginPrefix + ":" + commandName.toLowerCase();

            Command command = knownCommands.get(commandName.toLowerCase());
            if (command == null) {
                command = knownCommands.get(namespacedKey);
            }

            if (command != null) {
                knownCommands.remove(commandName.toLowerCase());
                knownCommands.remove(namespacedKey);
                CSend.debug(commandName + " has been removed from the known command map");

                for (String alias : command.getAliases()) {
                    if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(pluginPrefix)) {
                        CSend.debug("Removing alias '" + alias + "' of the command '" + commandName + "'");
                        knownCommands.remove(alias);
                    }
                }
            } else {
                CSend.debug("Command " + commandName + " not found in knownCommands");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
