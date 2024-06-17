package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos applyTeamSpawnPosInConstructor(BlockPos original, MinecraftServer server, ServerLevel level, GameProfile gameProfile, ClientInformation clientInformation) {
        PlayerTeam team = server.getScoreboard().getPlayerTeam(gameProfile.getName());
        if (team != null) {
            GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
            if (teamSpawnPos != null) {
                return teamSpawnPos.pos();
            }
        }

        return original;
    }

    @ModifyVariable(method = "adjustSpawnLocation", at = @At(value = "HEAD"), argsOnly = true)
    private BlockPos applyTeamSpawnPos(BlockPos original) {
        PlayerTeam team = getTeam();
        if (team != null) {
            GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
            if (teamSpawnPos != null) {
                return teamSpawnPos.pos();
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel respawnTeamInDimension(ServerLevel original) {
        PlayerTeam team = this.getTeam();
        if (team != null) {
            GlobalPos respawnPos = PlayerTeamExt.getTeamSpawnPos(team);
            if (respawnPos != null) {
                ServerLevel level = this.getServer().getLevel(respawnPos.dimension());
                if (level != null) {
                    return level;
                }
            }
        }

        return original;
    }
}
