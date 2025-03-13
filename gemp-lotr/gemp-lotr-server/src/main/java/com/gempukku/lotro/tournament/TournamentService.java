package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.timer.DraftTimerProducer;
import com.gempukku.lotro.draft3.TableDraftDefinition;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
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
    private final SoloDraftDefinitions _soloDraftLibrary;
    private final TableDraftDefinitions _tableDraftLibrary;

    private final CollectionsManager _collectionsManager;

    private static final Duration _tournamentRepeatPeriod = Duration.ofDays(2);
    private static final int _scheduledTournamentLoadTime = 7; // In days; one week

    private final Map<String, Tournament> _activeTournaments = new LinkedHashMap<>();
    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();

    public TournamentService(CollectionsManager collectionsManager, ProductLibrary productLibrary, DraftPackStorage draftPackStorage,
                             TournamentDAO tournamentDao, TournamentPlayerDAO tournamentPlayerDao, TournamentMatchDAO tournamentMatchDao,
                             GameHistoryDAO gameHistoryDao, LotroCardBlueprintLibrary bpLibrary, LotroFormatLibrary formatLibrary,
                             SoloDraftDefinitions soloDraftLibrary, TableDraftDefinitions tableDraftDefinitions) {
        _collectionsManager = collectionsManager;
        _productLibrary = productLibrary;
        _draftPackStorage = draftPackStorage;
        _tournamentDao = tournamentDao;
        _tournamentPlayerDao = tournamentPlayerDao;
        _tournamentMatchDao = tournamentMatchDao;
        _gameHistoryDao = gameHistoryDao;
        _blueprintLibrary = bpLibrary;
        _formatLibrary = formatLibrary;
        _soloDraftLibrary = soloDraftLibrary;
        _tableDraftLibrary = tableDraftDefinitions;
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

    private void addImmediateRecurringSealed(String queueId, String queueName, String prefix, String formatCode) {

        var sealedParams = new SealedTournamentParams();
        sealedParams.type = Tournament.TournamentType.SEALED;

        sealedParams.deckbuildingDuration = 25;
        sealedParams.turnInDuration = 5;

        var sealedFormat = _formatLibrary.GetSealedTemplate(formatCode);
        sealedParams.sealedFormatCode = formatCode;
        sealedParams.format = sealedFormat.GetFormat().getCode();
        sealedParams.requiresDeck = false;

        sealedParams.tournamentId = prefix;
        sealedParams.name = queueName;
        sealedParams.cost = 0;
        sealedParams.minimumPlayers = 2;
        sealedParams.playoff = Tournament.PairingType.SINGLE_ELIMINATION;
        sealedParams.prizes = Tournament.PrizeType.NONE;

        _tournamentQueues.put(queueId, new ImmediateRecurringQueue(this, queueId, queueName,
                new SealedTournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.Today(),
                        sealedParams))
        );
    }

    private void addImmediateRecurringDraft(String queueId, String queueName, String prefix, String formatCode) {

        var soloDraftParams = new SoloDraftTournamentParams();
        soloDraftParams.type = Tournament.TournamentType.SOLODRAFT;

        soloDraftParams.deckbuildingDuration = 25;
        soloDraftParams.turnInDuration = 5;

        var soloDraft = _soloDraftLibrary.getSoloDraft(formatCode);
        soloDraftParams.soloDraftFormatCode = formatCode;
        soloDraftParams.format = soloDraft.getFormat();
        soloDraftParams.requiresDeck = false;

        soloDraftParams.tournamentId = prefix;
        soloDraftParams.name = queueName;
        soloDraftParams.cost = 0;
        soloDraftParams.minimumPlayers = 2;
        soloDraftParams.playoff = Tournament.PairingType.SINGLE_ELIMINATION;
        soloDraftParams.prizes = Tournament.PrizeType.NONE;

        _tournamentQueues.put(queueId, new ImmediateRecurringQueue(this, queueId, queueName,
                new SoloDraftTournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.Today(),
                        soloDraftParams, _soloDraftLibrary))
        );
    }

    private void addImmediateRecurringSoloTableDraft(String queueId, String queueName, String prefix, String formatCode) {

        SoloTableDraftTournamentParams soloDraftParams = new SoloTableDraftTournamentParams();
        soloDraftParams.type = Tournament.TournamentType.TABLE_SOLODRAFT;

        soloDraftParams.deckbuildingDuration = 25;
        soloDraftParams.turnInDuration = 5;

        TableDraftDefinition tableDraft = _tableDraftLibrary.getTableDraftDefinition(formatCode);
        soloDraftParams.soloTableDraftFormatCode = formatCode;
        soloDraftParams.format = tableDraft.getFormat();
        soloDraftParams.requiresDeck = false;

        soloDraftParams.tournamentId = prefix;
        soloDraftParams.name = queueName;
        soloDraftParams.cost = 0;
        soloDraftParams.minimumPlayers = 2;
        soloDraftParams.playoff = Tournament.PairingType.SINGLE_ELIMINATION;
        soloDraftParams.prizes = Tournament.PrizeType.NONE;

        _tournamentQueues.put(queueId, new ImmediateRecurringQueue(this, queueId, queueName,
                new SoloTableDraftTournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.Today(),
                        soloDraftParams, _tableDraftLibrary))
        );
    }

    private void addImmediateRecurringTableDraft(String queueId, String queueName, String prefix, String formatCode, int players) {

        TableDraftTournamentParams draftParams = new TableDraftTournamentParams();
        draftParams.type = Tournament.TournamentType.TABLE_DRAFT;

        draftParams.deckbuildingDuration = 15;
        draftParams.turnInDuration = 2;
        draftParams.draftTimerProducerType = DraftTimerProducer.Type.CLASSIC;

        TableDraftDefinition tableDraft = _tableDraftLibrary.getTableDraftDefinition(formatCode);
        draftParams.tableDraftFormatCode = formatCode;
        draftParams.format = tableDraft.getFormat();
        draftParams.requiresDeck = false;

        draftParams.tournamentId = prefix;
        draftParams.name = queueName;
        draftParams.cost = 0;
        draftParams.minimumPlayers = players;
        draftParams.playoff = Tournament.PairingType.SWISS_3;
        draftParams.prizes = Tournament.PrizeType.NONE;

        _tournamentQueues.put(queueId, new ImmediateRecurringQueue(this, queueId, queueName,
                new TableDraftTournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.Today(),
                        draftParams, _tableDraftLibrary), true)
        );
    }

    private void addRecurringScheduledQueue(String queueId, String queueName, String time, String prefix, String formatCode) {
        _tournamentQueues.put(queueId, new RecurringScheduledQueue(this, queueId, queueName,
                new TournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.ParseStringDate(time),
                        new TournamentParams(prefix, queueName, formatCode, 0, 4, Tournament.PairingType.SWISS_3, Tournament.PrizeType.DAILY)), _tournamentRepeatPeriod));
    }

    private void addRecurringScheduledSealed(String queueId, String queueName, String time, String prefix, String formatCode) {

        var sealedParams = new SealedTournamentParams();
        sealedParams.type = Tournament.TournamentType.SEALED;

        sealedParams.deckbuildingDuration = 25;
        sealedParams.turnInDuration = 5;

        var sealedFormat = _formatLibrary.GetSealedTemplate(formatCode);
        sealedParams.sealedFormatCode = formatCode;
        sealedParams.format = sealedFormat.GetFormat().getCode();
        sealedParams.requiresDeck = false;

        sealedParams.tournamentId = prefix;
        sealedParams.name = queueName;
        sealedParams.cost = 0;
        sealedParams.minimumPlayers = 4;
        sealedParams.playoff = Tournament.PairingType.SWISS_3;
        sealedParams.prizes = Tournament.PrizeType.DAILY;

        _tournamentQueues.put(queueId, new RecurringScheduledQueue(this, queueId, queueName,
                new SealedTournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.ParseStringDate(time),
                        sealedParams), _tournamentRepeatPeriod)
        );
    }

    private void addRecurringScheduledDraft(String queueId, String queueName, String time, String prefix, String formatCode) {

        var soloDraftParams = new SoloDraftTournamentParams();
        soloDraftParams.type = Tournament.TournamentType.SOLODRAFT;

        soloDraftParams.deckbuildingDuration = 25;
        soloDraftParams.turnInDuration = 5;

        var soloDraft = _soloDraftLibrary.getSoloDraft(formatCode);
        soloDraftParams.soloDraftFormatCode = formatCode;
        soloDraftParams.format = soloDraft.getFormat();
        soloDraftParams.requiresDeck = false;

        soloDraftParams.tournamentId = prefix;
        soloDraftParams.name = queueName;
        soloDraftParams.cost = 0;
        soloDraftParams.minimumPlayers = 4;
        soloDraftParams.playoff = Tournament.PairingType.SWISS_3;
        soloDraftParams.prizes = Tournament.PrizeType.DAILY;

        _tournamentQueues.put(queueId, new RecurringScheduledQueue(this, queueId, queueName,
                new SoloDraftTournamentInfo(this, _productLibrary, _formatLibrary, DateUtils.ParseStringDate(time),
                        soloDraftParams, _soloDraftLibrary), _tournamentRepeatPeriod)
        );
    }

    public void reloadTournaments(TableHolder tables) {
        _tables = tables;
        clearCache();
        _tournamentQueues.clear();

        addImmediateRecurringLimitedGames();

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
            addRecurringScheduledQueue("fotr_daily_us", "Daily Rohan Fellowship Block", "2013-01-16 00:30:00", "fotrDailyUS-", "fotr_block");
            addRecurringScheduledQueue("movie_daily_eu", "Daily Gondor Movie Block", "2013-01-16 19:30:00", "movieDailyEu-", "movie");
            addRecurringScheduledQueue("movie_daily_us", "Daily Rohan Movie Block", "2013-01-17 00:30:00", "movieDailyUs-", "movie");

//            addRecurringScheduledDraft("fotr_draft_daily_eu", "Daily Gondor Fellowship Draft", "2013-01-16 19:30:00", "fotrDraftDailyEu-", "fotr_draft");
//            addRecurringScheduledDraft("fotr_draft_daily_us", "Daily Rohan Fellowship Draft", "2013-01-17 00:30:00", "fotrDraftDailyUS-", "fotr_draft");
//
//            addRecurringScheduledSealed("movie_sealed_daily_eu", "Daily Gondor Movie Sealed", "2013-01-15 19:30:00", "movieSealedDailyEu-", "single_movie_sealed");
//            addRecurringScheduledSealed("movie_sealed_daily_us", "Daily Rohan Movie Sealed", "2013-01-16 00:30:00", "movieSealedDailyUs-", "single_movie_sealed");

        } catch (DateTimeParseException exp) {
            // Ignore, can't happen
            System.out.println(exp);
        }

        getLiveTournaments();
    }

    private void addImmediateRecurringLimitedGames() {
        addImmediateRecurringTableDraft("fotr_mixed_table_draft_queue", "FotR Mixed Table Draft", "fotrMixedTableDraftQueue-", "fotr_mixed_table_draft", 6);
        addImmediateRecurringTableDraft("fotr_table_draft_queue", "FotR Table Draft", "fotrTableDraftQueue-", "fotr_table_draft", 6);
        addImmediateRecurringTableDraft("ttt_mixed_table_draft_queue", "TTT Mixed Table Draft", "tttMixedTableDraftQueue-", "ttt_mixed_table_draft", 6);
        addImmediateRecurringTableDraft("ttt_table_draft_queue", "TTT Table Draft", "tttTableDraftQueue-", "ttt_table_draft", 6);

        addImmediateRecurringSoloTableDraft("fotr_mixed_solo_table_draft_queue", "FotR Mixed Solo Table Draft", "fotrMixedSoloTableDraftQueue-", "fotr_mixed_table_draft");
        addImmediateRecurringSoloTableDraft("fotr_solo_table_draft_queue", "FotR Solo Table Draft", "fotrSoloTableDraftQueue-", "fotr_table_draft");
        addImmediateRecurringSoloTableDraft("ttt_mixed_solo_table_draft_queue", "TTT Mixed Solo Table Draft", "tttMixedSoloTableDraftQueue-", "ttt_mixed_table_draft");
        addImmediateRecurringSoloTableDraft("ttt_solo_table_draft_queue", "TTT Solo Table Draft", "tttSoloTableDraftQueue-", "ttt_table_draft");

        addImmediateRecurringDraft("fotr_draft_queue", "FotR Draft", "fotrDraftQueue-", "fotr_draft");
        addImmediateRecurringDraft("ttt_draft_queue", "TTT Draft", "tttDraftQueue-", "ttt_draft");
        addImmediateRecurringDraft("hobbit_draft_queue", "Hobbit Draft", "hobbitDraftQueue-", "hobbit_random_draft");

        addImmediateRecurringSealed("fotr_sealed_queue", "Fellowship Block Sealed", "fotrSealedQueue-", "single_fotr_block_sealed");
        addImmediateRecurringSealed("ttt_sealed_queue", "Towers Block Sealed", "tttSealedQueue-", "single_ttt_block_sealed");
        addImmediateRecurringSealed("ts_sealed_queue", "Towers Standard Sealed", "tsSealedQueue-", "single_ts_sealed");
        addImmediateRecurringSealed("king_sealed_queue", "King Block Sealed", "rotkSealedQueue-", "single_rotk_block_sealed");
        addImmediateRecurringSealed("movie_sealed_queue", "Movie Sealed", "movieSealedQueue-", "single_movie_sealed");
        addImmediateRecurringSealed("wotr_sealed_queue", "War of the Ring Block Sealed", "wotrSealedQueue-", "single_wotr_block_sealed");
        addImmediateRecurringSealed("th_sealed_queue", "Hunters Block Sealed", "thSealedQueue-", "single_th_block_sealed");
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
                    queue.getPlayerCount(), queue.getPlayerList(), queue.isPlayerSignedUp(player.getName()), queue.isJoinable(), queue.isStartable(player.getName()));
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
            return new ScheduledTournamentQueue(this, info.Parameters().tournamentId, info.Parameters().name, info);
        }
        else if(info.Parameters().type == Tournament.TournamentType.CONSTRUCTED) {
            return new ScheduledTournamentQueue(this, info.Parameters().tournamentId, info.Parameters().name, info);
        }
        else if(info.Parameters().type == Tournament.TournamentType.SOLODRAFT) {
            return new ScheduledTournamentQueue(this, info.Parameters().tournamentId, info.Parameters().name, info);
        }
        else if(info.Parameters().type == Tournament.TournamentType.TABLE_SOLODRAFT) {
            return new ScheduledTournamentQueue(this, info.Parameters().tournamentId, info.Parameters().name, info);
        }
        else if(info.Parameters().type == Tournament.TournamentType.TABLE_DRAFT) {
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
        else if(type == Tournament.TournamentType.SOLODRAFT) {
            return new ScheduledTournamentQueue(this, tourney.tournament_id, tourney.name,
                    new SoloDraftTournamentInfo(this, _productLibrary, _formatLibrary, tourney, _soloDraftLibrary));
        }
        else if(type == Tournament.TournamentType.TABLE_SOLODRAFT) {
            return new ScheduledTournamentQueue(this, tourney.tournament_id, tourney.name,
                    new SoloTableDraftTournamentInfo(this, _productLibrary, _formatLibrary, tourney, _tableDraftLibrary));
        }
        else if(type == Tournament.TournamentType.TABLE_DRAFT) {
            return new ScheduledTournamentQueue(this, tourney.tournament_id, tourney.name,
                    new TableDraftTournamentInfo(this, _productLibrary, _formatLibrary, tourney, _tableDraftLibrary));
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
                    tournament = new SealedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                }
                else if(info.Parameters().type == Tournament.TournamentType.CONSTRUCTED) {
                    tournament = new ConstructedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                } else if (info.Parameters().type == Tournament.TournamentType.SOLODRAFT) {
                    tournament = new SoloDraftTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                } else if (info.Parameters().type == Tournament.TournamentType.TABLE_SOLODRAFT) {
                    tournament = new SoloTableDraftTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                } else if (info.Parameters().type == Tournament.TournamentType.TABLE_DRAFT) {
                    tournament = new TableDraftTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                }

                _activeTournaments.put(tid, tournament);
            }
            else {
                tournament.RefreshTournamentInfo();
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
                    tournament = new SealedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                }
                else if(type == Tournament.TournamentType.CONSTRUCTED) {
                    tournament = new ConstructedTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                } else if (type == Tournament.TournamentType.SOLODRAFT) {
                    tournament = new SoloDraftTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                } else if (type == Tournament.TournamentType.TABLE_SOLODRAFT) {
                    tournament = new SoloTableDraftTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                } else if (type == Tournament.TournamentType.TABLE_DRAFT) {
                    tournament = new TableDraftTournament(this, _collectionsManager, _productLibrary, _formatLibrary, _soloDraftLibrary, _tableDraftLibrary, _tables, tid);
                }

                _activeTournaments.put(tid, tournament);
            }
            else {
                tournament.RefreshTournamentInfo();
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
