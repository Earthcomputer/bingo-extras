package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.EntityArgument.*;
import static net.minecraft.commands.arguments.ResourceOrTagArgument.*;
import static net.minecraft.commands.arguments.coordinates.Vec2Argument.*;

public final class BingoSpreadPlayers4dCommand {
    private BingoSpreadPlayers4dCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("bingospreadplayers4d")
            .requires(source -> source.hasPermission(2))
            .then(argument("center", vec2())
                .then(argument("maxDistance", doubleArg(0))
                    .then(argument("targets", entities())
                        .then(argument("excludedBiomes", resourceOrTag(buildContext, Registries.BIOME))
                            .executes(ctx -> spreadPlayers4d(
                                ctx.getSource(),
                                getVec2(ctx, "center"),
                                getDouble(ctx, "maxDistance"),
                                getEntities(ctx, "targets"),
                                getResourceOrTag(ctx, "excludedBiomes", Registries.BIOME))
                            ))))));
    }

    private static int spreadPlayers4d(
        CommandSourceStack source,
        Vec2 center,
        double maxDistance,
        Collection<? extends Entity> entities,
        Predicate<Holder<Biome>> excludedBiomes
    ) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        RandomSource rand = RandomSource.create();
        for (int attempt = 0; attempt < 100; attempt++) {
            double relX;
            double relY;
            do {
                relX = rand.nextDouble() * (maxDistance * 2) - maxDistance;
                relY = rand.nextDouble() * (maxDistance * 2) - maxDistance;
            } while (relX * relX + relY * relY > maxDistance * maxDistance);

            int x = Mth.floor(center.x + relX);
            int z = Mth.floor(center.y + relY);
            BlockPos destPos = new BlockPos(x, BingoSpreadPlayersCommand.findSurface(level, x, z), z);
            if (!excludedBiomes.test(level.getBiome(destPos))) {
                teleportPlayers(source, destPos, entities);
                return Command.SINGLE_SUCCESS;
            }
        }

        throw BingoSpreadPlayersCommand.FAILED_TO_SPREAD_EXCEPTION.create();
    }

    private static void teleportPlayers(CommandSourceStack source, BlockPos destPos, Collection<? extends Entity> entities) {
        ServerLevel level = source.getLevel();
        ServerLevel originalLevel = Objects.requireNonNullElse(ServerLevelExt_Fantasy.getOriginalLevel(source.getLevel()), level);

        for (Entity entity : entities) {
            PlayerTeam team = entity.getTeam();
            ServerLevel destLevel;
            if (team != null) {
                destLevel = PlayerTeamExt_Fantasy.getTeamSpecificLevel(source.getServer(), team, originalLevel.dimension());
            } else {
                destLevel = originalLevel;
            }
            entity.teleportTo(destLevel, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5, Set.of(), entity.getYRot(), entity.getXRot());
        }
    }
}
