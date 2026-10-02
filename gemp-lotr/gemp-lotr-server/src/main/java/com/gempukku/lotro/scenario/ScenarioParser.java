package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.scenario.ScenarioDefinition.*;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads a scenario file (HJSON) into a ScenarioDefinition.
 *
 * Scenario files are written the way card definitions are: one key per line, no quotes needed, and lists written on
 * one line with commas (see Terms).  Note what that implies in HJSON: an unquoted value runs to the end of its line,
 * so a comment has to go on a line of its own, and an object or a [list] cannot be closed on the line it was opened
 * on unless its values are quoted.
 *
 * This checks the file's own shape only: that fields are known, values are of the right kind, and every card alias
 * that is used was declared.  Whether the blueprints and the format exist is for ScenarioValidator, and whether the
 * steps actually play out is for ScenarioRunner.
 */
public final class ScenarioParser {
    /** The outlines a client knows how to draw. */
    private static final List<String> HIGHLIGHT_STYLES = List.of("gold", "red", "green", "blue");
    private static final Pattern OPPONENT_NAME = Pattern.compile("[\\p{L}\\p{N} '.,\\-]{1,30}");

    private ScenarioParser() { }

    public static ScenarioDefinition parse(String hjson) throws ScenarioFormatException {
        JsonValue root;
        try {
            root = JsonValue.readHjson(hjson);
        } catch (RuntimeException exp) {
            throw new ScenarioFormatException("Not valid HJSON: " + exp.getMessage()
                    + ".  (Things that commonly cause this: an unquoted value that starts with [ or {, such as text opening with a [[card link]]; "
                    + "a comment on the same line as an unquoted value; an { object } or [list] opened and closed on one line.)", exp);
        }
        if (!root.isObject())
            throw new ScenarioFormatException("A scenario file has to be one { object }");
        JsonObject object = root.asObject();

        checkFields(object, "scenario", "id", "version", "kind", "title", "summary", "author", "tags", "format",
                "human", "opponent", "coverage", "options", "cards", "ringBearer", "ring", "sites", "rules", "setup",
                "rng", "steps", "outcome");

        ScenarioDefinition def = new ScenarioDefinition();
        def.id = requireString(object, "id", "scenario");
        if (!def.id.matches("[a-z0-9][a-z0-9-]*"))
            throw new ScenarioFormatException("id: '" + def.id + "' may only contain lowercase letters, digits and dashes");
        def.version = optionalInt(object, "version", 1, "scenario");
        def.kind = optionalEnum(object, "kind", Kind.class, Kind.TUTORIAL, "scenario");
        def.title = requireString(object, "title", "scenario");
        def.summary = optionalString(object, "summary", null, "scenario");
        def.author = optionalString(object, "author", null, "scenario");
        def.tags = list(object.get("tags"), "tags");
        def.format = requireString(object, "format", "scenario");
        def.human = parseAbsoluteSeat(optionalString(object, "human", "freeps", "scenario"), "human");
        def.coverage = optionalEnum(object, "coverage", Coverage.class, Coverage.STRICT, "scenario");

        JsonObject opponent = optionalObject(object, "opponent", "scenario");
        if (opponent != null) {
            checkFields(opponent, "opponent", "name", "fallback");
            def.opponentName = optionalString(opponent, "name", null, "opponent");
            //It is shown wherever a player's name is, the game log among them, so it is held to what a name may be
            if (def.opponentName != null && !OPPONENT_NAME.matcher(def.opponentName).matches())
                throw new ScenarioFormatException("opponent.name: has to be 1 to 30 letters, digits, spaces or any of ' . , -");
            def.fallback = optionalEnum(opponent, "fallback", Fallback.class, Fallback.FAIL, "opponent");
        }

        JsonObject options = optionalObject(object, "options", "scenario");
        if (options != null) {
            checkFields(options, "options", "autoPass", "autoAccept", "rngDefault", "opponentDelayMs", "runnerLevel");
            JsonValue autoPass = get(options, "autoPass");
            if (autoPass != null) {
                String written = autoPass.isArray() ? "" : scalar(autoPass, "options.autoPass");
                if (written.equalsIgnoreCase("true")) {
                    //The same phases a player gets by default at an ordinary table
                    def.autoPass = EnumSet.of(Phase.FELLOWSHIP, Phase.MANEUVER, Phase.ARCHERY, Phase.ASSIGNMENT, Phase.REGROUP);
                } else if (!written.equalsIgnoreCase("false")) {
                    for (String phase : list(autoPass, "options.autoPass"))
                        def.autoPass.add(parsePhase(phase, "options.autoPass"));
                }
            }
            def.autoAccept = optionalBoolean(options, "autoAccept", true, "options");
            def.rngDefault = optionalEnum(options, "rngDefault", RngDefault.class, RngDefault.IDENTITY, "options");
            def.opponentDelayMs = optionalInt(options, "opponentDelayMs", 900, "options");
            def.runnerLevel = optionalEnum(options, "runnerLevel", RunnerLevel.class, null, "options");
        }

        parseCards(def, requireObject(object, "cards", "scenario"));
        parsePerSeatAlias(def, optionalObject(object, "ringBearer", "scenario"), def.ringBearer, "ringBearer");
        parsePerSeatAlias(def, optionalObject(object, "ring", "scenario"), def.ring, "ring");
        for (Seat seat : Seat.values())
            if (!def.ringBearer.containsKey(seat))
                throw new ScenarioFormatException("ringBearer: the " + seat.key() + " seat needs a Ring-bearer (the game puts one on the table for each player)");

        JsonObject sites = requireObject(object, "sites", "scenario");
        for (String name : sites.names())
            def.sites.put(parseSeat(def, name, "sites"), list(sites.get(name), "sites." + name));
        for (Seat seat : Seat.values())
            if (def.sites.getOrDefault(seat, List.of()).isEmpty())
                throw new ScenarioFormatException("sites: the " + seat.key() + " seat needs at least site 1");

        JsonObject rules = optionalObject(object, "rules", "scenario");
        if (rules != null) {
            for (String name : rules.names()) {
                if (name.equalsIgnoreCase("oneSided")) {
                    def.oneSided = optionalBoolean(rules, name, false, "rules");
                } else {
                    def.rules.put(parseSeat(def, name, "rules"), jsonArrayText(rules.get(name), "rules." + name));
                }
            }
        }

        parseSetup(def, optionalObject(object, "setup", "scenario"));

        JsonValue rng = object.get("rng");
        if (rng != null)
            for (String line : lines(rng, "rng"))
                for (Terms.Term term : Terms.parse(line, "rng"))
                    def.rng.add(parseRng(def, term));

        JsonValue steps = object.get("steps");
        if (steps != null) {
            if (!steps.isArray())
                throw new ScenarioFormatException("steps: has to be a [list] of steps");
            Set<String> ids = new HashSet<>();
            int index = 0;
            for (JsonValue stepValue : steps.asArray()) {
                index++;
                Step step = parseStep(def, stepValue, index);
                if (step.id != null && !ids.add(step.id))
                    throw new ScenarioFormatException("steps: there are two steps called '" + step.id + "'");
                def.steps.add(step);
            }
        }

        JsonObject outcome = optionalObject(object, "outcome", "scenario");
        if (outcome != null) {
            checkFields(outcome, "outcome", "win", "lose", "offScript");
            if (outcome.get("win") != null)
                def.outcome.win = jsonArrayText(outcome.get("win"), "outcome.win");
            if (outcome.get("lose") != null)
                def.outcome.lose = jsonArrayText(outcome.get("lose"), "outcome.lose");
            JsonObject offScript = optionalObject(outcome, "offScript", "outcome");
            if (offScript != null) {
                checkFields(offScript, "outcome.offScript", "message", "result");
                def.outcome.offScriptMessage = optionalString(offScript, "message", null, "outcome.offScript");
                if (def.outcome.offScriptMessage != null)
                    Templates.check(def, def.outcome.offScriptMessage, "outcome.offScript.message");
                def.outcome.offScriptResult = optionalEnum(offScript, "result", OffScriptResult.class, OffScriptResult.RESTART, "outcome.offScript");
            }
        }

        return def;
    }

