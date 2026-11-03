package net.earthcomputer.bingoextras.mixin.fantasy.bingo;

import com.mojang.brigadier.context.CommandContext;
import io.github.gaming32.bingo.BingoCommand;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BingoCommand.class)
public class BingoCommandMixin {
    @Inject(method = "resetGame", at = @At("RETURN"))
    private static void onResetGame(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> cir) {
        for (PlayerTeam team : context.getSource().getServer().getScoreboard().getPlayerTeams()) {
            FantasyUtil.destroyTeamSpecificLevels(team);
        }
    }
}
