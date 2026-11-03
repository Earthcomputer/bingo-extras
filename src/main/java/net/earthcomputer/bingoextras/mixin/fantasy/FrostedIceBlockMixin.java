package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FrostedIceBlock.class)
public class FrostedIceBlockMixin {
    // frosted ice only melts from block light in the end
    @Definition(id = "level", local = @Local(type = ServerLevel.class, argsOnly = true))
    @Definition(id = "dimension", method = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(level.dimension()) == END")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension, @Local(argsOnly = true) ServerLevel level) {
        return FantasyUtil.getOriginalDimension(level);
    }
}
