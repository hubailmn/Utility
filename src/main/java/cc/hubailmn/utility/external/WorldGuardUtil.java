package cc.hubailmn.utility.external;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldGuardUtil {

    private static final RegionContainer REGION_CONTAINER = WorldGuard.getInstance().getPlatform().getRegionContainer();
    private static final FlagRegistry FLAG_REGISTRY = WorldGuard.getInstance().getFlagRegistry();

    private static final Map<World, RegionManager> REGION_MANAGER_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, World> REGION_WORLD_CACHE = new ConcurrentHashMap<>();

    public static boolean createRegion(Player player, String regionName) {
        if (player == null || regionName == null || regionName.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<Region> selection = WorldEditUtil.getPlayerSelection(player);
            return selection.filter(blockVector3s -> createRegionFromSelection(player.getWorld(), regionName, blockVector3s)).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean createCuboidRegion(World world, String regionName, Location minLoc, Location maxLoc) {
        if (!validateRegionCreationParams(world, regionName, minLoc, maxLoc)) {
            return false;
        }

        try {
            RegionManager regionManager = getRegionManager(world);
            if (regionManager == null) {
                return false;
            }

            Location actualMin = getMinLocation(minLoc, maxLoc);
            Location actualMax = getMaxLocation(minLoc, maxLoc);

            BlockVector3 min = BlockVector3.at(actualMin.getBlockX(), actualMin.getBlockY(), actualMin.getBlockZ());
            BlockVector3 max = BlockVector3.at(actualMax.getBlockX(), actualMax.getBlockY(), actualMax.getBlockZ());

            ProtectedRegion region = new ProtectedCuboidRegion(regionName, min, max);
            regionManager.addRegion(region);

            REGION_WORLD_CACHE.put(regionName.toLowerCase(), world);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deleteRegion(World world, String regionName) {
        if (world == null || regionName == null || regionName.trim().isEmpty()) {
            return false;
        }

        try {
            RegionManager regionManager = getRegionManager(world);
            if (regionManager != null && regionManager.hasRegion(regionName)) {
                regionManager.removeRegion(regionName);
                REGION_WORLD_CACHE.remove(regionName.toLowerCase());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deleteRegion(String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) {
            return false;
        }

        World world = getRegionWorld(regionName);
        return deleteRegion(world, regionName);
    }

    public static ProtectedRegion getProtectedRegion(String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) {
            return null;
        }

        World cachedWorld = REGION_WORLD_CACHE.get(regionName.toLowerCase());
        if (cachedWorld != null) {
            ProtectedRegion region = getProtectedRegion(cachedWorld, regionName);
            if (region != null) {
                return region;
            } else {
                REGION_WORLD_CACHE.remove(regionName.toLowerCase());
            }
        }

        return searchAllWorldsForRegion(regionName);
    }

    public static ProtectedRegion getProtectedRegion(World world, String regionName) {
        if (world == null || regionName == null || regionName.trim().isEmpty()) {
            return null;
        }

        try {
            RegionManager regionManager = getRegionManager(world);
            return (regionManager != null) ? regionManager.getRegion(regionName) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static World getRegionWorld(String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) {
            return null;
        }

        World cachedWorld = REGION_WORLD_CACHE.get(regionName.toLowerCase());
        if (cachedWorld != null) {
            if (getProtectedRegion(cachedWorld, regionName) != null) {
                return cachedWorld;
            } else {
                REGION_WORLD_CACHE.remove(regionName.toLowerCase());
            }
        }

        for (World world : Bukkit.getWorlds()) {
            if (hasRegion(world, regionName)) {
                REGION_WORLD_CACHE.put(regionName.toLowerCase(), world);
                return world;
            }
        }
        return null;
    }

    public static Set<UUID> getMembers(String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) {
            return new HashSet<>();
        }

        ProtectedRegion region = getProtectedRegion(regionName);
        return region.getMembers().getUniqueIds();
    }

    public static boolean addMember(String regionName, UUID uuid) {
        if (regionName == null || uuid == null) {
            return false;
        }

        try {
            ProtectedRegion region = getProtectedRegion(regionName);
            if (region != null) {
                region.getMembers().addPlayer(uuid);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean removeMember(String regionName, UUID uuid) {
        if (regionName == null || uuid == null) {
            return false;
        }

        try {
            ProtectedRegion region = getProtectedRegion(regionName);
            if (region != null) {
                region.getMembers().removePlayer(uuid);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean removeAllMembers(String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) {
            return false;
        }

        try {
            ProtectedRegion region = getProtectedRegion(regionName);
            if (region != null) {
                region.getMembers().removeAll();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasRegion(World world, String regionName) {
        if (world == null || regionName == null || regionName.trim().isEmpty()) {
            return false;
        }

        try {
            RegionManager regionManager = getRegionManager(world);
            return regionManager != null && regionManager.hasRegion(regionName);
        } catch (Exception e) {
            return false;
        }
    }

    public static void clearCaches() {
        REGION_MANAGER_CACHE.clear();
        REGION_WORLD_CACHE.clear();
    }

    public static Set<ProtectedRegion> getAllRegions(World world) {
        if (world == null) {
            return Collections.emptySet();
        }

        try {
            RegionManager regionManager = getRegionManager(world);
            return regionManager != null ? new HashSet<>(regionManager.getRegions().values()) : Collections.emptySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private static RegionManager getRegionManager(World world) {
        if (world == null) {
            return null;
        }

        RegionManager cached = REGION_MANAGER_CACHE.get(world);
        if (cached != null) {
            return cached;
        }

        try {
            RegionManager manager = REGION_CONTAINER.get(BukkitAdapter.adapt(world));
            if (manager != null) {
                REGION_MANAGER_CACHE.put(world, manager);
            }
            return manager;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean createRegionFromSelection(World world, String regionName, Region selection) {
        RegionManager regionManager = getRegionManager(world);
        if (regionManager == null) {
            return false;
        }

        ProtectedRegion newRegion = new ProtectedCuboidRegion(regionName, selection.getMinimumPoint(), selection.getMaximumPoint());
        regionManager.addRegion(newRegion);

        REGION_WORLD_CACHE.put(regionName.toLowerCase(), world);
        return true;
    }

    private static ProtectedRegion searchAllWorldsForRegion(String regionName) {
        for (World world : Bukkit.getWorlds()) {
            ProtectedRegion region = getProtectedRegion(world, regionName);
            if (region != null) {
                REGION_WORLD_CACHE.put(regionName.toLowerCase(), world);
                return region;
            }
        }
        return null;
    }

    private static boolean validateRegionCreationParams(World world, String regionName, Location minLoc, Location maxLoc) {
        if (world == null) return false;
        if (regionName == null || regionName.trim().isEmpty()) return false;
        if (minLoc == null || maxLoc == null) return false;
        return minLoc.getWorld().equals(world) && maxLoc.getWorld().equals(world);
    }

    public static boolean setFlag(String regionName, StateFlag flag, Boolean value) {
        return setFlag(regionName, flag, value == null ? null : (value ? StateFlag.State.ALLOW : StateFlag.State.DENY));
    }

    public static boolean setFlag(String regionName, StateFlag flag, RegionGroup regionGroup, Boolean value) {
        ProtectedRegion region = getProtectedRegion(regionName);
        if (region == null || flag == null || regionGroup == null) return false;

        try {
            StateFlag.State state = value == null ? null : (value ? StateFlag.State.ALLOW : StateFlag.State.DENY);
            region.setFlag(flag, state);
            region.setFlag(flag.getRegionGroupFlag(), regionGroup);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setFlag(String regionName, StateFlag flag, StateFlag.State state) {
        ProtectedRegion region = getProtectedRegion(regionName);
        if (region == null || flag == null) return false;

        try {
            region.setFlag(flag, state);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setFlags(String regionName, Map<StateFlag, Boolean> flags) {
        ProtectedRegion region = getProtectedRegion(regionName);
        if (region == null || flags == null || flags.isEmpty()) return false;

        boolean allSuccess = true;
        for (Map.Entry<StateFlag, Boolean> entry : flags.entrySet()) {
            StateFlag flag = entry.getKey();
            Boolean value = entry.getValue();
            StateFlag.State state = value == null ? null : (value ? StateFlag.State.ALLOW : StateFlag.State.DENY);

            try {
                region.setFlag(flag, state);
            } catch (Exception e) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    public static Boolean getFlag(String regionName, StateFlag flag) {
        StateFlag.State state = getFlagState(regionName, flag);
        return state == null ? null : (state == StateFlag.State.ALLOW);
    }

    public static StateFlag.State getFlagState(String regionName, StateFlag flag) {
        ProtectedRegion region = getProtectedRegion(regionName);
        if (region == null || flag == null) return null;

        try {
            return region.getFlag(flag);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean removeFlag(String regionName, StateFlag flag) {
        return setFlag(regionName, flag, (StateFlag.State) null);
    }

    public static Map<StateFlag, Boolean> getAllFlags(String regionName) {
        Map<StateFlag, Boolean> result = new HashMap<>();
        Map<StateFlag, StateFlag.State> allStates = getAllFlagStates(regionName);

        for (Map.Entry<StateFlag, StateFlag.State> entry : allStates.entrySet()) {
            result.put(entry.getKey(), entry.getValue() == StateFlag.State.ALLOW);
        }

        return result;
    }

    public static Map<StateFlag, StateFlag.State> getAllFlagStates(String regionName) {
        Map<StateFlag, StateFlag.State> result = new HashMap<>();
        ProtectedRegion region = getProtectedRegion(regionName);
        if (region == null) return result;

        try {
            for (Map.Entry<Flag<?>, Object> entry : region.getFlags().entrySet()) {
                if (entry.getKey() instanceof StateFlag stateFlag && entry.getValue() instanceof StateFlag.State state) {
                    result.put(stateFlag, state);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static boolean isFlagSet(String regionName, StateFlag flag) {
        return getFlagState(regionName, flag) != null;
    }

    private static Location getMinLocation(Location loc1, Location loc2) {
        return new Location(loc1.getWorld(), Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()));
    }

    private static Location getMaxLocation(Location loc1, Location loc2) {
        return new Location(loc1.getWorld(), Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()));
    }

    /**
     * Checks whether a block is in one of the allowed/excluded regions.
     * Lightweight and cache-friendly.
     *
     * @param block          The block to check
     * @param regionIds      The region IDs to check against
     * @param useAsBlacklist True if regionIds should be excluded, false if only regionIds are allowed
     * @return True if the block is "allowed" according to the mode
     */
    public static boolean isBlockInAllowedRegion(Block block, Set<String> regionIds, boolean useAsBlacklist) {
        if (block == null || regionIds == null || regionIds.isEmpty()) return true;

        RegionManager manager = getRegionManager(block.getWorld());
        if (manager == null) return true;

        var applicableRegions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));
        boolean intersects = applicableRegions.getRegions().stream().anyMatch(r -> regionIds.contains(r.getId()));

        return useAsBlacklist != intersects;
    }

    /**
     * Checks whether a player is in one of the allowed/excluded regions.
     * Useful for future player-based checks.
     *
     * @param player         The player to check
     * @param regionIds      The region IDs to check against
     * @param useAsBlacklist True if regionIds should be excluded, false if only regionIds are allowed
     * @return True if the player is "allowed" according to the mode
     */
    public static boolean isPlayerInAllowedRegion(Player player, Set<String> regionIds, boolean useAsBlacklist) {
        if (player == null) return true;
        return isLocationInAllowedRegion(player.getLocation(), regionIds, useAsBlacklist);
    }

    /**
     * Checks whether a location is in one of the allowed/excluded regions.
     * Reusable for both blocks and players.
     */
    public static boolean isLocationInAllowedRegion(Location loc, Set<String> regionIds, boolean useAsBlacklist) {
        if (loc == null || regionIds == null || regionIds.isEmpty()) return true;

        RegionManager manager = getRegionManager(loc.getWorld());
        if (manager == null) return true;

        var applicableRegions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        boolean intersects = applicableRegions.getRegions().stream().anyMatch(r -> regionIds.contains(r.getId()));

        return useAsBlacklist != intersects;
    }

}