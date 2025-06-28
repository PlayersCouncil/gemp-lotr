package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTournamentQueue implements TournamentQueue {
    protected final String _id;
    protected final String _tournamentQueueName;
    protected Queue<String> _players = new LinkedList<>();
    protected String _playerList;
    protected Map<String, LotroDeck> _playerDecks = new HashMap<>();
    private final TournamentQueueCallback tournamentQueueCallback;
    private final CollectionsManager collectionsManager;
    private final boolean destroyWhenTournamentStarts;
    protected boolean shouldDestroy = false;

    private final boolean readyCheckNeeded;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> readyCheckTask = null;
    private final int readyCheckTimeSecs;
    private long timerStartTime = 0;
    private final List<String> confirmedPlayers = Collections.synchronizedList(new ArrayList<>());

    private final boolean startableEarly;

    private final CollectionType _currencyCollection = CollectionType.MY_CARDS;

    protected TournamentInfo _tournamentInfo;

    protected final TournamentService _tournamentService;

    public AbstractTournamentQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info,
                                   TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager, boolean destroyWhenTournamentStarts) {
        this(tournamentService,queueId, queueName, info, false, -1, tournamentQueueCallback, collectionsManager, destroyWhenTournamentStarts);
    }

    public AbstractTournamentQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info,
                                   boolean startableEarly, int readyCheckTimeSecs, TournamentQueueCallback tournamentQueueCallback,
                                   CollectionsManager collectionsManager, boolean destroyWhenTournamentStarts) {
        _tournamentService = tournamentService;
        _id = queueId;
        _tournamentQueueName = queueName;
        _tournamentInfo = info;

        this.startableEarly = startableEarly;
        this.tournamentQueueCallback = tournamentQueueCallback;
        this.collectionsManager = collectionsManager;
        this.destroyWhenTournamentStarts = destroyWhenTournamentStarts;

        this.readyCheckTimeSecs = readyCheckTimeSecs;
        readyCheckNeeded = readyCheckTimeSecs > 0;
        if (readyCheckNeeded) {
            scheduler = Executors.newSingleThreadScheduledExecutor();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }));
        } else {
            scheduler = null;
        }
    }

    protected synchronized void startTournament() {
        if (readyCheckRequired() && !isReadyCheckTimerRunning()) {
            startReadyCheckTimer();
            return;
        } else if (readyCheckRequired() && !allConfirmedReadyCheck()) {
            return; // Wait for timer to end or for all players to accept
        }

        if (_players.isEmpty()) { // Don't start tournament with 0 players
            return;
        }

        TournamentInfo tournamentInfo;

        if (!destroyWhenTournamentStarts) {
            String tid = _tournamentInfo.generateTimestampId();
            String tournamentName = _tournamentQueueName + " - " + DateUtils.getStringDateWithHour();
            tournamentInfo = getNewInfo(tid, tournamentName);
        } else {
            tournamentInfo = getNewInfo(_tournamentInfo._params.tournamentId, _tournamentInfo._params.name);
        }



        for (String player : _players) {
            _tournamentService.recordTournamentPlayer(tournamentInfo._params.tournamentId, player, _playerDecks.get(player));
        }
        clearPlayersInternal();


        var tournament = _tournamentService.addTournament(tournamentInfo);
        tournamentQueueCallback.createTournament(tournament);

        shouldDestroy = destroyWhenTournamentStarts;
    }

    @Override
    public TournamentInfo getInfo() { return _tournamentInfo; }

    private TournamentInfo getNewInfo(String tournamentId, String tournamentName) {
        TournamentParams params = getNewParams(tournamentId, tournamentName);

        boolean isSealed = _tournamentInfo instanceof SealedTournamentInfo;
        boolean isSoloDraft = _tournamentInfo instanceof SoloDraftTournamentInfo;
        boolean isSoloTableDraft = _tournamentInfo instanceof SoloTableDraftTournamentInfo;
        boolean isTableDraft = _tournamentInfo instanceof TableDraftTournamentInfo;

        TournamentInfo newInfo;

        if (isSealed) {
            newInfo = new SealedTournamentInfo((SealedTournamentInfo) _tournamentInfo, (SealedTournamentParams) params);
        } else if (isSoloDraft) {
            newInfo = new SoloDraftTournamentInfo((SoloDraftTournamentInfo) _tournamentInfo, (SoloDraftTournamentParams) params);
        } else if(isSoloTableDraft) {
            newInfo = new SoloTableDraftTournamentInfo((SoloTableDraftTournamentInfo) _tournamentInfo, (SoloTableDraftTournamentParams) params);
        } else if (isTableDraft) {
            newInfo = new TableDraftTournamentInfo((TableDraftTournamentInfo) _tournamentInfo, (TableDraftTournamentParams) params);
        } else {
            newInfo = new TournamentInfo(_tournamentInfo, params);
        }

        return newInfo;
    }

    private TournamentParams getNewParams(String tournamentId, String tournamentName) {
        TournamentParams tbr;
        if (_tournamentInfo instanceof SealedTournamentInfo) {
            tbr = new SealedTournamentParams() {{
                this.type = Tournament.TournamentType.SEALED;
                this.deckbuildingDuration = ((SealedTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((SealedTournamentParams) _tournamentInfo._params).turnInDuration;
                this.sealedFormatCode = ((SealedTournamentParams) _tournamentInfo._params).sealedFormatCode;
            }};
        } else if (_tournamentInfo instanceof SoloDraftTournamentInfo) {
            tbr = new SoloDraftTournamentParams() {{
                this.type = Tournament.TournamentType.SOLODRAFT;
                this.deckbuildingDuration = ((SoloDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((SoloDraftTournamentParams) _tournamentInfo._params).turnInDuration;
                this.soloDraftFormatCode = ((SoloDraftTournamentParams) _tournamentInfo._params).soloDraftFormatCode;
            }};
        } else if (_tournamentInfo instanceof SoloTableDraftTournamentInfo) {
            tbr = new SoloTableDraftTournamentParams() {{
                this.type = Tournament.TournamentType.TABLE_SOLODRAFT;
                this.deckbuildingDuration = ((SoloTableDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((SoloTableDraftTournamentParams) _tournamentInfo._params).turnInDuration;
                this.soloTableDraftFormatCode = ((SoloTableDraftTournamentParams) _tournamentInfo._params).soloTableDraftFormatCode;
            }};
        } else if (_tournamentInfo instanceof TableDraftTournamentInfo) {
            tbr = new TableDraftTournamentParams() {{
                this.type = Tournament.TournamentType.TABLE_DRAFT;
                this.draftTimerType = ((TableDraftTournamentParams) _tournamentInfo._params).draftTimerType;
                this.deckbuildingDuration = ((TableDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((TableDraftTournamentParams) _tournamentInfo._params).turnInDuration;
                this.tableDraftFormatCode = ((TableDraftTournamentParams) _tournamentInfo._params).tableDraftFormatCode;
            }};
        } else {
            tbr = new TournamentParams();
            tbr.type = Tournament.TournamentType.CONSTRUCTED;
        }

        tbr.tournamentId = tournamentId;
        tbr.name = tournamentName;
        tbr.format = getFormatCode();
        tbr.startTime = DateUtils.Now().toLocalDateTime();
        tbr.playoff = _tournamentInfo._params.playoff;
        tbr.manualKickoff = false;
        tbr.cost = getCost();
        tbr.prizes = _tournamentInfo._params.prizes;
        tbr.minimumPlayers = _tournamentInfo._params.minimumPlayers;
        tbr.maximumPlayers = _tournamentInfo._params.maximumPlayers;
        tbr.requiresDeck = _tournamentInfo._params.requiresDeck;
        tbr.wc = _tournamentInfo._params.wc;

        return tbr;
    }

    @Override
    public String getTournamentQueueName() {
        return _tournamentQueueName;
    }

    @Override
    public String getPairingDescription() {
        return _tournamentInfo.PairingMechanism.getPlayOffSystem();
    }

    @Override
    public final CollectionType getCollectionType() {
        return _tournamentInfo.Collection;
    }

    @Override
    public final String getPrizesDescription() {
        return _tournamentInfo.Prizes.getPrizeDescription();
    }

    protected void regeneratePlayerList() {
        _playerList =  String.join(", ", new ArrayList<>(_players).stream().sorted().toList() );
    }

    @Override
    public final synchronized void joinPlayer(Player player, LotroDeck deck) throws SQLException, IOException {
        if (!_players.contains(player.getName()) && isJoinable()) {
            if (_tournamentInfo._params.cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection("Joined "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost)) {
                _players.add(player.getName());
                regeneratePlayerList();
                if (_tournamentInfo._params.requiresDeck)
                    _playerDecks.put(player.getName(), deck);
            }
        }
    }

    @Override
    public final synchronized void joinPlayer(Player player) throws SQLException, IOException {
        if (_tournamentInfo._params.requiresDeck) { // Only for limited games
            return;
        }
        if (!_players.contains(player.getName()) && isJoinable()) {
            if (_tournamentInfo._params.cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection("Joined "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost)) {
                _players.add(player.getName());
                regeneratePlayerList();
            }
        }
    }

    @Override
    public final synchronized void leavePlayer(Player player) throws SQLException, IOException {
        leavePlayer(player.getName());
    }

    protected final void leavePlayer(String player) throws SQLException, IOException {
        if (_players.contains(player)) {
            if (_tournamentInfo._params.cost > 0)
                collectionsManager.addCurrencyToPlayerCollection(true, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost);
            _players.remove(player);
            regeneratePlayerList();
            _playerDecks.remove(player);

            confirmedPlayers.remove(player);
        }
    }

    @Override
    public final synchronized void leaveAllPlayers() throws SQLException, IOException {
        if (_tournamentInfo._params.cost > 0) {
            for (String player : _players)
                collectionsManager.addCurrencyToPlayerCollection(false, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost);
        }
        clearPlayersInternal();
    }

    protected void clearPlayersInternal() {
        _players.clear();
        regeneratePlayerList();
        _playerDecks.clear();

        confirmedPlayers.clear();
    }

    @Override
    public final synchronized int getPlayerCount() {
        return _players.size();
    }

    @Override
    public String getPlayerList() {
        if (_tournamentQueueName.toLowerCase().contains("competitive")) {
            return "Competitive, player count: " + _players.size();
        } else {
            return _playerList;
        }
    }

    @Override
    public final synchronized boolean isPlayerSignedUp(String player) {
        return _players.contains(player);
    }

    @Override
    public final String getID() {
        return _id;
    }

    @Override
    public final int getCost() {
        return _tournamentInfo._params.cost;
    }

    @Override
    public final boolean isRequiresDeck() {
        return _tournamentInfo._params.requiresDeck;
    }

    @Override
    public final String getFormatCode() {
        return _tournamentInfo.Format.getCode();
    }

    @Override
    public boolean isStartable(String byWhom) {
        return startableEarly && byWhom.equals(_players.peek()) && !isReadyCheckTimerRunning(); // Cannot start if check is already in progress
    }

    protected boolean isStartableEarly() {
        return startableEarly;
    }

    @Override
    public boolean requestStart(String byWhom) {
        if (isStartable(byWhom)) {
            startTournament();
            return true;
        }
        return false;
    }

    @Override
    public int getSecondsRemainingForReadyCheck() {
        if (!isReadyCheckTimerRunning()) {
            return -1; // No timer
        }
        int elapsedTime = (int) ((System.currentTimeMillis() - timerStartTime) / 1000); // Convert to seconds
        return readyCheckTimeSecs - elapsedTime;
    }

    protected boolean isReadyCheckTimerRunning() {
        return readyCheckTask != null
                && !readyCheckTask.isCancelled()
                && !readyCheckTask.isDone()
                && timerStartTime != 0;
    }

    @Override
    public boolean confirmReadyCheck(String player) {
        if (isReadyCheckTimerRunning()) {
            if (!hasConfirmedReadyCheck(player)) {
                confirmedPlayers.add(player);
                if (allConfirmedReadyCheck()) {
                    startTournament();
                    cancelReadyCheckTimer();
                }
                return true;
            }
        }
        return false;
    }

    private boolean allConfirmedReadyCheck() {
        return confirmedPlayers.containsAll(_players);
    }

    @Override
    public boolean hasConfirmedReadyCheck(String player) {
        return confirmedPlayers.contains(player);
    }

    private boolean readyCheckRequired() {
        return readyCheckNeeded && _players.size() > 1; // Do not bother solo drafter with ready check
    }

    private void startReadyCheckTimer() {
        if (readyCheckNeeded) {
            confirmedPlayers.clear();

            // Record the start time
            timerStartTime = System.currentTimeMillis();

            // Cancel any previously scheduled task
            if (readyCheckTask != null && !readyCheckTask.isDone()) {
                readyCheckTask.cancel(false);
            }

            readyCheckTask = scheduler.schedule((Runnable) () -> removeAfkPlayersFromQueue(collectionsManager), readyCheckTimeSecs, TimeUnit.SECONDS);
        }
    }

    private void removeAfkPlayersFromQueue(CollectionsManager collectionsManager) {
        List<String> afkPlayers = new ArrayList<>();
        _players.stream().filter(s -> !confirmedPlayers.contains(s)).forEach(afkPlayers::add);

        afkPlayers.forEach(afkPlayer -> {
            try {
                leavePlayer(afkPlayer);
            } catch (Exception e) { // Entry refund failed, remove anyway
                _players.remove(afkPlayer);
                regeneratePlayerList();
                _playerDecks.remove(afkPlayer);
            }
        });
    }

    private void cancelReadyCheckTimer() {
        if (readyCheckTask != null) {
            readyCheckTask.cancel(false);
            readyCheckTask = null;
        }
    }

    @Override
    public boolean isWC() {
        return _tournamentInfo._params.wc;
    }

    @Override
    public String getDraftCode() {
        if (_tournamentInfo instanceof TableDraftTournamentInfo) {
            return ((TableDraftTournamentInfo) _tournamentInfo).tableDraftDefinition.getCode();
        }
        return null;
    }
}
