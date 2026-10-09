package com.gempukku.lotro.logic.timing.processes.turn.move;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.results.WhenMoveFromResult;
import com.gempukku.lotro.logic.timing.results.WhenMoveToResult;
import com.gempukku.lotro.logic.timing.results.WhenMovesResult;

public class MovementGameProcess implements GameProcess {
    private final GameProcess _afterMovementGameProcess;

    public MovementGameProcess(GameProcess afterMovementGameProcess) {
        _afterMovementGameProcess = afterMovementGameProcess;
    }

    @Override
    public void process(LotroGame game) {
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        final SystemQueueAction action = new SystemQueueAction();
        // The steps of a move, in the order the rules list them (Comprehensive Rules 4.2, "moving your
        // fellowship"): the next site is placed if needed; "When you move from..."; "When the fellowship
        // moves..."; the player marker is moved; "When you move to..."; the twilight of the move.  The first
        // two kinds of text are answered with the fellowship still at the site it is leaving.
        game.getFormat().getAdventure().appendNextSiteAction(action);
        action.appendEffect(
                new TriggeringResultEffect(Effect.Type.BEFORE_MOVE_FROM, new WhenMoveFromResult(currentSite), "Fellowship moved from"));
        action.appendEffect(
                new TriggeringResultEffect(Effect.Type.BEFORE_MOVE, new WhenMovesResult(), "Fellowship moves"));
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        GameState gameState = game.getGameState();
                        gameState.movePlayerToNextSite(game);
                        // The marker is on the new site, but the fellowship has moved there only when the
                        // twilight of the move has been added: until then, standing checks on where the
                        // fellowship is wait (GameState.isFellowshipArriving).
                        gameState.setFellowshipArriving(true);
                    }
                });
        action.appendEffect(
                new TriggeringResultEffect(Effect.Type.BEFORE_MOVE_TO, new WhenMoveToResult(), "Fellowship moved to"));
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        GameState gameState = game.getGameState();

                        int siteTwilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, gameState.getCurrentSite(), null, 0, false);

                        //We check both the format and the actual sites being used, because in the case of Race to Mount Doom,
                        // we can have modifiers that permit the use of Shadows sites in otherwise movie-era formats.
                        if (!game.getFormat().isOrderedSites()
                                || gameState.getCurrentSite().getBlueprint().getSiteNumber() == 0) {
                            final int siteNumber = gameState.getCurrentSiteNumber();
                            if (siteNumber > 3 && siteNumber <= 6)
                                siteTwilightCost += 3;
                            else if (siteNumber > 6 && siteNumber <= 9)
                                siteTwilightCost += 6;
                        }
                        int companionsAddingTwilightForMoveCount = Filters.countActive(game, CardType.COMPANION,
                                new Filter() {
                                    @Override
                                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                        return game.getModifiersQuerying().addsTwilightForCompanionMove(game, physicalCard);
                                    }
                                });

                        AddTwilightEffect effect = new AddTwilightEffect(
                                null,
                                AddTwilightEffect.Cause.MOVE,
                                siteTwilightCost + companionsAddingTwilightForMoveCount) {
                            @Override
                            protected FullEffectResult playEffectReturningResult(LotroGame game) {
                                FullEffectResult result = super.playEffectReturningResult(game);
                                // The fellowship has now moved to the new site.  A card that is discarded for
                                // being there goes in the batch of this effect's result, after its text has
                                // had its say in the twilight just added (Decipher on Bill the Pony: "the
                                // Shadow number of that site is reduced before Bill the Pony is discarded").
                                game.getGameState().setFellowshipArriving(false);
                                return result;
                            }
                        };
                        effect.setSourceText("Moving");
                        action.insertEffect(effect);
                    }
                });

        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _afterMovementGameProcess;
    }
}
