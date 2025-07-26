package net.earthcomputer.bingoextras.ext.bingo;

public interface BingoGameExt {
    long bingoExtras$getSeed();
    void bingoExtras$setSeed(long seed);

    static long getSeed(Object bingoGame) {
        return ((BingoGameExt) bingoGame).bingoExtras$getSeed();
    }

    static void setSeed(Object bingoGame, long seed) {
        ((BingoGameExt) bingoGame).bingoExtras$setSeed(seed);
    }
}
