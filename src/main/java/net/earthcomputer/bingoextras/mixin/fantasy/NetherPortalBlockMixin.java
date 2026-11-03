package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    // which dimension the portal leads to (PortalBlocksMixin then redirects it to the team specific level), and whether
    // to search for and create the exit portal as if it were in the nether
    @Definition(id = "dimension", method = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "NETHER", field = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(?.dimension()) == NETHER")
    @ModifyExpressionValue(method = "getPortalDestination", at = @At(value = "MIXINEXTRAS:EXPRESSION"), require = 2)
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension, @Local(argsOnly = true) ServerLevel currentLevel) {
        return FantasyUtil.getOriginalDimension(currentLevel.getServer(), dimension);
    }
}
