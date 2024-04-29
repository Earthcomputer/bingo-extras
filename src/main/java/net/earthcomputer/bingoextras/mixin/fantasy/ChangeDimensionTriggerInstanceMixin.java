package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChangeDimensionTrigger.TriggerInstance.class)
public class ChangeDimensionTriggerInstanceMixin {
    @ModifyVariable(method = "matches", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ResourceKey<Level> modifyDimension1(ResourceKey<Level> dimension) {
        MinecraftServer server = FantasyUtil.currentServer.get();
        if (server != null) {
            return FantasyUtil.getOriginalDimension(server, dimension);
        } else {
            return dimension;
        }
    }

    @ModifyVariable(method = "matches", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private ResourceKey<Level> modifyDimension2(ResourceKey<Level> dimension) {
        MinecraftServer server = FantasyUtil.currentServer.get();
        if (server != null) {
            return FantasyUtil.getOriginalDimension(server, dimension);
        } else {
            return dimension;
        }
    }
}
