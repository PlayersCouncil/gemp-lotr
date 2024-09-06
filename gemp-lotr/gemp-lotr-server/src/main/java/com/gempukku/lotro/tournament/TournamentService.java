package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.HallInfoVisitor;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.DraftPackStorage;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TournamentService {
    private final ProductLibrary _productLibrary;
    private final DraftPackStorage _draftPackStorage;
    private final TournamentDAO _tournamentDao;
    private final TournamentPlayerDAO _tournamentPlayerDao;
    private final TournamentMatchDAO _tournamentMatchDao;
    private final GameHistoryDAO _gameHistoryDao;
    private final LotroCardBlueprintLibrary _library;
    private TableHolder _tables;

    private final CollectionsManager _collectionsManager;

    private static final Duration _tournamentRepeatPeriod = Duration.ofDays(2);
    private static final int _scheduledTournamentLoadTime = 7; // In days; one week

    private final Map<String, Tournament> _activeTournaments = new LinkedHashMap<>();
    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();

    public TournamentService(CollectionsManager collectionsManager, ProductLibrary productLibrary, DraftPackStorage draftPackStorage,
                             TournamentDAO tournamentDao, TournamentPlayerDAO tournamentPlayerDao, TournamentMatchDAO tournamentMatchDao,
                             GameHistoryDAO gameHistoryDao, LotroCardBlueprintLibrary library) {
        _collectionsManager = collectionsManager;
        _productLibrary = productLibrary;
        _draftPackStorage = draftPackStorage;
        _tournamentDao = tournamentDao;
        _tournamentPlayerDao = tournamentPlayerDao;
        _tournamentMatchDao = tournamentMatchDao;
        _gameHistoryDao = gameHistoryDao;
        _library = library;
    }

    public PairingMechanism getPairingMechanism(String pairing) {
        return Tournament.getPairingMechanism(pairing);
    }

    public void reloadTournaments(TableHolder tables) {
        _tables = tables;
        clearCache();
        _tournamentQueues.clear();

        _tournamentQueues.put("fotr_queue", new ImmediateRecurringQueue("fotr_queue", 1500, "fotr_block",
                CollectionType.ALL_CARDS, "fotrQueue-", "Fellowship Block", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));
        _tournamentQueues.put("pc_fotr_queue", new ImmediateRecurringQueue("pc_fotr_queue", 1500, "pc_fotr_block",
                CollectionType.ALL_CARDS, "pcfotrQueue-", "PC-Fellowship", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));
        _tournamentQueues.put("ts_queue", new ImmediateRecurringQueue("ts_queue", 1500, "towers_standard",
                CollectionType.ALL_CARDS, "tsQueue-", "Towers Standard", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));
        _tournamentQueues.put("movie_queue", new ImmediateRecurringQueue("movie_queue", 1500, "movie",
                CollectionType.ALL_CARDS, "movieQueue-", "Movie Block", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));
        _tournamentQueues.put("pc_movie_queue", new ImmediateRecurringQueue("pc_movie_queue", 1500, "pc_movie",
                CollectionType.ALL_CARDS, "pcmovieQueue-", "PC-Movie", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));
        _tournamentQueues.put("expanded_queue", new ImmediateRecurringQueue("expanded_queue", 1500, "expanded",
                CollectionType.ALL_CARDS, "expandedQueue-", "Expanded", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));
        _tournamentQueues.put("pc_expanded_queue", new ImmediateRecurringQueue("pc_expanded_queue", 1500, "pc_expanded",
                CollectionType.ALL_CARDS, "pcexpandedQueue-", "PC-Expanded", 4,
                true, this, Tournament.getTournamentPrizes(_productLibrary, "onDemand"), Tournament.getPairingMechanism("singleElimination")));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            _tournamentQueues.put("fotr_daily_eu", new RecurringScheduledQueue("fotr_daily_eu", DateUtils.ParseStringDate("2013-01-15 19:30:00"), _tournamentRepeatPeriod, "fotrDailyEu-", "Daily Gondor Fellowship Block", 0,
                    true, CollectionType.ALL_CARDS, this, Tournament.getTournamentPrizes(_productLibrary, "daily"), Tournament.getPairingMechanism("swiss-3"),
                    "fotr_block", 4));
            _tournamentQueues.put("fotr_daily_us", new RecurringScheduledQueue("fotr_daily_us", DateUtils.ParseStringDate("2013-01-16 00:30:00"), _tournamentRepeatPeriod, "fotrDailyUs-", "Daily Rohan Fellowship Block", 0,
                    true, CollectionType.ALL_CARDS, this, Tournament.getTournamentPrizes(_productLibrary, "daily"), Tournament.getPairingMechanism("swiss-3"),
                    "fotr_block", 4));
            _tournamentQueues.put("movie_daily_eu", new RecurringScheduledQueue("movie_daily_eu", DateUtils.ParseStringDate("2013-01-16 19:30:00"), _tournamentRepeatPeriod, "movieDailyEu-", "Daily Gondor Movie Block", 0,
                    true, CollectionType.ALL_CARDS, this, Tournament.getTournamentPrizes(_productLibrary, "daily"), Tournament.getPairingMechanism("swiss-3"),
                    "movie", 4));
            _tournamentQueues.put("movie_daily_us", new RecurringScheduledQueue("movie_daily_us", DateUtils.ParseStringDate("2013-01-17 00:30:00"), _tournamentRepeatPeriod, "movieDailyUs-", "Daily Rohan Movie Block", 0,
                    true, CollectionType.ALL_CARDS, this, Tournament.getTournamentPrizes(_productLibrary, "daily"), Tournament.getPairingMechanism("swiss-3"),
                    "movie", 4));
        } catch (DateTimeParseException exp) {
            // Ignore, can't happen
            System.out.println(exp);
        }

        getLiveTournaments();
    }

    public void clearCache() {
        _activeTournaments.clear();
    }

    public void cancelAllTournamentQueues() throws SQLException, IOException {
        for (TournamentQueue tournamentQueue : _tournamentQueues.values())
            tournamentQueue.leaveAllPlayers(_collectionsManager);
    }

    public TournamentQueue getTournamentQueue(String queueId) {
        return _tournamentQueues.get(queueId);
    }

    public Collection<TournamentQueue> getAllTournamentQueues() {
        return _tournamentQueues.values();
    }

    public void processTournamentsForHall(LotroFormatLibrary formatLibrary, Player player, HallInfoVisitor visitor) {

        for (var entry : _tournamentQueues.entrySet()) {
            var queueID = entry.getKey();
            var queue = entry.getValue();
            visitor.visitTournamentQueue(queueID, queue.getCost(), queue.getCollectionType().getFullName(),
                    formatLibrary.getFormat(queue.getFormat()).getName(), queue.getTournamentQueueName(),
                    queue.getPrizesDescription(), queue.getPairingDescription(), queue.getStartCondition(),
                    queue.getPlayerCount(), queue.getPlayerList(), queue.isPlayerSignedUp(player.getName()), queue.isJoinable());
        }

        for (var entry : _activeTournaments.entrySet()) {
            var tourneyID = entry.getKey();
            var tournament = entry.getValue();
            visitor.visitTournament(tourneyID, tournament.getCollectionType().getFullName(),
                    formatLibrary.getFormat(tournament.getFormat()).getName(), tournament.getTournamentName(), tournament.getPlayOffSystem(),
                    tournament.getTournamentStage().getHumanReadable(),
                    tournament.getCurrentRound(), tournament.getPlayersInCompetitionCount(), tournament.getPlayerList(), tournament.isPlayerInCompetition(player.getName()));
        }

    }

    public boolean processTournamentQueues() throws SQLException, IOException {
        boolean queuesChanged = false;
        for (var entry : new HashMap<>(_tournamentQueues).entrySet()) {
            var queueID = entry.getKey();
            var queue = entry.getValue();
            var callback = new TournamentQueueCallback() {
                @Override
                public void createTournament(Tournament tournament) {
                    _activeTournaments.put(tournament.getTournamentId(), tournament);
                }
            };
            // If it's finished, remove it
            if (queue.process(callback, _collectionsManager)) {
                _tournamentQueues.remove(queueID);
                queuesChanged = true;
            }
        }

        return queuesChanged;
    }

    public boolean processTournaments(HallServer hall) {
        boolean tournamentsChanged = false;
        for (var entry : new HashMap<>(_activeTournaments).entrySet()) {
            var tourneyID = entry.getKey();
            var tourney = entry.getValue();

            TournamentCallback tournamentCallback = hall.getTournamentCallback(tourney);
            List<TournamentProcessAction> actions =  tourney.advanceTournament(_collectionsManager);
            tournamentsChanged |= !actions.isEmpty();
            for (TournamentProcessAction action : actions) {
                action.process(tournamentCallback);
            }
            if (tourney.getTournamentStage() == Tournament.Stage.FINISHED)
                _activeTournaments.remove(tourneyID);
        }

        return tournamentsChanged;
    }

    public boolean refreshQueues() {
        boolean queuesChanged = false;
        var unstartedTournamentQueues = retrieveUnstartedScheduledTournamentQueues(ZonedDateTime.now().plusDays(_scheduledTournamentLoadTime));
        for (var unstartedTourney : unstartedTournamentQueues) {
            String id = unstartedTourney.tournament_id;
            if (!_tournamentQueues.containsKey(id)) {
                var scheduledTourney = new ScheduledTournamentQueue(id, this, _productLibrary, unstartedTourney);
                _tournamentQueues.put(id, scheduledTourney);
                queuesChanged = true;
            }
        }

        return queuesChanged;
    }

    public DBDefs.Tournament retrieveTournamentData(String tournamentId) {
        return _tournamentDao.getTournament(tournamentId);
    }

    public void recordTournamentPlayer(String tournamentId, String playerName, LotroDeck deck) {
        _tournamentPlayerDao.addPlayer(tournamentId, playerName, deck);
    }

    public void recordPlayerTournamentAbandon(String tournamentId, String playerName) {
        _tournamentPlayerDao.dropPlayer(tournamentId, playerName);
    }

    public Set<String> retrieveTournamentPlayers(String tournamentId) {
        return _tournamentPlayerDao.getPlayers(tournamentId);
    }

    public Map<String, LotroDeck> retrievePlayerDecks(String tournamentId, String format) {
        return _tournamentPlayerDao.getPlayerDecks(tournamentId, format);
    }

    public Set<String> retrieveAbandonedPlayers(String tournamentId) {
        return _tournamentPlayerDao.getDroppedPlayers(tournamentId);
    }

    public LotroDeck retrievePlayerDeck(String tournamentId, String player, String format) {
        return _tournamentPlayerDao.getPlayerDeck(tournamentId, player, format);
    }

    public void recordMatchup(String tournamentId, int round, String playerOne, String playerTwo) {
        _tournamentMatchDao.addMatch(tournamentId, round, playerOne, playerTwo);
    }

    public void recordMatchupResult(String tournamentId, int round, String winner) {
        _tournamentMatchDao.setMatchResult(tournamentId, round, winner);
    }

    public void updateRecordedPlayerDeck(String tournamentId, String player, LotroDeck deck) {
        _tournamentPlayerDao.updatePlayerDeck(tournamentId, player, deck);
    }

    public List<TournamentMatch> retrieveMatchups(String tournamentId) {
        var dbMatches = _tournamentMatchDao.getMatches(tournamentId);
        var matches = new ArrayList<TournamentMatch>();
        for(var dbmatch : dbMatches) {
            matches.add(TournamentMatch.fromDB(dbmatch));
        }
        return matches;
    }

    public List<DBDefs.GameHistory> getRecordedGames(String tournamentName) {
        return _gameHistoryDao.getGamesForTournament(tournamentName);
    }

    public Tournament addTournament(TournamentInfo info) {
        _tournamentDao.addTournament(info.ToDB());
        return upsertTournamentInCache(info.tournamentId());
    }

    public boolean addScheduledTournament(DBDefs.ScheduledTournament info) {
        if (_tournamentQueues.containsKey(info.tournament_id))
            return false;

        _tournamentDao.addScheduledTournament(info);
        var scheduledTourney = new ScheduledTournamentQueue(info.tournament_id, this, _productLibrary, info);
        _tournamentQueues.put(info.tournament_id, scheduledTourney);

        return true;
    }

    public void recordTournamentStage(String tournamentId, Tournament.Stage stage) {
        _tournamentDao.updateTournamentStage(tournamentId, stage);
    }

    public void recordTournamentRound(String tournamentId, int round) {
        _tournamentDao.updateTournamentRound(tournamentId, round);
    }

    public List<Tournament> getOldTournaments(ZonedDateTime since) {
        List<Tournament> result = new ArrayList<>();
        for (var dbinfo : _tournamentDao.getFinishedTournamentsSince(since)) {
            var tournament = upsertTournamentInCache(dbinfo.tournament_id);
            result.add(tournament);
        }
        return result;
    }

    public List<Tournament> getLiveTournaments() {
        List<Tournament> result = new ArrayList<>();
        for (var dbinfo : _tournamentDao.getUnfinishedTournaments()) {
            var tournament = upsertTournamentInCache(dbinfo.tournament_id);
            result.add(tournament);
        }
        return result;
    }

    public Tournament getTournamentById(String tournamentId) {
        Tournament tournament = _activeTournaments.get(tournamentId);
        if (tournament == null) {
            var dbinfo = _tournamentDao.getTournamentById(tournamentId);
            if (dbinfo == null)
                return null;

            tournament = upsertTournamentInCache(dbinfo.tournament_id);
        }
        return tournament;
    }

    private Tournament upsertTournamentInCache(String tournamentId) {
        Tournament tournament;
        try {
            tournament = _activeTournaments.get(tournamentId);
            if (tournament == null) {
                tournament = new DefaultTournament(this, _collectionsManager, _productLibrary, _tables, tournamentId);
                _activeTournaments.put(tournamentId, tournament);
            }
            else {
                tournament.RefreshTournamentInfo();;
            }
        } catch (Exception exp) {
            throw new RuntimeException("Unable to create/update Tournament", exp);
        }

        return tournament;
    }

    public void recordTournamentRoundBye(String tournamentId, String player, int round) {
        _tournamentMatchDao.addBye(tournamentId, player, round);
    }

    public Map<String, Integer> retrieveTournamentByes(String tournamentId) {
        return _tournamentMatchDao.getPlayerByes(tournamentId);
    }

    public List<DBDefs.ScheduledTournament> retrieveUnstartedScheduledTournamentQueues(ZonedDateTime tillDate) {
        return _tournamentDao.getUnstartedScheduledTournamentQueues(tillDate);
    }

    public void recordScheduledTournamentStarted(String scheduledTournamentId) {
        _tournamentDao.updateScheduledTournamentStarted(scheduledTournamentId);
    }
}
