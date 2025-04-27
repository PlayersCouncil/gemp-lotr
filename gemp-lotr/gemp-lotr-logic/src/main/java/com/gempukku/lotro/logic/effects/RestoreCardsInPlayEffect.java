package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.results.RestoredResult;

import java.util.Collection;

public class RestoreCardsInPlayEffect extends AbstractPreventableCardEffect {
    private PhysicalCard _source;
    private String _sourceText;

    public RestoreCardsInPlayEffect(PhysicalCard source, PhysicalCard... cards) {
        super(cards);
        if (source != null) {
            _source = source;
            _sourceText = GameUtils.getCardLink(source);
        }
    }

    public RestoreCardsInPlayEffect(PhysicalCard source, Filterable... filter) {
        super(filter);
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
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getGameState().isHindered(physicalCard);
            }
        };
    }

    @Override
    public Type getType() {
        return Type.BEFORE_RESTORE;
    }

    @Override
    public String getText(LotroGame game) {
        Collection<PhysicalCard> cards = getAffectedCardsMinusPrevented(game);
        return "Restore " + getAppendedTextNames(cards);
    }

    @Override
    protected void playoutEffectOn(LotroGame game, Collection<PhysicalCard> cards) {
        if (!cards.isEmpty()) {
            game.getGameState().sendMessage(_sourceText + " restores " + getAppendedNames(cards));
        }

        game.getGameState().restore(cards);

        for (var card : cards) {
            game.getActionsEnvironment().emitEffectResult(new RestoredResult(_source, card));
        }
    }
}
