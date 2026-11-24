package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoKind;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoOp;
import com.gempukku.lotro.scenario.ScenarioDefinition.SetupZone;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A sandbox's table, which the person playing it may stop, rearrange and start again (kind: sandbox).
 *
 * Changing the board and playing are separate modes.  Stop is allowed only where the game can ask its waiting
 * decision again (DefaultLotroGame.canAskAgain: at rest, nothing on the action stack, waiting on a decision a game
 * process asked directly, such as a phase's "play an action or pass").  While stopped, nobody's answer is taken
 * and the scripted opponent does nothing (LotroGameMediator sees to both); the person's edits are made to the game
 * at once.  Start asks the waiting decision again, against the board as it now stands: the game runs its
 * state-based checks first, so a character an edit left with no vitality dies, and whatever that sets off is played
 * out before the decision comes back.  A sandbox opens stopped, at its first such decision.
 *
 * The engine cannot save or restore a game; taking a game back rebuilds it from its start and its journal
 * (LotroGameMediator.revertTo).  So Stop, Start and each list of edits are written into the game's journal as
 * notes, and a game that follows the journal does each again at the same point (see follow).  A card an edit adds
 * gets a name of its own (written in the note, so the rebuilt game gives it the same name) and, because card ids
 * are handed out in a fixed order, the same card id.  Edits name cards by those names, never by id, since a card's
 * id changes as it comes into play.
 *
 * Edits are placements, not plays: no "when played" or "when killed" text answers them, and Delete takes a card out
 * of the game without discarding it (as a scenario's do already behaves).  A card that leaves the table (characters,
 * support, minions) for anywhere else, or is deleted, or is attached or stacked somewhere new, takes nothing with it:
 * whatever was attached to it or stacked on it goes to its owner's discard pile, as the game discards what a
 * character bore when it leaves play.  A card moved from one of those zones to another keeps what it has.
 */
public class ScenarioSandbox {
    /**
     * What a sandbox's player may change, each a name options.tools can list:
     * add (a card by its blueprint), copy (another of a card, in the same place), delete (out of the game),
     * move (to a zone), attach, stack, wounds (add or heal), tokens (culture tokens), modify (a modifier put on a card
     * by hand, or taken off: CardModifiers), counters (twilight, burdens, threats), markers (a seat's marker, and who controls a site),
     * sites (a site put on the path), piles (looking in the hidden piles: both draw decks, the other seat's hand and
     * adventure deck).
     */
    public static final List<String> TOOLS = List.of("add", "copy", "delete", "move", "attach", "stack", "wounds", "tokens",
            "modify", "counters", "markers", "sites", "piles");
    /** Tools whose name is kept but which this tree cannot do yet (none now). */
    public static final Set<String> NOT_BUILT = Set.of();

    /** The journal's notes: detail is one of these, player the person, value what goes with it. */
    static final String STOP = "sandbox.stop";
    static final String START = "sandbox.start";
    static final String EDIT = "sandbox.edit";
    /** A Stop's value when it is the one the sandbox opens with (no place to go back to is made for it). */
    private static final String OPENING = "opening";

    /** Is told of each place a sandbox's game can be taken back to: before a Stop, an edit or a Start. */
    public interface Points {
        void reached(int entryCount, String label);
    }

    /** What came of a list of edits: null problem if all were made; otherwise whether any had been made before one failed. */
    public record Outcome(String problem, boolean partlyMade, List<String> added) {
        static Outcome done(List<String> added) {
            return new Outcome(null, false, added);
        }
    }

    /** One edit that could not be made; what to tell the person. */
    static final class Refused extends Exception {
        Refused(String message) {
            super(message);
        }
    }

    private final ScenarioDefinition _def;
    private final ScenarioTable _table;
    private final String _human;
    private boolean _stopped;
    private Points _points;
    /** Set once the op being made has changed the game: a refusal after that has the game taken back (see edit). */
    private boolean _changed;

    ScenarioSandbox(ScenarioDefinition def, ScenarioTable table, String human) {
        _def = def;
        _table = table;
        _human = human;
        table.OnNote(this::follow);
    }

    public void OnPoint(Points points) {
        _points = points;
    }

    /** The seat the person looks at the table from (LiveScenario.Looking); the one they sit in until told. */
    private java.util.function.Supplier<Seat> _looking;

    void LookingFrom(java.util.function.Supplier<Seat> looking) {
        _looking = looking;
    }

    /** A place to go back to, just before something the person is about to do to the game (LiveScenario.SetOtherSeat). */
    void Point(String label) {
        point(label);
    }

    /** Whether the game is stopped (edit mode): nothing is answered, and the board may be changed. */
    public boolean Stopped() {
        return _stopped;
    }

    public List<String> Tools() {
        return _def.toolsInForce();
    }

    private DefaultLotroGame game() {
        return _table.game();
    }

    /** Whether Stop would be taken now. */
    public boolean CanStop() {
        return !_stopped && !game().isFinished() && game().canAskAgain();
    }

    /** Whether Start would be taken now. */
    public boolean CanStart() {
        return _stopped && !game().isFinished();
    }

    /**
     * The game has begun (LiveScenario.Begin).  A sandbox opens stopped, unless the journal it followed has already
     * stopped or started it (a game taken back), or the game is not at a place it can be stopped.
     */
    void Opened() {
        for (GameJournal.Entry entry : _table.Journal().entries())
            if (entry.kind() == GameJournal.Kind.NOTE && entry.detail().startsWith("sandbox."))
                return;
        if (CanStop())
            stop(OPENING);
    }

    /** @return Null if the game was stopped; otherwise why it could not be. */
    public String Stop() {
        if (_stopped)
            return "The game is stopped already.";
        if (game().isFinished())
            return "The game is over.";
        if (!game().canAskAgain())
            return "The game can be stopped only while it waits at a phase's own prompt with nothing being played (it is in the middle of something now).";
        stop("");
        return null;
    }

    private void stop(String value) {
        if (!value.equals(OPENING))
            point("Stopped play");
        _table.Journal().note(_human, STOP, value);
        _stopped = true;
    }

    /** @return Null if the game was started again; otherwise why it could not be. */
    public String Start() {
        if (!_stopped)
            return "The game is not stopped.";
        if (game().isFinished())
            return "The game is over.";
        point("Started play");
        _table.Journal().note(_human, START, "");
        start();
        return null;
    }

    private void start() {
        _stopped = false;
        //(an edit cannot take away the decision the game was stopped at, but nothing is asked again that cannot be)
        if (game().canAskAgain())
            game().askAgain();
    }

    private void point(String label) {
        if (_points != null)
            _points.reached(_table.Journal().entries().size(), label);
    }

    /**
     * A note of a journal this game is following (see the class's description): the Stop, Start or edits are done
     * again here, and written into this game's journal at the same place.
     * @throws IllegalStateException if they cannot be, which stops the following there.
     */
    private void follow(GameJournal.Entry note) {
        switch (note.detail()) {
            case STOP -> stop(note.value());
            case START -> {
                point("Started play");
                _table.Journal().note(_human, START, "");
                start();
            }
            case EDIT -> {
                JsonArray ops = JsonValue.readJSON(note.value()).asArray();
                Outcome outcome = edit(ops);
                if (outcome.problem() != null)
                    throw new IllegalStateException("a sandbox edit could not be made again: " + outcome.problem());
            }
            default -> { }
        }
    }

    /**
     * Makes a list of edits, in order (see the notes on each op in EDITS).  Only while stopped.  Everything is
     * checked that can be before anything is changed (that each op is one, that its tool is allowed, that what it
     * names exists); what can only be checked against the board as the earlier ops leave it is checked as each op
     * comes, and if one of those fails, the ones before it have been made (Outcome.partlyMade): whoever called this
     * then takes the game back to before the list (LotroGameMediator does), so that the list is made whole or not
     * at all.
     * @param json A JSON array of ops (or one op).
     */
    public Outcome Edit(String json) {
        if (!_stopped)
            return new Outcome("The board can be changed only while the game is stopped.", false, List.of());
        JsonArray ops;
        try {
            JsonValue value = JsonValue.readJSON(json);
            ops = value.isArray() ? value.asArray() : new JsonArray().add(value);
        } catch (RuntimeException exp) {
            return new Outcome("That is not a list of edits: " + exp.getMessage(), false, List.of());
        }
        if (ops.isEmpty())
            return new Outcome("There is nothing to do.", false, List.of());
        for (int i = 0; i < ops.size(); i++) {
            String problem = checkShape(ops.get(i));
            if (problem != null)
                return new Outcome(which(ops, i) + problem, false, List.of());
        }
        return edit(ops);
    }

    private static String which(JsonArray ops, int i) {
        return ops.size() == 1 ? "" : "Edit " + (i + 1) + ": ";
    }

    /** What can be said about an op without the board: that it is one, its tool allowed, its numbers numbers. */
    private String checkShape(JsonValue value) {
        if (!value.isObject())
            return "an edit is an object {op: ...}";
        JsonObject op = value.asObject();
        String kind = op.getString("op", "");
        String tool = toolOf(kind);
        if (tool == null)
            return "'" + kind + "' is not an edit.  The edits are " + String.join(", ", EDITS) + ".";
        if (NOT_BUILT.contains(tool))
            return "'" + kind + "' (the " + tool + " tool) is not built in this version of the server.";
        if (!Tools().contains(tool))
            return "this sandbox does not allow " + kind + " (its options.tools leave out '" + tool + "').";
        return null;
    }

    /** The ops an edit can be, each with the tool that allows it. */
    static final List<String> EDITS = List.of("add", "copy", "delete", "move", "attach", "stack", "wounds", "tokens", "modify",
            "twilight", "burdens", "threats", "marker", "control", "liberate", "site", "unmodify");

    static String toolOf(String op) {
        return switch (op) {
            case "add", "copy", "delete", "move", "attach", "stack", "wounds", "tokens", "modify" -> op;
            case "twilight", "burdens", "threats" -> "counters";
            case "marker", "control", "liberate" -> "markers";
            case "site" -> "sites";
            case "unmodify" -> "modify";
            default -> null;
        };
    }

    private Outcome edit(JsonArray ops) {
        int entries = _table.Journal().entries().size();
        //Each op as the journal keeps it: its cards by name, and an added card's name with it
        JsonArray done = new JsonArray();
        List<String> labels = new ArrayList<>();
        List<String> added = new ArrayList<>();
        for (int i = 0; i < ops.size(); i++) {
            _changed = false;
            try {
                JsonObject op = ops.get(i).asObject();
                JsonObject kept = apply(op, labels, added);
                done.add(kept);
            } catch (Refused | RuntimeException exp) {
                String message = exp instanceof Refused ? exp.getMessage() : "it could not be done (" + exp + ")";
                //(each op is checked before it changes anything, but a scenario do can find its problem part way, and
                // nothing half made may stay: a card made and not kept would have taken a card id a rebuilt game does not)
                return new Outcome(which(ops, i) + message, i > 0 || _changed, added);
            }
        }
        //The place to go back to is just before the edits: where the note goes
        if (_points != null)
            _points.reached(entries, labels.getFirst() + (labels.size() > 1 ? " and " + (labels.size() - 1) + " more" : ""));
        _table.Journal().note(_human, EDIT, done.toString());
        return Outcome.done(added);
    }

    /* ---- the ops ---- */

    private GameState state() {
        return _table.gameState();
    }

    /**
     * Makes one edit.
     * @return The op as the journal keeps it.
     */
    private JsonObject apply(JsonObject op, List<String> labels, List<String> added) throws Refused {
        String kind = op.getString("op", "");
        JsonObject kept = new JsonObject().add("op", kind);
        switch (kind) {
            case "add" -> {
                String blueprintId = string(op, "blueprint");
                Placement placement = placement(op, kept, null);
                fits(blueprint(blueprintId), placement);
                PhysicalCardImpl card = create(blueprintId, placement.owner(), op.getString("as", null));
                put(card, placement);
                String alias = _table.AliasOf(card);
                kept.add("blueprint", blueprintId).add("seat", placement.owner().key()).add("as", alias);
                added.add(alias);
                labels.add("Added " + GameUtils.getFullName(card));
            }
            case "copy" -> {
                String alias = card(op, "card");
                PhysicalCardImpl original = _table.Card(alias);
                if (isSite(original))
                    throw new Refused("a site is put on the path with the site edit, not copied");
                if (original == ringBearer(original) || original == ring(original))
                    throw new Refused("there is only one Ring-bearer and one Ring to a seat");
                Placement placement = copyPlacement(original);
                fits(original.getBlueprint(), placement);
                PhysicalCardImpl card = create(original.getBlueprintId(), placement.owner(), op.getString("as", null));
                put(card, placement);
                String alias2 = _table.AliasOf(card);
                //(kept as the add it comes to, so that the rebuilt game makes the same card in the same place)
                kept = new JsonObject().add("op", "add").add("blueprint", original.getBlueprintId()).add("seat", placement.owner().key()).add("as", alias2);
                placement.keepIn(kept);
                added.add(alias2);
                labels.add("Copied " + GameUtils.getFullName(card));
            }
            case "delete" -> {
                String alias = card(op, "card");
                PhysicalCardImpl card = _table.Card(alias);
                notTheRing(card, "deleted");
                if (isSite(card))
                    throw new Refused("a site on the path is replaced with the site edit, not deleted");
                letGoOfWhatItHas(card);
                state().removeCardsFromZone(null, List.of(card));
                kept.add("card", alias);
                labels.add("Deleted " + GameUtils.getFullName(card));
            }
            case "move" -> {
                String alias = card(op, "card");
                PhysicalCardImpl card = _table.Card(alias);
                notTheRing(card, "moved");
                if (isSite(card))
                    throw new Refused("a site is put on the path with the site edit");
                Seat owner = _table.SeatOf(card.getOwner());
                ZoneTarget target = zoneTarget(string(op, "to"), owner);
                if (target.seat() != owner)
                    throw new Refused(GameUtils.getFullName(card) + " belongs to the " + owner.key() + " seat, so it can go only to the "
                            + owner.key() + " seat's " + zoneWords(target.zone()) + ", not the " + target.seat().key() + " seat's");
                fits(card.getBlueprint(), target.zone());
                _changed = true;
                if (!onTable(target.zone()) || !onTable(card.getZone()))
                    letGoOfWhatItHas(card);
                _table.Place(card, target.seat(), target.zone(), target.bottom());
                kept.add("card", alias).add("to", target.seat().key() + "." + zoneName(target.zone()) + (target.zone() == SetupZone.DECK ? (target.bottom() ? ".bottom" : ".top") : ""));
                labels.add("Moved " + GameUtils.getFullName(card) + " to " + zoneWords(target.zone()));
            }
            case "attach", "stack" -> {
                String alias = card(op, "card");
                String to = card(op, "to");
                PhysicalCardImpl card = _table.Card(alias);
                PhysicalCardImpl host = _table.Card(to);
                notTheRing(card, kind.equals("attach") ? "attached" : "stacked");
                if (isSite(card))
                    throw new Refused("a site cannot be " + (kind.equals("attach") ? "attached" : "stacked"));
                if (host.getZone() == null || !host.getZone().isInPlay())
                    throw new Refused(GameUtils.getFullName(host) + " is not on the table");
                if (host == card || bears(card, host))
                    throw new Refused(GameUtils.getFullName(card) + " cannot go on " + (host == card ? "itself" : "something it has on it"));
                if (kind.equals("attach")) {
                    CardType type = card.getBlueprint().getCardType();
                    if (type == CardType.COMPANION || type == CardType.MINION || type == CardType.ALLY)
                        throw new Refused("a " + type.name().toLowerCase(Locale.ROOT) + " is not borne by anything (stack it, or move it)");
                }
                letGoOfWhatItHas(card);
                if (kind.equals("attach"))
                    _table.AttachCardsTo(host, card);
                else
                    _table.StackCardsOn(host, card);
                kept.add("card", alias).add("to", to);
                labels.add((kind.equals("attach") ? "Attached " : "Stacked ") + GameUtils.getFullName(card) + (kind.equals("attach") ? " to " : " on ")
                        + GameUtils.getFullName(host));
            }
            case "wounds" -> {
                String alias = card(op, "card");
                PhysicalCardImpl card = _table.Card(alias);
                int amount = number(op, "amount", -99, 99);
                CardType type = card.getBlueprint().getCardType();
                if (card.getZone() == null || !card.getZone().isInPlay() || !(type == CardType.COMPANION || type == CardType.ALLY || type == CardType.MINION))
                    throw new Refused(GameUtils.getFullName(card) + " is not a character on the table");
                _changed = true;
                if (amount > 0)
                    _table.AddWoundsToChar(card, amount);
                else if (amount < 0)
                    _table.RemoveWoundsFromChar(card, Math.min(-amount, _table.GetWoundsOn(card)));
                kept.add("card", alias).add("amount", amount);
                labels.add((amount >= 0 ? "Wounded " : "Healed ") + GameUtils.getFullName(card));
            }
            case "tokens" -> {
                String alias = card(op, "card");
                PhysicalCardImpl card = _table.Card(alias);
                int amount = number(op, "amount", -99, 99);
                if (card.getZone() == null || !card.getZone().isInPlay())
                    throw new Refused(GameUtils.getFullName(card) + " is not on the table");
                Token token = token(op, card);
                _changed = true;
                if (amount > 0)
                    state().addTokens(card, token, amount);
                else if (amount < 0)
                    state().removeTokens(card, token, Math.min(-amount, state().getTokenCount(card, token)));
                kept.add("card", alias).add("amount", amount).add("culture", token.name().toLowerCase(Locale.ROOT));
                labels.add((amount >= 0 ? "Added " : "Removed ") + Math.abs(amount) + " " + token.name().toLowerCase(Locale.ROOT) + " token"
                        + (Math.abs(amount) == 1 ? "" : "s") + (amount >= 0 ? " to " : " from ") + GameUtils.getFullName(card));
            }
            case "modify" -> {
                //{op: modify, card, what: strength | fierce | ..., amount?, until?: phase | turn}: as do: modify(...) is
                String alias = card(op, "card");
                PhysicalCardImpl card = _table.Card(alias);
                if (card.getZone() == null || !card.getZone().isInPlay())
                    throw new Refused(GameUtils.getFullName(card) + " is not on the table");
                List<String> rest = new ArrayList<>();
                if (op.get("amount") != null)
                    rest.add(String.valueOf(number(op, "amount", -99, 99)));
                String until = op.getString("until", null);
                if (until != null && !until.isBlank() && !until.equalsIgnoreCase("removed"))
                    rest.add(until);
                ScenarioDefinition.Modification modification;
                try {
                    modification = CardModifiers.parse(string(op, "what"), rest, "modify");
                } catch (ScenarioFormatException exp) {
                    throw new Refused(exp.getMessage());
                }
                String unsuited = CardModifiers.unsuited(modification, card.getBlueprint().getCardType());
                if (unsuited != null)
                    throw new Refused(unsuited);
                _changed = true;
                _table.Modify(card, modification);
                kept.add("card", alias).add("what", modification.what());
                if (modification.amount() != 0)
                    kept.add("amount", modification.amount());
                if (modification.until() != ScenarioDefinition.ModifierTime.REMOVED)
                    kept.add("until", modification.until().name().toLowerCase(Locale.ROOT));
                labels.add("Modified " + GameUtils.getFullName(card) + " (" + modification.text() + ")");
            }
            case "unmodify" -> {
                //{op: unmodify, card, what?}: every modifier put on the card by hand, or those of one kind; or {id}: that one
                if (op.get("id") != null) {
                    int id = number(op, "id", 1, 100000);
                    if (!_table.Unmodify(id))
                        throw new Refused("no modifier " + id + " is in force");
                    _changed = true;
                    kept.add("id", id);
                    labels.add("Took a modifier off");
                } else {
                    String alias = card(op, "card");
                    PhysicalCardImpl card = _table.Card(alias);
                    String what = op.getString("what", null);
                    _changed = true;
                    _table.Unmodify(card, what);
                    kept.add("card", alias);
                    if (what != null)
                        kept.add("what", what);
                    labels.add("Took modifiers off " + GameUtils.getFullName(card));
                }
            }
            case "twilight", "burdens", "threats" -> {
                boolean relative = op.get("set") == null;
                int amount = relative ? number(op, "amount", -99, 99) : number(op, "set", 0, 99);
                DoKind doKind = kind.equals("twilight") ? DoKind.TWILIGHT : kind.equals("burdens") ? DoKind.BURDENS : DoKind.THREATS;
                applyDo(new DoOp(doKind, null, null, amount, relative, "sandbox " + kind));
                kept.add(relative ? "amount" : "set", amount);
                labels.add((relative ? "Changed " : "Set ") + kind + (relative ? " by " + (amount > 0 ? "+" : "") + amount : " to " + amount));
            }
            case "marker" -> {
                Seat seat = seat(op, "seat");
                int site = number(op, "site", 1, 9);
                applyDo(new DoOp(DoKind.MARKER, null, seat.key(), site, false, "sandbox marker"));
                kept.add("seat", seat.key()).add("site", site);
                labels.add("Put the " + seat.key() + " marker on site " + site);
            }
            case "control" -> {
                Seat seat = seat(op, "seat");
                int site = number(op, "site", 1, 9);
                PhysicalCard there = state().getSite(site);
                if (there != null && there.getCardController() != null && _table.SeatOf(there.getCardController()) == seat)
                    throw new Refused("the " + seat.key() + " seat controls site " + site + " already");
                applyDo(new DoOp(DoKind.CONTROL, null, seat.key(), site, false, "sandbox control"));
                kept.add("site", site).add("seat", seat.key());
                labels.add("Gave site " + site + " to the " + seat.key() + " seat");
            }
            case "liberate" -> {
                int site = number(op, "site", 1, 9);
                applyDo(new DoOp(DoKind.LIBERATE, null, null, site, false, "sandbox liberate"));
                kept.add("site", site);
                labels.add("Liberated site " + site);
            }
            case "site" -> {
                int number = number(op, "number", 1, 9);
                PhysicalCard there = state().getSite(number);
                if (there != null && there.getCardController() != null)
                    throw new Refused("site " + number + " is controlled by the " + _table.SeatOf(there.getCardController()).key() + " seat: liberate it first");
                PhysicalCardImpl site = siteFor(op, kept, number);
                layOnPath(site, number);
                kept.add("number", number);
                labels.add("Site " + number + ": " + GameUtils.getFullName(site));
            }
            default -> throw new Refused("'" + kind + "' is not an edit");
        }
        return kept;
    }

    /** A scenario do, with whatever it would have written down as a problem taken as a refusal. */
    private void applyDo(DoOp op) throws Refused {
        _changed = true;
        int before = _table.Problems().size();
        _table.Apply(op);
        List<String> problems = _table.Problems();
        if (problems.size() > before)
            throw new Refused(problems.getLast().replaceFirst("^sandbox \\w+: ", ""));
    }

    /** Where an added card goes: a zone of a seat, or onto a card. */
    private record Placement(Seat owner, ZoneTarget zone, String attachTo, String stackOn) {
        void keepIn(JsonObject kept) {
            if (zone != null)
                kept.add("to", zone.seat().key() + "." + zoneName(zone.zone()) + (zone.zone() == SetupZone.DECK ? (zone.bottom() ? ".bottom" : ".top") : ""));
            else if (attachTo != null)
                kept.add("attach", attachTo);
            else
                kept.add("stack", stackOn);
        }
    }

    private record ZoneTarget(Seat seat, SetupZone zone, boolean bottom) { }

    private Placement placement(JsonObject op, JsonObject kept, Seat defaultSeat) throws Refused {
        int said = (op.get("to") != null ? 1 : 0) + (op.get("attach") != null ? 1 : 0) + (op.get("stack") != null ? 1 : 0);
        if (said != 1)
            throw new Refused("say where the card goes: to (a zone, such as freeps.hand), attach (the card to bear it) or stack (the card to go on)");
        Seat seat = op.get("seat") != null ? seat(op, "seat") : defaultSeat;
        Placement placement;
        if (op.get("to") != null) {
            ZoneTarget target = zoneTarget(string(op, "to"), seat != null ? seat : _def.human);
            if (seat != null && seat != target.seat())
                throw new Refused("a card added to the " + target.seat().key() + " seat's " + zoneWords(target.zone()) + " belongs to that seat");
            placement = new Placement(target.seat(), target, null, null);
        } else {
            String host = card(op, op.get("attach") != null ? "attach" : "stack");
            PhysicalCardImpl hostCard = _table.Card(host);
            if (hostCard.getZone() == null || !hostCard.getZone().isInPlay())
                throw new Refused(GameUtils.getFullName(hostCard) + " is not on the table");
            //(a card on another is its own owner's: by default, the host's)
            Seat owner = seat != null ? seat : _table.SeatOf(hostCard.getOwner());
            placement = op.get("attach") != null ? new Placement(owner, null, host, null) : new Placement(owner, null, null, host);
        }
        placement.keepIn(kept);
        return placement;
    }

    private Placement copyPlacement(PhysicalCardImpl original) throws Refused {
        Seat owner = _table.SeatOf(original.getOwner());
        if (original.getAttachedTo() != null)
            return new Placement(owner, null, _table.AliasOf(original.getAttachedTo()), null);
        if (original.getStackedOn() != null)
            return new Placement(owner, null, null, _table.AliasOf(original.getStackedOn()));
        SetupZone zone = setupZoneOf(original.getZone());
        if (zone == null)
            throw new Refused(GameUtils.getFullName(original) + " is nowhere a card can be added");
        return new Placement(owner, new ZoneTarget(owner, zone, false), null, null);
    }

    /** The card a blueprint id names, as the format has it (its errata), if it is one that can be added. */
    private LotroCardBlueprint blueprint(String blueprintId) throws Refused {
        LotroCardBlueprint blueprint;
        try {
            blueprint = _table.Library().getLotroCardBlueprint(_table.Format().applyErrata(blueprintId));
        } catch (CardNotFoundException | RuntimeException exp) {
            throw new Refused("there is no card " + blueprintId);
        }
        if (blueprint == null)
            throw new Refused("there is no card " + blueprintId);
        if (blueprint.getCardType() == CardType.SITE)
            throw new Refused("a site is put on the path with the site edit");
        if (blueprint.getCardType() == CardType.THE_ONE_RING)
            throw new Refused("a seat has one Ring, which comes with its Ring-bearer");
        return blueprint;
    }

    /** Whether a card of this kind can go where it is placed: checked before the card is made. */
    private static void fits(LotroCardBlueprint blueprint, Placement placement) throws Refused {
        if (placement.zone() != null) {
            fits(blueprint, placement.zone().zone());
        } else if (placement.attachTo() != null) {
            CardType type = blueprint.getCardType();
            if (type == CardType.COMPANION || type == CardType.MINION || type == CardType.ALLY)
                throw new Refused("a " + type.name().toLowerCase(Locale.ROOT) + " is not borne by anything (stack it, or put it among the characters)");
        }
    }

    /** Makes a card (checked already: see blueprint and fits) and names it. */
    private PhysicalCardImpl create(String blueprintId, Seat owner, String as) throws Refused {
        LotroCardBlueprint blueprint = blueprint(blueprintId);
        String id = _table.Format().applyErrata(blueprintId);
        if (as != null && _table.Named().containsKey(as))
            throw new Refused("there is a card called '" + as + "' already");
        _changed = true;
        try {
            PhysicalCardImpl card = (PhysicalCardImpl) state().createPhysicalCard(_table.Player(owner), _table.Library(), id);
            _table.Name(owner, as != null ? as : newName(blueprint), card);
            return card;
        } catch (CardNotFoundException exp) {
            throw new Refused("there is no card " + blueprintId);
        }
    }

    /**
     * A name for an added card: its title, in lower case and without what cannot be in a name, and a number if a
     * card has that name already ("gimli", then "gimli2").  It depends only on the names there are, so a game
     * rebuilt from the journal names it the same (the journal keeps the name all the same).
     */
    private String newName(LotroCardBlueprint blueprint) {
        String base = com.gempukku.lotro.common.Names.SanitizeDisplayName(blueprint.getTitle()).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (base.isEmpty() || !Character.isLetter(base.charAt(0)))
            base = "card" + base;
        if (!_table.Named().containsKey(base) && !_def.cards.containsKey(base) && !_def.siteCards.containsKey(base))
            return base;
        for (int n = 2; ; n++)
            if (!_table.Named().containsKey(base + n) && !_def.cards.containsKey(base + n) && !_def.siteCards.containsKey(base + n))
                return base + n;
    }

    private void put(PhysicalCardImpl card, Placement placement) {
        if (placement.zone() != null) {
            _table.Place(card, placement.zone().seat(), placement.zone().zone(), placement.zone().bottom());
        } else if (placement.attachTo() != null) {
            _table.AttachCardsTo(_table.Card(placement.attachTo()), card);
        } else {
            _table.StackCardsOn(_table.Card(placement.stackOn()), card);
        }
    }

    /** Whether a card of this kind can be put in that zone of the table. */
    private static void fits(LotroCardBlueprint blueprint, SetupZone zone) throws Refused {
        CardType type = blueprint.getCardType();
        switch (zone) {
            case CHARACTERS -> {
                if (type != CardType.COMPANION)
                    throw new Refused("only a companion goes among the characters (an ally goes in the support area)");
            }
            case MINIONS -> {
                if (type != CardType.MINION)
                    throw new Refused("only a minion goes among the minions");
            }
            case SUPPORT -> {
                if (type == CardType.COMPANION || type == CardType.MINION || type == CardType.EVENT)
                    throw new Refused("a " + type.name().toLowerCase(Locale.ROOT) + " does not go in the support area");
            }
            default -> { }
        }
    }

    /** Whatever is attached to a card or stacked on it goes to its owner's discard pile (see the class's description). */
    private void letGoOfWhatItHas(PhysicalCardImpl card) {
        _changed = true;
        if (card.getZone() == null)
            return;
        for (PhysicalCard on : new ArrayList<>(state().getAttachedCards(card)))
            _table.MoveCardToZone((PhysicalCardImpl) on, Zone.DISCARD);
        for (PhysicalCard on : new ArrayList<>(state().getStackedCards(card)))
            _table.MoveCardToZone((PhysicalCardImpl) on, Zone.DISCARD);
    }

    private boolean bears(PhysicalCard card, PhysicalCard other) {
        for (PhysicalCard on = other.getAttachedTo() != null ? other.getAttachedTo() : other.getStackedOn(); on != null;
             on = on.getAttachedTo() != null ? on.getAttachedTo() : on.getStackedOn())
            if (on == card)
                return true;
        return false;
    }

    private PhysicalCard ringBearer(PhysicalCard card) {
        return state().getRingBearer(card.getOwner());
    }

    private PhysicalCard ring(PhysicalCard card) {
        return state().getRing(card.getOwner());
    }

    private void notTheRing(PhysicalCardImpl card, String done) throws Refused {
        if (card == ringBearer(card))
            throw new Refused(GameUtils.getFullName(card) + " is the " + _table.SeatOf(card.getOwner()).key() + " seat's Ring-bearer, and cannot be " + done
                    + " (wound the Ring-bearer, or add burdens, to see what the game does)");
        if (card == ring(card))
            throw new Refused("The One Ring stays with its Ring-bearer, and cannot be " + done);
    }

    private static boolean isSite(PhysicalCard card) {
        return card.getBlueprint().getCardType() == CardType.SITE;
    }

    private static boolean onTable(SetupZone zone) {
        return zone == SetupZone.CHARACTERS || zone == SetupZone.SUPPORT || zone == SetupZone.MINIONS;
    }

    private static boolean onTable(Zone zone) {
        return zone == Zone.FREE_CHARACTERS || zone == Zone.SUPPORT || zone == Zone.SHADOW_CHARACTERS;
    }

    private static SetupZone setupZoneOf(Zone zone) {
        if (zone == null)
            return null;
        return switch (zone) {
            case FREE_CHARACTERS -> SetupZone.CHARACTERS;
            case SUPPORT -> SetupZone.SUPPORT;
            case SHADOW_CHARACTERS -> SetupZone.MINIONS;
            case HAND -> SetupZone.HAND;
            case DECK -> SetupZone.DECK;
            case DISCARD -> SetupZone.DISCARD;
            case DEAD -> SetupZone.DEAD_PILE;
            case REMOVED -> SetupZone.REMOVED;
            default -> null;
        };
    }

    private static String zoneName(SetupZone zone) {
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

    private static String zoneWords(SetupZone zone) {
        return switch (zone) {
            case CHARACTERS -> "characters";
            case SUPPORT -> "support area";
            case MINIONS -> "minions";
            case HAND -> "hand";
            case DECK -> "draw deck";
            case DISCARD -> "discard pile";
            case DEAD_PILE -> "dead pile";
            case REMOVED -> "removed pile";
        };
    }

    /** "freeps.hand", "shadow.deck.bottom", or a zone alone ("discard") for the given seat's. */
    private ZoneTarget zoneTarget(String text, Seat seat) throws Refused {
        String full = text.contains(".") && (text.startsWith("freeps.") || text.startsWith("shadow.") || text.startsWith("human.") || text.startsWith("opponent."))
                ? text : seat.key() + "." + text;
        try {
            ScenarioParser.ZoneRef ref = ScenarioParser.parseZoneRef(_def, full, "to");
            return new ZoneTarget(ref.seat(), ref.zone(), ref.bottom());
        } catch (ScenarioFormatException exp) {
            throw new Refused(exp.getMessage());
        }
    }

    /** The culture of tokens an op means: its culture, or the card's own. */
    private static Token token(JsonObject op, PhysicalCard card) throws Refused {
        String culture = op.getString("culture", null);
        if (culture == null) {
            Token own = Token.findTokenForCulture(card.getBlueprint().getCulture());
            if (own == null)
                throw new Refused(GameUtils.getFullName(card) + " has no culture of its own: say which culture's tokens");
            return own;
        }
        String name = culture.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            Token token = Token.valueOf(name);
            if (token.getCulture() == null)
                throw new IllegalArgumentException();
            return token;
        } catch (IllegalArgumentException exp) {
            List<String> names = new ArrayList<>();
            for (Token token : Token.values())
                if (token.getCulture() != null)
                    names.add(token.name().toLowerCase(Locale.ROOT));
            throw new Refused("'" + culture + "' is not a culture.  The cultures are " + String.join(", ", names));
        }
    }

    /** The site an op puts on the path: by blueprint, the site of that number from a seat's adventure deck, or a named site. */
    private PhysicalCardImpl siteFor(JsonObject op, JsonObject kept, int number) throws Refused {
        int said = (op.get("blueprint") != null ? 1 : 0) + (op.get("from") != null ? 1 : 0) + (op.get("card") != null ? 1 : 0);
        if (said != 1)
            throw new Refused("say which site: blueprint (with seat, whose it is), from (a seat: its site of that number) or card (a site in an adventure deck, by name)");
        if (op.get("blueprint") != null) {
            String blueprintId = string(op, "blueprint");
            Seat owner = op.get("seat") != null ? seat(op, "seat") : _def.human;
            String id = _table.Format().applyErrata(blueprintId);
            try {
                LotroCardBlueprint blueprint = _table.Library().getLotroCardBlueprint(id);
                if (blueprint == null || blueprint.getCardType() != CardType.SITE)
                    throw new Refused(blueprintId + " is not a site");
                String as = op.getString("as", null);
                if (as != null && _table.Named().containsKey(as))
                    throw new Refused("there is a card called '" + as + "' already");
                _changed = true;
                PhysicalCardImpl card = (PhysicalCardImpl) state().createPhysicalCard(_table.Player(owner), _table.Library(), id);
                //(kept in its owner's adventure deck until it is laid, as every site is)
                _table.Name(owner, op.getString("as", null) != null ? op.getString("as", null) : newName(blueprint), card);
                state().addCardToZone(game(), card, Zone.ADVENTURE_DECK);
                kept.add("blueprint", blueprintId).add("seat", owner.key()).add("as", _table.AliasOf(card));
                return card;
            } catch (CardNotFoundException exp) {
                throw new Refused("there is no card " + blueprintId);
            }
        }
        if (op.get("from") != null) {
            Seat seat = seat(op, "from");
            for (PhysicalCard card : state().getAdventureDeck(_table.Player(seat)))
                if (card.getBlueprint().getSiteNumber() == number) {
                    kept.add("card", _table.AliasOf(card));
                    return (PhysicalCardImpl) card;
                }
            throw new Refused("the " + seat.key() + " seat's adventure deck has no site " + number + " (it may be on the path already)");
        }
        String alias = card(op, "card");
        PhysicalCardImpl card = _table.Card(alias);
        if (!isSite(card) || card.getZone() != Zone.ADVENTURE_DECK)
            throw new Refused(GameUtils.getFullName(card) + " is not a site in an adventure deck");
        kept.add("card", alias);
        return card;
    }

    /**
     * Lays a site from its adventure deck on the path at a number, as the game lays a site: whatever site is there
     * goes back to its owner's adventure deck.  A controlled site is not replaced: liberate it first.
     */
    private void layOnPath(PhysicalCardImpl site, int number) {
        _changed = true;
        PhysicalCard there = state().getSite(number);
        if (there != null) {
            state().removeCardsFromZone(null, List.of(there));
            ((PhysicalCardImpl) there).setSiteNumber(null);
            state().addCardToZone(game(), there, Zone.ADVENTURE_DECK);
        }
        state().removeCardsFromZone(null, List.of(site));
        site.setSiteNumber(number);
        state().addCardToZone(game(), site, Zone.ADVENTURE_PATH);
    }

    /* ---- reading an op ---- */

    private static String string(JsonObject op, String name) throws Refused {
        JsonValue value = op.get(name);
        if (value == null || !value.isString() || value.asString().isBlank())
            throw new Refused("'" + name + "' is missing");
        return value.asString().trim();
    }

    private static int number(JsonObject op, String name, int least, int most) throws Refused {
        JsonValue value = op.get(name);
        if (value == null || !value.isNumber())
            throw new Refused("'" + name + "' has to be a number");
        int number = value.asInt();
        if (number < least || number > most)
            throw new Refused("'" + name + "' has to be from " + least + " to " + most);
        return number;
    }

    private Seat seat(JsonObject op, String name) throws Refused {
        String text = string(op, name);
        try {
            return ScenarioParser.parseSeat(_def, text, name);
        } catch (ScenarioFormatException exp) {
            throw new Refused(exp.getMessage());
        }
    }

    /**
     * The card an op names, by its name in the scenario (or its card id, as cardId): one that is in the game and
     * that the person may see.
     * @return Its name.
     */
    private String card(JsonObject op, String name) throws Refused {
        String alias;
        JsonValue byId = op.get(name + "Id");
        if (op.get(name) != null) {
            alias = string(op, name);
        } else if (byId != null && byId.isNumber()) {
            alias = _table.AliasOfCardId(String.valueOf(byId.asInt()));
            if (alias == null)
                throw new Refused("there is no card " + byId.asInt() + " that can be changed");
        } else {
            throw new Refused("'" + name + "' is missing: the card's name (or its id, as " + name + "Id)");
        }
        PhysicalCardImpl card = _table.Named().get(alias);
        if (card == null)
            throw new Refused("there is no card called '" + alias + "'");
        if (card.getZone() == null)
            throw new Refused("'" + alias + "' is not in the game any more");
        if (!visible(card))
            throw new Refused("'" + alias + "' is somewhere this sandbox does not let you look (its options.tools leave out 'piles')");
        return alias;
    }

    /**
     * Whether the person may see the card where it is: anywhere with piles, otherwise not in a hidden pile.  A seat's
     * hand and adventure deck are hidden from the person only while they neither sit in that seat nor look at the
     * table from it (the flip shows the hand of the seat looked from, as its player sees it).
     */
    private boolean visible(PhysicalCard card) {
        if (Tools().contains("piles"))
            return true;
        Zone zone = card.getZone();
        Seat owner = _table.SeatOf(card.getOwner());
        boolean mine = owner == _def.human || (_looking != null && owner == _looking.get());
        return zone != Zone.DECK && !(zone == Zone.HAND && !mine) && !(zone == Zone.ADVENTURE_DECK && !mine);
    }

    /* ---- what the page is shown ---- */

    /**
     * The table as the person may see it, for the page: {mode, level, tools, canStop, canStart, cards, piles,
     * twilight, burdens, threats, markers, currentSite, moves, path, skirmishes, adventureDecks}.  See the notes
     * file for each field.
     */
    public Map<String, Object> State() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("mode", _stopped ? "edit" : "play");
        state.put("level", _def.level());
        state.put("tools", Tools());
        state.put("canStop", CanStop());
        state.put("canStart", CanStart());
        state.put("over", game().isFinished());
        //(what a card can be modified in by hand, and for how long: the card menu's Modify...)
        state.put("modify", ScenarioAuthoring.describeModify());
        Map<PhysicalCard, Integer> places = BoardView.places(_table);
        List<Map<String, Object>> cards = new ArrayList<>();
        for (Map.Entry<String, PhysicalCardImpl> named : _table.Named().entrySet()) {
            PhysicalCardImpl card = named.getValue();
            if (card.getZone() == null || !visible(card))
                continue;
            Map<String, Object> json = BoardView.card(_table, named.getKey(), card, _table.SeatOf(card.getOwner()), places);
            //(every culture's tokens on it, not just its own: an edit can put any culture's there)
            Map<String, Integer> tokens = new LinkedHashMap<>();
            for (Map.Entry<Token, Integer> token : state().getTokens(card).entrySet())
                if (token.getKey().getCulture() != null && token.getValue() > 0)
                    tokens.put(token.getKey().name().toLowerCase(Locale.ROOT), token.getValue());
            json.put("cultureTokens", tokens);
            json.put("culture", card.getBlueprint().getCulture() == null ? null : card.getBlueprint().getCulture().name().toLowerCase(Locale.ROOT));
            //(a site's number on the path, a controlled one's too, null in an adventure deck; and the number printed on it, 0 for one with none)
            json.put("site", isSite(card) && card.getZone() != Zone.ADVENTURE_DECK ? card.getSiteNumber() : null);
            json.put("printed", isSite(card) ? card.getBlueprint().getSiteNumber() : null);
            json.put("added", !_def.cards.containsKey(named.getKey()) && !_def.siteCards.containsKey(named.getKey()));
            cards.add(json);
        }
        state.put("cards", cards);
        //How many cards each pile holds, whether or not the person may look in it
        Map<String, Object> piles = new LinkedHashMap<>();
        for (Seat seat : Seat.values()) {
            String player = _table.Player(seat);
            Map<String, Object> counts = new LinkedHashMap<>();
            counts.put("hand", state().getHand(player).size());
            counts.put("deck", state().getDeck(player).size());
            counts.put("discard", state().getDiscard(player).size());
            counts.put("deadPile", state().getDeadPile(player).size());
            counts.put("removed", state().getRemoved(player).size());
            counts.put("adventureDeck", state().getAdventureDeck(player).size());
            piles.put(seat.key(), counts);
        }
        state.put("piles", piles);
        state.put("hiddenPilesOpen", Tools().contains("piles"));
        state.put("twilight", _table.GetTwilight());
        state.put("burdens", _table.GetBurdens());
        state.put("threats", _table.GetThreats());
        BoardView.where(_table, state);
        List<String> cultures = new ArrayList<>();
        for (Token token : Token.values())
            if (token.getCulture() != null)
                cultures.add(token.name().toLowerCase(Locale.ROOT));
        state.put("cultures", cultures);
        return state;
    }
}
