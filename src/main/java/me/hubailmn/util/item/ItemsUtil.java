package me.hubailmn.util.item;

import me.hubailmn.util.other.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ItemsUtil {

    private ItemsUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Checks if the inventory has enough of a target item to meet the required amount.
     *
     * @param inventory      the player's inventory
     * @param target         the target ItemStack to check
     * @param requiredAmount the amount required
     * @return true if inventory contains the required amount of the target item
     */
    public static boolean hasEnough(PlayerInventory inventory, ItemStack target, int requiredAmount) {
        if (target == null || requiredAmount <= 0) return false;

        return inventory.all(target)
                .values()
                .stream()
                .mapToInt(ItemStack::getAmount)
                .sum() >= requiredAmount;
    }


    /**
     * Removes the specified amount of a target item from the inventory.
     *
     * @param inventory the player's inventory
     * @param target    the target ItemStack to remove
     * @param amount    the amount to remove
     * @return true if the items are successfully removed
     */
    public static boolean removeItem(PlayerInventory inventory, ItemStack target, int amount) {
        if (target == null || amount <= 0) return false;

        Map<Integer, ? extends ItemStack> matchingItems = inventory.all(target);
        int availableAmount = matchingItems.values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();

        if (availableAmount < amount) return false;

        int remaining = amount;

        for (Map.Entry<Integer, ? extends ItemStack> entry : matchingItems.entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();

            int stackAmount = item.getAmount();
            if (stackAmount <= remaining) {
                inventory.clear(slot);
                remaining -= stackAmount;
            } else {
                item.setAmount(stackAmount - remaining);
                inventory.setItem(slot, item);
                return true;
            }

            if (remaining <= 0) return true;
        }

        return remaining <= 0;
    }


    /**
     * Adds an item to the inventory. If there isn't enough space, any leftover items
     * will be dropped at the player's location.
     *
     * @param inventory the player's inventory
     * @param itemToAdd the item to add
     * @param amount    the amount to add
     */
    public static void addItem(PlayerInventory inventory, ItemStack itemToAdd, int amount) {
        if (itemToAdd == null || amount <= 0) return;

        itemToAdd.setAmount(amount);
        Map<Integer, ItemStack> remainingItems = inventory.addItem(itemToAdd);

        if (!remainingItems.isEmpty()) {
            remainingItems.values().forEach(remaining ->
                    inventory.getLocation().getWorld().dropItemNaturally(inventory.getLocation(), remaining)
            );
        }
    }

    /**
     * Gets the name of an ItemStack based on its display name, or its material name if no display name is set.
     *
     * @param item the ItemStack to get the name of
     * @return the name of the item
     */
    public static String getItemName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Unknown Item";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName != null) {
                return PlainTextComponentSerializer.plainText().serialize(displayName);
            }
        }

        return StringUtil.capitalizeWords(item.getType().name().replace('_', ' ').toLowerCase());
    }
}