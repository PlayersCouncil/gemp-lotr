package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



public class TableDraftClassic implements TableDraft{
    //TODO thread locks

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final int maxPlayers;
    private final int rounds;
    private final String uniqueTableIdentifier;

    private final DraftTimer draftTimer;
    private long timerStartTime = 0;
    private int timerDuration = 0;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask = null;

    private final CollectionsManager collectionsManager;
    private final CollectionType collectionType;

    private int currentRound;
    private final List<String> botNames = new ArrayList<>(List.of("Aragorn_bot", "Gandalf_bot", "Legolas_bot", "Gimli_bot", "Samwise_bot", "Frodo_bot", "Boromir_bot", "Merry_bot", "Pippin_bot"));
    private final List<DraftPlayer> players = new ArrayList<>();
    private final Map<DraftPlayer, Booster> assignedBoosters = new HashMap<>();
    private final Map<DraftPlayer, String> chosenCards = new HashMap<>();

    public TableDraftClassic(CollectionsManager collectionsManager, CollectionType collectionType,
                             StartingCollectionProducer startingCollectionProducer, BoosterProducer boosterProducer,
                             int maxPlayers, int rounds, DraftTimer draftTimer) {
        this.collectionsManager = collectionsManager;
        this.collectionType = collectionType;

        this.startingCollectionProducer = startingCollectionProducer;
        this.boosterProducer = boosterProducer;
        if (maxPlayers > botNames.size()) {
            throw new IllegalArgumentException("Too many players at the table - " + maxPlayers);
        }
        this.maxPlayers = maxPlayers;
        if (rounds > boosterProducer.getMaxRound(maxPlayers)) {
            throw new IllegalArgumentException("Booster producer cannot support " + rounds + " rounds with " + maxPlayers + " players");
        }
        this.rounds = rounds;
        this.draftTimer = draftTimer;
        this.uniqueTableIdentifier = "" + System.currentTimeMillis();

        this.currentRound = 0; // Not started yet
    }

    @Override
    public List<String> getCardsToPickFrom(DraftPlayer draftPlayer) {
        if (!assignedBoosters.containsKey(draftPlayer)) {
            return List.of();
        }
        return assignedBoosters.get(draftPlayer).getCardsInPack();
    }

    @Override
    public String getChosenCard(DraftPlayer draftPlayer) {
        return chosenCards.get(draftPlayer);
    }

    @Override
    public void advanceDraft() {
        if (isFinished()) {
            return;
        }

        if (currentRound == 0) {
            startDraft();
            startTimer();
            makeBotsDeclare();
        } else if (haveAllPlayersDeclared()) {
            pickDeclaredCards();
            if (areBoostersEmpty()) {
                if (currentRound < rounds) {
                    openNextBoosters();
                } else {
                    // Draft finished, shutdown timer scheduler
                    scheduler.shutdown();
                    return;
                }
            } else {
                passBoosters();
            }
            startTimer();
            makeBotsDeclare();
        } else {
            if (choiceTimePassed()) {
                forceRandomDeclares();
                advanceDraft();
            }
        }
    }

    private void startDraft() {
        // Fill the table with bots
        while (players.size() < maxPlayers) {
            registerPlayer(botNames.remove(0), true);
        }
        // Ensure random seating
        Collections.shuffle(players);
        // Assign starting cards
        assignStartingCollections();
        // Deal first boosters
        openNextBoosters();
    }

    private void assignStartingCollections() {
        players.forEach(draftPlayer -> {
            if (draftPlayer.isBot()) {
                return;
            }

            // Remove collection content if it exists
            try {
                CardCollection collection = getPickedCards(draftPlayer);
                if (collection != null) {
                    for (CardCollection.Item item : collection.getAll()) {
                        for (int i = 0; i < item.getCount(); i++) {
                            collectionsManager.sellCardInPlayerCollection(draftPlayer.getName(), collectionType, item.getBlueprintId(), 0);
                        }
                    }
                }
            } catch (SQLException | IOException ignore) {

            }
            // Create new collection for the player
            var startingCollection = new DefaultCardCollection();
            // Initialize with starting cards
            for (CardCollection.Item item : startingCollectionProducer.getStartingCardCollection(uniqueTableIdentifier, draftPlayer.getName()).getAll())
                startingCollection.addItem(item.getBlueprintId(), item.getCount());
            // Save
            collectionsManager.addPlayerCollection(false, "Draft tournament product", draftPlayer.getName(), collectionType, startingCollection);
        });
    }

    private boolean haveAllPlayersDeclared() {
        return chosenCards.size() == players.size();
    }

    private void pickDeclaredCards() {
        // Assuming everyone declared and boosters contain those cards
        chosenCards.forEach((draftPlayer, card) -> {
            // Remove card from booster
            String pickedCard = assignedBoosters.get(draftPlayer).pickCard(card);
            // Add to collection
            if (draftPlayer.isBot()) {
                return;
            }
            DefaultCardCollection pickedCardCollection = new DefaultCardCollection();
            pickedCardCollection.addItem(pickedCard, 1);
            collectionsManager.addItemsToPlayerCollection(false, "Draft pick", draftPlayer.getName(), collectionType, pickedCardCollection.getAll());
        });
        chosenCards.clear();
    }

