package com.gempukku.lotro.logic.timing.processes.turn.skirmish;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.actions.SkirmishPhaseAction;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayoutSkirmishesGameProcess implements GameProcess {
    private GameProcess _nextProcess;

    @Override
    public void process(LotroGame game) {
        if (game.getModifiersQuerying().shouldSkipPhase(game, Phase.SKIRMISH, null)) {
            _nextProcess = new AfterSkirmishesGameProcess();
        } else {
            final GameState gameState = game.getGameState();
            final List<Assignment> assignments = gameState.getAssignments();

            if (assignments.size() > 0) {
                Set<PhysicalCard> nonLurkerSkirmishFps = new HashSet<>();
                Set<PhysicalCard> lurkerSkirmishFps = new HashSet<>();
                for (Assignment assignment : assignments) {
                    if (Filters.acceptsAny(game, assignment.getShadowCharacters(), Keyword.LURKER))
                        lurkerSkirmishFps.add(assignment.getFellowshipCharacter());
                    else
                        nonLurkerSkirmishFps.add(assignment.getFellowshipCharacter());
                }

                String playerChoosingSkirmishOrder = gameState.getCurrentPlayerId();
                if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.SKIRMISH_ORDER_BY_FIRST_SHADOW_PLAYER)) {
                    final PlayOrder playerOrder = gameState.getPlayerOrder().getCounterClockwisePlayOrder(playerChoosingSkirmishOrder, false);
                    // Skip first one
                    playerOrder.getNextPlayer();
                    playerChoosingSkirmishOrder = playerOrder.getNextPlayer();
                }

                SystemQueueAction chooseNextSkirmishAction = new SystemQueueAction();

                Set<PhysicalCard> skirmishChoice;
                if (nonLurkerSkirmishFps.size() > 0)
                    skirmishChoice = nonLurkerSkirmishFps;
                else
                    skirmishChoice = lurkerSkirmishFps;

                ChooseActiveCardEffect chooseNextSkirmish = new ChooseActiveCardEffect(null, playerChoosingSkirmishOrder, "Choose next skirmish to resolve", Filters.in(skirmishChoice)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        game.getGameState().sendMessage("Next skirmish to resolve is for " + GameUtils.getCardLink(card));
                        final Assignment assignment = findAssignment(assignments, card);
                        game.getGameState().removeAssignment(assignment);
                        game.getActionsEnvironment().addActionToStack(
                                new SkirmishPhaseAction(assignment.getFellowshipCharacter(), assignment.getShadowCharacters()));
                    }
                };
                chooseNextSkirmish.setUseShortcut(false);

                chooseNextSkirmishAction.appendEffect(chooseNextSkirmish);

                game.getActionsEnvironment().addActionToStack(chooseNextSkirmishAction);
                _nextProcess = this;
            } else {
                _nextProcess = new AfterSkirmishesGameProcess();
            }
        }
    }

    private Assignment findAssignment(List<Assignment> assignments, PhysicalCard freePeopleCharacter) {
        for (Assignment assignment : assignments) {
            if (assignment.getFellowshipCharacter() == freePeopleCharacter)
                return assignment;
        }
        return null;
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
