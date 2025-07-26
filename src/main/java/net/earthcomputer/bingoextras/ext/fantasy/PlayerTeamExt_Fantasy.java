package net.earthcomputer.bingoextras.ext.fantasy;

import com.google.common.base.Preconditions;
import net.earthcomputer.bingoextras.BingoUtil;
import net.earthcomputer.bingoextras.ext.bingo.BingoGameExt;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.scores.PlayerTeam;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.Map;
import java.util.Objects;

public interface PlayerTeamExt_Fantasy {
    Map<ResourceKey<Level>, RuntimeWorldHandle> bingoExtras$getTeamSpecificLevels();

    @SuppressWarnings("deprecation")
    static ServerLevel getTeamSpecificLevel(MinecraftServer server, PlayerTeam team, ResourceKey<Level> dimension) {
        Object bingoGame = BingoUtil.getBingoGame(server);
        return ((PlayerTeamExt_Fantasy) team).bingoExtras$getTeamSpecificLevels().computeIfAbsent(dimension, k -> {
            ServerLevel originalLevel = Objects.requireNonNull(server.getLevel(dimension), () -> "No server level associated with " + dimension);
            Preconditions.checkArgument(ServerLevelExt_Fantasy.getTeam(originalLevel) == null, "Tried to get team specific level of team level %s", dimension);
            RuntimeWorldHandle handle = Fantasy.get(server).openTemporaryWorld(
                new RuntimeWorldConfig()
                    .setDimensionType(originalLevel.dimensionTypeRegistration())
                    .setDifficulty(originalLevel.getDifficulty())
                    .setGenerator(originalLevel.getChunkSource().getGenerator())
                    .setSeed(bingoGame == null ? originalLevel.getSeed() : BingoGameExt.getSeed(bingoGame))
                    .setShouldTickTime(true)
                    .setMirrorOverworldGameRules(true)
                    .setTimeOfDay(originalLevel.dayTime())
            );
            ServerLevelExt_Fantasy.initializeTeam(handle.asWorld(), team, originalLevel);
            if (originalLevel.dimension() == Level.END && originalLevel.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
                handle.asWorld().setDragonFight(new EndDragonFight(handle.asWorld(), originalLevel.getSeed(), EndDragonFight.Data.DEFAULT));
            }
            return handle;
        }).asWorld();
    }
}
