package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstructedLeague implements LeagueData {
    private final LeaguePrizes _leaguePrizes;
    private final List<LeagueSerieInfo> _series = new ArrayList<>();
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;

    private final LeagueParams _parameters;

    public ConstructedLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, LeagueParams parameters) {
        _leaguePrizes = new FixedLeaguePrizes(productLibrary);
        _parameters = parameters;

        var serieStart = _parameters.GetUTCStart();
        int seriesCount = _parameters.series.size();

        int count = 1;
        for(var serie : _parameters.series) {
            _series.add(new DefaultLeagueSerieInfo(_leaguePrizes, false, "Serie " + count, serieStart,
                    serieStart.plusDays(serie.duration() - 1), serie.matches(), formatLibrary.getFormat(serie.format()),
                    CollectionType.ALL_CARDS));

            serieStart = serieStart.plusDays(serie.duration());
            count++;
        }

//        var autoPrizes = new EventAutoPrizes(CardCollection.Item.createItems(params[3]),
//                Integer.parseInt(params[4]),
//                CardCollection.Item.createItems(params[5]),
//                Integer.parseInt(params[6]));
//
//        _leaguePrizes = new IncentiveLeaguePrizes(productLibrary, autoPrizes);
    }

    public static ConstructedLeague fromRawParameters(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, String parameters) {

        if(parameters.contains("{")) {
            var parsedParams = JsonUtils.Convert(parameters, LeagueParams.class);
            if(parsedParams == null)
                throw new RuntimeException("Unable to parse raw parameters for Constructed league: " + parameters);
            return new ConstructedLeague(productLibrary, formatLibrary, parsedParams);
        }

        return new ConstructedLeague(productLibrary, formatLibrary, parameters);
    }

    //This is only here as a fallback for reloading old leagues which have not been converted to use Json parameters.

    private ConstructedLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(productLibrary);

        // Example of the stored db parameter string:
        // 20240726,default,0.7,1,1,pc_movie,3,6
        
        String[] params = parameters.split(",");
        var startDate = DateUtils.ParseDate(Integer.parseInt(params[0]));

        // "default" collection is ignored as all constructed leagues use all cards.
        // "0.7" is the prize multiplier, which was never fully implemented and has been excised.

        int repeatGames = Integer.parseInt(params[3]);
        int seriesCount = Integer.parseInt(params[4]);

        var serieStart = startDate;
        for (int i = 0; i < seriesCount; i++) {
            String format = params[5 + i * 3];
            int duration = Integer.parseInt(params[6 + i * 3]);
            int maxMatches = Integer.parseInt(params[7 + i * 3]);
            _series.add(new DefaultLeagueSerieInfo(_leaguePrizes, false, "Serie " + (i + 1),
                    serieStart, serieStart.plusDays(duration - 1),
                    maxMatches, formatLibrary.getFormat(format), CollectionType.ALL_CARDS));

            serieStart = serieStart.plusDays(duration);
        }

        _parameters = new LeagueParams() {{
            start = startDate.toLocalDateTime();
            maxRepeatMatches = repeatGames;
        }};
    }

    @Override
    public LeagueParams getParameters() { return _parameters; }

    @Override
    public int getMaxRepeatMatchesPerSerie() {
        return _parameters.maxRepeatMatches;
    }

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
    public CardCollection createLeagueCollection(CollectionsManager collecionsManager, Player player, ZonedDateTime currentTime) {
        return null;
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, ZonedDateTime currentTime) {
        int status = oldStatus;
        if (status == 0) {
            int maxGamesPlayed = 0;
            for (LeagueSerieInfo sery : _series) {
                maxGamesPlayed+=sery.getMaxMatches();
            }

            LeagueSerieInfo lastSerie = _series.getLast();
            if (DateUtils.IsAtLeastDayAfter(currentTime, lastSerie.getEnd())) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(leagueStanding.standing, leagueStandings.size(), leagueStanding.gamesPlayed, maxGamesPlayed);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league prizes", leagueStanding.playerName, _prizeCollectionType, leaguePrize.getAll());

                    final CardCollection leagueTrophies = _leaguePrizes.getTrophiesForLeague(leagueStanding, leagueStandings, maxGamesPlayed);
                    if (leagueTrophies != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league trophies", leagueStanding.playerName, _prizeCollectionType, leagueTrophies.getAll());
                }
                status++;
            }
        }

        return status;
    }
}
