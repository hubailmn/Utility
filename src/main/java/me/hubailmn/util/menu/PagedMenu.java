package me.hubailmn.util.menu;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.menu.interactive.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Getter
@Setter
public abstract class PagedMenu extends Menu {

    private final List<ItemStack> items = new ArrayList<>();
    private Button nextPageButton;
    private Button previousPageButton;
    private int startSlot = 0;
    private int endSlot = getSize() - 1;
    private int page = 0;

    private boolean itemClickedCancel = true;

    public PagedMenu(Player player) {
        super(player);
    }

    public void addItem(ItemStack item) {
        items.add(item);
    }

    public void addItems(ItemStack... items) {
        Collections.addAll(this.items, items);
    }

    @Override
    public void display() {
        player.closeInventory();

        Inventory inventory = Bukkit.createInventory(player, getSize(), getTitle());
        loadInv(inventory);

        player.setMetadata(MENU_METADATA_KEY, new FixedMetadataValue(getInstance(), this));
        player.openInventory(inventory);
    }

    private void loadInv(Inventory inventory) {
        inventory.clear();

        int itemsPerPage = endSlot - startSlot + 1;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startSlot; i <= endSlot; i++) {
            inventory.setItem(i, null);
        }

        setItems(inventory);

        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(startSlot + (i - startIndex), items.get(i));
        }

        for (Button button : getButtons()) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        if (page > 0) {
            inventory.setItem(previousPageButton.getSlot(), previousPageButton.getItem());
        }

        if (endIndex < items.size()) {
            inventory.setItem(nextPageButton.getSlot(), nextPageButton.getItem());
        }
    }

    public void handleMenuClick(InventoryClickEvent e) {
        int slot = e.getSlot();

        e.setCancelled(itemClickedCancel);

        if (slot == previousPageButton.getSlot() && page > 0) {
            e.setCancelled(true);
            page--;
            loadInv(e.getInventory());
        } else if (slot == nextPageButton.getSlot() && (page + 1) * (endSlot - startSlot + 1) < items.size()) {
            e.setCancelled(true);
            page++;
            loadInv(e.getInventory());
        }
    }
}
