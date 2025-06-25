package net.earthcomputer.bingoextras.ext;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Optional;

public interface PlayerTeamPackedExt {
    Optional<GlobalPos> bingoExtras$getTeamSpawnPos();

    void bingoExtras$setTeamSpawnPos(Optional<GlobalPos> pos);

    static Optional<GlobalPos> getTeamSpawnPos(PlayerTeam.Packed packed) {
        return ((PlayerTeamPackedExt) (Object) packed).bingoExtras$getTeamSpawnPos();
    }

    static void setTeamSpawnPos(PlayerTeam.Packed packed, Optional<GlobalPos> pos) {
        ((PlayerTeamPackedExt) (Object) packed).bingoExtras$setTeamSpawnPos(pos);
    }
}
