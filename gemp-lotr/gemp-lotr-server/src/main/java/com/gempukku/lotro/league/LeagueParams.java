package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.prizes.PrizeTier;
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
    /**
     * Tags leagues that belong together ("Yuletide 2026") so that campaign-scoped participation prizes can count
     * games across all of them.  Null for a stand-alone league.
     */
    public String campaign;
    /**
     * Prize tiers awarded when the league ends, in addition to the automatic {@link FixedLeaguePrizes}.
     */
    public ArrayList<PrizeTier> prizeTiers = new ArrayList<>();

    // RTMD-specific fields (null/ignored for non-RTMD leagues)
    public ArrayList<String> racePath;           // Ordered list of modifier blueprint IDs
    public ArrayList<String> raceVisualPath = new ArrayList<>();  // Ordered list of visual position card blueprint IDs (set 90), parallel to racePath
    public boolean raceCumulative = false;        // If true, all sites 1..current are active
    public int raceIntensityFloor = 1;            // Min intensity for auto-generation pool
    public int raceIntensityCeiling = 10;         // Max intensity for auto-generation pool
    public RTMDLeague.AdvanceType raceAdvancementMode = RTMDLeague.AdvanceType.WIN;    // "win" or "points"
    public int raceAdvanceFactor = 1;      // How many wins or points to advance

    public record SerieData(String format, int duration, int matches) {
    }

    public ZonedDateTime GetUTCStart() {
        return DateUtils.ParseDate(start);
    }

    @Override
    public String toString() {
        return JsonUtils.Serialize(this);
    }
}
