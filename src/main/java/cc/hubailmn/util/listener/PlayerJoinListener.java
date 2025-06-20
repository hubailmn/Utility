package cc.hubailmn.util.listener;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.annotation.RegisterListener;
import cc.hubailmn.util.interaction.player.PlayerMessageUtil;
import cc.hubailmn.util.other.HashUtil;
import cc.hubailmn.util.plugin.CheckUpdates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RegisterListener
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            boolean isHashed = HashUtil.isHashed(player.getName());
            String latestVersion = null;

            if (BasePlugin.isCheckUpdates() && player.hasPermission(BasePlugin.getPluginName() + ".update") && isHashed) {
                latestVersion = CheckUpdates.getLatestVersion();
            }

            final String versionToAnnounce = latestVersion;

            Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> {
                if (!player.isOnline()) return;

                if (isHashed) {
                    String pluginName = BasePlugin.getPluginName();
                    String version = BasePlugin.getPluginVersion();
                    String prefix = BasePlugin.getPrefix();
                    String authors = String.join(", ", BasePlugin.getInstance().getPluginMeta().getAuthors());

                    Component info = Component.text("Plugin Information:", NamedTextColor.YELLOW, TextDecoration.BOLD)
                            .append(Component.newline())
                            .append(Component.text(" - Name: ", NamedTextColor.GRAY))
                            .append(Component.text(pluginName, NamedTextColor.AQUA))
                            .append(Component.newline())
                            .append(Component.text(" - Version: ", NamedTextColor.GRAY))
                            .append(Component.text(version, NamedTextColor.GREEN))
                            .append(Component.newline())
                            .append(Component.text(" - Authors: ", NamedTextColor.GRAY))
                            .append(Component.text(authors, NamedTextColor.LIGHT_PURPLE))
                            .append(Component.newline())
                            .append(Component.text(" - Prefix: ", NamedTextColor.GRAY))
                            .append(Component.text(prefix, NamedTextColor.GOLD));

                    PlayerMessageUtil.send(player, info);
                }

                if (versionToAnnounce != null) {
                    Component update = Component.text()
                            .append(Component.text("A new version is available: ", NamedTextColor.YELLOW))
                            .append(Component.text(versionToAnnounce, NamedTextColor.GOLD))
                            .build();

                    PlayerMessageUtil.prefixed(player, update);
                }
            });
        });
    }
}
