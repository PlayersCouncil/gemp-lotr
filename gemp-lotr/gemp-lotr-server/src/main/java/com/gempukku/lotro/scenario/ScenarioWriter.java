package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.scenario.ScenarioDefinition.*;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The other direction from ScenarioParser: turns a ScenarioDefinition back into a scenario file.
 *
 * It produces the same thing in two forms.  toJson is the file as a tree, field for field what the parser reads; as
 * JSON text that is what the authoring tool's screen edits and sends back (the parser reads JSON, HJSON being a
 * superset of it).  toHjson is that tree written out the way scenario files are written by hand: tabs, one key to a
 * line, no quotes where none are needed, lists on one line with commas.
 *
 * What a hand-written file has that this cannot give back is its comments: the parser never sees them.  What an
 * author wants kept goes in a 'note' (the scenario, a step and a message can each have one).  In their place the
 * writer puts comments of its own, which are always right because they are worked out afresh: the name of each
 * card beside its alias, the sites by name, and each step's number.
 *
 * Fields that still have the value they would have had if they had been left out are left out.
 */
public final class ScenarioWriter {
    private ScenarioWriter() { }

    /* ------------------------------------------------------------------ the definition as a tree */

    public static JsonObject toJson(ScenarioDefinition def) {
        JsonObject root = new JsonObject();
        root.add("id", def.id);
        root.add("version", def.version);
        root.add("kind", lower(def.kind));
        root.add("title", def.title);
        addIfSet(root, "summary", def.summary);
        addIfSet(root, "author", def.author);
        if (!def.tags.isEmpty())
            root.add("tags", String.join(", ", def.tags));
        addIfSet(root, "note", def.note);
        root.add("format", def.format);
        root.add("human", def.human.key());

        JsonObject opponent = new JsonObject();
        addIfSet(opponent, "name", def.opponentName);
        if (def.fallback != Fallback.FAIL)
            opponent.add("fallback", lower(def.fallback));
        if (!def.orders.isEmpty()) {
            JsonArray orders = new JsonArray();
            for (Order order : def.orders) {
                JsonObject json = new JsonObject();
                json.add("when", when(order.when()));
                json.add("answer", answers(order.answers()));
                orders.add(json);
            }
            opponent.add("orders", orders);
        }
        if (!opponent.isEmpty())
            root.add("opponent", opponent);
        root.add("coverage", lower(def.coverage));

        JsonObject options = new JsonObject();
        if (!def.autoPass.isEmpty()) {
            StringJoiner phases = new StringJoiner(", ");
            def.autoPass.forEach(phase -> phases.add(lower(phase)));
            options.add("autoPass", phases.toString());
        }
        if (!def.autoAccept)
            options.add("autoAccept", false);
        if (def.rngDefault != RngDefault.IDENTITY)
            options.add("rngDefault", lower(def.rngDefault));
        if (def.opponentDelayMs != 900)
            options.add("opponentDelayMs", def.opponentDelayMs);
        if (def.runnerLevel != null)
            options.add("runnerLevel", lower(def.runnerLevel));
        addIfSet(options, "bubbleGap", def.bubbleGap);
        if (!options.isEmpty())
            root.add("options", options);

        JsonObject cards = new JsonObject();
        for (Seat seat : Seat.values()) {
            JsonObject seatCards = new JsonObject();
            for (CardDef card : def.cards.values())
                if (card.owner() == seat)
                    seatCards.add(card.alias(), card.blueprintId());
            cards.add(seat.key(), seatCards);
        }
        root.add("cards", cards);
        root.add("ringBearer", perSeat(def.ringBearer));
        if (!def.ring.isEmpty())
            root.add("ring", perSeat(def.ring));
        JsonObject sites = new JsonObject();
        for (Seat seat : Seat.values()) {
            if (!def.sites.containsKey(seat))
                continue;
            List<String> blueprints = def.sites.get(seat);
            List<String> aliases = def.siteAliases.getOrDefault(seat, List.of());
            boolean named = false;
            for (int at = 0; at < aliases.size(); at++)
                named |= !aliases.get(at).equals(def.defaultSiteAlias(seat, at + 1));
            if (!named) {
                sites.add(seat.key(), String.join(", ", blueprints));
            } else {
                //(sites given names of their own are written with them, in their order on the path)
                JsonObject path = new JsonObject();
                for (int at = 0; at < blueprints.size(); at++)
                    path.add(aliases.get(at), blueprints.get(at));
                sites.add(seat.key(), path);
            }
        }
        root.add("sites", sites);

        JsonObject rules = new JsonObject();
        if (def.oneSided)
            rules.add("oneSided", true);
        for (Seat seat : Seat.values())
            if (def.rules.containsKey(seat))
                rules.add(seat.key(), JsonValue.readHjson(def.rules.get(seat)));
        if (!rules.isEmpty())
            root.add("rules", rules);

        JsonObject setup = setup(def);
        if (!setup.isEmpty())
            root.add("setup", setup);

        if (!def.rng.isEmpty()) {
            JsonArray rng = new JsonArray();
            for (RngEntry entry : def.rng)
                rng.add(lower(entry.kind()) + "(" + entry.seat().key() + "." + entry.zone() + ": " + String.join(", ", entry.result()) + ")");
            root.add("rng", rng);
        }

        JsonObject outcome = new JsonObject();
        if (def.outcome.win != null)
            outcome.add("win", requirements(def.outcome.win));
        if (def.outcome.lose != null)
            outcome.add("lose", requirements(def.outcome.lose));
        addIfSet(outcome, "loseMessage", def.outcome.loseMessage);
        JsonObject offScript = new JsonObject();
        addIfSet(offScript, "message", def.outcome.offScriptMessage);
        if (def.outcome.offScriptResult != OffScriptResult.RESTART)
            offScript.add("result", lower(def.outcome.offScriptResult));
        if (!offScript.isEmpty())
            outcome.add("offScript", offScript);
        if (!outcome.isEmpty())
            root.add("outcome", outcome);

        JsonArray steps = new JsonArray();
        for (Step step : def.steps)
            steps.add(step(step));
        root.add("steps", steps);
        return root;
    }

