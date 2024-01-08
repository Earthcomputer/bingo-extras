package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.bingoextras.BingoExtras;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.Optionull;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.TeamArgument.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;

public final class TeamSpawnPointCommand {
    private TeamSpawnPointCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("teamspawnpoint")
            .requires(source -> source.hasPermission(2))
            .then(argument("team", team())
                .executes(ctx -> setTeamSpawnPoint(ctx.getSource(), getTeam(ctx, "team"), null))
                .then(argument("pos", blockPos())
                    .executes(ctx -> setTeamSpawnPoint(ctx.getSource(), getTeam(ctx, "team"), getBlockPos(ctx, "pos"))))));
    }

    private static int setTeamSpawnPoint(CommandSourceStack source, PlayerTeam team, @Nullable BlockPos pos) {
        PlayerTeamExt.setTeamSpawnPos(team, Optionull.map(pos, p -> GlobalPos.of(source.getLevel().dimension(), p)));
        MinecraftServer server = source.getServer();
        ServerLevel overworld = server.overworld();
        // this packet will be modified to update all players of their team spawn point
        server.getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(overworld.getSharedSpawnPos(), overworld.getSharedSpawnAngle()));
        if (pos != null) {
            source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.teamspawnpoint.set.success", team.getFormattedDisplayName(), pos.getX(), pos.getY(), pos.getZ()), true);
        } else {
            source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.teamspawnpoint.clear.success", team.getFormattedDisplayName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}
