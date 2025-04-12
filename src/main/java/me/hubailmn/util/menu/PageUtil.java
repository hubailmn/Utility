package me.hubailmn.util.menu;

import me.hubailmn.util.BasePlugin;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class PageUtil {

    private final String name;

    public PageUtil(String name) {
        this.name = name;
    }

    private String getMetadataKey() {
        return BasePlugin.getInstance().getName() + "." + name;
    }

    public void setPlayerPage(Player player, int page) {
        if (player == null) {
            return;
        }

        if (player.hasMetadata(getMetadataKey())) {
            player.removeMetadata(getMetadataKey(), BasePlugin.getInstance());
        }

        player.setMetadata(getMetadataKey(), new FixedMetadataValue(BasePlugin.getInstance(), page));
    }

    public int getPage(Player player) {
        if (player == null) {
            return 1;
        }

        if (player.hasMetadata(getMetadataKey())) {
            List<MetadataValue> page = player.getMetadata(getMetadataKey());

            if (page.isEmpty()) {
                return 1;
            }

            return page.get(0).asInt();
        }

        return 1;
    }

    public void addPage(Player player) {
        setPlayerPage(player, getPage(player) + 1);
    }

    public void subtractPage(Player player) {
        setPlayerPage(player, getPage(player) - 1);
    }
}
