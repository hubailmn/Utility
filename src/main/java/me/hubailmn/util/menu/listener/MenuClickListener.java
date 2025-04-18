package me.hubailmn.util.menu.listener;

import me.hubailmn.util.annotation.EventListener;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.interactive.Button;
import me.hubailmn.util.menu.type.MenuBuilder;
import me.hubailmn.util.menu.type.PagedMenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

@EventListener
public class MenuClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        MenuBuilder menu = MenuManager.getActiveMenu(player);
        if (menu == null) return;

        if (!e.isCancelled()) {
            e.setCancelled(menu.isInventoryClickCancel());
        }

        if (e.getClickedInventory() == player.getInventory()) {
            e.setCancelled(menu.isPlayerInventoryClickCancel());
            return;
        }

        if (menu instanceof PagedMenuBuilder) {
            ((PagedMenuBuilder) menu).handleMenuClick(e);
        }

        int slot = e.getSlot();
        for (Button button : menu.getButtons()) {
            if (button.getSlot() == slot) {
                e.setCancelled(menu.isButtonClickCancel());
                button.onClick(player);
                return;
            }
        }
    }
}
