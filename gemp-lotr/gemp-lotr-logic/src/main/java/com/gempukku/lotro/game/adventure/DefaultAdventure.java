package com.gempukku.lotro.game.adventure;

import com.gempukku.lotro.game.Adventure;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.PlaySiteEffect;
import com.gempukku.lotro.logic.effects.PlaySiteFromOutsideGameEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.PregameSetupFeedback;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.pregame.BiddingGameProcess;
import com.gempukku.lotro.logic.timing.processes.pregame.MappingPreBidGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ShadowPhasesGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.archery.FellowshipPlayerChoosesShadowPlayerToAssignDamageToGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.assign.ShadowPlayersAssignTheirMinionsGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.regroup.DiscardAllMinionsGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.regroup.PlayerReconcilesGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.regroup.ReturnFollowersToSupportGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.regroup.ShadowPlayersReconcileGameProcess;
import com.gempukku.lotro.logic.timing.rules.WinConditionRule;

import java.util.Set;

public class DefaultAdventure implements Adventure {
    @Override
    public void applyAdventureRules(LotroGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        new WinConditionRule(actionsEnvironment).applyRule();
    }

    @Override
    public void appendNextSiteAction(final SystemQueueAction action) {
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        GameState gameState = game.getGameState();

                        final int nextSiteNumber = gameState.getCurrentSiteNumber() + 1;
                        PhysicalCard nextSite = gameState.getSite(nextSiteNumber);

                        if (nextSite == null) {
                            //Race to Mount Doom 94_31 extends the path past its last site; that extra site is not in
                            // anybody's adventure deck, so the Shadow player plays one from outside the game instead.
                            if (nextSiteNumber > 9
                                    && nextSiteNumber == RuleUtils.getFellowshipWinSiteNumber(game)) {
                                PlayOrder shadowOrder = gameState.getPlayerOrder().getCounterClockwisePlayOrder(gameState.getCurrentPlayerId(), false);
                                shadowOrder.getNextPlayer();
                                String shadowPlayer = shadowOrder.getNextPlayer();
                                action.insertEffect(
                                        new PlaySiteFromOutsideGameEffect(action, shadowPlayer, nextSiteNumber, 9, 4));
                                return;
                            }

                            LotroCardBlueprint.Direction nextSiteDirection = gameState.getCurrentSite().getBlueprint().getSiteDirection();
                            String playerToPlaySite;
                            if (nextSiteDirection == LotroCardBlueprint.Direction.LEFT) {
                                PlayOrder order = gameState.getPlayerOrder().getClockwisePlayOrder(gameState.getCurrentPlayerId(), false);
                                order.getNextPlayer();
                                playerToPlaySite = order.getNextPlayer();
                            } else {
                                PlayOrder order = gameState.getPlayerOrder().getCounterClockwisePlayOrder(gameState.getCurrentPlayerId(), false);
                                order.getNextPlayer();
                                playerToPlaySite = order.getNextPlayer();
                            }

                            action.insertEffect(
                                    new PlaySiteEffect(action, playerToPlaySite, null, nextSiteNumber));
                        }
                    }
                });
    }

    @Override
    public GameProcess getAfterFellowshipArcheryGameProcess(int fellowshipArcheryTotal, GameProcess followingProcess) {
        return new FellowshipPlayerChoosesShadowPlayerToAssignDamageToGameProcess(fellowshipArcheryTotal, followingProcess);
    }

    @Override
    public GameProcess getAfterFellowshipAssignmentGameProcess(Set<PhysicalCard> leftoverMinions, GameProcess followingProcess) {
        return new ShadowPlayersAssignTheirMinionsGameProcess(followingProcess, leftoverMinions);
    }

    @Override
    public GameProcess getBeforeFellowshipChooseToMoveGameProcess(GameProcess followingProcess) {
        return new ShadowPlayersReconcileGameProcess(followingProcess);
    }

    @Override
    public GameProcess getPlayerStaysGameProcess(LotroGame game, GameProcess followingProcess) {
        return new PlayerReconcilesGameProcess(game.getGameState().getCurrentPlayerId(),
                new ReturnFollowersToSupportGameProcess(
                        new DiscardAllMinionsGameProcess(followingProcess)));
    }

    @Override
    public GameProcess getAfterFellowshipPhaseGameProcess() {
        return new ShadowPhasesGameProcess();
    }

    @Override
    public GameProcess getStartingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback, PregameSetupFeedback pregameSetupFeedback) {
        return new MappingPreBidGameProcess(players, playerOrderFeedback, pregameSetupFeedback);
    }

    @Override
    public boolean isSolo() {
        return false;
    }
}