    private static void parseCards(ScenarioDefinition def, JsonObject cards) throws ScenarioFormatException {
        for (String seatName : cards.names()) {
            Seat seat = parseSeat(def, seatName, "cards");
            JsonValue seatCards = cards.get(seatName);
            if (!seatCards.isObject())
                throw new ScenarioFormatException("cards." + seatName + ": has to be an { object } of alias: blueprint");
            for (String alias : seatCards.asObject().names()) {
                if (!alias.matches("[A-Za-z][A-Za-z0-9_-]*"))
                    throw new ScenarioFormatException("cards." + seatName + ": '" + alias + "' is not usable as a card alias (letters, digits, dashes and underscores; starting with a letter)");
                if (def.cards.containsKey(alias))
                    throw new ScenarioFormatException("cards: the alias '" + alias + "' is used twice");
                def.cards.put(alias, new CardDef(alias, scalar(seatCards.asObject().get(alias), "cards." + seatName + "." + alias), seat));
            }
        }
    }

    private static void parsePerSeatAlias(ScenarioDefinition def, JsonObject object, Map<Seat, String> target, String context) throws ScenarioFormatException {
        if (object == null)
            return;
        for (String seatName : object.names()) {
            Seat seat = parseSeat(def, seatName, context);
            String alias = scalar(object.get(seatName), context + "." + seatName);
            if (alias.equalsIgnoreCase("none"))
                continue;
            CardDef card = card(def, alias, context + "." + seatName);
            if (card.owner() != seat)
                throw new ScenarioFormatException(context + "." + seatName + ": '" + alias + "' belongs to the other seat");
            target.put(seat, alias);
        }
    }

