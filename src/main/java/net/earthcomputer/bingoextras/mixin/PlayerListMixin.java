package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;

    @ModifyExpressionValue(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel respawnTeamInDimension(ServerLevel original, ServerPlayer player, boolean wonGame) {
        PlayerTeam team = player.getTeam();
        if (team != null) {
            GlobalPos respawnPos = PlayerTeamExt.getTeamSpawnPos(team);
            if (respawnPos != null) {
                ServerLevel level = server.getLevel(respawnPos.dimension());
                if (level != null) {
                    return level;
                }
            }
        }

        return original;
    }
}
