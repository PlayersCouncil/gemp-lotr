package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.Preventable;
import com.gempukku.lotro.logic.timing.results.AddTwilightResult;

public class AddTwilightEffect extends AbstractEffect implements Preventable {
    public enum Cause {
        AMBUSH, CARD_EFFECT, CARD_PLAY, MOVE;

        public static AddTwilightEffect.Cause parse(String name) {
            String nameCaps = name.toUpperCase().trim()
                    .replace(' ', '_')
                    .replace('-', '_');

            for (AddTwilightEffect.Cause cause : values()) {
                if (cause.toString().equals(nameCaps))
                    return cause;
            }
            return null;
        }
    }

    private final PhysicalCard _source;
    private final Evaluator _twilight;
    private boolean _prevented;
    private String _sourceText;

    private final Cause _cause;

    public AddTwilightEffect(PhysicalCard source, Cause cause, Evaluator twilight) {
        _source = source;
        _twilight = twilight;
        if (source != null)
            _sourceText = GameUtils.getCardLink(source);
        _cause = cause;
    }

    public AddTwilightEffect(PhysicalCard source, Cause cause, int twilight) {
        this(source, cause, new ConstantEvaluator(twilight));
    }

    public void setSourceText(String sourceText) {
        _sourceText = sourceText;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    public String getText(LotroGame game) {
        return "Add (" + getTwilightToAdd(game) + ")";
    }

    public Cause getCause() {
        return _cause;
    }

    @Override
    public Effect.Type getType() {
        return Effect.Type.BEFORE_ADD_TWILIGHT;
    }

    @Override
    public boolean isPrevented(LotroGame game) {
        return _prevented;
    }

    @Override
    public void prevent() {
        _prevented = true;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (!isPrevented(game)) {
            int count = getTwilightToAdd(game);
            game.getGameState().sendMessage(_sourceText + " added " + count + " twilight");
            game.getGameState().addTwilight(count);
            game.getActionsEnvironment().emitEffectResult(new AddTwilightResult(_source, _cause, count));
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    private int getTwilightToAdd(LotroGame game) {
        return _twilight.evaluateExpression(game, null);
    }
}
