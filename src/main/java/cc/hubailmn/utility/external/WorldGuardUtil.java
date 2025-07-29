package cc.hubailmn.utility.external;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

public class WorldGuardUtil {

    private static final RegionContainer REGION_CONTAINER = WorldGuard.getInstance().getPlatform().getRegionContainer();

    public static void createRegion(Player player, String regionName) {
        Optional<Region> selection = WorldEditUtil.getPlayerSelection(player);
        if (selection.isEmpty()) {
            return;
        }

        RegionManager regionManager = REGION_CONTAINER.get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) return;

        ProtectedRegion newRegion = new ProtectedCuboidRegion(regionName, selection.get().getMinimumPoint(), selection.get().getMaximumPoint());
        regionManager.addRegion(newRegion);

    }

    public static void createCuboidRegion(World bukkitWorld, String regionName, Location minLoc, Location maxLoc) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        RegionManager regionManager = container.get(weWorld);
        if (regionManager == null) return;

        BlockVector3 min = BlockVector3.at(minLoc.getBlockX(), minLoc.getBlockY(), minLoc.getBlockZ());
        BlockVector3 max = BlockVector3.at(maxLoc.getBlockX(), maxLoc.getBlockY(), maxLoc.getBlockZ());

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, min, max);
        regionManager.addRegion(region);

    }

    public static void deleteRegion(World world, String regionName) {
        RegionManager regionManager = REGION_CONTAINER.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return;

        if (regionManager.hasRegion(regionName)) {
            regionManager.removeRegion(regionName);
        }
    }

    public static void deleteRegion(String regionName) {
        World world = getRegionWorld(regionName);
        if (world != null) {
            deleteRegion(world, regionName);
        }
    }

    public static ProtectedRegion getProtectedRegion(String regionName) {
        for (World world : Bukkit.getWorlds()) {
            ProtectedRegion region = getProtectedRegion(world, regionName);
            if (region != null) return region;
        }
        return null;
    }

    public static ProtectedRegion getProtectedRegion(World world, String regionName) {
        RegionManager regionManager = REGION_CONTAINER.get(BukkitAdapter.adapt(world));
        return (regionManager != null) ? regionManager.getRegion(regionName) : null;
    }

    public static World getRegionWorld(String regionName) {
        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = REGION_CONTAINER.get(BukkitAdapter.adapt(world));
            if (regionManager != null && regionManager.hasRegion(regionName)) {
                return world;
            }
        }
        return null;
    }
}