    private static JsonObject setup(ScenarioDefinition def) {
        Setup from = def.setup;
        JsonObject setup = new JsonObject();
        if (from.turn != 1 || from.phase != com.gempukku.lotro.common.Phase.FELLOWSHIP) {
            JsonObject startAt = new JsonObject();
            startAt.add("turn", from.turn);
            startAt.add("phase", lower(from.phase));
            setup.add("startAt", startAt);
        }
        for (Seat seat : Seat.values()) {
            Map<SetupZone, List<String>> zones = from.zones.get(seat);
            if (zones == null)
                continue;
            JsonObject json = new JsonObject();
            for (Map.Entry<SetupZone, List<String>> zone : zones.entrySet())
                if (!zone.getValue().isEmpty())
                    json.add(zoneName(zone.getKey()), String.join(", ", zone.getValue()));
            if (!json.isEmpty())
                setup.add(seat.key(), json);
        }
        addMap(setup, "attach", from.attach);
        addMap(setup, "stack", from.stack);
        addCounts(setup, "wounds", from.wounds);
        addCounts(setup, "tokens", from.tokens);
        if (from.burdens != 0)
            setup.add("burdens", from.burdens);
        if (from.threats != 0)
            setup.add("threats", from.threats);
        return setup;
    }

    public static String zoneName(SetupZone zone) {
        return switch (zone) {
            case CHARACTERS -> "characters";
            case SUPPORT -> "support";
            case MINIONS -> "minions";
            case HAND -> "hand";
            case DECK -> "deck";
            case DISCARD -> "discard";
            case DEAD_PILE -> "deadPile";
            case REMOVED -> "removed";
        };
    }

    public static JsonObject step(Step step) {
        JsonObject json = new JsonObject();
        addIfSet(json, "id", step.id);
        addIfSet(json, "note", step.note);
        if (step.checkpoint)
            json.add("checkpoint", true);
        if (step.pause)
            json.add("pause", true);
        if (step.once)
            json.add("once", true);
        if (!step.onlyIf.isEmpty())
            json.add("if", String.join(", ", step.onlyIf));
        if (!step.unless.isEmpty())
            json.add("unless", String.join(", ", step.unless));
        //(a pause is given its seat by the parser; a 'when' is only written out if one was written in)
        if (!step.when.source.isEmpty())
            json.add("when", when(step.when));
        if (step.until != null)
            json.add("until", when(step.until));
        if (step.skipWhen != null)
            json.add("skipWhen", when(step.skipWhen));
        if (!step.doOps.isEmpty()) {
            StringJoiner ops = new StringJoiner(", ");
            step.doOps.forEach(op -> ops.add(op.source()));
            json.add("do", ops.toString());
        }
        if (!step.messages.isEmpty())
            json.add("messages", messages(step.messages));
        if (step.allow != null)
            json.add("allow", allow(step.allow));
        if (!step.answers.isEmpty())
            json.add("answer", answers(step.answers));
        if (step.end != null) {
            if (step.end.messages.isEmpty()) {
                json.add("end", lower(step.end.result));
            } else {
                JsonObject end = new JsonObject();
                end.add("result", lower(step.end.result));
                end.add("messages", messages(step.end.messages));
                json.add("end", end);
            }
        }
        if (step.block) {
            if (step.skirmishes)
                json.add("skirmishes", true);
            JsonArray children = new JsonArray();
            for (Step child : step.children)
                children.add(step(child));
            json.add("anyOrder", children);
        }
        return json;
    }

