package cc.hubailmn.utility.menu.interactive;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
@Setter
public class GuiSlotButton extends GuiElement {

    private final int slot;
    boolean clickCancel;

    public GuiSlotButton(int slot, ItemStack item, Consumer<Player> onClick, boolean clickCancel) {
        super(item, onClick);
        this.slot = slot;
        this.clickCancel = clickCancel;
    }

    public GuiSlotButton(int slot, ItemStack item, Consumer<Player> onClick) {
        this(slot, item, onClick, true);
    }

    public GuiSlotButton(int slot, ItemStack item) {
        this(slot, item, null, true);
    }
}
