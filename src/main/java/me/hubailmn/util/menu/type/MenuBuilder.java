package me.hubailmn.util.menu.type;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.menu.MenuManager;
import me.hubailmn.util.menu.annotation.Menu;
import me.hubailmn.util.menu.interactive.Button;
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

    protected final Player player;
    protected final BasePlugin plugin;
    protected final List<Button> buttons = new ArrayList<>();

    protected String title;
    protected int size;
    protected boolean buttonClickCancel;
    protected boolean playerInventoryClickCancel;
    protected boolean inventoryClickCancel;

    public MenuBuilder(Player player) {
        this.plugin = BasePlugin.getInstance();
        this.player = player;

        Menu annotation = this.getClass().getAnnotation(Menu.class);
        if (annotation == null) {
            throw new IllegalStateException("Missing @Menu annotation on " + getClass().getName());
        }

        this.title = annotation.title();
        this.size = annotation.size();
        this.buttonClickCancel = annotation.buttonClickCancel();
        this.playerInventoryClickCancel = annotation.playerInvClickCancel();
        this.inventoryClickCancel = annotation.inventoryClickCancel();
    }

    protected Inventory createInventory() {
        return Bukkit.createInventory(player, size, title);
    }

    public void display() {
        player.closeInventory();
        Inventory inventory = createInventory();
        setItems(inventory);

        for (Button button : buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        MenuManager.addActiveMenu(player, this);
        player.openInventory(inventory);
    }

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