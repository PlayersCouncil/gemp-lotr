package com.gempukku.lotro.packs;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Exercises the shared pack-expansion routine against a synthetic product library, plus the real
 * product definitions for the tagged products the designer asked for.
 */
public class ProductOpenerTest extends AbstractAtTest {

    /**
     * A small, fully deterministic-where-it-matters product set.  Kept out of the real product
     * directory so the cycle/depth fixtures cannot reach a running server.
     */
    private static final String PRODUCTS = """
            [
                { "name": "TestOpenOnDelivery", "type": "random", "openOnDelivery": true,
                  "items": ["1x1_1", "1x1_2", "1x1_3"] },
                { "name": "TestSingleCard", "type": "random", "openOnDelivery": true,
                  "items": ["1x1_1"] },
                { "name": "TestPlainPack", "type": "pack",
                  "items": ["2x1_1", "1x1_2"] },
                { "name": "TestNestedOuter", "type": "pack", "openOnDelivery": true,
                  "items": ["1xTestSingleCard", "1x1_2"] },
                { "name": "TestRecursiveParent", "type": "pack", "recursive": true,
                  "items": ["1xTestPlainPack"] },
                { "name": "TestTaggedSelection", "type": "selection", "openOnDelivery": true,
                  "items": ["1x1_1", "1x1_2"] },
                { "name": "TestSelfReferential", "type": "pack", "openOnDelivery": true,
                  "items": ["1xTestSelfReferential"] },
                { "name": "TestSelfRecursive", "type": "pack", "recursive": true,
                  "items": ["1xTestSelfRecursive"] }
            ]
            """;

    private static ProductLibrary _testProducts;

    @BeforeClass
    public static void loadTestProducts() throws IOException {
        Path dir = Files.createTempDirectory("gemp-product-opener-test");
        dir.toFile().deleteOnExit();
        File file = dir.resolve("TestProducts.hjson").toFile();
        Files.writeString(file.toPath(), PRODUCTS, StandardCharsets.UTF_8);
        file.deleteOnExit();
        _testProducts = new ProductLibrary(_cardLibrary, dir.toFile());
    }

    private static CardCollection.Item pack(String name, int count) {
        return CardCollection.Item.createItem(name, count);
    }

    private static List<String> ids(List<CardCollection.Item> items) {
        List<String> result = new ArrayList<>();
        for (CardCollection.Item item : items)
            result.add(item.getCount() + "x" + item.getBlueprintId());
        return result;
    }

    private static int totalCount(List<CardCollection.Item> items) {
        int total = 0;
        for (CardCollection.Item item : items)
            total += item.getCount();
        return total;
    }

    // ------------------------------------------------------------------------------------------------
    // Loading the tag
    // ------------------------------------------------------------------------------------------------

    @Test
    public void tagIsReadForRandomProducts() {
        assertTrue(_testProducts.opensOnDelivery("TestOpenOnDelivery"));
        assertTrue(_testProducts.opensOnDelivery("TestNestedOuter"));
        assertFalse(_testProducts.opensOnDelivery("TestPlainPack"));
        assertFalse(_testProducts.opensOnDelivery("No Such Product"));
        assertFalse(_testProducts.opensOnDelivery(null));
    }

    @Test
    public void tagIsRejectedForSelectionProducts() {
        //A selection needs the player to pick, and there is nobody to ask at award time.
        assertNotNull(_testProducts.GetProduct("TestTaggedSelection"));
        assertFalse(_testProducts.opensOnDelivery("TestTaggedSelection"));
    }

    @Test
    public void designerTaggedProductsAreTaggedInTheRealData() {
        assertTrue(_productLibrary.opensOnDelivery("Random PC Full Art"));
        assertTrue(_productLibrary.opensOnDelivery("Random Alt Image Promo"));
        assertTrue(_productLibrary.opensOnDelivery("Random Tengwar"));
        assertTrue(_productLibrary.opensOnDelivery("Random Masterwork"));
    }

    @Test
    public void untaggedRealProductsAreNotTagged() {
        assertFalse(_productLibrary.opensOnDelivery("FotR - Booster"));
        assertFalse(_productLibrary.opensOnDelivery("Event Chase Booster"));
        assertFalse(_productLibrary.opensOnDelivery("Placement Random Chase Card Selector"));
    }

