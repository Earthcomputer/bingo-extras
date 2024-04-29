package net.earthcomputer.bingoextras.ext.fantasy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public interface ServerLevelExt_Fantasy {
    @Nullable
    PlayerTeam bingoExtras$getTeam();

    void bingoExtras$setTeam(@Nullable PlayerTeam team);

    @Nullable
    ServerLevel bingoExtras$getOriginalLevel();

    void bingoExtras$setOriginalLevel(@Nullable ServerLevel level);

    @Nullable
    static PlayerTeam getTeam(ServerLevel level) {
        return ((ServerLevelExt_Fantasy) level).bingoExtras$getTeam();
    }

    @Nullable
    static ServerLevel getOriginalLevel(ServerLevel level) {
        return ((ServerLevelExt_Fantasy) level).bingoExtras$getOriginalLevel();
    }

    static void initializeTeam(ServerLevel level, @Nullable PlayerTeam team, @Nullable ServerLevel originalLevel) {
        ServerLevelExt_Fantasy ext = (ServerLevelExt_Fantasy) level;
        ext.bingoExtras$setTeam(team);
        ext.bingoExtras$setOriginalLevel(originalLevel);
    }
}
