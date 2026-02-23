package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.util.JsonUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;

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

    // RTMD-specific fields (null/ignored for non-RTMD leagues)
    public ArrayList<String> racePath;           // Ordered list of modifier blueprint IDs
    public boolean raceCumulative = false;        // If true, all sites 1..current are active
    public int raceIntensityFloor = 1;            // Min intensity for auto-generation pool
    public int raceIntensityCeiling = 10;         // Max intensity for auto-generation pool
    public RTMDLeague.AdvanceType raceAdvancementMode = RTMDLeague.AdvanceType.WIN;    // "win" or "points"
    public int raceAdvanceFactor = 1;      // How many wins or points to advance

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
