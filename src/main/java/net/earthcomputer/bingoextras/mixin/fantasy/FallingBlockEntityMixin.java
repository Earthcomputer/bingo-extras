package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    // falling blocks get an extra tick when going to or from the end
    @Definition(id = "oldDimension", local = @Local(type = ResourceKey.class, name = "oldDimension"))
    @Definition(id = "newDimension", local = @Local(type = ResourceKey.class, name = "newDimension"))
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression({"@(oldDimension) == END", "@(newDimension) == END"})
    @ModifyExpressionValue(method = "teleport", at = @At("MIXINEXTRAS:EXPRESSION"), require = 2)
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension, @Local(argsOnly = true) TeleportTransition transition) {
        return FantasyUtil.getOriginalDimension(transition.newLevel().getServer(), dimension);
    }
}
