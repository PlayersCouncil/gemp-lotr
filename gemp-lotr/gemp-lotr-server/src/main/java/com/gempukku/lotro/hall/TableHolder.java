package com.gempukku.lotro.hall;

import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroGameParticipant;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.tournament.TournamentService;

import java.util.*;

public class TableHolder {
    private final LeagueService leagueService;
    private final TournamentService tournamentService;
    private final IgnoreDAO ignoreDAO;

    private final Map<String, GameTable> awaitingTables = new LinkedHashMap<>();
    private final Map<String, GameTable> runningTables = new LinkedHashMap<>();

    private int _nextTableId = 1;

    public TableHolder(LeagueService leagueService, TournamentService tournamentService, IgnoreDAO ignoreDAO) {
        this.leagueService = leagueService;
        this.tournamentService = tournamentService;
        this.ignoreDAO = ignoreDAO;
    }

    public int getTableCount() {
        return runningTables.size();
    }

    public void cancelWaitingTables() {
        awaitingTables.clear();
    }

    public GameTable createTable(Player player, GameSettings gameSettings, LotroDeck lotroDeck) throws HallException {
        String tableId = String.valueOf(_nextTableId++);

        final League league = gameSettings.league();
        if (league != null) {
            verifyNotPlayingLeagueGame(player, league);

            if (!leagueService.isPlayerInLeague(league, player))
                throw new HallException("You're not in that league and somehow were able to get this message.  Please report a bug.");

            if (!leagueService.canPlayRankedGame(league, gameSettings.leagueSerie(), player.getName()))
                throw new HallException("You have already played max games in league");
        }

        GameTable table = new GameTable(gameSettings);

        boolean tableFull = table.addPlayer(new LotroGameParticipant(player.getName(), lotroDeck));
        if (tableFull) {
            runningTables.put(tableId, table);
            return table;
        }

        awaitingTables.put(tableId, table);
        return null;
    }

    public GameTable joinTable(String tableId, Player player, LotroDeck lotroDeck) throws HallException {
        final GameTable awaitingTable = awaitingTables.get(tableId);

        if (awaitingTable == null || awaitingTable.wasGameStarted())
            throw new HallException("Table is already taken or was removed");

        if (awaitingTable.hasPlayer(player.getName()))
            throw new HallException("You can't play against yourself");

        final League league = awaitingTable.getGameSettings().league();
        if (league != null) {
            verifyNotPlayingLeagueGame(player, league);

            if (!leagueService.isPlayerInLeague(league, player))
                throw new HallException("You're not registered for this league; go to the Events tab to sign up.");

            LeagueSerieInfo leagueSerie = awaitingTable.getGameSettings().leagueSerie();
            if (!leagueService.canPlayRankedGame(league, leagueSerie, player.getName()))
                throw new HallException("You have already played max games for this league: " + leagueSerie.getMaxMatches());
            if (!awaitingTable.getPlayerNames().isEmpty() && !leagueService.canPlayRankedGameAgainst(league, leagueSerie, awaitingTable.getPlayerNames().iterator().next(), player.getName()))
                throw new HallException("You have already played max matches against this player for this league series");
        }

        final boolean tableFull = awaitingTable.addPlayer(new LotroGameParticipant(player.getName(), lotroDeck));
        if (tableFull) {
            awaitingTables.remove(tableId);
            runningTables.put(tableId, awaitingTable);

            // Leave all other tables this player is waiting on
            for (LotroGameParticipant awaitingTablePlayer : awaitingTable.getPlayers())
                leaveAwaitingTablesForPlayer(awaitingTablePlayer.getPlayerId());

            return awaitingTable;
        }
        return null;
    }

    public GameTable setupTournamentTable(GameSettings gameSettings, LotroGameParticipant[] participants) {
        String tableId = String.valueOf(_nextTableId++);

        GameTable table = new GameTable(gameSettings);
        for (LotroGameParticipant participant : participants) {
            table.addPlayer(participant);
        }
        runningTables.put(tableId, table);

        return table;
    }

    public GameSettings getGameSettings(String tableId) throws HallException {
        final GameTable gameTable = awaitingTables.get(tableId);
        if (gameTable != null)
            return gameTable.getGameSettings();
        GameTable runningTable = runningTables.get(tableId);
        if (runningTable != null)
            return runningTable.getGameSettings();
        throw new HallException("Table was already removed");
    }

    public boolean leaveAwaitingTable(Player player, String tableId) {
        GameTable table = awaitingTables.get(tableId);
        if (table != null && table.hasPlayer(player.getName())) {
            boolean empty = table.removePlayer(player.getName());
            if (empty)
                awaitingTables.remove(tableId);
            return true;
        }
        return false;
    }

    public boolean leaveAwaitingTablesForPlayer(Player player) {
        return leaveAwaitingTablesForPlayer(player.getName());
    }

