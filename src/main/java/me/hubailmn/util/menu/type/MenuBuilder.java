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

        for (Button button : buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        MenuManager.setActiveMenu(player, this);
        player.openInventory(inventory);
    }

    public abstract void setupButtons(Player player);

    protected void fillInventory(Inventory inventory, ItemStack filler) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    public void addButtons(Button... buttons) {
        Collections.addAll(this.buttons, buttons);
    }

    public abstract void setItems(Inventory inventory);
}