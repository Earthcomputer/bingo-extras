package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.bingoextras.BingoExtras;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodConstants;

import java.util.Collection;
import java.util.List;

import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.EntityArgument.*;

public final class SetEntityValCommand {
    private SetEntityValCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("setentityval")
            .requires(source -> source.hasPermission(2))
            .then(literal("health")
                .then(argument("entities", entities())
                    .then(argument("health", floatArg(0))
                        .executes(ctx -> setHealth(ctx.getSource(), getLivingEntities(ctx, "entities"), getFloat(ctx, "health"))))))
            .then(literal("food")
                .then(argument("players", players())
                    .then(argument("food", integer(0, FoodConstants.MAX_FOOD))
                        .executes(ctx -> setFood(ctx.getSource(), getPlayers(ctx, "players"), getInteger(ctx, "food"))))))
            .then(literal("saturation")
                .then(argument("players", players())
                    .then(argument("saturation", floatArg(0, FoodConstants.MAX_SATURATION))
                        .executes(ctx -> setSaturation(ctx.getSource(), getPlayers(ctx, "players"), getFloat(ctx, "saturation"))))))
            .then(literal("exhaustion")
                .then(argument("players", players())
                    .then(argument("exhaustion", floatArg(0, FoodConstants.EXHAUSTION_DROP))
                        .executes(ctx -> setExhaustion(ctx.getSource(), getPlayers(ctx, "players"), getFloat(ctx, "exhaustion")))))));
    }

    private static List<LivingEntity> getLivingEntities(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        List<LivingEntity> result = getOptionalEntities(ctx, name).stream()
            .filter(entity -> entity instanceof LivingEntity)
            .map(entity -> (LivingEntity) entity)
            .toList();
        if (result.isEmpty()) {
            throw NO_ENTITIES_FOUND.create();
        } else {
            return result;
        }
    }

    private static int setHealth(CommandSourceStack source, List<LivingEntity> entities, float health) {
        for (LivingEntity entity : entities) {
            if (health == 0) {
                entity.kill();
            } else {
                entity.setHealth(Math.min(health, entity.getMaxHealth()));
            }
        }

        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.setentityval.health.success", entities.size(), health), true);
        return entities.size();
    }

    private static int setFood(CommandSourceStack source, Collection<ServerPlayer> players, int food) {
        for (ServerPlayer player : players) {
            player.getFoodData().setFoodLevel(food);
        }
        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.setentityval.food.success", players.size(), food), true);
        return players.size();
    }

    private static int setSaturation(CommandSourceStack source, Collection<ServerPlayer> players, float saturation) {
        for (ServerPlayer player : players) {
            player.getFoodData().setSaturation(saturation);
        }
        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.setentityval.saturation.success", players.size(), saturation), true);
        return players.size();
    }

    private static int setExhaustion(CommandSourceStack source, Collection<ServerPlayer> players, float exhaustion) {
        for (ServerPlayer player : players) {
            player.getFoodData().setExhaustion(exhaustion);
        }
        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.setentityval.exhaustion.success", players.size(), exhaustion), true);
        return players.size();
    }
}
