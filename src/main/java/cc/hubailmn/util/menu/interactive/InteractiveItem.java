package cc.hubailmn.util.menu.interactive;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Data
public abstract class InteractiveItem {

    private final ItemStack item;
    private final Consumer<Player> onClick;

    public InteractiveItem(ItemStack item, Consumer<Player> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public void handleClick(Player player) {
        if (onClick != null) {
            onClick.accept(player);
        }
    }
}

