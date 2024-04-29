package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
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

    @ModifyVariable(method = "changeDimension", at = @At("HEAD"), argsOnly = true)
    private ServerLevel modifyDestDimension(ServerLevel dest) {
        PlayerTeam currentLevelTeam = ServerLevelExt_Fantasy.getTeam((ServerLevel) level);
        if (currentLevelTeam != null) {
            dest = PlayerTeamExt_Fantasy.getTeamSpecificLevel(getServer(), currentLevelTeam, dest.dimension());
        }
        return dest;
    }
}
