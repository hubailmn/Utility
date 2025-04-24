package me.hubailmn.util.menu.type;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.annotation.Menu;
import me.hubailmn.util.menu.interactive.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public abstract class MenuBuilder {

    protected final List<Button> buttons = new ArrayList<>();

    protected Component title;
    protected int size;
    protected boolean buttonClickCancel;
    protected boolean playerInventoryClickCancel;
    protected boolean inventoryClickCancel;

    public MenuBuilder() {
        Menu annotation = this.getClass().getAnnotation(Menu.class);
        if (annotation == null) {
            throw new IllegalStateException("Missing @Menu annotation on " + getClass().getName());
        }

        this.title = Component.text(annotation.title());
        this.size = annotation.rows() * 9;
        this.buttonClickCancel = annotation.buttonClickCancel();
        this.playerInventoryClickCancel = annotation.playerInvClickCancel();
        this.inventoryClickCancel = annotation.inventoryClickCancel();
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


    /**
     * Fills the entire inventory with the given filler ItemStack,
     * except for the slots already occupied by buttons.
     */
    protected void fillInventory(Inventory inventory, ItemStack filler) {
        if (filler == null) return;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public void addButtons(Button... buttons) {
        if (buttons != null) {
            Collections.addAll(this.buttons, buttons);
        }
    }

    /**
     * Called after buttons are placed. Use this to place additional items.
     */
    public abstract void setItems(Inventory inventory);

    /**
     * Called before inventory is created. Use this to register buttons via addButtons(...).
     */
    public abstract void setupButtons(Player player);
}
