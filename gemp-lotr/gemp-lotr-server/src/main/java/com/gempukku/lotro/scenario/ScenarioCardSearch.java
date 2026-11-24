package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.CompleteCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.GameUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Finds cards the way the deck builder does: its own filter language (SortAndFilterCards, the 'filter' string the
 * collection calls take: set, side, culture, card type, keyword, name words, sort and so on), over every card in the
 * library once, as the deck builder's "all cards" view has them (CompleteCardCollection: the base printing of each
 * card, never an alternate printing; the format's errata are applied when a card goes into a game).  For the
 * sandbox's + and the authoring panel's search alike, so that what the page finds and what the server takes can never
 * disagree; nothing here needs a game or a draft.
 */
public final class ScenarioCardSearch {
    /** The most cards one call answers. */
    public static final int MOST = 200;

    private final LotroCardBlueprintLibrary _library;
    private final LotroFormatLibrary _formats;
    private final SortAndFilterCards _filter = new SortAndFilterCards();

    public ScenarioCardSearch(LotroCardBlueprintLibrary library, LotroFormatLibrary formats) {
        _library = library;
        _formats = formats;
    }

    /**
     * @param filter A deck builder filter ("cardType:COMPANION culture:ELVEN sort:name", and so on); null or empty for
     *               every card.
     * @param start How many of the cards found to skip.
     * @param count How many to answer, at most MOST.
     * @return {total, start, cards: [{blueprintId, name, type, side, culture, site, siteNumber, twilight}]}
     */
    public Map<String, Object> search(String filter, int start, int count) {
        return answer(found(filter, _formats), start, count);
    }

    /** What a search was asked that cannot be answered: a filter that is not one, said for the person. */
    public static final class Refused extends Exception {
        Refused(String message) {
            super(message);
        }
    }

    /**
     * A search kept inside a bound: the cards the typed filter finds that the pool and the bound each find as well.
     * Each of the three is searched with by itself and the answers are put together (the cards all three find, in the
     * order the typed filter's search gives, its sort: included), because the deck builder's text reads a key said
     * twice as "either": "culture:ROHAN" in a bound and "culture:GONDOR" typed would find both cultures as one text.
     * So a person's typing can narrow what a slot takes and never widen it, whatever it says.
     * @param pool A puzzle's pool, or null; read strictly, as a bound is (see SlotBounds.filterProblem).
     * @param bound A slot's filter, or null; read strictly.
     * @param strict Whether the typed filter is read strictly too: a key the search does not know, or a value written
     *               otherwise than it writes it, is refused rather than ignored (format: is allowed in it).
     * @throws Refused saying what is wrong with whichever filter is not one.
     */
    public Map<String, Object> search(String filter, String pool, String bound, boolean strict, int start, int count) throws Refused {
        if (strict && filter != null && !filter.isBlank()) {
            //(its format: is read with the formats, as the deck builder reads it; the rest as a bound is)
            String problem = SlotBounds.filterProblem(filter.replaceAll("(?i)(^|\\s)format:\\S*", " ").trim(), _library, false);
            if (problem != null)
                throw new Refused("filter: " + problem);
        }
        for (String[] limit : List.of(new String[] {"pool", pool}, new String[] {"bound", bound})) {
            String problem = SlotBounds.filterProblem(blank(limit[1]), _library);
            if (problem != null)
                throw new Refused(limit[0] + ": " + problem);
        }
        List<CardCollection.Item> found = found(filter, _formats);
        found = within(found, blank(pool));
        found = within(found, blank(bound));
        return answer(found, start, count);
    }

    /**
     * Whether a filter is a bound a slot can be given, and how many cards it finds: {ok, why, total}.  As strict as
     * saving the slot is (SlotBounds.filterProblem): ok false and why the validator's words, total null.  With a pool,
     * total is how many cards both find, each searched with by itself, as a slot's search and its check do.
     */
    public Map<String, Object> bound(String filter, String pool) {
        Map<String, Object> result = new LinkedHashMap<>();
        String problem = SlotBounds.filterProblem(blank(filter), _library);
        if (problem == null && blank(pool) != null) {
            problem = SlotBounds.filterProblem(pool, _library);
            if (problem != null)
                problem = "pool: " + problem;
        }
        result.put("ok", problem == null);
        result.put("why", problem);
        //(a bound is searched with no format, as the server checks a pick: format names mean nothing in one)
        result.put("total", problem == null ? within(found(filter, null), blank(pool)).size() : null);
        return result;
    }

    private static String blank(String filter) {
        return filter == null || filter.isBlank() ? null : filter.trim();
    }

    private List<CardCollection.Item> found(String filter, LotroFormatLibrary formats) {
        return _filter.process(filter == null ? "" : filter, new CompleteCardCollection(_library).getAll(), _library, formats);
    }

    /** Those of the cards that a filter finds as well, searched with by itself (with no format: see bound); all of them for none. */
    private List<CardCollection.Item> within(List<CardCollection.Item> cards, String filter) {
        if (filter == null)
            return cards;
        java.util.Set<String> also = new java.util.HashSet<>();
        for (CardCollection.Item item : _filter.process(filter, new CompleteCardCollection(_library).getAll(), _library, null))
            also.add(item.getBlueprintId());
        List<CardCollection.Item> both = new ArrayList<>();
        for (CardCollection.Item item : cards)
            if (also.contains(item.getBlueprintId()))
                both.add(item);
        return both;
    }

    private Map<String, Object> answer(List<CardCollection.Item> found, int start, int count) {
        List<Map<String, Object>> cards = new ArrayList<>();
        int from = Math.max(0, start);
        int until = Math.min(found.size(), from + Math.max(0, Math.min(count, MOST)));
        for (int i = from; i < until; i++) {
            String blueprintId = found.get(i).getBlueprintId();
            LotroCardBlueprint blueprint;
            try {
                blueprint = _library.getLotroCardBlueprint(blueprintId);
            } catch (CardNotFoundException exp) {
                continue;
            }
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("blueprintId", blueprintId);
            json.put("name", GameUtils.getFullName(blueprint));
            //(the card type as the sandbox's state and the Board tab write it, CardType's own name: "COMPANION")
            json.put("type", blueprint.getCardType() == null ? null : blueprint.getCardType().name());
            json.put("side", blueprint.getSide() == null ? null : blueprint.getSide().name().toLowerCase(Locale.ROOT));
            json.put("culture", blueprint.getCulture() == null ? null : blueprint.getCulture().name().toLowerCase(Locale.ROOT));
            boolean site = blueprint.getCardType() == CardType.SITE;
            json.put("site", site);
            //(the number printed on a site: 0 for a site of the Shadows block on, which has none)
            json.put("siteNumber", site ? blueprint.getSiteNumber() : null);
            json.put("twilight", site ? null : blueprint.getTwilightCost());
            cards.add(json);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", found.size());
        result.put("start", from);
        result.put("cards", cards);
        return result;
    }
}
