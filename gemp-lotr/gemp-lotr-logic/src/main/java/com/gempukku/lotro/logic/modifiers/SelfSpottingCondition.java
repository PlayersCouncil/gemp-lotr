package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.state.LotroGame;

/**
 * The condition of a modifier that a card lays on itself where what the modifier gives is what the condition
 * spots, and nothing in the text leaves the card out ("While you can spot a fierce minion, this minion is
 * fierce": Sentry Uruk 15_170, the definition word `spotsItself` of `addKeyword`).  Once the condition has held,
 * the card is itself a card the condition can spot, and keeps what the modifier gives while the condition goes
 * on holding with the card counted (Ketura on Q-0276: "without 'another' in the spot filter text, once it has
 * bootstrapped itself it must continue spotting itself").  Alone, it starts with nothing.
 * <p>
 * The game works out a modifier's condition with that modifier set aside (ModifiersLogic's skip set), so a plain
 * condition never counts its own modifier's grant.  This one remembers whether it held when it was last worked
 * out; while it is being worked out, ModifiersLogic counts its modifier as in force if it held.  It is worked out
 * at every step of the game (ModifiersLogic.checkSelfSpottingConditions, from TurnProcedure, beside the other
 * state-based checks), so what it remembers does not depend on whether anything asked about the card in between,
 * and also whenever the modifier is asked about.  What it remembers goes with the modifier: a card whose text is
 * stopped and started again (it leaves play, it is hindered and restored) starts again from nothing.
 */
public class SelfSpottingCondition implements Condition {
    private final Condition _condition;
    private boolean _held;

    public SelfSpottingCondition(Condition condition) {
        _condition = condition;
    }

    @Override
    public boolean isFullfilled(LotroGame game) {
        _held = _condition == null || _condition.isFullfilled(game);
        return _held;
    }

    /**
     * Whether the condition held when it was last worked out: what is read of it while it is being worked out.
     */
    public boolean hasHeld() {
        return _held;
    }
}
