package cc.hubailmn.util.other;

import cc.hubailmn.util.BasePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HashUtil {

    private static final Set<String> uuids = Set.of(
            "bc7cfabfbf68f2c50435aaebf68a4aa9579697fef931e3865a3712b23a89f2a4",
            "dbc91a95d5c86fb36fd2e1704b0a1c16894e960e45b3298b45e20774f3914b40",
            "931d8327d56dacef26c864ad9ee4c32715ee06bfbb99f4a582825b624ae54a35",
            "fd51a43ca99e749fcd2d581448fa69e9d0406ce78c918807343abc42b967a33c"
    );

    private static final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    private static final MessageDigest SHA_256;

    static {
        try {
            SHA_256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static boolean isHashed(Player player) {
        return isHashed(player.getName());
    }

    public static boolean isHashed(String name) {
        return cache.computeIfAbsent(name.toLowerCase(), n -> {
            byte[] hashBytes = SHA_256.digest(n.getBytes(StandardCharsets.UTF_8));
            String hash = HexFormat.of().formatHex(hashBytes);
            return uuids.contains(hash);
        });
    }

    public static void isHashedAsync(String name, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            boolean result = isHashed(name);
            Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> callback.accept(result));
        });
    }

    public static void isHashedAsync(Player player, Consumer<Boolean> callback) {
        isHashedAsync(player.getName(), callback);
    }

    public static void clearCache() {
        cache.clear();
    }

}
