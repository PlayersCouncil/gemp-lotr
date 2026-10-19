package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.scenario.ScenarioDefinition.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Turns an answer written in a scenario ("play(gandalf)", "assign(frodo: bowman)") into the string the game expects
 * for the decision in front of it, and describes decisions back in the scenario's own terms.
 */
public final class AnswerCompiler {
    private AnswerCompiler() { }

    /**
     * @return What to send the game as the answer.
     * @throws ScenarioScriptException if the answer makes no sense for this decision, or asks for something the
     * decision does not offer.
     */
    public static String compile(ScenarioTable table, AwaitingDecision decision, Answer answer) throws ScenarioScriptException {
        AwaitingDecisionType type = decision.getDecisionType();
        Map<String, String[]> params = decision.getDecisionParameters();
        switch (answer.kind()) {
            case RAW:
                return answer.text();

            case PASS:
                switch (type) {
                    case CARD_ACTION_CHOICE, ASSIGN_MINIONS:
                        return "";
                    case CARD_SELECTION, ARBITRARY_CARDS:
                        if (Integer.parseInt(params.get("min")[0]) == 0)
                            return "";
                        throw mismatch(table, decision, answer, "at least " + params.get("min")[0] + " card(s) must be chosen");
                    default:
                        throw mismatch(table, decision, answer, "this kind of decision cannot be passed");
                }

            case PLAY, USE: {
                PhysicalCardImpl card = table.Card(answer.cards().getFirst());
                String wantedText = answer.kind() == ScenarioDefinition.AnswerKind.PLAY ? "Play" : answer.text();
                if (type == AwaitingDecisionType.CARD_ACTION_CHOICE) {
                    String[] cardIds = params.get("cardId");
                    String[] texts = params.get("actionText");
                    for (int i = 0; i < cardIds.length; i++)
                        if (cardIds[i].equals(String.valueOf(card.getCardId())) && textMatches(texts[i], wantedText))
                            return params.get("actionId")[i];
                } else if (type == AwaitingDecisionType.ACTION_CHOICE) {
                    //Actions here are identified by the card they come from only through its blueprint
                    String[] blueprintIds = params.get("blueprintId");
                    String[] texts = params.get("actionText");
                    for (int i = 0; i < blueprintIds.length; i++)
                        if (blueprintIds[i].equals(card.getBlueprintId()) && textMatches(texts[i], wantedText))
                            return params.get("actionId")[i];
                } else {
                    throw mismatch(table, decision, answer, "this decision is not a choice of action");
                }
                throw mismatch(table, decision, answer, "no such action is on offer");
            }

            case CHOOSE:
                return chosen(table, decision, answer, false).getFirst();

            case OPTION: {
                if (type != AwaitingDecisionType.MULTIPLE_CHOICE)
                    throw mismatch(table, decision, answer, "this decision is not a choice between options");
                String[] results = params.get("results");
                String wanted = answer.text();
                if (wanted.startsWith("#")) {
                    try {
                        int index = Integer.parseInt(wanted.substring(1).trim()) - 1;
                        if (index >= 0 && index < results.length)
                            return String.valueOf(index);
                    } catch (NumberFormatException ignored) { }
                    throw mismatch(table, decision, answer, "there is no option " + wanted);
                }
                for (int i = 0; i < results.length; i++)
                    if (results[i].equalsIgnoreCase(wanted))
                        return String.valueOf(i);
                for (int i = 0; i < results.length; i++)
                    if (results[i].toLowerCase().contains(wanted.toLowerCase()))
                        return String.valueOf(i);
                throw mismatch(table, decision, answer, "no such option is on offer");
            }

            case NUMBER: {
                if (type != AwaitingDecisionType.INTEGER)
                    throw mismatch(table, decision, answer, "this decision does not ask for a number");
                int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : Integer.MIN_VALUE;
                int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : Integer.MAX_VALUE;
                if (answer.number() < min || answer.number() > max)
                    throw mismatch(table, decision, answer, "the number has to be from " + min + " to " + max);
                return String.valueOf(answer.number());
            }

            case ASSIGN: {
                if (type != AwaitingDecisionType.ASSIGN_MINIONS)
                    throw mismatch(table, decision, answer, "this decision is not an assignment of minions");
                Set<String> characters = new HashSet<>(Arrays.asList(params.get("freeCharacters")));
                Set<String> minions = new HashSet<>(Arrays.asList(params.get("minions")));
                StringJoiner groups = new StringJoiner(",");
                for (Map.Entry<String, List<String>> entry : answer.assignments().entrySet()) {
                    if (entry.getValue().isEmpty())
                        continue;
                    String characterId = String.valueOf(table.Card(entry.getKey()).getCardId());
                    if (!characters.contains(characterId))
                        throw mismatch(table, decision, answer, "'" + entry.getKey() + "' cannot be assigned to");
                    StringJoiner group = new StringJoiner(" ");
                    group.add(characterId);
                    for (String minion : entry.getValue()) {
                        String minionId = String.valueOf(table.Card(minion).getCardId());
                        if (!minions.contains(minionId))
                            throw mismatch(table, decision, answer, "'" + minion + "' cannot be assigned");
                        group.add(minionId);
                    }
                    groups.add(group.toString());
                }
                return groups.toString();
            }
        }
        throw mismatch(table, decision, answer, "unknown kind of answer");
    }

