package net.earthcomputer.bingoextras.mixin.bingo;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BingoGame.class)
public interface BingoGameAccessor {
    @Accessor
    BingoBoard.Teams getRemainingTeams();
}
