package cc.hubailmn.utility.item;

import net.kyori.adventure.text.Component;
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

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack itemStack) {
        this.stack = itemStack;
        this.meta = this.stack.getItemMeta();
    }

    public ItemBuilder material(Material material) {
        this.stack.setType(material);
        return this;
    }

    public ItemBuilder name(Component name) {
        this.meta.displayName(name);
        return this;
    }

    public ItemBuilder name(String name) {
        return name(Component.text(name));
    }

    public ItemBuilder lore(Component line) {
        List<Component> lore = this.meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(line);
        this.meta.lore(lore);
        return this;
    }

    public ItemBuilder lore(String line) {
        return lore(Component.text(line));
    }

    public ItemBuilder lore(List<String> lines) {
        for (String line : lines) {
            lore(line);
        }
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

    public ItemBuilder flag(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        this.meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        this.meta.setCustomModelData(data);
        return this;
    }

    public ItemStack build() {
        this.stack.setItemMeta(this.meta);
        return this.stack;
    }
}
