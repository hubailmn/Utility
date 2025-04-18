package me.hubailmn.util.menu.listener;

import me.hubailmn.util.annotation.EventListener;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.type.MenuBuilder;
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

        MenuBuilder menu = MenuManager.getActiveMenu(player);
        if (menu == null) return;

        MenuManager.clearActiveMenu(player);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        MenuBuilder menu = MenuManager.getActiveMenu(player);
        if (menu == null) return;

        MenuManager.clearActiveMenu(player);
    }
}
