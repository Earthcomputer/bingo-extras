package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TheEndGatewayBlockEntity.class)
public class TheEndGatewayBlockEntityMixin {
    // gateways without an exit find one in the outer end islands
    @Definition(id = "currentLevel", local = @Local(type = ServerLevel.class, argsOnly = true))
    @Definition(id = "dimension", method = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(currentLevel.dimension()) == END")
    @ModifyExpressionValue(method = "getPortalPosition", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension, @Local(argsOnly = true) ServerLevel currentLevel) {
        return FantasyUtil.getOriginalDimension(currentLevel);
    }
}
