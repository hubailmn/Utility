package cc.hubailmn.utility.listener;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.RegisterListener;
import cc.hubailmn.utility.plugin.CSend;
import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.interaction.player.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RegisterListener
public class ChatInputManager implements Listener {

    private static final Map<UUID, InputSession> sessions = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT_TICKS = 20 * 60;
    private static final Set<String> CANCEL_WORDS = Set.of("#cancel", "#stop", "#exit", "#quit", "#abort");

    public static void ask(Player player, String prompt, Consumer<String> callback) {
        ask(player, prompt, callback, s -> true, DEFAULT_TIMEOUT_TICKS, true);
    }

    public static void ask(Player player, String prompt, Consumer<String> callback, Predicate<String> validator, long timeoutTicks) {
        ask(player, prompt, callback, validator, timeoutTicks, true);
    }

    /**
     * Ask a player for input with full customization
     *
     * @param player       The player to ask
     * @param prompt       The prompt message
     * @param callback     Function to handle the response
     * @param validator    Function to validate input
     * @param timeoutTicks Timeout in ticks
     * @param cancellable  Whether the input can be cancelled
     */
    public static void ask(Player player, String prompt, Consumer<String> callback, Predicate<String> validator, long timeoutTicks, boolean cancellable) {
        UUID uuid = player.getUniqueId();

        if (isAwaitingInput(player)) {
            MessageUtil.prefixed(player, "§cYou're already being asked for input.");
            return;
        }

        MessageUtil.prefixed(player, prompt);
        if (cancellable) {
            MessageUtil.prefixed(player, "§7Type '#cancel' to cancel this input.");
        }

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BasePlugin.getInstance(), () -> {
            InputSession session = sessions.remove(uuid);
            if (session != null && player.isOnline()) {
                MessageUtil.prefixed(player, "§eInput timed out.");
                if (session.timeoutCallback != null) {
                    session.timeoutCallback.run();
                }
            }
        }, timeoutTicks);

        sessions.put(uuid, new InputSession(callback, validator, taskId, cancellable, null, null));
    }

    public static void ask(Player player, String prompt, Consumer<String> callback, Predicate<String> validator, long timeoutTicks, boolean cancellable, Runnable cancelCallback, Runnable timeoutCallback) {
        UUID uuid = player.getUniqueId();

        if (isAwaitingInput(player)) {
            MessageUtil.prefixed(player, "§cYou're already being asked for input.");
            return;
        }

        MessageUtil.prefixed(player, prompt);
        if (cancellable) {
            MessageUtil.prefixed(player, "§7Type 'cancel' to cancel this input.");
        }

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BasePlugin.getInstance(), () -> {
            InputSession session = sessions.remove(uuid);
            if (session != null && player.isOnline()) {
                MessageUtil.prefixed(player, "§eInput timed out.");
                if (timeoutCallback != null) {
                    timeoutCallback.run();
                }
            }
        }, timeoutTicks);

        sessions.put(uuid, new InputSession(callback, validator, taskId, cancellable, cancelCallback, timeoutCallback));
    }

    public static boolean isAwaitingInput(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public static boolean cancelInput(Player player) {
        UUID uuid = player.getUniqueId();
        InputSession session = sessions.remove(uuid);

        if (session == null) {
            return false;
        }

        Bukkit.getScheduler().cancelTask(session.taskId);

        if (session.cancelCallback != null) {
            session.cancelCallback.run();
        }

        return true;
    }

    public static int getActiveSessionCount() {
        return sessions.size();
    }

    public static void clearAllSessions() {
        sessions.values().forEach(session -> Bukkit.getScheduler().cancelTask(session.taskId));
        sessions.clear();
    }

    private static void handleInput(Player player, String message) {
        UUID uuid = player.getUniqueId();
        InputSession session = sessions.get(uuid);

        if (session == null) return;

        if (session.cancellable && CANCEL_WORDS.contains(message.toLowerCase().trim())) {
            sessions.remove(uuid);
            Bukkit.getScheduler().cancelTask(session.taskId);
            MessageUtil.prefixed(player, "§eInput cancelled.");
            SoundUtil.play(player, SoundUtil.SoundType.GAME_TRIGGER);

            if (session.cancelCallback != null) {
                session.cancelCallback.run();
            }
            return;
        }

        if (!session.validator.test(message)) {
            MessageUtil.prefixed(player, "§cInvalid input. Please try again.");
            return;
        }

        sessions.remove(uuid);
        Bukkit.getScheduler().cancelTask(session.taskId);

        try {
            session.callback.accept(message);
        } catch (Exception e) {
            MessageUtil.prefixed(player, "§cAn error occurred while processing your input.");
            CSend.error("Error processing chat input for {} : {}", e, player.getName(), e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (!isAwaitingInput(player)) {
            return;
        }

        event.viewers().removeIf(viewer ->
                viewer instanceof Player viewerPlayer && isAwaitingInput(viewerPlayer)
        );

        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());

        Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> handleInput(player, message));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        InputSession session = sessions.remove(uuid);

        if (session != null) {
            Bukkit.getScheduler().cancelTask(session.taskId);
        }
    }

    private record InputSession(
            Consumer<String> callback,
            Predicate<String> validator,
            int taskId,
            boolean cancellable,
            Runnable cancelCallback,
            Runnable timeoutCallback
    ) {
    }
}