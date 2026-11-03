package net.earthcomputer.bingoextras.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.game.BingoGame;
import net.earthcomputer.bingoextras.BingoUtil;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.earthcomputer.bingoextras.gametest.mixin.MapItemSavedDataInvoker;
import net.earthcomputer.bingoextras.gametest.mixin.ServerPlayerAccessor;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

import static net.earthcomputer.bingoextras.gametest.BingoExtrasClientGameTest.*;

/**
 * Tests that the team specific Fantasy dimensions behave like the dimensions they are copies of, in particular wherever
 * Minecraft has a hardcoded check for {@link Level#OVERWORLD}, {@link Level#NETHER} or {@link Level#END}. Expects the
 * player to have been spread into their team's overworld.
 *
 * <p>This mod is server side only, so the client is treated as a vanilla client: only the server side checks are
 * tested, and the client is only checked for following the player to the right dimension.
 */
@SuppressWarnings("UnstableApiUsage")
final class FantasyTests {
    private static final int PORTAL_TIMEOUT = 600;
    private static final BlockPos END_PLATFORM_POS = ServerLevel.END_SPAWN_POINT.below();
    private static final BlockPos END_EXIT_PORTAL_POS = END_PLATFORM_POS.offset(-4, 0, 0);
    private static final BlockPos END_GATEWAY_POS = END_PLATFORM_POS.offset(0, 0, 4);

    private final ClientGameTestContext context;
    private final TestSingleplayerContext singleplayer;
    private final TestServerContext server;
    private final BlockPos teamSpawnPos;

    FantasyTests(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        this.context = context;
        this.singleplayer = singleplayer;
        this.server = singleplayer.getServer();
        this.teamSpawnPos = server.computeOnServer(server -> getTeamSpawnPos(server).pos());
    }

    void run() {
        giveProtection();
        testNetherPortals();
        testFinishAtSpawn();
        testEndPortals();
    }

