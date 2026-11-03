package net.earthcomputer.bingoextras.mixin.bingo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.gaming32.bingo.conditions.DistanceFromSpawnCondition;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DistanceFromSpawnCondition.class)
public class DistanceFromSpawnConditionMixin {
    @ModifyExpressionValue(method = "test", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData$RespawnData;pos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos modifySpawnPoint(BlockPos original, LootContext lootContext) {
        Entity entity = lootContext.getOptional(LootContextParams.THIS_ENTITY);
        if (entity == null) {
            return original;
        }
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
