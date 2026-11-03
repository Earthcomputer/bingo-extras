package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    // whether lighting fire can create a nether portal
    @Definition(id = "level", local = @Local(type = Level.class, argsOnly = true))
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "OVERWORLD", field = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "NETHER", field = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;")
    @Expression({"@(level.dimension()) == OVERWORLD", "@(level.dimension()) == NETHER"})
    @ModifyExpressionValue(method = "inPortalDimension", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension, @Local(argsOnly = true) Level level) {
        return FantasyUtil.getOriginalDimension(level);
    }
}
