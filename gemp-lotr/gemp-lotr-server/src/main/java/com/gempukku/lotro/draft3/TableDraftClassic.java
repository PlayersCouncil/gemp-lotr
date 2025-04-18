package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.bot.DraftBot;
import com.gempukku.lotro.draft3.bot.WeightDraftBot;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TableDraftClassic implements TableDraft{
    private static final int MISSED_CARDS_ALLOWED = 2;

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final int maxPlayers;
    private final int rounds;
    private final String uniqueTableIdentifier;
    private final Map<String, Double> cardValuesForBots;

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
    private final Map<DraftPlayer, Integer> missedPicksInARow = new HashMap<>();
    private final Map<DraftBot, MutableCardCollection> botCollections = new HashMap<>();

    public TableDraftClassic(CollectionsManager collectionsManager, CollectionType collectionType,
                             StartingCollectionProducer startingCollectionProducer, BoosterProducer boosterProducer,
                             int maxPlayers, int rounds, DraftTimer draftTimer, Map<String, Double> cardValuesForBots) {
        this.collectionsManager = collectionsManager;
        this.collectionType = collectionType;

        this.startingCollectionProducer = startingCollectionProducer;
        this.boosterProducer = boosterProducer;
        if (maxPlayers > botNames.size()) {
            throw new IllegalArgumentException("Too many players at the table - " + maxPlayers);
        }
        this.maxPlayers = maxPlayers;
        if (rounds > boosterProducer.getMaxRound()) {
            throw new IllegalArgumentException("Booster producer cannot support " + rounds + " rounds");
        }
        this.rounds = rounds;
        this.draftTimer = draftTimer;
        this.uniqueTableIdentifier = "" + System.currentTimeMillis();
        this.cardValuesForBots = cardValuesForBots;

        this.currentRound = 0; // Not started yet
    }

    @Override
    public List<String> getCardsToPickFrom(DraftPlayer draftPlayer) {
        readLock.lock();
        try {
            if (!assignedBoosters.containsKey(draftPlayer)) {
                return List.of();
            }
            return assignedBoosters.get(draftPlayer).getCardsInPack();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getChosenCard(DraftPlayer draftPlayer) {
        readLock.lock();
        try {
            return chosenCards.get(draftPlayer);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void advanceDraft() {
        writeLock.lock();
        try {
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
                makeAfkersDeclare();
            } else {
                if (choiceTimePassed()) {
                    forcePlayerDeclares();
                    advanceDraft();
                }
            }
        } finally {
            writeLock.unlock();
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

            // Remove collection content if it exists
            try {
                CardCollection collection = getPickedCards(draftPlayer);
                if (collection != null) {
                    if (draftPlayer instanceof DraftBot) {
                        botCollections.remove(draftPlayer);
                    } else {
                        collectionsManager.removeFromPlayerCollection(draftPlayer.getName(), collectionType.getCode(), collection, "Duplicate tournament collection");
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
            if (draftPlayer instanceof DraftBot) {
                botCollections.put(((DraftBot) draftPlayer), startingCollection);
            } else {
                collectionsManager.addPlayerCollection(false, "Draft tournament product", draftPlayer.getName(), collectionType, startingCollection);
            }
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
            if (draftPlayer instanceof DraftBot) {
                botCollections.get(((DraftPlayer) draftPlayer)).addItem(pickedCard, 1);
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
            if (draftPlayer instanceof DraftBot) {
                chooseCard(draftPlayer, ((DraftBot) draftPlayer).chooseCard(booster.getCardsInPack()));
            }
        });
    }

    private void makeAfkersDeclare() {
        // This does not advance the draft - if all players are afk, draft will go slow

        assignedBoosters.forEach((draftPlayer, booster) -> {
            if (missedPicksInARow.get(draftPlayer) > MISSED_CARDS_ALLOWED) {
                // Player missed a lot of picks, choose one at random from current booster
                chosenCards.put(draftPlayer, chooseLikeBot(assignedBoosters.get(draftPlayer).getCardsInPack()));
                missedPicksInARow.put(draftPlayer, missedPicksInARow.get(draftPlayer) + 1);
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
        readLock.lock();
        try {
            if (draftTimer == null || timerStartTime == 0) {
                throw new IllegalStateException("No timer set.");
            }
            int elapsedTime = (int) ((System.currentTimeMillis() - timerStartTime) / 1000); // Convert to seconds
            return timerDuration - elapsedTime;
        } finally {
            readLock.unlock();
        }
    }

    private void forcePlayerDeclares() {
        players.forEach(draftPlayer -> {
            if (chosenCards.containsKey(draftPlayer)) {
                return;
            }
            // Player has not declared card, choose one at random from current booster
            chosenCards.put(draftPlayer, chooseLikeBot(assignedBoosters.get(draftPlayer).getCardsInPack()));
            // Flag that inactivity
            missedPicksInARow.put(draftPlayer, missedPicksInARow.get(draftPlayer) + 1);
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

    private String chooseLikeBot(List<String> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return new WeightDraftBot(this, "dummy", cardValuesForBots).chooseCard(source);
    }

    @Override
    public void chooseCard(DraftPlayer who, String what) {
        writeLock.lock();
        try {
            // Is player at this table
            if (!players.contains(who)) {
                return;
            }

            // Is chosen card in his booster
            if (!assignedBoosters.get(who).getCardsInPack().contains(what)) {
                return;
            }

            chosenCards.put(who, what);
            missedPicksInARow.put(who, 0);

            //Check if all players chose
            if (!(who instanceof DraftBot)) {
                advanceDraft();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isFinished() {
        readLock.lock();
        try {
            return areBoostersEmpty() && currentRound == rounds;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DraftPlayer getPlayer(String name) {
        readLock.lock();
        try {
            return players.stream().filter(draftPlayer -> draftPlayer.getName().equals(name)).findFirst().orElse(null);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DraftPlayer registerPlayer(String name) {
        writeLock.lock();
        try {
            // Only before draft starts
            if (currentRound != 0 || name == null) {
                return null;
            }
            return registerPlayer(name, false);
        } finally {
            writeLock.unlock();
        }
    }

    private DraftPlayer registerPlayer(String name, boolean bot) {
        // Check if the player is not already registered
        boolean alreadyRegistered = players.stream().anyMatch(draftPlayer -> draftPlayer.getName().equals(name));

        if (alreadyRegistered) {
            return null;
        }

        // Register bot or regular player as requested
        DraftPlayer tbr = bot ? new WeightDraftBot(this, name, cardValuesForBots) : new DraftPlayer(this, name);
        players.add(tbr);
        missedPicksInARow.put(tbr, 0);
        return tbr;
    }

    @Override
    public boolean removePlayer(String name) {
        writeLock.lock();
        try {
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
            if (players.remove(toRemove)) {
                missedPicksInARow.remove(toRemove);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public CardCollection getPickedCards(DraftPlayer draftPlayer) {
        readLock.lock();
        try {
            if (draftPlayer instanceof DraftBot) {
                return botCollections.get(((DraftBot) draftPlayer));
            }

            return collectionsManager.getPlayerCollection(draftPlayer.getName(), collectionType.getCode());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Document getDocument(DraftPlayer draftPlayer, LotroCardBlueprintLibrary cardLibrary, LotroFormatLibrary formatLibrary) throws ParserConfigurationException {
        readLock.lock();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            Element rootElement = doc.createElement("draftStatus");
            doc.appendChild(rootElement);

            appendPlayersInfo(doc, rootElement);
            appendPickedCards(doc, rootElement, getPickedCards(draftPlayer), cardLibrary, formatLibrary);
            appendAvailablePicks(doc, rootElement, getCardsToPickFrom(draftPlayer), getChosenCard(draftPlayer));
            try {
                appendTimeRemaining(doc, rootElement, getSecondsRemainingForPick());
            } catch (IllegalStateException ignore) {
                // No timer
            }
            appendRoundsInfo(doc, rootElement);

            return doc;
        } finally {
            readLock.unlock();
        }
    }

    private void appendPlayersInfo(Document doc, Element rootElement) {
        players.forEach(draftPlayer -> {
            Element playerElement = doc.createElement("player");
            playerElement.setAttribute("name", draftPlayer.getName());
            playerElement.setAttribute("hasChosen", String.valueOf(chosenCards.containsKey(draftPlayer)));
            rootElement.appendChild(playerElement);
        });

        Element pickOrder = doc.createElement("pickOrderAscending");
        pickOrder.setAttribute("value", String.valueOf(currentRound % 2 == 1)); // Alternate pick order with each booster
        rootElement.appendChild(pickOrder);
    }


    private void appendTimeRemaining(Document doc, Element rootElement, int timeRemaining) {
        Element time = doc.createElement("timeRemaining");
        time.setAttribute("value", "" + timeRemaining);
        rootElement.appendChild(time);
    }

    private void appendRoundsInfo(Document doc, Element rootElement) {
        Element time = doc.createElement("roundsInfo");
        time.setAttribute("currentRound", "" + currentRound);
        time.setAttribute("roundsTotal", "" + rounds);
        rootElement.appendChild(time);
    }

    private void appendPickedCards(Document doc, Element rootElement, CardCollection toAppend, LotroCardBlueprintLibrary cardLibrary, LotroFormatLibrary formatLibrary) {
        if (toAppend == null) {
            return;
        }

        SortAndFilterCards sortAndFilterCards = new SortAndFilterCards();
        List<CardCollection.Item> pickedCards = sortAndFilterCards.process(
                "sort:cardType,culture,name",
                toAppend.getAll(),
                cardLibrary,
                formatLibrary
        );
        for (CardCollection.Item item : pickedCards) {
            Element pickedCard = doc.createElement("pickedCard");
            pickedCard.setAttribute("blueprintId", item.getBlueprintId());
            pickedCard.setAttribute("count", String.valueOf(item.getCount()));
            rootElement.appendChild(pickedCard);
        }
    }

    private void appendAvailablePicks(Document doc, Element rootElement, List<String> availablePicks, String chosenCard) {
        if (availablePicks == null || availablePicks.isEmpty()) {
            return;
        }

        for (String availableChoice : availablePicks) {
            Element availablePick = doc.createElement("availablePick");
            availablePick.setAttribute("id", availableChoice);
            availablePick.setAttribute("blueprintId", availableChoice);
            if (chosenCard != null && chosenCard.equals(availableChoice)) {
                availablePick.setAttribute("chosen", "true");
            }
            rootElement.appendChild(availablePick);
        }
    }
}
