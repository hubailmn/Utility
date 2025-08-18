package cc.hubailmn.utility.item;

import cc.hubailmn.utility.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class InventoryItemManager {

    private InventoryItemManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean hasEnough(PlayerInventory inventory, ItemStack target, int requiredAmount) {
        if (target == null || requiredAmount <= 0) return false;

        return inventory.all(target.getType()).values().stream()
                .filter(item -> item.isSimilar(target))
                .mapToInt(ItemStack::getAmount)
                .sum() >= requiredAmount;
    }

    public static boolean removeItem(PlayerInventory inventory, ItemStack target, int amount) {
        if (target == null || amount <= 0) return false;

        Map<Integer, ? extends ItemStack> matchingItems = inventory.all(target.getType());

        int availableAmount = matchingItems.values().stream()
                .filter(item -> item.isSimilar(target))
                .mapToInt(ItemStack::getAmount)
                .sum();

        if (availableAmount < amount) return false;

        int remaining = amount;

        for (Map.Entry<Integer, ? extends ItemStack> entry : matchingItems.entrySet()) {
            ItemStack item = entry.getValue();

            if (!item.isSimilar(target)) continue;

            int slot = entry.getKey();
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

        return false;
    }

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

    public static int getAmount(PlayerInventory inventory, ItemStack target) {
        if (target == null) {
            return 0;
        }
        return inventory.all(target).values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

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