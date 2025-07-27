package net.earthcomputer.bingoextras.mixin.bingo;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.bingoextras.BingoUtil;
import net.earthcomputer.bingoextras.command.CsCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(GameModeCommand.class)
public class GameModeCommandMixin {

    @Inject(method = "setMode", at = @At("HEAD"))
    private static void onSetGameMode(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, GameType newGameMode, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null && BingoUtil.isOnRemainingTeam(player) && players.contains(player)) {
            throw CsCommand.PLAYING_BINGO_EXCEPTION.create();
        }
    }
}
