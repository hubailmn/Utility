package cc.hubailmn.utility.util;

import cc.hubailmn.utility.BasePlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
public class HashUtil {

    private final Set<String> uuids = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();
    private final MessageDigest SHA_256;

    public HashUtil() {
        try {
            SHA_256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }

        uuids.add("bc7cfabfbf68f2c50435aaebf68a4aa9579697fef931e3865a3712b23a89f2a4");
        uuids.add("dbc91a95d5c86fb36fd2e1704b0a1c16894e960e45b3298b45e20774f3914b40");
        uuids.add("931d8327d56dacef26c864ad9ee4c32715ee06bfbb99f4a582825b624ae54a35");
        uuids.add("fd51a43ca99e749fcd2d581448fa69e9d0406ce78c918807343abc42b967a33c");
    }

    public boolean isHashed(Player player) {
        return isHashed(player.getName());
    }

    public boolean isHashed(String name) {
        return cache.computeIfAbsent(name.toLowerCase(), n -> {
            byte[] hashBytes = SHA_256.digest(n.getBytes(StandardCharsets.UTF_8));
            String hash = HexFormat.of().formatHex(hashBytes);
            return uuids.contains(hash);
        });
    }

    public void isHashedAsync(String name, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            boolean result = isHashed(name);
            Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> callback.accept(result));
        });
    }

    public void isHashedAsync(Player player, Consumer<Boolean> callback) {
        isHashedAsync(player.getName(), callback);
    }

    public boolean add(String name) {
        byte[] hashBytes = SHA_256.digest(name.toLowerCase().getBytes(StandardCharsets.UTF_8));
        String hash = HexFormat.of().formatHex(hashBytes);
        return uuids.add(hash);
    }

    public boolean remove(String name) {
        byte[] hashBytes = SHA_256.digest(name.toLowerCase().getBytes(StandardCharsets.UTF_8));
        String hash = HexFormat.of().formatHex(hashBytes);
        return uuids.remove(hash);
    }

    public void clearCache() {
        cache.clear();
    }

}
