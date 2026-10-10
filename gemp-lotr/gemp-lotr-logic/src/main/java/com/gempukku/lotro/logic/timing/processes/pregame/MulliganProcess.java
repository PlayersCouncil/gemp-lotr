package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.BetweenTurnsProcess;

import java.util.HashSet;
import java.util.Set;

public class MulliganProcess implements GameProcess {
    private final PlayOrder _playOrder;

    private GameProcess _nextProcess;

    public MulliganProcess(PlayOrder playOrder) {
        _playOrder = playOrder;
    }

    @Override
    public void process(final LotroGame game) {
        final String nextPlayer = _playOrder.getNextPlayer();
        if (nextPlayer != null) {
            final int handSize = game.getModifiersQuerying().getHandSize(game, nextPlayer);
            game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Do you wish to mulligan? (Shuffle cards back and draw " + (handSize - 2) + ")", new String[]{"No", "Yes"}) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            if (index == 1) {
                                final GameState gameState = game.getGameState();
                                gameState.sendMessage(nextPlayer + " mulligans");
                                Set<PhysicalCard> hand = new HashSet<>(gameState.getHand(nextPlayer));
                                gameState.removeCardsFromZone(nextPlayer, hand);
                                for (PhysicalCard card : hand)
                                    gameState.addCardToZone(game, card, Zone.DECK);

                                gameState.shuffleDeck(nextPlayer);
                                for (int i = 0; i < handSize - 2; i++)
                                    gameState.playerDrawsCard(nextPlayer);
                            } else {
                                game.getGameState().sendMessage(nextPlayer + " decides not to mulligan");
                            }
                        }
                    });
            _nextProcess = new MulliganProcess(_playOrder);
        } else {
            //The first turn is about to begin.  What the starting fellowships left "until the end of the turn"
            // ends here, so that it does not reach the first turn (Theoden, The Renowned 13_137), and so do
            // their counts of what was done "this turn".  No turn has ended, though, so no end of turn is
            // announced (F-0327): the player who goes second is the current player at this point, and an
            // EndOfTurnResult would be answered by the "at the end of each turn" texts of his cards and of
            // every meta-site.
            ((ModifiersLogic) game.getModifiersEnvironment()).signalEndOfTurn();
            ((DefaultActionsEnvironment) game.getActionsEnvironment()).signalEndOfTurn();
            _nextProcess = new BetweenTurnsProcess();
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
