package net.earthcomputer.bingoextras.gametest.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
    @Accessor
    @Nullable
    Vec3 getEnteredNetherPosition();

    @Accessor
    void setEnteredNetherPosition(@Nullable Vec3 enteredNetherPosition);
}
