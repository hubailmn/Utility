package cc.hubailmn.utility.menu.type;

import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.item.ItemBuilder;
import cc.hubailmn.utility.menu.MenuLayout;
import cc.hubailmn.utility.menu.interactive.GuiSlotButton;
import cc.hubailmn.utility.menu.interactive.GuiElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class ScrollableMenuBuilder extends MenuBuilder {

    protected final List<GuiElement> items = new ArrayList<>();
    protected final Map<Integer, GuiElement> renderedItems = new HashMap<>();
    protected List<Integer> contentSlots = new ArrayList<>();
    protected int scrollOffset = 0;

    protected GuiSlotButton scrollBackButton;
    protected GuiSlotButton scrollNextButton;

    public ScrollableMenuBuilder() {
        super();
        setScrollArea(1, 1, (size / 9) - 1, 9);

        scrollBackButton = new GuiSlotButton(MenuLayout.getSlot(4, size / 9), new ItemBuilder()
                .material(Material.ARROW)
                .name("§eScroll Back")
                .build(), player -> {
            if (scrollOffset > 0) {
                scrollBy(-1);
                SoundUtil.play(player, SoundUtil.SoundType.PAGE_FLIP);
            }
        }, true);

        scrollNextButton = new GuiSlotButton(MenuLayout.getSlot(6, size / 9), new ItemBuilder()
                .material(Material.ARROW)
                .name("§eScroll Forward")
                .build(), player -> {
            int maxOffset = Math.max(0, items.size() - contentSlots.size());
            if (scrollOffset < maxOffset) {
                scrollBy(1);
                SoundUtil.play(player, SoundUtil.SoundType.PAGE_FLIP);
            }
        }, true);
    }

    public void setScrollArea(int startRow, int startCol, int endRow, int endCol) {
        contentSlots.clear();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                int slot = MenuLayout.getSlot(col, row);
                if (slot >= 0 && slot < size) {
                    contentSlots.add(slot);
                }
            }
        }
    }

    public void addItems(GuiElement... items) {
        if (items != null) Collections.addAll(this.items, items);
    }

    public void scrollBy(int amount) {
        int maxOffset = Math.max(0, items.size() - contentSlots.size());
        int newOffset = Math.max(0, Math.min(scrollOffset + amount, maxOffset));
        if (newOffset == scrollOffset) return;
        scrollOffset = newOffset;
        renderVisibleItems();
    }

    protected void renderVisibleItems() {
        Inventory inv = inventory;
        if (inv == null) return;

        inv.clear();
        buttons.clear();
        renderedItems.clear();

        setupButtons();
        setItems(inv);

        int max = Math.min(scrollOffset + contentSlots.size(), items.size());
        for (int i = scrollOffset; i < max; i++) {
            int slot = contentSlots.get(i - scrollOffset);
            GuiElement item = items.get(i);
            renderedItems.put(slot, item);
            inv.setItem(slot, item.getItem());
        }

        if (scrollOffset > 0) {
            inv.setItem(scrollBackButton.getSlot(), scrollBackButton.getItem());
            addButtons(scrollBackButton);
        }

        if (scrollOffset + contentSlots.size() < items.size()) {
            inv.setItem(scrollNextButton.getSlot(), scrollNextButton.getItem());
            addButtons(scrollNextButton);
        }

        for (GuiSlotButton button : buttons.values()) {
            if (button.getSlot() >= 0 && button.getSlot() < inv.getSize()) {
                inv.setItem(button.getSlot(), button.getItem());
            }
        }
    }

    public GuiElement getInteractiveItemBySlot(int slot) {
        return renderedItems.get(slot);
    }

    @Override
    protected void afterDisplay(Inventory inventory) {
        scrollOffset = 0;
        renderVisibleItems();
    }

    public abstract void setupButtons();

    public abstract void setItems(Inventory inventory);
}
