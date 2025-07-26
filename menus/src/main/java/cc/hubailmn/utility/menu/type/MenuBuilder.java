package cc.hubailmn.utility.menu.type;

import cc.hubailmn.utility.menu.MenuInventoryHolder;
import cc.hubailmn.utility.menu.annotation.Menu;
import cc.hubailmn.utility.menu.interactive.GuiSlotButton;
import cc.hubailmn.utility.util.TextParserUtil;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class MenuBuilder {

    protected final Map<Integer, GuiSlotButton> buttons = new HashMap<>();
    protected Component title;
    protected int size;
    protected boolean inventoryClickCancelled;
    protected boolean menuClickCancelled;
    protected Inventory inventory;
    protected Player player;

    public MenuBuilder() {
        Menu annotation = this.getClass().getAnnotation(Menu.class);
        if (annotation == null) {
            throw new IllegalStateException("Missing @Menu annotation on " + getClass().getName());
        }

        this.title = TextParserUtil.parse(annotation.title());
        this.size = annotation.rows() * 9;
        this.inventoryClickCancelled = annotation.inventoryClickCancelled();
        this.menuClickCancelled = annotation.menuClickCancelled();
    }

    public void display(Player player) {
        this.player = player;
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(new MenuInventoryHolder(this), size, title);
        }

        buttons.clear();

        setItems(inventory);
        setupButtons();

        for (GuiSlotButton button : buttons.values()) {
            int slot = button.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, button.getItem());
            }
        }

        afterDisplay(inventory);

        if (!player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.openInventory(inventory);
        }
    }

    public void addButtons(GuiSlotButton... buttons) {
        if (buttons != null) {
            for (GuiSlotButton button : buttons) {
                this.buttons.put(button.getSlot(), button);
            }
        }
    }

    protected void afterDisplay(Inventory inventory) {
    }

    public void onClose(Player player) {
    }

    public abstract void setupButtons();

    public abstract void setItems(Inventory inventory);
}