    private boolean areBoostersEmpty() {
        return assignedBoosters.values().stream().allMatch(Booster::isEmpty);
    }

    private void openNextBoosters() {
        currentRound++;
        if (currentRound <= rounds) {
            players.forEach(draftPlayer -> assignedBoosters.put(draftPlayer, boosterProducer.getBooster(currentRound)));
        }
    }

    private void passBoosters() {
        // Create a temporary map to store new booster positions
        Map<DraftPlayer, Booster> newAssignments = new HashMap<>();

        for (int i = 0; i < players.size(); i++) {
            // Find next player
            int nextPlayerIndex;
            if (currentRound % 2 == 1) {
                // Passing left
                nextPlayerIndex = (i + 1) % players.size();
            } else {
                // Passing right
                nextPlayerIndex = (i + players.size() - 1) % players.size();
            }
            DraftPlayer nextPlayer = players.get(nextPlayerIndex);

            // Assign the booster to the next player
            DraftPlayer currentPlayer = players.get(i);
            newAssignments.put(nextPlayer, assignedBoosters.get(currentPlayer));
        }
        // Move the boosters
        assignedBoosters.clear();
        assignedBoosters.putAll(newAssignments);
    }

    private void makeBotsDeclare() {
        assignedBoosters.forEach((draftPlayer, booster) -> {
            if (draftPlayer.isBot()) {
                ((DraftBot) draftPlayer).chooseCard(booster.getCardsInPack());
            }
        });
    }

    private void startTimer() {
        if (draftTimer == null) {
            return; // No timer for this draft
        }

        if (assignedBoosters.isEmpty()) {
            return; // No boosters, nothing to time
        }

        // Get the number of cards in the current booster (assuming all players have one booster)
        int cardsInPack = assignedBoosters.values().iterator().next().getCardsInPack().size();

        // Get the time limit for this pick
        timerDuration = draftTimer.getSecondsAllotted(cardsInPack);

        // Record the start time
        timerStartTime = System.currentTimeMillis();

        // Cancel any previously scheduled task
        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(false);
        }

        // Schedule advanceDraft() to be called when time runs out
        scheduledTask = scheduler.schedule((Runnable) this::advanceDraft, timerDuration + 1, TimeUnit.SECONDS);
    }

    private boolean choiceTimePassed() {
        if (draftTimer == null || timerStartTime == 0) {
            return false; // Timer hasn't started or draft has no timer
        }
        long elapsedTime = (System.currentTimeMillis() - timerStartTime) / 1000; // Convert to seconds
        return elapsedTime >= timerDuration;
    }

    @Override
    public int getSecondsRemainingForPick() throws IllegalStateException {
        if (draftTimer == null || timerStartTime == 0) {
            throw new IllegalStateException("No timer set.");
        }
        int elapsedTime = (int) ((System.currentTimeMillis() - timerStartTime) / 1000); // Convert to seconds
        return timerDuration - elapsedTime;
    }

    private void forceRandomDeclares() {
        players.forEach(draftPlayer -> {
            if (chosenCards.containsKey(draftPlayer)) {
                return;
            }
            // Player has not declared card, choose one at random from current booster
            chosenCards.put(draftPlayer, chooseRandom(assignedBoosters.get(draftPlayer).getCardsInPack()));
        });
    }

    private String chooseRandom(List<String> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(source.size());
        return source.get(randomIndex);
    }

    @Override
    public void chooseCard(DraftPlayer who, String what) {
        // Is player at this table
        if (!players.contains(who)) {
            return;
        }

        // Is chosen card in his booster
        if (!assignedBoosters.get(who).getCardsInPack().contains(what)) {
            return;
        }

        chosenCards.put(who, what);

        //Check if all players chose
        advanceDraft();
    }

    @Override
    public boolean isFinished() {
        return areBoostersEmpty() && currentRound == rounds;
    }

    @Override
    public DraftPlayer getPlayer(String name) {
        return players.stream().filter(draftPlayer -> draftPlayer.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public DraftPlayer registerPlayer(String name) {
        // Only before draft starts
        if (currentRound != 0) {
            return null;
        }
        return registerPlayer(name, false);
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

    @Override
    public CardCollection getPickedCards(DraftPlayer draftPlayer) {
        return collectionsManager.getPlayerCollection(draftPlayer.getName(), collectionType.getCode());
    }

    @Override
    public TableStatus getTableStatus() {
        List<PlayerStatus> statuses = new ArrayList<>();
        players.forEach(draftPlayer -> statuses.add(new PlayerStatus(draftPlayer.getName(), chosenCards.containsKey(draftPlayer))));
        return new TableStatus(statuses, currentRound % 2 == 1); // Alternate pick order with each booster
    }
}
