package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class LeaguePrizesTest extends AbstractAtTest {
    @Test
    public void test() {
        LeaguePrizes leaguePrizes = new FixedLeaguePrizes(_productLibrary);
        CardCollection prize = leaguePrizes.getPrizeForLeagueMatchWinner(2, 2);
        for (CardCollection.Item stringIntegerEntry : prize.getAll()) {
            System.out.println(stringIntegerEntry.getBlueprintId() + ": " + stringIntegerEntry.getCount());
        }
    }

    @Test
    public void testLeaguePrize() {
        LeaguePrizes leaguePrizes = new FixedLeaguePrizes(_productLibrary);
        for (int i = 1; i <= 32; i++) {
            System.out.println("Place "+i);
            CardCollection prize = leaguePrizes.getPrizeForLeague(i, 60, 1, 2);
            if (prize != null)
                for (CardCollection.Item stringIntegerEntry : prize.getAll()) {
                    System.out.println(stringIntegerEntry.getBlueprintId() + ": " + stringIntegerEntry.getCount());
                }
        }
    }

    @Test
    public void testAutoLeaguePrizes() {

        var topPrize = List.of(CardCollection.Item.createItem("1_1", 1));
        var partPrize = List.of(CardCollection.Item.createItem("1_2", 2));
        var autoPrizes = new EventAutoPrizes(topPrize, 10, partPrize, 3);
        var leaguePrizes = new IncentiveLeaguePrizes(_productLibrary, autoPrizes);
        var standings = List.of(
                new PlayerStanding("p01", 50, 25, 25, 0, 0, 0.5f, 1),
                new PlayerStanding("p02", 50, 25, 25, 0, 0, 0.4f, 2),
                new PlayerStanding("p03", 50, 25, 25, 0, 0, 0.3f, 3),
                new PlayerStanding("p04", 50, 25, 25, 0, 0, 0.2f, 4),
                new PlayerStanding("p05", 50, 25, 25, 0, 0, 0.1f, 5),
                new PlayerStanding("p06", 49, 25, 24, 1, 0, 0.5f, 6),
                new PlayerStanding("p07", 49, 25, 24, 1, 0, 0.4f, 7),
                new PlayerStanding("p08", 49, 25, 24, 1, 0, 0.3f, 8),
                new PlayerStanding("p09", 49, 25, 24, 1, 0, 0.2f, 9),
                new PlayerStanding("p10", 48, 25, 23, 2, 0, 0.6f, 10),
                new PlayerStanding("p11", 48, 25, 23, 2, 0, 0.5f, 11),
                new PlayerStanding("p12", 48, 25, 23, 2, 0, 0.4f, 12),
                new PlayerStanding("p13", 48, 25, 23, 2, 0, 0.3f, 13),
                new PlayerStanding("p14", 48, 25, 23, 2, 0, 0.2f, 14),
                new PlayerStanding("p15", 48, 25, 23, 2, 0, 0.1f, 15),
                new PlayerStanding("p16", 47, 25, 22, 3, 0, 0.5f, 16),
                new PlayerStanding("p17", 47, 25, 22, 3, 0, 0.4f, 17),
                new PlayerStanding("p18", 47, 25, 22, 3, 0, 0.3f, 18),
                new PlayerStanding("p19", 47, 25, 22, 3, 0, 0.2f, 19),
                new PlayerStanding("p20", 47, 25, 22, 3, 0, 0.1f, 20),
                new PlayerStanding("p21", 46, 25, 21, 4, 0, 0.5f, 21),
                new PlayerStanding("p22", 46, 25, 21, 4, 0, 0.4f, 22),
                new PlayerStanding("p23", 46, 25, 21, 4, 0, 0.3f, 23),
                new PlayerStanding("p24", 46, 25, 21, 4, 0, 0.2f, 24),
                new PlayerStanding("p25", 46, 25, 21, 4, 0, 0.1f, 25),
                new PlayerStanding("p26", 12,  6,  6, 0, 0, 0.5f, 26),
                new PlayerStanding("p27", 10,  5,  5, 0, 0, 0.4f, 27),
                new PlayerStanding("p28",  8,  4,  4, 0, 0, 0.3f, 28),
                new PlayerStanding("p29",  6,  3,  3, 0, 0, 0.2f, 29),
                new PlayerStanding("p30",  4,  2,  2, 0, 0, 0.1f, 30),
                new PlayerStanding("p31",  2,  1,  1, 0, 0, 0.5f, 31),
                new PlayerStanding("p32",  0,  0,  0, 0, 0, 0.5f, 32)
        );

        for (var standing : standings) {
            System.out.println("Place " + standing.standing);
            var collection = leaguePrizes.getTrophiesForLeague(standing, standings, 50);

            List<CardCollection.Item> prizes = new ArrayList<>();

            if(collection != null)
                collection.getAll().forEach(prizes::add);

            // The top 10 get the top prize, but also anyone who ties the person in tenth (strength of schedule is ignored)
            if(standing.standing <= 10 || standing.points == 48) {
                assertTrue(prizes.stream().anyMatch(x -> x.getBlueprintId().equals("1_1") && x.getCount() == 1));
            }

            if(standing.gamesPlayed >= 3) {
                assertTrue(prizes.stream().anyMatch(x -> x.getBlueprintId().equals("1_2") && x.getCount() == 2));
            }

            switch (standing.standing) {
                case 29:
                    assertTrue(prizes.stream().anyMatch(x -> x.getBlueprintId().equals("1_2") && x.getCount() == 2));
                    break;
                case 30:
                case 31:
                case 32:
                    assertEquals(0, prizes.size());
            }
        }
    }
}
