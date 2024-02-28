package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.earthcomputer.bingoextras.BingoExtras;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import static net.minecraft.commands.Commands.*;

public final class FullBrightCommand {
    private static final DynamicCommandExceptionType NOT_LIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType(entity -> BingoExtras.translatable("bingo_extras.fullbright.notLivingEntity", entity));

    private FullBrightCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("fullbright")
            .executes(ctx -> toggleFullBright(ctx.getSource())));
    }

    private static int toggleFullBright(CommandSourceStack source) throws CommandSyntaxException {
        Entity entity = source.getEntityOrException();
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw NOT_LIVING_ENTITY_EXCEPTION.create(entity.getName());
        }

        if (livingEntity.hasEffect(MobEffects.NIGHT_VISION)) {
            livingEntity.removeEffect(MobEffects.NIGHT_VISION);
        } else {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
        }

        return Command.SINGLE_SUCCESS;
    }
}
