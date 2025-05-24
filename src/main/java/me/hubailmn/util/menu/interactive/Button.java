package me.hubailmn.util.menu.interactive;

import lombok.Data;
import me.hubailmn.util.interaction.SoundPreset;
import me.hubailmn.util.interaction.player.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public abstract class Button {
    private final int slot;
    private final ItemStack item;
    private boolean clickCancel;

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

    // Java Edition Actions
    public void onRightClick(Player player) {
        onClick(player);
    }

    public void onLeftClick(Player player) {
        onClick(player);
    }

    public void onShiftRightClick(Player player) {
        onClick(player);
    }

    public void onShiftLeftClick(Player player) {
        onClick(player);
    }

    // Bedrock Edition Actions
    public void onBedrockPrimaryAction(Player player) {
        // Single tap (A/X button) → Treat as LEFT CLICK equivalent
        if (PlayerUtil.isBedrock(player)) {
            onLeftClick(player); // Default to left click behavior
        }
    }

    public void onBedrockSecondaryAction(Player player) {
        // Hold (RT/R2) → Treat as RIGHT CLICK equivalent
        if (PlayerUtil.isBedrock(player)) {
            onRightClick(player);
        }
    }

    public void onBedrockQuickMove(Player player) {
        // Double tap (A/X) → Treat as SHIFT+CLICK equivalent
        if (PlayerUtil.isBedrock(player)) {
            onShiftLeftClick(player); // Or onShiftRightClick(player) depending on context
        }
    }

    public void playSound(Player player) {
        SoundPreset.play(player, SoundPreset.SoundType.CLICK);
    }
}