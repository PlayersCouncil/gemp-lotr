package com.gempukku.lotro.game.state;

import com.gempukku.lotro.game.LotroFormat;

import java.util.List;
import java.util.Map;

public record PreGameInfo (
        List<String> participants,
        String tournamentName,
        String timingInfo,
        boolean privateGame,
        LotroFormat format,
        //String tableDescription,
        String formatAddenda,
        Map<String, String> notes,
        Map<String, String> maps,
        GameExtraInfo extraInfo) {

    public String perPlayerNotes(String playerId) {
        if(!notes.containsKey(playerId))
            return "";

        return notes.get(playerId);
    }

    public String getGameSummary() {
        var leagueType = "";
        var metaSiteNote = "";
        if (extraInfo instanceof RTMDGameInfo) {
            leagueType = "Race to Mount Doom league ";
            metaSiteNote = " Each player has their own meta-site card based on their league performance, shown below.";
        }

        var summary = tournamentName.split("-")[0] + " - a " + (privateGame ? "private" : "public") + " " + leagueType
                + "game of <b>" + format.getName() + "</b>." + metaSiteNote + " " + timingInfo + "<br/><br/>" + formatAddenda;

        return summary;
    }
}
