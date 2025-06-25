package cc.hubailmn.utility.listener;

import cc.hubailmn.utility.annotation.RegisterListener;
import cc.hubailmn.utility.menu.MenuManager;
import cc.hubailmn.utility.menu.interactive.Button;
import cc.hubailmn.utility.menu.interactive.InteractiveItem;
import cc.hubailmn.utility.menu.type.MenuBuilder;
import cc.hubailmn.utility.menu.type.PagedMenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

@RegisterListener
public class MenuListener implements Listener {

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

        if (menu instanceof PagedMenuBuilder pagedMenu) {
            InteractiveItem item = pagedMenu.getInteractiveItemBySlot(slot);
            if (item != null) {
                item.handleClick(player);
                return;
            }
        }

        List<Button> buttons = menu.getButtons();
        if (buttons == null) return;

        for (Button button : buttons) {
            if (button.getSlot() == slot) {
                e.setCancelled(button.isClickCancel());
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
