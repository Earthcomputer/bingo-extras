package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @ModifyVariable(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), argsOnly = true, name = "transition")
    private TeleportTransition modifyDestDimension(TeleportTransition dest) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam((ServerLevel) level());
        PlayerTeam destLevelTeam = ServerLevelExt_Fantasy.getTeam(dest.newLevel());
        if (!FantasyUtil.isForcedDimensionChange() && currentLevelTeam != null && destLevelTeam == null) {
            dest = new TeleportTransition(
                PlayerTeamExt_Fantasy.getTeamSpecificLevel(level().getServer(), currentLevelTeam, dest.newLevel().dimension()),
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

    @ModifyVariable(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"), argsOnly = true, name = "level")
    private ServerLevel modifyDestDimension(ServerLevel dest) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam((ServerLevel) level());
        PlayerTeam destLevelTeam = ServerLevelExt_Fantasy.getTeam(dest);
        if (!FantasyUtil.isForcedDimensionChange() && currentLevelTeam != null && destLevelTeam == null) {
            dest = PlayerTeamExt_Fantasy.getTeamSpecificLevel(level().getServer(), currentLevelTeam, dest.dimension());
        }
        return dest;
    }

    @WrapOperation(method = "setCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z"))
    private boolean forceDimensionChange(ServerPlayer instance, ServerLevel level, double x, double y, double z, Set<Relative> relatives, float newYRot, float newXRot, boolean resetCamera, Operation<Boolean> original) {
        return FantasyUtil.forceDimensionChange(() -> original.call(instance, level, x, y, z, relatives, newYRot, newXRot, resetCamera));
    }

    // remembers where the player entered the nether, for the nether travel advancement
    @Definition(id = "lastDimension", local = @Local(type = ResourceKey.class, name = "lastDimension"))
    @Definition(id = "dimension", method = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "OVERWORLD", field = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "NETHER", field = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;")
    @Expression({"@(lastDimension) == OVERWORLD", "@(?.dimension()) == NETHER"})
    @ModifyExpressionValue(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("MIXINEXTRAS:EXPRESSION"), require = 2)
    private ResourceKey<Level> modifyTeleportDimension(ResourceKey<Level> dimension) {
        return FantasyUtil.getOriginalDimension(level().getServer(), dimension);
    }

    // the nether travel advancement
    @Definition(id = "oldKey", local = @Local(type = ResourceKey.class, name = "oldKey"))
    @Definition(id = "newKey", local = @Local(type = ResourceKey.class, name = "newKey"))
    @Definition(id = "OVERWORLD", field = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;")
    @Definition(id = "NETHER", field = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;")
    @Expression({"@(oldKey) == NETHER", "@(newKey) == OVERWORLD", "@(newKey) != NETHER"})
    @ModifyExpressionValue(method = "triggerDimensionChangeTriggers", at = @At("MIXINEXTRAS:EXPRESSION"), require = 3)
    private ResourceKey<Level> modifyTriggerDimension(ResourceKey<Level> dimension) {
        return FantasyUtil.getOriginalDimension(level().getServer(), dimension);
    }
}
