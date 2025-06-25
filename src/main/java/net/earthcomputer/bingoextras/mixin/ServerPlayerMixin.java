package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.earthcomputer.bingoextras.command.FullBrightCommand;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.earthcomputer.bingoextras.ext.ServerPlayerExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerExt {
    @Unique
    private boolean fullbright = false;

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

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onLoadNbt(CompoundTag tag, CallbackInfo ci) {
        fullbright = tag.getBooleanOr("bingoextras:fullbright", false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void onSaveNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("bingoextras:fullbright", fullbright);
    }

    @ModifyArg(method = "onEffectsRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private Packet<?> modifyRemoveEffectPacket(Packet<?> packet, @Local MobEffectInstance effect) {
        if (fullbright && effect.is(MobEffects.NIGHT_VISION)) {
            return new ClientboundUpdateMobEffectPacket(getId(), FullBrightCommand.createNightVisionEffect(), false);
        }
        return packet;
    }

    @Override
    public boolean bingoExtras$isFullbright() {
        return fullbright;
    }

    @Override
    public void bingoExtras$setFullbright(boolean fullbright) {
        this.fullbright = fullbright;
    }
}
