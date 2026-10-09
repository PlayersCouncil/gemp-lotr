package com.gempukku.lotro.cards.build.field.effect;

import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Settles, when a definition is loaded, whether an action's effects can be forecast before its costs are paid.
 *
 * An effect is forecast from the board as it stands while the action is on offer (see EffectForecast).  By the
 * time the effect is carried out the action's costs have been paid, and a cost may have made the very thing the
 * effect needed: Celeborn's "Exert Celeborn to heal an [elven] ally" wounds an ally for it to heal.  Where a cost
 * could do that, the action is not judged at all, and so is never flagged as doing nothing.  The reading errs
 * toward not judging: a cost of a kind that is not known here stops it.
 *
 * One kind of action is read apart from that (isARevealAndWhatIsDoneWithTheCards): a reveal from a draw deck that
 * a cost buys, with nothing after it but what is done with the cards revealed.
 */
public final class NoEffectForesight {
    private NoEffectForesight() {
    }

    // Costs that change nothing an effect's choice of cards in play can see.
    private static final Set<String> INERT = Set.of(
            "addtwilight", "removetwilight",
            "addculturetokens", "removeculturetokens", "removetokenscumulative", "removealltokens",
            "discardfromhand", "discardcardatrandomfromhand", "revealcardsfromhand", "revealhand",
            "discardtopcardsfromdeck", "revealtopcardsofdrawdeck",
            "discardstackedcards", "removecardsindiscardfromgame", "removecardsindeadpilefromgame",
            "choosecardsfromdiscard",
            "spot", "chooseactivecards", "chooseanumber", "choosehowmanytospot", "choosehowmanyburdenstospot",
            "choosehowmanythreatstospot", "choosehowmanytwilighttokenstospot", "choosehowmanyfpculturestospot",
            "memorize", "memorizeactive", "memorizenumber", "memorizeinfo", "memorizetitle",
            "incrementperphaselimit", "incrementperturnlimit", "sendmessage");
    // Costs that put wounds on characters or take them off.
    private static final Set<String> WOUNDS = Set.of("exert", "exhaust", "wound", "heal");
    private static final Set<String> BURDENS = Set.of("addburdens", "removeburdens");
    private static final Set<String> THREATS = Set.of("addthreats", "removethreats");
    // Costs that take a card out of play, or out of what filters see of it.  A wound may kill.
    private static final Set<String> REMOVALS = Set.of("discard", "kill", "returntohand", "removefromthegame",
            "stackcardsfromplay", "hinder", "wound");
    // Costs, and effects, that hold others.
    private static final Set<String> WRAPPERS = Set.of("choice", "if", "multiple", "costtoeffect", "optional",
            "repeat", "preventable");
    private static final String[] NESTED = {"effects", "effect", "true", "false", "cost", "instead"};
    // The reveals of cards from a draw deck, and the effects that take cards out of a draw deck by a "select".
    private static final Set<String> DRAW_DECK_REVEALS = Set.of("revealtopcardsofdrawdeck", "revealbottomcardsofdrawdeck");
    private static final Set<String> TAKES_FROM_DRAW_DECK = Set.of("putcardsfromdeckintohand",
            "putcardsfromdeckontopofdeck", "putcardsfromdeckonbottomofdeck", "discardcardsfromdrawdeck");

    private static final class Footprint {
        boolean wounds, burdens, threats, removals, unknown;
        boolean heals, removesBurdens, removesThreats;
        final StringBuilder selects = new StringBuilder();
    }

    /**
     * @param costs The action's costs, as the definition writes them.
     * @param effects The action's effects, as the definition writes them.
     * @return Whether what the effects will find can be told before the costs are paid.
     */
    public static boolean canForesee(JSONObject[] costs, JSONObject[] effects) {
        final Footprint footprint = new Footprint();
        for (JSONObject cost : costs)
            readCost(cost, footprint);
        for (JSONObject effect : effects)
            readEffect(effect, footprint);

        if (footprint.unknown)
            return false;

        final String selects = footprint.selects.toString();
        // A cost that exerts or wounds makes what a heal, or a choice among the wounded, is looking for.
        if (footprint.wounds && (footprint.heals || selects.contains("wound") || selects.contains("vitality") || selects.contains("exhaust")))
            return false;
        if (footprint.burdens && (footprint.removesBurdens || selects.contains("burden") || selects.contains("resistance")))
            return false;
        if (footprint.threats && (footprint.removesThreats || selects.contains("threat")))
            return false;
        // Taking a card out of play can only add to a choice that is made by what is not there.
        if (footprint.removals && selects.contains("not("))
            return false;
        return true;
    }

