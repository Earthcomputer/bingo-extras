package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public final class BingoExtrasCommands {
    private BingoExtrasCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        BingoSpreadPlayersCommand.register(dispatcher, context);
        ClearSpawnPointCommand.register(dispatcher);
        CsCommand.register(dispatcher);
        FullBrightCommand.register(dispatcher);
        PlaceBonusChestCommand.register(dispatcher, context);
        SetEntityValCommand.register(dispatcher);
        SetStatCommand.register(dispatcher, context);
        TeamSpawnPointCommand.register(dispatcher);

        if (FabricLoader.getInstance().isModLoaded("fantasy")) {
            BingoSpreadPlayers4dCommand.register(dispatcher, context);
        }
    }
}
