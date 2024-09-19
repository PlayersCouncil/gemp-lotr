package com.gempukku.lotro.competitive;

import com.gempukku.util.DescComparator;
import com.gempukku.util.MultipleComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BestOfOneStandingsProducer {
    private static final Comparator<PlayerStanding> LEAGUE_STANDING_COMPARATOR =
            new MultipleComparator<>(
                    new DescComparator<>(new PointsComparator()),
                    new GamesPlayedComparator(),
                    new DescComparator<>(new OpponentsWinComparator()));


    public static List<PlayerStanding> produceStandings(Collection<String> participants, Collection<? extends CompetitiveMatchResult> matches,
                                                        int pointsForWin, int pointsForLoss, Map<String, Integer> playersWithByes) {
        Map<String, List<String>> playerOpponents = new HashMap<>();
        Map<String, AtomicInteger> playerWinCounts = new HashMap<>();
        Map<String, AtomicInteger> playerLossCounts = new HashMap<>();

        // Initialize the list
        for (String playerName : participants) {
            playerOpponents.put(playerName, new ArrayList<>());
            playerWinCounts.put(playerName, new AtomicInteger(0));
            playerLossCounts.put(playerName, new AtomicInteger(0));
        }

        for (CompetitiveMatchResult leagueMatch : matches) {
            playerOpponents.get(leagueMatch.getWinner()).add(leagueMatch.getLoser());
            playerOpponents.get(leagueMatch.getLoser()).add(leagueMatch.getWinner());
            playerWinCounts.get(leagueMatch.getWinner()).incrementAndGet();
            playerLossCounts.get(leagueMatch.getLoser()).incrementAndGet();
        }

        var standings = new HashMap<String, PlayerStanding>();
        for (String playerName : participants) {
            int playerWins = playerWinCounts.get(playerName).intValue();
            int playerLosses = playerLossCounts.get(playerName).intValue();
            int points = playerWins * pointsForWin + playerLosses * pointsForLoss;
            int gamesPlayed = playerWins + playerLosses;
            var byes = new ArrayList<Integer>();

            int byeRound = 0;
            if (playersWithByes.containsKey(playerName)) {
                byeRound = playersWithByes.get(playerName);
                points += pointsForWin;
                gamesPlayed += 1;
            }


            List<String> opponents = playerOpponents.get(playerName);
            int opponentWins = 0;
            int opponentGames = 0;

            for (String opponent : opponents) {
                opponentWins += playerWinCounts.get(opponent).intValue();
                opponentGames += playerWinCounts.get(opponent).intValue() + playerLossCounts.get(opponent).intValue();
            }
            float score = 0f;
            if (opponentGames != 0) {
                score = opponentWins * 1f / opponentGames;
            }

            var standing = new PlayerStanding(playerName, points, gamesPlayed, playerWins, playerLosses, byeRound);
            standing.opponentWinRate = score;
            standings.put(playerName, standing);
        }

        var tempStandings = new ArrayList<>(standings.values());
        tempStandings.sort(LEAGUE_STANDING_COMPARATOR);

        int standing = 0;
        int position = 1;
        PlayerStanding lastStanding = null;
        for (var eventStanding : tempStandings) {
            if (lastStanding == null || LEAGUE_STANDING_COMPARATOR.compare(eventStanding, lastStanding) != 0) {
                standing = position;
            }
            var newStanding = PlayerStanding.CopyStanding(eventStanding);
            newStanding.standing = standing;
            standings.put(newStanding.playerName, newStanding);
            position++;
            lastStanding = eventStanding;
        }
        return new ArrayList<>(standings.values());

    }

    private static class PointsComparator implements Comparator<PlayerStanding> {
        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            return o1.points - o2.points;
        }
    }

    private static class GamesPlayedComparator implements Comparator<PlayerStanding> {
        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            return o1.gamesPlayed - o2.gamesPlayed;
        }
    }

    private static class OpponentsWinComparator implements Comparator<PlayerStanding> {
        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            final float diff = o1.opponentWinRate - o2.opponentWinRate;
            if (diff < 0) {
                return -1;
            }
            if (diff > 0) {
                return 1;
            }
            return 0;
        }
    }
}