    /** A 'when' the way it was written, if it was written as terms; as an { object } if it has requirements. */
    private static JsonValue when(When when) {
        if (when.require == null && !when.source.startsWith("{"))
            return JsonValue.valueOf(when.source);
        if (when.require == null)
            return JsonValue.valueOf(terms(when));
        JsonObject json = new JsonObject();
        if (when.seat != null)
            json.add("seat", when.seat.key());
        if (when.decision != null)
            json.add("decision", AnswerCompiler.typeName(when.decision));
        addIfSet(json, "text", when.text);
        if (when.phase != null)
            json.add("phase", lower(when.phase));
        if (when.turn != null)
            json.add("turn", when.turn);
        if (when.skirmish != null)
            json.add("skirmish", when.skirmish);
        addIfSet(json, "offers", when.offers);
        addIfSet(json, "inSkirmish", when.fighting);
        addIfSet(json, "after", when.after);
        json.add("require", requirements(when.require));
        return json;
    }

    /** A 'when' without requirements, as terms. */
    static String terms(When when) {
        StringJoiner terms = new StringJoiner(", ");
        if (when.seat != null)
            terms.add(when.seat.key());
        if (when.decision != null)
            terms.add(AnswerCompiler.typeName(when.decision));
        if (when.phase != null)
            terms.add("phase(" + lower(when.phase) + ")");
        if (when.turn != null)
            terms.add("turn(" + when.turn + ")");
        if (when.skirmish != null)
            terms.add("skirmish(" + when.skirmish + ")");
        if (when.offers != null)
            terms.add("offers(" + when.offers + ")");
        if (when.fighting != null)
            terms.add("inSkirmish(" + when.fighting + ")");
        if (when.after != null)
            terms.add("after(" + when.after + ")");
        if (when.text != null)
            terms.add("text(" + when.text + ")");
        if (when.opening)
            terms.add("opening");
        return terms.toString();
    }

    private static String answers(List<Answer> answers) {
        StringJoiner terms = new StringJoiner(", ");
        answers.forEach(answer -> terms.add(answer.source()));
        return terms.toString();
    }

    private static JsonValue allow(Allow allow) {
        if (allow.passWhen == null && allow.otherwise == null && allow.hints.isEmpty())
            return JsonValue.valueOf(answers(allow.answers));
        JsonObject json = new JsonObject();
        if (!allow.answers.isEmpty())
            json.add("answers", answers(allow.answers));
        if (allow.passWhen != null)
            json.add("passWhen", JsonValue.readHjson(allow.passWhen));
        addIfSet(json, "otherwise", allow.otherwise);
        if (!allow.hints.isEmpty()) {
            JsonObject hints = new JsonObject();
            allow.hints.forEach(hints::add);
            json.add("hints", hints);
        }
        return json;
    }

    private static JsonArray messages(List<Message> messages) {
        JsonArray array = new JsonArray();
        for (Message message : messages)
            array.add(message(message));
        return array;
    }