    private void testNetherPortals() {
        BlockPos overworldPortalPos = server.computeOnServer(s -> {
            // BaseFireBlock.inPortalDimension
            BlockPos portalPos = buildNetherPortal(teamLevel(s, Level.OVERWORLD), teamSpawnPos.offset(4, -1, 3));
            // ServerLevel.isAllowedToEnterPortal
            teamLevel(s, Level.OVERWORLD).getGameRules().set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false, s);
            teleport(teamLevel(s, Level.OVERWORLD), Vec3.atBottomCenterOf(portalPos));
            return portalPos;
        });
        context.waitTicks(150);
        server.runOnServer(s -> {
            check(player().level() == teamLevel(s, Level.OVERWORLD), "nether portal: player should not enter the nether when it's disabled by game rule, but was in %s", player().level().dimension());
            teamLevel(s, Level.OVERWORLD).getGameRules().set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, true, s);
            // the failed attempt put the player on portal cooldown, which lasts until they step out of the portal
            teleport(teamLevel(s, Level.OVERWORLD), Vec3.atBottomCenterOf(teamSpawnPos));
        });
        enterPortal(Level.OVERWORLD, overworldPortalPos);

        // NetherPortalBlock.getPortalDestination, ServerPlayer.teleport
        waitForPlayerIn(Level.NETHER);
        BlockPos netherPortalPos = server.computeOnServer(s -> {
            ServerPlayer player = player();
            ServerLevel level = player.level();
            BlockPos portalPos = player.blockPosition();
            check(level.getBlockState(portalPos).is(Blocks.NETHER_PORTAL), "nether portal: player should have arrived in a nether portal");
            // new portals are placed up to 16 blocks from the scaled position
            check(Math.abs(portalPos.getX() - Math.floorDiv(overworldPortalPos.getX(), 8)) <= 20 && Math.abs(portalPos.getZ() - Math.floorDiv(overworldPortalPos.getZ(), 8)) <= 20, "nether portal: arrived at %s, which is too far from the overworld portal at %s", portalPos, overworldPortalPos);
            check(((ServerPlayerAccessor) player).getEnteredNetherPosition() != null, "nether portal: entered nether position should have been set");
            check(hasAdvancement(s, player, "story/enter_the_nether"), "nether portal: player should have the enter the nether advancement");
            check(Objects.requireNonNull(s.getLevel(Level.NETHER)).players().isEmpty(), "nether portal: player should not be in the real nether");
            return portalPos;
        });
        checkClientArrived(Level.NETHER);

        BlockPos roomPos = netherPortalPos.offset(0, 0, 12);
        server.runOnServer(s -> {
            ServerLevel level = teamLevel(s, Level.NETHER);
            // move out of the portal before it takes us back to the overworld
            buildRoom(level, roomPos);
            teleport(level, Vec3.atBottomCenterOf(roomPos));

            // BaseFireBlock.inPortalDimension
            buildNetherPortal(level, roomPos.offset(-4, -1, -4));

            // MapItemSavedData.calculateRotation: the player marker on maps spins in the nether
            MapItemSavedData mapData = MapItemSavedData.createForClient((byte) 0, false, level.dimension());
            int s10 = (int) (level.getGameTime() / 10L);
            byte netherRotation = (byte) (s10 * s10 * 34187121 + s10 * 121 >> 15 & 15);
            float yRot = ((netherRotation + 8) & 15) * 22.5F; // a rotation which would give a different map rotation outside the nether
            byte rotation = ((MapItemSavedDataInvoker) mapData).invokeCalculateRotation(level, yRot);
            check(rotation == netherRotation, "nether map: player marker rotation should be %d in the nether, got %d", netherRotation, rotation);
        });

        testGrowTreeInNether(roomPos);

        // NetherPortalBlock.getPortalDestination, ServerPlayer.triggerDimensionChangeTriggers
        server.runOnServer(s -> {
            // pretend we've traveled far so we get the nether travel advancement on the way back
            ((ServerPlayerAccessor) player()).setEnteredNetherPosition(Vec3.atBottomCenterOf(overworldPortalPos).add(8000, 0, 0));
        });
        enterPortal(Level.NETHER, netherPortalPos);
        waitForPlayerIn(Level.OVERWORLD);
        server.runOnServer(s -> {
            ServerPlayer player = player();
            check(player.blockPosition().closerThan(overworldPortalPos, 3), "nether portal: player should have come back through the overworld portal at %s, but was at %s", overworldPortalPos, player.blockPosition());
            check(hasAdvancement(s, player, "nether/fast_travel"), "nether portal: player should have the nether travel advancement");
            check(!s.overworld().players().contains(player), "nether portal: player should not be in the real overworld");
            teleport(player.level(), Vec3.atBottomCenterOf(teamSpawnPos));
        });
        checkClientArrived(Level.OVERWORLD);
    }

    private void testGrowTreeInNether(BlockPos roomPos) {
        BlockPos saplingPos = roomPos.south();
        BingoGame game = startGoal("bingo:easy/grow_tree_in_nether");
        server.runOnServer(s -> {
            ServerLevel level = teamLevel(s, Level.NETHER);
            level.setBlockAndUpdate(saplingPos.below(), Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(saplingPos, Blocks.BIRCH_SAPLING.defaultBlockState());
            // look at the sapling
            player().teleportTo(level, roomPos.getX() + 0.5, roomPos.getY(), roomPos.getZ() + 0.5, Set.of(), 0, 45, true);
            player().getInventory().setSelectedItem(new ItemStack(Items.BONE_MEAL, 64));
            player().containerMenu.broadcastChanges();
        });
        singleplayer.getConnection().waitForChunksDownload();
        singleplayer.getConnection().waitForClientboundPackets();
        context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        try {
            server.waitFor(s -> teamLevel(s, Level.NETHER).getBlockState(saplingPos).is(BlockTags.LOGS));
        } finally {
            context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        }
        waitForGoal(game, "grow tree in nether");
    }

    private void testFinishAtSpawn() {
        // the compass should point to the team spawn rather than spinning
        ResourceKey<Level> teamOverworld = server.computeOnServer(s -> {
            teleport(teamLevel(s, Level.OVERWORLD), Vec3.atBottomCenterOf(teamSpawnPos.offset(10, 0, 0)));
            return teamLevel(s, Level.OVERWORLD).dimension();
        });
        context.waitFor(client -> client.level != null && client.level.dimension() == teamOverworld && client.player != null && client.player.blockPosition().equals(teamSpawnPos.offset(10, 0, 0)));
        context.runOnClient(client -> {
            ItemStack compass = new ItemStack(Items.COMPASS);
            GlobalPos target = CompassAngleState.CompassTarget.SPAWN.get(Objects.requireNonNull(client.level), compass, client.player);
            check(GlobalPos.of(teamOverworld, teamSpawnPos).equals(target), "compass: should point to the team spawn %s in %s, but pointed to %s", teamSpawnPos, teamOverworld, target);
        });

        server.runOnServer(s -> {
            teleport(teamLevel(s, Level.OVERWORLD), Vec3.atBottomCenterOf(teamSpawnPos));
            player().getInventory().setSelectedItem(new ItemStack(Items.COMPASS));
            player().containerMenu.broadcastChanges();
        });
        BingoGame game = startGoal("bingo:easy/finish_at_spawn");
        waitForGoal(game, "finish at spawn");
    }

    private void testEndPortals() {
        BlockPos overworldEndPortalPos = teamSpawnPos.offset(-4, -1, 3);

        // FallingBlockEntity.teleport
        FallingBlockEntity fallingBlock = server.computeOnServer(s -> {
            ServerLevel level = teamLevel(s, Level.OVERWORLD);
            level.setBlockAndUpdate(overworldEndPortalPos, Blocks.END_PORTAL.defaultBlockState());
            FallingBlockEntity entity = Objects.requireNonNull(EntityTypes.FALLING_BLOCK.create(level, EntitySpawnReason.COMMAND));
            entity.setPos(Vec3.atCenterOf(overworldEndPortalPos));
            entity.setNoGravity(true);
            level.addFreshEntity(entity);
            return entity;
        });
        server.waitFor(s -> fallingBlock.isRemoved() && findTeamLevel(s, Level.END) != null && !findTeamLevel(s, Level.END).getEntities(EntityTypes.FALLING_BLOCK, e -> true).isEmpty(), PORTAL_TIMEOUT);
        server.runOnServer(s -> {
            check(fallingBlock.forceTickAfterTeleportToDuplicate, "end portal: falling block should get an extra tick after going to the end");
            teamLevel(s, Level.END).getEntities(EntityTypes.FALLING_BLOCK, e -> true).forEach(e -> e.discard());
        });

        // EndPortalBlock.getPortalDestination
        enterEnd(overworldEndPortalPos);
        server.runOnServer(s -> {
            ServerPlayer player = player();
            check(teamLevel(s, Level.END).getBlockState(END_PLATFORM_POS.below()).is(Blocks.OBSIDIAN), "end portal: obsidian platform should have been created in the team's end");
            check(hasAdvancement(s, player, "story/enter_the_end"), "end portal: player should have the enter the end advancement");
        });

        server.waitFor(s -> !teamLevel(s, Level.END).getEntities(EntityTypes.ENDER_DRAGON, e -> true).isEmpty(), PORTAL_TIMEOUT);
        server.runOnServer(s -> {
            // Level.canHaveWeather: the end has sky light, but it shouldn't rain there
            check(!teamLevel(s, Level.END).canHaveWeather(), "end weather: it should not be able to rain in the end");
            check(teamLevel(s, Level.OVERWORLD).canHaveWeather(), "overworld weather: it should be able to rain in the overworld");
            // keep the dragon out of the way
            teamLevel(s, Level.END).getEntities(EntityTypes.ENDER_DRAGON, e -> true).forEach(dragon -> dragon.setNoAi(true));
        });

        // ThrownEnderpearl.canTeleport, Entity.canTeleport: pearls can't leave the end before the owner has seen the credits
        server.runOnServer(s -> {
            ServerLevel level = teamLevel(s, Level.END);
            level.setBlockAndUpdate(END_EXIT_PORTAL_POS.below(), Blocks.OBSIDIAN.defaultBlockState());
            level.setBlockAndUpdate(END_EXIT_PORTAL_POS, Blocks.END_PORTAL.defaultBlockState());
        });
        ThrownEnderpearl stuckPearl = server.computeOnServer(s -> spawnPearl(teamLevel(s, Level.END), END_EXIT_PORTAL_POS));
        context.waitTicks(40);
        server.runOnServer(s -> {
            check(!stuckPearl.isRemoved() && stuckPearl.level() == teamLevel(s, Level.END), "end portal: ender pearl should not leave the end before its owner has seen the credits");
            stuckPearl.discard();
        });

        // EndPortalBlock.entityInside: credits are shown when leaving the end for the first time
        enterPortal(Level.END, END_EXIT_PORTAL_POS);
        context.waitForScreen(WinScreen.class);
        server.runOnServer(s -> check(player().seenCredits, "end portal: player should have seen the credits"));
        context.runOnClient(client -> Objects.requireNonNull(client.gui.screen()).onClose());
        waitForPlayerIn(Level.OVERWORLD);
        server.runOnServer(s -> check(player().blockPosition().closerThan(teamSpawnPos, 3), "end credits: player should have respawned at the team spawn point %s, but was at %s", teamSpawnPos, player().blockPosition()));
        checkClientArrived(Level.OVERWORLD);
        giveProtection();

        enterEnd(overworldEndPortalPos);

        // ThrownEnderpearl.canTeleport, Entity.canTeleport: now the owner has seen the credits the pearl can leave
        server.runOnServer(s -> spawnPearl(teamLevel(s, Level.END), END_EXIT_PORTAL_POS));
        server.waitFor(s -> !teamLevel(s, Level.OVERWORLD).getEntities(EntityTypes.ENDER_PEARL, e -> true).isEmpty(), PORTAL_TIMEOUT);
        server.runOnServer(s -> teamLevel(s, Level.OVERWORLD).getEntities(EntityTypes.ENDER_PEARL, e -> true).forEach(e -> e.discard()));

        // TheEndGatewayBlockEntity.getPortalPosition
        server.runOnServer(s -> {
            ServerLevel level = teamLevel(s, Level.END);
            level.setBlockAndUpdate(END_GATEWAY_POS.below(), Blocks.OBSIDIAN.defaultBlockState());
            level.setBlockAndUpdate(END_GATEWAY_POS, Blocks.END_GATEWAY.defaultBlockState());
        });
        enterPortal(Level.END, END_GATEWAY_POS);
        server.waitFor(s -> player().level() == teamLevel(s, Level.END) && player().position().horizontalDistance() > 500, PORTAL_TIMEOUT);

        // EndPortalBlock.getPortalDestination: leaving the end once the credits have been seen
        enterPortal(Level.END, END_EXIT_PORTAL_POS);
        waitForPlayerIn(Level.OVERWORLD);
        server.runOnServer(s -> check(player().blockPosition().closerThan(teamSpawnPos, 3), "end portal: player should have come back to the team spawn point %s, but was at %s", teamSpawnPos, player().blockPosition()));
        checkClientArrived(Level.OVERWORLD);
    }

    private void enterEnd(BlockPos overworldEndPortalPos) {
        enterPortal(Level.OVERWORLD, overworldEndPortalPos);
        waitForPlayerIn(Level.END);
        server.runOnServer(s -> check(Objects.requireNonNull(s.getLevel(Level.END)).players().isEmpty(), "end portal: player should not be in the real end"));
        checkClientArrived(Level.END);
    }

    private void giveProtection() {
        server.runCommand("effect give @a resistance infinite 255 true");
        server.runCommand("effect give @a fire_resistance infinite 0 true");
        server.runCommand("effect give @a saturation infinite 0 true");
    }

    private BingoGame startGoal(String goal) {
        return server.computeOnServer(s -> {
            BingoGame oldGame = BingoUtil.getBingoGame(s);
            if (oldGame != null) {
                oldGame.endGame(s.getPlayerList());
            }
            s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), "bingo start --require-goal " + goal + " --size 1 " + TEAM);
            return Objects.requireNonNull(BingoUtil.getBingoGame(s), () -> "Failed to start game with goal " + goal);
        });
    }

    private void waitForGoal(BingoGame game, String goalName) {
        server.waitFor(s -> BingoUtil.getBingoGame(s) != game);
        check(game.getBoard().getStates()[0].any(), "goal %s: game ended without completing the goal", goalName);
    }

    private void waitForPlayerIn(ResourceKey<Level> originalDimension) {
        server.waitFor(s -> {
            ServerLevel level = findTeamLevel(s, originalDimension);
            return level != null && player().level() == level;
        }, PORTAL_TIMEOUT);
        server.runOnServer(s -> {
            ServerLevel level = player().level();
            check(ServerLevelExt_Fantasy.getTeam(level) == getTeam(s), "player should be in a level for team %s, but was in %s", TEAM, level.dimension());
            check(ServerLevelExt_Fantasy.getOriginalLevel(level) == s.getLevel(originalDimension), "player's level %s should be a copy of %s", level.dimension(), originalDimension);
        });
    }

    private void checkClientArrived(ResourceKey<Level> originalDimension) {
        ResourceKey<Level> dimension = server.computeOnServer(s -> teamLevel(s, originalDimension).dimension());
        context.waitFor(client -> client.level != null && client.level.dimension() == dimension);
        singleplayer.getConnection().waitForChunksDownload();
    }

    private ServerPlayer player() {
        return singleplayer.getConnection().getServerPlayer();
    }

    private void teleport(ServerLevel level, Vec3 pos) {
        ServerPlayer player = player();
        player.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), player.getYRot(), player.getXRot(), true);
    }

    private void enterPortal(ResourceKey<Level> originalDimension, BlockPos portalPos) {
        // standing in a portal while on portal cooldown keeps resetting the cooldown, so wait for it to run out first
        server.waitFor(s -> !player().isOnPortalCooldown());
        server.runOnServer(s -> teleport(teamLevel(s, originalDimension), Vec3.atBottomCenterOf(portalPos)));
    }

    private ThrownEnderpearl spawnPearl(ServerLevel level, BlockPos pos) {
        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player(), new ItemStack(Items.ENDER_PEARL));
        pearl.setPos(Vec3.atBottomCenterOf(pos).add(0, 0.25, 0));
        pearl.setDeltaMovement(Vec3.ZERO);
        pearl.setNoGravity(true);
        level.addFreshEntity(pearl);
        return pearl;
    }

    @Nullable
    private static ServerLevel findTeamLevel(MinecraftServer server, ResourceKey<Level> originalDimension) {
        var handle = ((PlayerTeamExt_Fantasy) getTeam(server)).bingoExtras$getTeamSpecificLevels().get(originalDimension);
        return handle == null ? null : handle.asLevel();
    }

    private static ServerLevel teamLevel(MinecraftServer server, ResourceKey<Level> originalDimension) {
        return Objects.requireNonNull(findTeamLevel(server, originalDimension), () -> "Team " + TEAM + " has no level for " + originalDimension);
    }

    private static boolean hasAdvancement(MinecraftServer server, ServerPlayer player, String id) {
        AdvancementHolder advancement = Objects.requireNonNull(server.getAdvancements().get(Identifier.withDefaultNamespace(id)), () -> "No advancement " + id);
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    /**
     * Builds an obsidian frame along the X axis with its bottom corner at {@code corner}, lights it, and returns the
     * position of the bottom of the portal.
     */
    private static BlockPos buildNetherPortal(ServerLevel level, BlockPos corner) {
        for (int dx = 0; dx < 4; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                boolean frame = dx == 0 || dx == 3 || dy == 0 || dy == 4;
                level.setBlockAndUpdate(corner.offset(dx, dy, 0), frame ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
        }
        BlockPos portalPos = corner.offset(1, 1, 0);
        level.setBlockAndUpdate(portalPos, BaseFireBlock.getState(level, portalPos));
        check(level.getBlockState(portalPos).is(Blocks.NETHER_PORTAL), "nether portal: lighting a portal in %s should create a portal, but got %s", level.dimension(), level.getBlockState(portalPos));
        return portalPos;
    }

    /**
     * Builds a glass room, to keep out lava, with an obsidian floor below {@code center}.
     */
    private static void buildRoom(ServerLevel level, BlockPos center) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -1; dy <= 11; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockState state;
                    if (dy == -1) {
                        state = Blocks.OBSIDIAN.defaultBlockState();
                    } else if (Math.abs(dx) == 5 || Math.abs(dz) == 5 || dy == 11) {
                        state = Blocks.GLASS.defaultBlockState();
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    level.setBlockAndUpdate(center.offset(dx, dy, dz), state);
                }
            }
        }
    }
}
