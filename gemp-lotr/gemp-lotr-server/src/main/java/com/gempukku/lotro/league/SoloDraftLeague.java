package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

public class SoloDraftLeague implements LeagueData {
    public static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;
    private final SoloDraft _draft;
    private final CollectionType _collectionType;
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final LeaguePrizes _leaguePrizes;
    private final LeagueSerieInfo _serie;

    private final LeagueParams _parameters;

    public SoloDraftLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, LeagueParams parameters) {
        _leaguePrizes = new FixedLeaguePrizes(productLibrary);
        _parameters = parameters;


        _draft = soloDraftDefinitions.getSoloDraft(_parameters.series.getFirst().format());
        var start = _parameters.GetUTCStart();
        int serieDuration = _parameters.series.getFirst().duration();
        int maxMatches = _parameters.series.getFirst().matches();

        _collectionType = new CollectionType(_parameters.code, _parameters.collectionName);

        //ttt_draft,20240807,12,10,1722989352076,Draft - Towers Block

        _serie = new DefaultLeagueSerieInfo(_leaguePrizes, true, "Serie 1",
                start, start.plusDays( serieDuration - 1), maxMatches,
                formatLibrary.getFormat(_draft.getFormat()), _collectionType);
    }

    public static SoloDraftLeague fromRawParameters(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary,  SoloDraftDefinitions soloDraftDefinitions, String parameters) {

        if(parameters.contains("{")) {
            var parsedParams = JsonUtils.Convert(parameters, LeagueParams.class);
            if(parsedParams == null)
                throw new RuntimeException("Unable to parse raw parameters for Solo Draft league: " + parameters);
            return new SoloDraftLeague(productLibrary, formatLibrary, soloDraftDefinitions, parsedParams);
        }

        return new SoloDraftLeague(productLibrary, formatLibrary, soloDraftDefinitions, parameters);
    }

    private SoloDraftLeague(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(productLibrary);

        String[] params = parameters.split(",");
        var draftDef = params[0];
        _draft = soloDraftDefinitions.getSoloDraft(draftDef);
        var startDate = DateUtils.ParseDate(Integer.parseInt(params[1]));
        int serieDuration = Integer.parseInt(params[2]);
        int maxMatches = Integer.parseInt(params[3]);

        _collectionType = new CollectionType(params[4], params[5]);

        //ttt_draft,20240807,12,10,1722989352076,Draft - Towers Block

        _serie = new DefaultLeagueSerieInfo(_leaguePrizes, true, "Serie 1",
                startDate, startDate.plusDays( serieDuration - 1), maxMatches,
                formatLibrary.getFormat(_draft.getFormat()), _collectionType);

        _parameters = new LeagueParams() {{
            start = startDate.toLocalDateTime();
            series = new ArrayList<>() {{
                add(new SerieData(draftDef, serieDuration, maxMatches));
            }};
        }};
    }
    @Override
    public LeagueParams getParameters() { return _parameters; }

    public CollectionType getCollectionType() {
        return _collectionType;
    }

    @Override
    public SoloDraft getSoloDraft() {
        return _draft;
    }

    @Override
    public boolean isSoloDraftLeague() {
        return true;
    }

    @Override
    public List<LeagueSerieInfo> getSeries() {
        return Collections.singletonList(_serie);
    }

    private long getSeed(Player player) {
        return _collectionType.getCode().hashCode() + player.getId() * HIGH_ENOUGH_PRIME_NUMBER;
    }

    @Override
    public CardCollection createLeagueCollection(CollectionsManager collectionsManager, Player player, ZonedDateTime currentTime) {
        MutableCardCollection startingCollection = new DefaultCardCollection();
        long seed = getSeed(player);

        CardCollection leagueProduct = _draft.initializeNewCollection(seed);

        for (CardCollection.Item serieCollectionItem : leagueProduct.getAll())
            startingCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());

        startingCollection.setExtraInformation(createExtraInformation(seed));
        collectionsManager.addPlayerCollection(false, "Sealed league product", player, _collectionType, startingCollection);
        return startingCollection;
    }

    private Map<String, Object> createExtraInformation(long seed) {
        Map<String, Object> extraInformation = new HashMap<>();
        extraInformation.put("finished", false);
        extraInformation.put("stage", 0);
        extraInformation.put("seed", seed);
        extraInformation.put("draftPool", _draft.initializeDraftPool(seed, _parameters.code));
        return extraInformation;
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, ZonedDateTime currentTime) {
        int status = oldStatus;

        if (status == 0) {
            if (DateUtils.IsAtLeastDayAfter(currentTime, _serie.getEnd())) {
                int maxGamesTotal = _serie.getMaxMatches();

                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(leagueStanding.standing, leagueStandings.size(), leagueStanding.gamesPlayed, maxGamesTotal);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league prizes", leagueStanding.playerName, _prizeCollectionType, leaguePrize.getAll());
                    final CardCollection leagueTrophies = _leaguePrizes.getTrophiesForLeague(leagueStanding, leagueStandings, maxGamesTotal);
                    if (leagueTrophies != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league trophies", leagueStanding.playerName, CollectionType.TROPHY, leagueTrophies.getAll());
                }
                status++;
            }
        }

        return status;
    }
}
