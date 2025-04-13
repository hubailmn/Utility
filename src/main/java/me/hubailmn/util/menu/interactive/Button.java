package me.hubailmn.util.menu.interactive;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class Button {

    private final int slot;
    private final ItemStack item;

    public Button(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    public void onClick(Player player) {

    }
}
