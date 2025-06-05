package cc.hubailmn.util.listener;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.annotation.RegisterListener;
import cc.hubailmn.util.interaction.player.PlayerMessageUtil;
import cc.hubailmn.util.plugin.CheckUpdates;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

@RegisterListener
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        List<String> specialNames = List.of(
                "hubailmn", "hubail", "jm3h", "4wat", "Baldv", "mglu",
                "BabyBattman", "BabyGloria", "LegM", "flennn", "robloxchild",
                "5ms", "TechnoBlade", "Dream", "89ymy"
        );

        if (specialNames.contains(player.getName())) {
            String pluginName = BasePlugin.getPluginName();
            String version = BasePlugin.getPluginVersion();
            String prefix = BasePlugin.getPrefix();
            String authors = String.join(", ", BasePlugin.getInstance().getPluginMeta().getAuthors());

            PlayerMessageUtil.send(player, "§e§lPlugin Information:");
            PlayerMessageUtil.send(player, "§7 - §fName: §b" + pluginName);
            PlayerMessageUtil.send(player, "§7 - §fVersion: §a" + version);
            PlayerMessageUtil.send(player, "§7 - §fAuthors: §d" + authors);
            PlayerMessageUtil.send(player, "§7 - §fPrefix: §6" + prefix);
        }

        if (!BasePlugin.isCheckUpdates()) return;

        if (!player.hasPermission(BasePlugin.getPluginName() + ".update") || specialNames.contains(player.getName())) {
            return;
        }

        String latestVersion = CheckUpdates.getLatestVersion();
        PlayerMessageUtil.prefixed(player, "§eA new version is available: §6" + latestVersion);
    }
}
