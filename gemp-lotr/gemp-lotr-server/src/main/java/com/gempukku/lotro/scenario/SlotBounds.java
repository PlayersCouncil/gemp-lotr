package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.BasicCardItem;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.scenario.ScenarioDefinition.Slot;
import com.gempukku.lotro.scenario.ScenarioDefinition.SlotZone;
import com.gempukku.lotro.scenario.ScenarioDefinition.SetupZone;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * What a puzzle's slots may hold, and whether a pick fits.
 *
 * A slot's bound and the puzzle's pool are written in the deck builder's own filter language (the text the collection
 * search takes: set:1-6 culture:ROHAN cardtype:CONDITION), and a pick is checked with the deck builder's own search
 * (SortAndFilterCards), so the page's search and the server's check cannot disagree.  The pool and the slot's filter
 * are each searched with by themselves, and a card fits if both find it: written as one text, a key said in both
 * (cardtype in the pool and in the slot) would be either of them rather than both.
 *
 * Beyond its bound, a pick has to be a card the game could have there: a site in a site's slot and nothing else
 * there, a companion or ally among the characters, a minion among the minions, an item or condition attached, no
 * One Ring anywhere; and no two copies of a unique card in play for the same player, counting the cards the setup
 * already has there.  The game itself refuses those, and nothing else is held against a pick.
 */
public final class SlotBounds {
    private SlotBounds() { }

    /** The keys the deck builder's filter understands (SortAndFilterCards), as they are written. */
    private static final Set<String> KEYS = Set.of("product", "rarity", "side", "sort", "block", "set", "cardtype", "culture", "name",
            "gametext", "keyword", "phase", "race", "itemclass", "twilight", "twilightcompare", "sitenumber", "sitenumbercompare",
            "strength", "strengthcompare", "vitality", "vitalitycompare", "resistance", "resistancecompare", "signet", "canstartwithring",
            "blueprints");

    private static final SortAndFilterCards SEARCH = new SortAndFilterCards();

    /** Where on the table a card is in play, for uniqueness: a card in hand or in a pile is not. */
    private static final Set<SlotZone> IN_PLAY = EnumSet.of(SlotZone.CHARACTERS, SlotZone.SUPPORT, SlotZone.MINIONS, SlotZone.ATTACHED);

    /**
     * @return What is wrong with a filter, said for an author; null if the deck builder's search understands it.
     */
    public static String filterProblem(String filter, LotroCardBlueprintLibrary library) {
        if (filter == null)
            return null;
        for (String param : filter.trim().split(" +")) {
            if (param.isEmpty())
                continue;
            int colon = param.indexOf(':');
            if (colon <= 0 || colon == param.length() - 1)
                return "'" + param + "' is not a part of a search: each part is key:value (set:1-6, culture:ROHAN, cardtype:CONDITION, name:Aldor)";
            String key = param.substring(0, colon).toLowerCase(Locale.ROOT);
            if (key.equals("format"))
                return "'" + param + "': a slot is bounded by sets (set:4-6), not by a format, whose sets the scenario's own format already decides";
            if (!KEYS.contains(key))
                return "'" + param + "': the deck builder's search has no '" + key + "' (it has " + String.join(", ", KEYS.stream().sorted().toList()) + ")";
            if (key.equals("set") || key.equals("block"))
                for (String set : param.substring(colon + 1).split(","))
                    if (!set.matches("\\d+(-\\d+)?"))
                        return "'" + param + "': a set is a number or a range of them (set:1-6, set:4,7)";
        }
        try {
            SEARCH.process(filter, List.of(new BasicCardItem("1_2")), library, null);
        } catch (IllegalArgumentException exp) {
            //(an enumeration the search knows by its names, written otherwise: cardtype:condition for CONDITION)
            return "a value in it is not one the search knows (" + exp.getMessage() + "): card types, cultures and the like are written "
                    + "as the search writes them, in capitals (cardtype:CONDITION, culture:ROHAN)";
        } catch (RuntimeException exp) {
            return "the deck builder's search could not read it: " + exp;
        }
        return null;
    }

