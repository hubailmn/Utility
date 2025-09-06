package cc.hubailmn.utility.listener;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.RegisterListener;
import cc.hubailmn.utility.interaction.player.MessageUtil;
import cc.hubailmn.utility.interaction.player.PlayerUtil;
import cc.hubailmn.utility.plugin.CheckUpdates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RegisterListener
public class UpdateCheckListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            boolean isHashed = PlayerUtil.hasBypassAccess(player);
            String latestVersion = null;

            if ((BasePlugin.getInstance().isCheckUpdates() || BasePlugin.getInstance().getPluginConfig().isCheckForUpdates()) && (PlayerUtil.hasPermission(player, BasePlugin.getInstance().getPluginName() + ".update"))) {
                latestVersion = CheckUpdates.getLatestVersion();
            }

            final String versionToAnnounce = latestVersion;

            Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> {
                if (!player.isOnline()) return;

                if (isHashed) {
                    String pluginName = BasePlugin.getInstance().getPluginName();
                    String version = BasePlugin.getInstance().getPluginVersion();
                    String authors = String.join(", ", BasePlugin.getInstance().getPluginMeta().getAuthors());

                    Component info = Component.text("Plugin Information:", NamedTextColor.YELLOW)
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
                            .append(BasePlugin.getPrefix());

                    MessageUtil.send(player, info);
                }

                if (versionToAnnounce != null) {
                    Component update = Component.text()
                            .append(Component.text("A new version is available: ", NamedTextColor.YELLOW))
                            .append(Component.text(versionToAnnounce, NamedTextColor.GOLD))
                            .build();

                    MessageUtil.prefixed(player, update);
                }
            });
        });
    }
}
