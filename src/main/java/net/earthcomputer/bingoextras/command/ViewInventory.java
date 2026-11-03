package net.earthcomputer.bingoextras.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.earthcomputer.bingoextras.BingoExtras;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

import static net.minecraft.commands.Commands.*;

public final class ViewInventory {
    private static final SimpleCommandExceptionType NOT_SPECTATOR_EXCEPTION = new SimpleCommandExceptionType(BingoExtras.translatable("bingo_extras.viewinventory.notSpectator"));

    private static final int ROWS = 5;

    // Maps each slot of the 9x5 chest GUI to a slot in the target's inventory, or -1 for filler.
    // Row 0: head, chest, legs, feet, (gap), offhand. Rows 1-3: main inventory. Row 4: hotbar.
    private static final int[] SLOT_MAPPING = new int[ROWS * 9];
    static {
        Arrays.fill(SLOT_MAPPING, -1);
        SLOT_MAPPING[0] = 39;
        SLOT_MAPPING[1] = 38;
        SLOT_MAPPING[2] = 37;
        SLOT_MAPPING[3] = 36;
        SLOT_MAPPING[5] = Inventory.SLOT_OFFHAND;
        for (int i = 9; i < Inventory.INVENTORY_SIZE; i++) {
            SLOT_MAPPING[i] = i;
        }
        for (int i = 0; i < Inventory.SELECTION_SIZE; i++) {
            SLOT_MAPPING[Inventory.INVENTORY_SIZE + i] = i;
        }
    }

    private ViewInventory() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("viewinventory")
            .then(argument("player", EntityArgument.player())
                .executes(ctx -> viewInventory(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
    }

    private static int viewInventory(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer viewer = source.getPlayerOrException();
        if (!viewer.isSpectator()) {
            throw NOT_SPECTATOR_EXCEPTION.create();
        }

        MinecraftServer server = source.getServer();
        UUID targetUuid = target.getUUID();
        viewer.openMenu(new SimpleMenuProvider(
            (containerId, inventory, _) -> new ViewMenu(containerId, inventory, new InventoryView(server, targetUuid)),
            BingoExtras.translatable("bingo_extras.viewinventory.title", target.getDisplayName())
        ));

        return Command.SINGLE_SUCCESS;
    }

    private static final class ViewMenu extends ChestMenu {
        private final InventoryView view;

        ViewMenu(int containerId, Inventory inventory, InventoryView view) {
            super(MenuType.GENERIC_9x5, containerId, inventory, view, ROWS);
            this.view = view;
        }

        @Override
        public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return player.isSpectator() && view.getTarget() != null;
        }
    }

    private record InventoryView(MinecraftServer server, UUID targetUuid) implements Container {
        private static final ItemStack FILLER = Util.make(new ItemStack(Items.STAINED_GLASS_PANE.pick(DyeColor.GRAY)), stack -> stack.set(DataComponents.CUSTOM_NAME, Component.empty()));

        @Nullable ServerPlayer getTarget() {
            return server.getPlayerList().getPlayer(targetUuid);
        }

        @Override
        public int getContainerSize() {
            return SLOT_MAPPING.length;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int slot) {
            int targetSlot = SLOT_MAPPING[slot];
            if (targetSlot == -1) {
                return FILLER;
            }
            ServerPlayer target = getTarget();
            return target == null ? ItemStack.EMPTY : target.getInventory().getItem(targetSlot);
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack itemStack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return getTarget() != null;
        }

        @Override
        public void clearContent() {
        }
    }
}
