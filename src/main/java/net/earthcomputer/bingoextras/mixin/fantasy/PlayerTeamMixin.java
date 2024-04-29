package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.ext.fantasy.PlayerTeamExt_Fantasy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements PlayerTeamExt_Fantasy {
    @Unique
    private final Map<ResourceKey<Level>, RuntimeWorldHandle> teamSpecificLevels = new HashMap<>();

    @Override
    public Map<ResourceKey<Level>, RuntimeWorldHandle> bingoExtras$getTeamSpecificLevels() {
        return teamSpecificLevels;
    }
}
