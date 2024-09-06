package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.BestOfOneStandingsProducer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.LeagueMatchDAO;
import com.gempukku.lotro.db.LeagueParticipationDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeagueService {
    private final LeagueDAO _leagueDao;
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final ProductLibrary _productLibrary;

    // Cached on this layer
    private final CachedLeagueMatchDAO _leagueMatchDao;
    private final CachedLeagueParticipationDAO _leagueParticipationDAO;

    private final CollectionsManager _collectionsManager;
    private final SoloDraftDefinitions _soloDraftDefinitions;

    private final Map<String, List<PlayerStanding>> _leagueStandings = new ConcurrentHashMap<>();
    private final Map<String, List<PlayerStanding>> _leagueSerieStandings = new ConcurrentHashMap<>();

    private ZonedDateTime _activeLeaguesLoadedDate;
    private List<League> _activeLeagues;

    public LeagueService(LeagueDAO leagueDao, LeagueMatchDAO leagueMatchDao,
                         LeagueParticipationDAO leagueParticipationDAO, CollectionsManager collectionsManager,
                         LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary, ProductLibrary productLibrary, SoloDraftDefinitions soloDraftDefinitions) {
        _leagueDao = leagueDao;
        _cardLibrary = library;
        _formatLibrary = formatLibrary;
        _productLibrary = productLibrary;
        _leagueMatchDao = new CachedLeagueMatchDAO(leagueMatchDao);
        _leagueParticipationDAO = new CachedLeagueParticipationDAO(leagueParticipationDAO);
        _collectionsManager = collectionsManager;
        _soloDraftDefinitions = soloDraftDefinitions;
    }

    public synchronized void clearCache() {
        _leagueSerieStandings.clear();
        _leagueStandings.clear();
        _activeLeaguesLoadedDate = DateUtils.MinDate();

        _leagueMatchDao.clearCache();
        _leagueParticipationDAO.clearCache();
    }

    private synchronized void ensureLoadedCurrentLeagues() {
        var currentDate = DateUtils.Today();

        if (_activeLeagues == null || !DateUtils.IsSameDay(currentDate, _activeLeaguesLoadedDate)) {
            _leagueMatchDao.clearCache();
            _leagueParticipationDAO.clearCache();

            try {
                _activeLeagues = _leagueDao.loadActiveLeagues(currentDate);
                _activeLeaguesLoadedDate = currentDate;
                processLoadedLeagues(currentDate);
            } catch (SQLException e) {
                throw new RuntimeException("Unable to load Leagues", e);
            }
        }
    }

    public synchronized List<League> getActiveLeagues() {
        if (_activeLeaguesLoadedDate != null && DateUtils.IsToday(_activeLeaguesLoadedDate))
            return Collections.unmodifiableList(_activeLeagues);
        else {
            ensureLoadedCurrentLeagues();
            return Collections.unmodifiableList(_activeLeagues);
        }
    }

    private void processLoadedLeagues(ZonedDateTime currentDate) {
        for (League activeLeague : _activeLeagues) {
            int oldStatus = activeLeague.getStatus();
            int newStatus = activeLeague.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions)
                    .process(_collectionsManager, getLeagueStandings(activeLeague), oldStatus, currentDate);
            if (newStatus != oldStatus)
                _leagueDao.setStatus(activeLeague, newStatus);
        }
    }

    public synchronized boolean isPlayerInLeague(League league, Player player) {
        return _leagueParticipationDAO.getUsersParticipating(league.getCodeStr()).contains(player.getName());
    }

    public synchronized boolean playerJoinsLeague(League league, Player player, String remoteAddr) throws SQLException, IOException {
        return playerJoinsLeague(league, player, remoteAddr, false, false);
    }

    public synchronized boolean playerJoinsLeague(League league, Player player, String remoteAddr, boolean skipCost, boolean addedByAdmin) throws SQLException, IOException {
        if (isPlayerInLeague(league, player))
            return false;
        if (league.inviteOnly() && !addedByAdmin)
            return false;
        int cost = league.getCost();

        if (skipCost || _collectionsManager.removeCurrencyFromPlayerCollection("Joining "+league.getName()+" league", player, CollectionType.MY_CARDS, cost)) {
            league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).createLeagueCollection(_collectionsManager, player, DateUtils.Now());
            _leagueParticipationDAO.userJoinsLeague(league.getCodeStr(), player, remoteAddr);
            _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));

            return true;
        } else {
            return false;
        }
    }

    public synchronized League getLeagueByType(String type) {
        for (League league : getActiveLeagues()) {
            if (league.getCodeStr().equals(type))
                return league;
        }
        return null;
    }

    public synchronized CollectionType getCollectionTypeByCode(String collectionTypeCode) {
        for (League league : getActiveLeagues()) {
            for (LeagueSerieInfo leagueSerieInfo : league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getSeries()) {
                CollectionType collectionType = leagueSerieInfo.getCollectionType();
                if (collectionType != null && collectionType.getCode().equals(collectionTypeCode))
                    return collectionType;
            }
        }
        return null;
    }

    public synchronized LeagueSerieInfo getCurrentLeagueSerie(League league) {
        var currentDate = DateUtils.Now();

        for (LeagueSerieInfo leagueSerieInfo : league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getSeries()) {
            if (currentDate.isAfter( leagueSerieInfo.getStart()) && currentDate.isBefore(leagueSerieInfo.getEnd()))
                return leagueSerieInfo;
        }

        return null;
    }

    public synchronized void reportLeagueGameResult(League league, LeagueSerieInfo serie, String winner, String loser) {
        _leagueMatchDao.addPlayedMatch(league.getCodeStr(), serie.getName(), winner, loser);

        _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));
        _leagueSerieStandings.remove(LeagueMapKeys.getLeagueSerieMapKey(league, serie));

        awardPrizesToPlayer(league, serie, winner, true);
        awardPrizesToPlayer(league, serie, loser, false);
    }

    private void awardPrizesToPlayer(League league, LeagueSerieInfo serie, String player, boolean winner) {
        int count = 0;
        Collection<LeagueMatchResult> playerMatchesPlayedOn = getPlayerMatchesInSerie(league, serie, player);
        for (LeagueMatchResult leagueMatch : playerMatchesPlayedOn) {
            if (leagueMatch.getWinner().equals(player))
                count++;
        }

        CardCollection prize;
        if (winner)
            prize = serie.getPrizeForLeagueMatchWinner(count, playerMatchesPlayedOn.size());
        else
            prize = serie.getPrizeForLeagueMatchLoser(count, playerMatchesPlayedOn.size());
        if (prize != null) {
            _collectionsManager.addItemsToPlayerCollection(true, "Prize for winning league game", player, CollectionType.MY_CARDS, prize.getAll());
        }


    }

    public synchronized Collection<LeagueMatchResult> getPlayerMatchesInSerie(League league, LeagueSerieInfo serie, String player) {
        final Collection<LeagueMatchResult> allMatches = _leagueMatchDao.getLeagueMatches(league.getCodeStr());
        Set<LeagueMatchResult> result = new HashSet<>();
        for (LeagueMatchResult match : allMatches) {
            if (match.getSerieName().equals(serie.getName()) && (match.getWinner().equals(player) || match.getLoser().equals(player)))
                result.add(match);
        }
        return result;
    }

    public synchronized List<PlayerStanding> getLeagueStandings(League league) {
        List<PlayerStanding> leagueStandings = _leagueStandings.get(LeagueMapKeys.getLeagueMapKey(league));
        if (leagueStandings == null) {
            synchronized (this) {
                leagueStandings = createLeagueStandings(league);
                _leagueStandings.put(LeagueMapKeys.getLeagueMapKey(league), leagueStandings);
            }
        }
        return leagueStandings;
    }

    public synchronized List<PlayerStanding> getLeagueSerieStandings(League league, LeagueSerieInfo leagueSerie) {
        List<PlayerStanding> serieStandings = _leagueSerieStandings.get(LeagueMapKeys.getLeagueSerieMapKey(league, leagueSerie));
        if (serieStandings == null) {
            synchronized (this) {
                serieStandings = createLeagueSerieStandings(league, leagueSerie);
                _leagueSerieStandings.put(LeagueMapKeys.getLeagueSerieMapKey(league, leagueSerie), serieStandings);
            }
        }
        return serieStandings;
    }

    private List<PlayerStanding> createLeagueSerieStandings(League league, LeagueSerieInfo leagueSerie) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getCodeStr());
        final Collection<LeagueMatchResult> matches = _leagueMatchDao.getLeagueMatches(league.getCodeStr());

        Set<LeagueMatchResult> matchesInSerie = new HashSet<>();
        for (LeagueMatchResult match : matches) {
            if (match.getSerieName().equals(leagueSerie.getName()))
                matchesInSerie.add(match);
        }

        return createStandingsForMatchesAndPoints(playersParticipating, matchesInSerie);
    }

    private List<PlayerStanding> createLeagueStandings(League league) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getCodeStr());
        final Collection<LeagueMatchResult> matches = _leagueMatchDao.getLeagueMatches(league.getCodeStr());

        return createStandingsForMatchesAndPoints(playersParticipating, matches);
    }

    private List<PlayerStanding> createStandingsForMatchesAndPoints(Collection<String> playersParticipating, Collection<LeagueMatchResult> matches) {
        return BestOfOneStandingsProducer.produceStandings(playersParticipating, matches, 2, 1, Collections.emptyMap());
    }

    public synchronized boolean canPlayRankedGame(League league, LeagueSerieInfo season, String player) {
        int maxMatches = season.getMaxMatches();
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSerie(league, season, player);
        if (playedInSeason.size() >= maxMatches)
            return false;
        return true;
    }

    public synchronized boolean canPlayRankedGameAgainst(League league, LeagueSerieInfo season, String playerOne, String playerTwo) {
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSerie(league, season, playerOne);
        int maxGames = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions)
                .getMaxRepeatMatchesPerSerie();
        int totalGames = 0;
        for (LeagueMatchResult leagueMatch : playedInSeason) {
            if (playerTwo.equals(leagueMatch.getWinner()) || playerTwo.equals(leagueMatch.getLoser()))
                totalGames++;
        }
        return totalGames < maxGames;
    }
}
