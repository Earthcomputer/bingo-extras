package net.earthcomputer.bingoextras.mixin.bingo;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.BingoCommand;
import io.github.gaming32.bingo.game.BingoGame;
import net.earthcomputer.bingoextras.ext.bingo.BingoGameExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BingoCommand.class)
public class BingoCommandMixin {
    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lio/github/gaming32/bingo/ext/MinecraftServerExt;bingo$setGame(Lio/github/gaming32/bingo/game/BingoGame;)V", shift = At.Shift.AFTER))
    private static void injectSeed(CallbackInfoReturnable<Integer> cir, @Local(name = "game") BingoGame game, @Local(name = "seed") long seed) {
        BingoGameExt.setSeed(game, seed);
    }
}
