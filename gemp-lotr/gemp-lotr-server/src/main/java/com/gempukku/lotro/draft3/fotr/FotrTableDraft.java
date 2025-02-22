package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.draft3.*;

import java.util.*;

public class FotrTableDraft implements TableDraft {
    //TODO thread locks

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final int maxPlayers;
    private final int rounds;
    private final String uniqueTableIdentifier;

    private int currentRound;
    private int boostersRemaining;
    private final List<String> botNames = new ArrayList<>(List.of("Aragorn_bot", "Gandalf_bot", "Legolas_bot", "Gimli_bot", "Samwise_bot", "Frodo_bot"));
    private final List<DraftPlayer> players = new ArrayList<>();
    private final Set<DraftPlayer> playersReadyToPick = new HashSet<>();

    public FotrTableDraft(StartingCollectionProducer startingCollectionProducer, BoosterProducer boosterProducer, int maxPlayers, int rounds) {
        this.startingCollectionProducer = startingCollectionProducer;
        this.boosterProducer = boosterProducer;
        this.maxPlayers = maxPlayers;
        this.rounds = rounds;
        this.uniqueTableIdentifier = "FotR Table Draft - " + + System.currentTimeMillis();

        this.currentRound = 0; // Not started yet
        this.boostersRemaining = 0; // 0 boosters opened at the table
    }

    @Override
    public void startDraft() {
        if (currentRound == 0) {
            // Fill the table with bots
            while (players.size() < maxPlayers) {
                registerPlayer(botNames.remove(0), true);
            }
            // Ensure random seating
            Collections.shuffle(players);
            // Assign starting cards
            players.forEach(draftPlayer -> {
                draftPlayer.setCardCollection(startingCollectionProducer.getStartingCardCollection(uniqueTableIdentifier, draftPlayer.getName()));
            });
            // Deal first boosters
            dealBoosters();
        }
    }

    @Override
    public boolean readyToPick(DraftPlayer who) {
        // Signal ready
        playersReadyToPick.add(who);


        // If all ready, pick
        if (playersReadyToPick.size() == players.size()) {
            playersReadyToPick.clear();
            players.forEach(DraftPlayer::pickChosenCardAndPassBooster);
            return true;
        }
        return false;
    }

    private void dealBoosters() {
        currentRound++;
        if (currentRound <= rounds) {
            players.forEach(draftPlayer -> {
                draftPlayer.receiveBooster(boosterProducer.getBooster(currentRound));
                boostersRemaining++;
            });
        }
    }

    @Override
    public boolean passBooster(DraftPlayer from, Booster booster) {
        int playerIndex = players.indexOf(from);
        // Check if player is at the table
        if (playerIndex == -1) {
            return false;
        }
        // Check if the booster is empty
        if (booster.isEmpty()) {
            boostersRemaining--;
            if (boostersRemaining == 0) {
                if (currentRound < rounds) {
                    // Start next round
                    dealBoosters();
                } // Else draft is finished
            }
            return true;
        }

        // Find next player
        int nextPlayerIndex;
        if (currentRound % 2 == 1) {
            // Passing left
            nextPlayerIndex = (playerIndex + 1) % players.size();
        } else {
            // Passing right
            nextPlayerIndex = (playerIndex + players.size() - 1) % players.size();
        }
        players.get(nextPlayerIndex).receiveBooster(booster);
        return true;
    }

    @Override
    public boolean isFinished() {
        return boostersRemaining == 0 && currentRound == rounds;
    }

    @Override
    public DraftPlayer registerPlayer(String name) {
        return registerPlayer(name, false);
    }

    @Override
    public DraftPlayer getPlayer(String name) {
        return players.stream().filter(draftPlayer -> draftPlayer.getName().equals(name)).findFirst().orElse(null);
    }

    private DraftPlayer registerPlayer(String name, boolean bot) {
        // Check if the player is not already registered
        boolean alreadyRegistered = players.stream().anyMatch(draftPlayer -> draftPlayer.getName().equals(name));

        if (alreadyRegistered) {
            return null;
        }

        // Register bot or regular player as requested
        DraftPlayer tbr = bot ? new DraftBot(this, name) : new DraftPlayer(this, name);
        players.add(tbr);
        return tbr;
    }

    @Override
    public boolean removePlayer(String name) {
        // Only before draft starts
        if (currentRound != 0) {
            return false;
        }

        DraftPlayer toRemove = null;
        for (DraftPlayer player : players) {
            if (player.getName().equals(name)) {
                toRemove = player;
            }
        }

        return players.remove(toRemove);
    }
}
