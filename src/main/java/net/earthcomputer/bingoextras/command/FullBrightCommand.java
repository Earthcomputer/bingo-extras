package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.bingoextras.BingoExtras;
import net.earthcomputer.bingoextras.ext.ServerPlayerExt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import static net.minecraft.commands.Commands.*;

public final class FullBrightCommand {
    private FullBrightCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("fullbright")
            .executes(ctx -> toggleFullBright(ctx.getSource())));
    }

    private static int toggleFullBright(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        ServerPlayerExt.setFullbright(player, !ServerPlayerExt.isFullbright(player));
        sendUpdate(player, false);

        source.sendSuccess(() -> BingoExtras.translatable("bingo_extras.fullbright.success"), false);

        return Command.SINGLE_SUCCESS;
    }

    public static MobEffectInstance createNightVisionEffect() {
        return new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0, false, false, false);
    }

    public static void sendUpdate(ServerPlayer player, boolean creating) {
        if (player.hasEffect(MobEffects.NIGHT_VISION)) {
            return;
        }
        if (ServerPlayerExt.isFullbright(player)) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), createNightVisionEffect(), false));
        } else if (!creating) {
            player.connection.send(new ClientboundRemoveMobEffectPacket(player.getId(), MobEffects.NIGHT_VISION));
        }
    }
}
