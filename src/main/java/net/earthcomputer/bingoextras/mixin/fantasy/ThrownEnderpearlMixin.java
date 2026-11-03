package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {
    // ender pearls can't leave the end until their owner has seen the credits
    @Definition(id = "from", local = @Local(type = Level.class, argsOnly = true, ordinal = 0))
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(from.dimension()) == END")
    @ModifyExpressionValue(method = "canTeleport", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyFromDimension(ResourceKey<Level> dimension, @Local(argsOnly = true, ordinal = 0) Level from) {
        return FantasyUtil.getOriginalDimension(from);
    }

    @Definition(id = "to", local = @Local(type = Level.class, argsOnly = true, ordinal = 1))
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "OVERWORLD", field = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(to.dimension()) == OVERWORLD")
    @ModifyExpressionValue(method = "canTeleport", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyToDimension(ResourceKey<Level> dimension, @Local(argsOnly = true, ordinal = 1) Level to) {
        return FantasyUtil.getOriginalDimension(to);
    }
}
