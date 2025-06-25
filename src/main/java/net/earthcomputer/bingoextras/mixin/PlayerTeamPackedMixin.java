package net.earthcomputer.bingoextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.earthcomputer.bingoextras.ext.PlayerTeamPackedExt;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(PlayerTeam.Packed.class)
public class PlayerTeamPackedMixin implements PlayerTeamPackedExt {
    @Unique
    private Optional<GlobalPos> teamSpawnPos;

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;", remap = false))
    private static Codec<PlayerTeam.Packed> modifyCodec(Codec<PlayerTeam.Packed> original) {
        return RecordCodecBuilder.create(instance -> instance.group(
            MapCodec.assumeMapUnsafe(original).forGetter(Function.identity()),
            GlobalPos.CODEC.optionalFieldOf("bingo_extras:spawn_pos").forGetter(PlayerTeamPackedExt::getTeamSpawnPos)
        ).apply(instance, (packed, teamSpawnPos) -> {
            ((PlayerTeamPackedMixin) (Object) packed).teamSpawnPos = teamSpawnPos;
            return packed;
        }));
    }

    @Override
    public Optional<GlobalPos> bingoExtras$getTeamSpawnPos() {
        return teamSpawnPos;
    }

    @Override
    public void bingoExtras$setTeamSpawnPos(Optional<GlobalPos> pos) {
        teamSpawnPos = pos;
    }
}
