package me.hubailmn.util.item;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack stack;
    private final ItemMeta meta;

    public ItemBuilder() {
        this(new ItemStack(Material.STONE));
    }

    public ItemBuilder(ItemStack itemStack) {
        this.stack = itemStack;
        this.meta = this.stack.getItemMeta();
    }

    public ItemBuilder material(Material material) {
        this.stack.setType(material);
        return this;
    }

    public ItemBuilder name(String name) {
        this.meta.setDisplayName("§f" + name);
        return this;
    }

    public ItemBuilder lore(String loreLine) {
        List<String> lore = this.meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add("§f" + loreLine);
        this.meta.setLore(lore);
        return this;
    }

    public ItemBuilder lore(Iterable<String> loreList) {
        loreList.forEach(this::lore);
        return this;
    }

    public ItemBuilder count(int amount) {
        this.stack.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder flag(ItemFlag... itemFlags) {
        this.meta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier attributeModifier) {
        this.meta.addAttributeModifier(attribute, attributeModifier);
        return this;
    }

    public ItemBuilder unbreakable(boolean isUnbreakable) {
        this.meta.setUnbreakable(isUnbreakable);
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        this.meta.setCustomModelData(customModelData);
        return this;
    }

    public ItemStack build() {
        this.stack.setItemMeta(this.meta);
        return this.stack;
    }
}