    /**
     * @param effects Effects that are carried out one after another, as the definition writes them.
     * @return Whether they are one effect, and one that holds no other.  Such an effect, about to be carried out,
     * can say for itself whether it will do anything (EffectForecast.isCertainToDoNothing).  Of several effects,
     * or of one that holds others (a cost and what it buys, the branches of an "if"), the later cannot be asked
     * before the earlier have been carried out: those may make, or remember, the very thing it needs.
     */
    public static boolean isOneEffectHoldingNoOther(JSONObject[] effects) {
        return effects.length == 1 && !WRAPPERS.contains(String.valueOf(effects[0].get("type")).toLowerCase());
    }

    /**
     * Settles whether an action is a reveal from a draw deck and what is then done with the cards revealed, and
     * nothing else: "Exert a Man and hinder a beacon to reveal the top 4 cards of your draw deck.  You may take any
     * [gondor] or [rohan] cards revealed into hand.  Replace the rest in any order."  With nothing in that deck such
     * an action does nothing, whatever it cost (Ketura on Q-0163).
     *
     * The reveal must be the action's first effect and must remember what it reveals.  Every effect after it must be
     * one that can act on those cards only (see actsOnlyOn).  One that does anything else, or that this reading does
     * not know (a wound, a bonus the size of the card revealed, a shuffle), and the answer is false: the action is
     * then judged as any other is.
     * @param costs The action's costs, as the definition writes them.  None of the kinds known here puts a card in
     *              a draw deck; a cost of any other kind might, and the answer is false.
     * @param effects The action's effects, as the definition writes them.
     * @return Whether the action will do nothing if the deck its first effect reveals from is empty while the
     * action is on offer.
     */
    public static boolean isARevealAndWhatIsDoneWithTheCards(JSONObject[] costs, JSONObject[] effects) {
        if (effects.length == 0 || !DRAW_DECK_REVEALS.contains(typeOf(effects[0])))
            return false;
        if (!(effects[0].get("memorize") instanceof String memory))
            return false;

        final Footprint footprint = new Footprint();
        for (JSONObject cost : costs)
            readCost(cost, footprint);
        if (footprint.unknown)
            return false;

        final Set<String> revealed = new HashSet<>();
        revealed.add(memory.toLowerCase());
        for (int i = 1; i < effects.length; i++) {
            if (!actsOnlyOn(revealed, effects[i]))
                return false;
        }
        return true;
    }

    // Whether an effect that follows a reveal can act only on cards of the given memories, each of which holds
    // cards that were revealed and no others, so that with none revealed it does nothing:
    // - a choice among those cards ("ChooseArbitraryCards"), whose own memory then holds revealed cards too;
    // - taking cards from the draw deck that are picked from those cards by its "select", with no shuffle after;
    // - an "if" that asks for a card of a given kind among them: with none revealed there is none, and what is
    //   left of it is its other branch;
    // - a "repeat" once for each of those cards;
    // - an "if" of any other kind, a "repeat", an "optional" with no cost, a "choice" or a "multiple", holding
    //   nothing but such effects.
    private static boolean actsOnlyOn(Set<String> revealed, Object effect) {
        if (!(effect instanceof JSONObject object))
            return false;
        final String type = typeOf(object);
        if (type.equals("choosearbitrarycards")) {
            if (!isOneOf(revealed, object.get("fromMemory")))
                return false;
            if (object.get("memorize") instanceof String chosen)
                revealed.add(chosen.toLowerCase());
            return true;
        }
        if (TAKES_FROM_DRAW_DECK.contains(type))
            return !isSet(object.get("shuffle")) && !isSet(object.get("showAll"))
                    && picksFrom(revealed, object.get("select"));
        if (type.equals("if")) {
            if (asksForACardOf(revealed, object.get("check")))
                return allActOnlyOn(revealed, object.get("false"));
            return allActOnlyOn(revealed, object.get("true")) && allActOnlyOn(revealed, object.get("false"));
        }
        if (type.equals("repeat")) {
            if (object.get("times") instanceof JSONObject times && typeOf(times).equals("foreachinmemory")
                    && isOneOf(revealed, times.get("memory")) && hasNoKeyBut(times, "type", "memory", "filter"))
                return true;
            return allActOnlyOn(revealed, object.get("effect"));
        }
        if (type.equals("optional"))
            return isAbsent(object.get("cost")) && allActOnlyOn(revealed, object.get("effect"));
        if (type.equals("choice") || type.equals("multiple"))
            return allActOnlyOn(revealed, object.get("effects"));
        return false;
    }

    // Every effect in what a definition writes under a key: one effect, a list of them, or (the options of a
    // choice) a list of lists.  Nothing written is nothing done.
    private static boolean allActOnlyOn(Set<String> revealed, Object effects) {
        final boolean[] all = {true};
        forEachObject(effects, effect -> all[0] &= actsOnlyOn(revealed, effect));
        return all[0];
    }

