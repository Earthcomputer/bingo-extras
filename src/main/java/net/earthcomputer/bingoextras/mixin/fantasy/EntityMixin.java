package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private Level level;

    @ModifyVariable(method = "teleport", at = @At("HEAD"), argsOnly = true, name = "transition")
    private TeleportTransition modifyDestDimension(TeleportTransition dest) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam((ServerLevel) level);
        PlayerTeam destLevelTeam = ServerLevelExt_Fantasy.getTeam(dest.newLevel());
        if (!FantasyUtil.isForcedDimensionChange() && currentLevelTeam != null && destLevelTeam == null) {
            dest = new TeleportTransition(
                PlayerTeamExt_Fantasy.getTeamSpecificLevel(level.getServer(), currentLevelTeam, dest.newLevel().dimension()),
                dest.position(),
                dest.deltaMovement(),
                dest.yRot(),
                dest.xRot(),
                dest.missingRespawnBlock(),
                dest.asPassenger(),
                dest.relatives(),
                dest.postTeleportTransition()
            );
        }
        return dest;
    }

    // entities can't take players out of the end until they have seen the credits
    @Definition(id = "from", local = @Local(type = Level.class, argsOnly = true, ordinal = 0))
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "END", field = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(from.dimension()) == END")
    @ModifyExpressionValue(method = "canTeleport", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyCanTeleportFromDimension(ResourceKey<Level> dimension, @Local(argsOnly = true, ordinal = 0) Level from) {
        return FantasyUtil.getOriginalDimension(from);
    }

    @Definition(id = "to", local = @Local(type = Level.class, argsOnly = true, ordinal = 1))
    @Definition(id = "dimension", method = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "OVERWORLD", field = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;")
    @Expression("@(to.dimension()) == OVERWORLD")
    @ModifyExpressionValue(method = "canTeleport", at = @At("MIXINEXTRAS:EXPRESSION"))
    private ResourceKey<Level> modifyCanTeleportToDimension(ResourceKey<Level> dimension, @Local(argsOnly = true, ordinal = 1) Level to) {
        return FantasyUtil.getOriginalDimension(to);
    }
}
