package me.hubailmn.util.menu.listeners;

import me.hubailmn.util.annotation.EventListener;
import me.hubailmn.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@EventListener
public class MenuCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        Menu menu = Menu.getActiveMenu(player);
        if (menu == null) return;

        Menu.clearActiveMenu(player);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Menu menu = Menu.getActiveMenu(player);
        if (menu == null) return;

        Menu.clearActiveMenu(player);
    }
}
