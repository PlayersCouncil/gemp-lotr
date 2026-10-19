package com.gempukku.lotro.scenario;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gives one of a scenario's cards another name, everywhere the scenario says the old one.
 *
 * It works on the scenario as its file has it (see ScenarioWriter.toJson), and it goes by what each part of the file
 * is: a card's name is only looked for where a card can be named, and in the way it is named there.  That is what
 * keeps a card called "ring" apart from the part of the file called ring, a card called "hand" apart from
 * freeps.hand, and either apart from the same word in a sentence a player reads.
 *
 *     cards, ringBearer, ring, setup            the name itself
 *     rng, do, answer, allow, an order's answer the name as a word inside the (brackets) of a term: play(x), choose(x, y),
 *                                               assign(x: y + z), move(x, freeps.hand).  What a term quotes from the
 *                                               game (text(...), option(...), the words after the card in use(x, ...))
 *                                               is left as it is.
 *     when, until, skipWhen                     offers(x), inSkirmish(x) (or fighting(x)), after(x)
 *     sites                                     the name itself, where the path is written { name: site }
 *     a message's anchor, highlight, avoid,     card:x, card:x:part, ui:assignment:x
 *     zoom
 *     a message's advance and on                inspect(x), picked(x), assigned(x)
 *     what a player reads                       {strength:x} and the like; [[x]], which goes on showing the old name
 *                                               ([[y|x]]), and [[x|words]]
 *     requirements and rules (when.require,     alias(x), the card builder's filter for one of a scenario's cards,
 *     allow.passWhen, outcome, rules)           wherever in them it is written
 */
final class AliasRename {
    private static final Set<String> QUOTED = Set.of("text", "option", "raw", "title", "name");
    private static final Set<String> NO_CARDS = Set.of("draw", "twilight", "burdens", "threats", "number");

    private final String _from;
    private final String _to;
    private final Pattern _asked;
    private final Pattern _filtered;
    private final Pattern _anchor;
    private final Pattern _bracketed;
    private final Pattern _template;
    private final Pattern _link;

    private AliasRename(String from, String to) {
        _from = from;
        _to = to;
        String name = Pattern.quote(from);
        _asked = Pattern.compile("(?i)(?<![A-Za-z0-9_-])(offers|fighting|inSkirmish|after)\\(\\s*(?-i:" + name + ")\\s*\\)");
        _filtered = Pattern.compile("(?i)(?<![A-Za-z0-9_-])(alias)\\(\\s*(?-i:" + name + ")\\s*\\)");
        _anchor = Pattern.compile("(?<![A-Za-z0-9_:-])(card:|ui:assignment:)" + name + "(?![A-Za-z0-9_-])");
        _bracketed = Pattern.compile("\\(\\s*" + name + "\\s*\\)");
        _template = Pattern.compile("\\{([A-Za-z]+):\\s*" + name + "\\s*}");
        _link = Pattern.compile("\\[\\[" + name + "(\\||]])");
    }

    /** Changes the scenario in place. */
    static void rename(JsonObject root, String from, String to) {
        new AliasRename(from, to).root(root);
    }

    private void root(JsonObject root) {
        JsonObject cards = object(root.get("cards"));
        if (cards != null)
            for (String seat : names(cards))
                cards.set(seat, keys(cards.get(seat)));
        JsonObject sites = object(root.get("sites"));
        if (sites != null)
            for (String seat : names(sites))
                sites.set(seat, keys(sites.get(seat)));
        for (String part : List.of("ringBearer", "ring")) {
            JsonObject perSeat = object(root.get(part));
            if (perSeat != null)
                for (String seat : names(perSeat))
                    perSeat.set(seat, exact(perSeat.get(seat)));
        }
        JsonObject setup = object(root.get("setup"));
        if (setup != null) {
            for (String part : names(setup)) {
                JsonValue value = setup.get(part);
                switch (part) {
                    case "attach", "stack" -> {
                        JsonObject renamed = object(keys(value));
                        if (renamed != null)
                            for (String card : names(renamed))
                                renamed.set(card, exact(renamed.get(card)));
                        setup.set(part, renamed == null ? value : renamed);
                    }
                    case "wounds", "tokens" -> setup.set(part, keys(value));
                    case "startAt", "burdens", "threats" -> { }
                    default -> {
                        //(a seat: each of its zones is a list of cards)
                        JsonObject zones = object(value);
                        if (zones != null)
                            for (String zone : names(zones))
                                zones.set(zone, listed(zones.get(zone)));
                    }
                }
            }
        }
        if (root.get("rules") != null)
            root.set("rules", filters(root.get("rules")));
        if (root.get("rng") != null)
            root.set("rng", terms(root.get("rng")));
        JsonObject opponent = object(root.get("opponent"));
        if (opponent != null && opponent.get("orders") != null && opponent.get("orders").isArray()) {
            for (JsonValue order : opponent.get("orders").asArray()) {
                JsonObject json = object(order);
                if (json == null)
                    continue;
                if (json.get("when") != null)
                    json.set("when", when(json.get("when")));
                if (json.get("answer") != null)
                    json.set("answer", terms(json.get("answer")));
            }
        }
        JsonObject outcome = object(root.get("outcome"));
        if (outcome != null) {
            for (String part : List.of("win", "lose"))
                if (outcome.get(part) != null)
                    outcome.set(part, filters(outcome.get(part)));
            if (outcome.get("loseMessage") != null)
                outcome.set("loseMessage", prose(outcome.get("loseMessage")));
            JsonObject offScript = object(outcome.get("offScript"));
            if (offScript != null && offScript.get("message") != null)
                offScript.set("message", prose(offScript.get("message")));
        }
        if (root.get("steps") != null && root.get("steps").isArray())
            for (JsonValue step : root.get("steps").asArray())
                step(object(step));
    }

