package me.hubailmn.util.item;

import me.hubailmn.util.other.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemsUtil {

    private ItemsUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static boolean hasEnough(PlayerInventory inventory, ItemStack target, int requiredAmount) {
        if (target == null || requiredAmount <= 0) return false;

        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.isSimilar(target)) {
                total += item.getAmount();
                if (total >= requiredAmount) return true;
            }
        }
        return false;
    }

    public static boolean removeItem(PlayerInventory inventory, ItemStack target, int amount) {
        if (target == null || amount <= 0) return false;
        if (!hasEnough(inventory, target, amount)) return false;

        int remaining = amount;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || !item.isSimilar(target)) continue;

            int stackAmount = item.getAmount();
            if (stackAmount <= remaining) {
                inventory.setItem(i, null);
                remaining -= stackAmount;
            } else {
                item.setAmount(stackAmount - remaining);
                inventory.setItem(i, item);
                return true;
            }

            if (remaining <= 0) return true;
        }

        return true;
    }

    public static String getItemName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Unknown Item";
        }

        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;

        if (meta != null && meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName != null) {
                return PlainTextComponentSerializer.plainText().serialize(displayName);
            }
        }

        return StringUtil.capitalizeWords(item.getType().name().toLowerCase().replace('_', ' '));
    }
}
