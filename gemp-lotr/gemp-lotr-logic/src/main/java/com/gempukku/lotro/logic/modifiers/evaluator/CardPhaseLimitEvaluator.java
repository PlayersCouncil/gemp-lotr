package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.LimitCounter;

public class CardPhaseLimitEvaluator implements Evaluator {
    private Integer _evaluated;

    private final Evaluator _amount;

    private final PhysicalCard _source;
    private final Phase _phase;
    private final Evaluator _limit;
    private final boolean _limitCanBeRead;

    public CardPhaseLimitEvaluator(PhysicalCard source, Phase phase, Evaluator limit, Evaluator amount) {
        this(source, phase, limit, amount, limit instanceof ConstantEvaluator);
    }

    /**
     * @param limitCanBeRead Whether the limit can be worked out beforehand without changing the game: true for
     * any limit that does not itself count on a limit (a number, or a value such as a Conditional that only reads
     * the board).  Only then is the cap read when the no-effect flag is judged.
     */
    public CardPhaseLimitEvaluator(PhysicalCard source, Phase phase, Evaluator limit, Evaluator amount, boolean limitCanBeRead) {
        _source = source;
        _phase = phase;
        _limit = limit;
        _amount = amount;
        _limitCanBeRead = limitCanBeRead;
    }

    // The limit is how much, whichever way the amount goes: "(limit +3)" on a bonus and "(limit -2)" on a
    // discount are limits of 3 and of 2, and the amount keeps its own sign.  (limitPerCardThisPhase on
    // ModifyStrength is read the same way.)
    private int evaluateOnce(LotroGame game, PhysicalCard cardAffected) {
        LimitCounter limitCounter = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_source, _phase);
        int amountResult = _amount.evaluateExpression(game, cardAffected);
        // A limit given by a value may fall below what the card has already given ("limit +2, or +5 if you can
        // spot ...", and the thing spotted has gone).  Nothing is left to give then, and nothing is taken back.
        int limitResult = Math.max(Math.abs(_limit.evaluateExpression(game, cardAffected)), limitCounter.getUsedLimit());
        if (amountResult < 0)
            return -limitCounter.incrementToLimit(limitResult, -amountResult);
        return limitCounter.incrementToLimit(limitResult, amountResult);
    }

    @Override
    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
        if (_evaluated == null)
            _evaluated = evaluateOnce(game, cardAffected);
        return _evaluated;
    }

    @Override
    public int evaluateWithoutSpending(LotroGame game, PhysicalCard cardAffected) {
        if (_evaluated != null)
            return _evaluated;
        // What evaluateOnce would give, with the counter read and not counted on.
        final int used = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_source, _phase).getUsedLimit();
        final int amountResult = _amount.evaluateWithoutSpending(game, cardAffected);
        final int left = Math.max(0, Math.abs(_limit.evaluateWithoutSpending(game, cardAffected)) - used);
        if (amountResult < 0)
            return -Math.min(left, -amountResult);
        return Math.min(left, amountResult);
    }

    @Override
    public boolean isSpent(LotroGame game, PhysicalCard cardAffected) {
        // The limit is read only where working it out cannot change the game: a plain number, or a value that
        // only reads the board ("limit +3, or +6 if you can spot ...").  A limit that itself counts on a limit
        // is not asked.
        if (_evaluated != null || !_limitCanBeRead)
            return false;
        return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_source, _phase).getUsedLimit() >= Math.abs(_limit.evaluateExpression(game, cardAffected));
    }
}
