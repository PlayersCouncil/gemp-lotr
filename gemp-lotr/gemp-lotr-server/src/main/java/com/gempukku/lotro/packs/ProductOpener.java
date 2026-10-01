package com.gempukku.lotro.packs;

import com.gempukku.lotro.game.CardCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The single place where "this item is really a pack that should be opened" is decided and acted on.
 *
 * <p>Two flags feed into it:</p>
 * <ul>
 *     <li><code>recursive</code> — carried on a {@link CardCollection.Item} emitted by a {@link PackBox}.
 *     It is a <em>parent's</em> statement about its children: "the products I emit are opened, not
 *     deposited".</li>
 *     <li><code>openOnDelivery</code> — a property of the product itself, looked up by name from
 *     {@link ProductLibrary}: "I am never deposited as a pack".</li>
 * </ul>
 *
 * <p>A product's own <code>openOnDelivery</code> always wins; a parent's <code>recursive: false</code>
 * is merely the default and does not veto it.</p>
 */
public final class ProductOpener {
    private static final Logger _log = LogManager.getLogger(ProductOpener.class);

    /**
     * How deep pack-inside-pack expansion may go before we give up and deposit the pack unopened.
     * A cycle in the product definitions would otherwise blow the stack while the collection write
     * lock is held, wedging collections for every player.
     */
    public static final int MAX_EXPANSION_DEPTH = 10;

    private ProductOpener() {
    }

    /**
     * Expands the items of an award (league/tournament prize, prize tier, admin grant, starting pool)
     * on their way into a player's collection.  A top-level item is opened only if its product is
     * tagged <code>openOnDelivery</code>: the <code>recursive</code> flag is deliberately not honoured
     * at this level, because award sites have always built their items with it set arbitrarily and it
     * has never been read here.  Below the top level the normal rules apply.
     */
    public static List<CardCollection.Item> expandForDelivery(ProductLibrary productLibrary, Iterable<CardCollection.Item> items) {
        List<CardCollection.Item> result = new ArrayList<>();
        if (items != null) {
            for (CardCollection.Item item : items)
                flatten(productLibrary, item, 0, false, result);
        }
        return result;
    }

    /**
     * @see #expandForDelivery(ProductLibrary, Iterable)
     */
    public static List<CardCollection.Item> expandForDelivery(ProductLibrary productLibrary, CardCollection.Item item) {
        List<CardCollection.Item> result = new ArrayList<>();
        flatten(productLibrary, item, 0, false, result);
        return result;
    }

    /**
     * Expands one item that came out of a pack the player just opened.  Here the emitting pack's
     * <code>recursive</code> flag does apply to the item, as it always has.
     */
    public static List<CardCollection.Item> expandPackContents(ProductLibrary productLibrary, CardCollection.Item item) {
        List<CardCollection.Item> result = new ArrayList<>();
        flatten(productLibrary, item, 0, true, result);
        return result;
    }

    /**
     * @param honourRecursive whether this item's own <code>recursive</code> flag may trigger expansion.
     *                        Always true below the top level.
     */
    private static void flatten(ProductLibrary productLibrary, CardCollection.Item item, int depth,
                                boolean honourRecursive, List<CardCollection.Item> out) {
        if (item == null)
            return;

        //Cards are terminal, and a selection needs a player to choose from it, so it is deposited as-is.
        if (item.getType() != CardCollection.Item.Type.PACK) {
            out.add(item);
            return;
        }

        boolean expand = (honourRecursive && item.isRecursive())
                || productLibrary.opensOnDelivery(item.getBlueprintId());
        if (!expand) {
            out.add(item);
            return;
        }

        if (depth >= MAX_EXPANSION_DEPTH) {
            _log.warn("Reached the maximum product expansion depth of " + MAX_EXPANSION_DEPTH + " while opening '"
                    + item.getBlueprintId() + "'; depositing it unopened.  Check the product definitions for a cycle.");
            out.add(item);
            return;
        }

        PackBox product = productLibrary.GetProduct(item.getBlueprintId());
        if (product == null) {
            _log.warn("No product definition named '" + item.getBlueprintId() + "'; depositing it unopened.");
            out.add(item);
            return;
        }

        //One independent roll per copy: awarding 3 of a random product must not award the same card 3 times.
        for (int i = 0; i < item.getCount(); i++) {
            for (CardCollection.Item child : product.openPack())
                flatten(productLibrary, child, depth + 1, true, out);
        }
    }
}
