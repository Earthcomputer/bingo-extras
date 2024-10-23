package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private Level level;

    @Shadow
    @Nullable
    public abstract MinecraftServer getServer();

    @ModifyVariable(method = "teleport", at = @At("HEAD"), argsOnly = true)
    private TeleportTransition modifyDestDimension(TeleportTransition dest) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam((ServerLevel) level);
        PlayerTeam destLevelTeam = ServerLevelExt_Fantasy.getTeam(dest.newLevel());
        if (!FantasyUtil.isForcedDimensionChange() && currentLevelTeam != null && destLevelTeam == null) {
            dest = new TeleportTransition(
                PlayerTeamExt_Fantasy.getTeamSpecificLevel(getServer(), currentLevelTeam, dest.newLevel().dimension()),
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
}
