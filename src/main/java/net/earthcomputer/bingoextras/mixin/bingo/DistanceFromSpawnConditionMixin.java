package net.earthcomputer.bingoextras.mixin.bingo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
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
    @ModifyExpressionValue(method = "name=/^test$/desc=/^\\(Lnet.*\\)Z$/", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CompassItem;getSpawnPosition(Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/GlobalPos;", remap = true))
    @Dynamic
    private GlobalPos modifySpawnPoint(GlobalPos original, LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        PlayerTeam team = entity.getTeam();
        if (team != null) {
            GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
            if (teamSpawnPos != null) {
                return teamSpawnPos;
            }
        }

        return original;
    }
}
