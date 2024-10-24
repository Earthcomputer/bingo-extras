package net.earthcomputer.bingoextras.ext;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPlayerExt {
    boolean bingoExtras$isFullbright();
    void bingoExtras$setFullbright(boolean fullbright);

    static boolean isFullbright(ServerPlayer player) {
        return ((ServerPlayerExt) player).bingoExtras$isFullbright();
    }

    static void setFullbright(ServerPlayer player, boolean fullbright) {
        ((ServerPlayerExt) player).bingoExtras$setFullbright(fullbright);
    }
}
