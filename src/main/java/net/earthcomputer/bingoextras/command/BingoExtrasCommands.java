package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public final class BingoExtrasCommands {
    private BingoExtrasCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        BingoSpreadPlayersCommand.register(dispatcher, context);
        ClearSpawnPointCommand.register(dispatcher);
        FullBrightCommand.register(dispatcher);
        PlaceBonusChestCommand.register(dispatcher, context);
        SetEntityValCommand.register(dispatcher);
        SetStatCommand.register(dispatcher, context);
        TeamSpawnPointCommand.register(dispatcher);
    }
}
