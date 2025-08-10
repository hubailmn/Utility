package cc.hubailmn.utility.listener;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.RegisterListener;
import cc.hubailmn.utility.interaction.player.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RegisterListener
public class ChatInputManager implements Listener {

    private static final Map<UUID, InputSession> sessions = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT_TICKS = 20 * 30;

    public static void ask(Player player, String prompt, Consumer<String> callback) {
        ask(player, prompt, callback, s -> true, DEFAULT_TIMEOUT_TICKS);
    }

    public static void ask(Player player, String prompt, Consumer<String> callback, Predicate<String> validator, long timeoutTicks) {
        UUID uuid = player.getUniqueId();

        if (isAwaitingInput(player)) {
            MessageUtil.prefixed(player, "§cYou're already being asked for input.");
            return;
        }

        MessageUtil.prefixed(player, prompt);

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BasePlugin.getInstance(), () -> {
            sessions.remove(uuid);
            MessageUtil.prefixed(player, "§eInput timed out.");
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
            MessageUtil.prefixed(player, "§cInvalid input. Try again.");
            return;
        }

        session.callback.accept(message);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!isAwaitingInput(player)) return;

        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());

        Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> handleInput(player, message));
    }

    private record InputSession(Consumer<String> callback, Predicate<String> validator, int taskId) {
    }
}
