package net.earthcomputer.bingoextras.mixin;

import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements PlayerTeamExt {
    @Shadow @Final private Scoreboard scoreboard;
    @Unique
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
}
