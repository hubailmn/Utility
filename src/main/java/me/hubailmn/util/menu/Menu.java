package me.hubailmn.util.menu;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.menu.interactive.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Menu {

    public static final String MENU_METADATA_KEY = BasePlugin.getPluginName().toLowerCase() + "_activeMenu";
    public final Player player;
    private final BasePlugin instance;
    private final List<Button> buttons = new ArrayList<>();
    private String title;
    private int size;
    private boolean buttonClickCancel = true;
    private boolean PlayerInventoryClickCancel = true;

    public Menu(Player player) {
        this.instance = BasePlugin.getInstance();
        this.player = player;
    }

    public static Menu getActiveMenu(Player player) {
        if (player.hasMetadata(MENU_METADATA_KEY)) {
            return (Menu) player.getMetadata(MENU_METADATA_KEY).get(0).value();
        }
        return null;
    }

    public static void clearActiveMenu(Player player) {
        if (player.hasMetadata(MENU_METADATA_KEY) && BasePlugin.getInstance() != null) {
            player.removeMetadata(MENU_METADATA_KEY, BasePlugin.getInstance());
            player.closeInventory();
        }
    }

    public void setItems(Inventory inventory) {

    }

    protected void addButtons(Button... buttons) {
        Collections.addAll(this.buttons, buttons);
    }

    protected void addButton(Button buttons) {
        this.buttons.add(buttons);
    }

    public void display() {
        player.closeInventory();

        Inventory inventory = Bukkit.createInventory(player, size, title);

        setItems(inventory);

        for (Button button : buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        player.setMetadata(MENU_METADATA_KEY, new FixedMetadataValue(instance, this));
        player.openInventory(inventory);
    }

}
