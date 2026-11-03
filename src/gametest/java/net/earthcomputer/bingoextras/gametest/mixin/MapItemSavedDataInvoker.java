package net.earthcomputer.bingoextras.gametest.mixin;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataInvoker {
    @Invoker
    byte invokeCalculateRotation(@Nullable LevelAccessor level, double yRot);
}
