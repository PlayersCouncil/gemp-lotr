package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayerOrder;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.*;

public class ChooseSeatingOrderGameProcess implements GameProcess {
    private final String[] _choices = new String[]{"first", "second", "third", "fourth", "fifth"};
    private final Map<String, Integer> _bids;
    private final PlayerOrderFeedback _playerOrderFeedback;

    private Iterator<String> _biddingOrderPlayers;
    private final String[] _orderedPlayers;
    private boolean _sentBids;

    public ChooseSeatingOrderGameProcess(Map<String, Integer> bids, PlayerOrderFeedback playerOrderFeedback) {
        _bids = bids;
        _playerOrderFeedback = playerOrderFeedback;

        _orderedPlayers = new String[bids.size()];
    }

    /**
     * Highest bid chooses first.  Tied bids are settled at random, which is why this waits until the game (and so
     * the game's source of randomness) is at hand.
     */
    private void determineBiddingOrder(LotroGame game) {
        ArrayList<String> participantList = new ArrayList<>(_bids.keySet());
        game.getGameState().getRandom().shufflePlayers(participantList);

        participantList.sort(new Comparator<>() {
            @Override
            public int compare(String o1, String o2) {
                return _bids.get(o2) - _bids.get(o1);
            }
        });

        _biddingOrderPlayers = participantList.iterator();
    }

    @Override
    public void process(LotroGame game) {
        if (!_sentBids) {
            _sentBids = true;
            determineBiddingOrder(game);
            for (Map.Entry<String, Integer> playerBid : _bids.entrySet())
                game.getGameState().sendMessage(playerBid.getKey() + " bid " + playerBid.getValue());
        }
        checkForNextSeating(game);
    }

    private int getLastEmptySeat() {
        boolean found = false;
        int emptySeatIndex = -1;
        for (int i = 0; i < _orderedPlayers.length; i++) {
            if (_orderedPlayers[i] == null) {
                if (found)
                    return -1;
                found = true;
                emptySeatIndex = i;
            }
        }
        return emptySeatIndex;
    }

    private void checkForNextSeating(LotroGame game) {
        int lastEmptySeat = getLastEmptySeat();
        if (lastEmptySeat == -1)
            askNextPlayerToChoosePlace(game);
        else {
            _orderedPlayers[lastEmptySeat] = _biddingOrderPlayers.next();
            _playerOrderFeedback.setPlayerOrder(new PlayerOrder(Arrays.asList(_orderedPlayers)), _orderedPlayers[0]);
        }
    }

    private String[] getEmptySeatNumbers() {
        List<String> result = new LinkedList<>();
        for (int i = 0; i < _orderedPlayers.length; i++)
            if (_orderedPlayers[i] == null)
                result.add("Go " + _choices[i]);
        return result.toArray(new String[result.size()]);
    }

    private void participantHasChosenSeat(LotroGame game, String participant, int placeIndex) {
        _orderedPlayers[placeIndex] = participant;

        checkForNextSeating(game);
    }

    private void askNextPlayerToChoosePlace(final LotroGame game) {
        final String playerId = _biddingOrderPlayers.next();
        game.getUserFeedback().sendAwaitingDecision(playerId,
                new MultipleChoiceAwaitingDecision(1, "Choose one", getEmptySeatNumbers()) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        game.getGameState().sendMessage(playerId + " has chosen to go " + _choices[index]);
                        participantHasChosenSeat(game, playerId, index);
                    }
                }
        );
    }

    @Override
    public GameProcess getNextProcess() {
        return new FirstPlayerPlaysSiteGameProcess(_bids, _orderedPlayers[0]);
    }
}
