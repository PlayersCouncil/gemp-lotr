package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.SpecialFlagModifier;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import com.gempukku.lotro.logic.timing.results.ReplaceSiteResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PlaySiteEffect extends AbstractEffect {
    private final Action _action;
    private final String _playerId;
    private final SitesBlock _siteBlock;
    private final int _siteNumber;
    private final Filterable[] _extraSiteFilters;

    public PlaySiteEffect(Action action, String playerId, SitesBlock siteBlock, int siteNumber) {
        this(action, playerId, siteBlock, siteNumber, Filters.any);
    }

    public PlaySiteEffect(Action action, String playerId, SitesBlock siteBlock, int siteNumber, Filterable... extraSiteFilters) {
        _action = action;
        _playerId = playerId;
        _siteBlock = siteBlock;
        _siteNumber = siteNumber;
        _extraSiteFilters = extraSiteFilters;
    }

    protected int getSiteNumberToPlay(LotroGame game) {
        return _siteNumber;
    }

    private Collection<PhysicalCard> getMatchingSites(LotroGame game) {
        return findSitesToPlay(game, _playerId, _siteBlock, getSiteNumberToPlay(game), _extraSiteFilters);
    }

    /**
     * The sites of a player's adventure deck that a laying of the given site number chooses among: on a numbered
     * path, those printed with that number; where the player's sites are unordered (GameState.usesUnorderedSites),
     * any.  Each is also of the given block (null for any) and accepted by the filters.  Empty for a number off the
     * path.  This is the one reading of what a site-laying card can lay; the definition word
     * sameNumberInYourAdventureDeck (Filters.sameNumberInAdventureDeck) asks it too, so that an action is not
     * offered where its laying would find nothing (Q-0327).
     */
    public static Collection<PhysicalCard> findSitesToPlay(LotroGame game, String playerId, SitesBlock siteBlock, int siteNumber, Filterable... extraSiteFilters) {
        if (siteNumber > 9 || siteNumber < 1)
            return Collections.emptySet();

        if (!game.getGameState().usesUnorderedSites(playerId)) {
            Filter printedSiteNumber = new Filter() {
                @Override
                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                    return physicalCard.getBlueprint().getSiteNumber() == siteNumber;
                }
            };
            if (siteBlock != null)
                return Filters.filter(game, game.getGameState().getAdventureDeck(playerId), Filters.and(extraSiteFilters, printedSiteNumber, Filters.siteBlock(siteBlock)));
            else
                return Filters.filter(game, game.getGameState().getAdventureDeck(playerId), Filters.and(extraSiteFilters, printedSiteNumber));
        } else {
            if (siteBlock != null)
                return Filters.filter(game, game.getGameState().getAdventureDeck(playerId), Filters.and(extraSiteFilters, Filters.siteBlock(siteBlock)));
            else
                return Filters.filter(game, game.getGameState().getAdventureDeck(playerId), extraSiteFilters);
        }
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getMatchingSites(game).size() > 0;
    }

    @Override
    public String getText(LotroGame game) {
        return "Play site";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        final int siteNumber = getSiteNumberToPlay(game);

        Collection<PhysicalCard> newSite = getMatchingSites(game);

        PhysicalCard currentSite = game.getGameState().getSite(siteNumber);

        if (!newSite.isEmpty() && (currentSite == null || game.getModifiersQuerying().canReplaceSite(game, _playerId, currentSite))
                && game.getModifiersQuerying().canPlaySite(game, _playerId)) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(
                    new ChooseArbitraryCardsEffect(_playerId, "Choose site to play", newSite, 1, 1) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                            PhysicalCard newSite = selectedCards.iterator().next();

                            GameState gameState = game.getGameState();
                            PhysicalCard oldSite = gameState.getSite(siteNumber);

                            Zone zone = null;
                            String controlled = null;
                            List<PhysicalCard> stacked = null;
                            List<PhysicalCard> playedOn = Collections.emptyList();

                            if (oldSite != null) {
                                controlled = oldSite.getCardController();
                                if (controlled != null) {
                                    zone = oldSite.getZone();
                                    //The old site goes back to its owner's adventure deck as nobody's (CR 4.2 "control")
                                    gameState.loseControlOfCard(oldSite, Zone.ADVENTURE_PATH);
                                }
                                stacked = new LinkedList<>(gameState.getStackedCards(oldSite));
                                playedOn = new LinkedList<>(gameState.getAttachedCards(oldSite));

                                gameState.removeCardsFromZone(_playerId, Collections.singleton(oldSite));
                                oldSite.setSiteNumber(null);
                                gameState.addCardToZone(game, oldSite, Zone.ADVENTURE_DECK);
                                //"A Shadow player has replaced the fellowship's current site this turn": the mark is
                                //that site's, laid wherever the fellowship stood when the site was replaced (a site
                                //ahead of it that it comes to later in the turn was replaced this turn all the same:
                                //Q-0259).  It is in force only while the fellowship is at that site, and a site the
                                //fellowship has moved on from marks nothing (Q-0234)
                                if (!_playerId.equals(gameState.getCurrentPlayerId()))
                                    game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                                            new SpecialFlagModifier(null,
                                                    gameNow -> gameNow.getGameState().getCurrentSiteNumber() == siteNumber,
                                                    ModifierFlag.SHADOW_PLAYER_REPLACED_CURRENT_SITE));

                                game.getActionsEnvironment().emitEffectResult(new ReplaceSiteResult(_playerId, siteNumber));
                            }

                            gameState.removeCardsFromZone(_playerId, Collections.singleton(newSite));
                            newSite.setSiteNumber(siteNumber);
                            gameState.addCardToZone(game, newSite, Zone.ADVENTURE_PATH);
                            gameState.sendMessage(newSite.getOwner() + " plays " + GameUtils.getCardLink(newSite));

                            if (controlled != null) {
                                gameState.takeControlOfCard(controlled, game, newSite, zone);
                                //Taken off the old site first: a card stacked again while still stacked is listed twice
                                gameState.removeCardsFromZone(_playerId, stacked);
                                for (PhysicalCard physicalCard : stacked)
                                    gameState.stackCard(game, physicalCard, newSite);
                            }

                            //"When a site is replaced, all cards played on or stacked on the old site are moved to
                            //the new site" (CR 4.2 "site"), on a controlled site and on the adventure path alike.
                            //A card played on the old site never leaves play: it is handed to the new site as a
                            //card that changes bearer is, keeping its card id and its tokens, and a text that is
                            //in force (not that of a card inactive this turn, or hindered) is stopped and started
                            //once so that it answers for the new site (F-0467)
                            for (PhysicalCard physicalCard : playedOn) {
                                gameState.transferCard(physicalCard, newSite);
                                if (gameState.isCardInPlayActive(physicalCard))
                                    gameState.reapplyAffectingForCard(game, physicalCard);
                            }

                            sitePlayedCallback(newSite);
                            game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playerId, Zone.ADVENTURE_DECK, newSite, null, null, false));
                        }
                    });

            game.getActionsEnvironment().addActionToStack(subAction);
            return new FullEffectResult(true);
        } else if (newSite.size() > 0) {
            game.getGameState().sendMessage("Can't play a site");
            return new FullEffectResult(false);
        } else {
            game.getGameState().sendMessage("Can't find a matching site to play in " + _playerId + " adventure deck");
            return new FullEffectResult(false);
        }
    }

    protected void sitePlayedCallback(PhysicalCard site) {

    }
}
