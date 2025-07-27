package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.earthcomputer.bingoextras.BingoExtras;
import net.earthcomputer.bingoextras.BingoUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import static net.minecraft.commands.Commands.*;

public final class CsCommand {
    public static final SimpleCommandExceptionType PLAYING_BINGO_EXCEPTION = new SimpleCommandExceptionType(BingoExtras.translatable("bingo_extras.gamemode.playing"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("cs")
            .executes(ctx -> toggleSurvivalSpectator(ctx.getSource())));
    }

    private static int toggleSurvivalSpectator(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (BingoUtil.isOnRemainingTeam(player)) {
            throw PLAYING_BINGO_EXCEPTION.create();
        }

        if (player.isSpectator()) {
            player.setGameMode(GameType.SURVIVAL);
            source.sendSuccess(() -> Component.literal("Set game mode to survival"), true);
        } else {
            player.setGameMode(GameType.SPECTATOR);
            source.sendSuccess(() -> Component.literal("Set game mode to spectator"), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
