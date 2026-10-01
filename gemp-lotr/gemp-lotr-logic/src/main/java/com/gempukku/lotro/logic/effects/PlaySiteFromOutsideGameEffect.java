package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lets a player play a site from outside the game (i.e. from the format's whole legal card pool rather than from an
 * adventure deck) into a site slot on the adventure path, optionally adding twilight for doing so.
 *
 * Used for Race to Mount Doom meta-site 94_31, which extends the path past its last site: the Shadow player picks any
 * site with the given printed site number from the Fellowship, Towers, King or Hobbit blocks (i.e. every block that
 * assigns site numbers - Shadows-block sites have none) and it is played as the next site.  The pool is every such
 * site in the card library, regardless of the current game's format legality, minus any site card already existing
 * somewhere in this game (in either player's adventure deck, already played, discarded, etc).  The cards offered are
 * display-only instances (never registered in the game state) built the same way as the booster-pack opener does;
 * only the card actually chosen is created for real.
 */
public class PlaySiteFromOutsideGameEffect extends AbstractEffect {
    /** Blocks that assign printed site numbers; the Shadows block has none and is never eligible. */
    private static final Set<SitesBlock> NUMBERED_SITE_BLOCKS =
            EnumSet.of(SitesBlock.FELLOWSHIP, SitesBlock.TWO_TOWERS, SitesBlock.KING, SitesBlock.HOBBIT);

    private final Action _action;
    private final String _playerId;
    private final int _siteNumber;
    private final int _printedSiteNumber;
    private final int _extraTwilight;
    private boolean _carriedOut;

    public PlaySiteFromOutsideGameEffect(Action action, String playerId, int siteNumber, int printedSiteNumber, int extraTwilight) {
        _action = action;
        _playerId = playerId;
        _siteNumber = siteNumber;
        _printedSiteNumber = printedSiteNumber;
        _extraTwilight = extraTwilight;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return !getEligibleBlueprintIds(game).isEmpty();
    }

    @Override
    public String getText(LotroGame game) {
        return "Play a site from outside the game";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean wasCarriedOut() {
        return _carriedOut;
    }

    private List<String> getEligibleBlueprintIds(LotroGame game) {
        final Set<String> blueprintIdsAlreadyInGame = new HashSet<>();
        for (PhysicalCard card : game.getGameState().getAllCards()) {
            if (card.getBlueprint().getCardType() == CardType.SITE)
                blueprintIdsAlreadyInGame.add(card.getBlueprintId());
        }

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, LotroCardBlueprint> entry : game.getLotroCardBlueprintLibrary().getBaseCards().entrySet()) {
            LotroCardBlueprint blueprint = entry.getValue();
            if (blueprint.getCardType() != CardType.SITE || blueprint.getSiteNumber() != _printedSiteNumber)
                continue;
            if (!NUMBERED_SITE_BLOCKS.contains(blueprint.getSiteBlock()))
                continue;
            if (blueprintIdsAlreadyInGame.contains(entry.getKey()))
                continue;
            result.add(entry.getKey());
        }
        result.sort(null);
        return result;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        final List<String> blueprintIds = getEligibleBlueprintIds(game);
        if (blueprintIds.isEmpty()) {
            game.getGameState().sendMessage("No site outside the game could be played as site " + _siteNumber);
            return new FullEffectResult(false);
        }

        final List<PhysicalCard> displayCards = new ArrayList<>();
        int tempId = -1;
        for (String blueprintId : blueprintIds) {
            try {
                LotroCardBlueprint blueprint = game.getLotroCardBlueprintLibrary().getLotroCardBlueprint(blueprintId);
                displayCards.add(new PhysicalCardImpl(tempId--, blueprintId, _playerId, blueprint));
            } catch (CardNotFoundException ignored) {
                // Skip unknown cards
            }
        }

        game.getUserFeedback().sendAwaitingDecision(_playerId,
                new ArbitraryCardsSelectionDecision(1, "Choose a site from outside the game to play as site " + _siteNumber,
                        displayCards, displayCards, 1, 1) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        List<PhysicalCard> selected = getSelectedCardsByResponse(result);
                        sitePicked(game, selected.get(0).getBlueprintId());
                    }
                });

        _carriedOut = true;
        return new FullEffectResult(true);
    }

    private void sitePicked(LotroGame game, String blueprintId) {
        GameState gameState = game.getGameState();
        PhysicalCard site;
        try {
            site = gameState.createPhysicalCard(_playerId, game.getLotroCardBlueprintLibrary(), blueprintId);
        } catch (CardNotFoundException exp) {
            gameState.sendMessage("Failed to create site: " + blueprintId);
            return;
        }

        site.setSiteNumber(_siteNumber);
        gameState.addCardToZone(game, site, Zone.ADVENTURE_PATH);
        gameState.sendMessage(_playerId + " plays " + GameUtils.getCardLink(site) + " from outside the game as site " + _siteNumber);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playerId, Zone.ADVENTURE_DECK, site, null, null, false));

        if (_extraTwilight > 0) {
            SubAction subAction = new SubAction(_action);
            AddTwilightEffect twilight = new AddTwilightEffect(null, AddTwilightEffect.Cause.MOVE, _extraTwilight);
            twilight.setSourceText("Moving past the last site");
            subAction.appendEffect(twilight);
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
