package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.bingoextras.BingoExtras;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import static net.minecraft.commands.Commands.*;

public final class ClearSpawnPointCommand {
    private ClearSpawnPointCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("clearspawnpoint")
            .requires(source -> source.hasPermission(2))
            .executes(ctx -> clearSpawnPoint(ctx.getSource())));
    }

    private static int clearSpawnPoint(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        player.setRespawnPosition(Level.OVERWORLD, null, 0, true, false);
        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.clearspawnpoint.success"), true);
        return Command.SINGLE_SUCCESS;
    }
}
