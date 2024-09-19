package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

public class SealedLeague implements LeagueData {
    private final List<LeagueSerieInfo> _series;
    private final CollectionType _collectionType;

    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final LeaguePrizes _leaguePrizes;
    private final LotroFormatLibrary _formatLibrary;

    private final LeagueParams _parameters;

    public SealedLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, LeagueParams parameters) {
        _leaguePrizes = new FixedLeaguePrizes(productLibrary);
        _formatLibrary = formatLibrary;
        _parameters = parameters;

        _collectionType = new CollectionType(_parameters.code, parameters.collectionName);

        var def = _formatLibrary.GetSealedTemplate(_parameters.series.getFirst().format());
        var start = _parameters.GetUTCStart();
        var serieDuration = _parameters.series.getFirst().duration();
        var maxMatches = _parameters.series.getFirst().matches();

        _series = new LinkedList<>();
        for (int i = 0; i < def.GetSerieCount(); i++) {
            _series.add(
                    new DefaultLeagueSerieInfo(_leaguePrizes, true, "Serie " + (i + 1),
                            start.plusDays(i * serieDuration), start.plusDays ((i + 1) * serieDuration - 1), maxMatches,
                            def.GetFormat(), _collectionType));
        }
    }
    public static SealedLeague fromRawParameters(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, String parameters) {

        if(parameters.contains("{")) {
            var parsedParams = JsonUtils.Convert(parameters, LeagueParams.class);
            if(parsedParams == null)
                throw new RuntimeException("Unable to parse raw parameters for Sealed league: " + parameters);

            return new SealedLeague(productLibrary, formatLibrary, parsedParams);
        }

        return new SealedLeague(productLibrary, formatLibrary, parameters);
    }

    private SealedLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(productLibrary);
        _formatLibrary = formatLibrary;
        
        String[] params = parameters.split(",");
        var leagueTemplateName = params[0];
        var startDate = DateUtils.ParseDate(Integer.parseInt(params[1]));
        int serieDuration = Integer.parseInt(params[2]);
        int maxMatches = Integer.parseInt(params[3]);

        _collectionType = new CollectionType(params[4], params[5]);

        var def = _formatLibrary.GetSealedTemplate(leagueTemplateName);

        _series = new LinkedList<>();
        for (int i = 0; i < def.GetSerieCount(); i++) {
            _series.add(
                    new DefaultLeagueSerieInfo(_leaguePrizes, true, "Serie " + (i + 1),
                            startDate.plusDays(i * serieDuration), startDate.plusDays ((i + 1) * serieDuration - 1), maxMatches,
                            def.GetFormat(), _collectionType));
        }

        _parameters = new LeagueParams() {{
            start = startDate.toLocalDateTime();
            series = new ArrayList<>() {{
                add(new SerieData(leagueTemplateName, serieDuration, maxMatches));
            }};
        }};
    }

    @Override
    public LeagueParams getParameters() { return _parameters; }

    @Override
    public boolean isSoloDraftLeague() {
        return false;
    }

    @Override
    public SoloDraft getSoloDraft() {
        return null;
    }

    @Override
    public List<LeagueSerieInfo> getSeries() {
        return Collections.unmodifiableList(_series);
    }

    @Override
    public CardCollection createLeagueCollection(CollectionsManager collectionManager, Player player, ZonedDateTime currentTime) {
        var startingCollection = new DefaultCardCollection();
        for (int i = 0; i < _series.size(); i++) {
            LeagueSerieInfo serie = _series.get(i);
            if (DateUtils.IsAfterStart(currentTime, serie.getStart())) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_parameters.series.getFirst().format());
                var leagueProduct = sealedLeague.GetProductForSerie(i);

                for (CardCollection.Item serieCollectionItem : leagueProduct)
                    startingCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());
            }
        }
        collectionManager.addPlayerCollection(true, "Sealed league product", player, _collectionType, startingCollection);
        return startingCollection;
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, ZonedDateTime currentTime) {
        int status = oldStatus;

        for (int i = status; i < _series.size(); i++) {
            LeagueSerieInfo serie = _series.get(i);
            if (DateUtils.IsAfterStart(currentTime, serie.getStart())) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_parameters.series.getFirst().format());
                var leagueProduct = sealedLeague.GetProductForSerie(i);
                Map<Player, CardCollection> map = collectionsManager.getPlayersCollection(_collectionType.getCode());
                for (Map.Entry<Player, CardCollection> playerCardCollectionEntry : map.entrySet()) {
                    collectionsManager.addItemsToPlayerCollection(true, "New sealed league product", playerCardCollectionEntry.getKey(), _collectionType, leagueProduct);
                }
                status = i + 1;
            }
        }

        int maxGamesTotal = 0;
        for (LeagueSerieInfo sery : _series)
            maxGamesTotal+=sery.getMaxMatches();

        if (status == _series.size()) {
            LeagueSerieInfo lastSerie = _series.getLast();
            if (DateUtils.IsAtLeastDayAfter(currentTime, lastSerie.getEnd())) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(leagueStanding.standing, leagueStandings.size(), leagueStanding.gamesPlayed, maxGamesTotal);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league prizes", leagueStanding.playerName, _prizeCollectionType, leaguePrize.getAll());
                    final CardCollection leagueTrophies = _leaguePrizes.getTrophiesForLeague(leagueStanding, leagueStandings, maxGamesTotal);
                    if (leagueTrophies != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league trophies", leagueStanding.playerName, _prizeCollectionType, leagueTrophies.getAll());
                }
                status++;
            }
        }

        return status;
    }
}