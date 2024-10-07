package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @WrapOperation(method = "handleTeleportToEntityPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"))
    private void forceTeleport(ServerPlayer instance, ServerLevel serverLevel, double d, double e, double f, float g, float h, Operation<Void> original) {
        FantasyUtil.forceDimensionChange(() -> original.call(instance, serverLevel, d, e, f, g, h));
    }
}
