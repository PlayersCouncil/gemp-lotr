package com.gempukku.lotro.framework;

import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DeckValidationContext;
import com.gempukku.lotro.game.formats.DefaultLotroFormat;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper for testing deck validation rules without a full game scenario.
 * Provides deck building utilities with auto-filler to meet minimum deck sizes.
 *
 * The filler is as the scenario's format plays it: where the format has errata for a filler card (the PC formats
 * have, for a few), the errata'd card goes into the deck; a card the format refuses on its own account (one on its
 * X-list: Forces of Mordor 1_248 in Multipath) or allows fewer than four copies of (one on its R-list: the same
 * card in Expanded) is passed over; and so is a card whose title an earlier filler card has (Boromir 1_97 after
 * Boromir 1_96: the format counts copies by title).  So the filler itself is never what a format objects to, at
 * any size up to four copies of every card left in its range (100 a side and more).  The filler knows nothing of the
 * cards a test names: a test that names a card of the filler's range (1_88 to 1_119, 1_239 to 1_283), or another
 * card of one of their titles, may find its copies added to.
 *
 * A deck that is otherwise legal therefore validates with no errors at all, in a format that takes the sites.
 * The Ring-bearer, the Ring, the Map and the sites are the ones the test names, as it names them; buildDeck and
 * buildDeckWithCopies use the rig's FellowshipSites, of which Multipath, the format of the constructor with no
 * argument, X-lists two (Emyn Muil 1_360 and Ettenmoors 1_331): every such deck draws "Card is X-listed" for
 * each there, and a test in Multipath reads the objection it is about and not the whole list.
 */
public class DeckValidationScenario implements TestConstants {

    // FP filler seed: Gondor set 1 (1_88 through 1_119, ~32 cards)
    private static final int FP_FILLER_START = 88;
    private static final int FP_FILLER_END = 119;
    private static final String FP_FILLER_PREFIX = "1_";

    // Shadow filler seed: Sauron set 1 (1_239 through 1_283, ~45 cards)
    private static final int SHADOW_FILLER_START = 239;
    private static final int SHADOW_FILLER_END = 283;
    private static final String SHADOW_FILLER_PREFIX = "1_";

    private final DefaultLotroFormat _format;

    public DeckValidationScenario() {
        this(Multipath);
    }

    public DeckValidationScenario(String formatCode) {
        _format = (DefaultLotroFormat) VirtualTableScenario._formatLibrary.getFormat(formatCode);
    }

    public DefaultLotroFormat getFormat() {
        return _format;
    }

    /**
     * Builds a deck with specified test cards plus auto-generated filler.
     * Filler rotates through cards in groups of 4 (the default max-same-name).
     *
     * @param fpCards Map of FP card blueprint IDs to desired copy counts
     * @param shadowCards Map of Shadow card blueprint IDs to desired copy counts
     * @param fpFillerCount Number of additional FP filler cards to add
     * @param shadowFillerCount Number of additional Shadow filler cards to add
     * @return A constructed LotroDeck
     */
    public LotroDeck buildDeck(Map<String, Integer> fpCards, Map<String, Integer> shadowCards,
                                int fpFillerCount, int shadowFillerCount) {
        var deck = new LotroDeck("test");
        deck.setTargetFormat(_format.getCode());
        deck.setRingBearer(FOTRFrodo);
        deck.setRing(RulingRing);

        for (var entry : FellowshipSites.entrySet()) {
            if (entry.getKey().startsWith("site")) {
                deck.addSite(entry.getValue());
            }
        }

        if (fpCards != null) {
            for (var entry : fpCards.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    deck.addCard(entry.getKey());
                }
            }
        }

