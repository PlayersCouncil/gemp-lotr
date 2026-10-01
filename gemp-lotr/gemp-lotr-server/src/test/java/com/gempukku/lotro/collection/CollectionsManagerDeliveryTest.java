package com.gempukku.lotro.collection;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.db.CollectionDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.packs.ProductLibrary;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verifies that awarding an {@code openOnDelivery} product through {@link CollectionsManager} deposits
 * the rolled contents rather than the product, both in the DB write and in the delivery notification.
 */
public class CollectionsManagerDeliveryTest extends AbstractAtTest {

    private static final String PRODUCTS = """
            [
                { "name": "TestAwardedCard", "type": "random", "openOnDelivery": true,
                  "items": ["1x1_1"] },
                { "name": "TestAwardedRandom", "type": "random", "openOnDelivery": true,
                  "items": ["1x1_1", "1x1_2", "1x1_3"] },
                { "name": "TestPlainBooster", "type": "pack",
                  "items": ["1x1_4"] }
            ]
            """;

    private static ProductLibrary _testProducts;

    private PlayerDAO _playerDAO;
    private CollectionDAO _collectionDAO;
    private TransferDAO _transferDAO;
    private CollectionsManager _manager;
    private Player _player;

    @BeforeClass
    public static void loadTestProducts() throws IOException {
        Path dir = Files.createTempDirectory("gemp-collections-delivery-test");
        dir.toFile().deleteOnExit();
        File file = dir.resolve("TestProducts.hjson").toFile();
        Files.writeString(file.toPath(), PRODUCTS, StandardCharsets.UTF_8);
        file.deleteOnExit();
        _testProducts = new ProductLibrary(_cardLibrary, dir.toFile());
    }

