package me.hubailmn.util.menu.interactive;

import lombok.Getter;
import me.hubailmn.util.interaction.SoundPreset;
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
        playSound(player);
    }

    public void playSound(Player player) {
        SoundPreset.play(player, SoundPreset.SoundType.CLICK);
    }
}