    /**
     * The search that finds what a slot may hold, as one text for the page's search window: the pool, and the slot's
     * own filter after it.  Where the two say the same key, one text would find either rather than both: see
     * Overlap, which the page then has to send as two.
     */
    public static String bound(ScenarioDefinition def, Slot slot) {
        List<String> parts = new ArrayList<>();
        if (def.pool != null)
            parts.add(def.pool);
        if (slot.filter() != null)
            parts.add(slot.filter());
        return String.join(" ", parts);
    }

    /** The keys the pool and a slot's filter both say: a search written as one text would find either for them. */
    public static List<String> overlap(ScenarioDefinition def, Slot slot) {
        List<String> both = new ArrayList<>();
        if (def.pool == null || slot.filter() == null)
            return both;
        Set<String> pool = keys(def.pool);
        for (String key : keys(slot.filter()))
            if (pool.contains(key))
                both.add(key);
        return both;
    }

    private static Set<String> keys(String filter) {
        Set<String> keys = new java.util.LinkedHashSet<>();
        for (String param : filter.trim().split(" +"))
            if (param.indexOf(':') > 0)
                keys.add(param.substring(0, param.indexOf(':')).toLowerCase(Locale.ROOT));
        return keys;
    }

    /** Whether the deck builder's search, given a filter, finds a card. */
    static boolean finds(String filter, String blueprintId, LotroCardBlueprintLibrary library) {
        if (filter == null)
            return true;
        try {
            return !SEARCH.process(filter, List.of(new BasicCardItem(blueprintId)), library, null).isEmpty();
        } catch (RuntimeException exp) {
            //(a filter the search cannot read finds nothing: the validator says what is wrong with it)
            return false;
        }
    }

    /**
     * Whether a card may go in a slot, and if not, why, in plain words for the solver.
     * @param picks The other slots' picks, for uniqueness (the slot's own is ignored).
     * @return Null if it fits.
     */
    public static String why(ScenarioDefinition def, String slotId, String blueprintId, Map<String, String> picks, LotroCardBlueprintLibrary library) {
        Slot slot = def.slots.get(slotId);
        if (slot == null)
            return "There is no slot called '" + slotId + "'.";
        if (blueprintId == null || !blueprintId.trim().matches("\\d+_\\d+[*T]?"))
            return "'" + blueprintId + "' is not a card.";
        blueprintId = blueprintId.trim();
        LotroCardBlueprint card;
        try {
            card = library.getLotroCardBlueprint(blueprintId);
        } catch (CardNotFoundException exp) {
            card = null;
        }
        if (card == null)
            return "There is no card " + blueprintId + ".";
        String name = GameUtils.getFullName(card);
        String kind = kindProblem(slot, card.getCardType());
        if (kind != null)
            return name + " cannot go here: " + kind + ".";
        if (slot.zone() == SlotZone.SITE) {
            //(a site takes the place on the path of the site the board shows: one of the same number)
            ScenarioDefinition.CardDef shown = def.siteCards.get(slot.id());
            int number = 0;
            try {
                LotroCardBlueprint site = shown == null ? null : library.getLotroCardBlueprint(shown.blueprintId());
                number = site == null ? 0 : site.getSiteNumber();
            } catch (CardNotFoundException ignored) {
                //(the validator says so)
            }
            if (number > 0 && card.getSiteNumber() != number)
                return name + " is site " + card.getSiteNumber() + ", and this slot is site " + number + " of the path.";
        }
        if (!finds(def.pool, blueprintId, library))
            return name + " is not one of the cards this puzzle is played with (" + def.pool + ").";
        if (!finds(slot.filter(), blueprintId, library))
            return name + " is not " + (slot.label() != null ? slot.label() : "a card this slot takes") + " (the slot takes what the search "
                    + slot.filter() + " finds).";
        if (card.isUnique() && IN_PLAY.contains(slot.zone())) {
            String title = card.getTitle();
            for (String other : inPlay(def, slot.seat(), picks, slotId, library))
                if (other.equals(title))
                    return name + " is unique, and " + (slot.seat() == def.human ? "you have" : "the opponent has") + " a " + title
                            + " in play already: the game allows only one.";
        }
        return null;
    }