    private static boolean textMatches(String actionText, String wanted) {
        return wanted == null || actionText.toLowerCase().contains(wanted.toLowerCase());
    }

    /**
     * Every answer the game would take for a choose(...) that could be one of several cards: choose(skirmishing)
     * with two minions in the skirmish is either of them.  Any other answer has only the one.
     */
    public static List<String> compileAll(ScenarioTable table, AwaitingDecision decision, Answer answer) throws ScenarioScriptException {
        if (answer.kind() == ScenarioDefinition.AnswerKind.CHOOSE)
            return chosen(table, decision, answer, true);
        return List.of(compile(table, decision, answer));
    }

    /** One of the cards a decision offers to be chosen. */
    private record Offered(String answer, PhysicalCard card, String blueprintId) { }

    /**
     * @param every Whether to give every way of choosing that fits (only where the answer is a single pick that
     *              more than one card fits, and that does not say which of them); otherwise the one way.
     */
    private static List<String> chosen(ScenarioTable table, AwaitingDecision decision, Answer answer, boolean every) throws ScenarioScriptException {
        AwaitingDecisionType type = decision.getDecisionType();
        Map<String, String[]> params = decision.getDecisionParameters();
        List<Offered> offered = new ArrayList<>();
        if (type == AwaitingDecisionType.CARD_SELECTION) {
            for (String id : params.get("cardId"))
                offered.add(new Offered(id, table.gameState().findCardById(Integer.parseInt(id)), null));
        } else if (type == AwaitingDecisionType.ARBITRARY_CARDS) {
            String[] tempIds = params.get("cardId");
            String[] selectable = params.get("selectable");
            String[] blueprintIds = params.get("blueprintId");
            List<? extends PhysicalCard> physical = decision instanceof ArbitraryCardsSelectionDecision arbitrary
                    && arbitrary.getPhysicalCards() != null ? new ArrayList<>(arbitrary.getPhysicalCards()) : null;
            for (int i = 0; i < tempIds.length; i++)
                if (selectable[i].equals("true"))
                    offered.add(new Offered(tempIds[i], physical != null ? physical.get(i) : null, blueprintIds[i]));
        } else {
            throw mismatch(table, decision, answer, "this decision is not a choice of cards");
        }

        List<ScenarioDefinition.Pick> picks = answer.picks().isEmpty()
                ? answer.cards().stream().map(alias -> new ScenarioDefinition.Pick(alias, false, null, null, 0, alias)).toList()
                : answer.picks();
        String player = table.PlayerOf(decision);
        Set<Offered> used = new HashSet<>();
        List<String> ids = new ArrayList<>();
        List<String> alternatives = null;
        for (ScenarioDefinition.Pick pick : picks) {
            List<Offered> fit = new ArrayList<>();
            if (pick.plain()) {
                PhysicalCardImpl card = table.Card(pick.alias());
                for (Offered one : offered)
                    if (!used.contains(one) && (one.card() != null ? one.card() == card : one.blueprintId().equals(card.getBlueprintId())))
                        fit.add(one);
            } else {
                List<PhysicalCard> cards = new ArrayList<>();
                for (Offered one : offered)
                    if (!used.contains(one))
                        cards.add(one.card());
                List<PhysicalCard> picked = table.Picked(pick, player, cards);
                for (Offered one : offered)
                    if (!used.contains(one) && one.card() != null && picked.contains(one.card()))
                        fit.add(one);
            }
            if (pick.ordinal() > 0) {
                if (fit.size() < pick.ordinal())
                    throw mismatch(table, decision, answer, "'" + pick.source() + "': " + (fit.isEmpty() ? "no card on offer fits"
                            : "only " + fit.size() + " card(s) on offer fit"));
                fit = List.of(fit.get(pick.ordinal() - 1));
            }
            if (fit.isEmpty())
                throw mismatch(table, decision, answer, "'" + pick.source() + "' cannot be chosen");
            if (every && picks.size() == 1 && fit.size() > 1)
                alternatives = fit.stream().map(Offered::answer).toList();
            used.add(fit.getFirst());
            ids.add(fit.getFirst().answer());
        }
        checkCount(table, decision, answer, picks.size());
        return alternatives != null ? alternatives : List.of(String.join(",", ids));
    }

