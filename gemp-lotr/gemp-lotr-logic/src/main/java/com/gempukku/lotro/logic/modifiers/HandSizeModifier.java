package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

import java.util.function.Supplier;

/**
 * Changes how many cards a player's hand holds: the number drawn at the start of the game, and the number
 * reconciled to at the end of each turn.
 */
public class HandSizeModifier extends AbstractModifier {
    private final Supplier<String> _player;
    private final Evaluator _evaluator;

    /**
     * @param player Supplies the player whose hand size changes, looked up each time it is needed.  If null (or if it
     *               supplies null), every player's hand size changes.
     */
    public HandSizeModifier(PhysicalCard source, Condition condition, Supplier<String> player, Evaluator evaluator) {
        super(source, "Hand size is modified", null, condition, ModifierEffect.HAND_SIZE_MODIFIER);
        _player = player;
        _evaluator = evaluator;
    }

    @Override
    public int getHandSizeModifier(LotroGame game, String playerId) {
        String affected = _player == null ? null : _player.get();
        if (affected != null && !affected.equals(playerId))
            return 0;
        return _evaluator.evaluateExpression(game, null);
    }
}
