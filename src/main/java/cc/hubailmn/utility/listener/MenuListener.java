package cc.hubailmn.utility.listener;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.RegisterListener;
import cc.hubailmn.utility.menu.MenuInventoryHolder;
import cc.hubailmn.utility.menu.interactive.GuiSlotButton;
import cc.hubailmn.utility.menu.interactive.GuiElement;
import cc.hubailmn.utility.menu.type.MenuBuilder;
import cc.hubailmn.utility.menu.type.PagedMenuBuilder;
import cc.hubailmn.utility.menu.type.ScrollableMenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@RegisterListener
public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Inventory clickedInv = e.getClickedInventory();
        if (clickedInv == null) return;

        Inventory topInv = player.getOpenInventory().getTopInventory();
        InventoryHolder holder = topInv.getHolder();
        if (!(holder instanceof MenuInventoryHolder menuHolder)) return;

        MenuBuilder menu = menuHolder.getMenu();
        if (menu == null) return;

        if (clickedInv.equals(player.getInventory())) {
            e.setCancelled(menu.isInventoryClickCancelled());
            return;
        }

        if (clickedInv.equals(topInv)) {
            e.setCancelled(menu.isMenuClickCancelled());
        } else {
            return;
        }

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= topInv.getSize()) return;

        GuiSlotButton button = menu.getButtons().get(slot);
        if (button != null) {
            e.setCancelled(button.isClickCancel());
            button.handleClick(player);
            return;
        }

        GuiElement item = null;
        boolean cancel = false;

        if (menu instanceof PagedMenuBuilder pagedMenu) {
            item = pagedMenu.getInteractiveItemBySlot(slot);
            cancel = pagedMenu.isItemClickedCancel();
        } else if (menu instanceof ScrollableMenuBuilder scrollableMenu) {
            item = scrollableMenu.getInteractiveItemBySlot(slot);
            cancel = scrollableMenu.isMenuClickCancelled();
        }

        if (item != null) {
            e.setCancelled(cancel);
            item.handleClick(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof MenuInventoryHolder menuHolder)) return;

        MenuBuilder menu = menuHolder.getMenu();
        if (menu == null) return;

        menu.onClose(player);
    }

    @EventHandler
    public void onPluginShutDown(PluginDisableEvent e) {
        if (!e.getPlugin().equals(BasePlugin.getInstance())) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuInventoryHolder) {
                player.closeInventory();
            }
        }
    }
}
