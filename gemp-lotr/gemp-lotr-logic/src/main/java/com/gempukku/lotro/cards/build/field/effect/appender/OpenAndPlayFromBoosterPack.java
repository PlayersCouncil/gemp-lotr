package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.packs.PackOpener;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HJSON effect appender: opens a booster pack, reveals the contents, and lets a player
 * play one matching card ignoring all costs.
 *
 * Usage:
 *   { type: OpenAndPlayFromBoosterPack, player: shadow, filter: side(shadow) }
 *
 * Parameters:
 *   player - who opens the pack and chooses (default: "you")
 *   filter - restricts which revealed cards are selectable for play (default: "any")
 */
public class OpenAndPlayFromBoosterPack implements EffectAppenderProducer {

    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "filter");
        final String playerStr = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(playerStr);

        final String filterStr = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filterStr, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                String player = playerSource.getPlayer(actionContext);
                Filterable filter = filterSource.getFilterable(actionContext);
                return new ChoosePackEffect(action, actionContext, player, filter);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                PackOpener packOpener = actionContext.getGame().getPackOpener();
                return packOpener != null && !packOpener.getAvailablePackIds().isEmpty();
            }
        };
    }

    /**
     * Step 1: Player chooses which booster pack to open.
     */
    private static class ChoosePackEffect extends AbstractEffect {
        private final CostToEffectAction _action;
        private final ActionContext _actionContext;
        private final String _player;
        private final Filterable _filter;
        private boolean _carriedOut;

        ChoosePackEffect(CostToEffectAction action, ActionContext actionContext, String player, Filterable filter) {
            _action = action;
            _actionContext = actionContext;
            _player = player;
            _filter = filter;
        }

        @Override
        public boolean isPlayableInFull(LotroGame game) {
            return game.getPackOpener() != null;
        }

        @Override
        public String getText(LotroGame game) {
            return "Open a booster pack";
        }

        @Override
        public Type getType() {
            return null;
        }

        @Override
        protected FullEffectResult playEffectReturningResult(LotroGame game) {
            PackOpener packOpener = game.getPackOpener();
            if (packOpener == null) {
                return new FullEffectResult(false);
            }

            List<String> packIds = packOpener.getAvailablePackIds();
            // Use "tempN" as cardIds (client expects this format for jQuery selectors)
            // and pack names as blueprintIds (client renders product images from these)
            List<String> tempCardIds = new ArrayList<>();
            List<String> allSelectable = new ArrayList<>();
            for (int i = 0; i < packIds.size(); i++) {
                tempCardIds.add("temp" + i);
                allSelectable.add("true");
            }

            game.getUserFeedback().sendAwaitingDecision(_player,
                    new ArbitraryCardsSelectionDecision(1, "Choose a booster pack to open",
                            tempCardIds, packIds, allSelectable, 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            // Response is "tempN" where N is the index into the pack list
                            int index;
                            try {
                                String trimmed = result.trim();
                                if (trimmed.startsWith("temp")) {
                                    trimmed = trimmed.substring(4);
                                }
                                index = Integer.parseInt(trimmed);
                            } catch (NumberFormatException e) {
                                throw new DecisionResultInvalidException();
                            }
                            if (index < 0 || index >= packIds.size()) {
                                throw new DecisionResultInvalidException();
                            }

                            String chosenPackId = packIds.get(index);
                            game.getGameState().sendMessage(_player + " selects " + GameUtils.getProductLink(chosenPackId));

                            // Open the chosen pack
                            List<String> cardBlueprintIds = packOpener.openPack(chosenPackId);

                            // Queue step 2: reveal and optionally play
                            SubAction revealAction = new SubAction(_action);
                            revealAction.appendEffect(new RevealAndPlayEffect(
                                    _action, _actionContext, _filter, cardBlueprintIds, _player));
                            game.getActionsEnvironment().addActionToStack(revealAction);
                        }
                    });

            _carriedOut = true;
            return new FullEffectResult(true);
        }

        @Override
        public boolean wasCarriedOut() {
            return _carriedOut;
        }
    }

    /**
     * Step 2: Reveal pack contents and let player optionally play one card of the playable side,
     * ignoring all costs.
     */
    private static class RevealAndPlayEffect extends AbstractEffect {
        private final CostToEffectAction _action;
        private final ActionContext _actionContext;
        private final Filterable _filter;
        private final List<String> _cardBlueprintIds;
        private final String _player;
        private boolean _carriedOut;

        RevealAndPlayEffect(CostToEffectAction action, ActionContext actionContext, Filterable filter,
                            List<String> cardBlueprintIds, String player) {
            _action = action;
            _actionContext = actionContext;
            _filter = filter;
            _cardBlueprintIds = cardBlueprintIds;
            _player = player;
        }

        @Override
        public boolean isPlayableInFull(LotroGame game) {
            return true;
        }

        @Override
        public String getText(LotroGame game) {
            return "Reveal booster pack contents";
        }

        @Override
        public Type getType() {
            return null;
        }

        @Override
        protected FullEffectResult playEffectReturningResult(LotroGame game) {
            // Create lightweight display-only PhysicalCardImpl instances.
            // These are NOT registered in GameState._allCards — they exist only for the
            // ArbitraryCardsSelectionDecision UI and are garbage collected afterward.
            // Only the card the player actually chooses to play gets properly created
            // via GameState.createPhysicalCard().
            List<PhysicalCard> displayCards = new ArrayList<>();
            int tempId = -1;
            for (String bpId : _cardBlueprintIds) {
                try {
                    LotroCardBlueprint bp = game.getLotroCardBlueprintLibrary().getLotroCardBlueprint(bpId);
                    PhysicalCardImpl displayCard = new PhysicalCardImpl(tempId--, bpId, _player, bp);
                    displayCards.add(displayCard);
                } catch (CardNotFoundException e) {
                    // Skip unknown cards
                }
            }

            if (displayCards.isEmpty()) {
                game.getGameState().sendMessage("No cards found in booster pack");
                _carriedOut = true;
                return new FullEffectResult(false);
            }

            game.getGameState().sendMessage("Booster pack contained: " + GameUtils.getAppendedNames(displayCards));

            // Determine which cards are selectable (matches filter + passes uniqueness)
            List<PhysicalCard> selectableCards = new ArrayList<>();
            for (PhysicalCard card : displayCards) {
                if (Filters.and(_filter).accepts(game, card)
                        && PlayUtils.checkPlayRequirementsIgnoringCosts(game, card)) {
                    selectableCards.add(card);
                }
            }

            // Show all cards to all players; performing player can select 0 or 1
            game.getUserFeedback().sendAwaitingDecision(_player,
                    new ArbitraryCardsSelectionDecision(1, "Booster pack contents \u2014 choose a card to play (or none)",
                            displayCards, selectableCards, 0, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);

                            if (selectedCards.size() == 1) {
                                PhysicalCard displayCard = selectedCards.get(0);
                                String chosenBpId = displayCard.getBlueprintId();

                                // Now create the REAL physical card via GameState (proper ID, registered in _allCards)
                                try {
                                    PhysicalCard realCard = game.getGameState().createPhysicalCard(
                                            _player, game.getLotroCardBlueprintLibrary(), chosenBpId);

                                    game.getGameState().sendMessage(_player + " plays " +
                                            GameUtils.getCardLink(realCard) +
                                            " from booster pack ignoring all costs");

                                    // Put in VOID_FROM_HAND so PlayPermanentAction can find it in a zone
                                    game.getGameState().addCardToZone(game, realCard,
                                            Zone.VOID_FROM_HAND);

                                    CostToEffectAction playAction = PlayUtils.getPlayCardActionIgnoringCosts(game, realCard);
                                    game.getActionsEnvironment().addActionToStack(playAction);
                                } catch (CardNotFoundException e) {
                                    game.getGameState().sendMessage("Failed to create card: " + chosenBpId);
                                }
                            } else {
                                game.getGameState().sendMessage(_player + " chooses not to play a card from the booster pack");
                            }
                            // Display-only cards are not in any zone or _allCards — nothing to clean up
                        }
                    });

            // Also show to opponent (non-interactive reveal)
            String opponent = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_player, false).getNextPlayer();
            if (opponent != null && !opponent.equals(_player)) {
                game.getUserFeedback().sendAwaitingDecision(opponent,
                        new ArbitraryCardsSelectionDecision(1, _player + " reveals booster pack contents",
                                displayCards, Collections.emptyList(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                                // No action needed — just dismiss
                            }
                        });
            }

            _carriedOut = true;
            return new FullEffectResult(true);
        }

        @Override
        public boolean wasCarriedOut() {
            return _carriedOut;
        }
    }
}