    private void step(JsonObject step) {
        if (step == null)
            return;
        for (String part : List.of("when", "until", "skipWhen"))
            if (step.get(part) != null)
                step.set(part, when(step.get(part)));
        for (String part : List.of("do", "answer"))
            if (step.get(part) != null)
                step.set(part, terms(step.get(part)));
        JsonValue allow = step.get("allow");
        if (allow != null && object(allow) == null) {
            step.set("allow", terms(allow));
        } else if (allow != null) {
            JsonObject json = allow.asObject();
            if (json.get("answers") != null)
                json.set("answers", terms(json.get("answers")));
            if (json.get("passWhen") != null)
                json.set("passWhen", filters(json.get("passWhen")));
            if (json.get("otherwise") != null)
                json.set("otherwise", prose(json.get("otherwise")));
            JsonObject hints = object(keys(json.get("hints")));
            if (hints != null) {
                for (String card : names(hints))
                    hints.set(card, prose(hints.get(card)));
                json.set("hints", hints);
            }
        }
        if (step.get("messages") != null)
            step.set("messages", messages(step.get("messages")));
        JsonObject end = object(step.get("end"));
        if (end != null && end.get("messages") != null)
            end.set("messages", messages(end.get("messages")));
        if (step.get("anyOrder") != null && step.get("anyOrder").isArray())
            for (JsonValue child : step.get("anyOrder").asArray())
                step(object(child));
    }

    private JsonValue messages(JsonValue value) {
        if (!value.isArray())
            return message(value);
        JsonArray renamed = new JsonArray();
        for (JsonValue message : value.asArray())
            renamed.add(message(message));
        return renamed;
    }

    private JsonValue message(JsonValue value) {
        JsonObject message = object(value);
        if (message == null)
            return prose(value);
        for (String part : List.of("text", "header"))
            if (message.get(part) != null)
                message.set(part, prose(message.get(part)));
        for (String part : List.of("anchor", "highlight", "avoid", "zoom"))
            if (message.get(part) != null)
                message.set(part, strings(message.get(part), text -> _anchor.matcher(text).replaceAll("$1" + Matcher.quoteReplacement(_to))));
        for (String part : List.of("advance", "on"))
            if (message.get(part) != null)
                message.set(part, strings(message.get(part), text -> _bracketed.matcher(text).replaceAll(Matcher.quoteReplacement("(" + _to + ")"))));
        return message;
    }

    /* ------------------------------------------------------------------ the ways a card is named */

    private JsonValue exact(JsonValue value) {
        return value != null && value.isString() && value.asString().trim().equals(_from) ? JsonValue.valueOf(_to) : value;
    }

    /** An { object } whose members are named after cards, with the one named after this card renamed (and in its place). */
    private JsonValue keys(JsonValue value) {
        JsonObject object = object(value);
        if (object == null)
            return value;
        JsonObject renamed = new JsonObject();
        for (JsonObject.Member member : object)
            renamed.add(member.getName().equals(_from) ? _to : member.getName(), member.getValue());
        return renamed;
    }

    /** A list of cards: "a, b, c", or a [list]. */
    private JsonValue listed(JsonValue value) {
        return strings(value, text -> {
            List<String> cards = new ArrayList<>();
            for (String card : text.split(","))
                cards.add(card.trim().equals(_from) ? _to : card.trim());
            return String.join(", ", cards);
        });
    }

