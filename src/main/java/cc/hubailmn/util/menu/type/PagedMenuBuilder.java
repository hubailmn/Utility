package cc.hubailmn.util.menu.type;

import cc.hubailmn.util.interaction.SoundPreset;
import cc.hubailmn.util.item.ItemBuilder;
import cc.hubailmn.util.menu.MenuLayout;
import cc.hubailmn.util.menu.MenuManager;
import cc.hubailmn.util.menu.interactive.Button;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    protected List<Integer> contentSlots = new ArrayList<>();
    protected Button nextPageButton;
    protected Button previousPageButton;
    protected int page = 0;
    protected boolean itemClickedCancel = true;

    public PagedMenuBuilder() {
        super();

        nextPageButton = new Button(MenuLayout.getSlot(6, getSize() / 9), new ItemBuilder()
                .material(Material.ARROW)
                .name("§eNext Page")
                .build()) {
            @Override
            public void onClick(Player player) {
                SoundPreset.play(player, SoundPreset.SoundType.PAGE_FLIP);
                setPage(getPage() + 1);
                display(player);
            }
        };

        previousPageButton = new Button(MenuLayout.getSlot(4, getSize() / 9), new ItemBuilder()
                .material(Material.ARROW)
                .name("§ePrevious Page")
                .build()) {
            @Override
            public void onClick(Player player) {
                SoundPreset.play(player, SoundPreset.SoundType.PAGE_FLIP);
                setPage(getPage() - 1);
                display(player);
            }
        };
    }

    public void addItems(ItemStack... items) {
        if (items != null) Collections.addAll(this.items, items);
    }

    public void setPageArea(int startRow, int startCol, int endRow, int endCol) {
        contentSlots.clear();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                int slot = MenuLayout.getSlot(col, row);
                if (slot >= 0 && slot < size) {
                    contentSlots.add(slot);
                }
            }
        }
    }

    @Override
    public void display(Player player) {
        MenuManager.clearActiveMenu(player);
        Inventory inventory = Bukkit.createInventory(player, getSize(), getTitle());
        buttons.clear();

        setupButtons(player);
        loadPage(inventory);

        MenuManager.setActiveMenu(player, this);
        player.openInventory(inventory);
    }

    protected void loadPage(Inventory inventory) {
        inventory.clear();

        setItems(inventory);

        if (contentSlots.isEmpty()) return;

        int itemsPerPage = contentSlots.size();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = contentSlots.get(i - startIndex);
            inventory.setItem(slot, items.get(i));
        }

        if (previousPageButton != null && page > 0) {
            inventory.setItem(previousPageButton.getSlot(), previousPageButton.getItem());
            addButtons(previousPageButton);
        }

        if (nextPageButton != null && (page + 1) * itemsPerPage < items.size()) {
            inventory.setItem(nextPageButton.getSlot(), nextPageButton.getItem());
            addButtons(nextPageButton);
        }

        for (Button button : buttons) {
            int slot = button.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, button.getItem());
            }
        }

        onPageChange(page);
    }

    public void handleMenuClick(InventoryClickEvent e) {
        int slot = e.getSlot();
        e.setCancelled(itemClickedCancel);

        int itemsPerPage = contentSlots.size();
        if (itemsPerPage == 0) return;

        if (previousPageButton != null && slot == previousPageButton.getSlot() && page > 0) {
            page--;
            loadPage(e.getInventory());
            e.setCancelled(true);
        } else if (nextPageButton != null && slot == nextPageButton.getSlot() && (page + 1) * itemsPerPage < items.size()) {
            page++;
            loadPage(e.getInventory());
            e.setCancelled(true);
        }
    }

    protected void onPageChange(int newPage) {
        // Optional override
    }

    @Override
    public abstract void setupButtons(Player player);

    @Override
    public abstract void setItems(Inventory inventory);

}
