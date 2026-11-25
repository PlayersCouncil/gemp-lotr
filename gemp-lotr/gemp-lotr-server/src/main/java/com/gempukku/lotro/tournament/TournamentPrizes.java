package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;

public interface TournamentPrizes {
    CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints);
    CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints);
    String getRegistryRepresentation();

    /** What the prizes are, as a label and plain-text lines: the source of {@link #getPrizeDescription()}. */
    PrizeSummary getPrizeSummary();

    /** The prizes as the HTML snippet (or plain label) the legacy hall shows; derived from {@link #getPrizeSummary()}. */
    default String getPrizeDescription() {
        return getPrizeSummary().toLegacyHtml();
    }
}
