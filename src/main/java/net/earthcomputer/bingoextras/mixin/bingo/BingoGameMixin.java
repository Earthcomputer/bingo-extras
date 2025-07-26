package net.earthcomputer.bingoextras.mixin.bingo;

import net.earthcomputer.bingoextras.ext.bingo.BingoGameExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "io.github.gaming32.bingo.game.BingoGame", remap = false)
@Pseudo
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
