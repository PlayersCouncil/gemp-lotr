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
    private final LotroFormatLibrary _formatLibrary;
    private final DraftPackStorage _draftPackStorage;
    private final TournamentDAO _tournamentDao;
    private final TournamentPlayerDAO _tournamentPlayerDao;
    private final TournamentMatchDAO _tournamentMatchDao;
    private final GameHistoryDAO _gameHistoryDao;
    private final LotroCardBlueprintLibrary _blueprintLibrary;
    private TableHolder _tables;

    private final CollectionsManager _collectionsManager;

    private static final Duration _tournamentRepeatPeriod = Duration.ofDays(2);
    private static final int _scheduledTournamentLoadTime = 7; // In days; one week

    private final Map<String, Tournament> _activeTournaments = new LinkedHashMap<>();
    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();

    public TournamentService(CollectionsManager collectionsManager, ProductLibrary productLibrary, DraftPackStorage draftPackStorage,
                             TournamentDAO tournamentDao, TournamentPlayerDAO tournamentPlayerDao, TournamentMatchDAO tournamentMatchDao,
                             GameHistoryDAO gameHistoryDao, LotroCardBlueprintLibrary bpLibrary, LotroFormatLibrary formatLibrary) {
        _collectionsManager = collectionsManager;
        _productLibrary = productLibrary;
        _draftPackStorage = draftPackStorage;
        _tournamentDao = tournamentDao;
        _tournamentPlayerDao = tournamentPlayerDao;
        _tournamentMatchDao = tournamentMatchDao;
        _gameHistoryDao = gameHistoryDao;
        _blueprintLibrary = bpLibrary;
        _formatLibrary = formatLibrary;
    }

    public PairingMechanism getPairingMechanism(Tournament.PairingType pairing) {
        return Tournament.getPairingMechanism(pairing);
    }

    private void addImmediateRecurringQueue(String queueId, String queueName, String prefix, String formatCode) {
        _tournamentQueues.put(queueId, new ImmediateRecurringQueue(this, queueId, queueName,
                new TournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.Today(),
                        new TournamentParams(prefix, queueName, formatCode, 1500, 4, Tournament.PairingType.SINGLE_ELIMINATION, Tournament.PrizeType.ON_DEMAND)))
        );
    }

    private void addRecurringScheduledQueue(String queueId, String queueName, String time, String prefix, String formatCode) {
        _tournamentQueues.put(queueId, new RecurringScheduledQueue(this, queueId, queueName,
                new TournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.ParseStringDate(time),
                        new TournamentParams(prefix, queueName, formatCode, 0, 4, Tournament.PairingType.SWISS_3, Tournament.PrizeType.DAILY)), _tournamentRepeatPeriod, 4));
    }

    public void reloadTournaments(TableHolder tables) {
        _tables = tables;
        clearCache();
        _tournamentQueues.clear();

        addImmediateRecurringQueue("fotr_queue", "Fellowship Block", "fotrQueue-", "fotr_block");
        addImmediateRecurringQueue("pc_fotr_queue", "PC-Fellowship", "pcfotrQueue-", "pc_fotr_block");
        addImmediateRecurringQueue("ts_queue", "Towers Standard", "tsQueue-", "towers_standard");
        addImmediateRecurringQueue("movie_queue", "Movie Block", "movieQueue-", "movie");
        addImmediateRecurringQueue("pc_movie_queue", "PC-Movie", "pcmovieQueue-", "pc_movie");
        addImmediateRecurringQueue("expanded_queue", "Expanded", "expandedQueue-", "expanded");
        addImmediateRecurringQueue("pc_expanded_queue", "PC-Expanded", "pcexpandedQueue-", "pc_expanded");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            addRecurringScheduledQueue("fotr_daily_eu", "Daily Gondor Fellowship Block", "2013-01-15 19:30:00", "fotrDailyEu-", "fotr_block");
            addRecurringScheduledQueue("fotr_daily_us", "Daily Rohan Fellowship Block", "2013-01-16 00:30:00", "fotrDailyEu-", "fotr_block");
            addRecurringScheduledQueue("movie_daily_eu", "Daily Gondor Movie Block", "2013-01-16 19:30:00", "fotrDailyEu-", "movie");
            addRecurringScheduledQueue("movie_daily_us", "Daily Rohan Fellowship Block", "2013-01-17 00:30:00", "fotrDailyEu-", "movie");

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
                    formatLibrary.getFormat(queue.getFormatCode()).getName(), queue.getInfo().Parameters().type.toString(), queue.getTournamentQueueName(),
                    queue.getPrizesDescription(), queue.getPairingDescription(), queue.getStartCondition(),
                    queue.getPlayerCount(), queue.getPlayerList(), queue.isPlayerSignedUp(player.getName()), queue.isJoinable());
        }

        for (var entry : _activeTournaments.entrySet()) {
            var tourneyID = entry.getKey();
            var tournament = entry.getValue();
            visitor.visitTournament(tourneyID, tournament.getCollectionType().getFullName(),
                    formatLibrary.getFormat(tournament.getFormatCode()).getName(), tournament.getTournamentName(), tournament.getInfo().Parameters().type.toString(), tournament.getPlayOffSystem(),
                    tournament.getTournamentStage().getHumanReadable(),
                    tournament.getCurrentRound(), tournament.getPlayersInCompetitionCount(), tournament.getPlayerList(), tournament.isPlayerInCompetition(player.getName()), tournament.isPlayerAbandoned(player.getName()));
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
                var scheduledTourney = getTournamentQueue(unstartedTourney);
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
        return upsertTournamentInCache(info);
    }

    public boolean addScheduledTournament(TournamentInfo info) {
        if (_tournamentQueues.containsKey(info._params.tournamentId))
            return false;

        if(_tournamentDao.getScheduledTournament(info._params.tournamentId) != null)
            return false;

        _tournamentDao.addScheduledTournament(info.ToScheduledDB());
        var scheduledTourney = getTournamentQueue(info);
        _tournamentQueues.put(info._params.tournamentId, scheduledTourney);

        return true;
    }

    public TournamentQueue getTournamentQueue(TournamentInfo info) {
        if(info.Parameters().type == Tournament.TournamentType.SEALED) {
            return new ScheduledTournamentQueue(this, info.Parameters().tournamentId, info.Parameters().name, (SealedTournamentInfo) info);
        }
        else if(info.Parameters().type == Tournament.TournamentType.CONSTRUCTED) {
            return new ScheduledTournamentQueue(this, info.Parameters().tournamentId, info.Parameters().name, info);
        }

        return null;
    }

    public TournamentQueue getTournamentQueue(DBDefs.ScheduledTournament tourney) {
        var type = Tournament.TournamentType.parse(tourney.type);
        if(type == Tournament.TournamentType.SEALED) {
            return new ScheduledTournamentQueue(this, tourney.tournament_id, tourney.name,
                    new SealedTournamentInfo(this, _productLibrary, _formatLibrary, tourney));
        }
        else if(type == Tournament.TournamentType.CONSTRUCTED) {
            return new ScheduledTournamentQueue(this, tourney.tournament_id, tourney.name,
                    new TournamentInfo(this, _productLibrary, _formatLibrary, tourney));
        }

        return null;
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
            var tournament = upsertTournamentInCache(dbinfo);
            result.add(tournament);
        }
        return result;
    }

    public List<Tournament> getLiveTournaments() {
        List<Tournament> result = new ArrayList<>();
        for (var dbinfo : _tournamentDao.getUnfinishedTournaments()) {
            var tournament = upsertTournamentInCache(dbinfo);
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

            tournament = upsertTournamentInCache(dbinfo);
        }
        return tournament;
    }

    public synchronized CollectionType getCollectionTypeByCode(String collectionTypeCode) {
        for (var tourney : getLiveTournaments()) {
            var collection = tourney.getInfo().Collection;
            if(collection != null && collection.getCode().equals(collectionTypeCode))
                return collection;
        }
        return null;
    }

    public DBDefs.ScheduledTournament getScheduledTournamentById(String tournamentId) {
        try {
            return _tournamentDao.getScheduledTournament(tournamentId);
        }
        catch(NoSuchElementException ex) {
            return null;
        }
    }

    private Tournament upsertTournamentInCache(TournamentInfo info) {
        Tournament tournament;
        try {
            String tid = info.Parameters().tournamentId;
            tournament = _activeTournaments.get(tid);
            if (tournament == null) {
                if(info.Parameters().type == Tournament.TournamentType.SEALED) {
                    tournament = new SealedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _tables, tid);
                }
                else if(info.Parameters().type == Tournament.TournamentType.CONSTRUCTED) {
                    tournament = new ConstructedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _tables, tid);
                }

                _activeTournaments.put(tid, tournament);
            }
            else {
                tournament.RefreshTournamentInfo();;
            }
        } catch (Exception exp) {
            throw new RuntimeException("Unable to create/update Tournament", exp);
        }

        return tournament;
    }

    private Tournament upsertTournamentInCache(DBDefs.Tournament data) {
        Tournament tournament;
        try {
            String tid = data.tournament_id;
            var type = Tournament.TournamentType.parse(data.type);
            tournament = _activeTournaments.get(tid);
            if (tournament == null) {
                if(type == Tournament.TournamentType.SEALED) {
                    tournament = new SealedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _tables, tid);
                }
                else if(type == Tournament.TournamentType.CONSTRUCTED) {
                    tournament = new ConstructedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _tables, tid);
                }

                _activeTournaments.put(tid, tournament);
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
