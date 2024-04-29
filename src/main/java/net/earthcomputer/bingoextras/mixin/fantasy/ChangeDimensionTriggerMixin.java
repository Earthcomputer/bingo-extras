package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(ChangeDimensionTrigger.class)
public class ChangeDimensionTriggerMixin {
    @WrapOperation(method = "trigger", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ChangeDimensionTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Predicate;)V"))
    private void storeServer(ChangeDimensionTrigger instance, ServerPlayer player, Predicate<ChangeDimensionTrigger.TriggerInstance> predicate, Operation<Void> original) {
        MinecraftServer prevServer = FantasyUtil.currentServer.get();
        FantasyUtil.currentServer.set(player.server);
        try {
            original.call(instance, player, predicate);
        } finally {
            FantasyUtil.currentServer.set(prevServer);
        }
    }
}
