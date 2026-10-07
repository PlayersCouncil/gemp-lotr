package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.results.EnteredPlayResult;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;

import java.util.Collections;

/**
 * Puts a card that is being played into play, in two steps (CR 4.2 "playing a card").
 *
 * Carrying this effect out is the entering step: the card is placed and "is now in play", and an EnteredPlayResult
 * is published.  That gives the texts that watch the table all the time their turn first: a card the Watcher in
 * the Water discards, or one that is discarded for not being at a marsh, goes now.
 *
 * The effect that getPublishEffect() returns is the next step, which the play action takes once everything
 * answering the first has finished: the play is announced with a PlayCardResult, and it is that result which a
 * card's own "When you play ...", other cards' "Each time ... is played" and responses to the play answer.  The
 * loss of initiative that the card's leaving the hand causes is published in the same batch.  A card that is no
 * longer in play by then, whatever took it out, is not announced at all (CR 4.2 "discard": "If a card is
 * discarded when it comes into play, ignore any effects triggered when it comes into play").
 *
 * completePlay() is the last step, "The card is played", taken by the play action when everything answering the
 * play has finished.  A play whose card is not in play then did not happen: it pays no cost, and it is taken
 * back out of the record of what was played this phase and this turn.
 */
public class PlayCardEffect extends AbstractEffect {
    private final String performingPlayerId;
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private PhysicalCard _attachedToCard;
    private final Zone _zone;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    private final boolean _paidToil;

    private PlayCardResult _playResult;

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo, PhysicalCard attachedOrStackedPlayedFrom, boolean paidToil) {
        this.performingPlayerId = performingPlayerId;
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _paidToil = paidToil;
    }

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed, PhysicalCard attachedToCard, PhysicalCard attachedOrStackedPlayedFrom, boolean paidToil) {
        this.performingPlayerId = performingPlayerId;
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _attachedToCard = attachedToCard;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _paidToil = paidToil;
        _zone = Zone.ATTACHED;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedToCard;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        game.getGameState().removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
        if (_attachedToCard != null) {
            game.getGameState().attachCard(game, _cardPlayed, _attachedToCard);
        } else {
            game.getGameState().addCardToZone(game, _cardPlayed, _zone);
        }

        game.getActionsEnvironment().noteCardEnteringPlay(_cardPlayed, _playedFrom);
        game.getActionsEnvironment().emitEffectResult(new EnteredPlayResult(_cardPlayed));

        return new FullEffectResult(true);
    }

    /**
     * The step after the card has entered play: its play is announced, unless the card has left play meanwhile.
     * To be carried out by the play action once everything that answered the card's entering play has finished.
     */
    public Effect getPublishEffect() {
        return new UnrespondableEffect() {
            @Override
            protected void doPlayEffect(LotroGame game) {
                game.getActionsEnvironment().noteCardEnteredPlay(_cardPlayed);

                if (isCardInPlay()) {
                    _playResult = new PlayCardResult(performingPlayerId, _playedFrom, _cardPlayed, _attachedToCard, _attachedOrStackedPlayedFrom, _paidToil);
                    game.getActionsEnvironment().emitEffectResult(_playResult);
                } else {
                    game.getGameState().sendMessage(GameUtils.getCardLink(_cardPlayed) + " left play as it came into play");
                }
            }
        };
    }

    /**
     * The last step of the play, taken once everything that answered it has finished.
     *
     * @return whether the card was played: its play was announced and it is still in play
     */
    public boolean completePlay(LotroGame game) {
        if (_playResult == null)
            return false;

        if (isCardInPlay())
            return true;

        game.getActionsEnvironment().retractEffectResult(_playResult);
        return false;
    }

    private boolean isCardInPlay() {
        return _cardPlayed.getZone() != null && _cardPlayed.getZone().isInPlay();
    }
}
