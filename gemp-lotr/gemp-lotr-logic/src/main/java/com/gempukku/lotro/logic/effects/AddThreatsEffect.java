package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.results.AddThreatResult;

public class AddThreatsEffect extends AbstractEffect {
    private final String _performingPlayer;
    private final PhysicalCard _source;
    private final Evaluator _count;

    public AddThreatsEffect(String performingPlayer, PhysicalCard source, Evaluator count) {
        _performingPlayer = performingPlayer;
        _source = source;
        _count = count;
    }

    public AddThreatsEffect(String performingPlayer, PhysicalCard source, int count) {
        this(performingPlayer, source, new ConstantEvaluator(count));
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return PlayConditions.canAddThreat(game, null, evaluateCount(game));
    }

    private int evaluateCount(LotroGame game) {
        return _count.evaluateExpression(game, null);
    }

    @Override
    public String getText(LotroGame game) {
        int number = evaluateCount(game);
        return "Add " + number + " threat" + ((number > 1) ? "s" : "");
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        int count = evaluateCount(game);
        int toAdd = Math.min(count, PlayConditions.getMaxAddThreatCount(game));
        if (toAdd > 0) {
            game.getGameState().sendMessage(_performingPlayer + " adds " + GameUtils.formatNumber(toAdd, count) + " threat" + ((toAdd > 1) ? "s" : "") + " with " + GameUtils.getCardLink(_source));
            game.getGameState().addThreats(game.getGameState().getCurrentPlayerId(), toAdd);

            for (int i = 0; i < toAdd; i++)
                game.getActionsEnvironment().emitEffectResult(new AddThreatResult(_source));

            return new FullEffectResult(toAdd == count);
        }
        return new FullEffectResult(count == 0);
    }
}