    private static void parseSetup(ScenarioDefinition def, JsonObject setup) throws ScenarioFormatException {
        if (setup == null)
            return;
        Setup result = def.setup;
        Set<String> placed = new HashSet<>();
        for (String name : setup.names()) {
            JsonValue value = setup.get(name);
            switch (name.toLowerCase(Locale.ROOT)) {
                case "startat" -> {
                    JsonObject startAt = asObject(value, "setup.startAt");
                    checkFields(startAt, "setup.startAt", "turn", "phase");
                    result.turn = optionalInt(startAt, "turn", 1, "setup.startAt");
                    result.phase = parsePhase(optionalString(startAt, "phase", "fellowship", "setup.startAt"), "setup.startAt.phase");
                }
                case "attach" -> aliasMap(def, asObject(value, "setup.attach"), result.attach, "setup.attach");
                case "stack" -> aliasMap(def, asObject(value, "setup.stack"), result.stack, "setup.stack");
                case "wounds" -> countMap(def, asObject(value, "setup.wounds"), result.wounds, "setup.wounds");
                case "tokens" -> countMap(def, asObject(value, "setup.tokens"), result.tokens, "setup.tokens");
                case "burdens" -> result.burdens = asInt(value, "setup.burdens");
                case "threats" -> result.threats = asInt(value, "setup.threats");
                default -> {
                    Seat seat = parseSeat(def, name, "setup");
                    JsonObject zones = asObject(value, "setup." + name);
                    Map<SetupZone, List<String>> seatZones = new EnumMap<>(SetupZone.class);
                    for (String zoneName : zones.names()) {
                        String context = "setup." + name + "." + zoneName;
                        SetupZone zone = parseSetupZone(zoneName, context);
                        List<String> aliases = list(zones.get(zoneName), context);
                        for (String alias : aliases) {
                            CardDef card = card(def, alias, context);
                            if (card.owner() != seat)
                                throw new ScenarioFormatException(context + ": '" + alias + "' belongs to the other seat");
                            if (!placed.add(alias))
                                throw new ScenarioFormatException(context + ": '" + alias + "' is placed in more than one zone");
                        }
                        seatZones.put(zone, aliases);
                    }
                    result.zones.put(seat, seatZones);
                }
            }
        }
        for (String alias : result.attach.keySet())
            if (!placed.add(alias))
                throw new ScenarioFormatException("setup.attach: '" + alias + "' is also placed in a zone");
        for (String alias : result.stack.keySet())
            if (!placed.add(alias))
                throw new ScenarioFormatException("setup.stack: '" + alias + "' is also placed in a zone or attached");
    }

    private static RngEntry parseRng(ScenarioDefinition def, Terms.Term term) throws ScenarioFormatException {
        RngKind kind;
        if (term.is("shuffle"))
            kind = RngKind.SHUFFLE;
        else if (term.is("pick"))
            kind = RngKind.PICK;
        else
            throw new ScenarioFormatException("rng: '" + term.raw() + "' is neither shuffle(...) nor pick(...)");

        String whole = term.wholeArg();
        int colon = whole.indexOf(':');
        if (colon < 0)
            throw new ScenarioFormatException("rng: '" + term.raw() + "' should read " + term.name() + "(seat.zone: card, card)");
        String[] pile = whole.substring(0, colon).trim().split("\\.");
        if (pile.length != 2)
            throw new ScenarioFormatException("rng: '" + term.raw() + "' needs a pile such as freeps.deck");
        Seat seat = parseSeat(def, pile[0], "rng");
        String zone = pile[1].trim();
        if (!List.of("deck", "hand", "adventureDeck").contains(zone))
            throw new ScenarioFormatException("rng: '" + term.raw() + "' names a pile that is never random (deck, hand and adventureDeck are)");
        List<String> result = Terms.split(whole.substring(colon + 1), ',', "rng");
        result.removeIf(String::isEmpty);
        for (String alias : result)
            card(def, alias, "rng");
        return new RngEntry(kind, seat, zone, result);
    }

    private static Step parseStep(ScenarioDefinition def, JsonValue value, int number) throws ScenarioFormatException {
        JsonObject object = asObject(value, "step " + number);
        Step step = new Step();
        //Steps are known by where they come in the list.  A step can be given a name as well, which shows up beside
        // its number in reports; nothing else uses it.
        step.number = number;
        step.id = optionalString(object, "id", null, "step " + number);
        if (step.id != null && !step.id.matches("[A-Za-z][A-Za-z0-9_-]*"))
            throw new ScenarioFormatException("step " + number + ": '" + step.id + "' is not usable as a step's name (letters, digits, dashes and underscores; starting with a letter)");
        String context = "step " + step.label();
        checkFields(object, context, "id", "when", "until", "skipWhen", "checkpoint", "do", "messages", "allow",
                "answer", "end");

        if (object.get("when") != null)
            step.when = parseWhen(def, object.get("when"), context + ".when");
        if (object.get("until") != null)
            step.until = parseWhen(def, object.get("until"), context + ".until");
        if (object.get("skipWhen") != null)
            step.skipWhen = parseWhen(def, object.get("skipWhen"), context + ".skipWhen");
        step.checkpoint = optionalBoolean(object, "checkpoint", false, context);

        if (object.get("do") != null)
            for (String line : lines(object.get("do"), context + ".do"))
                for (Terms.Term term : Terms.parse(line, context + ".do"))
                    step.doOps.add(parseDo(def, term, context + ".do"));

        if (object.get("messages") != null)
            step.messages = parseMessages(def, object.get("messages"), context + ".messages");

        if (object.get("allow") != null)
            step.allow = parseAllow(def, object.get("allow"), context + ".allow");

        if (object.get("answer") != null)
            for (String line : lines(object.get("answer"), context + ".answer"))
                for (Terms.Term term : Terms.parse(line, context + ".answer"))
                    step.answers.add(parseAnswer(def, term, context + ".answer"));

        JsonValue end = object.get("end");
        if (end != null) {
            step.end = new End();
            if (end.isObject()) {
                checkFields(end.asObject(), context + ".end", "result", "messages");
                step.end.result = optionalEnum(end.asObject(), "result", EndResult.class, EndResult.COMPLETE, context + ".end");
                if (end.asObject().get("messages") != null)
                    step.end.messages = parseMessages(def, end.asObject().get("messages"), context + ".end.messages");
            } else {
                step.end.result = parseEnum(scalar(end, context + ".end"), EndResult.class, context + ".end");
            }
        }

        if (step.until == null && step.answers.size() > 1)
            throw new ScenarioFormatException(context + ": has several answers but answers only one decision (give it an 'until' to make it repeat)");
        return step;
    }

