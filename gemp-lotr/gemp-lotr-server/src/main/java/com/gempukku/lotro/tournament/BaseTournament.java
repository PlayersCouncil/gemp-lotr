package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.competitive.ModifiedMedianStandingsProducer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.actions.AbstractCostToEffectAction;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.BroadcastAction;
import com.gempukku.lotro.tournament.action.CreateGameAction;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BaseTournament implements Tournament {
    // 10 minutes
    public static final int DeckBuildTime = 10 * 60 * 1000;
    public static Duration PairingDelayTime = Duration.ofMinutes(1);

    protected final String _tournamentId;
    protected TournamentInfo _tournamentInfo;

    protected final Set<String> _players = new HashSet<>();
    protected String _playerList;
    protected final Map<String, LotroDeck> _playerDecks = new HashMap<>();
    protected final Set<String> _droppedPlayers  = new HashSet<>();
    protected final HashMap<String, Integer> _playerByes = new HashMap<>();

    protected final Set<String> _currentlyPlayingPlayers = new HashSet<>();
    protected final Set<TournamentMatch> _finishedTournamentMatches = new HashSet<>();

    protected final TournamentService _tournamentService;
    protected final CollectionsManager _collectionsManager;
    protected final ProductLibrary _productLibrary;
    protected final LotroFormatLibrary _formatLibrary;
    protected final TableHolder _tables;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Lock readLock = lock.readLock();
    protected final Lock writeLock = lock.writeLock();

    protected TournamentTask _nextTask;

    protected List<PlayerStanding> _currentStandings;

    protected String _tournamentReport;

    public BaseTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
            LotroFormatLibrary formatLibrary,  TableHolder tables, String tournamentId) {
        _tournamentService = tournamentService;
        _collectionsManager = collectionsManager;
        _productLibrary = productLibrary;
        _formatLibrary = formatLibrary;
        _tournamentId = tournamentId;
        _tables = tables;

        RefreshTournamentInfo();
    }

    protected abstract void recreateTournamentInfo(DBDefs.Tournament data);


    public void RefreshTournamentInfo() {
        var data = _tournamentService.retrieveTournamentData(_tournamentId);
        if(data == null)
            return;

        writeLock.lock();
        try {
            recreateTournamentInfo(data);

            _players.clear();
            _players.addAll(_tournamentService.retrieveTournamentPlayers(_tournamentId));

            _droppedPlayers.clear();
            _droppedPlayers.addAll(_tournamentService.retrieveAbandonedPlayers(_tournamentId));

            _playerDecks.clear();
            _playerDecks.putAll(_tournamentService.retrievePlayerDecks(_tournamentId, _tournamentInfo.Format.getCode()));

            _playerByes.clear();
            _playerByes.putAll(_tournamentService.retrieveTournamentByes(_tournamentId));

            _finishedTournamentMatches.clear();

            regeneratePlayerList();

            _currentlyPlayingPlayers.clear();

            resumeTournamentFromDatabase();

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public TournamentInfo getInfo(){
        return _tournamentInfo;
    }


    protected abstract void resumeTournamentFromDatabase();

    protected void regeneratePlayerList() {
        _playerList = "";

        for(var player : _players) {
            if(!_droppedPlayers.contains(player)) {
                _playerList += player + ", ";
            }
        }

        if(!_players.isEmpty() && _playerList.length() > 2) {
            _playerList = _playerList.substring(0, _playerList.length() - 2);
        }

        if(!_droppedPlayers.isEmpty()) {
            _playerList += ", " + String.join("*, ", _droppedPlayers);
            if(!_droppedPlayers.isEmpty()) {
                _playerList += "*";
            }
        }

    }

    // Used for test only
    protected void setWaitForPairingsTime(long waitForPairingsTime) {
        PairingDelayTime = Duration.ofMillis(waitForPairingsTime);
    }

    @Override
    public String getPlayOffSystem() {
        return _tournamentInfo.PairingMechanism.getPlayOffSystem();
    }

    @Override
    public int getPlayersInCompetitionCount() {
        readLock.lock();
        try {
            return _players.size() - _droppedPlayers.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getPlayerList() {
        return _playerList;
    }

    @Override
    public String getTournamentId() {
        return _tournamentId;
    }

    @Override
    public String getTournamentName() {
        return _tournamentInfo.Parameters().name;
    }

    @Override
    public Stage getTournamentStage() {
        return _tournamentInfo.Stage;
    }

    @Override
    public CollectionType getCollectionType() {
        return _tournamentInfo.Collection;
    }

    @Override
    public int getCurrentRound() {
        return _tournamentInfo.Round;
    }

    @Override
    public String getFormatCode() {
        return _tournamentInfo.Format.getCode();
    }

    @Override
    public void issuePlayerMaterial(String player) {
        //sealed only
    }

    @Override
    public boolean isPlayerInCompetition(String player) {
        readLock.lock();
        try {
            return getTournamentStage() != Stage.FINISHED && _players.contains(player) && !_droppedPlayers.contains(player);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isPlayerAbandoned(String player) {
        readLock.lock();
        try {
            return _droppedPlayers.contains(player);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void reportGameFinished(String winner, String loser) {
        writeLock.lock();
        try {
            if (_currentlyPlayingPlayers.contains(winner) && _currentlyPlayingPlayers.contains(loser)) {
                _tournamentService.recordMatchupResult(_tournamentId, getCurrentRound(), winner);
                _currentlyPlayingPlayers.remove(winner);
                _currentlyPlayingPlayers.remove(loser);
                _finishedTournamentMatches.add(
                        new TournamentMatch(winner, loser, winner, getCurrentRound()));
                if (_tournamentInfo.PairingMechanism.shouldDropLoser()) {
                    _tournamentService.recordPlayerTournamentAbandon(_tournamentId, loser);
                    _droppedPlayers.add(loser);
                }
                _currentStandings = null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public LotroDeck getPlayerDeck(String player) {
        readLock.lock();
        try {
            return _playerDecks.get(player);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String dropPlayer(String player) {
        writeLock.lock();
        try {
            if (_currentlyPlayingPlayers.contains(player))
                return "You are currently playing a match.  Please complete your match (or concede) before abandoning.";
            if (getTournamentStage() == Stage.FINISHED)
                return "That tournament has already concluded and there is no need to abandon.";
            if (_droppedPlayers.contains(player))
                return "You have already abandoned that tournament.";
            if (!_players.contains(player))
                return "You cannot abandon a tournament that you never joined in the first place.";

            _tournamentService.recordPlayerTournamentAbandon(_tournamentId, player);
            _droppedPlayers.add(player);
            regeneratePlayerList();
            return "You have successfully dropped from the tournament.  Thanks for playing!";
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<PlayerStanding> getCurrentStandings() {
        List<PlayerStanding> result = _currentStandings;
        if (result != null)
            return result;

        readLock.lock();
        try {
            _currentStandings = ModifiedMedianStandingsProducer.produceStandings(_players, _finishedTournamentMatches, 1, 0, _playerByes);
            return _currentStandings;
        } finally {
            readLock.unlock();
        }
    }

    protected TournamentProcessAction finishTournament(CollectionsManager collectionsManager) {
        _tournamentInfo.Stage = Stage.FINISHED;
        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
        awardPrizes(collectionsManager);
        return new BroadcastAction("Tournament " + getTournamentName() + " is finished.");
    }

    protected void awardPrizes(CollectionsManager collectionsManager) {
        List<PlayerStanding> list = getCurrentStandings();
        for (PlayerStanding playerStanding : list) {
            CardCollection prizes = _tournamentInfo.Prizes.getPrizeForTournament(playerStanding, list.size());
            if (prizes != null)
                collectionsManager.addItemsToPlayerCollection(true, "Tournament " + getTournamentName() + " prize", playerStanding.playerName, CollectionType.MY_CARDS, prizes.getAll());
            CardCollection trophies = _tournamentInfo.Prizes.getTrophyForTournament(playerStanding, list.size());
            if (trophies != null)
                collectionsManager.addItemsToPlayerCollection(true, "Tournament " + getTournamentName() + " trophy", playerStanding.playerName, CollectionType.TROPHY, trophies.getAll());
        }
    }

    protected TournamentProcessAction createNewGame(String playerOne, String playerTwo) {
        return new CreateGameAction(playerOne, _playerDecks.get(playerOne),
                playerTwo, _playerDecks.get(playerTwo));
    }

    protected void doPairing(List<TournamentProcessAction> actions, CollectionsManager collectionsManager) {
        _tournamentInfo.Round++;
        _tournamentService.recordTournamentRound(_tournamentId, _tournamentInfo.Round);
        Map<String, String> pairingResults = new HashMap<>();
        Set<String> byeResults = new HashSet<>();

        Map<String, Set<String>> previouslyPaired = getPreviouslyPairedPlayersMap();

        boolean finished = _tournamentInfo.PairingMechanism.pairPlayers(_tournamentInfo.Round, _players, _droppedPlayers, _playerByes,
                getCurrentStandings(), previouslyPaired, pairingResults, byeResults);
        if (finished) {
            actions.add(finishTournament(collectionsManager));
        } else {
            for (Map.Entry<String, String> pairing : pairingResults.entrySet()) {
                String playerOne = pairing.getKey();
                String playerTwo = pairing.getValue();
                _tournamentService.recordMatchup(_tournamentId, _tournamentInfo.Round, playerOne, playerTwo);
                _currentlyPlayingPlayers.add(playerOne);
                _currentlyPlayingPlayers.add(playerTwo);
                actions.add(createNewGame(playerOne, playerTwo));
            }

            if (!byeResults.isEmpty()) {
                actions.add(new BroadcastAction("Bye awarded to: "+ StringUtils.join(byeResults, ", ")));
            }

            for (String bye : byeResults) {
                _tournamentService.recordTournamentRoundBye(_tournamentId, bye, _tournamentInfo.Round);
                _playerByes.put(bye, _tournamentInfo.Round);
            }
        }
    }

    protected Map<String, Set<String>> getPreviouslyPairedPlayersMap() {
        Map<String, Set<String>> previouslyPaired = new HashMap<>();
        for (String player : _players)
            previouslyPaired.put(player, new HashSet<>());

        for (TournamentMatch finishedTournamentMatch : _finishedTournamentMatches) {
            previouslyPaired.get(finishedTournamentMatch.getWinner()).add(finishedTournamentMatch.getLoser());
            previouslyPaired.get(finishedTournamentMatch.getLoser()).add(finishedTournamentMatch.getWinner());
        }
        return previouslyPaired;
    }

    protected class PairPlayers implements TournamentTask {
        private final long _taskStart = System.currentTimeMillis() + PairingDelayTime.toMillis();

        @Override
        public void executeTask(List<TournamentProcessAction> actions, CollectionsManager collectionsManager) {
            doPairing(actions, collectionsManager);
        }

        @Override
        public long getExecuteAfter() {
            return _taskStart;
        }
    }

    protected class CreateMissingGames implements TournamentTask {
        private final Map<String, String> _gamesToCreate;

        public CreateMissingGames(Map<String, String> gamesToCreate) {
            _gamesToCreate = gamesToCreate;
        }

        @Override
        public void executeTask(List<TournamentProcessAction> actions, CollectionsManager collectionsManager) {
            for (Map.Entry<String, String> pairings : _gamesToCreate.entrySet()) {
                String playerOne = pairings.getKey();
                String playerTwo = pairings.getValue();
                actions.add(createNewGame(playerOne, playerTwo));
            }
        }

        @Override
        public long getExecuteAfter() {
            return 0;
        }
    }

    private Map.Entry<String, String> createEntry(String label, String url) {
        return new AbstractMap.SimpleEntry<>(label, url);
    }

    @Override
    public String produceReport(DeckRenderer renderer) throws CardNotFoundException {
        readLock.lock();
        try {
            if (_tournamentReport == null) {
                var tournamentStart = _tournamentInfo.StartTime;
                var tournamentEnd = _tournamentInfo.StartTime;

                var games = _tournamentService.getRecordedGames(getTournamentName());

                for (var match : _finishedTournamentMatches) {
                    var game = games.stream()
                            .filter((x) -> x.winner.equals(match.getWinner()) && x.loser.equals(match.getLoser()))
                            .findFirst()
                            .orElse(null);

                    if (game == null)
                        continue;

                    var gameStart = game.GetUTCStartDate();
                    var gameEnd = game.GetUTCEndDate();

                    if (tournamentStart == null || gameStart.isBefore(tournamentStart)) {
                        tournamentStart = gameStart;
                    }

                    if (tournamentEnd == null || gameEnd.isAfter(tournamentEnd)) {
                        tournamentEnd = gameEnd;
                    }
                }

                StringBuilder summary = new StringBuilder();
                summary
                        .append("<h1>").append(StringEscapeUtils.escapeHtml3(getTournamentName())).append("</h1>")
                        .append("<ul>")
                        .append("<li>Format: ").append(_tournamentInfo.Format).append("</li>")
                        .append("<li>Collection: ").append(_tournamentInfo.Collection.getFullName()).append("</li>")
                        .append("<li>Total Rounds: ").append(_tournamentInfo.Round).append("</li>")
                        .append("<li>Start: ").append(DateUtils.FormatDateTime(tournamentStart)).append("</li>")
                        .append("<li>End: ").append(DateUtils.FormatDateTime(tournamentEnd)).append("</li>")
                        .append("</ul><br/><br/><hr>");

                var sections = new ArrayList<String>();
                sections.add(summary.toString());

                for (var standing : getCurrentStandings()) {
                    var playerName = standing.playerName;

                    var rounds = new ArrayList<Map.Entry<String, String>>();

                    var playerRounds = _finishedTournamentMatches.stream()
                            .filter((x) -> x.getPlayerOne().equals(playerName) || x.getPlayerTwo().equals(playerName))
                            .toList();
                    for (int i = 1; i <= _tournamentInfo.Round; i++) {
                        if (_playerByes.containsKey(playerName) && _playerByes.get(playerName) == i) {
                            rounds.add(createEntry("[bye]", ""));
                            continue;
                        }

                        int currentRound = i;
                        var match = playerRounds.stream().filter(x -> x.getRound() == currentRound)
                                .findFirst().orElse(null);

                        if (match == null) {
                            rounds.add(createEntry("[dropped]", ""));
                            continue;
                        }

                        var game = games.stream().filter((x) -> x.winner.equals(match.getWinner()) && x.loser.equals(match.getLoser()))
                                .findFirst()
                                .orElse(null);
                        if (game == null)
                            continue;

                        String replayId = game.win_recording_id;
                        if (match.getLoser().equals(playerName)) {
                            replayId = game.lose_recording_id;
                        }

                        String label = "Round " + i;
                        String url = "https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=" +
                                playerName.replace("_", "%5F") + "$" + replayId;

                        rounds.add(createEntry(label, url));
                    }

                    LotroDeck deck = _tournamentService.retrievePlayerDeck(_tournamentId, playerName, _tournamentInfo.Format.getCode());

                    var fragment = renderer.convertDeckToForumFragment(deck, playerName, rounds);
                    sections.add(fragment);
                }

                _tournamentReport = renderer.AddDeckReadoutHeaderAndFooter(sections);
            }
        } finally {
            readLock.unlock();
        }
        return _tournamentReport;
    }
}
