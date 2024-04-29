package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocationPredicate.class)
public class LocationPredicateMixin {
    @ModifyExpressionValue(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> original, ServerLevel level) {
        return FantasyUtil.getOriginalDimension(level.getServer(), original);
    }
}