    private static When parseWhen(ScenarioDefinition def, JsonValue value, String context) throws ScenarioFormatException {
        When when = new When();
        if (value.isObject()) {
            JsonObject object = value.asObject();
            checkFields(object, context, "seat", "decision", "text", "phase", "turn", "skirmish", "offers", "require");
            when.source = object.toString();
            if (object.get("seat") != null)
                when.seat = parseSeat(def, scalar(object.get("seat"), context + ".seat"), context + ".seat");
            if (object.get("decision") != null)
                when.decision = parseDecisionType(scalar(object.get("decision"), context + ".decision"), context + ".decision");
            when.text = optionalString(object, "text", null, context);
            if (object.get("phase") != null)
                when.phase = parsePhase(scalar(object.get("phase"), context + ".phase"), context + ".phase");
            if (object.get("turn") != null)
                when.turn = asInt(object.get("turn"), context + ".turn");
            if (object.get("skirmish") != null)
                when.skirmish = asInt(object.get("skirmish"), context + ".skirmish");
            if (object.get("offers") != null)
                when.offers = card(def, scalar(object.get("offers"), context + ".offers"), context + ".offers").alias();
            if (object.get("require") != null)
                when.require = jsonArrayText(object.get("require"), context + ".require");
            return when;
        }

        when.source = scalar(value, context);
        for (Terms.Term term : Terms.parse(when.source, context)) {
            Seat seat = tryParseSeat(def, term.name());
            AwaitingDecisionType decisionType = tryParseDecisionType(term.name());
            if (term.args().isEmpty() && seat != null)
                when.seat = seat;
            else if (term.args().isEmpty() && decisionType != null)
                when.decision = decisionType;
            else if (term.is("phase"))
                when.phase = parsePhase(term.arg(0, context), context);
            else if (term.is("turn"))
                when.turn = parseInt(term.arg(0, context), context);
            else if (term.is("skirmish"))
                when.skirmish = parseInt(term.arg(0, context), context);
            else if (term.is("offers"))
                when.offers = card(def, term.arg(0, context), context).alias();
            else if (term.is("text"))
                when.text = term.wholeArg();
            else
                throw new ScenarioFormatException(context + ": don't know what '" + term.raw() + "' means.  Expected a seat "
                        + "(freeps, shadow, human, opponent), a kind of decision (action, actionChoice, cards, arbitrary, choice, "
                        + "number, assign), or one of phase(...), turn(...), skirmish(...), offers(...), text(...)");
        }
        return when;
    }

    static Answer parseAnswer(ScenarioDefinition def, Terms.Term term, String context) throws ScenarioFormatException {
        if (term.is("pass"))
            return new Answer(AnswerKind.PASS, List.of(), null, 0, Map.of(), term.raw());
        if (term.is("play"))
            return new Answer(AnswerKind.PLAY, List.of(card(def, term.arg(0, context), context).alias()), null, 0, Map.of(), term.raw());
        if (term.is("use")) {
            String text = term.args().size() > 1 ? String.join(", ", term.args().subList(1, term.args().size())) : null;
            return new Answer(AnswerKind.USE, List.of(card(def, term.arg(0, context), context).alias()), text, 0, Map.of(), term.raw());
        }
        if (term.is("choose")) {
            List<String> cards = new ArrayList<>();
            for (String alias : term.args())
                if (!alias.isEmpty())
                    cards.add(card(def, alias, context).alias());
            return new Answer(AnswerKind.CHOOSE, cards, null, 0, Map.of(), term.raw());
        }
        if (term.is("option"))
            return new Answer(AnswerKind.OPTION, List.of(), term.wholeArg(), 0, Map.of(), term.raw());
        if (term.is("number"))
            return new Answer(AnswerKind.NUMBER, List.of(), null, parseInt(term.arg(0, context), context), Map.of(), term.raw());
        if (term.is("raw"))
            return new Answer(AnswerKind.RAW, List.of(), term.wholeArg(), 0, Map.of(), term.raw());
        if (term.is("assign")) {
            Map<String, List<String>> assignments = new LinkedHashMap<>();
            for (String pairing : term.args()) {
                if (pairing.isEmpty())
                    continue;
                int colon = pairing.indexOf(':');
                String character = (colon < 0 ? pairing : pairing.substring(0, colon)).trim();
                List<String> minions = new ArrayList<>();
                if (colon >= 0)
                    for (String minion : pairing.substring(colon + 1).split("\\+"))
                        if (!minion.trim().isEmpty())
                            minions.add(card(def, minion.trim(), context).alias());
                assignments.put(card(def, character, context).alias(), minions);
            }
            return new Answer(AnswerKind.ASSIGN, List.of(), null, 0, assignments, term.raw());
        }
        throw new ScenarioFormatException(context + ": don't know what '" + term.raw() + "' means.  Expected one of pass, "
                + "play(card), use(card) or use(card, action text), choose(card, card), option(text) or option(#n), number(n), "
                + "assign(character: minion + minion, character: minion), raw(text)");
    }

