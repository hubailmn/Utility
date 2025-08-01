package cc.hubailmn.utility.menu.interactive;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Data
public class GuiElement {

    private final ItemStack item;
    private final Consumer<Player> onClick;

    public GuiElement(ItemStack item, Consumer<Player> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public GuiElement(ItemStack item) {
        this(item, null);
    }

    public void handleClick(Player player) {
        if (onClick != null) {
            onClick.accept(player);
        }
    }
}

