package net.earthcomputer.bingoextras.mixin;

import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> modifyClientboundPacket(Packet<?> packet) {
        if (!((Object) this instanceof ServerGamePacketListenerImpl game)) {
            return packet;
        }

        if (packet instanceof ClientboundSetDefaultSpawnPositionPacket spawnPosPacket) {
            PlayerTeam team = game.player.getTeam();
            if (team != null) {
                GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
                if (teamSpawnPos != null && teamSpawnPos.dimension() == Level.OVERWORLD) {
                    return new ClientboundSetDefaultSpawnPositionPacket(teamSpawnPos.pos(), spawnPosPacket.getAngle());
                }
            }
        }

        return packet;
    }
}