    /** Why a card of a kind cannot be in a slot's place at all; null if it can. */
    private static String kindProblem(Slot slot, CardType type) {
        if (type == CardType.THE_ONE_RING)
            return "The One Ring is the Ring-bearer's, not a card to pick";
        if (slot.zone() == SlotZone.SITE)
            return type == CardType.SITE ? null : "this slot is for a site";
        if (type == CardType.SITE)
            return "a site goes on the adventure path, and this slot is not a site's";
        if (type == CardType.MAP || type == CardType.METASITE || type == CardType.ADVENTURE)
            return "it is not a card that goes in a deck";
        return switch (slot.zone()) {
            case CHARACTERS -> type == CardType.COMPANION || type == CardType.ALLY ? null : "only a companion or an ally is among the characters";
            case MINIONS -> type == CardType.MINION ? null : "only a minion is among the minions";
            case SUPPORT -> type == CardType.CONDITION || type == CardType.POSSESSION || type == CardType.ARTIFACT || type == CardType.ALLY
                    || type == CardType.FOLLOWER ? null : "the support area holds conditions, possessions, artifacts, allies and followers";
            case ATTACHED -> type == CardType.CONDITION || type == CardType.POSSESSION || type == CardType.ARTIFACT || type == CardType.FOLLOWER
                    ? null : "only a condition, a possession, an artifact or a follower is borne by another card";
            default -> null;
        };
    }

    /**
     * The titles of a seat's unique cards in play when the game begins: the setup's (its characters, support area,
     * minions, attached cards, the Ring-bearer) and the other slots' picks there.
     */
    private static List<String> inPlay(ScenarioDefinition def, Seat seat, Map<String, String> picks, String except, LotroCardBlueprintLibrary library) {
        List<String> blueprints = new ArrayList<>();
        Map<SetupZone, List<String>> zones = def.setup.zones.getOrDefault(seat, Map.of());
        List<String> aliases = new ArrayList<>();
        for (SetupZone zone : List.of(SetupZone.CHARACTERS, SetupZone.SUPPORT, SetupZone.MINIONS))
            aliases.addAll(zones.getOrDefault(zone, List.of()));
        aliases.addAll(def.setup.attach.keySet());
        if (def.ringBearer.get(seat) != null)
            aliases.add(def.ringBearer.get(seat));
        for (String alias : aliases) {
            ScenarioDefinition.CardDef card = def.cards.get(alias);
            if (card != null && card.owner() == seat)
                blueprints.add(card.blueprintId());
        }
        for (Map.Entry<String, String> pick : picks.entrySet()) {
            Slot other = def.slots.get(pick.getKey());
            if (other != null && !other.id().equals(except) && other.seat() == seat && IN_PLAY.contains(other.zone()))
                blueprints.add(pick.getValue());
        }
        List<String> titles = new ArrayList<>();
        for (String blueprint : blueprints) {
            try {
                LotroCardBlueprint card = library.getLotroCardBlueprint(blueprint);
                if (card != null && card.isUnique())
                    titles.add(card.getTitle());
            } catch (CardNotFoundException ignored) {
                //(the validator says so)
            }
        }
        return titles;
    }