    private boolean leaveAwaitingTablesForPlayer(String playerId) {
        boolean result = false;
        final Iterator<Map.Entry<String, GameTable>> iterator = awaitingTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> table = iterator.next();
            if (table.getValue().hasPlayer(playerId)) {
                boolean empty = table.getValue().removePlayer(playerId);
                if (empty)
                    iterator.remove();
                result = true;
            }
        }
        return result;
    }

    private void verifyNotPlayingLeagueGame(Player player, League league) throws HallException {
        for (GameTable awaitingTable : awaitingTables.values()) {
            if (league.equals(awaitingTable.getGameSettings().league())
                    && awaitingTable.hasPlayer(player.getName())) {
                throw new HallException("You can't play in multiple league games at the same time");
            }
        }

        for (GameTable runningTable : runningTables.values()) {
            if (league.equals(runningTable.getGameSettings().league())) {
                LotroGameMediator game = runningTable.getLotroGameMediator();
                if (game != null && !game.isFinished() && game.getPlayersPlaying().contains(player.getName()))
                    throw new HallException("You can't play in multiple league games at the same time");
            }
        }
    }

    public void processTables(boolean isAdmin, Player player, HallInfoVisitor visitor) {
        // First waiting
        for (Map.Entry<String, GameTable> tableInformation : awaitingTables.entrySet()) {
            final GameTable table = tableInformation.getValue();

            List<String> players;
            if (table.getGameSettings().league() != null) {
                players = Collections.emptyList();
            }
            else {
                players = table.getPlayerNames();
            }

            if (isAdmin || isNoIgnores(players, player.getName())) {
                visitor.visitTable(tableInformation.getKey(), null, false, HallInfoVisitor.TableStatus.WAITING,
                        "Waiting", table.getGameSettings().format().getName(), getTournamentName(table),
                        table.getGameSettings().userDescription(), players,
                        table.getPlayerNames().contains(player.getName()), table.getGameSettings().privateGame(),
                        table.getGameSettings().isInviteOnly(), null);
            }
        }

        // Then non-finished
        Map<String, GameTable> finishedTables = new HashMap<>();

        for (Map.Entry<String, GameTable> runningGame : runningTables.entrySet()) {
            final GameTable runningTable = runningGame.getValue();
            LotroGameMediator lotroGameMediator = runningTable.getLotroGameMediator();
            if (lotroGameMediator != null) {
                if (isAdmin || (lotroGameMediator.isVisibleToUser(player.getName()) &&
                        isNoIgnores(lotroGameMediator.getPlayersPlaying(), player.getName()))) {
                    if (lotroGameMediator.isFinished())
                    {
                        finishedTables.put(runningGame.getKey(), runningTable);
                    }
                    else {
                        visitor.visitTable(runningGame.getKey(), lotroGameMediator.getGameId(),
                                isAdmin || lotroGameMediator.isAllowSpectators(), HallInfoVisitor.TableStatus.PLAYING,
                                lotroGameMediator.getGameStatus(), runningTable.getGameSettings().format().getName(),
                                getTournamentName(runningTable), runningTable.getGameSettings().userDescription(),
                                lotroGameMediator.getPlayersPlaying(),
                                lotroGameMediator.getPlayersPlaying().contains(player.getName()),
                                runningTable.getGameSettings().privateGame(),
                                runningTable.getGameSettings().isInviteOnly(), lotroGameMediator.getWinner());
                    }

                    if (!lotroGameMediator.isFinished() && lotroGameMediator.getPlayersPlaying().contains(player.getName()))
                        visitor.runningPlayerGame(lotroGameMediator.getGameId());
                }
            }
        }

        // Then rest
        for (Map.Entry<String, GameTable> nonPlayingGame : finishedTables.entrySet()) {
            final GameTable runningTable = nonPlayingGame.getValue();
            LotroGameMediator lotroGameMediator = runningTable.getLotroGameMediator();
            if (lotroGameMediator != null) {
                if (isAdmin || isNoIgnores(lotroGameMediator.getPlayersPlaying(), player.getName()))
                    visitor.visitTable(nonPlayingGame.getKey(), lotroGameMediator.getGameId(), false, HallInfoVisitor.TableStatus.FINISHED, lotroGameMediator.getGameStatus(), runningTable.getGameSettings().format().getName(), getTournamentName(runningTable), runningTable.getGameSettings().userDescription(), lotroGameMediator.getPlayersPlaying(), lotroGameMediator.getPlayersPlaying().contains(player.getName()), runningTable.getGameSettings().privateGame(),  runningTable.getGameSettings().isInviteOnly(), lotroGameMediator.getWinner());
            }
        }
    }

    public boolean removeFinishedGames() {
        boolean changed = false;
        final Iterator<Map.Entry<String, GameTable>> iterator = runningTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> runningTable = iterator.next();
            LotroGameMediator lotroGameMediator = runningTable.getValue().getLotroGameMediator();
            if (lotroGameMediator == null || lotroGameMediator.isDestroyed()) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    private boolean isNoIgnores(Collection<String> participants, String playerLooking) {
        // Do not ignore your own stuff
        if (participants.contains(playerLooking))
            return true;

        // This player ignores someone of the participants
        final Set<String> ignoredUsers = ignoreDAO.getIgnoredUsers(playerLooking);
        if (!Collections.disjoint(ignoredUsers, participants))
            return false;

        // One of the participants ignores this player
        for (String player : participants) {
            final Set<String> ignored = ignoreDAO.getIgnoredUsers(player);
            if (ignored.contains(playerLooking))
                return false;
        }

        return true;
    }

    private String getTournamentName(GameTable table) {
        final League league = table.getGameSettings().league();
        if (league != null) {
            return league.getName() + " - " + table.getGameSettings().leagueSerie().getName();
        } else if (table.getGameSettings().tournamentId() != null) {
            final String tournamentTableDescription = tournamentService.getActiveTournamentTableDescription(table.getGameSettings().tournamentId());
            if (tournamentTableDescription != null) {
                return tournamentTableDescription;
            }
        }

        return "Casual - " + table.getGameSettings().timeSettings().name();
    }

    public List<GameTable> getTournamentTables(String tournamentId) {
        var tables = new ArrayList<GameTable>();
        for(var table : runningTables.values()) {
            String tid = table.getGameSettings().tournamentId();
            if(tid != null && tid.equals(tournamentId)) {
                tables.add(table);
            }
        }

        return tables;
    }
}