    private static DoOp parseDo(ScenarioDefinition def, Terms.Term term, String context) throws ScenarioFormatException {
        if (term.is("move")) {
            String target = term.arg(1, context);
            parseZoneRef(def, target, context);
            return new DoOp(DoKind.MOVE, card(def, term.arg(0, context), context).alias(), target, 0, false, term.raw());
        }
        if (term.is("attach") || term.is("stack"))
            return new DoOp(term.is("attach") ? DoKind.ATTACH : DoKind.STACK,
                    card(def, term.arg(0, context), context).alias(), card(def, term.arg(1, context), context).alias(), 0, false, term.raw());
        if (term.is("wound") || term.is("heal") || term.is("tokens")) {
            int amount = term.args().size() > 1 ? parseInt(term.arg(1, context), context) : 1;
            DoKind kind = term.is("wound") ? DoKind.WOUND : term.is("heal") ? DoKind.HEAL : DoKind.TOKENS;
            return new DoOp(kind, card(def, term.arg(0, context), context).alias(), null, amount, false, term.raw());
        }
        if (term.is("twilight") || term.is("burdens") || term.is("threats")) {
            String amount = term.arg(0, context);
            boolean relative = amount.startsWith("+") || amount.startsWith("-");
            DoKind kind = term.is("twilight") ? DoKind.TWILIGHT : term.is("burdens") ? DoKind.BURDENS : DoKind.THREATS;
            return new DoOp(kind, null, null, parseInt(amount.startsWith("+") ? amount.substring(1) : amount, context), relative, term.raw());
        }
        throw new ScenarioFormatException(context + ": don't know what '" + term.raw() + "' means.  Expected one of "
                + "move(card, seat.zone), attach(card, bearer), stack(card, host), wound(card, n), heal(card, n), tokens(card, n), "
                + "twilight(n), burdens(n), threats(n) (write +n or -n to change a total rather than set it)");
    }

    /**
     * A place on the board a card can be moved to mid-game: "freeps.hand", "shadow.deck.top", and so on.
     */
    public record ZoneRef(Seat seat, SetupZone zone, boolean bottom) { }

    public static ZoneRef parseZoneRef(ScenarioDefinition def, String text, String context) throws ScenarioFormatException {
        String[] parts = text.trim().split("\\.");
        if (parts.length < 2 || parts.length > 3)
            throw new ScenarioFormatException(context + ": '" + text + "' should name a seat and a zone, e.g. freeps.hand or shadow.deck.bottom");
        Seat seat = parseSeat(def, parts[0], context);
        SetupZone zone = parseSetupZone(parts[1], context);
        boolean bottom = false;
        if (parts.length == 3) {
            if (zone != SetupZone.DECK || !(parts[2].equalsIgnoreCase("top") || parts[2].equalsIgnoreCase("bottom")))
                throw new ScenarioFormatException(context + ": '" + text + "': only the deck has a .top and a .bottom");
            bottom = parts[2].equalsIgnoreCase("bottom");
        }
        return new ZoneRef(seat, zone, bottom);
    }

    private static List<Message> parseMessages(ScenarioDefinition def, JsonValue value, String context) throws ScenarioFormatException {
        List<Message> result = new ArrayList<>();
        List<JsonValue> entries = new ArrayList<>();
        if (value.isArray())
            value.asArray().forEach(entries::add);
        else
            entries.add(value);

        int index = 0;
        for (JsonValue entry : entries) {
            index++;
            String entryContext = context + "[" + index + "]";
            Message message = new Message();
            if (entry.isObject()) {
                JsonObject object = entry.asObject();
                if (object.get("title") != null)
                    throw new ScenarioFormatException(entryContext + ": a message's heading is called 'header', not 'title' (and most messages want none)");
                checkFields(object, entryContext, "header", "text", "anchor", "highlight", "highlightStyle", "avoid", "side", "advance", "on",
                        "zoom", "focus", "magnify");
                message.header = optionalString(object, "header", null, entryContext);
                message.text = requireString(object, "text", entryContext);
                message.anchor = optionalString(object, "anchor", null, entryContext);
                message.highlight = list(object.get("highlight"), entryContext + ".highlight");
                message.highlightStyle = optionalString(object, "highlightStyle", "gold", entryContext).toLowerCase(Locale.ROOT);
                if (!HIGHLIGHT_STYLES.contains(message.highlightStyle))
                    throw new ScenarioFormatException(entryContext + ".highlightStyle: has to be one of " + String.join(", ", HIGHLIGHT_STYLES));
                message.avoid = list(object.get("avoid"), entryContext + ".avoid");
                message.side = optionalString(object, "side", "auto", entryContext).toLowerCase(Locale.ROOT);
                if (!List.of("auto", "above", "below", "left", "right").contains(message.side))
                    throw new ScenarioFormatException(entryContext + ".side: has to be auto, above, below, left or right");
                message.advance = optionalEnum(object, "advance", Advance.class, null, entryContext);
                String on = optionalString(object, "on", null, entryContext);
                if (on != null)
                    message.on = parseMoment(def, on, entryContext + ".on");
                //"start" is what a message with no moment already means, and everything downstream tells the two
                // kinds of message apart by whether there is a moment at all
                if (message.on != null && message.on.kind() == MomentKind.START)
                    message.on = null;
                message.zoom = parseZoom(def, object, entryContext);
                if (message.zoom != null) {
                    //A card shown large is in the way of the table, so it is read and then put away
                    if (message.on != null)
                        throw new ScenarioFormatException(entryContext + ": a message with 'zoom' cannot also wait for a moment ('on')");
                    if (message.advance == Advance.DECISION)
                        throw new ScenarioFormatException(entryContext + ".advance: a message with 'zoom' always waits for Next");
                    message.advance = Advance.NEXT;
                }
            } else {
                message.text = scalar(entry, entryContext);
            }

            if (message.anchor != null)
                Anchors.check(def, message.anchor, entryContext + ".anchor");
            for (String anchor : message.highlight)
                Anchors.check(def, anchor, entryContext + ".highlight");
            for (String anchor : message.avoid)
                Anchors.check(def, anchor, entryContext + ".avoid");
            Templates.check(def, message.text, entryContext + ".text");
            if (message.header != null)
                Templates.check(def, message.header, entryContext + ".header");
            result.add(message);
        }
        return result;
    }

