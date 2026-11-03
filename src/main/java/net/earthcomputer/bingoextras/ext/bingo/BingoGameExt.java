package net.earthcomputer.bingoextras.ext.bingo;

import io.github.gaming32.bingo.game.BingoGame;

public interface BingoGameExt {
    long bingoExtras$getSeed();
    void bingoExtras$setSeed(long seed);

    static long getSeed(BingoGame bingoGame) {
        return ((BingoGameExt) bingoGame).bingoExtras$getSeed();
    }

    static void setSeed(BingoGame bingoGame, long seed) {
        ((BingoGameExt) bingoGame).bingoExtras$setSeed(seed);
    }
}
