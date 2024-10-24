package net.earthcomputer.bingoextras;

import com.demonwav.mcdev.annotations.Translatable;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.earthcomputer.bingoextras.command.BingoExtrasCommands;
import net.earthcomputer.bingoextras.command.FullBrightCommand;
import net.earthcomputer.bingoextras.ext.ServerPlayerExt;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class BingoExtras implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> BingoExtrasCommands.register(dispatcher, context));
        new ModConfigBuilder<>("bingoextras", Configs.class).build();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> FullBrightCommand.sendUpdate(handler.player, true));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> FullBrightCommand.sendUpdate(newPlayer, true));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ServerPlayerExt.setFullbright(newPlayer, ServerPlayerExt.isFullbright(oldPlayer)));
    }

    public static MutableComponent translatable(@Translatable String translationKey) {
        return ensureHasFallback(Component.translatable(translationKey));
    }

    public static MutableComponent translatable(@Translatable String translationKey, Object... args) {
        return ensureHasFallback(Component.translatable(translationKey, args));
    }

    public static MutableComponent ensureHasFallback(MutableComponent component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatable) {
            if (translatable.getFallback() == null) {
                MutableComponent result = Component.translatableWithFallback(translatable.getKey(), component.getString(), translatable.getArgs());
                result.getSiblings().addAll(component.getSiblings());
                return result;
            }
        }
        return component;
    }
}
