package net.earthcomputer.bingoextras.mixin.fantasy;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.earthcomputer.bingoextras.ext.fantasy.ServerLevelExt_Fantasy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements ServerLevelExt_Fantasy {
    @Shadow
    @Final
    private ServerLevelData serverLevelData;

    @Unique
    @Nullable
    private PlayerTeam team = null;

    @Unique
    @Nullable
    private ServerLevel originalLevel = null;

    @Override
    @Nullable
    public PlayerTeam bingoExtras$getTeam() {
        return team;
    }

    @Override
    public void bingoExtras$setTeam(@Nullable PlayerTeam team) {
        this.team = team;
    }

    @Override
    @Nullable
    public ServerLevel bingoExtras$getOriginalLevel() {
        return originalLevel;
    }

    @Override
    public void bingoExtras$setOriginalLevel(@Nullable ServerLevel level) {
        this.originalLevel = level;
    }

    @Inject(method = "advanceWeatherCycle", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;oThunderLevel:F", ordinal = 0))
    private void copyWeatherFromOriginal(CallbackInfo ci) {
        if (originalLevel != null) {
            ServerLevelData originalData = (ServerLevelData) originalLevel.getLevelData();
            serverLevelData.setClearWeatherTime(originalData.getClearWeatherTime());
            serverLevelData.setRainTime(originalData.getRainTime());
            serverLevelData.setThunderTime(originalData.getThunderTime());
            serverLevelData.setRaining(originalData.isRaining());
            serverLevelData.setThundering(originalData.isThundering());
        }
    }

    @Inject(method = "canSleepThroughNights", at = @At("HEAD"), cancellable = true)
    private void preventSleepingInSubworlds(CallbackInfoReturnable<Boolean> cir) {
        if (originalLevel != null) {
            cir.setReturnValue(false);
        }
    }

    @WrapWithCondition(method = "saveLevelData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;setEndDragonFightData(Lnet/minecraft/world/level/dimension/end/EndDragonFight$Data;)V"))
    private boolean shouldSaveEndDragonFight(WorldData instance, EndDragonFight.Data data) {
        return originalLevel == null;
    }
}
