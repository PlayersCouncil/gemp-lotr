package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.ActivateCardResult;

public class ActivateCardEffect extends AbstractEffect {
    protected final PhysicalCard _source;
    protected final Timeword _actionTimeword;

    private final String _player;

    protected ActivateCardResult _activateCardResult;

    public ActivateCardEffect(PhysicalCard source, String performingPlayer, Timeword actionTimeword) {
        _source = source;
        _actionTimeword = actionTimeword;
        _player = performingPlayer;
        _activateCardResult = new ActivateCardResult(_source, performingPlayer, _actionTimeword);
    }

    public ActivateCardResult getActivateCardResult() {
        return _activateCardResult;
    }

    public Timeword getActionTimeword() {
        return _actionTimeword;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        game.getActionsEnvironment().emitEffectResult(_activateCardResult);
        return new FullEffectResult(true);
    }

    public String getPerformingPlayer() {
        return _player;
    }
}
