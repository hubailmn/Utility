package cc.hubailmn.utility.item;

import cc.hubailmn.utility.util.TextParserUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ItemBuilder {
    private final ItemStack stack;
    private final ItemMeta meta;
    private List<Component> loreList;

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
        return name(TextParserUtil.parse(name));
    }

    private List<Component> getLoreList() {
        if (loreList == null) {
            loreList = meta.lore();
            if (loreList == null) {
                loreList = new ArrayList<>();
            }
        }
        return loreList;
    }

    public ItemBuilder lore(Component line) {
        getLoreList().add(line);
        return this;
    }

    public ItemBuilder lore(String line) {
        return lore(TextParserUtil.parse(line));
    }

    public ItemBuilder lore(String... lines) {
        List<Component> components = Arrays.stream(lines)
                .map(TextParserUtil::parse)
                .toList();
        getLoreList().addAll(components);
        return this;
    }

    public ItemBuilder lore(Collection<String> lines) {
        List<Component> components = lines.stream()
                .map(TextParserUtil::parse)
                .toList();
        getLoreList().addAll(components);
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

    public ItemBuilder owningPlayer(String playerName) {
        if (this.stack.getType() != Material.PLAYER_HEAD) return this;

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (this.meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemStack build() {
        if (loreList != null) {
            meta.lore(loreList);
        }
        stack.setItemMeta(meta);
        return stack;
    }

}
