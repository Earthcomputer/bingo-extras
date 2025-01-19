package net.earthcomputer.bingoextras.mixin.bingo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "io.github.gaming32.bingo.conditions.DistanceFromSpawnCondition", remap = false)
@Pseudo
public class DistanceFromSpawnConditionMixin {
    @ModifyExpressionValue(method = "test", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;", remap = true))
    @Dynamic
    private BlockPos modifySpawnPoint(BlockPos original, LootContext lootContext) {
        Entity entity = lootContext.getParameter(LootContextParams.THIS_ENTITY);
        PlayerTeam team = entity.getTeam();
        if (team != null) {
            GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
            if (teamSpawnPos != null && teamSpawnPos.dimension() == lootContext.getLevel().dimension()) {
                return teamSpawnPos.pos();
            }
        }

        return original;
    }
}
