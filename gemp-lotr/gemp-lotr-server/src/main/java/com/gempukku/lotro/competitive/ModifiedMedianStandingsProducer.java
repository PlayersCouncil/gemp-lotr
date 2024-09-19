package com.gempukku.lotro.competitive;

import com.gempukku.lotro.tournament.TournamentMatch;
import com.gempukku.util.DescComparator;
import com.gempukku.util.MultipleComparator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ModifiedMedianStandingsProducer {

    private static final FaceOffComparator FACE_OFF_COMPARATOR = new FaceOffComparator();
    private static final Comparator<PlayerStanding> MEDIAN_STANDING_COMPARATOR =
            new MultipleComparator<>(
                    new DescComparator<>(Comparator.comparingInt(x -> x.points)),
                    new DescComparator<>(FACE_OFF_COMPARATOR),
                    new DescComparator<>(Comparator.comparingInt(x -> x.medianScore)),
                    new DescComparator<>(Comparator.comparingInt(x -> x.cumulativeScore)),
                    new DescComparator<>(Comparator.comparingDouble(x -> x.opponentWinRate)));


    public static List<PlayerStanding> produceStandings(Collection<String> participants, Collection<TournamentMatch> matches,
                                                        int pointsForWin, int pointsForLoss, Map<String, Integer> playersWithByes) {
        Map<String, List<String>> playerOpponents = new HashMap<>();
        Map<String, AtomicInteger> playerWinCounts = new HashMap<>();
        Map<String, AtomicInteger> playerLossCounts = new HashMap<>();
        Map<String, List<Integer>> playerScores = new HashMap<>();

        FACE_OFF_COMPARATOR.matches = matches;


        for (String playerName : participants) {
            playerOpponents.put(playerName, new ArrayList<>());
            playerWinCounts.put(playerName, new AtomicInteger(0));
            playerLossCounts.put(playerName, new AtomicInteger(0));
            playerScores.put(playerName, new ArrayList<>());
        }

        var rounds = 0;

        for (var match : matches) {
            playerOpponents.get(match.getWinner()).add(match.getLoser());
            playerOpponents.get(match.getLoser()).add(match.getWinner());
            playerWinCounts.get(match.getWinner()).incrementAndGet();
            playerLossCounts.get(match.getLoser()).incrementAndGet();

            if(match.getRound() > rounds)
                rounds = match.getRound();
        }

        //used for cumulative scoring, which requires we have the game results in the order that they occurred.
        for(int i = 1; i <= rounds; i++ ) {
            for(var match : matches) {
                if(match.getRound() == i) {
                    playerScores.get(match.getWinner()).add(1);
                    playerScores.get(match.getLoser()).add(0);
                }
            }

            for(var player : playersWithByes.keySet()) {
                if(playersWithByes.get(player) == i) {
                    //For the purposes of cumulative scoring, we'll count a bye as a loss only because
                    // it represents no effort, and cumulative scoring is supposed to compound higher
                    // effort wins.
                    playerScores.get(player).add(0);
                }
            }
        }

        var standings = new HashMap<String, PlayerStanding>();
        for (String playerName : participants) {
            int playerWins = playerWinCounts.get(playerName).intValue();
            int playerLosses = playerLossCounts.get(playerName).intValue();
            int points = playerWins * pointsForWin + playerLosses * pointsForLoss;
            int gamesPlayed = playerWins + playerLosses;

            int byeRound = 0;
            if (playersWithByes.containsKey(playerName)) {
                byeRound = playersWithByes.get(playerName);
                points += pointsForWin;
                gamesPlayed += 1;
            }

            /*

            The Modified Median system calculates tiebreakers as follows:

                Players with the same score who faced each other defer to the winner.
                Players with more than 50% score have only their lowest-scoring opponent's score discarded;
                Players with less than 50% score have only their highest-scoring opponent's score discarded.
                Players with exactly 50% score discard both their lowest and highest opponent score;

             */

            List<String> opponents = playerOpponents.get(playerName);
            List<Integer> oppScores = new ArrayList<>();
            int opponentWins = 0;
            int opponentGames = 0;

            for (String opponent : opponents) {
                int wins = playerWinCounts.get(opponent).intValue();
                if (playersWithByes.containsKey(opponent)) {
                    wins += 1;
                }
                oppScores.add(wins);
                opponentWins += wins;
                opponentGames += playerWinCounts.get(opponent).intValue() + playerLossCounts.get(opponent).intValue();
            }

            oppScores.sort(Collections.reverseOrder());
            float opponentWR = 0f;
            if (opponentGames != 0) {
                opponentWR = opponentWins * 1f / opponentGames;
            }

            //List of opponent scores is now sorted such that the first entry is the highest score, and the last entry
            // is the lowest score.  We will drop one or more of those positions based on the player's performance:

            if(gamesPlayed > 0) {
                if(playerWins == playerLosses) { // i.e. that player has a 50% win rate; this eliminates floating point comparisons
                    if(oppScores.size() > 1) {
                        oppScores.removeLast();
                    }

                    if(oppScores.size() > 1) {
                        oppScores.removeFirst();
                    }
                }
                else if(playerWins > playerLosses) {
                    if(oppScores.size() > 1) {
                        oppScores.removeLast();
                    }
                }
                else { //playerWins < playerLosses
                    if(oppScores.size() > 1) {
                        oppScores.removeFirst();
                    }
                }
            }

            int median = 0;
            if(!oppScores.isEmpty()) {
                median = oppScores.stream().mapToInt(Integer::intValue).sum();
            }

            int cumulative = 0;
            int lastStep = 0;
            if(gamesPlayed > 0) {
                for(int score : playerScores.get(playerName)) {
                    lastStep += score;
                    cumulative += lastStep;
                }
            }

            var standing = new PlayerStanding(playerName, points, gamesPlayed, playerWins, playerLosses, byeRound);
            standing.medianScore = median;
            standing.opponentWinRate = opponentWR;
            standing.cumulativeScore = cumulative;

            standings.put(playerName, standing);
        }

        var tempStandings = new ArrayList<>(standings.values());
        tempStandings.sort(MEDIAN_STANDING_COMPARATOR);

        int standing = 0;
        int position = 1;
        PlayerStanding lastStanding = null;
        for (var eventStanding : tempStandings) {
            if (lastStanding == null || MEDIAN_STANDING_COMPARATOR.compare(eventStanding, lastStanding) != 0) {
                standing = position;
            }
            var newStanding = PlayerStanding.CopyStanding(eventStanding);
            newStanding.standing = standing;
            standings.put(newStanding.playerName, newStanding);
            position++;
            lastStanding = eventStanding;
        }

        var finalStandings = new ArrayList<>(standings.values());

        finalStandings.sort(Comparator.comparingInt(x -> x.standing));

        return finalStandings;

    }

    private static class FaceOffComparator implements Comparator<PlayerStanding> {

        public Collection<? extends CompetitiveMatchResult> matches;


        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            var result = matches.stream().filter(x ->
                    (x.getLoser().equals(o1.playerName) && x.getWinner().equals(o2.playerName)) ||
                    (x.getLoser().equals(o2.playerName) && x.getWinner().equals(o1.playerName)) ).findFirst();


            if(result.isEmpty())
                return 0;

            if(result.get().getWinner().equals(o1.playerName))
                return 1;

            return -1;
        }
    }
}
