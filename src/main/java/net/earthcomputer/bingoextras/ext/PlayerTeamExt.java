package net.earthcomputer.bingoextras.ext;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

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