    private JsonValue when(JsonValue value) {
        JsonObject object = object(value);
        if (object == null)
            return strings(value, text -> _asked.matcher(text).replaceAll(match -> Matcher.quoteReplacement(match.group(1) + "(" + _to + ")")));
        for (String part : List.of("offers", "fighting", "inSkirmish", "after"))
            if (object.get(part) != null)
                object.set(part, exact(object.get(part)));
        if (object.get("require") != null)
            object.set("require", filters(object.get("require")));
        return object;
    }

    /** Something written for the card builder (requirements, rules): alias(x), in whatever text it has in it. */
    private JsonValue filters(JsonValue value) {
        if (value.isString())
            return JsonValue.valueOf(_filtered.matcher(value.asString()).replaceAll(match -> Matcher.quoteReplacement(match.group(1) + "(" + _to + ")")));
        if (value.isArray()) {
            JsonArray changed = new JsonArray();
            for (JsonValue element : value.asArray())
                changed.add(filters(element));
            return changed;
        }
        if (!value.isObject())
            return value;
        JsonObject changed = new JsonObject();
        for (JsonObject.Member member : value.asObject())
            changed.add(member.getName(), filters(member.getValue()));
        return changed;
    }

    private JsonValue prose(JsonValue value) {
        return strings(value, text -> {
            String templated = _template.matcher(text).replaceAll(match -> Matcher.quoteReplacement("{" + match.group(1) + ":" + _to + "}"));
            //(a link that showed the card's old name goes on showing it)
            return _link.matcher(templated).replaceAll(match -> Matcher.quoteReplacement(
                    match.group(1).equals("|") ? "[[" + _to + "|" : "[[" + _to + "|" + _from + "]]"));
        });
    }

    private JsonValue terms(JsonValue value) {
        return strings(value, this::terms);
    }

    /**
     * A line of terms, with the card renamed wherever it is a word of its own inside a term's brackets.  A word with
     * a dot on either side of it is part of a place (freeps.hand), and a word that opens a bracket is a term.
     */
    private String terms(String text) {
        StringBuilder out = new StringBuilder();
        int depth = 0;
        int at = 0;
        while (at < text.length()) {
            char ch = text.charAt(at);
            if (!wordChar(ch)) {
                if (ch == '(')
                    depth++;
                if (ch == ')')
                    depth = Math.max(0, depth - 1);
                out.append(ch);
                at++;
                continue;
            }
            int end = at;
            while (end < text.length() && wordChar(text.charAt(end)))
                end++;
            String word = text.substring(at, end);
            boolean opens = end < text.length() && text.charAt(end) == '(';
            if (opens) {
                int close = closing(text, end);
                String name = word.toLowerCase(java.util.Locale.ROOT);
                if (QUOTED.contains(name) || NO_CARDS.contains(name)) {
                    //Nothing in it names a card: it is left as it is, brackets and all
                    out.append(text, at, close + 1);
                    at = close + 1;
                    continue;
                }
                if (name.equals("use")) {
                    //use(card, some of the words the game offers the action in): the card, and nothing after it
                    String inside = text.substring(end + 1, close);
                    int comma = inside.indexOf(',');
                    String card = comma < 0 ? inside : inside.substring(0, comma);
                    out.append(word).append('(').append(card.trim().equals(_from) ? card.replace(_from, _to) : card)
                            .append(comma < 0 ? "" : inside.substring(comma)).append(text, close, Math.min(text.length(), close + 1));
                    at = close + 1;
                    continue;
                }
                out.append(word);
                at = end;
                continue;
            }
            boolean dotted = (at > 0 && text.charAt(at - 1) == '.') || (end < text.length() && text.charAt(end) == '.');
            out.append(depth > 0 && !dotted && word.equals(_from) ? _to : word);
            at = end;
        }
        return out.toString();
    }

    /** Where the bracket that opens at a place closes; the end of the text if it never does. */
    private static int closing(String text, int open) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            if (text.charAt(i) == '(')
                depth++;
            if (text.charAt(i) == ')' && --depth == 0)
                return i;
        }
        return text.length() - 1;
    }

    private static boolean wordChar(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-';
    }

    /* ------------------------------------------------------------------ small things */

    /** A value that is text, or a [list] of texts (a file can have either wherever it has one), each text changed. */
    private static JsonValue strings(JsonValue value, java.util.function.UnaryOperator<String> change) {
        if (value == null)
            return null;
        if (value.isString())
            return JsonValue.valueOf(change.apply(value.asString()));
        if (!value.isArray())
            return value;
        JsonArray changed = new JsonArray();
        for (JsonValue element : value.asArray())
            changed.add(element.isString() ? JsonValue.valueOf(change.apply(element.asString())) : element);
        return changed;
    }

    private static JsonObject object(JsonValue value) {
        return value != null && value.isObject() ? value.asObject() : null;
    }

    private static List<String> names(JsonObject object) {
        return new ArrayList<>(object.names());
    }
}
