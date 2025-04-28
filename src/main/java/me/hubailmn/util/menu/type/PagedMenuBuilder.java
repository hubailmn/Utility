package me.hubailmn.util.menu.type;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.interactive.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public abstract class PagedMenuBuilder extends MenuBuilder {

    protected final List<ItemStack> items = new ArrayList<>();
    protected Button nextPageButton;
    protected Button previousPageButton;
    protected int startSlot = 0;
    protected int endSlot = getSize() - 1;
    protected int page = 0;
    protected boolean itemClickedCancel = true;

    public void addItems(ItemStack... items) {
        Collections.addAll(this.items, items);
    }

    @Override
    public void display(Player player) {
        MenuManager.clearActiveMenu(player);
        Inventory inventory = Bukkit.createInventory(player, getSize(), getTitle());
        buttons.clear();

        setupButtons(player);
        setItems(inventory);

        loadPage(inventory);

        for (Button button : buttons) {
            int slot = button.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, button.getItem());
            }
        }

        MenuManager.setActiveMenu(player, this);
        player.openInventory(inventory);
    }

    protected void loadPage(Inventory inventory) {
        inventory.clear();
        int itemsPerPage = endSlot - startSlot + 1;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(startSlot + (i - startIndex), items.get(i));
        }

        setItems(inventory);
        buttons.forEach(button -> inventory.setItem(button.getSlot(), button.getItem()));

        if (page > 0 && previousPageButton != null) {
            inventory.setItem(previousPageButton.getSlot(), previousPageButton.getItem());
        }

        if (endIndex < items.size() && nextPageButton != null) {
            inventory.setItem(nextPageButton.getSlot(), nextPageButton.getItem());
        }

        onPageChange(page);
    }

    public void handleMenuClick(InventoryClickEvent e) {
        int slot = e.getSlot();
        e.setCancelled(itemClickedCancel);

        if (previousPageButton != null && slot == previousPageButton.getSlot() && page > 0) {
            e.setCancelled(true);
            page--;
            loadPage(e.getInventory());
        } else if (nextPageButton != null && slot == nextPageButton.getSlot() && (page + 1) * (endSlot - startSlot + 1) < items.size()) {
            e.setCancelled(true);
            page++;
            loadPage(e.getInventory());
        }
    }

    protected void onPageChange(int newPage) {
    }
}