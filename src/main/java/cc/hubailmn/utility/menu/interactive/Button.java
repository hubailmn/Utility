package cc.hubailmn.utility.menu.interactive;

import cc.hubailmn.utility.interaction.SoundUtil;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public abstract class Button {

    private final int slot;
    private final ItemStack item;
    boolean clickCancel;

    public Button(int slot, ItemStack item) {
        this(slot, item, true);
    }

    public Button(int slot, ItemStack item, boolean clickCancel) {
        this.slot = slot;
        this.item = item;
        this.clickCancel = clickCancel;
    }

    public void onClick(Player player) {
        playSound(player);
    }

    public void playSound(Player player) {
        SoundUtil.play(player, SoundUtil.SoundType.CLICK);
    }
}
