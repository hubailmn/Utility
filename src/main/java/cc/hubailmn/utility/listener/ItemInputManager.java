package cc.hubailmn.utility.listener;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.player.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ItemInputManager implements Listener {
    private static final Map<UUID, ItemInputSession> sessions = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT_TICKS = 20 * 30;

    public static void ask(Player player, Consumer<ItemStack> callback) {
        ask(player, callback, item -> true, DEFAULT_TIMEOUT_TICKS);
    }

    public static void ask(Player player, Consumer<ItemStack> callback, Predicate<ItemStack> validator, long timeoutTicks) {
        UUID uuid = player.getUniqueId();

        if (sessions.containsKey(uuid)) {
            MessageUtil.prefixed(player, "§cYou're already selecting an item.");
            return;
        }

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BasePlugin.getInstance(), () -> {
            sessions.remove(uuid);
            MessageUtil.prefixed(player, "§eItem selection timed out.");
        }, timeoutTicks);

        sessions.put(uuid, new ItemInputSession(callback, validator, taskId));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        if (!sessions.containsKey(uuid)) return;
        if (event.getClickedInventory() != player.getInventory()) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        ItemStack item = (clicked == null || clicked.getType().isAir()) ? new ItemStack(Material.AIR) : clicked.clone();

        ItemInputSession session = sessions.remove(uuid);
        Bukkit.getScheduler().cancelTask(session.taskId);

        if (!session.validator.test(item)) {
            MessageUtil.prefixed(player, "§cInvalid item selected. Try again.");
            return;
        }

        session.callback.accept(item);
    }

    private record ItemInputSession(Consumer<ItemStack> callback, Predicate<ItemStack> validator, int taskId) {
    }
}
