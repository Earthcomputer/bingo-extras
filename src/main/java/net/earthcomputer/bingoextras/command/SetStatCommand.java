package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.bingoextras.BingoExtras;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.EntityArgument.*;
import static net.minecraft.commands.arguments.ResourceArgument.*;

public final class SetStatCommand {
    private SetStatCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        // force load the class so that all the stat types are registered
        @SuppressWarnings("unused") Object unused = Stats.CUSTOM;

        for (StatType<?> statType : BuiltInRegistries.STAT_TYPE) {
            registerStatType(dispatcher, context, statType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerStatType(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, StatType<T> statType) {
        dispatcher.register(literal("setstat")
            .requires(source -> source.hasPermission(2))
            .then(argument("players", players())
                .then(literal(getName(statType))
                    .then(argument("stat", resource(context, statType.getRegistry().key()))
                        .then(argument("value", integer())
                            .executes(ctx -> setStat(
                                ctx.getSource(),
                                getPlayers(ctx, "players"),
                                statType.get(getResource(ctx, "stat", (ResourceKey<Registry<T>>) statType.getRegistry().key()).value()),
                                getInteger(ctx, "value")
                            )))))));
    }

    private static String getName(StatType<?> statType) {
        ResourceLocation key = BuiltInRegistries.STAT_TYPE.getKey(statType);
        if (key == null) {
            return "null";
        } else {
            return key.getNamespace().equals("minecraft") ? key.getPath() : key.toString();
        }
    }

    private static <T> int setStat(CommandSourceStack source, Collection<ServerPlayer> players, Stat<T> stat, int value) {
        for (ServerPlayer player : players) {
            player.resetStat(stat);
            player.awardStat(stat, value);
        }

        MutableComponent statName;
        if (stat.getType() == Stats.CUSTOM) {
            statName = Component.translatable(((ResourceLocation) stat.getValue()).toLanguageKey("stat"));
        } else if (isRegistry(stat, Registries.BLOCK)) {
            statName = stat.getType().getDisplayName().copy().append(" ").append(((Block) stat.getValue()).getName());
        } else if (isRegistry(stat, Registries.ITEM)) {
            statName = stat.getType().getDisplayName().copy().append(" ").append(((Item) stat.getValue()).getName());
        } else if (isRegistry(stat, Registries.ENTITY_TYPE)) {
            statName = stat.getType().getDisplayName().copy().append(" ").append(((EntityType<?>) stat.getValue()).getDescription());
        } else {
            statName = stat.getType().getDisplayName().copy().append(" " + stat.getType().getRegistry().getKey(stat.getValue()));
        }

        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.setstat.success", statName, players.size(), value), true);
        return players.size();
    }

    private static <T> boolean isRegistry(Stat<?> stat, ResourceKey<Registry<T>> registry) {
        return stat.getType().getRegistry().key() == registry;
    }
}
