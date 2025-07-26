package net.earthcomputer.bingoextras.mixin.bingo;

import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.BingoUtil;
import net.earthcomputer.bingoextras.ext.bingo.BingoGameExt;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "io.github.gaming32.bingo.BingoCommand", remap = false)
@Pseudo
public class BingoCommandMixin {
    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lio/github/gaming32/bingo/ext/MinecraftServerExt;bingo$setGame(Lio/github/gaming32/bingo/game/BingoGame;)V", shift = At.Shift.AFTER))
    private static void injectSeed(CallbackInfoReturnable<Integer> cir, @Local MinecraftServer server, @Local long seed) {
        BingoGameExt.setSeed(BingoUtil.getBingoGame(server), seed);
    }
}
