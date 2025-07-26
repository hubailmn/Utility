package cc.hubailmn.utility.item;

import cc.hubailmn.utility.util.TextParserUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

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

    public ItemBuilder durability(int durability) {
        if (meta instanceof Damageable damageable) {
            damageable.damage(durability);
        }
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

    public ItemBuilder addUnsafeEnchantment(Enchantment enchantment, int level) {
        this.stack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        this.stack.removeEnchantment(enchantment);
        return this;
    }

    public ItemBuilder setInfinityDurability() {
        durability(32767);
        return this;
    }

    public ItemBuilder setArmorColor(Color color) {
        if (this.meta instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
        }
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

    public ItemBuilder skull(String playerName) {
        if (!(meta instanceof SkullMeta skullMeta) || stack.getType() != Material.PLAYER_HEAD) {
            return this;
        }

        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        return this;
    }

    public ItemBuilder skullTexture(String base64) {
        if (!(meta instanceof SkullMeta skullMeta) || stack.getType() != Material.PLAYER_HEAD) {
            return this;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", base64));
        skullMeta.setPlayerProfile(profile);

        return this;
    }

    public ItemBuilder skullTexture(String texture, String signature) {
        if (!(meta instanceof SkullMeta skullMeta) || stack.getType() != Material.PLAYER_HEAD) {
            return this;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", texture, signature));
        skullMeta.setPlayerProfile(profile);

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
