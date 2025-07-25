package net.earthcomputer.bingoextras;

import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class FantasyUtil {
    public static final ThreadLocal<MinecraftServer> currentServer = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isForcedDimensionChange = ThreadLocal.withInitial(() -> false);

    private FantasyUtil() {
    }

    public static ResourceKey<Level> getOriginalDimension(@Nullable MinecraftServer server, ResourceKey<Level> teamDimension) {
        if (server == null) {
            return teamDimension;
        }
        ServerLevel teamLevel = Objects.requireNonNull(server.getLevel(teamDimension), () -> "Could not find server level for dimension " + teamDimension);
        ServerLevel originalLevel = ServerLevelExt_Fantasy.getOriginalLevel(teamLevel);
        if (originalLevel != null) {
            return originalLevel.dimension();
        }
        return teamDimension;
    }

    // Called from mass ASM
    public static Level originalLevelOrSelf(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return level;
        }
        ServerLevel originalLevel = ServerLevelExt_Fantasy.getOriginalLevel(serverLevel);
        return originalLevel != null ? originalLevel : level;
    }

    public static void destroyTeamSpecificLevels(PlayerTeam team) {
        var teamSpecificLevels = ((PlayerTeamExt_Fantasy) team).bingoExtras$getTeamSpecificLevels();
        for (RuntimeWorldHandle handle : teamSpecificLevels.values()) {
            for (ServerPlayer player : new ArrayList<>(handle.asWorld().players())) {
                ServerLevel originalLevel = Objects.requireNonNull(ServerLevelExt_Fantasy.getOriginalLevel(handle.asWorld()), "No original level for team specific world");
                BlockPos spawnPoint = originalLevel.getSharedSpawnPos();
                forceDimensionChange(() -> player.teleportTo(originalLevel, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true));
            }
            handle.delete();
        }
        teamSpecificLevels.clear();
    }

    public static void forceDimensionChange(Runnable action) {
        boolean wasForcedDimensionChange = isForcedDimensionChange.get();
        isForcedDimensionChange.set(true);
        try {
            action.run();
        } finally {
            isForcedDimensionChange.set(wasForcedDimensionChange);
        }
    }

    public static <T> T forceDimensionChange(Supplier<T> action) {
        boolean wasForcedDimensionChange = isForcedDimensionChange.get();
        isForcedDimensionChange.set(true);
        try {
            return action.get();
        } finally {
            isForcedDimensionChange.set(wasForcedDimensionChange);
        }
    }

    public static boolean isForcedDimensionChange() {
        return isForcedDimensionChange.get();
    }
}
