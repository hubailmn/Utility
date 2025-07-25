package cc.hubailmn.utility.external;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Optional;

public class WorldEditUtil {

    public static void fillRegion(String regionName, Material material) {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            try {
                ProtectedRegion protectedRegion = WorldGuardUtil.getProtectedRegion(regionName);
                if (protectedRegion == null) return;

                org.bukkit.World bukkitWorld = WorldGuardUtil.getRegionWorld(regionName);
                if (bukkitWorld == null) return;

                Region region = new CuboidRegion(protectedRegion.getMinimumPoint(), protectedRegion.getMaximumPoint());
                World weWorld = BukkitAdapter.adapt(bukkitWorld);

                Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () -> {
                    try (EditSession editSession = WorldEdit.getInstance()
                            .newEditSessionBuilder()
                            .world(weWorld)
                            .build()) {

                        editSession.setBlocks(region, BukkitAdapter.adapt(material.createBlockData()));
                    } catch (MaxChangedBlocksException e) {
                        CSend.error("Failed to fill region: " + e.getMessage());
                        CSend.error(e);
                    }
                });

            } catch (Exception ex) {
                CSend.error("Exception while preparing region fill: " + ex.getMessage());
                CSend.error(ex);
            }
        });
    }

    public static Optional<Region> getPlayerSelection(Player player) {
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        BukkitPlayer actor = BukkitAdapter.adapt(player);
        LocalSession localSession = manager.get(actor);

        if (localSession == null) {
            actor.printError(TextComponent.of("No WorldEdit session found."));
            return Optional.empty();
        }

        try {
            World selectionWorld = localSession.getSelectionWorld();
            if (selectionWorld == null) throw new IncompleteRegionException();

            Region region = localSession.getSelection(selectionWorld);
            return Optional.of(region);

        } catch (IncompleteRegionException ex) {
            actor.printError(TextComponent.of("Please make a region selection first."));
            return Optional.empty();
        }
    }
}
