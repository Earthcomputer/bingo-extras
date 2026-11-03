package net.earthcomputer.bingoextras.mixin;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
    @Shadow
    @Final
    protected MinecraftServer server;

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), argsOnly = true, name = "packet")
    private Packet<?> modifyClientboundPacket(Packet<?> packet) {
        if (!((Object) this instanceof ServerGamePacketListenerImpl game)) {
            return packet;
        }

        if (packet instanceof ClientboundSetDefaultSpawnPositionPacket spawnPosPacket) {
            PlayerTeam team = game.player.getTeam();
            if (team != null) {
                GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
                if (teamSpawnPos != null && isOverworld(teamSpawnPos.dimension())) {
                    return new ClientboundSetDefaultSpawnPositionPacket(new LevelData.RespawnData(teamSpawnPos, spawnPosPacket.respawnData().yaw(), spawnPosPacket.respawnData().pitch()));
                }
            }
        }

        return packet;
    }

    @Unique
    private boolean isOverworld(ResourceKey<Level> dimension) {
        // FantasyUtil can only be loaded when Fantasy is installed
        if (FabricLoader.getInstance().isModLoaded("fantasy")) {
            dimension = FantasyUtil.getOriginalDimension(server, dimension);
        }
        return dimension == Level.OVERWORLD;
    }
}
