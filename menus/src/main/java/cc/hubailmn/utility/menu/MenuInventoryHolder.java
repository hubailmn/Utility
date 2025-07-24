package cc.hubailmn.utility.menu;

import cc.hubailmn.utility.menu.type.MenuBuilder;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@Getter
public class MenuInventoryHolder implements InventoryHolder {

    private final MenuBuilder menu;

    public MenuInventoryHolder(MenuBuilder menu) {
        this.menu = menu;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return menu.getInventory();
    }
}