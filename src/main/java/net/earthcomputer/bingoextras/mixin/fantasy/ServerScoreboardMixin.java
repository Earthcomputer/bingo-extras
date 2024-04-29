package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "onTeamRemoved", at = @At("RETURN"))
    private void onTeamRemoved(PlayerTeam team, CallbackInfo ci) {
        FantasyUtil.destroyTeamSpecificLevels(team);
    }
}
