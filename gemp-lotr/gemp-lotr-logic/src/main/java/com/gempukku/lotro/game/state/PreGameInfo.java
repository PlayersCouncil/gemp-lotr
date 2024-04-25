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
        Map<String, String> maps) {

    public String perPlayerNotes(String playerId) {
        if(!notes.containsKey(playerId))
            return "";

        return notes.get(playerId);
    }

    public String getGameSummary() {
        var summary = tournamentName.split("-")[0] + " - a " + (privateGame ? "private" : "public") + " game of <b>"
                + format.getName() + "</b>. " + timingInfo + "<br/><br/>" + formatAddenda;

        return summary;
    }
}
