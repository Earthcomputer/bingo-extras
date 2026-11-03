package net.earthcomputer.bingoextras.mixin.bingo;

import io.github.gaming32.bingo.game.BingoGame;
import net.earthcomputer.bingoextras.ext.bingo.BingoGameExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BingoGame.class)
public class BingoGameMixin implements BingoGameExt {
    @Unique
    private long seed;

    @Override
    public long bingoExtras$getSeed() {
        return seed;
    }

    @Override
    public void bingoExtras$setSeed(long seed) {
        this.seed = seed;
    }
}
