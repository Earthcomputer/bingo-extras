package net.earthcomputer.bingoextras.mixin.fantasy.bingo;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "io.github.gaming32.bingo.BingoCommand", remap = false)
@Pseudo
public class BingoCommandMixin {
    @Shadow
    @Final
    private PlayerTeam[] teams;

    @Inject(method = "resetGame", at = @At("RETURN"))
    private void onEndGame(CallbackInfo ci) {
        for (PlayerTeam team : teams) {
            FantasyUtil.destroyTeamSpecificLevels(team);
        }
    }
}
