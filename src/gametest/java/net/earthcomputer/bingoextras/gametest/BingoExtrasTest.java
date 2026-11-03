package net.earthcomputer.bingoextras.gametest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class BingoExtrasTest implements ModInitializer {
    public static final String MOD_ID = "bingo-extras-test";

    @Override
    public void onInitialize() {
        // the bingo datapack, loaded from resourcepacks/bingo
        ResourceLoader.registerBuiltinPack(
            Identifier.fromNamespaceAndPath(MOD_ID, "bingo"),
            FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
            PackActivationType.ALWAYS_ENABLED
        );
    }
}
