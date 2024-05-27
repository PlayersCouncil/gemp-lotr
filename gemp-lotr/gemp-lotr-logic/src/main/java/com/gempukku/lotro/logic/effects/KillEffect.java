package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractSuccessfulEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.logic.timing.results.ForEachKilledResult;
import com.gempukku.lotro.logic.timing.results.KilledResult;

import java.util.*;

public class KillEffect extends AbstractSuccessfulEffect {
    private final Collection<? extends PhysicalCard> _cards;

    private final Collection<? extends PhysicalCard> _killers;
    private final Cause _cause;

    public enum Cause {
        WOUNDS, OVERWHELM, CARD_EFFECT
    }

    public KillEffect(PhysicalCard card, PhysicalCard killer, Cause cause) {
        this(Collections.singleton(card), Collections.singleton(killer), cause);
    }

    public KillEffect(Collection<? extends PhysicalCard> cards, PhysicalCard killer, Cause cause) {
        this(cards, Collections.singleton(killer), cause);
    }

    public KillEffect(Collection<? extends PhysicalCard> cards, Collection<? extends PhysicalCard> killers, Cause cause) {
        _cards = cards;
        _killers = killers;
        _cause = cause;
    }

    public Cause getCause() {
        return _cause;
    }

    @Override
    public Effect.Type getType() {
        return Effect.Type.BEFORE_KILLED;
    }

    public List<PhysicalCard> getCharactersToBeKilled() {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : _cards) {
            if (card.getZone() != null && card.getZone().isInPlay())
                result.add(card);
        }

        return result;
    }

    public Collection<? extends PhysicalCard> getKillers() {
        return _killers;
    }

    @Override
    public String getText(LotroGame game) {
        List<PhysicalCard> cards = getCharactersToBeKilled();
        return "Kill - " + getAppendedTextNames(cards);
    }

    @Override
    public void playEffect(LotroGame game) {
        List<PhysicalCard> toBeKilled = getCharactersToBeKilled();

        GameState gameState = game.getGameState();

        for (PhysicalCard card : toBeKilled)
            gameState.sendMessage(GameUtils.getCardLink(card) + " gets killed");

        // For result
        Set<PhysicalCard> discardedCards = new HashSet<>();
        Set<PhysicalCard> killedCards = new HashSet<>();

        // Prepare the moves
        Set<PhysicalCard> toRemoveFromZone = new HashSet<>();
        Set<PhysicalCard> toAddToDeadPile = new HashSet<>();
        Set<PhysicalCard> toAddToDiscard = new HashSet<>();

        for (PhysicalCard card : toBeKilled) {
            toRemoveFromZone.add(card);

            if (card.getBlueprint().getSide() == Side.FREE_PEOPLE) {
                killedCards.add(card);
                toAddToDeadPile.add(card);
            } else {
                killedCards.add(card);
                discardedCards.add(card);
                toAddToDiscard.add(card);
            }
        }

        DiscardUtils.cardsToChangeZones(game, toBeKilled, discardedCards, toAddToDiscard);
        toRemoveFromZone.addAll(toAddToDiscard);

        gameState.removeCardsFromZone(null, toRemoveFromZone);

        for (PhysicalCard deadCard : toAddToDeadPile)
            gameState.addCardToZone(game, deadCard, Zone.DEAD);

        for (PhysicalCard discardedCard : toAddToDiscard)
            gameState.addCardToZone(game, discardedCard, Zone.DISCARD);

        if (killedCards.size() > 0)
            game.getActionsEnvironment().emitEffectResult(new KilledResult(killedCards, new HashSet<>(_killers), _cause));
        for (PhysicalCard killedCard : killedCards)
            game.getActionsEnvironment().emitEffectResult(new ForEachKilledResult(killedCard, _killers, _cause));
        for (PhysicalCard discardedCard : discardedCards)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));

    }
}
