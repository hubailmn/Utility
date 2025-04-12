package me.hubailmn.util.Registry;

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
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            DynamicCommand dynamicCommand = new DynamicCommand(commandName, executor);
            commandMap.register(commandName, "quickshopgui", dynamicCommand);

            registeredCommands.add(commandName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void unRegisterCommand() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            if (!(commandMap instanceof SimpleCommandMap simpleCommandMap)) {
                throw new IllegalStateException("Unsupported command map implementation");
            }

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(simpleCommandMap);

            for (String commandName : registeredCommands) {
                knownCommands.remove(commandName);
                knownCommands.remove("quickshopgui:" + commandName);
            }

            registeredCommands.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}