    @Before
    public void setUp() throws Exception {
        _playerDAO = Mockito.mock(PlayerDAO.class);
        _collectionDAO = Mockito.mock(CollectionDAO.class);
        _transferDAO = Mockito.mock(TransferDAO.class);
        _player = new Player(1, "Test", "pass", "u", null, null, null, null, false);
        Mockito.when(_collectionDAO.getPlayerCollection(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(new DefaultCardCollection());
        _manager = new CollectionsManager(_playerDAO, _collectionDAO, _transferDAO, _cardLibrary, _testProducts);
    }

    private static List<CardCollection.Item> items(String... combined) {
        List<CardCollection.Item> result = new ArrayList<>();
        for (String item : combined)
            result.add(CardCollection.Item.createItem(item));
        return result;
    }

    /** What was written to the DB by the last {@code addToCollectionContents} call. */
    private CardCollection persisted() throws Exception {
        var captor = ArgumentCaptor.forClass(CardCollection.class);
        Mockito.verify(_collectionDAO).addToCollectionContents(Mockito.eq(1), Mockito.eq("permanent"),
                captor.capture(), Mockito.anyString());
        return captor.getValue();
    }

    /** What the player will be shown in their delivery popup. */
    private CardCollection delivered() {
        var captor = ArgumentCaptor.forClass(CardCollection.class);
        Mockito.verify(_transferDAO).addTransferTo(Mockito.anyBoolean(), Mockito.eq("Test"), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt(), captor.capture());
        return captor.getValue();
    }

    private static int distinctEntries(CardCollection collection) {
        int count = 0;
        for (CardCollection.Item ignored : collection.getAll())
            count++;
        return count;
    }

    // ------------------------------------------------------------------------------------------------
    // addItemsToPlayerCollection
    // ------------------------------------------------------------------------------------------------

    @Test
    public void taggedProductIsOpenedInsteadOfDeposited() throws Exception {
        _manager.addItemsToPlayerCollection(true, "League prize", _player, CollectionType.MY_CARDS,
                items("1xTestAwardedCard"));

        CardCollection stored = persisted();
        assertEquals(0, stored.getItemCount("TestAwardedCard"));
        assertEquals(1, stored.getItemCount("1_1"));
        assertEquals(1, distinctEntries(stored));
    }

    @Test
    public void deliveryNotificationCarriesTheRolledCard() throws Exception {
        _manager.addItemsToPlayerCollection(true, "League prize", _player, CollectionType.MY_CARDS,
                items("1xTestAwardedCard"));

        //addedCards is both what is persisted and what is handed to the transfer log, so the popup
        //must show exactly what landed in the collection.
        assertEquals(persisted(), delivered());
        assertEquals(1, delivered().getItemCount("1_1"));
        assertEquals(0, delivered().getItemCount("TestAwardedCard"));
    }

    @Test
    public void countGreaterThanOneRollsThatManyTimes() throws Exception {
        _manager.addItemsToPlayerCollection(true, "League prize", _player, CollectionType.MY_CARDS,
                items("3xTestAwardedRandom"));

        CardCollection stored = persisted();
        assertEquals(0, stored.getItemCount("TestAwardedRandom"));
        int total = 0;
        for (CardCollection.Item item : stored.getAll()) {
            assertEquals(CardCollection.Item.Type.CARD, item.getType());
            total += item.getCount();
        }
        assertEquals(3, total);
    }

    @Test
    public void countGreaterThanOneOfASingleOptionProductIsExact() throws Exception {
        _manager.addItemsToPlayerCollection(true, "League prize", _player, CollectionType.MY_CARDS,
                items("3xTestAwardedCard"));

        assertEquals(3, persisted().getItemCount("1_1"));
        assertEquals(1, distinctEntries(persisted()));
    }

    @Test
    public void untaggedPacksAreStillDepositedAsPacks() throws Exception {
        _manager.addItemsToPlayerCollection(true, "League prize", _player, CollectionType.MY_CARDS,
                items("2xTestPlainBooster", "1x1_231"));

        CardCollection stored = persisted();
        assertEquals(2, stored.getItemCount("TestPlainBooster"));
        assertEquals(1, stored.getItemCount("1_231"));
        assertEquals(0, stored.getItemCount("1_4"));
    }

    @Test
    public void unknownProductsAreStillDeposited() throws Exception {
        _manager.addItemsToPlayerCollection(true, "League prize", _player, CollectionType.MY_CARDS,
                items("1xNo Such Product"));

        assertEquals(1, persisted().getItemCount("No Such Product"));
    }

    // ------------------------------------------------------------------------------------------------
    // addPlayerCollection (sealed/draft starting pools)
    // ------------------------------------------------------------------------------------------------

    @Test
    public void startingPoolsExpandTaggedProducts() throws Exception {
        DefaultCardCollection startingPool = new DefaultCardCollection();
        startingPool.addItem("TestAwardedCard", 2);
        startingPool.addItem("TestPlainBooster", 6);

        _manager.addPlayerCollection(true, "Sealed league", _player, new CollectionType(1234L, "Test"), startingPool);

        var captor = ArgumentCaptor.forClass(CardCollection.class);
        Mockito.verify(_collectionDAO).overwriteCollectionContents(Mockito.eq(1), Mockito.eq("1234"),
                captor.capture(), Mockito.anyString());

        CardCollection stored = captor.getValue();
        assertEquals(0, stored.getItemCount("TestAwardedCard"));
        assertEquals(2, stored.getItemCount("1_1"));
        assertEquals(6, stored.getItemCount("TestPlainBooster"));
    }

    @Test
    public void startingPoolsWithoutTaggedProductsArePassedThroughUntouched() throws Exception {
        DefaultCardCollection startingPool = new DefaultCardCollection();
        startingPool.addItem("TestPlainBooster", 6);
        startingPool.addItem("(S)TestStarter", 1);

        _manager.addPlayerCollection(true, "Sealed league", _player, new CollectionType(1234L, "Test"), startingPool);

        //Identity, not just equality: the seven starting-pool call sites must be unaffected.
        Mockito.verify(_collectionDAO).overwriteCollectionContents(Mockito.eq(1), Mockito.eq("1234"),
                Mockito.same(startingPool), Mockito.anyString());
        Mockito.verify(_transferDAO).addTransferTo(Mockito.anyBoolean(), Mockito.eq("Test"), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.same(startingPool));
    }

    @Test
    public void startingPoolCurrencyIsPreservedThroughExpansion() throws Exception {
        DefaultCardCollection startingPool = new DefaultCardCollection();
        startingPool.addItem("TestAwardedCard", 1);
        startingPool.addCurrency(500);

        _manager.addPlayerCollection(true, "Sealed league", _player, new CollectionType(1234L, "Test"), startingPool);

        var captor = ArgumentCaptor.forClass(CardCollection.class);
        Mockito.verify(_collectionDAO).overwriteCollectionContents(Mockito.eq(1), Mockito.eq("1234"),
                captor.capture(), Mockito.anyString());
        assertEquals(500, captor.getValue().getCurrency());
        assertEquals(1, captor.getValue().getItemCount("1_1"));
    }

    // ------------------------------------------------------------------------------------------------
    // Regression: the real, untagged prize products
    // ------------------------------------------------------------------------------------------------

    @Test
    public void realEventPrizePacksAreStillDeliveredAsPacks() throws Exception {
        CollectionsManager manager = new CollectionsManager(_playerDAO, _collectionDAO, _transferDAO,
                _cardLibrary, _productLibrary);
        manager.addItemsToPlayerCollection(true, "End of league prizes", _player, CollectionType.MY_CARDS,
                //FixedLeaguePrizes/DailyTournamentPrizes build these items with recursive=true; that flag
                //has never been honoured on the award path and must not start being honoured now.
                List.of(CardCollection.Item.createItem("Event Chase Booster", 3, true),
                        CardCollection.Item.createItem("Placement Random Chase Card Selector", 2, true)));

        CardCollection stored = persisted();
        assertEquals(3, stored.getItemCount("Event Chase Booster"));
        assertEquals(2, stored.getItemCount("Placement Random Chase Card Selector"));
        assertEquals(2, distinctEntries(stored));
    }

    @Test
    public void realTaggedRandomProductIsOpened() throws Exception {
        CollectionsManager manager = new CollectionsManager(_playerDAO, _collectionDAO, _transferDAO,
                _cardLibrary, _productLibrary);
        manager.addItemsToPlayerCollection(true, "End of league prizes", _player, CollectionType.MY_CARDS,
                items("1xRandom PC Full Art"));

        CardCollection stored = persisted();
        assertEquals(0, stored.getItemCount("Random PC Full Art"));
        assertEquals(1, distinctEntries(stored));
        for (CardCollection.Item item : stored.getAll()) {
            assertEquals(CardCollection.Item.Type.CARD, item.getType());
            assertTrue(_productLibrary.GetProduct("Random PC Full Art").GetAllOptions()
                    .contains(item.getBlueprintId()));
        }
    }
}