    private static void checkCount(ScenarioTable table, AwaitingDecision decision, Answer answer, int count) throws ScenarioScriptException {
        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        if (count < min || count > max)
            throw mismatch(table, decision, answer, "from " + min + " to " + max + " card(s) must be chosen");
    }

    private static ScenarioScriptException mismatch(ScenarioTable table, AwaitingDecision decision, Answer answer, String why) {
        return new ScenarioScriptException("'" + answer.source() + "' does not fit the decision: " + why
                + ".  The decision is: " + describe(table, decision));
    }

    /**
     * The other way about: an answer the game was given, written the way a scenario would write it.  This is how
     * what somebody did at the table becomes a line of script (see ScenarioAuthoring).  What comes back is a guess at
     * the best wording, not a promise: whoever uses it should compile it again and see that it gives the same answer.
     * @return The answer as a term ("play(gandalf)", "assign(frodo: bowman)", "pass"); "raw(...)" when the scenario's
     * own words cannot say it (a card the scenario has no name for).
     */
    public static String decompile(ScenarioTable table, AwaitingDecision decision, String answer) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String raw = "raw(" + answer + ")";
        switch (decision.getDecisionType()) {
            case CARD_ACTION_CHOICE: {
                if (answer.isEmpty())
                    return "pass";
                String[] actionIds = params.get("actionId");
                String[] cardIds = params.get("cardId");
                String[] texts = params.get("actionText");
                for (int i = 0; i < actionIds.length; i++) {
                    if (!actionIds[i].equals(answer))
                        continue;
                    String alias = table.AliasOfCardId(cardIds[i]);
                    if (alias == null)
                        return raw;
                    String text = stripHtml(texts[i]);
                    //The first thing a card offers is what its name alone picks
                    boolean first = true;
                    for (int earlier = 0; earlier < i; earlier++)
                        if (cardIds[earlier].equals(cardIds[i]))
                            first = false;
                    if (text.startsWith("Play "))
                        return "play(" + alias + ")";
                    return first ? "use(" + alias + ")" : "use(" + alias + ", " + text + ")";
                }
                return raw;
            }
            case CARD_SELECTION: {
                if (answer.isEmpty())
                    return "pass";
                StringJoiner aliases = new StringJoiner(", ");
                for (String cardId : answer.split(",")) {
                    String alias = table.AliasOfCardId(cardId.trim());
                    if (alias == null)
                        return raw;
                    aliases.add(alias);
                }
                return "choose(" + aliases + ")";
            }
            case ARBITRARY_CARDS: {
                if (answer.isEmpty())
                    return "pass";
                String[] tempIds = params.get("cardId");
                String[] blueprintIds = params.get("blueprintId");
                List<? extends PhysicalCard> physical = decision instanceof ArbitraryCardsSelectionDecision arbitrary
                        && arbitrary.getPhysicalCards() != null ? new ArrayList<>(arbitrary.getPhysicalCards()) : null;
                StringJoiner aliases = new StringJoiner(", ");
                Set<String> used = new HashSet<>();
                for (String tempId : answer.split(",")) {
                    int index = Arrays.asList(tempIds).indexOf(tempId.trim());
                    if (index < 0)
                        return raw;
                    String alias = physical != null ? table.AliasOf(physical.get(index)) : null;
                    if (alias == null) {
                        //Shown by what it is, not which it is: any of the scenario's cards of that kind will do
                        for (String candidate : table.Definition().cards.keySet()) {
                            if (!used.contains(candidate) && table.Arranged()
                                    && table.Card(candidate).getBlueprintId().equals(blueprintIds[index])) {
                                alias = candidate;
                                break;
                            }
                        }
                    }
                    if (alias == null)
                        return raw;
                    used.add(alias);
                    aliases.add(alias);
                }
                return "choose(" + aliases + ")";
            }
            case MULTIPLE_CHOICE: {
                String[] results = params.get("results");
                try {
                    int index = Integer.parseInt(answer.trim());
                    if (index < 0 || index >= results.length)
                        return raw;
                    //By what it says, if that says which it is and can be written down; otherwise by where it comes
                    String text = results[index];
                    int same = 0;
                    for (String result : results)
                        if (result.equalsIgnoreCase(text))
                            same++;
                    boolean writable = same == 1 && !text.isEmpty() && !text.startsWith("#") && text.matches("[^(),\\n]*");
                    return "option(" + (writable ? text : "#" + (index + 1)) + ")";
                } catch (NumberFormatException exp) {
                    return raw;
                }
            }
            case INTEGER:
                return answer.trim().matches("-?\\d+") ? "number(" + answer.trim() + ")" : raw;
            case ASSIGN_MINIONS: {
                if (answer.trim().isEmpty())
                    return "pass";
                StringJoiner pairings = new StringJoiner(", ");
                for (String group : answer.split(",")) {
                    String[] ids = group.trim().split(" +");
                    String character = table.AliasOfCardId(ids[0]);
                    if (character == null)
                        return raw;
                    StringJoiner minions = new StringJoiner(" + ");
                    for (int i = 1; i < ids.length; i++) {
                        String minion = table.AliasOfCardId(ids[i]);
                        if (minion == null)
                            return raw;
                        minions.add(minion);
                    }
                    if (ids.length > 1)
                        pairings.add(character + ": " + minions);
                }
                return pairings.length() == 0 ? "pass" : "assign(" + pairings + ")";
            }
            default:
                return raw;
        }
    }

    /**
     * @return What to answer when nobody has said what to answer: do as little as the decision allows.
     */
    public static String leastAnswer(AwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        switch (decision.getDecisionType()) {
            case CARD_ACTION_CHOICE, ASSIGN_MINIONS:
                return "";
            case CARD_SELECTION: {
                int min = Integer.parseInt(params.get("min")[0]);
                return String.join(",", Arrays.copyOf(params.get("cardId"), min));
            }
            case ARBITRARY_CARDS: {
                int min = Integer.parseInt(params.get("min")[0]);
                StringJoiner ids = new StringJoiner(",");
                String[] tempIds = params.get("cardId");
                String[] selectable = params.get("selectable");
                for (int i = 0, picked = 0; i < tempIds.length && picked < min; i++)
                    if (selectable[i].equals("true")) {
                        ids.add(tempIds[i]);
                        picked++;
                    }
                return ids.toString();
            }
            case INTEGER:
                return params.containsKey("min") ? params.get("min")[0] : "0";
            case MULTIPLE_CHOICE: {
                //Declining is the least one can do, when declining is on offer
                String[] results = params.get("results");
                for (int i = 0; i < results.length; i++)
                    if (results[i].equalsIgnoreCase("No"))
                        return String.valueOf(i);
                return "0";
            }
            default:
                return "0";
        }
    }

    /**
     * @return The decision in a scenario author's terms: what kind it is, what it says, and what it offers, with
     * cards called by their aliases.
     */
    public static String describe(ScenarioTable table, AwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        StringBuilder sb = new StringBuilder();
        sb.append(typeName(decision.getDecisionType())).append(" \"").append(decision.getText()).append("\"");
        List<String> options = options(table, decision);
        if (params.containsKey("min") && decision.getDecisionType() != AwaitingDecisionType.INTEGER)
            sb.append(" (choose ").append(params.get("min")[0]).append(" to ").append(params.get("max")[0]).append(")");
        sb.append(options.isEmpty() ? " offering nothing but Pass" : " offering: " + String.join("; ", options));
        return sb.toString();
    }

    /**
     * @return Each thing the decision offers, written the way a scenario would write the answer that picks it.
     */
    public static List<String> options(ScenarioTable table, AwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        List<String> options = new ArrayList<>();
        switch (decision.getDecisionType()) {
            case CARD_ACTION_CHOICE -> {
                String[] cardIds = params.get("cardId");
                String[] texts = params.get("actionText");
                for (int i = 0; i < cardIds.length; i++) {
                    String name = aliasOrId(table, cardIds[i]);
                    String text = stripHtml(texts[i]);
                    options.add(text.startsWith("Play ") ? "play(" + name + ")" : "use(" + name + ", " + text + ")");
                }
            }
            case ACTION_CHOICE -> {
                String[] texts = params.get("actionText");
                for (int i = 0; i < texts.length; i++)
                    options.add("raw(" + i + ") [" + stripHtml(texts[i]) + "]");
            }
            case CARD_SELECTION -> {
                for (String cardId : params.get("cardId"))
                    options.add("choose(" + aliasOrId(table, cardId) + ")");
            }
            case ARBITRARY_CARDS -> {
                String[] blueprintIds = params.get("blueprintId");
                String[] selectable = params.get("selectable");
                List<? extends PhysicalCard> physical = decision instanceof ArbitraryCardsSelectionDecision arbitrary
                        && arbitrary.getPhysicalCards() != null ? new ArrayList<>(arbitrary.getPhysicalCards()) : null;
                for (int i = 0; i < blueprintIds.length; i++) {
                    String alias = physical != null ? table.AliasOf(physical.get(i)) : null;
                    options.add("choose(" + (alias != null ? alias : blueprintIds[i]) + ")"
                            + (selectable[i].equals("true") ? "" : " [shown, not selectable]"));
                }
            }
            case MULTIPLE_CHOICE -> {
                for (String result : params.get("results"))
                    options.add("option(" + result + ")");
            }
            case INTEGER -> options.add("number(" + (params.containsKey("min") ? params.get("min")[0] : "?") + " to "
                    + (params.containsKey("max") ? params.get("max")[0] : "?") + ")");
            case ASSIGN_MINIONS -> {
                StringJoiner characters = new StringJoiner(", ");
                for (String id : params.get("freeCharacters"))
                    characters.add(aliasOrId(table, id));
                StringJoiner minions = new StringJoiner(", ");
                for (String id : params.get("minions"))
                    minions.add(aliasOrId(table, id));
                options.add("assign(...) among characters [" + characters + "] and minions [" + minions + "]");
            }
        }
        return options;
    }

    /**
     * @return What to call a card in a description: its alias if the scenario gave it one, otherwise its name in
     * brackets (which is not something a scenario can write back, but says which card is meant).
     */
    private static String aliasOrId(ScenarioTable table, String cardId) {
        String alias = table.AliasOfCardId(cardId);
        if (alias != null)
            return alias;
        try {
            PhysicalCard card = table.gameState().findCardById(Integer.parseInt(cardId));
            if (card != null)
                return "[" + GameUtils.getFullName(card) + "]";
        } catch (NumberFormatException ignored) { }
        return "card#" + cardId;
    }

    public static String typeName(AwaitingDecisionType type) {
        return switch (type) {
            case CARD_ACTION_CHOICE -> "action";
            case ACTION_CHOICE -> "actionChoice";
            case CARD_SELECTION -> "cards";
            case ARBITRARY_CARDS -> "arbitrary";
            case MULTIPLE_CHOICE -> "choice";
            case INTEGER -> "number";
            case ASSIGN_MINIONS -> "assign";
        };
    }

    private static String stripHtml(String text) {
        return text.replaceAll("<[^>]*>", "").trim();
    }
}
