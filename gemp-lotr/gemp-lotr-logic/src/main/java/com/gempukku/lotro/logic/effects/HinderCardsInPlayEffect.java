package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.results.HinderedResult;

import java.util.Collection;

public class HinderCardsInPlayEffect extends AbstractPreventableCardEffect {
    private PhysicalCard _source;
    private final String _performingPlayer;
    private String _sourceText;

    public HinderCardsInPlayEffect(String performingPlayer, PhysicalCard source, PhysicalCard... cards) {
        super(cards);
        _performingPlayer = performingPlayer;
        if (source != null) {
            _source = source;
            _sourceText = GameUtils.getCardLink(source);
        }
    }

    public void setSourceText(String sourceText) {
        _sourceText = sourceText;
    }

    @Override
    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    protected Filter getExtraAffectableFilter() {
        return (game, physicalCard) -> game.getGameState().canBeHindered(physicalCard);
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

    @Override
    public Type getType() {
        return Type.BEFORE_HINDER;
    }

    @Override
    public String getText(LotroGame game) {
        Collection<PhysicalCard> cards = getAffectedCardsMinusPrevented(game);
        return "Hinder " + getAppendedTextNames(cards);
    }

    @Override
    protected void playoutEffectOn(LotroGame game, Collection<PhysicalCard> cards) {
        if (!cards.isEmpty()) {
            game.getGameState().sendMessage(_sourceText + " hinders " + getAppendedNames(cards));
        }

        game.getGameState().hinder(cards);

        for (var card : cards) {
            game.getActionsEnvironment().emitEffectResult(new HinderedResult(_source, _performingPlayer, card));
        }
    }
}
