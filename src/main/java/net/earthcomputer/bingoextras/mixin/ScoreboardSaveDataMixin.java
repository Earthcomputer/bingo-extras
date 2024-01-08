package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.bingoextras.ext.PlayerTeamExt;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScoreboardSaveData.class)
public class ScoreboardSaveDataMixin {
    @Inject(method = "loadTeams", at = @At(value = "CONSTANT", args = "stringValue=Players", ordinal = 0))
    private void onLoadTeam(CallbackInfo ci, @Local PlayerTeam team, @Local CompoundTag tag) {
        GlobalPos spawnPos = null;
        if (tag.contains("bingo_extras:spawn_pos", Tag.TAG_COMPOUND)) {
            spawnPos = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("bingo_extras:spawn_pos")).result().orElse(null);
        }

        PlayerTeamExt.setTeamSpawnPos(team, spawnPos);
    }

    @Inject(method = "saveTeams", at = @At(value = "CONSTANT", args = "stringValue=Players", ordinal = 0))
    private void onSaveTeam(CallbackInfoReturnable<ListTag> cir, @Local PlayerTeam team, @Local CompoundTag tag) {
        GlobalPos teamSpawnPos = PlayerTeamExt.getTeamSpawnPos(team);
        if (teamSpawnPos != null) {
            tag.put("bingo_extras:spawn_pos", Util.getOrThrow(GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, teamSpawnPos), IllegalStateException::new));
        }
    }
}
