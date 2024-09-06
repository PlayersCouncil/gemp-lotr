package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class SealedLeagueDataTest extends AbstractAtTest {

    private static ZonedDateTime JanuaryFirst2012 = ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, DateUtils.UTC);
    private static LeagueParams FOTRSealedTestParams = new LeagueParams() {{
       name = "Test League";
       code = 1234;
       start = JanuaryFirst2012.toLocalDateTime();
       cost = 0;
       collectionName = "Test Collection";
       series.add(new SerieData("fotr_block_sealed", 7, 1));
    }};

    @Test
    public void testJoinLeagueFirstWeek() throws IOException {
        SealedLeague league = new SealedLeague(_productLibrary, _formatLibrary, FOTRSealedTestParams);
        CollectionType collectionType = new CollectionType(FOTRSealedTestParams.code, FOTRSealedTestParams.collectionName);

        for (int i = 1; i <= 7; i++) {
            var date = JanuaryFirst2012.withDayOfMonth(i);
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Player player = new Player(1, "Test", "pass", "u", null, null, null, null, false);
            league.createLeagueCollection(collectionsManager, player, date);
            Mockito.verify(collectionsManager, new Times(1))
                .addPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType), Mockito.argThat(
                        new ArgumentMatcher<>() {
                            @Override
                            public boolean matches(CardCollection cards) {
                                if (Iterables.size(cards.getAll()) != 3)
                                    return false;
                                if (cards.getItemCount("(S)FotR - Starter") != 1)
                                    return false;
                                if (cards.getItemCount("FotR - Booster") != 6)
                                    return false;
                                if (cards.getItemCount("1_231") != 2)
                                    return false;
                                return true;
                            }
                        }
            ));
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testJoinLeagueSecondWeek() throws IOException {
        SealedLeague league = new SealedLeague(_productLibrary, _formatLibrary, FOTRSealedTestParams);
        CollectionType collectionType = new CollectionType(FOTRSealedTestParams.code, FOTRSealedTestParams.collectionName);
        for (int i = 8; i <= 14; i++) {
            var date = JanuaryFirst2012.withDayOfMonth(i);
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Player player = new Player(1, "Test", "pass", "u", null, null, null, null, false);
            league.createLeagueCollection(collectionsManager, player, date);
            Mockito.verify(collectionsManager, new Times(1)).addPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType), Mockito.argThat(
                    new ArgumentMatcher<>() {
//                        @Override
//                        public void describeTo(Description description) {
//                            description.appendText("Expected collection");
//                        }

                        @Override
                        public boolean matches(CardCollection cards) {
                            if (Iterables.size(cards.getAll()) != 6)
                                return false;
                            if (cards.getItemCount("(S)FotR - Starter") != 1)
                                return false;
                            if (cards.getItemCount("FotR - Booster") != 6)
                                return false;
                            if (cards.getItemCount("1_231") != 2)
                                return false;
                            if (cards.getItemCount("(S)MoM - Starter") != 1)
                                return false;
                            if (cards.getItemCount("MoM - Booster") != 3)
                                return false;
                            if (cards.getItemCount("2_51") != 1)
                                return false;
                            return true;
                        }
                    }
            ));
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testSwitchToFirstWeek() {
        SealedLeague league = new SealedLeague(_productLibrary, _formatLibrary, FOTRSealedTestParams);
        for (int i = 1; i <= 7; i++) {
            var date = JanuaryFirst2012.withDayOfMonth(i);
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection(FOTRSealedTestParams.code)).thenReturn(new HashMap<>());
            int result = league.process(collectionsManager, null, 0, date);
            assertEquals(1, result);
            Mockito.verify(collectionsManager, new Times(1)).getPlayersCollection("1234");
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testProcessMidFirstWeek() {
        SealedLeague league = new SealedLeague(_productLibrary, _formatLibrary, FOTRSealedTestParams);
        for (int i = 1; i <= 7; i++) {
            var date = JanuaryFirst2012.withDayOfMonth(i);
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection(FOTRSealedTestParams.code)).thenReturn(new HashMap<>());
            int result = league.process(collectionsManager, null, 1, date);
            assertEquals(1, result);
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testSwitchToSecondWeek() {
        SealedLeague league = new SealedLeague(_productLibrary, _formatLibrary, FOTRSealedTestParams);
        CollectionType collectionType = new CollectionType(FOTRSealedTestParams.code, FOTRSealedTestParams.collectionName);
        for (int i = 8; i <= 14; i++) {
            var date = JanuaryFirst2012.withDayOfMonth(i);
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Map<Player, CardCollection> playersInLeague = new HashMap<>();
            Player player = new Player(1, "Test", "pass", "u", null, null, null, null, false);
            playersInLeague.put(player, new DefaultCardCollection());
            Mockito.when(collectionsManager.getPlayersCollection(String.valueOf(FOTRSealedTestParams.code))).thenReturn(playersInLeague);
            int result = league.process(collectionsManager, null, 1, date);
            assertEquals(2, result);
            final List<CardCollection.Item> expectedToAdd = new ArrayList<>();
            expectedToAdd.add(CardCollection.Item.createItem("(S)MoM - Starter", 1));
            expectedToAdd.add(CardCollection.Item.createItem("MoM - Booster", 3));
            expectedToAdd.add(CardCollection.Item.createItem("2_51", 1));
            Mockito.verify(collectionsManager, new Times(1)).getPlayersCollection(String.valueOf(FOTRSealedTestParams.code));
            Mockito.verify(collectionsManager, new Times(1)).addItemsToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType),
                    Mockito.argThat(
                            new ArgumentMatcher<Collection<CardCollection.Item>>() {
                                @Override
                                public boolean matches(Collection<CardCollection.Item> argument) {
                                    if (argument.size() != expectedToAdd.size())
                                        return false;
                                    for (CardCollection.Item item : expectedToAdd) {
                                        if (!argument.contains(item))
                                            return false;
                                    }
                                    return true;
                                }
                            }));
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testProcessMidSecondWeek() {
        SealedLeague league = new SealedLeague(_productLibrary, _formatLibrary, FOTRSealedTestParams);
        CollectionType collectionType = new CollectionType(FOTRSealedTestParams.code, FOTRSealedTestParams.collectionName);
        for (int i = 8; i <= 14; i++) {
            var date = JanuaryFirst2012.withDayOfMonth(i);
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection(FOTRSealedTestParams.code)).thenReturn(new HashMap<>());
            int result = league.process(collectionsManager, null, 2, date);
            assertEquals(2, result);
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }
}
