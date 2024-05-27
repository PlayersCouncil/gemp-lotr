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
        game.getFormat().getAdventure().appendNextSiteAction(action);
        action.appendEffect(
                new TriggeringResultEffect(Effect.Type.BEFORE_MOVE_FROM, new WhenMoveFromResult(currentSite), "Fellowship moved from"));
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        GameState gameState = game.getGameState();
                        gameState.movePlayerToNextSite(game);
                    }
                });
        action.appendEffect(
                new TriggeringResultEffect(Effect.Type.BEFORE_MOVE, new WhenMovesResult(), "Fellowship moves"));
        action.appendEffect(
                new TriggeringResultEffect(Effect.Type.BEFORE_MOVE_TO, new WhenMoveToResult(), "Fellowship moved to"));
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        GameState gameState = game.getGameState();

                        int siteTwilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, gameState.getCurrentSite(), null, 0, false);
                        if (!game.getFormat().isOrderedSites()) {
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

                        AddTwilightEffect effect = new AddTwilightEffect(null, siteTwilightCost + companionsAddingTwilightForMoveCount);
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
