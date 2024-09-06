package com.gempukku.lotro.tournament.action;

import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.tournament.TournamentCallback;

public class CreateGameAction implements TournamentProcessAction{
    private String playerOne;
    private LotroDeck playerOneDeck;
    private String playerTwo;
    private LotroDeck playerTwoDeck;

    public CreateGameAction(String playerOne, LotroDeck playerOneDeck, String playerTwo, LotroDeck playerTwoDeck) {
        this.playerOne = playerOne;
        this.playerOneDeck = playerOneDeck;
        this.playerTwo = playerTwo;
        this.playerTwoDeck = playerTwoDeck;
    }

    @Override
    public void process(TournamentCallback callback) {
        callback.createGame(playerOne, playerOneDeck, playerTwo, playerTwoDeck);
    }
}