    public static JsonValue message(Message message) {
        JsonObject json = new JsonObject();
        addIfSet(json, "note", message.note);
        addIfSet(json, "header", message.header);
        json.add("text", message.text);
        if (!message.anchor.isEmpty())
            json.add("anchor", String.join(", ", message.anchor));
        if (!message.highlight.isEmpty())
            json.add("highlight", String.join(", ", message.highlight));
        if (!"gold".equals(message.highlightStyle))
            json.add("highlightStyle", message.highlightStyle);
        if (!message.avoid.isEmpty())
            json.add("avoid", String.join(", ", message.avoid));
        if (!"auto".equals(message.side))
            json.add("side", message.side);
        addIfSet(json, "gap", message.gap);
        //(a card shown large always waits for Next; the parser fills that in, and it need not be said)
        if (message.advance != null && !(message.zoom != null && message.advance == Advance.NEXT))
            json.add("advance", message.advance == Advance.INSPECT && message.advanceCard != null
                    ? "inspect(" + message.advanceCard + ")" : lower(message.advance));
        if (message.on != null)
            json.add("on", message.on.card() == null ? lower(message.on.kind()) : lower(message.on.kind()) + "(" + message.on.card() + ")");
        if (message.zoom != null) {
            Zoom zoom = message.zoom;
            json.add("zoom", zoom.card() != null ? "card:" + zoom.card() : zoom.site() != null ? "site:" + zoom.site()
                    : zoom.image() != null ? "image:" + zoom.image() : "blueprint:" + zoom.blueprint());
            if (!zoom.focus().isEmpty()) {
                StringJoiner focus = new StringJoiner(", ");
                for (Focus part : zoom.focus())
                    focus.add(part.name() != null ? part.name()
                            : "rect(" + number(part.rect()[0]) + ", " + number(part.rect()[1]) + ", " + number(part.rect()[2]) + ", " + number(part.rect()[3]) + ")");
                json.add("focus", focus.toString());
            }
            if (zoom.magnify() != 1)
                json.add("magnify", number(zoom.magnify()));
        }
        if (!message.choices.isEmpty()) {
            JsonObject choices = new JsonObject();
            message.choices.forEach(choices::add);
            json.add("choices", choices);
        }
        //A message that is nothing but its text is written as just that
        if (json.size() == 1)
            return JsonValue.valueOf(message.text);
        return json;
    }

