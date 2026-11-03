package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public class LevelMixin {
    // the end has sky light, so this is the only thing stopping it from raining in the end
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(this.dimension()) != END")
    @ModifyExpressionValue(method = "canHaveWeather", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension) {
        return FantasyUtil.getOriginalDimension((Level) (Object) this);
    }
}
