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
        List<CardCollection.Item> found = _filter.process(filter == null ? "" : filter, new CompleteCardCollection(_library).getAll(), _library, _formats);
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
            json.put("type", blueprint.getCardType() == null ? null : blueprint.getCardType().name().toLowerCase(Locale.ROOT));
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
