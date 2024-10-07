package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.earthcomputer.bingoextras.FantasyUtil;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @ModifyVariable(method = "changeDimension", at = @At("HEAD"), argsOnly = true)
    private DimensionTransition modifyDestDimension(DimensionTransition dest) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam((ServerLevel) level());
        PlayerTeam destLevelTeam = ServerLevelExt_Fantasy.getTeam(dest.newLevel());
        if (!FantasyUtil.isForcedDimensionChange() && currentLevelTeam != null && destLevelTeam == null) {
            dest = new DimensionTransition(
                    PlayerTeamExt_Fantasy.getTeamSpecificLevel(getServer(), currentLevelTeam, dest.newLevel().dimension()),
                    dest.pos(),
                    dest.speed(),
                    dest.yRot(),
                    dest.xRot(),
                    dest.missingRespawnBlock(),
                    dest.postDimensionTransition()
            );
        }
        return dest;
    }

    @WrapOperation(method = "setCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"))
    private boolean forceDimensionChange(ServerPlayer instance, ServerLevel serverLevel, double d, double e, double f, Set<RelativeMovement> set, float g, float h, Operation<Boolean> original) {
        return FantasyUtil.forceDimensionChange(() -> original.call(instance, serverLevel, d, e, f, set, g, h));
    }
}
