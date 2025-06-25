package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.earthcomputer.bingoextras.ext.PlayerTeamPackedExt;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements PlayerTeamExt {
    @Shadow
    @Final
    private Scoreboard scoreboard;
    @Unique
    @Nullable
    private GlobalPos teamSpawnPos;

    @Override
    @Nullable
    public GlobalPos bingoExtras$getTeamSpawnPos() {
        return teamSpawnPos;
    }

    @Override
    public void bingoExtras$setTeamSpawnPos(@Nullable GlobalPos pos) {
        this.teamSpawnPos = pos;
        scoreboard.onTeamChanged((PlayerTeam) (Object) this);
    }

    @ModifyReturnValue(method = "pack", at = @At("RETURN"))
    private PlayerTeam.Packed onPack(PlayerTeam.Packed packed) {
        PlayerTeamPackedExt.setTeamSpawnPos(packed, Optional.ofNullable(teamSpawnPos));
        return packed;
    }
}
