package cc.hubailmn.util.interaction;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.annotation.RegisterListener;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RegisterListener
public class ChatInput implements Listener {

    private static final Map<UUID, InputSession> sessions = new HashMap<>();
    private static final long DEFAULT_TIMEOUT_TICKS = 20 * 30;

    public static void ask(Player player, String prompt, Consumer<String> callback) {
        ask(player, prompt, callback, s -> true, DEFAULT_TIMEOUT_TICKS);
    }

    public static void ask(Player player, String prompt, Consumer<String> callback, Predicate<String> validator, long timeoutTicks) {
        UUID uuid = player.getUniqueId();

        if (isAwaitingInput(player)) {
            player.sendMessage(BasePlugin.getPrefix() + " §cYou're already being asked for input.");
            return;
        }

        player.sendMessage(BasePlugin.getPrefix() + " " + prompt);

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BasePlugin.getInstance(), () -> {
            sessions.remove(uuid);
            player.sendMessage(BasePlugin.getPrefix() + " §eInput timed out.");
        }, timeoutTicks);

        sessions.put(uuid, new InputSession(callback, validator, taskId));
    }

    public static boolean isAwaitingInput(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    private static void handleInput(Player player, String message) {
        UUID uuid = player.getUniqueId();
        InputSession session = sessions.remove(uuid);
        if (session == null) return;

        Bukkit.getScheduler().cancelTask(session.taskId);

        if (!session.validator.test(message)) {
            player.sendMessage(BasePlugin.getPrefix() + " §cInvalid input. Try again.");
            return;
        }

        session.callback.accept(message);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!isAwaitingInput(player)) return;

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> handleInput(player, message));
    }

    private record InputSession(Consumer<String> callback, Predicate<String> validator, int taskId) {
    }
}
