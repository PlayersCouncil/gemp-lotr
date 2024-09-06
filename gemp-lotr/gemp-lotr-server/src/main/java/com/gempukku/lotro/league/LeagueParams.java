package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.util.JsonUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeagueParams {

    public String name;
    public long code;
    public LocalDateTime start;
    public int cost;
    public String collectionName = "default";
    public boolean inviteOnly = false;
    public int maxRepeatMatches = 1;
    public String description;
    public ArrayList<SerieData> series = new ArrayList<>();
    public PrizeData extraPrizes;

    public record SerieData(String format, int duration, int matches) {
    }

    public record PrizeData(String topPrize, int topCutoff, String participationPrize, int participationGames) {

    }

    public ZonedDateTime GetUTCStart() {
        return DateUtils.ParseDate(start);
    }

    @Override
    public String toString() {
        return JsonUtils.Serialize(this);
    }
}

//
//String parameters = start + "," + collectionType + "," + maxRepeatMatches
//        + "," + topPrizeStr + "," + topCutoff + "," + participationPrizeStr + "," + participationGames
//        + "," + formats.size();
//        for (int i = 0; i < formats.size(); i++) {
//parameters += "," + formats.get(i) + "," + serieDurations.get(i) + "," + maxMatches.get(i);
//        }