package me.hubailmn.util.menu.listener;

import me.hubailmn.util.annotation.RegisterListener;
import me.hubailmn.util.interaction.player.PlayerUtil;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.interactive.Button;
import me.hubailmn.util.menu.type.MenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RegisterListener
public class ButtonClickListener implements Listener {
    private final Map<UUID, Long> lastClickTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> clickSequences = new ConcurrentHashMap<>();
    private static final long ACTION_TIMEOUT = 500; // ms

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        MenuBuilder menu = MenuManager.getActiveMenu(player);
        if (menu == null) return;

        if (!menu.getTitle().equals(e.getView().title())) {
            MenuManager.clearActiveMenu(player);
            return;
        }

        if (e.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
            e.setCancelled(menu.isMenuClickCancelled());
        }

        if (e.getClickedInventory() == player.getInventory()) {
            e.setCancelled(menu.isInventoryClickCancelled());
            return;
        }

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        List<Button> buttons = menu.getButtons();
        if (buttons == null) return;

        for (Button button : buttons) {
            if (button.getSlot() == slot) {
                e.setCancelled(button.isClickCancel());
                handleClick(player, button, e);
                return;
            }
        }
    }

    private void handleClick(Player player, Button button, InventoryClickEvent e) {
        button.onClick(player);

        if (PlayerUtil.isBedrock(player)) {
            handleBedrockClick(player, button, e);
        } else {
            handleJavaClick(player, button, e);
        }
    }

    private void handleBedrockClick(Player player, Button button, InventoryClickEvent e) {
        long now = System.currentTimeMillis();
        long lastClick = lastClickTimes.getOrDefault(player.getUniqueId(), 0L);
        int clickCount = clickSequences.getOrDefault(player.getUniqueId(), 0);

        if (now - lastClick > 500) { // Reset if too slow
            clickCount = 0;
        }

        clickCount++;
        lastClickTimes.put(player.getUniqueId(), now);
        clickSequences.put(player.getUniqueId(), clickCount);

        if (e.isRightClick()) { // RT/R2 or touch-and-hold
            button.onBedrockSecondaryAction(player); // Right-click equivalent
        } else if (clickCount >= 2) { // Double tap (A/X)
            button.onBedrockQuickMove(player); // Shift-click equivalent
            clickSequences.put(player.getUniqueId(), 0); // Reset
        } else { // Single tap (A/X)
            button.onBedrockPrimaryAction(player); // Left-click equivalent
        }
    }

    private void handleJavaClick(Player player, Button button, InventoryClickEvent e) {
        if (e.isShiftClick()) {
            if (e.isLeftClick()) {
                button.onShiftLeftClick(player);
            } else if (e.isRightClick()) {
                button.onShiftRightClick(player);
            }
        } else {
            if (e.isLeftClick()) {
                button.onLeftClick(player);
            } else if (e.isRightClick()) {
                button.onRightClick(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        lastClickTimes.remove(uuid);
        clickSequences.remove(uuid);
        e.getPlayer().closeInventory();
        MenuManager.clearActiveMenu(e.getPlayer());
    }
}