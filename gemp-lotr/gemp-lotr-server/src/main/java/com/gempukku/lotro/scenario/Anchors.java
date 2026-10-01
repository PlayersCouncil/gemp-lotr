package com.gempukku.lotro.scenario;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * An anchor is how a scenario refers to something on the game screen without knowing where it is drawn:
 * "card:aragorn", "zone:self.hand", "ui:twilightPool".  The client turns an anchor into an element; the server only
 * needs to know that it is well-formed and, for cards, which card is meant.
 *
 *     card:<alias>                                   one card, wherever it currently is
 *     zone:<self|opp>.<hand|characters|support>      a player's row of cards
 *     zone:minions                                   the row of minions
 *     pile:<self|opp>.<deck|discard|deadPile|adventureDeck|removed|hand>   a pile counter
 *     ui:<name>                                      a fixed part of the screen (see UI below)
 *     ui:site:<n>                                    one site on the adventure path
 *     ui:button:<pass|done|reset>                    a decision button
 *     ui:skirmish:<fpStrength|shadowStrength>        a total in the skirmish box
 *     ui:assignment:<alias>                          the assignment group of a Free Peoples character
 *     ui:stats:<archery|move>                        a figure in the stats box
 *
 * "self" and "opp" are from the point of view of the person playing the scenario.
 */
public final class Anchors {
    private Anchors() { }

    private static final Set<String> UI = Set.of("twilightpool", "phase", "initiative", "ruleof4", "clock", "advpath",
            "alertbox", "skirmish", "stats", "chat", "gamelog", "settings", "dialog");
    private static final Set<String> ROWS = Set.of("hand", "characters", "support");
    private static final Set<String> PILES = Set.of("deck", "discard", "deadpile", "adventuredeck", "removed", "hand");

    public static void check(ScenarioDefinition def, String anchor, String context) throws ScenarioFormatException {
        String[] parts = anchor.trim().split(":");
        String kind = parts[0].toLowerCase(Locale.ROOT);
        switch (kind) {
            case "card" -> {
                expectParts(parts, 2, anchor, context);
                ScenarioParser.card(def, parts[1], context);
            }
            case "zone" -> {
                expectParts(parts, 2, anchor, context);
                if (!parts[1].equalsIgnoreCase("minions"))
                    checkOwned(parts[1], ROWS, anchor, context);
            }
            case "pile" -> {
                expectParts(parts, 2, anchor, context);
                checkOwned(parts[1], PILES, anchor, context);
            }
            case "ui" -> {
                if (parts.length < 2)
                    throw bad(anchor, context);
                String name = parts[1].toLowerCase(Locale.ROOT);
                switch (name) {
                    case "site" -> {
                        expectParts(parts, 3, anchor, context);
                        if (!parts[2].matches("[1-9]"))
                            throw bad(anchor, context);
                    }
                    case "button" -> {
                        expectParts(parts, 3, anchor, context);
                        if (!List.of("pass", "done", "reset").contains(parts[2].toLowerCase(Locale.ROOT)))
                            throw bad(anchor, context);
                    }
                    case "assignment" -> {
                        expectParts(parts, 3, anchor, context);
                        ScenarioParser.card(def, parts[2], context);
                    }
                    case "skirmish" -> {
                        if (parts.length == 3 && !List.of("fpstrength", "shadowstrength").contains(parts[2].toLowerCase(Locale.ROOT)))
                            throw bad(anchor, context);
                        if (parts.length > 3)
                            throw bad(anchor, context);
                    }
                    case "stats" -> {
                        if (parts.length == 3 && !List.of("archery", "move").contains(parts[2].toLowerCase(Locale.ROOT)))
                            throw bad(anchor, context);
                        if (parts.length > 3)
                            throw bad(anchor, context);
                    }
                    default -> {
                        expectParts(parts, 2, anchor, context);
                        if (!UI.contains(name))
                            throw bad(anchor, context);
                    }
                }
            }
            default -> throw bad(anchor, context);
        }
    }

    /**
     * @return The alias of the card an anchor refers to, or null if it does not refer to a card.
     */
    public static String cardAlias(String anchor) {
        String[] parts = anchor.trim().split(":");
        if (parts[0].equalsIgnoreCase("card") && parts.length == 2)
            return parts[1].trim();
        if (parts[0].equalsIgnoreCase("ui") && parts.length == 3 && parts[1].equalsIgnoreCase("assignment"))
            return parts[2].trim();
        return null;
    }

    private static void checkOwned(String text, Set<String> names, String anchor, String context) throws ScenarioFormatException {
        String[] parts = text.split("\\.");
        if (parts.length != 2
                || !(parts[0].equalsIgnoreCase("self") || parts[0].equalsIgnoreCase("opp"))
                || !names.contains(parts[1].toLowerCase(Locale.ROOT)))
            throw bad(anchor, context);
    }

    private static void expectParts(String[] parts, int count, String anchor, String context) throws ScenarioFormatException {
        if (parts.length != count)
            throw bad(anchor, context);
    }

    private static ScenarioFormatException bad(String anchor, String context) {
        return new ScenarioFormatException(context + ": '" + anchor + "' is not an anchor.  Anchors look like card:<alias>, "
                + "zone:self.hand, zone:minions, pile:opp.deck, ui:twilightPool, ui:site:3, ui:button:pass, ui:assignment:<alias>");
    }
}
