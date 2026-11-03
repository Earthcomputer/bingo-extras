package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {
    // player markers on maps spin in the nether
    @Definition(id = "dimension", field = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;dimension:Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "NETHER", field = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(this.dimension) == NETHER")
    @ModifyExpressionValue(method = "calculateRotation", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyDimension(ResourceKey<Level> dimension, @Local(argsOnly = true) @Nullable LevelAccessor level) {
        return level == null ? dimension : FantasyUtil.getOriginalDimension(level.getServer(), dimension);
    }
}
