package net.earthcomputer.bingoextras.ext;

import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.Map;
import java.util.Objects;

public interface PlayerTeamExt {
    @Nullable
    GlobalPos bingoExtras$getTeamSpawnPos();

    void bingoExtras$setTeamSpawnPos(@Nullable GlobalPos pos);

    @Nullable
    static GlobalPos getTeamSpawnPos(PlayerTeam team) {
        return ((PlayerTeamExt) team).bingoExtras$getTeamSpawnPos();
    }

    static void setTeamSpawnPos(PlayerTeam team, @Nullable GlobalPos pos) {
        ((PlayerTeamExt) team).bingoExtras$setTeamSpawnPos(pos);
    }
}
