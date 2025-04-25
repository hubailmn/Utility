package me.hubailmn.util.menu.listener;

import me.hubailmn.util.annotation.RegisterListener;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.interactive.Button;
import me.hubailmn.util.menu.type.MenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

@RegisterListener
public class ButtonClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        MenuBuilder menu = MenuManager.getActiveMenu(player);
        if (menu == null) {
            return;
        }

        if (!menu.getTitle().equals(e.getView().title())) {
            MenuManager.clearActiveMenu(player);
            return;
        }

        if (!e.isCancelled()) {
            e.setCancelled(menu.isInventoryClickCancel());
        }

        if (e.getClickedInventory() == player.getInventory()) {
            e.setCancelled(menu.isPlayerInventoryClickCancel());
            return;
        }

        e.setCancelled(menu.isInventoryClickCancel());

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        List<Button> buttons = menu.getButtons();
        if (buttons == null) return;

        for (Button button : buttons) {

            if (button.getSlot() == slot) {
                e.setCancelled(menu.isButtonClickCancel());
                button.onClick(player);
                return;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.getPlayer().closeInventory();
        MenuManager.clearActiveMenu(e.getPlayer());
    }
}
