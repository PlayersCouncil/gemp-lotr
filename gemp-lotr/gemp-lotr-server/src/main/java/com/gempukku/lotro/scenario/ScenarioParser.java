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

        checkFields(object, "scenario", "id", "version", "kind", "title", "summary", "author", "tags", "note", "format",
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
        def.note = optionalString(object, "note", null, "scenario");
        def.format = requireString(object, "format", "scenario");
        def.human = parseAbsoluteSeat(optionalString(object, "human", "freeps", "scenario"), "human");
        def.coverage = optionalEnum(object, "coverage", Coverage.class, Coverage.STRICT, "scenario");

        JsonObject opponent = optionalObject(object, "opponent", "scenario");
        if (opponent != null) {
            checkFields(opponent, "opponent", "name", "fallback", "orders");
            def.opponentName = optionalString(opponent, "name", null, "opponent");
            //It is shown wherever a player's name is, the game log among them, so it is held to what a name may be
            if (def.opponentName != null && !OPPONENT_NAME.matcher(def.opponentName).matches())
                throw new ScenarioFormatException("opponent.name: has to be 1 to 30 letters, digits, spaces or any of ' . , -");
            def.fallback = optionalEnum(opponent, "fallback", Fallback.class, Fallback.FAIL, "opponent");
        }

        JsonObject options = optionalObject(object, "options", "scenario");
        if (options != null) {
            checkFields(options, "options", "autoPass", "autoAccept", "rngDefault", "opponentDelayMs", "runnerLevel", "bubbleGap");
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
            def.bubbleGap = gap(optionalString(options, "bubbleGap", null, "options"), "options.bubbleGap");
        }

        parseCards(def, requireObject(object, "cards", "scenario"));
        parsePerSeatAlias(def, optionalObject(object, "ringBearer", "scenario"), def.ringBearer, "ringBearer");
        parsePerSeatAlias(def, optionalObject(object, "ring", "scenario"), def.ring, "ring");
        for (Seat seat : Seat.values())
            if (!def.ringBearer.containsKey(seat))
                throw new ScenarioFormatException("ringBearer: the " + seat.key() + " seat needs a Ring-bearer (the game puts one on the table for each player)");
        //(sites are named as cards are, and can be wherever a card can)
        parseSites(def, requireObject(object, "sites", "scenario"));
        //(after the cards and sites, because orders name them)
        if (opponent != null && get(opponent, "orders") != null)
            parseOrders(def, get(opponent, "orders"));


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
                for (Step child : step.children)
                    if (child.id != null && !ids.add(child.id))
                        throw new ScenarioFormatException("steps: there are two steps called '" + child.id + "'");
                for (Message message : step.messages)
                    for (String name : message.choices.keySet())
                        if (!def.flags.contains(name))
                            def.flags.add(name);
                def.steps.add(step);
            }
        }

        JsonObject outcome = optionalObject(object, "outcome", "scenario");
        if (outcome != null) {
            checkFields(outcome, "outcome", "win", "lose", "loseMessage", "offScript");
            def.outcome.loseMessage = optionalString(outcome, "loseMessage", null, "outcome");
            if (def.outcome.loseMessage != null)
                Templates.check(def, def.outcome.loseMessage, "outcome.loseMessage");
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

    /**
     * opponent.orders: a [list] of { when, answer }.  See ScenarioDefinition.Order.
     */
    private static void parseOrders(ScenarioDefinition def, JsonValue value) throws ScenarioFormatException {
        if (!value.isArray())
            throw new ScenarioFormatException("opponent.orders: has to be a [list] of { when, answer }");
        int index = 0;
        for (JsonValue entry : value.asArray()) {
            index++;
            String context = "opponent.orders[" + index + "]";
            JsonObject order = asObject(entry, context);
            checkFields(order, context, "when", "answer");
            if (order.get("when") == null || order.get("answer") == null)
                throw new ScenarioFormatException(context + ": needs both a 'when' and an 'answer'");
            When when = parseWhen(def, order.get("when"), context + ".when");
            if (when.seat == def.human)
                throw new ScenarioFormatException(context + ".when: orders are the opponent's; this one is about the player's seat");
            when.seat = def.human.other();
            List<Answer> answers = new ArrayList<>();
            for (String line : lines(order.get("answer"), context + ".answer"))
                for (Terms.Term term : Terms.parse(line, context + ".answer"))
                    answers.add(parseAnswer(def, term, context + ".answer"));
            if (answers.isEmpty())
                throw new ScenarioFormatException(context + ".answer: is empty");
            def.orders.add(new Order(when, answers, when.source));
        }
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

    static Step parseStep(ScenarioDefinition def, JsonValue value, int number) throws ScenarioFormatException {
        return parseStep(def, value, number, null, 0);
    }

    /**
     * @param parent The block the step is in, with the place it has there (from 1); null and 0 for a step of the
     *               script itself.
     */
    static Step parseStep(ScenarioDefinition def, JsonValue value, int number, Step parent, int sub) throws ScenarioFormatException {
        //(a block's steps are said as the parts of the file they are, so that whatever reads a complaint can find them)
        String place = parent == null ? "step " + number : "step " + number + ".anyOrder[" + sub + "]";
        JsonObject object = asObject(value, place);
        Step step = new Step();
        //Steps are known by where they come in the list.  A step can be given a name as well, which shows up beside
        // its number in reports; nothing else uses it.
        step.number = number;
        step.parent = parent;
        step.sub = sub;
        step.id = optionalString(object, "id", null, place);
        if (step.id != null && !step.id.matches("[A-Za-z][A-Za-z0-9_-]*"))
            throw new ScenarioFormatException(place + ": '" + step.id + "' is not usable as a step's name (letters, digits, dashes and underscores; starting with a letter)");
        String context = parent == null ? "step " + step.label() : place;
        checkFields(object, context, "id", "note", "when", "until", "skipWhen", "checkpoint", "pause", "if", "unless", "do",
                "messages", "allow", "answer", "end", "anyOrder", "once", "skirmishes");
        step.note = optionalString(object, "note", null, context);
        step.once = optionalBoolean(object, "once", false, context);
        if (step.once && parent == null)
            throw new ScenarioFormatException(context + ": 'once' is for a step in an 'anyOrder' block (a step of the script itself applies once anyway, unless it has an 'until')");

        if (object.get("when") != null)
            step.when = parseWhen(def, object.get("when"), context + ".when");
        if (object.get("until") != null)
            step.until = parseWhen(def, object.get("until"), context + ".until");
        if (object.get("skipWhen") != null)
            step.skipWhen = parseWhen(def, object.get("skipWhen"), context + ".skipWhen");
        step.checkpoint = optionalBoolean(object, "checkpoint", false, context);
        step.pause = optionalBoolean(object, "pause", false, context);
        //A pause about a skirmish that is over was once written for the choice of the next skirmish only
        // ("freeps, cards, after(x), text(next skirmish)"), which there is not after the last one.  It is for the first
        // decision after that skirmish, whatever it is.
        if (step.pause && step.when.after != null && step.when.text != null && step.when.text.trim().equalsIgnoreCase("next skirmish")
                && step.when.require == null && !step.when.source.startsWith("{")) {
            step.when.text = null;
            if (step.when.decision == AwaitingDecisionType.CARD_SELECTION)
                step.when.decision = null;
            step.when.source = ScenarioWriter.terms(step.when);
        }
        step.onlyIf = list(object.get("if"), context + ".if");
        step.unless = list(object.get("unless"), context + ".unless");
        for (String name : step.onlyIf)
            if (!def.flags.contains(name))
                throw new ScenarioFormatException(context + ".if: no earlier step offers a choice called '" + name + "'");
        for (String name : step.unless)
            if (!def.flags.contains(name))
                throw new ScenarioFormatException(context + ".unless: no earlier step offers a choice called '" + name + "'");

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
                //These are shown together on the panel that closes the scenario: there is no game left behind them
                for (Message message : step.end.messages)
                    if (!message.choices.isEmpty() || message.on != null || message.advance == Advance.INSPECT || message.advance == Advance.CLOSE)
                        throw new ScenarioFormatException(context + ".end.messages: closing messages are shown all together when the scenario is over; they cannot offer 'choices' or wait for anything ('on', 'advance')");
            } else {
                step.end.result = parseEnum(scalar(end, context + ".end"), EndResult.class, context + ".end");
            }
        }

        JsonValue anyOrder = object.get("anyOrder");
        if (anyOrder == null && object.get("skirmishes") != null)
            throw new ScenarioFormatException(context + ": 'skirmishes' is said of an 'anyOrder' block");
        if (anyOrder != null) {
            //A block: steps for a stretch of the game whose decisions come in no order the script can know
            if (parent != null)
                throw new ScenarioFormatException(context + ": a block cannot have another block in it");
            if (!anyOrder.isArray())
                throw new ScenarioFormatException(context + ".anyOrder: has to be a [list] of steps");
            if (step.until == null)
                throw new ScenarioFormatException(context + ": an 'anyOrder' block needs an 'until': the decision at which it is over (the next phase, say: until: phase(regroup))");
            if (step.pause || step.allow != null || !step.answers.isEmpty() || !step.messages.isEmpty() || !step.doOps.isEmpty() || step.end != null)
                throw new ScenarioFormatException(context + ": an 'anyOrder' block has no decision of its own, so it has no 'pause', 'messages', 'allow', 'answer', 'do' or 'end': those belong to the steps in it");
            if (step.when.opening)
                throw new ScenarioFormatException(context + ": the opening is one moment, and is a step by itself");
            step.block = true;
            step.skirmishes = optionalBoolean(object, "skirmishes", false, context);
            int place2 = 0;
            for (JsonValue child : anyOrder.asArray()) {
                place2++;
                step.children.add(parseStep(def, child, number, step, place2));
                //(a choice one of them offers is one the steps after it, in the block and out of it, can depend on)
                for (Message message : step.children.getLast().messages)
                    for (String name : message.choices.keySet())
                        if (!def.flags.contains(name))
                            def.flags.add(name);
            }
        }
        if (parent != null) {
            if (step.until != null || step.skipWhen != null)
                throw new ScenarioFormatException(context + ": a step in a block applies whenever a decision fits it, for as long as the block lasts: it has no 'until' or 'skipWhen' of its own (the block has)");
            if (step.checkpoint)
                throw new ScenarioFormatException(context + ": a checkpoint is a place the script always comes to; a step in a block is not.  Put it on the block.");
            if (step.when.opening)
                throw new ScenarioFormatException(context + ": the opening is one moment, and is a step by itself");
            if (step.once && step.answers.size() > 1)
                throw new ScenarioFormatException(context + ": is 'once' and has several answers");
        }

        if (!step.recurs() && step.answers.size() > 1)
            throw new ScenarioFormatException(context + ": has several answers but answers only one decision (give it an 'until' to make it repeat)");
        if ((step.until != null && step.until.opening) || (step.skipWhen != null && step.skipWhen.opening))
            throw new ScenarioFormatException(context + ": 'opening' is something only a 'when' can wait for");

        if (step.when.opening) {
            //The board as it is laid out, before the first turn.  It is read; then its 'do' happens; then the game begins.
            if (step.pause)
                throw new ScenarioFormatException(context + ": the opening is read as it is: it needs no 'pause'");
            if (step.allow != null || !step.answers.isEmpty())
                throw new ScenarioFormatException(context + ": the opening is read, not answered, so it has no 'allow' and no 'answer'");
            if (step.until != null || step.skipWhen != null || !step.onlyIf.isEmpty() || !step.unless.isEmpty() || step.end != null)
                throw new ScenarioFormatException(context + ": the opening happens once, at the start of every game: it has no 'until', 'skipWhen', 'if', 'unless' or 'end'");
            if (step.messages.isEmpty() && step.doOps.isEmpty())
                throw new ScenarioFormatException(context + ": an opening with no messages and no 'do' does nothing");
            for (Message message : step.messages)
                if (message.on != null || message.advance == Advance.DECISION || !message.choices.isEmpty())
                    throw new ScenarioFormatException(context + ".messages: the opening's messages are read one after another; they cannot wait for a decision ('advance: decision', 'on') or offer 'choices'");
            step.when.seat = def.human.other();
        }

        if (step.pause) {
            //It is there to be read.  The decision it is read over belongs to the step after it.
            if (step.allow != null || !step.answers.isEmpty())
                throw new ScenarioFormatException(context + ": a pause is read, not answered, so it has no 'allow' and no 'answer' (those belong to the step after it)");
            if (step.until != null)
                throw new ScenarioFormatException(context + ": a pause is read once, so it has no 'until'");
            if (step.messages.isEmpty())
                throw new ScenarioFormatException(context + ": a pause with no messages has nothing to be read");
            if (step.when.seat == def.human.other())
                throw new ScenarioFormatException(context + ": a pause is over one of the player's own decisions (an opponent's step already waits for its messages to be read)");
            step.when.seat = def.human;
        }
        for (int i = 0; i < step.messages.size(); i++) {
            Message message = step.messages.get(i);
            String where = context + ".messages[" + (i + 1) + "]";
            if (!message.choices.isEmpty()) {
                if (!step.pause)
                    throw new ScenarioFormatException(where + ".choices: only a step with 'pause: true' can offer a choice (the decision in front of the player belongs to the step after it)");
                if (i != step.messages.size() - 1)
                    throw new ScenarioFormatException(where + ".choices: a choice has to be a pause's last message");
            }
            if (step.pause && (message.on != null || message.advance == Advance.DECISION))
                throw new ScenarioFormatException(where + ": a pause's messages are read one after another; they cannot wait for the decision ('advance: decision') or for a moment in answering it ('on')");
        }
        return step;
    }

    private static When parseWhen(ScenarioDefinition def, JsonValue value, String context) throws ScenarioFormatException {
        When when = new When();
        if (value.isObject()) {
            JsonObject object = value.asObject();
            checkFields(object, context, "seat", "decision", "text", "phase", "turn", "skirmish", "offers", "inSkirmish", "fighting", "after", "require");
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
            for (String key : List.of("inSkirmish", "fighting"))
                if (object.get(key) != null)
                    when.fighting = card(def, scalar(object.get(key), context + "." + key), context + "." + key).alias();
            if (object.get("after") != null)
                when.after = card(def, scalar(object.get("after"), context + ".after"), context + ".after").alias();
            if (object.get("require") != null)
                when.require = jsonArrayText(object.get("require"), context + ".require");
            return when;
        }

        when.source = scalar(value, context);
        //(how each term is written from now on: fighting(x) is inSkirmish(x))
        List<String> written = new ArrayList<>();
        boolean renamed = false;
        for (Terms.Term term : Terms.parse(when.source, context)) {
            written.add(term.raw());
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
            else if (term.is("inSkirmish"))
                when.fighting = card(def, term.arg(0, context), context).alias();
            else if (term.is("fighting")) {
                when.fighting = card(def, term.arg(0, context), context).alias();
                written.set(written.size() - 1, "inSkirmish(" + term.arg(0, context) + ")");
                renamed = true;
            }
            else if (term.is("after"))
                when.after = card(def, term.arg(0, context), context).alias();
            else if (term.is("text"))
                when.text = term.wholeArg();
            else if (term.args().isEmpty() && term.is("opening"))
                when.opening = true;
            else
                throw new ScenarioFormatException(context + ": don't know what '" + term.raw() + "' means.  Expected a seat "
                        + "(freeps, shadow, human, opponent), a kind of decision (action, actionChoice, cards, arbitrary, choice, "
                        + "number, assign), or one of phase(...), turn(...), skirmish(n), inSkirmish(card), after(card), offers(card), text(...)");
        }
        if (renamed)
            when.source = String.join(", ", written);
        if (when.opening && (when.seat != null || when.decision != null || when.text != null || when.phase != null || when.turn != null
                || when.skirmish != null || when.offers != null || when.fighting != null || when.after != null))
            throw new ScenarioFormatException(context + ": 'opening' is the moment before the first turn, and stands by itself");
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
            List<Pick> picks = parsePicks(def, term, context);
            List<String> cards = new ArrayList<>();
            for (Pick pick : picks)
                if (pick.alias() != null)
                    cards.add(pick.alias());
            return new Answer(AnswerKind.CHOOSE, cards, null, 0, Map.of(), term.raw(), picks);
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

    /**
     * Each seat's adventure path: a list of blueprints, site 1 first ("1_319, 1_327"), the sites then being called
     * site1, site2... (the opponent's site1opp...); or { name: blueprint } in the same order, for sites given names
     * of their own.
     */
    private static void parseSites(ScenarioDefinition def, JsonObject sites) throws ScenarioFormatException {
        for (String name : sites.names()) {
            Seat seat = parseSeat(def, name, "sites");
            String context = "sites." + name;
            List<String> blueprints = new ArrayList<>();
            List<String> aliases = new ArrayList<>();
            JsonValue value = sites.get(name);
            if (value.isObject()) {
                for (JsonObject.Member member : value.asObject()) {
                    aliases.add(member.getName().trim());
                    blueprints.add(scalar(member.getValue(), context + "." + member.getName()).trim());
                }
            } else {
                blueprints.addAll(list(value, context));
                for (int number = 1; number <= blueprints.size(); number++)
                    aliases.add(def.defaultSiteAlias(seat, number));
            }
            for (int at = 0; at < aliases.size(); at++) {
                String alias = aliases.get(at);
                if (!alias.matches("[A-Za-z][A-Za-z0-9_-]*"))
                    throw new ScenarioFormatException(context + ": '" + alias + "' is not usable as a site's name (letters, digits, dashes and underscores; starting with a letter)");
                if (def.cards.containsKey(alias) || def.siteCards.containsKey(alias))
                    throw new ScenarioFormatException(context + ": '" + alias + "' is already the name of another card or site");
                def.siteCards.put(alias, new CardDef(alias, blueprints.get(at), seat));
            }
            def.sites.put(seat, blueprints);
            def.siteAliases.put(seat, aliases);
        }
        for (Seat seat : Seat.values())
            if (def.sites.getOrDefault(seat, List.of()).isEmpty())
                throw new ScenarioFormatException("sites: the " + seat.key() + " seat needs at least site 1");
    }

    /**
     * The cards a choose(...) picks.  Each of its parameters that names a card ("aragorn"), or picks one by its
     * skirmish ("skirmishing", "skirmishing(aragorn)"), begins another pick; a number says which of the cards it could
     * be (counting from 1), and anything else is a card filter that narrows the pick before it:
     * choose(skirmishing(aragorn), title(Orthanc Warrior), 2), choose(aragorn, not(exhausted)), choose(frodo, sam).
     */
    static List<Pick> parsePicks(ScenarioDefinition def, Terms.Term term, String context) throws ScenarioFormatException {
        List<Pick> picks = new ArrayList<>();
        String alias = null, against = null, source = null;
        boolean skirmishing = false, open = false;
        List<String> filters = new ArrayList<>();
        int ordinal = 0;
        for (String arg : term.args()) {
            if (arg.isEmpty())
                continue;
            Terms.Term part = Terms.parse(arg, context).getFirst();
            boolean starts = part.is("skirmishing") || (part.args().isEmpty() && isAlias(def, arg));
            if (starts) {
                if (open)
                    picks.add(pick(alias, skirmishing, against, filters, ordinal, source, context));
                open = true;
                alias = null; against = null; skirmishing = false; filters = new ArrayList<>(); ordinal = 0; source = arg;
                if (part.is("skirmishing")) {
                    skirmishing = true;
                    if (!part.args().isEmpty() && !part.args().getFirst().isEmpty())
                        against = card(def, part.args().getFirst(), context).alias();
                } else {
                    alias = card(def, arg, context).alias();
                }
                continue;
            }
            if (!open)
                throw new ScenarioFormatException(context + ": in '" + term.raw() + "', '" + arg + "' comes before any card: "
                        + "begin with a card's name in the scenario, or skirmishing / skirmishing(character)");
            source += ", " + arg;
            if (arg.matches("\\d+")) {
                ordinal = Integer.parseInt(arg);
                if (ordinal < 1)
                    throw new ScenarioFormatException(context + ": in '" + term.raw() + "', which card it is counts from 1");
                continue;
            }
            String filter = ScenarioTable.cardFilter(def, arg);
            try {
                ScenarioTable.checkFilter(filter);
            } catch (IllegalArgumentException exp) {
                throw new ScenarioFormatException(context + ": in '" + term.raw() + "', '" + arg + "' is neither a card of the "
                        + "scenario nor a card filter (" + exp.getMessage() + ")");
            }
            filters.add(filter);
        }
        if (open)
            picks.add(pick(alias, skirmishing, against, filters, ordinal, source, context));
        return picks;
    }

    private static Pick pick(String alias, boolean skirmishing, String against, List<String> filters, int ordinal, String source,
                             String context) {
        return new Pick(alias, skirmishing, against, filters.isEmpty() ? null : String.join(",", filters), ordinal, source);
    }

    /** Whether a word is the name of one of the scenario's cards (or sites). */
    static boolean isAlias(ScenarioDefinition def, String word) {
        return def.cards.containsKey(word.trim()) || def.siteCards.containsKey(word.trim());
    }

    static DoOp parseDo(ScenarioDefinition def, Terms.Term term, String context) throws ScenarioFormatException {
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
        if (term.is("draw")) {
            //draw(n) is the person's own seat; draw(seat, n) says whose
            boolean seated = term.args().size() > 1;
            Seat seat = seated ? parseSeat(def, term.arg(0, context), context) : def.human;
            int amount = parseInt(term.arg(seated ? 1 : 0, context), context);
            if (amount < 1 || amount > 60)
                throw new ScenarioFormatException(context + ": " + term.raw() + ": a number of cards from 1 to 60");
            return new DoOp(DoKind.DRAW, null, seat.key(), amount, false, term.raw());
        }
        if (term.is("order")) {
            //order(freeps.hand, a, b, c): those cards first (topmost, in a pile), in that order; the rest after them
            String target = term.arg(0, context).trim();
            ZoneRef ref = parseZoneRef(def, target, context);
            if (target.split("\\.").length != 2 || !ORDERED.contains(ref.zone()))
                throw new ScenarioFormatException(context + ": " + term.raw() + ": what has an order is a hand, a deck, a discard pile or a dead pile (freeps.hand, shadow.deck)");
            List<String> cards = new ArrayList<>();
            for (int i = 1; i < term.args().size(); i++) {
                CardDef card = card(def, term.arg(i, context), context);
                if (card.owner() != ref.seat())
                    throw new ScenarioFormatException(context + ": " + term.raw() + ": '" + card.alias() + "' belongs to the other seat");
                if (cards.contains(card.alias()))
                    throw new ScenarioFormatException(context + ": " + term.raw() + ": '" + card.alias() + "' is there twice");
                cards.add(card.alias());
            }
            if (cards.isEmpty())
                throw new ScenarioFormatException(context + ": " + term.raw() + ": needs the cards, in the order they are to come");
            return new DoOp(DoKind.ORDER, String.join(", ", cards), target, 0, false, term.raw());
        }
        throw new ScenarioFormatException(context + ": don't know what '" + term.raw() + "' means.  Expected one of "
                + "move(card, seat.zone), attach(card, bearer), stack(card, host), wound(card, n), heal(card, n), tokens(card, n), "
                + "draw(n) or draw(seat, n), order(seat.zone, card, card), twilight(n), burdens(n), threats(n) (write +n or -n to change a total rather than set it)");
    }

    /** The places whose cards come in an order that matters: a hand as it is shown, a pile from the top down. */
    public static final Set<SetupZone> ORDERED = EnumSet.of(SetupZone.HAND, SetupZone.DECK, SetupZone.DISCARD, SetupZone.DEAD_PILE);

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
                checkFields(object, entryContext, "note", "header", "text", "anchor", "highlight", "highlightStyle", "avoid", "side", "gap", "advance", "on",
                        "zoom", "focus", "magnify", "choices");
                message.note = optionalString(object, "note", null, entryContext);
                message.header = sidesNamed(def, optionalString(object, "header", null, entryContext));
                message.text = sidesNamed(def, requireString(object, "text", entryContext));
                message.anchor = sidesNamed(def, list(object.get("anchor"), entryContext + ".anchor"));
                message.gap = gap(optionalString(object, "gap", null, entryContext), entryContext + ".gap");
                message.highlight = sidesNamed(def, list(object.get("highlight"), entryContext + ".highlight"));
                message.highlightStyle = optionalString(object, "highlightStyle", "gold", entryContext).toLowerCase(Locale.ROOT);
                if (!HIGHLIGHT_STYLES.contains(message.highlightStyle))
                    throw new ScenarioFormatException(entryContext + ".highlightStyle: has to be one of " + String.join(", ", HIGHLIGHT_STYLES));
                message.avoid = sidesNamed(def, list(object.get("avoid"), entryContext + ".avoid"));
                message.side = optionalString(object, "side", "auto", entryContext).toLowerCase(Locale.ROOT);
                if (!List.of("auto", "above", "below", "left", "right").contains(message.side))
                    throw new ScenarioFormatException(entryContext + ".side: has to be auto, above, below, left or right");
                String advance = optionalString(object, "advance", null, entryContext);
                if (advance != null)
                    parseAdvance(def, message, advance, entryContext + ".advance");
                JsonObject choices = optionalObject(object, "choices", entryContext);
                if (choices != null) {
                    for (String name : choices.names()) {
                        if (!name.matches("[A-Za-z][A-Za-z0-9_-]*"))
                            throw new ScenarioFormatException(entryContext + ".choices: '" + name + "' is not usable as a choice's name (letters, digits, dashes and underscores; starting with a letter)");
                        message.choices.put(name, scalar(choices.get(name), entryContext + ".choices." + name));
                    }
                    if (message.choices.size() < 2)
                        throw new ScenarioFormatException(entryContext + ".choices: a choice needs at least two buttons");
                    if (message.advance != null && message.advance != Advance.NEXT)
                        throw new ScenarioFormatException(entryContext + ": a message with 'choices' is left by pressing one of them, so it has no 'advance'");
                }
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
                    if (message.advance != null && message.advance != Advance.NEXT)
                        throw new ScenarioFormatException(entryContext + ".advance: a message with 'zoom' always waits for Next");
                    message.advance = Advance.NEXT;
                }
                if (message.on != null && (message.advance == Advance.INSPECT || message.advance == Advance.CLOSE))
                    throw new ScenarioFormatException(entryContext + ".advance: a message tied to a moment ('on') stays until the next moment or the answer");
            } else {
                message.text = sidesNamed(def, scalar(entry, entryContext));
            }

            for (String anchor : message.anchor)
                Anchors.check(def, anchor, entryContext + ".anchor");
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
     * The skirmish box's totals were once named for the sides of the game (fpStrength, shadowStrength); they are
     * named for the person's side and the other (selfStrength, oppStrength), and what is written the old way is read
     * the new.
     */
    static String sidesNamed(ScenarioDefinition def, String text) {
        if (text == null)
            return null;
        boolean freeps = def.human == Seat.FREEPS;
        return text.replaceAll("(?i)(ui:skirmish:)fpStrength\\b", "$1" + (freeps ? "selfStrength" : "oppStrength"))
                .replaceAll("(?i)(ui:skirmish:)shadowStrength\\b", "$1" + (freeps ? "oppStrength" : "selfStrength"))
                .replaceAll("(?i)\\{fpStrength}", freeps ? "{selfStrength}" : "{oppStrength}")
                .replaceAll("(?i)\\{shadowStrength}", freeps ? "{oppStrength}" : "{selfStrength}");
    }

    static List<String> sidesNamed(ScenarioDefinition def, List<String> anchors) {
        List<String> named = new ArrayList<>();
        for (String anchor : anchors)
            named.add(sidesNamed(def, anchor));
        return named;
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
            throw new ScenarioFormatException(context + ".zoom: '" + zoom + "' has to be card:<alias>, site:<number>, blueprint:<id> or image:<file>");
        String what = parts[1].trim();
        String alias = null;
        Integer site = null;
        String blueprint = null;
        String image = null;
        switch (parts[0].trim().toLowerCase(Locale.ROOT)) {
            case "image" -> {
                if (!what.matches(Zoom.IMAGE_NAME))
                    throw new ScenarioFormatException(context + ".zoom: '" + what + "' is not the name of a picture: letters, digits, spaces, dots, "
                            + "dashes and underscores, ending in .png, .jpg, .gif or .webp (a file in the web folder's images/scenarios)");
                image = what;
            }
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
            default -> throw new ScenarioFormatException(context + ".zoom: '" + zoom + "' has to be card:<alias>, site:<number>, blueprint:<id> or image:<file>");
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
        }
        if (image != null)
            for (Focus part : focus)
                if (part.name() != null)
                    throw new ScenarioFormatException(context + ".focus: a picture has no named parts ('" + part.name() + "'): say where with rect(left, top, width, height), in percent of the picture");
        return new Zoom(alias, site, blueprint, image, focus, magnify);
    }

    /**
     * A distance between a bubble and what it points at: "2.5" (hundredths of the window's shorter side) or "24px".
     * It may be less than nothing, for a bubble that is to sit over the edge of a large thing.
     */
    private static String gap(String text, String context) throws ScenarioFormatException {
        if (text == null || text.isBlank())
            return null;
        String written = text.trim().toLowerCase(Locale.ROOT);
        if (!written.matches("-?\\d+(\\.\\d+)?(px)?"))
            throw new ScenarioFormatException(context + ": '" + text + "' is not a distance.  Write a number (hundredths of the window's shorter side: "
                    + "2 is a little, 10 is a lot) or a number of pixels as 24px");
        double amount = Double.parseDouble(written.replace("px", ""));
        if (written.endsWith("px") ? Math.abs(amount) > 1000 : Math.abs(amount) > 60)
            throw new ScenarioFormatException(context + ": " + text + " is further than any screen is wide");
        return written;
    }

    /**
     * advance: next | decision | inspect | inspect(card) | close
     */
    private static void parseAdvance(ScenarioDefinition def, Message message, String text, String context) throws ScenarioFormatException {
        List<Terms.Term> terms = Terms.parse(text, context);
        if (terms.size() == 1) {
            Terms.Term term = terms.getFirst();
            if (term.is("next") && term.args().isEmpty()) {
                message.advance = Advance.NEXT;
                return;
            }
            if (term.is("decision") && term.args().isEmpty()) {
                message.advance = Advance.DECISION;
                return;
            }
            if (term.is("close") && term.args().isEmpty()) {
                message.advance = Advance.CLOSE;
                return;
            }
            if (term.is("inspect") && term.args().size() <= 1) {
                message.advance = Advance.INSPECT;
                if (!term.args().isEmpty() && !term.args().getFirst().isEmpty())
                    message.advanceCard = card(def, term.args().getFirst(), context).alias();
                return;
            }
        }
        throw new ScenarioFormatException(context + ": don't know what '" + text + "' means.  Expected one of next, decision, "
                + "inspect (the player opens a card's information window), inspect(card), close (the player closes it again)");
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
                for (String part : Terms.split(line, ',', context)) {
                    //(every answer is allowed if it is on offer and no mistake if it is not: the ? that used to say so
                    // for one answer is still read, and means nothing more)
                    String written = part.endsWith("?") ? part.substring(0, part.length() - 1).trim() : part;
                    for (Terms.Term term : Terms.parse(written, context))
                        allow.answers.add(parseAnswer(def, term, context));
                }
        if (allow.answers.isEmpty() && allow.passWhen == null)
            throw new ScenarioFormatException(context + ": allows nothing at all");
        return allow;
    }

    /* ---- small readers ---- */

    static CardDef card(ScenarioDefinition def, String alias, String context) throws ScenarioFormatException {
        CardDef card = def.cards.get(alias.trim());
        if (card == null)
            card = def.siteCards.get(alias.trim());
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
