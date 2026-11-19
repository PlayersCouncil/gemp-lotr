package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.logic.timing.results.SiteLiberatedResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LiberateASiteEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _liberatingPlayer;
    private final String _liberatedSiteController;
    private final PhysicalCard _chosenSite;

    public LiberateASiteEffect(PhysicalCard source, String liberatingPlayer, String liberatedSiteController) {
        _source = source;
        _liberatingPlayer = liberatingPlayer;
        _liberatedSiteController = liberatedSiteController;
        _chosenSite = null;
    }

    /**
     * Liberates the site given, which the liberating player has chosen (the definition word {@code select} of
     * {@code liberateSite}; Ketura's ruling on Q-0370), in place of the rules' highest-first order. Nothing is
     * liberated if the site is no longer controlled, or can no longer be liberated, when the effect is carried out.
     */
    public LiberateASiteEffect(PhysicalCard source, String liberatingPlayer, PhysicalCard chosenSite) {
        _source = source;
        _liberatingPlayer = liberatingPlayer;
        _liberatedSiteController = null;
        _chosenSite = chosenSite;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        if (_chosenSite != null)
            return PlayConditions.canLiberateChosenSite(game, _liberatingPlayer, _source, _chosenSite);
        return PlayConditions.canLiberateASite(game, _source.getOwner(), _source, null);
    }

    @Override
    public String getText(LotroGame game) {
        return "Liberate a site";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    private PhysicalCard getSiteToLiberate(LotroGame game) {
        if (_chosenSite != null)
            return _chosenSite;

        int maxUnoccupiedSite = Integer.MAX_VALUE;
        for (String playerId : game.getGameState().getPlayerOrder().getAllPlayers())
            maxUnoccupiedSite = Math.min(maxUnoccupiedSite, game.getGameState().getPlayerPosition(playerId) - 1);

        for (int i = maxUnoccupiedSite; i >= 1; i--) {
            PhysicalCard site = game.getGameState().getSite(i);
            if (_liberatedSiteController == null) {
                if (site != null && site.getCardController() != null
                        && !site.getCardController().equals(game.getGameState().getCurrentPlayerId()))
                    return site;
            } else {
                if (site != null && site.getCardController() != null && site.getCardController().equals(_liberatedSiteController))
                    return site;
            }
        }

        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            PhysicalCard siteToLiberate = getSiteToLiberate(game);
            if (siteToLiberate != null) {
                Set<PhysicalCard> cardsToRemove = new HashSet<>();
                Set<PhysicalCard> discardedCards = new HashSet<>();

                List<PhysicalCard> stackedCards = game.getGameState().getStackedCards(siteToLiberate);
                cardsToRemove.addAll(stackedCards);

                List<PhysicalCard> attachedCards = game.getGameState().getAttachedCards(siteToLiberate);
                cardsToRemove.addAll(attachedCards);
                discardedCards.addAll(attachedCards);

                game.getGameState().removeCardsFromZone(_source.getOwner(), cardsToRemove);
                for (PhysicalCard removedCard : cardsToRemove)
                    game.getGameState().addCardToZone(game, removedCard, Zone.DISCARD);

                game.getGameState().loseControlOfCard(siteToLiberate, Zone.ADVENTURE_PATH);

                for (PhysicalCard discardedCard : discardedCards)
                    game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));

                game.getGameState().sendMessage(_liberatingPlayer + " liberated a " + GameUtils.getCardLink(siteToLiberate) + " using " + GameUtils.getCardLink(_source));

                game.getActionsEnvironment().emitEffectResult(new SiteLiberatedResult(_liberatingPlayer, siteToLiberate));
                liberatedSiteCallback(siteToLiberate);

                return new FullEffectResult(true);
            }
        }
        return new FullEffectResult(false);
    }

    public void liberatedSiteCallback(PhysicalCard liberatedSite) {
    }
}