    /**
     * Every pick checked: slot id -> why it does not fit (null where it does), in the order of the slots.  A slot
     * with no pick says so when every slot is asked about.
     * @param all Whether every slot has to be picked (a Submit); otherwise only those picked are looked at.
     */
    public static Map<String, String> check(ScenarioDefinition def, Map<String, String> picks, boolean all, LotroCardBlueprintLibrary library) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String id : picks.keySet())
            if (!def.slots.containsKey(id))
                result.put(id, "There is no slot called '" + id + "'.");
        for (Slot slot : def.slots.values()) {
            String pick = picks.get(slot.id());
            if (pick == null || pick.isBlank()) {
                if (all)
                    result.put(slot.id(), (slot.label() != null ? "The slot for " + slot.label() : "The slot " + slot.id()) + " is empty.");
                continue;
            }
            result.put(slot.id(), why(def, slot.id(), pick, picks, library));
        }
        return result;
    }

    /** Whether every pick fits (see check). */
    public static boolean allFit(Map<String, String> checked) {
        return checked.values().stream().allMatch(java.util.Objects::isNull);
    }

    /**
     * Whether two sets of picks put the same cards in the slots: the same card by its name, so that a reprint or an
     * errata'd version of a card listed in a solution is that card.
     */
    public static boolean same(Map<String, String> one, Map<String, String> other, LotroCardBlueprintLibrary library) {
        if (!one.keySet().equals(other.keySet()))
            return false;
        for (Map.Entry<String, String> pick : one.entrySet())
            if (!nameOf(pick.getValue(), library).equals(nameOf(other.get(pick.getKey()), library)))
                return false;
        return true;
    }

    /** A card's full name ("Aldor, Soldier of Edoras"); its blueprint where there is no such card. */
    public static String nameOf(String blueprintId, LotroCardBlueprintLibrary library) {
        try {
            LotroCardBlueprint card = library.getLotroCardBlueprint(blueprintId);
            return card == null ? blueprintId : GameUtils.getFullName(card);
        } catch (CardNotFoundException | RuntimeException exp) {
            return blueprintId;
        }
    }

    /**
     * What is wrong with a puzzle's slots, pool and solutions that the parser does not see: filters the search does
     * not understand, a slot whose place does not fit its seat, a solution's pick that its slot does not take.
     */
    static List<String> validate(ScenarioDefinition def, LotroCardBlueprintLibrary library) {
        List<String> problems = new ArrayList<>();
        String pool = filterProblem(def.pool, library);
        if (pool != null)
            problems.add("pool: " + pool);
        for (Slot slot : def.slots.values()) {
            String context = "slots." + slot.id();
            String filter = filterProblem(slot.filter(), library);
            if (filter != null)
                problems.add(context + ".filter: " + filter);
            if (slot.zone() == SlotZone.MINIONS && slot.seat() == Seat.FREEPS)
                problems.add(context + ": the Free Peoples seat is the Free Peoples player when the game begins, so none of its minions can be on "
                        + "the table (seat: shadow)");
            if (slot.zone() == SlotZone.CHARACTERS && slot.seat() == Seat.SHADOW && def.oneSided)
                problems.add(context + ": in a one-sided game the Shadow seat has no characters of its own on the table (seat: freeps, or zone: minions)");
            if (slot.on() != null && def.cards.get(slot.on()) != null && def.cards.get(slot.on()).owner() != slot.seat()
                    && slot.zone() == SlotZone.STACKED)
                problems.add(context + ".on: a card is stacked on one of its own player's cards; '" + slot.on() + "' is the other seat's");
        }
        //A solution's picks have to be cards its slots take, or the solution is not one
        for (int i = 0; i < def.solutions.size(); i++) {
            ScenarioDefinition.Solution solution = def.solutions.get(i);
            Map<String, String> checked = check(def, solution.picks(), true, library);
            for (Map.Entry<String, String> one : checked.entrySet())
                if (one.getValue() != null)
                    problems.add("solutions[" + (i + 1) + "].picks." + one.getKey() + ": " + one.getValue());
        }
        return problems;
    }
}