    // ------------------------------------------------------------------------------------------------
    // expandForDelivery - the award path
    // ------------------------------------------------------------------------------------------------

    @Test
    public void cardsAreLeftAlone() {
        var result = ProductOpener.expandForDelivery(_testProducts, CardCollection.Item.createItem("1_231", 4));
        assertEquals(List.of("4x1_231"), ids(result));
    }

    @Test
    public void selectionsAreLeftAlone() {
        var result = ProductOpener.expandForDelivery(_testProducts, CardCollection.Item.createItem("(S)TestTaggedSelection", 2));
        assertEquals(List.of("2x(S)TestTaggedSelection"), ids(result));
    }

    @Test
    public void untaggedPacksAreLeftAlone() {
        var result = ProductOpener.expandForDelivery(_testProducts, pack("TestPlainPack", 2));
        assertEquals(List.of("2xTestPlainPack"), ids(result));
    }

    @Test
    public void unknownProductsAreLeftAlone() {
        var result = ProductOpener.expandForDelivery(_testProducts, pack("No Such Product", 3));
        assertEquals(List.of("3xNo Such Product"), ids(result));
    }

    @Test
    public void topLevelRecursiveFlagDoesNotTriggerExpansionOnAward() {
        //Award sites have always built items with `recursive` set arbitrarily (FixedLeaguePrizes,
        //DailyTournamentPrizes) and it has never been read here.  It must stay that way.
        var item = CardCollection.Item.createItem("TestPlainPack", 1, true);
        assertEquals(List.of("1xTestPlainPack"), ids(ProductOpener.expandForDelivery(_testProducts, item)));
    }

    @Test
    public void taggedProductIsOpenedIntoItsContents() {
        var result = ProductOpener.expandForDelivery(_testProducts, pack("TestSingleCard", 1));
        assertEquals(List.of("1x1_1"), ids(result));
    }

    @Test
    public void taggedProductRollsOneOfItsOptions() {
        Set<String> options = new HashSet<>(_testProducts.GetProduct("TestOpenOnDelivery").GetAllOptions());
        for (int i = 0; i < 50; i++) {
            var result = ProductOpener.expandForDelivery(_testProducts, pack("TestOpenOnDelivery", 1));
            assertEquals(1, result.size());
            assertEquals(CardCollection.Item.Type.CARD, result.get(0).getType());
            assertTrue(options.contains(result.get(0).getBlueprintId()));
        }
    }

    @Test
    public void countIsRolledIndependentlyPerCopy() {
        Set<String> options = new HashSet<>(_testProducts.GetProduct("TestOpenOnDelivery").GetAllOptions());
        boolean sawTwoDifferentCards = false;
        for (int i = 0; i < 200; i++) {
            var result = ProductOpener.expandForDelivery(_testProducts, pack("TestOpenOnDelivery", 3));
            assertEquals("three copies must yield three cards", 3, totalCount(result));
            Set<String> distinct = new HashSet<>();
            for (CardCollection.Item item : result) {
                assertTrue(options.contains(item.getBlueprintId()));
                distinct.add(item.getBlueprintId());
            }
            if (distinct.size() > 1)
                sawTwoDifferentCards = true;
        }
        //A single roll multiplied by the count would make this impossible; over 200 draws of 3 from
        //3 options, never seeing two different cards has probability (1/9)^200.
        assertTrue("counts > 1 must be rolled independently, not rolled once and multiplied", sawTwoDifferentCards);
    }

    @Test
    public void countOfASingleItemProductIsExact() {
        var result = ProductOpener.expandForDelivery(_testProducts, pack("TestSingleCard", 3));
        assertEquals(3, totalCount(result));
        for (CardCollection.Item item : result)
            assertEquals("1_1", item.getBlueprintId());
    }

    @Test
    public void nestedTaggedProductsFullyExpand() {
        //TestNestedOuter is tagged and contains a tagged product plus a plain card.
        var result = ProductOpener.expandForDelivery(_testProducts, pack("TestNestedOuter", 1));
        for (CardCollection.Item item : result)
            assertEquals("everything must flatten down to cards", CardCollection.Item.Type.CARD, item.getType());
        assertEquals(List.of("1x1_1", "1x1_2"), ids(result));
    }

