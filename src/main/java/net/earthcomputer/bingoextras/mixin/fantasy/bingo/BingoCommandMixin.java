package net.earthcomputer.bingoextras.mixin.fantasy.bingo;

import com.mojang.brigadier.context.CommandContext;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "io.github.gaming32.bingo.BingoCommand", remap = false)
@Pseudo
public class BingoCommandMixin {
    @Inject(method = "resetGame", at = @At("RETURN"))
    private void onEndGame(CommandContext<CommandSourceStack> context, CallbackInfo ci) {
        for (PlayerTeam team : context.getSource().getServer().getScoreboard().getPlayerTeams()) {
            FantasyUtil.destroyTeamSpecificLevels(team);
        }
    }
}
