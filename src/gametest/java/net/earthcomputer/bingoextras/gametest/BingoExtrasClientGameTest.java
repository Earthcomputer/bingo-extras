package net.earthcomputer.bingoextras.gametest;

import io.github.gaming32.bingo.game.BingoGame;
import net.earthcomputer.bingoextras.BingoUtil;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class BingoExtrasClientGameTest implements FabricClientGameTest {
    static final String TEAM = "red";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            server.runCommand("function bingo:init");
            server.runCommand("team join " + TEAM + " @a");

            testSetup(singleplayer, "very_easy", 5);
            testSetup(singleplayer, "easy", 5);
            testSetup(singleplayer, "medium", 5);
            testSetup(singleplayer, "hard", 5);
            testSetup(singleplayer, "very_hard", 5);
            testSetup(singleplayer, "blitz", 3);

            testFantasySetup(context, singleplayer);
        }
    }

    private static void testSetup(TestSingleplayerContext singleplayer, String setup, int expectedSize) {
        runSetup(singleplayer, setup, expectedSize);

        singleplayer.getServer().runOnServer(server -> {
            ServerPlayer player = singleplayer.getConnection().getServerPlayer();
            check(player.level().dimension() == Level.OVERWORLD, "setup_%s: player should be in the overworld, but was in %s", setup, player.level().dimension());
            GlobalPos spawnPos = getTeamSpawnPos(server);
            check(spawnPos.dimension() == Level.OVERWORLD, "setup_%s: team spawn point should be in the overworld, but was in %s", setup, spawnPos.dimension());
        });
    }

    private static void testFantasySetup(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        TestServerContext server = singleplayer.getServer();
        server.runCommand("data modify storage bingo:vars consistent set value \"true\"");
        runSetup(singleplayer, "medium", 5);

        ResourceKey<Level> teamDimension = server.computeOnServer(s -> {
            ServerPlayer player = singleplayer.getConnection().getServerPlayer();
            ServerLevel level = player.level();
            PlayerTeam team = getTeam(s);
            check(ServerLevelExt_Fantasy.getTeam(level) == team, "consistent setup: player should be in a level for team %s, but was in %s", TEAM, level.dimension());
            check(ServerLevelExt_Fantasy.getOriginalLevel(level) == s.overworld(), "consistent setup: player's level %s should be a copy of the overworld", level.dimension());
            check(((PlayerTeamExt_Fantasy) team).bingoExtras$getTeamSpecificLevels().get(Level.OVERWORLD).asLevel() == level, "consistent setup: player's level %s is not the team's overworld", level.dimension());
            GlobalPos spawnPos = getTeamSpawnPos(s);
            check(spawnPos.dimension() == level.dimension(), "consistent setup: team spawn point should be in %s, but was in %s", level.dimension(), spawnPos.dimension());
            return level.dimension();
        });

        context.waitFor(client -> client.level != null && client.level.dimension() == teamDimension);
        singleplayer.getConnection().waitForChunksDownload();

        new FantasyTests(context, singleplayer).run();

        // resetting the game should send the player back to the real overworld
        server.runCommand("bingo reset");
        server.runOnServer(s -> {
            ServerPlayer player = singleplayer.getConnection().getServerPlayer();
            check(player.level() == s.overworld(), "bingo reset: player should be back in the overworld, but was in %s", player.level().dimension());
            check(((PlayerTeamExt_Fantasy) getTeam(s)).bingoExtras$getTeamSpecificLevels().isEmpty(), "bingo reset: team specific levels should have been deleted");
        });
        context.waitFor(client -> client.level != null && client.level.dimension() == Level.OVERWORLD);
    }

    private static void runSetup(TestSingleplayerContext singleplayer, String setup, int expectedSize) {
        TestServerContext server = singleplayer.getServer();
        BingoGame previousGame = server.computeOnServer(BingoUtil::getBingoGame);
        server.runCommand("function bingo:setup_" + setup);
        // bingo:setup schedules the rest of the setup, which starts the game, for 2 ticks later
        server.waitFor(s -> {
            BingoGame game = BingoUtil.getBingoGame(s);
            return game != null && game != previousGame;
        });
        server.runOnServer(s -> {
            int size = Objects.requireNonNull(BingoUtil.getBingoGame(s)).getBoard().getSize();
            check(size == expectedSize, "setup_%s: expected board size %d, got %d", setup, expectedSize, size);
        });
        singleplayer.getConnection().waitForChunksDownload();
    }

    static PlayerTeam getTeam(MinecraftServer server) {
        return Objects.requireNonNull(server.getScoreboard().getPlayerTeam(TEAM), "Team " + TEAM + " does not exist");
    }

    static GlobalPos getTeamSpawnPos(MinecraftServer server) {
        @Nullable GlobalPos spawnPos = PlayerTeamExt.getTeamSpawnPos(getTeam(server));
        check(spawnPos != null, "team spawn point was not set");
        return spawnPos;
    }

    static void check(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new AssertionError(message.formatted(args));
        }
    }
}