    @Test
    public void iterableOverloadPreservesOrderAndUntaggedItems() {
        var result = ProductOpener.expandForDelivery(_testProducts, List.of(
                CardCollection.Item.createItem("1_231", 1),
                pack("TestSingleCard", 1),
                pack("TestPlainPack", 1)));
        assertEquals(List.of("1x1_231", "1x1_1", "1xTestPlainPack"), ids(result));
    }

    @Test
    public void nullIterableIsTolerated() {
        assertTrue(ProductOpener.expandForDelivery(_testProducts, (Iterable<CardCollection.Item>) null).isEmpty());
    }

    // ------------------------------------------------------------------------------------------------
    // expandPackContents - the player-opens-a-pack path
    // ------------------------------------------------------------------------------------------------

    @Test
    public void recursiveFlagStillOpensChildPacks() {
        var item = CardCollection.Item.createItem("TestPlainPack", 1, true);
        assertEquals(List.of("2x1_1", "1x1_2"), ids(ProductOpener.expandPackContents(_testProducts, item)));
    }

    @Test
    public void nonRecursiveChildPacksAreStillDeposited() {
        var item = CardCollection.Item.createItem("TestPlainPack", 1, false);
        assertEquals(List.of("1xTestPlainPack"), ids(ProductOpener.expandPackContents(_testProducts, item)));
    }

    @Test
    public void existingRecursiveBehaviourIsUnchangedWhenOpeningAPack() {
        //TestRecursiveParent is recursive:false-by-default at the top but stamps recursive on its children,
        //so opening it deposits TestPlainPack's cards rather than the pack.
        DefaultCardCollection collection = new DefaultCardCollection();
        collection.addAndOpenPack("TestRecursiveParent", 1, _testProducts);

        assertEquals(2, collection.getItemCount("1_1"));
        assertEquals(1, collection.getItemCount("1_2"));
        assertEquals(0, collection.getItemCount("TestPlainPack"));
    }

    @Test
    public void taggedChildOpensEvenWhenTheParentIsNotRecursive() {
        //Decision: a product's own openOnDelivery always wins; recursive:false is only the default.
        var item = CardCollection.Item.createItem("TestSingleCard", 1, false);
        assertEquals(List.of("1x1_1"), ids(ProductOpener.expandPackContents(_testProducts, item)));
    }

    // ------------------------------------------------------------------------------------------------
    // Depth guard
    // ------------------------------------------------------------------------------------------------

    @Test
    public void selfReferentialTaggedProductStopsAtTheDepthLimit() {
        var result = ProductOpener.expandForDelivery(_testProducts, pack("TestSelfReferential", 1));
        //Deposited as-is rather than thrown: a StackOverflowError here would be raised while the
        //collection write lock is held.
        assertEquals(List.of("1xTestSelfReferential"), ids(result));
    }

    @Test
    public void selfRecursiveProductStopsAtTheDepthLimit() {
        var item = CardCollection.Item.createItem("TestSelfRecursive", 1, true);
        assertEquals(List.of("1xTestSelfRecursive"), ids(ProductOpener.expandPackContents(_testProducts, item)));
    }

    @Test
    public void selfRecursiveProductDoesNotBlowTheStackWhenOpened() {
        DefaultCardCollection collection = new DefaultCardCollection();
        collection.addAndOpenPack("TestSelfRecursive", 1, _testProducts);
        assertEquals(1, collection.getItemCount("TestSelfRecursive"));
    }

    // ------------------------------------------------------------------------------------------------
    // The motivating case, against the real product data
    // ------------------------------------------------------------------------------------------------

    @Test
    public void randomPcFullArtDeliversACard() {
        Set<String> options = new HashSet<>(_productLibrary.GetProduct("Random PC Full Art").GetAllOptions());
        for (int i = 0; i < 20; i++) {
            var result = ProductOpener.expandForDelivery(_productLibrary, pack("Random PC Full Art", 1));
            assertEquals(1, result.size());
            assertEquals(CardCollection.Item.Type.CARD, result.get(0).getType());
            assertTrue(options.contains(result.get(0).getBlueprintId()));
        }
    }
}
