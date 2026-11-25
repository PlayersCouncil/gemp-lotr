package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ArcheryGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.AssignmentGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.FellowshipGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ManeuverGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.RegroupGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ShadowPhasesGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.skirmish.PlayoutSkirmishesGameProcess;

import java.util.Set;

/**
 * Carries on a game from a prepared position in the middle of its story (see PregameSetup.carryOn): some sites down
 * the path, on a later turn, or part of the way through a turn.  The board is already as it is to be, the sites the
 * fellowships have passed laid on the path and their markers where they are.
 *
 * A turn is a chain of processes, each of which names the one after it, so beginning at a later phase is a matter of
 * joining the chain at that phase's process.  What the beginning of a turn does that the rest of the turn relies on
 * is done here first: whose turn it is and which turn it is, the cards on the table starting to affect the game, and
 * how many times the fellowship has already moved this turn.  The twilight pool is what the board was arranged with.
 * Nothing that already happened, as far as the story goes, happens again: there is no "start of turn" (and so no
 * sanctuary healing), and the fellowship does not "move to" the site it is at.  No other process is changed by this.
 */
public class MidTurnStartGameProcess implements GameProcess {
    /** The phases a game can be carried on from: those of a turn. */
    public static final Set<Phase> PHASES = Set.of(Phase.FELLOWSHIP, Phase.SHADOW, Phase.MANEUVER, Phase.ARCHERY,
            Phase.ASSIGNMENT, Phase.SKIRMISH, Phase.REGROUP);

    private final String _freePeoplesPlayer;
    private final int _turn;
    private final Phase _phase;
    private final int _movesMade;

    /**
     * @param freePeoplesPlayer Whose turn it is.
     * @param turn The number the turn counter has for it.
     * @param phase The phase the game carries on from, at its start.
     * @param movesMade How many times the fellowship has moved this turn (coming to the site it is at is one).
     */
    public MidTurnStartGameProcess(String freePeoplesPlayer, int turn, Phase phase, int movesMade) {
        if (!PHASES.contains(phase))
            throw new IllegalArgumentException("A game cannot be carried on from the " + phase + " phase");
        _freePeoplesPlayer = freePeoplesPlayer;
        _turn = turn;
        _phase = phase;
        _movesMade = movesMade;
    }

    @Override
    public void process(LotroGame game) {
        GameState gameState = game.getGameState();
        //(beginning a turn empties the pool; this one has been going for a while, and the pool is as it was laid out)
        int twilight = gameState.getTwilightPool();
        gameState.startNumberedPlayerTurn(_freePeoplesPlayer, _turn);
        gameState.startAffectingCardsForCurrentPlayer(game);
        gameState.setTwilight(twilight);
        for (int i = 0; i < _movesMade; i++)
            gameState.increaseMoveCount();
    }

    @Override
    public GameProcess getNextProcess() {
        return switch (_phase) {
            case FELLOWSHIP -> new FellowshipGameProcess();
            case SHADOW -> new ShadowPhasesGameProcess();
            case MANEUVER -> new ManeuverGameProcess();
            case ARCHERY -> new ArcheryGameProcess();
            case ASSIGNMENT -> new AssignmentGameProcess();
            case SKIRMISH -> new PlayoutSkirmishesGameProcess();
            case REGROUP -> new RegroupGameProcess();
            default -> throw new IllegalStateException("A game cannot be carried on from the " + _phase + " phase");
        };
    }
}
