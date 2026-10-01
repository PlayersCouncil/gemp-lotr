package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message text can quote the game instead of hard-coding a number that a later change to a card would make wrong:
 *
 *     {twilight} {burdens} {threats} {site} {turn}                 totals
 *     {strength:x} {vitality:x} {wounds:x} {cost:x} {title:x}      a card, by alias
 *
 * Strength, vitality and cost are the current values, with everything that modifies them applied.  Vitality is what
 * the character has left, i.e. how many more wounds would kill it.
 */
public final class Templates {
    private Templates() { }

    private static final Pattern TEMPLATE = Pattern.compile("\\{([A-Za-z]+)(?::([^{}]+))?}");
    private static final Set<String> TOTALS = Set.of("twilight", "burdens", "threats", "site", "turn");
    private static final Set<String> PER_CARD = Set.of("strength", "vitality", "wounds", "cost", "title");

    public static void check(ScenarioDefinition def, String text, String context) throws ScenarioFormatException {
        Matcher matcher = TEMPLATE.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            String alias = matcher.group(2);
            if (alias == null) {
                if (!TOTALS.contains(name))
                    throw new ScenarioFormatException(context + ": don't know how to fill in " + matcher.group()
                            + " (totals are " + String.join(", ", TOTALS.stream().sorted().toList()) + ")");
            } else {
                if (!PER_CARD.contains(name))
                    throw new ScenarioFormatException(context + ": don't know how to fill in " + matcher.group()
                            + " (per card there are " + String.join(", ", PER_CARD.stream().sorted().toList()) + ")");
                ScenarioParser.card(def, alias, context);
            }
        }
    }

    public static String render(ScenarioTable table, String text) {
        Matcher matcher = TEMPLATE.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            String alias = matcher.group(2);
            String value;
            if (alias == null) {
                value = switch (name) {
                    case "twilight" -> String.valueOf(table.GetTwilight());
                    case "burdens" -> String.valueOf(table.GetBurdens());
                    case "threats" -> String.valueOf(table.GetThreats());
                    case "site" -> String.valueOf(table.GetCurrentSiteNumber());
                    case "turn" -> String.valueOf(table.gameState().getTurnNumber());
                    default -> matcher.group();
                };
            } else {
                PhysicalCardImpl card = table.Card(alias.trim());
                value = switch (name) {
                    case "strength" -> String.valueOf(table.GetStrength(card));
                    case "vitality" -> String.valueOf(table.GetVitality(card));
                    case "wounds" -> String.valueOf(table.GetWoundsOn(card));
                    case "cost" -> String.valueOf(table.GetTwilightCost(card));
                    case "title" -> GameUtils.getFullName(card);
                    default -> matcher.group();
                };
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
