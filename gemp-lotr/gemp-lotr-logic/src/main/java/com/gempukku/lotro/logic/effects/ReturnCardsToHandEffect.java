package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.InactiveReason;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.logic.timing.results.ReturnCardsToHandResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReturnCardsToHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final Filterable _filter;
    // Which inactive cards the effect reaches beside the active ones: hindered cards, where the text names its
    // cards by nothing but "card" or a side (Ketura on Q-0098).
    private final Map<InactiveReason, Boolean> _spotOverrides;

    public ReturnCardsToHandEffect(PhysicalCard source, Filterable filter) {
        this(source, SpotOverride.NONE, filter);
    }

    public ReturnCardsToHandEffect(PhysicalCard source, Map<InactiveReason, Boolean> spotOverrides, Filterable filter) {
        _source = source;
        _filter = filter;
        _spotOverrides = spotOverrides;
    }

    @Override
    public String getText(LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filterActive(game, _spotOverrides, _filter);
        return "Return " + getAppendedNames(cards) + " to hand";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.filterActive(game, _spotOverrides, _filter,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return (_source == null || game.getModifiersQuerying().canBeReturnedToHand(game, physicalCard, _source));
                    }
                }).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        GameState gameState = game.getGameState();
        Collection<PhysicalCard> cardsToReturnToHand = Filters.filterActive(game, _spotOverrides, _filter);

        // Preparation, figure out, what's going where...
        Set<PhysicalCard> discardedFromPlay = new HashSet<>();
        Set<PhysicalCard> toGoToDiscardCards = new HashSet<>();

        DiscardUtils.cardsToChangeZones(game, cardsToReturnToHand, discardedFromPlay, toGoToDiscardCards);

        Set<PhysicalCard> cardsToRemoveFromZones = new HashSet<>(toGoToDiscardCards);
        cardsToRemoveFromZones.addAll(cardsToReturnToHand);

        // Remove from their zone
        gameState.removeCardsFromZone(_source.getOwner(), cardsToRemoveFromZones);

        // Add cards to hand
        for (PhysicalCard card : cardsToReturnToHand)
            gameState.addCardToZone(game, card, Zone.HAND);

        // Add discarded to discard
        for (PhysicalCard card : toGoToDiscardCards)
            gameState.addCardToZone(game, card, Zone.DISCARD);

        if (_source != null && cardsToReturnToHand.size() > 0)
            gameState.sendMessage(GameUtils.getCardLink(_source) + " returns " + getAppendedNames(cardsToReturnToHand) + " to hand");

        for (PhysicalCard discardedCard : discardedFromPlay)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));
        for (PhysicalCard cardReturned : cardsToReturnToHand)
            game.getActionsEnvironment().emitEffectResult(new ReturnCardsToHandResult(cardReturned));

        return new FullEffectResult(true);
    }
}
