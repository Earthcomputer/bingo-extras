package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.earthcomputer.bingoextras.Configs;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomizableContainerBlockEntity.class)
public class RandomizableContainerBlockEntityMixin {
    @ModifyExpressionValue(method = "canOpen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"))
    private boolean allowSpectatorsToGenerateLoot(boolean original) {
        return !Configs.spectatorsGenerateLoot && original;
    }
}
