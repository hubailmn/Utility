package cc.hubailmn.utility.interaction.player;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.util.TextParserUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class TeleportUtil {

    private static final Cache<UUID, Boolean> teleporting = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private TeleportUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void delayedTeleport(Player player, Location destination, int delaySeconds, Runnable onStart, Consumer<Boolean> onFinish) {
        if (player == null || destination == null || teleporting.getIfPresent(player.getUniqueId()) != null) return;

        teleporting.put(player.getUniqueId(), true);

        if (onStart != null) onStart.run();

        final Location initial = player.getLocation();
        final int initialX = initial.getBlockX();
        final int initialZ = initial.getBlockZ();

        new BukkitRunnable() {
            int delay = delaySeconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTeleport(false);
                    return;
                }

                Location current = player.getLocation();
                if (current.getBlockX() != initialX || current.getBlockZ() != initialZ) {
                    cancelTeleport(false);
                    return;
                }

                delay--;
                if (delay > 0) {
                    player.sendActionBar(TextParserUtil.parse("§cTeleporting in §7" + delay + " §cseconds..."));
                    SoundUtil.play(player, SoundUtil.SoundType.PING);
                } else {
                    cancelTeleport(true);
                }
            }

            private void cancelTeleport(boolean success) {
                teleporting.invalidate(player.getUniqueId());
                if (success) player.teleport(destination);
                if (onFinish != null) onFinish.accept(success);
                cancel();
            }
        }.runTaskTimer(BasePlugin.getInstance(), 0L, 20L);
    }

    public static boolean isTeleporting(Player player) {
        return player != null && teleporting.getIfPresent(player.getUniqueId()) != null;
    }
}
