package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.earthcomputer.bingoextras.BingoExtras;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.EntityArgument.*;
import static net.minecraft.commands.arguments.ResourceOrTagArgument.*;
import static net.minecraft.commands.arguments.coordinates.Vec2Argument.*;

public final class BingoSpreadPlayersCommand {
    private static final SimpleCommandExceptionType FAILED_TO_SPREAD_EXCEPTION = new SimpleCommandExceptionType(BingoExtras.translatable("bingo_extras.bingospreadplayers.failedToSpread"));

    private BingoSpreadPlayersCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("bingospreadplayers")
            .requires(source -> source.hasPermission(2))
            .then(argument("center", vec2())
                .then(argument("maxDistance", doubleArg(0))
                    .then(argument("distanceBetweenTeams", doubleArg(0))
                        .then(argument("respectTeams", bool())
                            .then(argument("targets", entities())
                                .then(argument("sameBiome", bool())
                                    .then(argument("excludedBiomes", resourceOrTag(buildContext, Registries.BIOME))
                                        .executes(ctx -> bingoSpreadPlayers(
                                            ctx.getSource(),
                                            getVec2(ctx, "center"),
                                            getDouble(ctx, "maxDistance"),
                                            getDouble(ctx, "distanceBetweenTeams"),
                                            getBool(ctx, "respectTeams"),
                                            getEntities(ctx, "targets"),
                                            getBool(ctx, "sameBiome"),
                                            getResourceOrTag(ctx, "excludedBiomes", Registries.BIOME)
                                        ))))))))));
    }

    private static int bingoSpreadPlayers(
        CommandSourceStack source,
        Vec2 center,
        double maxDistance,
        double distanceBetweenTeams,
        boolean respectTeams,
        Collection<? extends Entity> entities,
        boolean sameBiome,
        Predicate<Holder<Biome>> excludedBiomes
    ) throws CommandSyntaxException {
        List<List<Entity>> groups = groupEntities(entities, respectTeams);
        if (groups.isEmpty()) {
            throw new AssertionError("No groups");
        }

        RandomSource rand = RandomSource.create();
        Util.shuffle(groups, rand);

        double minX = (double) center.x - maxDistance;
        double minZ = (double) center.y - maxDistance;
        double maxX = (double) center.x + maxDistance;
        double maxZ = (double) center.y + maxDistance;

        List<Vector2d> possibleSpreadPoints = poissonDiskSampling(rand, distanceBetweenTeams, minX, minZ, maxX, maxZ);
        if (possibleSpreadPoints.size() < groups.size()) {
            throw FAILED_TO_SPREAD_EXCEPTION.create();
        }
        Util.shuffle(possibleSpreadPoints, rand);

        List<Vector2d> spreadPoints = new ArrayList<>(possibleSpreadPoints.subList(0, groups.size()));
        possibleSpreadPoints.subList(0, groups.size()).clear();

        Holder<Biome> chosenBiome = null;

        ServerLevel level = source.getLevel();
        for (int i = 0; i < spreadPoints.size(); i++) {
            Vector2d point = spreadPoints.get(i);
            while (true) {
                int height = findSurface(level, Mth.floor(point.x), Mth.floor(point.y));
                Holder<Biome> biome = level.getBiome(new BlockPos(Mth.floor(point.x), height, Mth.floor(point.y)));
                if (!excludedBiomes.test(biome)) {
                    chosenBiome = biome;
                    break;
                }
                if (possibleSpreadPoints.isEmpty()) {
                    throw FAILED_TO_SPREAD_EXCEPTION.create();
                }
                point = possibleSpreadPoints.remove(0);
                spreadPoints.set(i, point);
            }
        }

        assert chosenBiome != null;

        if (sameBiome) {
            for (int i = 0; i < spreadPoints.size(); i++) {
                Vector2d point = spreadPoints.get(i);
                Holder<Biome> chosenBiome_f = chosenBiome;
                var result = level.findClosestBiome3d(biome -> biome == chosenBiome_f, new BlockPos(Mth.floor(point.x), 0, Mth.floor(point.y)), Math.min(6400, (int) (distanceBetweenTeams / 4)), 32, 64);
                if (result != null) {
                    spreadPoints.set(i, new Vector2d(result.getFirst().getX() + 0.5, result.getFirst().getZ() + 0.5));
                }
            }
        }

        for (int i = 0; i < groups.size(); i++) {
            Vector3d dest = adjustToSafeLocation(level, spreadPoints.get(i));
            for (Entity entity : groups.get(i)) {
                entity.teleportTo(level, dest.x, dest.y, dest.z, Set.of(), entity.getYRot(), entity.getXRot());
            }
        }

        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.bingospreadplayers.success"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static List<List<Entity>> groupEntities(Collection<? extends Entity> entities, boolean respectTeams) {
        List<List<Entity>> groups = new ArrayList<>();
        if (respectTeams) {
            Map<PlayerTeam, List<Entity>> entitiesByTeam = new HashMap<>();
            for (Entity entity : entities) {
                PlayerTeam team = entity.getTeam();
                if (team != null) {
                    entitiesByTeam.computeIfAbsent(team, k -> {
                        List<Entity> newList = new ArrayList<>();
                        groups.add(newList);
                        return newList;
                    }).add(entity);
                } else {
                    groups.add(List.of(entity));
                }
            }
        } else {
            for (Entity entity : entities) {
                groups.add(List.of(entity));
            }
        }
        return groups;
    }

    private static int findSurface(ServerLevel level, int x, int z) {
        if (level.dimensionType().hasCeiling()) {
            int startY = level.getMinBuildHeight() + (int) (level.dimensionType().logicalHeight() * 0.7);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
            for (int dy = 0; dy < startY - level.getMinBuildHeight(); dy = dy <= 0 ? -dy + 1 : -dy) {
                if (startY + dy >= level.getMinBuildHeight() + level.dimensionType().logicalHeight()) {
                    continue;
                }
                if (canSpawnAt(level, pos.setY(startY + dy))) {
                    return startY + dy;
                }
            }

            return startY;
        } else {
            LevelChunk chunk = level.getChunk(x >> 4, z >> 4);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
            for (int y = chunk.getSectionYFromSectionIndex(chunk.getHighestFilledSectionIndex()) * 16 + 16; y > level.getMinBuildHeight(); y--) {
                BlockState stateBelow = chunk.getBlockState(pos.setY(y - 1));
                //noinspection deprecation
                if (stateBelow.blocksMotion() && !stateBelow.is(BlockTags.LEAVES)) {
                    return y;
                }
            }

            return level.getMinBuildHeight();
        }
    }

    public static Vector3d adjustToSafeLocation(ServerLevel level, Vector2d input) {
        final int STRIDE = 4;

        int surfaceHeight = findSurface(level, Mth.floor(input.x), Mth.floor(input.y));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(input.x), surfaceHeight, Mth.floor(input.y));
        if (canSpawnAt(level, pos)) {
            return new Vector3d(Math.floor(input.x) + 0.5, surfaceHeight, Math.floor(input.y) + 0.5);
        }

        for (int radius = 1; radius <= 50; radius++) {
            for (int dx = -radius; dx < radius; dx++) {
                int dz = radius - Math.abs(dx);
                surfaceHeight = findSurface(level, Mth.floor(input.x) + dx * STRIDE, Mth.floor(input.y) + dz * STRIDE);
                if (canSpawnAt(level, pos.set(Mth.floor(input.x) + dx * STRIDE, surfaceHeight, Mth.floor(input.y) + dz * STRIDE))) {
                    return new Vector3d(Math.floor(input.x) + 0.5 + dx * STRIDE, surfaceHeight, Math.floor(input.y) + 0.5 + dz * STRIDE);
                }

                dz = Math.abs(dx + 1) - radius;
                surfaceHeight = findSurface(level, Mth.floor(input.x) + (dx + 1) * STRIDE, Mth.floor(input.y) + dz * STRIDE);
                if (canSpawnAt(level, pos.set(Mth.floor(input.x) + (dx + 1) * STRIDE, surfaceHeight, Mth.floor(input.y) + dz * STRIDE))) {
                    return new Vector3d(Math.floor(input.x) + 0.5 + dx * STRIDE, surfaceHeight, Math.floor(input.y) + 0.5 + dz * STRIDE);
                }
            }
        }

        surfaceHeight = findSurface(level, Mth.floor(input.x), Mth.floor(input.y));
        return new Vector3d(Math.floor(input.x) + 0.5, surfaceHeight, Math.floor(input.y) + 0.5);
    }

    @SuppressWarnings("deprecation") // Mojang studios
    private static boolean canSpawnAt(ServerLevel level, BlockPos pos) {
        BlockState blockHere = level.getBlockState(pos);
        BlockState blockAbove = level.getBlockState(pos.above());
        BlockState blockBelow = level.getBlockState(pos.below());
        return !blockHere.isSolid() && blockHere.getFluidState().isEmpty() &&
            !blockAbove.isSolid() && blockAbove.getFluidState().isEmpty() &&
            blockBelow.blocksMotion();
    }

    private static boolean isValidPoint(Vector2d[][] grid, double cellsize,
                         double minX, double minY, double maxX, double maxY,
                         int gwidth, int gheight,
                         Vector2d p, double radius) {
        /* Make sure the point is on the screen */
        if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY) {
            return false;
        }

        /* Check neighboring eight cells */
        int xindex = Mth.floor((p.x - minX) / cellsize);
        int yindex = Mth.floor((p.y - minY) / cellsize);
        int i0 = Math.max(xindex - 1, 0);
        int i1 = Math.min(xindex + 1, gwidth - 1);
        int j0 = Math.max(yindex - 1, 0);
        int j1 = Math.min(yindex + 1, gheight - 1);

        for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
                if (grid[i][j] != null) {
                    double dx = grid[i][j].x - p.x;
                    double dy = grid[i][j].y - p.y;
                    if (dx * dx + dy * dy < radius * radius) {
                        return false;
                    }
                }
            }
        }

        /* If we get here, return true */
        return true;
    }

    private static void insertPoint(Vector2d[][] grid, double minX, double minY, double cellsize, Vector2d point) {
        int xindex = Mth.floor((point.x - minX) / cellsize);
        int yindex = Mth.floor((point.y - minY) / cellsize);
        grid[xindex][yindex] = point;
    }

    private static List<Vector2d> poissonDiskSampling(RandomSource rand, double radius, double minX, double minY, double maxX, double maxY) {
        final int K = 10;

        double width = maxX - minX;
        double height = maxY - minY;

        /* The final set of points to return */
        List<Vector2d> points = new ArrayList<>();
        /* The currently "active" set of points */
        List<Vector2d> active = new ArrayList<>();
        /* Initial point p0 */
        Vector2d p0 = new Vector2d(minX + rand.nextDouble() * width, minY + rand.nextDouble() * height);
        Vector2d[][] grid;
        double cellsize = Math.floor(radius/Mth.SQRT_OF_TWO);

        /* Figure out no. of cells in the grid for our canvas */
        int ncells_width = Mth.ceil(width/cellsize) + 1;
        int ncells_height = Mth.ceil(width/cellsize) + 1;

        /* Allocate the grid an initialize all elements to null */
        grid = new Vector2d[ncells_width][ncells_height];
        for (int i = 0; i < ncells_width; i++) {
            for (int j = 0; j < ncells_height; j++) {
                grid[i][j] = null;
            }
        }

        insertPoint(grid, minX, minY, cellsize, p0);
        points.add(p0);
        active.add(p0);

        while (!active.isEmpty()) {
            int random_index = rand.nextInt(active.size());
            Vector2d p = active.get(random_index);

            boolean found = false;
            for (int tries = 0; tries < K; tries++) {
                double theta = rand.nextDouble() * Mth.TWO_PI;
                double new_radius = radius + rand.nextDouble() * radius;
                double pnewx = p.x + new_radius * Math.cos(theta);
                double pnewy = p.y + new_radius * Math.sin(theta);
                Vector2d pnew = new Vector2d(pnewx, pnewy);

                if (!isValidPoint(grid, cellsize,
                    minX, minY, maxX, maxY,
                    ncells_width, ncells_height,
                    pnew, radius)) {
                    continue;
                }

                points.add(pnew);
                insertPoint(grid, minX, minY, cellsize, pnew);
                active.add(pnew);
                found = true;
                break;
            }

            /* If no point was found after k tries, remove p */
            if (!found) {
                active.remove(random_index);
            }
        }

        return points;
    }
}
