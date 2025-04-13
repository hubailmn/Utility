package me.hubailmn.util.menu.interactive;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class Clicker {

    private final ItemStack item;

    public Clicker(ItemStack item) {
        this.item = item;
    }

    public abstract void onClick(Player player);

}
