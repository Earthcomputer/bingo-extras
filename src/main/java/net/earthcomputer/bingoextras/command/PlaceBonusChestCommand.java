package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.joml.Vector2d;
import org.joml.Vector3d;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.ResourceLocationArgument.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;

public final class PlaceBonusChestCommand {
    private PlaceBonusChestCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("placebonuschest")
            .requires(source -> source.hasPermission(2))
            .then(argument("pos", blockPos())
                .then(argument("radius", integer(0))
                    .executes(ctx -> placeBonusChest(ctx.getSource(), getBlockPos(ctx, "pos"), getInteger(ctx, "radius"), BuiltInLootTables.SPAWN_BONUS_CHEST))
                    .then(argument("lootTable", id())
                        .suggests(LootCommand.SUGGEST_LOOT_TABLE)
                        .executes(ctx -> placeBonusChest(ctx.getSource(), getBlockPos(ctx, "pos"), getInteger(ctx, "radius"), getId(ctx, "lootTable")))))));
    }

    private static int placeBonusChest(CommandSourceStack source, BlockPos pos, int radius, ResourceLocation lootTable) {
        ServerLevel level = source.getLevel();
        RandomSource rand = RandomSource.create();

        pos = pos.east(rand.nextIntBetweenInclusive(-radius, radius)).south(rand.nextIntBetweenInclusive(-radius, radius));
        Vector3d safeLocation = BingoSpreadPlayersCommand.adjustToSafeLocation(level, new Vector2d(pos.getX(), pos.getZ()));
        pos = BlockPos.containing(safeLocation.x, safeLocation.y, safeLocation.z);

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos torchPos = pos.relative(dir);
            if (level.getBlockState(torchPos).canBeReplaced() && Blocks.TORCH.defaultBlockState().canSurvive(level, torchPos)) {
                level.setBlockAndUpdate(torchPos, Blocks.TORCH.defaultBlockState());
            }
        }

        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity blockEntity) {
            blockEntity.setLootTable(lootTable);
        }

        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.875, pos.getZ() + 0.5, new ItemStack(Items.TORCH), 0, 0, 0);
        item.setGlowingTag(true);
        level.addFreshEntity(item);

        return Command.SINGLE_SUCCESS;
    }
}
