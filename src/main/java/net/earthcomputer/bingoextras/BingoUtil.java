package net.earthcomputer.bingoextras;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class BingoUtil {
    private static final Class<?> CLS_BINGO_GAME = findClass("io.github.gaming32.bingo.game.BingoGame");
    private static final Class<?> CLS_TEAMS = findClass("io.github.gaming32.bingo.game.BingoBoard$Teams");
    private static final Field FD_BINGO_GAME = findField(MinecraftServer.class, "bingo$game");
    private static final Field FD_REMAINING_TEAMS = findField(CLS_BINGO_GAME, "remainingTeams");
    private static final Method MD_GET_TEAM = findMethod(CLS_BINGO_GAME, "getTeam", ServerPlayer.class);
    private static final Method MD_AND = findMethod(CLS_TEAMS, "and", CLS_TEAMS);

    private BingoUtil() {
    }

    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, paramTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @Nullable
    public static Object getBingoGame(MinecraftServer server) {
        try {
            return FD_BINGO_GAME.get(server);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static boolean isOnRemainingTeam(ServerPlayer player) {
        Object bingoGame = getBingoGame(player.getServer());
        if (bingoGame == null) {
            return false;
        }

        try {
            Object remainingTeams = FD_REMAINING_TEAMS.get(bingoGame);
            Object team = MD_GET_TEAM.invoke(bingoGame, player);
            return (Boolean) MD_AND.invoke(remainingTeams, team);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
