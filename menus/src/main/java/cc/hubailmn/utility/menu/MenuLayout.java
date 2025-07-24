package cc.hubailmn.utility.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuLayout {

    public static void fillInventory(Inventory inventory, ItemStack filler) {
        if (filler == null) return;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler.clone());
            }
        }
    }

    public static void fillInventory(Inventory inventory, ItemStack filler, int... slots) {
        if (filler == null || slots == null) return;
        for (int slot : slots) {
            if (isValidSlot(slot, inventory) && inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler.clone());
            }
        }
    }

    public static void fillInventory(Inventory inventory, ItemStack filler, int fromSlot, int toSlot) {
        if (filler == null) return;
        for (int slot = fromSlot; slot <= toSlot; slot++) {
            if (isValidSlot(slot, inventory) && inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler.clone());
            }
        }
    }

    public static void fillInventory(Inventory inventory, List<ItemStack> items) {
        if (items == null || items.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null && index < items.size()) {
                inventory.setItem(i, items.get(index++).clone());
            }
        }
    }

    public static void fillInventory(Inventory inventory, List<ItemStack> items, int... slots) {
        if (items == null || items.isEmpty() || slots == null) return;
        int index = 0;
        for (int slot : slots) {
            if (isValidSlot(slot, inventory) && inventory.getItem(slot) == null && index < items.size()) {
                inventory.setItem(slot, items.get(index++).clone());
            }
        }
    }

    public static void fillInventory(Inventory inventory, List<ItemStack> items, int fromSlot, int toSlot) {
        if (items == null || items.isEmpty()) return;
        int index = 0;
        for (int slot = fromSlot; slot <= toSlot; slot++) {
            if (isValidSlot(slot, inventory) && inventory.getItem(slot) == null && index < items.size()) {
                inventory.setItem(slot, items.get(index++).clone());
            }
        }
    }

    public static void rectangle(Inventory inventory, int x1, int y1, int x2, int y2, List<ItemStack> items) {
        if (items == null || items.isEmpty()) return;
        int index = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                int slot = getSlot(x, y);
                if (isValidSlot(slot, inventory) && index < items.size()) {
                    inventory.setItem(slot, items.get(index++).clone());
                }
            }
        }
    }

    public static void rectangle(Inventory inventory, int x1, int y1, int x2, int y2, ItemStack item) {
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                int slot = getSlot(x, y);
                if (isValidSlot(slot, inventory)) {
                    inventory.setItem(slot, item.clone());
                }
            }
        }
    }

    public static void border(Inventory inventory, int fromRow, int toRow, ItemStack item) {
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

    public static void row(Inventory inventory, int row, ItemStack item) {
        int start = (row - 1) * 9;
        for (int i = 0; i < 9; i++) {
            int slot = start + i;
            if (isValidSlot(slot, inventory)) {
                inventory.setItem(slot, item.clone());
            }
        }
    }

    public static void column(Inventory inventory, int column, ItemStack item) {
        int colIndex = column - 1;
        if (colIndex < 0 || colIndex >= 9) return;

        for (int row = 0; row < inventory.getSize() / 9; row++) {
            int slot = row * 9 + colIndex;
            if (isValidSlot(slot, inventory)) {
                inventory.setItem(slot, item.clone());
            }
        }
    }

    public static void corners(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        inventory.setItem(0, item.clone());
        inventory.setItem(8, item.clone());
        inventory.setItem((rows - 1) * 9, item.clone());
        inventory.setItem(inventory.getSize() - 1, item.clone());
    }

    public static void edge(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        row(inventory, 1, item);
        row(inventory, rows, item);

        for (int row = 2; row < rows; row++) {
            int leftSlot = (row - 1) * 9;
            int rightSlot = (row - 1) * 9 + 8;
            inventory.setItem(leftSlot, item.clone());
            inventory.setItem(rightSlot, item.clone());
        }
    }

    public static void clearSlots(Inventory inventory, int... slots) {
        for (int slot : slots) {
            if (isValidSlot(slot, inventory)) {
                inventory.clear(slot);
            }
        }
    }

    public static void cross(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        int middleRow = rows / 2;
        int middleCol = 4;

        row(inventory, middleRow + 1, item);
        column(inventory, middleCol + 1, item);
    }

    public static void checkerboard(Inventory inventory, ItemStack item1, ItemStack item2) {
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

    public static void diagonal(Inventory inventory, ItemStack item) {
        int rows = inventory.getSize() / 9;
        for (int i = 0; i < Math.min(9, rows); i++) {
            int slot = i * 9 + i;
            if (isValidSlot(slot, inventory)) {
                inventory.setItem(slot, item.clone());
            }
        }
    }

    public static void centerBox(Inventory inventory, int boxWidth, int boxHeight, ItemStack item) {
        int rows = inventory.getSize() / 9;
        int startX = (9 - boxWidth) / 2 + 1;
        int startY = (rows - boxHeight) / 2 + 1;
        rectangle(inventory, startX, startY, startX + boxWidth - 1, startY + boxHeight - 1, item);
    }

    public static int getSlot(int x, int y) {
        return (y - 1) * 9 + (x - 1);
    }

    public static boolean isValidSlot(int slot, Inventory inventory) {
        return slot >= 0 && slot < inventory.getSize();
    }

}
