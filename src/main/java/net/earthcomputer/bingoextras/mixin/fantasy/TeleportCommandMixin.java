package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
    @WrapOperation(method = "performTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"))
    private static boolean forceTeleport(Entity instance, ServerLevel serverLevel, double d, double e, double f, Set<RelativeMovement> set, float g, float h, Operation<Boolean> original) {
        return FantasyUtil.forceDimensionChange(() -> original.call(instance, serverLevel, d, e, f, set, g, h));
    }
}