    /**
     * zoom: card:<alias> | site:<n> | blueprint:<id>, with optional focus (region names or rect(l, t, w, h) in
     * percent of the card) and magnify.
     */
    private static Zoom parseZoom(ScenarioDefinition def, JsonObject object, String context) throws ScenarioFormatException {
        String zoom = optionalString(object, "zoom", null, context);
        if (zoom == null) {
            if (object.get("focus") != null || object.get("magnify") != null)
                throw new ScenarioFormatException(context + ": 'focus' and 'magnify' belong to a 'zoom'");
            return null;
        }
        String[] parts = zoom.trim().split(":", 2);
        if (parts.length != 2 || parts[1].isBlank())
            throw new ScenarioFormatException(context + ".zoom: '" + zoom + "' has to be card:<alias>, site:<number> or blueprint:<id>");
        String what = parts[1].trim();
        String alias = null;
        Integer site = null;
        String blueprint = null;
        switch (parts[0].trim().toLowerCase(Locale.ROOT)) {
            case "card" -> alias = card(def, what, context + ".zoom").alias();
            case "site" -> {
                if (!what.matches("[1-9]"))
                    throw new ScenarioFormatException(context + ".zoom: '" + what + "' is not a site number");
                site = Integer.parseInt(what);
            }
            case "blueprint" -> {
                if (!what.matches("\\d+_\\d+[*T]?"))
                    throw new ScenarioFormatException(context + ".zoom: '" + what + "' is not a blueprint ID");
                blueprint = what;
            }
            default -> throw new ScenarioFormatException(context + ".zoom: '" + zoom + "' has to be card:<alias>, site:<number> or blueprint:<id>");
        }

        List<Focus> focus = new ArrayList<>();
        JsonValue focusValue = object.get("focus");
        if (focusValue != null && !focusValue.isNull()) {
            for (String line : lines(focusValue, context + ".focus")) {
                for (Terms.Term term : Terms.parse(line, context + ".focus")) {
                    if (term.is("rect")) {
                        if (term.args().size() != 4)
                            throw new ScenarioFormatException(context + ".focus: rect needs left, top, width and height, in percent of the card");
                        double[] rect = new double[4];
                        for (int i = 0; i < 4; i++) {
                            try {
                                rect[i] = Double.parseDouble(term.args().get(i).trim());
                            } catch (NumberFormatException exp) {
                                throw new ScenarioFormatException(context + ".focus: '" + term.args().get(i) + "' is not a number");
                            }
                        }
                        if (rect[0] < 0 || rect[1] < 0 || rect[2] <= 0 || rect[3] <= 0 || rect[0] + rect[2] > 100 || rect[1] + rect[3] > 100)
                            throw new ScenarioFormatException(context + ".focus: " + term.raw() + " does not lie on the card (everything is in percent, 0 to 100)");
                        focus.add(new Focus(null, rect));
                    } else if (term.args().isEmpty() && CardFaces.known(term.name())) {
                        focus.add(new Focus(term.name(), null));
                    } else {
                        throw new ScenarioFormatException(context + ".focus: don't know what '" + term.raw() + "' means.  Expected rect(left, top, width, height) or one of "
                                + String.join(", ", CardFaces.names()));
                    }
                }
            }
        }

        double magnify = 1;
        String magnifyText = optionalString(object, "magnify", null, context);
        if (magnifyText != null) {
            try {
                magnify = Double.parseDouble(magnifyText.trim());
            } catch (NumberFormatException exp) {
                throw new ScenarioFormatException(context + ".magnify: '" + magnifyText + "' is not a number");
            }
            if (magnify < 1 || magnify > 6)
                throw new ScenarioFormatException(context + ".magnify: has to be from 1 (the whole card) to 6");
            if (magnify > 1 && focus.isEmpty())
                throw new ScenarioFormatException(context + ".magnify: a card shown larger than the screen needs a 'focus' to be centred on");
        }
        return new Zoom(alias, site, blueprint, focus, magnify);
    }

    private static Moment parseMoment(ScenarioDefinition def, String text, String context) throws ScenarioFormatException {
        List<Terms.Term> terms = Terms.parse(text, context);
        if (terms.size() == 1) {
            Terms.Term term = terms.getFirst();
            if (term.is("start") && term.args().isEmpty())
                return new Moment(MomentKind.START, null);
            if (term.is("ready") && term.args().isEmpty())
                return new Moment(MomentKind.READY, null);
            if (term.is("picked"))
                return new Moment(MomentKind.PICKED, card(def, term.arg(0, context), context).alias());
            if (term.is("assigned"))
                return new Moment(MomentKind.ASSIGNED, card(def, term.arg(0, context), context).alias());
        }
        throw new ScenarioFormatException(context + ": don't know what '" + text + "' means.  Expected one of start, "
                + "picked(card), assigned(character), ready");
    }

