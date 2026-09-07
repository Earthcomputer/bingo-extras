package net.earthcomputer.bingoextras.mixin.fantasy;

import net.earthcomputer.bingoextras.FantasyUtil;
import net.minecraft.advancements.triggers.ChangeDimensionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChangeDimensionTrigger.TriggerInstance.class)
public class ChangeDimensionTriggerInstanceMixin {
    @ModifyVariable(method = "matches", at = @At("HEAD"), argsOnly = true, name = "from")
    private ResourceKey<Level> modifyDimension1(ResourceKey<Level> from) {
        MinecraftServer server = FantasyUtil.currentServer.get();
        if (server != null) {
            return FantasyUtil.getOriginalDimension(server, from);
        } else {
            return from;
        }
    }

    @ModifyVariable(method = "matches", at = @At("HEAD"), argsOnly = true, name = "to")
    private ResourceKey<Level> modifyDimension2(ResourceKey<Level> _to) {
        MinecraftServer server = FantasyUtil.currentServer.get();
        if (server != null) {
            return FantasyUtil.getOriginalDimension(server, _to);
        } else {
            return _to;
        }
    }
}