    // Whether one of the checks of an "if", all of which must hold, is that a card of some kind is among the cards
    // of one of the given memories ("MemoryMatches", with the count of 1 it has when none is written).
    private static boolean asksForACardOf(Set<String> memories, Object checks) {
        final boolean[] any = {false};
        forEachObject(checks, check -> any[0] |= check instanceof JSONObject object
                && typeOf(object).equals("memorymatches") && isOneOf(memories, object.get("memory"))
                && object.get("count") == null);
        return any[0];
    }

    // Whether a "select" picks among the cards of one of the given memories and no others: "memory(x)" itself,
    // or "all(...)" or "choose(...)" of a filter one of whose parts, all of which must hold, is "memory(x)".
    private static boolean picksFrom(Set<String> memories, Object select) {
        if (!(select instanceof String text))
            return false;
        String filter = text.toLowerCase().replace(" ", "");
        if ((filter.startsWith("all(") || filter.startsWith("choose(")) && filter.endsWith(")"))
            filter = filter.substring(filter.indexOf('(') + 1, filter.length() - 1);
        int depth = 0;
        int start = 0;
        for (int i = 0; i <= filter.length(); i++) {
            final char c = i < filter.length() ? filter.charAt(i) : ',';
            if (c == '(')
                depth++;
            else if (c == ')')
                depth--;
            else if (c == ',' && depth == 0) {
                final String part = filter.substring(start, i);
                if (part.startsWith("memory(") && part.endsWith(")")
                        && memories.contains(part.substring("memory(".length(), part.length() - 1)))
                    return true;
                start = i + 1;
            }
        }
        return false;
    }

    private static String typeOf(JSONObject object) {
        return String.valueOf(object.get("type")).toLowerCase();
    }

    private static boolean isOneOf(Set<String> memories, Object name) {
        return name instanceof String text && memories.contains(text.toLowerCase());
    }

    // Whether a field that is false when it is not written has been written as anything but false.
    private static boolean isSet(Object value) {
        return value != null && !String.valueOf(value).equalsIgnoreCase("false");
    }

    private static boolean isAbsent(Object value) {
        return value == null || (value instanceof Collection<?> collection && collection.isEmpty());
    }

    private static boolean hasNoKeyBut(JSONObject object, String... keys) {
        return Set.of(keys).containsAll(object.keySet());
    }

    private static void readCost(Object cost, Footprint footprint) {
        if (!(cost instanceof JSONObject object)) {
            footprint.unknown = true;
            return;
        }
        final String type = String.valueOf(object.get("type")).toLowerCase();
        if (WRAPPERS.contains(type)) {
            for (String key : NESTED)
                forEachObject(object.get(key), nested -> readCost(nested, footprint));
            return;
        }
        boolean known = INERT.contains(type);
        if (WOUNDS.contains(type)) {
            footprint.wounds = true;
            known = true;
        }
        if (BURDENS.contains(type)) {
            footprint.burdens = true;
            known = true;
        }
        if (THREATS.contains(type)) {
            footprint.threats = true;
            known = true;
        }
        if (REMOVALS.contains(type)) {
            footprint.removals = true;
            known = true;
        }
        // Anything else may play a card, bring one into play or put one in a deck, and is not looked into.
        if (!known)
            footprint.unknown = true;
    }

    // Reads everything an effect says, and everything said by what it holds: what kinds of effect there are,
    // the costs written inside them, and every word of their selects, filters and checks.  The names of types
    // are left out of the words: "wound" as a kind of effect is not a choice among the wounded.
    private static void readEffect(Object effect, Footprint footprint) {
        if (effect instanceof JSONObject object) {
            final String type = String.valueOf(object.get("type")).toLowerCase();
            if (type.equals("heal"))
                footprint.heals = true;
            if (type.equals("removeburdens"))
                footprint.removesBurdens = true;
            if (type.equals("removethreats"))
                footprint.removesThreats = true;

            for (Object entryObject : object.entrySet()) {
                final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
                final String key = String.valueOf(entry.getKey());
                if (key.equals("type"))
                    continue;
                // A cost inside an effect ("you may exert X to ...") is paid before the rest of that effect.
                if (key.equals("cost"))
                    forEachObject(entry.getValue(), nested -> readCost(nested, footprint));
                readEffect(entry.getValue(), footprint);
            }
        } else if (effect instanceof Collection<?> collection) {
            for (Object element : collection)
                readEffect(element, footprint);
        } else if (effect instanceof Object[] array) {
            for (Object element : array)
                readEffect(element, footprint);
        } else if (effect instanceof String text) {
            footprint.selects.append(text.toLowerCase()).append(' ');
        }
    }

    private static void forEachObject(Object value, java.util.function.Consumer<Object> consumer) {
        if (value == null)
            return;
        if (value instanceof Collection<?> collection) {
            for (Object element : collection)
                forEachObject(element, consumer);
        } else if (value instanceof Object[] array) {
            for (Object element : array)
                forEachObject(element, consumer);
        } else {
            consumer.accept(value);
        }
    }
}