    private static Allow parseAllow(ScenarioDefinition def, JsonValue value, String context) throws ScenarioFormatException {
        Allow allow = new Allow();
        JsonValue answers = value;
        if (value.isObject()) {
            JsonObject object = value.asObject();
            checkFields(object, context, "answers", "passWhen", "otherwise", "hints");
            answers = object.get("answers");
            if (object.get("passWhen") != null)
                allow.passWhen = jsonArrayText(object.get("passWhen"), context + ".passWhen");
            allow.otherwise = optionalString(object, "otherwise", null, context);
            if (allow.otherwise != null)
                Templates.check(def, allow.otherwise, context + ".otherwise");
            JsonObject hints = optionalObject(object, "hints", context);
            if (hints != null)
                for (String alias : hints.names()) {
                    String hint = scalar(hints.get(alias), context + ".hints." + alias);
                    Templates.check(def, hint, context + ".hints." + alias);
                    allow.hints.put(card(def, alias, context + ".hints").alias(), hint);
                }
        }
        if (answers != null)
            for (String line : lines(answers, context))
                for (Terms.Term term : Terms.parse(line, context))
                    allow.answers.add(parseAnswer(def, term, context));
        if (allow.answers.isEmpty() && allow.passWhen == null)
            throw new ScenarioFormatException(context + ": allows nothing at all");
        return allow;
    }

    /* ---- small readers ---- */

    static CardDef card(ScenarioDefinition def, String alias, String context) throws ScenarioFormatException {
        CardDef card = def.cards.get(alias.trim());
        if (card == null)
            throw new ScenarioFormatException(context + ": there is no card called '" + alias.trim() + "' (card aliases are declared under 'cards' and are case-sensitive)");
        return card;
    }

    private static void aliasMap(ScenarioDefinition def, JsonObject object, Map<String, String> target, String context) throws ScenarioFormatException {
        for (String alias : object.names())
            target.put(card(def, alias, context).alias(), card(def, scalar(object.get(alias), context + "." + alias), context + "." + alias).alias());
    }

    private static void countMap(ScenarioDefinition def, JsonObject object, Map<String, Integer> target, String context) throws ScenarioFormatException {
        for (String alias : object.names())
            target.put(card(def, alias, context).alias(), asInt(object.get(alias), context + "." + alias));
    }

    static Seat parseSeat(ScenarioDefinition def, String name, String context) throws ScenarioFormatException {
        Seat seat = tryParseSeat(def, name);
        if (seat == null)
            throw new ScenarioFormatException(context + ": '" + name + "' is not a seat (freeps, shadow, human, opponent)");
        return seat;
    }

