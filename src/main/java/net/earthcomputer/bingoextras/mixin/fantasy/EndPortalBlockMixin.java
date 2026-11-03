package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    // the credits are shown when first leaving the end
    @Definition(id = "level", local = @Local(type = Level.class, argsOnly = true))
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(level.dimension()) == END")
    @ModifyExpressionValue(method = "entityInside", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyDimensionForCredits(ResourceKey<Level> dimension, @Local(argsOnly = true) Level level) {
        return FantasyUtil.getOriginalDimension(level);
    }

    // whether the portal leads out of the end or into it
    @Definition(id = "currentDimension", local = @Local(type = ResourceKey.class, name = "currentDimension"))
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(currentDimension) == END")
    @ModifyExpressionValue(method = "getPortalDestination", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyDimensionForDestination(ResourceKey<Level> dimension, @Local(argsOnly = true) ServerLevel currentLevel) {
        return FantasyUtil.getOriginalDimension(currentLevel.getServer(), dimension);
    }
}