    private static String number(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    /** Card-definition requirements, kept as JSON array text: one requirement is written as one { object }. */
    private static JsonValue requirements(String json) {
        JsonValue value = JsonValue.readHjson(json);
        return value.isArray() && value.asArray().size() == 1 ? value.asArray().get(0) : value;
    }

    private static JsonObject perSeat(Map<Seat, String> values) {
        JsonObject json = new JsonObject();
        for (Seat seat : Seat.values())
            if (values.containsKey(seat))
                json.add(seat.key(), values.get(seat));
        return json;
    }

    private static void addMap(JsonObject to, String name, Map<String, String> values) {
        if (values.isEmpty())
            return;
        JsonObject json = new JsonObject();
        values.forEach(json::add);
        to.add(name, json);
    }

    private static void addCounts(JsonObject to, String name, Map<String, Integer> values) {
        if (values.isEmpty())
            return;
        JsonObject json = new JsonObject();
        values.forEach(json::add);
        to.add(name, json);
    }

    private static void addIfSet(JsonObject to, String name, String value) {
        if (value != null && !value.isEmpty())
            to.add(name, value);
    }

    private static String lower(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    /* ------------------------------------------------------------------ the tree as a file */

    /**
     * @param library Used to name each card beside its alias; null to leave the names out.
     */
    public static String toHjson(ScenarioDefinition def, LotroCardBlueprintLibrary library) {
        JsonObject root = toJson(def);
        Map<String, String> comments = new HashMap<>();
        if (library != null) {
            for (CardDef card : def.cards.values())
                comments.put("cards." + card.owner().key() + "." + card.alias(), nameOf(library, card.blueprintId()));
            List<String> sites = new ArrayList<>();
            for (String site : def.sites.getOrDefault(def.human, List.of()))
                sites.add(nameOf(library, site));
            comments.put("sites", String.join(", ", sites));
        }
        for (Step step : def.steps) {
            comments.put("steps." + (step.number - 1), "---- " + step.number + (step.id == null ? "" : " (" + step.id + ")")
                    + (step.block ? ": these in whatever order the game asks for them" : ""));
            for (Step child : step.children)
                comments.put("steps." + (step.number - 1) + ".anyOrder." + (child.sub - 1), "-- " + child.label());
        }

        StringBuilder out = new StringBuilder();
        write(out, root, 0, "", comments);
        out.append('\n');
        return out.toString();
    }

    /**
     * @return Each top-level part of the scenario's file but its steps, as the text the file has for it (a card's name
     * as a comment above it, and so on): what an author copies between the file and the authoring tool.
     */
    public static Map<String, String> parts(ScenarioDefinition def, LotroCardBlueprintLibrary library) {
        JsonObject root = toJson(def);
        Map<String, String> comments = new HashMap<>();
        if (library != null) {
            for (CardDef card : def.cards.values())
                comments.put("cards." + card.owner().key() + "." + card.alias(), nameOf(library, card.blueprintId()));
            List<String> sites = new ArrayList<>();
            for (String site : def.sites.getOrDefault(def.human, List.of()))
                sites.add(nameOf(library, site));
            comments.put("sites", String.join(", ", sites));
        }
        Map<String, String> parts = new java.util.LinkedHashMap<>();
        for (JsonObject.Member member : root) {
            if (member.getName().equals("steps"))
                continue;
            StringBuilder out = new StringBuilder();
            comment(out, comments.get(member.getName()), 0);
            write(out, member.getValue(), 0, member.getName(), comments);
            parts.put(member.getName(), out.toString());
        }
        return parts;
    }

    /** A part of a scenario as the file would have it: what is copied between a file and the authoring tool. */
    public static String text(JsonValue value) {
        StringBuilder out = new StringBuilder();
        write(out, value, 0, "", Map.of());
        return out.toString();
    }

    private static String nameOf(LotroCardBlueprintLibrary library, String blueprintId) {
        try {
            LotroCardBlueprint blueprint = library.getLotroCardBlueprint(blueprintId);
            String name = blueprint.getSubtitle() == null ? blueprint.getTitle() : blueprint.getTitle() + ", " + blueprint.getSubtitle();
            //(card names have an accent or two; none has anything that would end a comment's line)
            return name.replace('\n', ' ').replace('\r', ' ');
        } catch (CardNotFoundException | RuntimeException exp) {
            return "no such card";
        }
    }

    private static void write(StringBuilder out, JsonValue value, int depth, String path, Map<String, String> comments) {
        if (value.isObject()) {
            out.append("{\n");
            for (JsonObject.Member member : value.asObject()) {
                String memberPath = path.isEmpty() ? member.getName() : path + "." + member.getName();
                comment(out, comments.get(memberPath), depth + 1);
                indent(out, depth + 1);
                out.append(member.getName()).append(": ");
                write(out, member.getValue(), depth + 1, memberPath, comments);
                out.append('\n');
            }
            indent(out, depth);
            out.append('}');
        } else if (value.isArray()) {
            out.append("[\n");
            int index = 0;
            for (JsonValue element : value.asArray()) {
                String elementPath = path + "." + index++;
                comment(out, comments.get(elementPath), depth + 1);
                indent(out, depth + 1);
                write(out, element, depth + 1, elementPath, comments);
                out.append('\n');
            }
            indent(out, depth);
            out.append(']');
        } else if (value.isString()) {
            String text = value.asString();
            out.append(needsQuotes(text) ? JsonValue.valueOf(text).toString() : text);
        } else {
            out.append(value.toString());
        }
    }

    private static void comment(StringBuilder out, String comment, int depth) {
        if (comment == null || comment.isEmpty())
            return;
        indent(out, depth);
        out.append("# ").append(comment).append('\n');
    }

    private static void indent(StringBuilder out, int depth) {
        out.append("\t".repeat(depth));
    }

    /**
     * Whether a value would come back as something else if it were written bare.  HJSON reads an unquoted value to
     * the end of its line, as text, unless it starts like something else or is exactly a number, true, false or null.
     */
    static boolean needsQuotes(String text) {
        if (text.isEmpty() || text.strip().length() != text.length())
            return true;
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) < ' ')
                return true;
        char first = text.charAt(0);
        if ("{}[],:\"'#".indexOf(first) >= 0 || text.startsWith("//") || text.startsWith("/*"))
            return true;
        if (text.equals("true") || text.equals("false") || text.equals("null"))
            return true;
        //Anything HJSON would take for a number comes back as a number, which is only harmless when writing the
        // number out again gives the same characters
        if (text.matches("-?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?") && !text.matches("-?(0|[1-9]\\d{0,8})"))
            return true;
        //...and it takes a number (or true, false, null) for all there is if what follows it is a comma, a closing
        // bracket or a comment: "3, then the rest" would come back as 3, and what comes after as something else
        if (text.matches("(-?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?|true|false|null)\\s*(,|\\]|\\}|#|//|/\\*).*"))
            return true;
        return false;
    }
}