    private static Seat tryParseSeat(ScenarioDefinition def, String name) {
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "freeps" -> Seat.FREEPS;
            case "shadow" -> Seat.SHADOW;
            case "human" -> def.human;
            case "opponent" -> def.human.other();
            default -> null;
        };
    }

    private static Seat parseAbsoluteSeat(String name, String context) throws ScenarioFormatException {
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "freeps" -> Seat.FREEPS;
            case "shadow" -> Seat.SHADOW;
            default -> throw new ScenarioFormatException(context + ": has to be freeps or shadow");
        };
    }

    private static AwaitingDecisionType parseDecisionType(String name, String context) throws ScenarioFormatException {
        AwaitingDecisionType type = tryParseDecisionType(name);
        if (type == null)
            throw new ScenarioFormatException(context + ": '" + name + "' is not a kind of decision (action, actionChoice, cards, arbitrary, choice, number, assign)");
        return type;
    }

    private static AwaitingDecisionType tryParseDecisionType(String name) {
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "action", "card_action_choice" -> AwaitingDecisionType.CARD_ACTION_CHOICE;
            case "actionchoice", "action_choice" -> AwaitingDecisionType.ACTION_CHOICE;
            case "cards", "card_selection" -> AwaitingDecisionType.CARD_SELECTION;
            case "arbitrary", "arbitrary_cards" -> AwaitingDecisionType.ARBITRARY_CARDS;
            case "choice", "multiple_choice" -> AwaitingDecisionType.MULTIPLE_CHOICE;
            case "number", "integer" -> AwaitingDecisionType.INTEGER;
            case "assign", "assign_minions" -> AwaitingDecisionType.ASSIGN_MINIONS;
            default -> null;
        };
    }

    private static Phase parsePhase(String name, String context) throws ScenarioFormatException {
        Phase phase = Phase.findPhase(name);
        if (phase == null)
            throw new ScenarioFormatException(context + ": '" + name + "' is not a phase");
        return phase;
    }

    private static SetupZone parseSetupZone(String name, String context) throws ScenarioFormatException {
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "characters" -> SetupZone.CHARACTERS;
            case "support" -> SetupZone.SUPPORT;
            case "minions" -> SetupZone.MINIONS;
            case "hand" -> SetupZone.HAND;
            case "deck" -> SetupZone.DECK;
            case "discard" -> SetupZone.DISCARD;
            case "deadpile" -> SetupZone.DEAD_PILE;
            case "removed" -> SetupZone.REMOVED;
            default -> throw new ScenarioFormatException(context + ": '" + name + "' is not a zone (characters, support, minions, hand, deck, discard, deadPile, removed)");
        };
    }

    private static <E extends Enum<E>> E parseEnum(String name, Class<E> type, String context) throws ScenarioFormatException {
        for (E value : type.getEnumConstants())
            if (value.name().equalsIgnoreCase(name.trim()))
                return value;
        throw new ScenarioFormatException(context + ": '" + name + "' is not one of "
                + Arrays.toString(type.getEnumConstants()).toLowerCase(Locale.ROOT));
    }

    private static int parseInt(String text, String context) throws ScenarioFormatException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exp) {
            throw new ScenarioFormatException(context + ": '" + text + "' is not a whole number");
        }
    }

    private static void checkFields(JsonObject object, String context, String... allowed) throws ScenarioFormatException {
        for (String name : object.names()) {
            boolean known = false;
            for (String candidate : allowed)
                if (candidate.equalsIgnoreCase(name)) {
                    known = true;
                    break;
                }
            if (!known)
                throw new ScenarioFormatException(context + ": unrecognized field '" + name + "' (known fields: " + String.join(", ", allowed) + ")");
        }
    }

    /** Any single value as text, the way it was written. */
    private static String scalar(JsonValue value, String context) throws ScenarioFormatException {
        if (value == null || value.isNull())
            throw new ScenarioFormatException(context + ": is missing");
        if (value.isString())
            return value.asString().trim();
        if (value.isNumber() || value.isBoolean())
            return value.toString();
        throw new ScenarioFormatException(context + ": has to be a single value, not a list or an object");
    }

    private static int asInt(JsonValue value, String context) throws ScenarioFormatException {
        return parseInt(scalar(value, context), context);
    }

    private static JsonObject asObject(JsonValue value, String context) throws ScenarioFormatException {
        if (value == null || !value.isObject())
            throw new ScenarioFormatException(context + ": has to be an { object }");
        return value.asObject();
    }

    private static String requireString(JsonObject object, String name, String context) throws ScenarioFormatException {
        return scalar(get(object, name), context + "." + name);
    }

    private static String optionalString(JsonObject object, String name, String fallback, String context) throws ScenarioFormatException {
        JsonValue value = get(object, name);
        return value == null ? fallback : scalar(value, context + "." + name);
    }

    private static int optionalInt(JsonObject object, String name, int fallback, String context) throws ScenarioFormatException {
        JsonValue value = get(object, name);
        return value == null ? fallback : asInt(value, context + "." + name);
    }

    private static boolean optionalBoolean(JsonObject object, String name, boolean fallback, String context) throws ScenarioFormatException {
        JsonValue value = get(object, name);
        if (value == null)
            return fallback;
        String text = scalar(value, context + "." + name);
        if (text.equalsIgnoreCase("true"))
            return true;
        if (text.equalsIgnoreCase("false"))
            return false;
        throw new ScenarioFormatException(context + "." + name + ": has to be true or false");
    }

    private static <E extends Enum<E>> E optionalEnum(JsonObject object, String name, Class<E> type, E fallback, String context) throws ScenarioFormatException {
        JsonValue value = get(object, name);
        return value == null ? fallback : parseEnum(scalar(value, context + "." + name), type, context + "." + name);
    }

    private static JsonObject requireObject(JsonObject object, String name, String context) throws ScenarioFormatException {
        return asObject(get(object, name), context + "." + name);
    }

    private static JsonObject optionalObject(JsonObject object, String name, String context) throws ScenarioFormatException {
        JsonValue value = get(object, name);
        return value == null ? null : asObject(value, context + "." + name);
    }

    /** Field names are not case-sensitive, the same as in card definitions. */
    private static JsonValue get(JsonObject object, String name) {
        for (String candidate : object.names())
            if (candidate.equalsIgnoreCase(name))
                return object.get(candidate);
        return null;
    }

    /**
     * A list of plain values, written either as a [list] or on one line with commas.  Missing means empty.
     */
    private static List<String> list(JsonValue value, String context) throws ScenarioFormatException {
        List<String> result = new ArrayList<>();
        if (value == null || value.isNull())
            return result;
        for (String line : lines(value, context))
            for (String part : Terms.split(line, ',', context))
                if (!part.isEmpty())
                    result.add(part);
        return result;
    }

    /**
     * The lines of something that can be written either as one value or as a [list] of them.
     */
    private static List<String> lines(JsonValue value, String context) throws ScenarioFormatException {
        List<String> result = new ArrayList<>();
        if (value.isArray()) {
            for (JsonValue entry : value.asArray())
                result.add(scalar(entry, context));
        } else {
            result.add(scalar(value, context));
        }
        return result;
    }

    /**
     * Card-definition JSON (requirements, effects), kept as text for the card builder to parse.  One { object } is
     * accepted as a list of one, the same as in card definitions.
     */
    private static String jsonArrayText(JsonValue value, String context) throws ScenarioFormatException {
        if (value.isObject()) {
            JsonArray array = new JsonArray();
            array.add(value);
            return array.toString();
        }
        if (value.isArray()) {
            for (JsonValue entry : value.asArray())
                if (!entry.isObject())
                    throw new ScenarioFormatException(context + ": every entry has to be an { object }, as in a card definition");
            return value.toString();
        }
        throw new ScenarioFormatException(context + ": has to be an { object } or a [list] of them, as in a card definition");
    }
}
