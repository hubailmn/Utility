package cc.hubailmn.utility.menu.type;

import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.item.ItemBuilder;
import cc.hubailmn.utility.menu.MenuLayout;
import cc.hubailmn.utility.menu.interactive.GuiElement;
import cc.hubailmn.utility.menu.interactive.GuiSlotButton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class PagedMenuBuilder extends MenuBuilder {

    protected final List<GuiElement> items = new ArrayList<>();
    protected List<Integer> contentSlots = new ArrayList<>();
    protected GuiSlotButton nextPageButton;
    protected GuiSlotButton previousPageButton;
    protected int page = 0;
    protected boolean itemClickedCancel = true;

    public PagedMenuBuilder() {
        super();
        setPageArea(1, 1, (size / 9) - 1, 9);

        previousPageButton = new GuiSlotButton(MenuLayout.getSlot(4, size / 9), new ItemBuilder()
                .material(Material.ARROW)
                .name("§ePrevious Page")
                .build(), player -> {
            SoundUtil.play(player, SoundUtil.SoundType.PAGE_FLIP);
            setPage(page - 1);
            loadPage(inventory);
            onPageChange(page);
        }, true);

        nextPageButton = new GuiSlotButton(MenuLayout.getSlot(6, size / 9), new ItemBuilder()
                .material(Material.ARROW)
                .name("§eNext Page")
                .build(), player -> {
            SoundUtil.play(player, SoundUtil.SoundType.PAGE_FLIP);
            setPage(page + 1);
            loadPage(inventory);
            onPageChange(page);
        }, true);
    }

    public void addItems(GuiElement... items) {
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

    public GuiElement getInteractiveItemBySlot(int clickedSlot) {
        int index = contentSlots.indexOf(clickedSlot);
        if (index == -1) return null;

        int globalIndex = page * contentSlots.size() + index;
        if (globalIndex >= items.size()) return null;

        return items.get(globalIndex);
    }

    @Override
    protected void afterDisplay(Inventory inventory) {
        this.page = 0;
        loadPage(inventory);
    }

    protected void loadPage(Inventory inventory) {
        inventory.clear();
        buttons.clear();
        setupButtons();
        setItems(inventory);

        if (contentSlots.isEmpty()) return;

        int itemsPerPage = contentSlots.size();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = contentSlots.get(i - startIndex);
            inventory.setItem(slot, items.get(i).getItem());
        }

        if (previousPageButton != null && page > 0) {
            inventory.setItem(previousPageButton.getSlot(), previousPageButton.getItem());
            addButtons(previousPageButton);
        }

        if (nextPageButton != null && (page + 1) * itemsPerPage < items.size()) {
            inventory.setItem(nextPageButton.getSlot(), nextPageButton.getItem());
            addButtons(nextPageButton);
        }

        for (GuiSlotButton button : buttons.values()) {
            int slot = button.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, button.getItem());
            }
        }
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
    }

    @Override
    public abstract void setupButtons();

    @Override
    public abstract void setItems(Inventory inventory);
}