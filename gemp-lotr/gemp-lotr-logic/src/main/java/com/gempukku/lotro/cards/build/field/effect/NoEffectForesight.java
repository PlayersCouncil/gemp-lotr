package com.gempukku.lotro.cards.build.field.effect;

import org.json.simple.JSONObject;

import java.util.Collection;
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
    // Costs that hold other costs.
    private static final Set<String> WRAPPERS = Set.of("choice", "if", "multiple", "costtoeffect", "optional",
            "repeat", "preventable");
    private static final String[] NESTED = {"effects", "effect", "true", "false", "cost", "instead"};

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
