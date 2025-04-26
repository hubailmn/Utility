package me.hubailmn.util.item;

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

/**
 * Builder class for creating and modifying ItemStacks.
 */

public class ItemBuilder {
    private final ItemStack stack;
    private final ItemMeta meta;

    /**
     * Creates a new ItemBuilder with STONE as the default material.
     */
    public ItemBuilder() {
        this(new ItemStack(Material.STONE));
    }

    /**
     * Creates a new ItemBuilder with the specified material.
     *
     * @param material The material to use
     */
    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Creates a new ItemBuilder from an existing ItemStack.
     *
     * @param itemStack The ItemStack to base the builder on
     */
    public ItemBuilder(ItemStack itemStack) {
        this.stack = itemStack;
        this.meta = this.stack.getItemMeta();
    }

    /**
     * Sets the material of the ItemStack.
     *
     * @param material The material to set
     * @return This builder for chaining
     */
    public ItemBuilder material(Material material) {
        this.stack.setType(material);
        return this;
    }

    /**
     * Sets the display name of the ItemStack using a Component.
     *
     * @param name The name to set
     * @return This builder for chaining
     */
    public ItemBuilder name(Component name) {
        this.meta.displayName(name);
        return this;
    }

    /**
     * Sets the display name of the ItemStack using a String.
     *
     * @param name The name to set
     * @return This builder for chaining
     */
    public ItemBuilder name(String name) {
        return name(Component.text(name));
    }

    /**
     * Adds a line to the item's lore using a Component.
     *
     * @param line The line to add
     * @return This builder for chaining
     */
    public ItemBuilder lore(Component line) {
        List<Component> lore = this.meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(line);
        this.meta.lore(lore);
        return this;
    }

    /**
     * Adds a line to the item's lore using a String.
     *
     * @param line The line to add
     * @return This builder for chaining
     */
    public ItemBuilder lore(String line) {
        return lore(Component.text(line));
    }

    /**
     * Adds multiple lines to the item's lore.
     *
     * @param lines The lines to add
     * @return This builder for chaining
     */
    public ItemBuilder lore(List<String> lines) {
        for (String line : lines) {
            lore(line);
        }
        return this;
    }

    /**
     * Sets the amount of items in the stack.
     *
     * @param amount The amount to set
     * @return This builder for chaining
     */
    public ItemBuilder count(int amount) {
        this.stack.setAmount(amount);
        return this;
    }

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment The enchantment to add
     * @param level       The level of the enchantment
     * @return This builder for chaining
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Adds item flags to the item.
     *
     * @param flags The flags to add
     * @return This builder for chaining
     */
    public ItemBuilder flag(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    /**
     * Adds an attribute modifier to the item.
     *
     * @param attribute The attribute to modify
     * @param modifier  The modifier to apply
     * @return This builder for chaining
     */
    public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        this.meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    /**
     * Sets whether the item is unbreakable.
     *
     * @param unbreakable True to make the item unbreakable
     * @return This builder for chaining
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param data The custom model data to set
     * @return This builder for chaining
     */
    public ItemBuilder customModelData(int data) {
        this.meta.setCustomModelData(data);
        return this;
    }

    /**
     * Builds and returns the final ItemStack.
     *
     * @return The constructed ItemStack
     */
    public ItemStack build() {
        this.stack.setItemMeta(this.meta);
        return this.stack;
    }
}
