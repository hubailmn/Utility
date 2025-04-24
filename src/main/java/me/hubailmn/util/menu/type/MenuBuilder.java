package me.hubailmn.util.menu.type;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.annotation.Menu;
import me.hubailmn.util.menu.interactive.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public abstract class MenuBuilder {

    protected final List<Button> buttons = new ArrayList<>();

    protected Component title;
    protected int size;
    protected boolean buttonClickCancel;
    protected boolean playerInventoryClickCancel;
    protected boolean inventoryClickCancel;

    public MenuBuilder() {
        Menu annotation = this.getClass().getAnnotation(Menu.class);
        if (annotation == null) {
            throw new IllegalStateException("Missing @Menu annotation on " + getClass().getName());
        }

        this.title = Component.text(annotation.title());
        this.size = annotation.rows() * 9;
        this.buttonClickCancel = annotation.buttonClickCancel();
        this.playerInventoryClickCancel = annotation.playerInvClickCancel();
        this.inventoryClickCancel = annotation.inventoryClickCancel();
    }

    public void display(Player player) {
        MenuManager.clearActiveMenu(player);
        Inventory inventory = Bukkit.createInventory(player, getSize(), getTitle());
        buttons.clear();

        setupButtons(player);
        setItems(inventory);

        for (Button button : buttons) {
            int slot = button.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, button.getItem());
            }
        }

        MenuManager.setActiveMenu(player, this);
        player.openInventory(inventory);
    }

    // ========== Layout Utilities ==========

    /**
     * Fills all empty slots in the inventory with the given item.
     */
    protected void fillInventory(Inventory inventory, ItemStack filler) {
        if (filler == null) return;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler.clone());
            }
        }
    }

    /**
     * Fills a rectangle between (x1, y1) and (x2, y2) inclusive.
     */
    protected void fillRectangle(Inventory inventory, int x1, int y1, int x2, int y2, ItemStack item) {
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                int slot = getSlot(x, y);
                if (isValidSlot(slot, inventory)) {
                    inventory.setItem(slot, item.clone());
                }
            }
        }
    }

    /**
     * Draws a border from row 'fromRow' to 'toRow' inclusive.
     */
    protected void drawBorder(Inventory inventory, int fromRow, int toRow, ItemStack item) {
        for (int row = fromRow; row <= toRow; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = (row - 1) * 9 + col;
                boolean isTopOrBottom = row == fromRow || row == toRow;
                boolean isEdge = col == 0 || col == 8;

                if ((isTopOrBottom || isEdge) && isValidSlot(slot, inventory)) {
                    inventory.setItem(slot, item.clone());
                }
            }
        }
    }

    /**
     * Fills an entire row (1-based index).
     */
    protected void fillRow(Inventory inventory, int row, ItemStack item) {
        int start = (row - 1) * 9;
        for (int i = 0; i < 9; i++) {
            int slot = start + i;
            if (isValidSlot(slot, inventory)) {
                inventory.setItem(slot, item.clone());
            }
        }
    }

    /**
     * Fills an entire column (1-based index).
     */
    protected void fillColumn(Inventory inventory, int column, ItemStack item) {
        int colIndex = column - 1;
        if (colIndex < 0 || colIndex >= 9) return;

        for (int row = 0; row < inventory.getSize() / 9; row++) {
            int slot = row * 9 + colIndex;
            if (isValidSlot(slot, inventory)) {
                inventory.setItem(slot, item.clone());
            }
        }
    }

    /**
     * Fills a centered box of specified width and height.
     */
    protected void fillCenterBox(Inventory inventory, int boxWidth, int boxHeight, ItemStack item) {
        int rows = inventory.getSize() / 9;
        int startX = (9 - boxWidth) / 2 + 1;
        int startY = (rows - boxHeight) / 2 + 1;
        fillRectangle(inventory, startX, startY, startX + boxWidth - 1, startY + boxHeight - 1, item);
    }

    /**
     * Converts (x, y) to slot index. x and y are 1-based.
     */
    protected int getSlot(int x, int y) {
        return (y - 1) * 9 + (x - 1);
    }

    /**
     * Checks if a slot is within the inventory bounds.
     */
    protected boolean isValidSlot(int slot, Inventory inventory) {
        return slot >= 0 && slot < inventory.getSize();
    }

    // ========== Button API ==========

    public void addButtons(Button... buttons) {
        if (buttons != null) {
            Collections.addAll(this.buttons, buttons);
        }
    }

    // ========== Hooks ==========

    /**
     * Called before inventory creation to register buttons.
     */
    public abstract void setupButtons(Player player);

    /**
     * Called after buttons are added to place decorative or functional items.
     */
    public abstract void setItems(Inventory inventory);
}
