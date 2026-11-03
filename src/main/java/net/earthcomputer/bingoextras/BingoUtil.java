package net.earthcomputer.bingoextras;

import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.game.BingoGame;
import net.earthcomputer.bingoextras.mixin.bingo.BingoGameAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class BingoUtil {
    private static final boolean BINGO_LOADED = FabricLoader.getInstance().isModLoaded("bingo");

    private BingoUtil() {
    }

    @Nullable
    public static BingoGame getBingoGame(MinecraftServer server) {
        if (!BINGO_LOADED) {
            return null;
        }
        return ((MinecraftServerExt) server).bingo$getGame();
    }

    public static boolean isOnRemainingTeam(ServerPlayer player) {
        BingoGame bingoGame = getBingoGame(player.level().getServer());
        if (bingoGame == null) {
            return false;
        }

        return ((BingoGameAccessor) bingoGame).getRemainingTeams().and(bingoGame.getTeam(player));
    }
}
