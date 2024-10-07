package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({NetherPortalBlock.class, EndPortalBlock.class})
public class PortalBlocksMixin {
    @ModifyVariable(method = "getPortalDestination", at = @At("STORE"), ordinal = 1)
    private ServerLevel modifyDestLevel(ServerLevel destLevel, ServerLevel sourceLevel) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam(sourceLevel);
        PlayerTeam destLevelTeam = ServerLevelExt_Fantasy.getTeam(destLevel);
        if (!FantasyUtil.isForcedDimensionChange() && currentLevelTeam != null && destLevelTeam == null) {
            destLevel = PlayerTeamExt_Fantasy.getTeamSpecificLevel(destLevel.getServer(), currentLevelTeam, destLevel.dimension());
        }
        return destLevel;
    }
}
