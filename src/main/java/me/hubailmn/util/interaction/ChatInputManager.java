package me.hubailmn.util.interaction;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.hubailmn.util.BasePlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {

    private static final Map<UUID, Consumer<String>> inputConsumers = new HashMap<>();

    public static void ask(Player player, String prompt, Consumer<String> callback) {
        player.sendMessage(BasePlugin.getPrefix() + " " + prompt);
        inputConsumers.put(player.getUniqueId(), callback);
    }

    public static void handleInput(Player player, String message) {
        Consumer<String> callback = inputConsumers.remove(player.getUniqueId());
        if (callback != null) {
            callback.accept(message);
        }
    }

    public static boolean isAwaitingInput(Player player) {
        return inputConsumers.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (ChatInputManager.isAwaitingInput(player)) {
            event.setCancelled(true);
            String input = PlainTextComponentSerializer.plainText().serialize(event.message());

            Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () ->
                    ChatInputManager.handleInput(player, input)
            );
        }
    }

}
