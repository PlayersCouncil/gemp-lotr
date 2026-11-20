package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message text can quote the game instead of hard-coding a number that a later change to a card would make wrong:
 *
 *     {twilight} {burdens} {threats} {site} {turn}                 totals
 *     {hand}                                                       how many cards the person has in hand
 *     {hand:seat}                                                  ...or a seat: {hand:shadow}, {hand:freeps},
 *                                                                  {hand:opponent}, {hand:human}
 *     {selfStrength} {oppStrength}                                 the person's side's total in the skirmish being
 *                                                                  fought, and the other side's ({fpStrength} and
 *                                                                  {shadowStrength}, as they were once written, are
 *                                                                  read as these)
 *     {strength:x} {vitality:x} {resistance:x} {wounds:x}          a card, by alias
 *     {tokens:x} {cost:x} {playCost:x} {title:x}
 *
 * Strength, vitality and resistance are the current values, with everything that modifies them applied.  Vitality is
 * what the character has left, i.e. how many more wounds would kill it.  Tokens are the culture tokens of the card's
 * own culture on it.
 *
 * {cost:x} is the card's twilight cost as the game reads it where the card is (VirtualTable.GetTwilightCost): for a
 * card in a hand or a pile, what it would cost to play with every modifier applied, the roaming penalty apart; for a
 * card in play, the number printed on it.  {playCost:x} is what playing it now would take from the twilight pool:
 * the same, plus the roaming penalty where the game would charge one (a minion whose site number is higher than the
 * fellowship's site).  The two differ only for a roaming minion.
 */
public final class Templates {
    private Templates() { }

    private static final Pattern TEMPLATE = Pattern.compile("\\{([A-Za-z]+)(?::([^{}]+))?}");
    private static final Set<String> TOTALS = Set.of("twilight", "burdens", "threats", "site", "turn", "selfstrength", "oppstrength", "fpstrength", "shadowstrength", "hand");
    private static final Set<String> PER_CARD = Set.of("strength", "vitality", "wounds", "cost", "title", "resistance", "tokens", "playcost");
    private static final Set<String> SEATS = Set.of("freeps", "shadow", "human", "opponent");

    /**
     * What can be filled in, as it is written, for the authoring tool to offer: {"totals": [...], "perCard": [...],
     * "perSeat": [...]} ({twilight}; {strength:x}; {hand:shadow}).  The old names (fpStrength, shadowStrength) are
     * left out: they are read, not written.
     */
    public static java.util.Map<String, java.util.List<String>> names() {
        java.util.Map<String, java.util.List<String>> names = new java.util.LinkedHashMap<>();
        names.put("totals", java.util.List.of("twilight", "burdens", "threats", "site", "turn", "hand", "selfStrength", "oppStrength"));
        names.put("perCard", java.util.List.of("strength", "vitality", "resistance", "wounds", "tokens", "cost", "playCost", "title"));
        names.put("perSeat", java.util.List.of("hand"));
        return names;
    }

    public static void check(ScenarioDefinition def, String text, String context) throws ScenarioFormatException {
        Matcher matcher = TEMPLATE.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            String alias = matcher.group(2);
            if (alias == null) {
                if (!TOTALS.contains(name))
                    throw new ScenarioFormatException(context + ": don't know how to fill in " + matcher.group()
                            + " (totals are " + String.join(", ", TOTALS.stream().sorted().toList()) + ")");
            } else if (name.equals("hand")) {
                if (!SEATS.contains(alias.trim().toLowerCase()))
                    throw new ScenarioFormatException(context + ": " + matcher.group() + ": a hand is a seat's (freeps, shadow, human, opponent)");
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
                    case "hand" -> String.valueOf(table.gameState().getHand(table.HumanPlayer()).size());
                    case "fpstrength", "shadowstrength", "selfstrength", "oppstrength" -> {
                        boolean selfIsFreeps = table.Definition().human == Seat.FREEPS;
                        boolean freeps = switch (name) {
                            case "fpstrength" -> true;
                            case "shadowstrength" -> false;
                            case "selfstrength" -> selfIsFreeps;
                            default -> !selfIsFreeps;
                        };
                        yield table.gameState().getSkirmish() == null ? "0"
                                : String.valueOf(freeps ? RuleUtils.getFellowshipSkirmishStrength(table.game()) : RuleUtils.getShadowSkirmishStrength(table.game()));
                    }
                    default -> matcher.group();
                };
            } else if (name.equals("hand")) {
                Seat seat = switch (alias.trim().toLowerCase()) {
                    case "freeps" -> Seat.FREEPS;
                    case "shadow" -> Seat.SHADOW;
                    case "human" -> table.Definition().human;
                    default -> table.Definition().human.other();
                };
                value = String.valueOf(table.gameState().getHand(table.Player(seat)).size());
            } else {
                PhysicalCardImpl card = table.Card(alias.trim());
                value = switch (name) {
                    case "strength" -> String.valueOf(table.GetStrength(card));
                    case "vitality" -> String.valueOf(table.GetVitality(card));
                    case "wounds" -> String.valueOf(table.GetWoundsOn(card));
                    case "cost" -> String.valueOf(table.GetTwilightCost(card));
                    case "playcost" -> String.valueOf(table.game().getModifiersQuerying().getTwilightCostToPlay(table.game(), card, null, 0, false));
                    case "resistance" -> String.valueOf(table.GetResistance(card));
                    case "tokens" -> {
                        var culture = card.getBlueprint().getCulture();
                        var token = culture == null ? null : com.gempukku.lotro.common.Token.findTokenForCulture(culture);
                        yield String.valueOf(token == null ? 0 : table.gameState().getTokenCount(card, token));
                    }
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