        if (shadowCards != null) {
            for (var entry : shadowCards.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    deck.addCard(entry.getKey());
                }
            }
        }

        addFiller(deck, FP_FILLER_PREFIX, FP_FILLER_START, FP_FILLER_END, fpFillerCount);
        addFiller(deck, SHADOW_FILLER_PREFIX, SHADOW_FILLER_START, SHADOW_FILLER_END, shadowFillerCount);

        return deck;
    }

    /**
     * Builds a deck with a specific site set (no map), plus auto-generated filler.
     */
    public LotroDeck buildDeckWithSites(HashMap<String, String> sites) {
        var deck = new LotroDeck("test");
        deck.setTargetFormat(_format.getCode());
        deck.setRingBearer(FOTRFrodo);
        deck.setRing(RulingRing);

        for (var entry : sites.entrySet()) {
            if (entry.getKey().startsWith("site")) {
                deck.addSite(entry.getValue());
            }
        }

        addFiller(deck, FP_FILLER_PREFIX, FP_FILLER_START, FP_FILLER_END, 30);
        addFiller(deck, SHADOW_FILLER_PREFIX, SHADOW_FILLER_START, SHADOW_FILLER_END, 30);

        return deck;
    }

    /**
     * Builds a deck with a specific map and site set, plus auto-generated filler.
     */
    public LotroDeck buildDeckWithMap(String mapId, HashMap<String, String> sites) {
        var deck = new LotroDeck("test");
        deck.setTargetFormat(_format.getCode());
        deck.setRingBearer(FOTRFrodo);
        deck.setRing(RulingRing);
        deck.setMap(mapId);

        for (var entry : sites.entrySet()) {
            if (entry.getKey().startsWith("site")) {
                deck.addSite(entry.getValue());
            }
        }

        addFiller(deck, FP_FILLER_PREFIX, FP_FILLER_START, FP_FILLER_END, 30);
        addFiller(deck, SHADOW_FILLER_PREFIX, SHADOW_FILLER_START, SHADOW_FILLER_END, 30);

        return deck;
    }

    public boolean hasSiteBlockError(List<String> errors) {
        return errors.stream().anyMatch(e ->
                e.contains("same block") ||
                e.contains("not allowed in Multipath") ||
                e.contains("does not use supported block") ||
                e.contains("cannot mix Movie block and Shadows block sites"));
    }

    public boolean hasMapError(List<String> errors) {
        return errors.stream().anyMatch(DeckValidationScenario::isMapError);
    }

    // The one wording the Maps give their objection, as the deck builder prints it: a sentence with one full stop.
    private static final Pattern MAP_ERROR =
            Pattern.compile("Adventure deck must only contain sites from the (Fellowship|Towers|King) site path\\.(?!\\.)");

    private static boolean isMapError(String error) {
        return MAP_ERROR.matcher(error).find();
    }

    /**
     * The sites that a Map's objection names: the cards of the adventure deck that are not of the Map's site path.
     * Use it to read which sites a Map turned away, where hasMapError only says that it turned some away.
     * @param errors What validate() returned.
     * @return The names of the sites the Map objects to, in the order the objection lists them; empty if the Map
     * has no objection.
     */
    public List<String> getMapErrorSites(List<String> errors) {
        final String listStart = " issues: ";
        for (String error : errors) {
            if (isMapError(error) && error.contains(listStart)) {
                return List.of(error.substring(error.indexOf(listStart) + listStart.length()).split(", "));
            }
        }
        return List.of();
    }

    /**
     * Simplified deck builder for when you only need to test copies of a single card: the copies, and filler to
     * make 30 cards of each side.  The deck's Ring-bearer is Frodo, Son of Drogo 1_290 and its Ring The Ruling Ring
     * 1_2, as in every deck built here, and a format counts the Ring-bearer and the Ring with the draw deck and the
     * sites when it counts copies of one title: four copies of any Frodo in the draw deck are five of "Frodo".
     */
    public LotroDeck buildDeckWithCopies(String cardId, int copies, boolean isFP) {
        var cards = new HashMap<String, Integer>();
        cards.put(cardId, copies);

        if (isFP) {
            return buildDeck(cards, null, 30 - copies, 30);
        } else {
            return buildDeck(null, cards, 30, 30 - copies);
        }
    }

    public List<String> validate(LotroDeck deck) {
        return _format.validateDeck(deck);
    }

    public List<String> validate(LotroDeck deck, DeckValidationContext context) {
        return _format.validateDeck(deck, context);
    }

    /**
     * Every line of every objection, one to an entry and in alphabetical order.  validate() gives the sites'
     * objections as one entry of several lines, in whatever order the deck holds its sites: this is the reading to
     * set beside a list of the objections expected, whichever order they came in.
     * @param errors What validate() returned.
     * @return Each line of each entry that is not empty, sorted.
     */
    public List<String> getErrorLines(List<String> errors) {
        return errors.stream().flatMap(e -> Arrays.stream(e.split("\n"))).filter(l -> !l.isEmpty()).sorted().toList();
    }

    public boolean hasMaxSameNameError(List<String> errors) {
        return errors.stream().anyMatch(e -> e.contains("more of the same card than allowed"));
    }

    public boolean hasMinimumDeckSizeError(List<String> errors) {
        return errors.stream().anyMatch(e -> e.contains("below minimum number of cards"));
    }

    /**
     * Adds filler cards, cycling through IDs in groups of 4 copies each.  An ID is passed over if the format has
     * something against the card itself (it is X-listed), if the format allows fewer than four copies of it (it
     * is R-listed, or limited to two or three), or if an earlier filler card has its title; past four copies of
     * every card left, the cycle starts again and the format will object to the copies.
     */
    private void addFiller(LotroDeck deck, String prefix, int startId, int endId, int count) {
        var fillerIds = new ArrayList<String>();
        var titles = new HashSet<String>();
        for (int id = startId; id <= endId; id++) {
            String blueprintId = _format.applyErrata(prefix + id);
            try {
                String title = VirtualTableScenario.FindCard(blueprintId).getTitle();
                String baseId = VirtualTableScenario._cardLibrary.getBaseBlueprintId(blueprintId);
                boolean limited = _format.getRestrictedCards().contains(baseId) || _format.getRestrictedCardNames().contains(title)
                        || _format.getLimit2Cards().contains(baseId) || _format.getLimit3Cards().contains(baseId);
                if (_format.validateCard(blueprintId) == null && !limited && titles.add(title)) {
                    fillerIds.add(blueprintId);
                }
            } catch (CardNotFoundException notACard) {
                // No such card: nothing to fill with.
            }
        }

        for (int i = 0; i < count; i++) {
            deck.addCard(fillerIds.get((i / 4) % fillerIds.size()));
        }
    }
}
