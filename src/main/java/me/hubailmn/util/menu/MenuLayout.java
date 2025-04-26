package me.hubailmn.util.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuLayout {

    /**
     * Fills all empty slots in the inventory with the given item.
     */
    public static void fillInventory(Inventory inventory, ItemStack filler) {
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
    public static void fillRectangle(Inventory inventory, int x1, int y1, int x2, int y2, ItemStack item) {
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
    public static void drawBorder(Inventory inventory, int fromRow, int toRow, ItemStack item) {
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
    public static void fillRow(Inventory inventory, int row, ItemStack item) {
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
    public static void fillColumn(Inventory inventory, int column, ItemStack item) {
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
     * Fills the four corners of the inventory.
     */
    public static void fillCorners(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        inventory.setItem(0, item.clone());
        inventory.setItem(8, item.clone());
        inventory.setItem((rows - 1) * 9, item.clone());
        inventory.setItem(inventory.getSize() - 1, item.clone());
    }

    /**
     * Fills only the outer edge of the inventory.
     */
    public static void fillEdge(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        fillRow(inventory, 1, item);
        fillRow(inventory, rows, item);

        for (int row = 2; row < rows; row++) {
            int leftSlot = (row - 1) * 9;
            int rightSlot = (row - 1) * 9 + 8;
            inventory.setItem(leftSlot, item.clone());
            inventory.setItem(rightSlot, item.clone());
        }
    }

    /**
     * Clears all items in specified slots.
     */
    public static void clearSlots(Inventory inventory, int... slots) {
        for (int slot : slots) {
            if (isValidSlot(slot, inventory)) {
                inventory.clear(slot);
            }
        }
    }

    /**
     * Creates a cross pattern in the inventory.
     */
    public static void fillCross(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        int middleRow = rows / 2;
        int middleCol = 4;

        fillRow(inventory, middleRow + 1, item);
        fillColumn(inventory, middleCol + 1, item);
    }

    /**
     * Creates a checkered pattern in the inventory.
     */
    public static void fillCheckerboard(Inventory inventory, ItemStack item1, ItemStack item2) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9;
            int col = slot % 9;
            if ((row + col) % 2 == 0) {
                inventory.setItem(slot, item1.clone());
            } else {
                inventory.setItem(slot, item2.clone());
            }
        }
    }

    /**
     * Fills diagonal slots from top-left to bottom-right.
     */
    public static void fillDiagonal(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        for (int i = 0; i < Math.min(9, rows); i++) {
            int slot = i * 9 + i;
            if (isValidSlot(slot, inventory)) {
                inventory.setItem(slot, item.clone());
            }
        }
    }

    /**
     * Fills a centered box of specified width and height.
     */
    public static void fillCenterBox(Inventory inventory, int boxWidth, int boxHeight, ItemStack item) {
        int rows = inventory.getSize() / 9;
        int startX = (9 - boxWidth) / 2 + 1;
        int startY = (rows - boxHeight) / 2 + 1;
        fillRectangle(inventory, startX, startY, startX + boxWidth - 1, startY + boxHeight - 1, item);
    }

    /**
     * Converts (x, y) to slot index. x and y are 1-based.
     */
    public static int getSlot(int x, int y) {
        return (y - 1) * 9 + (x - 1);
    }

    /**
     * Checks if a slot is within the inventory bounds.
     */
    public static boolean isValidSlot(int slot, Inventory inventory) {
        return slot >= 0 && slot < inventory.getSize();
    }

}
