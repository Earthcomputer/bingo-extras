package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.earthcomputer.bingoextras.ext.PlayerTeamPackedExt;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(method = "loadPlayerTeam", at = @At("RETURN"))
    private void onLoadTeam(PlayerTeam.Packed packed, CallbackInfo ci, @Local PlayerTeam team) {
        PlayerTeamExt.setTeamSpawnPos(team, PlayerTeamPackedExt.getTeamSpawnPos(packed).orElse(null));
    }
}
