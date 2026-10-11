package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.InactiveReason;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.common.Zone;
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

    /**
     * The cards this effect is aimed at that may be returned to hand.  The effect asks for itself what bars a card
     * from that (ModifiersQuerying.canBeReturnedToHand: the Ring-bearer by the rules, CR 4.2 "Ring-bearer", hindered
     * or not), as the discard and the hinder effects ask what bars theirs.  The definition word ReturnToHand asks
     * the same of the cards it chooses from, so no card's text is carried out differently for it; only an effect
     * handed its cards directly is, which no card does (mechanics/HinderedRingBearerTests staples one).
     */
    private Collection<PhysicalCard> getCardsThatMayBeReturned(LotroGame game) {
        return Filters.filterActive(game, _spotOverrides, _filter,
                _source == null ? Filters.any : Filters.canBeReturnedToHand(_source));
    }

    @Override
    public String getText(LotroGame game) {
        Collection<PhysicalCard> cards = getCardsThatMayBeReturned(game);
        return "Return " + getAppendedNames(cards) + " to hand";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getCardsThatMayBeReturned(game).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        GameState gameState = game.getGameState();
        Collection<PhysicalCard> cardsToReturnToHand = getCardsThatMayBeReturned(game);

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
