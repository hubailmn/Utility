package cc.hubailmn.util.menu.type;

import lombok.Getter;
import lombok.Setter;
import cc.hubailmn.util.menu.MenuManager;
import cc.hubailmn.util.menu.annotation.Menu;
import cc.hubailmn.util.menu.interactive.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public abstract class MenuBuilder {

    protected final List<Button> buttons = new ArrayList<>();

    protected Component title;
    protected int size;
    protected boolean inventoryClickCancelled;
    protected boolean menuClickCancelled;

    public MenuBuilder() {
        Menu annotation = this.getClass().getAnnotation(Menu.class);
        if (annotation == null) {
            throw new IllegalStateException("Missing @Menu annotation on " + getClass().getName());
        }

        this.title = Component.text(annotation.title());
        this.size = annotation.rows() * 9;
        this.inventoryClickCancelled = annotation.inventoryClickCancelled();
        this.menuClickCancelled = annotation.menuClickCancelled();
    }

    public void display(Player player) {
        MenuManager.clearActiveMenu(player);
        Inventory inventory = Bukkit.createInventory(player, getSize(), getTitle());
        buttons.clear();

        setupButtons(player);
        setItems(inventory);

        for (Button button : buttons) {
            int slot = button.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, button.getItem());
            }
        }

        MenuManager.setActiveMenu(player, this);
        player.openInventory(inventory);
    }

    public void addButtons(Button... buttons) {
        if (buttons != null) {
            Collections.addAll(this.buttons, buttons);
        }
    }

    public abstract void setupButtons(Player player);

    public abstract void setItems(Inventory inventory);
}
