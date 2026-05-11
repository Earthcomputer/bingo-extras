package net.earthcomputer.bingoextras.ext.fantasy;

import com.google.common.base.Preconditions;
import net.earthcomputer.bingoextras.BingoUtil;
import net.earthcomputer.bingoextras.ext.bingo.BingoGameExt;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.scores.PlayerTeam;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;

import java.util.Map;
import java.util.Objects;

public interface PlayerTeamExt_Fantasy {
    Map<ResourceKey<Level>, RuntimeLevelHandle> bingoExtras$getTeamSpecificLevels();

    @SuppressWarnings("deprecation")
    static ServerLevel getTeamSpecificLevel(MinecraftServer server, PlayerTeam team, ResourceKey<Level> dimension) {
        Object bingoGame = BingoUtil.getBingoGame(server);
        return ((PlayerTeamExt_Fantasy) team).bingoExtras$getTeamSpecificLevels().computeIfAbsent(dimension, k -> {
            ServerLevel originalLevel = Objects.requireNonNull(server.getLevel(dimension), () -> "No server level associated with " + dimension);
            Preconditions.checkArgument(ServerLevelExt_Fantasy.getTeam(originalLevel) == null, "Tried to get team specific level of team level %s", dimension);
            RuntimeLevelHandle handle = Fantasy.get(server).openTemporaryLevel(
                new RuntimeLevelConfig()
                    .setDimensionType(originalLevel.dimensionTypeRegistration())
                    .setDifficulty(originalLevel.getDifficulty())
                    .setGenerator(originalLevel.getChunkSource().getGenerator())
                    .setSeed(bingoGame == null ? originalLevel.getSeed() : BingoGameExt.getSeed(bingoGame))
                    .setShouldTickTime(true)
                    .setMirrorOverworldGameRules(true)
                    .setGameTime(originalLevel.getGameTime()) // TODO: is this right?
            );
            ServerLevelExt_Fantasy.initializeTeam(handle.asLevel(), team, originalLevel);
            if (originalLevel.dimension() == Level.END && originalLevel.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
                var dragonFight = EnderDragonFight.createDefault();
                dragonFight.init(handle.asLevel(), handle.asLevel().getSeed(), BlockPos.ZERO);
                handle.asLevel().setDragonFight(dragonFight);
            }
            return handle;
        }).asLevel();
    }
}
