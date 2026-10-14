package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.patchnotes.CardLookup;
import com.gempukku.lotro.scenario.ScenarioDefinition.Answer;
import com.gempukku.lotro.scenario.ScenarioDefinition.CardDef;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoOp;
import com.gempukku.lotro.scenario.ScenarioDefinition.SetupZone;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;
import com.gempukku.lotro.scenario.ScenarioDefinition.When;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Writing a scenario by playing it.
 *
 * An author has one draft at a time: a scenario definition, and a live game of it in which they sit in the person's
 * seat.  The script is the timeline.  Wherever it has something to say, the game does what a player's game would do;
 * where it has run out, the author plays on, and what they do (and what they have the opponent do) is written into
 * the script as steps.  Messages, restrictions and everything else about a step are edited beside the game, and a
 * change to the step in hand is on the screen at once.
 *
 * Getting about in the timeline is done by playing, not by rewinding: "go to step 12" starts a fresh game and has the
 * script play itself, both seats, up to step 12.  That is also what happens when a change cannot be made to the game
 * as it stands (a card added to a deck, a step put in before where the game is): the draft is changed and a fresh
 * game is brought to the same place.  It means the draft is always something that has just been played, and that a
 * script which no longer plays is found out at once, at the step where it stops.
 *
 * What the client edits is the scenario as its file has it (see ScenarioWriter.toJson): it sends the whole draft
 * back, and this works out whether the game in hand can take the change or has to be started again.
 *
 * Locks: an author's requests take turns (the session's monitor), and whatever they do to the game is done inside
 * the game's own lock.  The game calls back in here (Sitting) while it holds its lock, on whichever thread is
 * running it, so nothing in those calls may wait for a session's monitor.
 */
public class ScenarioAuthoring {
    private static final Logger LOG = LogManager.getLogger(ScenarioAuthoring.class);

    /** Parts of a scenario file that say nothing about how its game goes. */
    private static final Set<String> ABOUT_IT = Set.of("title", "summary", "author", "tags", "note", "version", "steps");
    /** Parts of a step that change what is shown and allowed, and nothing about which decisions it takes or how it answers. */
    private static final Set<String> SHOWN = Set.of("messages", "note", "id", "checkpoint", "allow");
    private static final Set<String> SHOWN_OR_UNDER = Set.of("messages", "note", "id", "checkpoint", "allow", "anyOrder");
    /** Parts of a step that can be written out in full, as { ... }: typed in as the file has them, they are read as HJSON. */
    private static final List<String> TYPED = List.of("when", "until", "skipWhen");
    private static final int SEARCH_LIMIT = 60;

    /** Whatever seats an author at a game: the server, or a test. */
    public interface Tables {
        /**
         * Sets a game going with this player in the person's seat, in place of whatever scenario they were in.
         * @throws RuntimeException if the game cannot be started; the player is then still where they were.
         */
        LotroGameMediator seat(Player player, LiveScenario live);
    }

    /** Something the author asked for that cannot be done, with words that say why. */
    public static class AuthoringException extends Exception {
        public AuthoringException(String message) {
            super(message);
        }
    }

    private final ScenarioLibrary _scenarios;
    private final LotroCardBlueprintLibrary _library;
    private final Function<String, LotroFormat> _formats;
    private final CardLookup _cardLookup;
    private final Tables _tables;
    private final ScheduledExecutorService _timer;
    private final Map<String, Session> _sessions = new ConcurrentHashMap<>();
    /** Where the web server keeps the pictures a scenario can show that are not cards; null if nowhere is known. */
    private final java.io.File _images;

    public ScenarioAuthoring(ScenarioLibrary scenarios, LotroCardBlueprintLibrary library, Function<String, LotroFormat> formats,
                             CardLookup cardLookup, Tables tables) {
        this(scenarios, library, formats, cardLookup, tables, null);
    }

    public ScenarioAuthoring(ScenarioLibrary scenarios, LotroCardBlueprintLibrary library, Function<String, LotroFormat> formats,
                             CardLookup cardLookup, Tables tables, java.io.File images) {
        _images = images;
        _scenarios = scenarios;
        _library = library;
        _formats = formats;
        _cardLookup = cardLookup;
        _tables = tables;
        AtomicInteger threadNumber = new AtomicInteger();
        _timer = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "scenario-authoring-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    /* ------------------------------------------------------------------ a draft and its games */

    /** One author's draft. */
    private static final class Session {
        final Player player;
        /** Whose file it is: null for one of the game's own.  Settled when it is first saved. */
        String owner;
        /** Where its file is (or will be) within its folder. */
        String path;
        /** Whether there is a file there yet: a new scenario has a name from the start, and a file once it is saved. */
        boolean onDisk;
        /** Set when the author's page says it is going away; any request from the page afterwards unsets it. */
        volatile boolean left;
        Sitting sitting;
        final AtomicInteger revision = new AtomicInteger();
        /** Numbers the names steps are given (see Sitting.uid). */
        final AtomicInteger uids = new AtomicInteger();
        volatile boolean dirty;
        /** Whether what is done where the script has nothing to say is written into the script. */
        volatile boolean recording = true;
        /** Whether the opponent's decisions that the script does not answer are left for the author. */
        volatile boolean askOpponent = true;
        /** The choices to take at the script's forks when it plays itself: the names in here, where a fork offers one. */
        final Set<String> way = new LinkedHashSet<>();
        /** The last thing worth telling the author that nothing on the game screen says. */
        volatile String notice;
        /** The notice that says where the script stopped when it played itself, while that is still the notice. */
        volatile String stopNotice;
        /** Whether it stopped at a decision that no step covers (and said nothing else). */
        volatile boolean stoppedUncovered;
        /** Set while the game in hand is a player's game of the draft: the step it was started at. */
        Integer testFrom;
        /** How many lines of comment the file has that saving the draft over it would lose. */
        int commentsLost;
        /**
         * What was left out of the file for it to be opened at all (see ScenarioSalvage): the draft is the file
         * without these, and saving it writes the file without them.  Kept until the author says they have seen it.
         */
        volatile List<ScenarioSalvage.LeftOut> leftOut = List.of();
        /** For each of those, the name of the step it was in (null if it was in none, or the whole step went). */
        volatile List<String> leftOutSteps = List.of();

        Session(Player player) {
            this.player = player;
        }

        void changed() {
            dirty = true;
            revision.incrementAndGet();
        }

        /** The game has gone on from where the script stopped: that it stopped there is no longer news. */
        void movedOn() {
            if (stopNotice != null && stopNotice.equals(notice))
                notice = null;
            stopNotice = null;
        }

        String choice(Step step) {
            for (String name : ScenarioSession.ChoicesOf(step).keySet())
                if (way.contains(name))
                    return name;
            return null;
        }
    }

    /**
     * One game of a draft, and the draft as that game has it.  A fresh game gets a fresh one of these (and a fresh
     * copy of the definition), so a game that has been given up can do nothing to the draft.
     */
    private final class Sitting implements LiveScenario.Author {
        final Session session;
        final ScenarioDefinition def;
        final LotroFormat format;
        /** False for a player's game of the draft (a test): nothing is held, nothing is written. */
        final boolean author;
        LiveScenario live;
        LotroGameMediator mediator;
        /** Changes made to the board that no step carries yet: they go on the next step to be answered. */
        final List<DoOp> pending = new ArrayList<>();
        /** Steps written before their answer was known: the answer is filled in when it is given. */
        final Set<Step> awaiting = Collections.newSetFromMap(new IdentityHashMap<>());
        /** A decision whose answer is not to be written down. */
        AwaitingDecision unrecorded;
        /** The opponent's decision that was last left for the author. */
        AwaitingDecision left;
        /** The answer the author gave to it, to be compared with the one the game took. */
        String intended;
        /**
         * What each step is called between here and the author's client.  A step cannot be known there by where it
         * comes in the script: that changes under the client whenever the game writes a step in.
         */
        final Map<Step, String> names = new IdentityHashMap<>();
        /** Whether the last change the game followed put anything on the person's screen. */
        boolean shown;

        Sitting(Session session, ScenarioDefinition def, LotroFormat format, boolean author) {
            this.session = session;
            this.def = def;
            this.format = format;
            this.author = author;
        }

        String uid(Step step) {
            return names.computeIfAbsent(step, unnamed -> "s" + session.uids.incrementAndGet());
        }

        /** The names of the steps, in the order the steps come. */
        List<String> uids() {
            List<String> all = new ArrayList<>();
            for (Step step : def.steps)
                all.add(uid(step));
            return all;
        }

        /** For each step in order, the names of the steps in it: none, unless it is a block. */
        List<List<String>> kids() {
            List<List<String>> all = new ArrayList<>();
            for (Step step : def.steps) {
                List<String> under = new ArrayList<>();
                for (Step child : step.children)
                    under.add(uid(child));
                all.add(under);
            }
            return all;
        }

        /**
         * Where the game is, for a fresh game to be brought to the same place: the step the script is at and, if
         * that is a block, how many decisions its steps have answered.
         */
        At at() {
            ScenarioDirector director = live.Session().Director();
            Step current = director.currentStep();
            return new At(position(), 0, current != null && current.block ? director.answeredInBlock() : 0);
        }

        /**
         * What a fresh game of the draft takes over from this one.
         * @param names The names of the steps of the draft the fresh game is of (null where a step is new).
         */
        Carried carry(Names names, At at) {
            Set<String> unanswered = new HashSet<>();
            for (Step step : awaiting)
                unanswered.add(uid(step));
            List<String> dropped = new ArrayList<>();
            pending.forEach(op -> dropped.add(op.source()));
            return new Carried(null, names, unanswered, at, dropped);
        }

        /** A step read again from what its file would say, in the place it has. */
        Step reparsed(Step step, JsonObject json) throws ScenarioFormatException {
            return ScenarioParser.parseStep(def, json, step.number, step.parent, step.sub);
        }

        @Override
        public boolean holds(Ruling ruling) {
            if (!author || !session.askOpponent)
                return false;
            left = ruling.decision();
            return true;
        }

        @Override
        public void answered(Ruling ruling, String answer) {
            session.movedOn();
            try {
                Step step = ruling.step();
                boolean human = ruling.player().equals(live.HumanPlayer());
                if (!human && left == ruling.decision() && intended != null) {
                    //(an answer the game will not take is replaced by one it will, as any opponent's is)
                    if (!intended.equals(answer))
                        session.notice = "The game would not take the opponent's answer \"" + intended + "\", and it did this instead: "
                                + term(ruling.decision(), answer);
                    intended = null;
                }
                if (step != null) {
                    boolean changed = false;
                    if (!pending.isEmpty()) {
                        step.doOps.addAll(pending);
                        pending.clear();
                        changed = true;
                    }
                    //(a step that was waiting to be given its answer, and has not been given one by hand meanwhile)
                    boolean unanswered = step.answers.isEmpty() && (step.allow == null || (step.allow.answers.isEmpty() && step.allow.passWhen == null));
                    boolean wanted = (awaiting.remove(step) && unanswered)
                            || (!human && !step.pause && ruling.answer() == null && step.answers.isEmpty());
                    if (wanted && unrecorded != ruling.decision()) {
                        JsonObject json = ScenarioWriter.step(step);
                        String term = term(ruling.decision(), answer);
                        //(a step that already says what to do about wrong clicks keeps saying it)
                        if (human && json.get("allow") != null && json.get("allow").isObject())
                            json.get("allow").asObject().set("answers", term);
                        else
                            json.set(human ? "allow" : "answer", term);
                        copyInto(step, reparsed(step, json));
                        changed = true;
                    } else if (human && session.recording && step.until != null && step.parent == null && !step.pause
                            && unrecorded != ruling.decision() && repeated(step, ruling, answer)) {
                        changed = true;
                    }
                    if (changed)
                        session.changed();
                    return;
                }
                if (!session.recording || unrecorded == ruling.decision() || live.Session().Director().end() != null)
                    return;
                //What the opponent does by itself where the script is silent (its orders, its fallback) is what it
                // will do again without being told.  Only what the author has it do is written.
                if (!human && left != ruling.decision())
                    return;
                ScenarioDirector director = live.Session().Director();
                Placing placing = placing(ruling.player(), ruling.decision());
                if (placing.problem() != null) {
                    session.notice = "Not written down: " + placing.problem();
                    return;
                }
                JsonObject json = new JsonObject();
                json.add("when", placing.when());
                //(what was done once is written as something done once: the block's other decisions of the same
                // kind are still to be written, or to be let through by taking the 'once' off)
                Step block = placing.block();
                if (block != null)
                    json.add("once", true);
                json.add(human ? "allow" : "answer", term(ruling.decision(), answer));
                Step written = block == null ? ScenarioParser.parseStep(def, json, 0)
                        : ScenarioParser.parseStep(def, json, block.number, block, block.children.size() + 1);
                written.doOps.addAll(pending);
                pending.clear();
                if (block != null) {
                    director.addToBlock(written, true);
                } else if (placing.repeating() != null) {
                    //A step that repeats was under way, and this is not a decision it applies to or ends at.  It ends
                    // here now, and says so, so that the script reads as it was played
                    giveWay(placing);
                    director.recordAfterCurrent(written);
                } else {
                    director.record(written);
                }
                session.changed();
            } catch (ScenarioFormatException exp) {
                session.notice = "What was just done could not be written into the script: " + exp.getMessage();
            } finally {
                if (unrecorded == ruling.decision())
                    unrecorded = null;
            }
        }

        /**
         * What a decision's step waits for, as tightly as can be said without knowing what the decision will offer.
         * @param block The block the step is for, if it is for one: what the block already says for all its steps
         *              is not said again.
         */
        String whenFor(String player, AwaitingDecision decision, Step block) {
            ScenarioTable table = live.Table();
            StringBuilder when = new StringBuilder();
            when.append(table.SeatOf(player).key()).append(", ").append(AnswerCompiler.typeName(decision.getDecisionType()));
            Phase phase = table.GetCurrentPhase();
            String name = phase.name().toLowerCase(Locale.ROOT);
            if (Phase.findPhase(name) == phase && (block == null || block.when.phase != phase))
                when.append(", phase(").append(name).append(")");
            if (block == null || block.when.turn == null)
                when.append(", turn(").append(table.gameState().getTurnNumber()).append(")");
            //A card's trigger being offered ("Optional responses") is the same kind of decision, in the same phase
            // of the same turn, as the phase's own "play something or pass": only what it says tells them apart
            String said = decision.getText() == null ? "" : decision.getText().replaceAll("<[^>]*>", "").trim();
            if (decision.getDecisionType() == AwaitingDecisionType.CARD_ACTION_CHOICE && said.matches("(?i)(optional|required) responses"))
                when.append(", text(").append(said).append(")");
            return when.toString();
        }

        /**
         * Where a step for a decision that no step covers goes, and what has to give way to it.
         * @param index Where in the list of steps it is put.
         * @param repeating The step that repeats and is under way, which the new step comes after; null if there is
         *                  none, and the new step comes before the step the script is waiting on.
         * @param until What the repeating step's 'until' has to become for it to end at this decision.
         * @param when The new step's 'when'.
         * @param problem Why no step can be written for it; null if one can.
         * @param block The block the script is at, if the step goes into it (as the last of its steps); null if it
         *              is a step of the script itself.
         */
        record Placing(int index, Step repeating, String until, String when, String problem, Step block) { }

        Placing placing(String player, AwaitingDecision decision) {
            ScenarioDirector director = live.Session().Director();
            Step current = director.currentStep();
            //The script is at a block, and this is one of the decisions the block is for (it fits what the block
            // waits for, and is not where the block ends): none of the block's steps takes it, so one more does
            if (current != null && current.block && director.matches(current.when, player, decision))
                return new Placing(current.number - 1, null, null, whenFor(player, decision, current), null, current);
            String when = whenFor(player, decision, null);
            if (current == null || director.timesApplied() == 0)
                return new Placing(position() - 1, null, null, when, null, null);
            try {
                return new Placing(current.number, current, givingWay(current, player, decision), when, null, null);
            } catch (AuthoringException exp) {
                return new Placing(current.number, current, null, when, exp.getMessage(), null);
            }
        }

        /**
         * A step that repeats goes on applying until a decision arrives that fits its 'until'.  One has arrived that
         * it does not apply to and that does not fit: for a step to be written for that decision, the repeating step
         * has to end there.  This is its 'until' with whatever the decision does not fit taken out; and, if that
         * could then be taken for one of the decisions the step itself applies to, with enough of what the decision
         * is (its seat, its kind, the phase, the turn) put in to tell them apart.
         */
        String givingWay(Step repeating, String player, AwaitingDecision decision) throws AuthoringException {
            ScenarioDirector director = live.Session().Director();
            ScenarioTable table = live.Table();
            When until = repeating.until;
            String cannot = repeating.describe() + (repeating.block ? " is a block that" : " repeats and") + " is under way, and ";
            if (until.require != null || until.source.startsWith("{"))
                throw new AuthoringException(cannot + "its 'until' is written out with requirements.  Change it yourself (Until, on the Step tab) so that it fits this decision; "
                        + "then a step can be added for the decision.");
            When kept = new When();
            if (until.seat != null && fits(director, player, decision, part -> part.seat = until.seat))
                kept.seat = until.seat;
            if (until.decision != null && fits(director, player, decision, part -> part.decision = until.decision))
                kept.decision = until.decision;
            if (until.phase != null && fits(director, player, decision, part -> part.phase = until.phase))
                kept.phase = until.phase;
            if (until.turn != null && fits(director, player, decision, part -> part.turn = until.turn))
                kept.turn = until.turn;
            if (until.skirmish != null && fits(director, player, decision, part -> part.skirmish = until.skirmish))
                kept.skirmish = until.skirmish;
            if (until.offers != null && fits(director, player, decision, part -> part.offers = until.offers))
                kept.offers = until.offers;
            if (until.fighting != null && fits(director, player, decision, part -> part.fighting = until.fighting))
                kept.fighting = until.fighting;
            if (until.text != null && fits(director, player, decision, part -> part.text = until.text))
                kept.text = until.text;
            When own = repeating.when;
            if (!apart(kept, own))
                kept.seat = table.SeatOf(player);
            if (!apart(kept, own))
                kept.decision = decision.getDecisionType();
            if (!apart(kept, own))
                kept.phase = table.GetCurrentPhase();
            if (!apart(kept, own))
                kept.turn = table.gameState().getTurnNumber();
            if (!apart(kept, own))
                throw new AuthoringException(cannot + "nothing simple tells this decision from the ones it applies to.  Change its 'until' yourself (Until, on the Step tab) "
                        + "so that it fits this decision; then a step can be added for the decision.");
            List<String> terms = new ArrayList<>();
            if (kept.seat != null)
                terms.add(kept.seat.key());
            if (kept.decision != null)
                terms.add(AnswerCompiler.typeName(kept.decision));
            if (kept.phase != null)
                terms.add("phase(" + kept.phase.name().toLowerCase(Locale.ROOT) + ")");
            if (kept.turn != null)
                terms.add("turn(" + kept.turn + ")");
            if (kept.skirmish != null)
                terms.add("skirmish(" + kept.skirmish + ")");
            if (kept.offers != null)
                terms.add("offers(" + kept.offers + ")");
            if (kept.fighting != null)
                terms.add("fighting(" + kept.fighting + ")");
            if (kept.text != null)
                terms.add("text(" + kept.text + ")");
            return String.join(", ", terms);
        }

        private boolean fits(ScenarioDirector director, String player, AwaitingDecision decision, Consumer<When> part) {
            When one = new When();
            part.accept(one);
            return director.matches(one, player, decision);
        }

        /** Whether no decision can fit both: they say different things about the same part of it. */
        private boolean apart(When one, When other) {
            return (one.seat != null && other.seat != null && one.seat != other.seat)
                    || (one.decision != null && other.decision != null && one.decision != other.decision)
                    || (one.phase != null && other.phase != null && one.phase != other.phase)
                    || (one.turn != null && other.turn != null && !one.turn.equals(other.turn))
                    || (one.skirmish != null && other.skirmish != null && !one.skirmish.equals(other.skirmish));
        }

        /** Has the repeating step end at the decision a step is being written for, and tells the author it has. */
        void giveWay(Placing placing) throws ScenarioFormatException {
            Step repeating = placing.repeating();
            String was = repeating.until.source;
            JsonObject json = ScenarioWriter.step(repeating);
            json.set("until", placing.until());
            copyInto(repeating, reparsed(repeating, json));
            session.notice = "Step " + repeating.label() + (repeating.block ? " is a block, and was still under way.  Its 'until' was \"" : " repeats, and was still under way.  Its 'until' was \"") + was + "\" and is now \""
                    + placing.until() + "\", so that it ends where the new step begins.";
        }

        /** The decision that is waiting and that no step covers: the player's, or the opponent's if it has been left for the author. */
        Ruling uncovered() {
            if (over() || inHand() != null)
                return null;
            for (String name : List.of(live.HumanPlayer(), live.OpponentPlayer())) {
                Ruling ruling = live.Session().Ruling(name);
                if (ruling == null || ruling.step() != null || live.Table().userFeedback().getAwaitingDecision(name) != ruling.decision())
                    continue;
                if (name.equals(live.HumanPlayer()) || (live.Held() != null && live.Held().decision() == ruling.decision()))
                    return ruling;
            }
            return null;
        }

        /** A step somebody put in by hand and has said nothing in yet, apart perhaps from what it waits for. */
        boolean blank(Step step) {
            return !step.pause && step.allow == null && step.answers.isEmpty() && step.messages.isEmpty() && step.doOps.isEmpty()
                    && step.end == null && step.until == null && step.skipWhen == null && step.onlyIf.isEmpty() && step.unless.isEmpty();
        }

        /**
         * Recording, at a step that repeats (it has an 'until'): what was just done is written into its 'answer' as
         * the answer for this time round, which is what the script plays when it plays itself through the step, and
         * into what it allows if it is not there already.  A Pass is not added to what it allows where the step says
         * when Pass is allowed (its 'passWhen').  Where the step's answers already say this for this time round,
         * nothing changes; where they say something else, what was done replaces it and the answers after it go:
         * they were for a way through that is not this one.
         * @return Whether the step was changed.
         */
        boolean repeated(Step step, Ruling ruling, String answer) throws ScenarioFormatException {
            ScenarioDirector director = live.Session().Director();
            int index = director.currentStep() == step ? director.timesApplied() - 1 : -1;
            if (index < 0)
                return false;
            List<String> answers = new ArrayList<>();
            for (Answer one : step.answers)
                answers.add(one.source());
            if (index > answers.size()) {
                //(the times round before this one were not written down: the list would not line up)
                session.notice = "Not written into step " + step.label() + "'s answers: it has applied " + index
                        + " times before this, and its answers say what to do for only " + answers.size() + ".  Go back to the step (Play to here) and do it again.";
                return false;
            }
            String term = term(ruling.decision(), answer);
            if (index < answers.size() && answers.get(index).equals(term))
                return false;
            List<String> now = new ArrayList<>(answers.subList(0, index));
            now.add(term);
            JsonObject json = ScenarioWriter.step(step);
            json.set("answer", String.join(", ", now));
            boolean pass = "pass".equals(term);
            boolean allowed = false;
            if (step.allow != null)
                for (Answer one : step.allow.answers)
                    if (one.source().equals(term))
                        allowed = true;
            if (!allowed && !(pass && step.allow != null && step.allow.passWhen != null)) {
                JsonValue allow = json.get("allow");
                if (allow == null) {
                    json.set("allow", term);
                } else if (allow.isString()) {
                    json.set("allow", allow.asString().isBlank() ? term : allow.asString() + ", " + term);
                } else if (allow.isObject()) {
                    JsonValue had = allow.asObject().get("answers");
                    String was = had == null ? "" : had.isString() ? had.asString() : "";
                    allow.asObject().set("answers", was.isBlank() ? term : was + ", " + term);
                }
            }
            copyInto(step, reparsed(step, json));
            return true;
        }

        /**
         * An answer in the scenario's words, having made sure that those words give the same answer back.  Where
         * they do not (or there are none) it is the answer itself, as the game takes it.
         */
        String term(AwaitingDecision decision, String answer) {
            ScenarioTable table = live.Table();
            try {
                String term = AnswerCompiler.decompile(table, decision, answer);
                List<Terms.Term> terms = Terms.parse(term, "answer");
                if (terms.size() == 1) {
                    Answer parsed = ScenarioParser.parseAnswer(def, terms.getFirst(), "answer");
                    if (sameAnswer(decision, AnswerCompiler.compile(table, decision, parsed), answer))
                        return term;
                }
            } catch (ScenarioFormatException | ScenarioScriptException | RuntimeException exp) {
                //Said as the game says it, then
            }
            return "raw(" + answer + ")";
        }

        /** The step the game is waiting on an answer (or a reading) for; null if the script has nothing in hand. */
        Step inHand() {
            for (String player : List.of(live.HumanPlayer(), live.OpponentPlayer())) {
                Ruling ruling = live.Session().Ruling(player);
                if (ruling != null && ruling.step() != null)
                    return ruling.step();
            }
            return null;
        }

        /** The number of the step the script is at: the one in hand, or the next it is waiting to come to. */
        int position() {
            Step current = live.Session().Director().currentStep();
            return current == null ? def.steps.size() + 1 : current.number;
        }

        boolean over() {
            return live.Result() != null || mediator.isFinished() || live.Session().Director().end() != null;
        }

        /**
         * Makes the game in hand follow a changed draft without starting again, if the change is one it can take:
         * nothing about how the game is set up, and nothing about what the steps already used (and the one in hand)
         * waited for or answered.
         * @return Whether it did.  If not, nothing has been changed.
         */
        boolean follow(ScenarioDefinition fresh, Names given) {
            List<String> uids = given.uids;
            shown = false;
            if (!author || over())
                return false;
            if (!without(ScenarioWriter.toJson(def), ABOUT_IT).equals(without(ScenarioWriter.toJson(fresh), ABOUT_IT)))
                return false;
            ScenarioDirector director = live.Session().Director();
            Step inHand = inHand();
            int done = position() - 1;
            int kept = inHand != null || director.timesApplied() > 0 ? done + 1 : done;
            if (fresh.steps.size() < kept || def.steps.size() < kept)
                return false;
            boolean somethingWaiting = live.Session().Ruling(live.HumanPlayer()) != null || live.Session().Ruling(live.OpponentPlayer()) != null;
            for (int i = 0; i < kept; i++) {
                Step was = def.steps.get(i), now = fresh.steps.get(i);
                if (was.block != now.block || !sameButShown(was, now))
                    return false;
                if (!was.block)
                    continue;
                List<String> names = given.under(i);
                if (was != director.currentStep()) {
                    //A block the game has been through: its steps are the ones that were played, in the order they came
                    if (was.children.size() != now.children.size())
                        return false;
                    for (int k = 0; k < was.children.size(); k++)
                        if (!uid(was.children.get(k)).equals(k < names.size() ? names.get(k) : null)
                                || !sameButShown(was.children.get(k), now.children.get(k)))
                            return false;
                    continue;
                }
                //The block the game is in: the steps it has used are still there and still answer as they did.  The
                // rest of them are what the block's remaining decisions will be put to, and can be anything.
                for (Step child : was.children) {
                    boolean used = child == inHand
                            || director.statuses().get(child.label()) == ScenarioDirector.StepStatus.MATCHED;
                    if (!used)
                        continue;
                    int at = names.indexOf(uid(child));
                    if (at < 0 || at >= now.children.size() || !sameButShown(child, now.children.get(at)))
                        return false;
                }
                //(a decision that is already in front of somebody cannot be taken back to be read over: a pause
                // that is new to the block, or that waits for something else than it did, might be over that one)
                if (inHand == null && somethingWaiting) {
                    Map<String, Step> had = new LinkedHashMap<>();
                    for (Step child : was.children)
                        had.put(uid(child), child);
                    for (int k = 0; k < now.children.size(); k++) {
                        Step child = now.children.get(k);
                        Step same = k < names.size() && names.get(k) != null ? had.get(names.get(k)) : null;
                        if (child.pause && (same == null || !sameButShown(same, child)))
                            return false;
                    }
                }
            }
            //A decision that is already in front of somebody cannot be taken back to be read over
            if (inHand == null && somethingWaiting) {
                //(the first step still to come, or any that may be passed over on the way to it)
                for (int i = kept; i < fresh.steps.size(); i++) {
                    Step next = fresh.steps.get(i);
                    if (next.pause || next.children.stream().anyMatch(child -> child.pause))
                        return false;
                    if (next.onlyIf.isEmpty() && next.unless.isEmpty())
                        break;
                }
            }

            Set<String> unanswered = new HashSet<>();
            for (Step step : awaiting)
                unanswered.add(uid(step));
            director.restructure(() -> {
                for (int i = 0; i < kept; i++) {
                    Step was = def.steps.get(i), now = fresh.steps.get(i);
                    copyInto(was, now);
                    if (!was.block)
                        continue;
                    //The steps in a block are known by their names: the ones it had stay the steps they were (the
                    // script knows which of them it has used), wherever in the block they now come
                    List<String> names = given.under(i);
                    Map<String, Step> had = new LinkedHashMap<>();
                    for (Step child : was.children)
                        had.put(uid(child), child);
                    List<Step> children = new ArrayList<>();
                    for (int k = 0; k < now.children.size(); k++) {
                        Step child = now.children.get(k);
                        Step same = k < names.size() && names.get(k) != null ? had.remove(names.get(k)) : null;
                        if (same != null)
                            copyInto(same, child);
                        children.add(same != null ? same : child);
                    }
                    was.children.clear();
                    was.children.addAll(children);
                }
                while (def.steps.size() > kept)
                    def.steps.removeLast();
                for (int i = kept; i < fresh.steps.size(); i++)
                    def.steps.add(fresh.steps.get(i));
            }, null);
            def.title = fresh.title;
            def.summary = fresh.summary;
            def.author = fresh.author;
            def.tags = fresh.tags;
            def.note = fresh.note;
            def.version = fresh.version;
            def.flags.clear();
            def.flags.addAll(fresh.flags);
            //(the steps after the ones in use are new copies of themselves, and so is any step new to a block)
            Set<Step> still = Collections.newSetFromMap(new IdentityHashMap<>());
            still.addAll(def.allSteps());
            awaiting.retainAll(still);
            for (int i = 0; i < def.steps.size(); i++) {
                Step step = def.steps.get(i);
                named(this, step, i < uids.size() ? uids.get(i) : null, unanswered);
                List<String> under = given.under(i);
                for (int k = 0; k < step.children.size(); k++)
                    named(this, step.children.get(k), k < under.size() ? under.get(k) : null, unanswered);
            }

            if (inHand != null) {
                live.Reshow();
                shown = true;
            } else {
                shown = live.Session().Reconsider();
            }
            return true;
        }
    }

    private static String without(JsonObject json, Set<String> names) {
        JsonObject rest = new JsonObject();
        for (JsonObject.Member member : json)
            if (!names.contains(member.getName()))
                rest.add(member.getName(), member.getValue());
        return rest.toString();
    }

    /** Whether two steps wait for, do and answer the same: they differ, if at all, in what they show and allow. */
    private static boolean sameButShown(Step one, Step other) {
        return without(ScenarioWriter.step(one), SHOWN_OR_UNDER).equals(without(ScenarioWriter.step(other), SHOWN_OR_UNDER));
    }

    /** Whether a step says nothing yet about what is done at it, so that what its author does there can be written in. */
    private static boolean unanswered(Step step) {
        return !step.pause && !step.block && step.allow == null && step.answers.isEmpty();
    }

    /** Gives a step of a fresh game the name it had, and has it go on waiting for its answer if it was. */
    private static void named(Sitting sitting, Step step, String uid, Set<String> awaiting) {
        if (uid == null)
            return;
        sitting.names.put(step, uid);
        if (awaiting.contains(uid) && unanswered(step))
            sitting.awaiting.add(step);
    }

    /** Everything about a step but the steps in it (a block's are its own, and are seen to one by one). */
    private static void copyInto(Step target, Step source) {
        target.once = source.once;
        target.id = source.id;
        target.note = source.note;
        target.when = source.when;
        target.until = source.until;
        target.skipWhen = source.skipWhen;
        target.checkpoint = source.checkpoint;
        target.pause = source.pause;
        target.onlyIf = source.onlyIf;
        target.unless = source.unless;
        target.doOps = source.doOps;
        target.messages = source.messages;
        target.allow = source.allow;
        target.answers = source.answers;
        target.end = source.end;
    }

    private static boolean sameAnswer(AwaitingDecision decision, String one, String other) {
        if (one.equals(other))
            return true;
        //Cards chosen (or groups assigned) in another order are the same choice
        return switch (decision.getDecisionType()) {
            case CARD_SELECTION, ARBITRARY_CARDS ->
                    new HashSet<>(Arrays.asList(one.trim().split(" *, *"))).equals(new HashSet<>(Arrays.asList(other.trim().split(" *, *"))));
            case ASSIGN_MINIONS -> assignments(one).equals(assignments(other));
            default -> false;
        };
    }

    /**
     * An assignment as the groups that say anything.  The game screen lists every character, with or without minions
     * ("12,14 30 31,16"); a scenario only says who fights whom ("14 30 31").
     */
    private static Set<Set<String>> assignments(String answer) {
        Set<Set<String>> groups = new HashSet<>();
        for (String group : answer.split(",")) {
            List<String> ids = new ArrayList<>(Arrays.asList(group.trim().split(" +")));
            if (ids.size() < 2)
                continue;
            //(which character, and which minions: the minions in any order)
            Set<String> said = new HashSet<>(ids.subList(1, ids.size()));
            said.add("to " + ids.getFirst());
            groups.add(said);
        }
        return groups;
    }

    /**
     * A place in the script for a game to be brought to.
     * @param step The number of the step.
     * @param sub If that step is a block: the one of its steps to be brought to, counting from 1; 0 for none.
     * @param times If it is a block and none of its steps is named: how many decisions its steps are to have answered.
     */
    private record At(int step, int sub, int times) {
        static At step(int number) {
            return new At(Math.max(1, number), 0, 0);
        }
    }

    /**
     * The names the steps of a draft go by between here and the author's client, as a change to the draft has them.
     * A step cannot be known there by where it comes: that changes whenever the game writes a step in.
     */
    private static final class Names {
        /** One for each step, in order; null where a step is new. */
        List<String> uids;
        /** For each step, in order, the names of the steps in it (none, unless it is a block). */
        List<List<String>> kids;

        Names(List<String> uids, List<List<String>> kids) {
            this.uids = uids;
            this.kids = kids;
        }

        List<String> under(int index) {
            return kids != null && index < kids.size() && kids.get(index) != null ? kids.get(index) : List.of();
        }
    }

    /**
     * What a fresh game takes over from the one before it.
     * @param draft The draft the fresh game is of; null if whoever asked has it already.
     * @param names The names of the draft's steps; null if they have none yet.
     * @param awaiting The names of the steps that are waiting to be given their answer.
     * @param at Where to play the script up to.
     * @param dropped Changes to the board that no step carried yet: the fresh game does not have them.
     */
    private record Carried(ScenarioDefinition draft, Names names, Set<String> awaiting, At at, List<String> dropped) { }

    /**
     * A copy of the draft as the game in hand has it, and what goes with it, taken while that game is held still.
     * @param at Where the fresh game is to be brought to; null for where this one is.
     */
    private Carried carry(Sitting sitting, At at) throws AuthoringException {
        AuthoringException[] failure = new AuthoringException[1];
        Carried carried = sitting.mediator.scenarioLocked(() -> {
            try {
                Carried rest = sitting.carry(new Names(sitting.uids(), sitting.kids()), at != null ? at : sitting.at());
                return new Carried(copy(sitting.def, null), rest.names(), rest.awaiting(), rest.at(), rest.dropped());
            } catch (AuthoringException exp) {
                failure[0] = exp;
                return null;
            }
        });
        if (carried == null)
            throw failure[0];
        return carried;
    }

    private Session session(Player player) throws AuthoringException {
        Session session = _sessions.get(player.getName());
        if (session == null)
            throw new AuthoringException("You have no scenario open for writing.  Open one from My Account > My Scenarios in the Game Hall.");
        session.left = false;
        return session;
    }

    /** Refuses to change a scenario that is in review, unless it is an admin who is changing it. */
    private void notLocked(Session session, String owner, String path) throws AuthoringException {
        if (path != null && !session.player.hasType(Player.Type.ADMIN) && _scenarios.locked(owner, path))
            throw new AuthoringException("It has been submitted for review, and cannot be changed until an admin has approved or "
                    + "rejected it.  To go on working on it, copy it in My Scenarios.");
    }

    /**
     * For submitting the author's scenario for review: its file, which has to be theirs and saved as it stands.
     * @return The path of the file within the author's folder.
     */
    public String submitting(Player player) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            if (!session.onDisk)
                throw new AuthoringException("It has not been saved yet.  Save it first.");
            if (session.dirty)
                throw new AuthoringException("It has changes that have not been saved.  Save it first (what is submitted is what is saved).");
            if (!player.getName().equals(session.owner))
                throw new AuthoringException("Only a scenario in your own folder can be submitted.");
            return session.path;
        }
    }

    /** Whether a player has a draft of this file open with changes that are not in it. */
    public boolean unsavedOn(String owner, String path) {
        Session session = owner == null ? null : _sessions.get(owner);
        return session != null && owner.equals(session.owner) && path.equals(session.path) && session.dirty;
    }

    /** Closes a player's draft if it is of this file: it has been locked (submitted for review). */
    public void closeOn(String owner, String path) {
        Session session = owner == null ? null : _sessions.get(owner);
        if (session != null && owner.equals(session.owner) && path.equals(session.path))
            close(session.player);
    }

    /** The author's page says it is being closed (or reloaded: if so, it will be heard from again in a moment). */
    public void left(Player player) {
        Session session = _sessions.get(player.getName());
        if (session != null)
            session.left = true;
    }

    /** A place for a scenario's file: whose folder (null for the game's own), and where in it. */
    private record Place(String owner, String path) {
        String shown() {
            return (owner == null ? "" : "users/" + owner + "/") + path;
        }
    }

    /** The folder scenarios of a kind are kept under: the first division of the game's own folder, and of an author's. */
    public static String kindFolder(ScenarioDefinition.Kind kind) {
        return switch (kind) {
            case TUTORIAL -> "tutorials";
            case PUZZLE -> "puzzles";
            case SANDBOX -> "sandboxes";
        };
    }

    private static final Set<String> KIND_FOLDERS = Set.of("tutorials", "puzzles", "sandboxes");

    /**
     * Where a scenario's file goes, given where its author would have it: under the folder of its kind, whatever
     * folders and name were asked for below that.  A path that already begins with the folder of a kind (this one or
     * another) has that folder replaced, so that a scenario whose kind changes moves across.
     */
    public static String filed(String path, ScenarioDefinition.Kind kind) {
        if (path == null)
            return null;
        String rest = path.trim();
        int slash = rest.indexOf('/');
        if (slash > 0 && KIND_FOLDERS.contains(rest.substring(0, slash).toLowerCase(Locale.ROOT)))
            rest = rest.substring(slash + 1);
        return kindFolder(kind) + "/" + rest;
    }

    private static ScenarioDefinition.Kind kindOf(Session session) {
        Sitting sitting = session.sitting;
        return sitting.mediator.scenarioLocked(() -> sitting.def.kind);
    }

    /**
     * @param path Folders and a file name, ending in .hjson.  (The page tidies what its author types; here it is
     *             taken as it comes, and anything that is not plainly a place in the folder is refused.)
     * @throws AuthoringException if a scenario cannot be kept there.
     */
    private Place place(Player player, String path, boolean canonical, boolean admin) throws AuthoringException {
        boolean ownFolder = _scenarios.hasUserFolder() && !(canonical && admin);
        if (!ownFolder && !admin)
            throw new AuthoringException("This server has nowhere to keep scenarios that are not its own.");
        if (path == null || path.isBlank())
            throw new AuthoringException("Say where: a file name, with folders before it if it goes in any (tutorials/basics-02-the-ring.hjson).");
        Place place = new Place(ownFolder ? player.getName() : null, path.trim());
        try {
            _scenarios.check(place.owner(), place.path());
        } catch (IOException exp) {
            throw new AuthoringException(exp.getMessage());
        }
        return place;
    }

    /** A copy of a definition that shares nothing with it: written out as its file would be, and read back. */
    private static ScenarioDefinition copy(ScenarioDefinition def, String id) throws AuthoringException {
        try {
            JsonObject json = ScenarioWriter.toJson(def);
            if (id != null)
                json.set("id", id);
            return ScenarioParser.parse(json.toString());
        } catch (ScenarioFormatException | RuntimeException exp) {
            throw new AuthoringException("The draft cannot be written out and read back: " + exp.getMessage());
        }
    }

    /**
     * Starts a fresh game of a draft and makes it the session's.
     * @param def The draft, which becomes the new game's own: nothing else may have hold of it.
     * @param author False to start a player's game of it.
     * @param carried What is taken over from the game before, and the step the script plays itself up to.
     */
    private void start(Session session, ScenarioDefinition def, boolean author, Carried carried) throws AuthoringException {
        LotroFormat format = _formats.apply(def.format);
        if (format == null)
            throw new AuthoringException("format: there is no format called '" + def.format + "'");
        List<String> errors = ScenarioValidator.validate(def, _library);
        if (!errors.isEmpty())
            throw new AuthoringException(String.join("\n", errors));

        int runTo = carried.at().step();
        Sitting sitting = new Sitting(session, def, format, author);
        Names names = carried.names();
        if (names != null) {
            for (int i = 0; i < def.steps.size() && i < names.uids.size(); i++) {
                Step step = def.steps.get(i);
                named(sitting, step, names.uids.get(i), carried.awaiting());
                List<String> under = names.under(i);
                for (int k = 0; k < step.children.size() && k < under.size(); k++)
                    named(sitting, step.children.get(k), under.get(k), carried.awaiting());
            }
        }
        try {
            LiveScenario live = new LiveScenario(def, _library, format, session.player.getName(),
                    new ScenarioText(def, _library, format, _cardLookup), _timer);
            if (author)
                live.SetAuthor(sitting);
            else
                live.SetDraftTest();
            live.RunTo(runTo, carried.at().sub(), carried.at().times(), session::choice);
            sitting.live = live;
            sitting.mediator = _tables.seat(session.player, live);
        } catch (RuntimeException exp) {
            LOG.warn("A draft of " + def.id + " could not be started for " + session.player.getName(), exp);
            throw new AuthoringException("The game could not be started: " + exp.getMessage());
        }
        session.sitting = sitting;
        session.testFrom = author ? null : runTo;
        String problem = sitting.live.RunProblem();
        String notice = problem == null ? null : "The script stopped before step " + runTo + ": " + problem;
        session.stopNotice = notice;
        session.stoppedUncovered = problem != null && problem.startsWith("no step covers");
        if (!carried.dropped().isEmpty())
            notice = (notice == null ? "" : notice + "\n") + "Changes to the board that no step carried yet are not in this game: "
                    + String.join(", ", carried.dropped());
        session.notice = notice;
        session.revision.incrementAndGet();
    }

    private static Carried fromTheStart() {
        return new Carried(null, null, Set.of(), At.step(1), List.of());
    }

    /* ------------------------------------------------------------------ what an author can ask for */

    /**
     * Opens one of the library's scenarios for writing.  Whatever draft the author had open is dropped.
     *
     * A file with mistakes in it is opened as well, if it can be read with the mistaken parts left out: the draft is
     * then the file without them, the author is shown what they were, and the draft counts as changed from the start.
     */
    public Map<String, Object> open(Player player, String id, boolean admin) throws AuthoringException {
        ScenarioLibrary.Entry entry = _scenarios.get(id);
        if (entry == null || entry.def() == null)
            throw new AuthoringException("There is no scenario called '" + id + "' that can be read.");
        if (!entry.openable())
            throw new AuthoringException("That scenario's file has mistakes that have to be put right in the file itself:\n" + String.join("\n", entry.errors()));
        if (!admin && (entry.owner() == null || !entry.owner().equals(player.getName())))
            throw new AuthoringException("That scenario is not yours to change.");
        if (!admin && _scenarios.locked(entry.owner(), entry.path()))
            throw new AuthoringException("It has been submitted for review, and cannot be changed until an admin has approved or "
                    + "rejected it.  To go on working on it, copy it in My Scenarios.");
        //A user's scenario is known to the library by its owner's name as well; its file is not
        String fileId = entry.owner() == null ? entry.id() : entry.id().substring(entry.owner().length() + 1);
        Session session = new Session(player);
        session.owner = entry.owner();
        session.path = entry.path();
        session.onDisk = true;
        synchronized (session) {
            ScenarioDefinition draft = copy(entry.def(), fileId);
            session.commentsLost = commentsLost(_scenarios.source(entry), draft);
            start(session, draft, true, fromTheStart());
            if (!entry.leftOut().isEmpty()) {
                //(steps keep their names as the author moves them about; where one was when the file was read is
                // only where it is until then)
                List<String> names = session.sitting.uids();
                List<String> steps = new ArrayList<>();
                for (ScenarioSalvage.LeftOut one : entry.leftOut())
                    steps.add(one.step() >= 1 && one.step() <= names.size() ? names.get(one.step() - 1) : null);
                session.leftOut = entry.leftOut();
                session.leftOutSteps = steps;
                session.dirty = true;
            }
            _sessions.put(player.getName(), session);
            return result(session, false, true);
        }
    }

    public Map<String, Object> create(Player player, String id, String title) throws AuthoringException {
        return create(player, id, title, false, true);
    }

    /**
     * Starts a new scenario: two Ring-bearers with their Rings, a Fellowship adventure path, and nothing else.  Its
     * file is named after it there and then (and written when it is first saved): a tutorial of the game's own goes
     * in tutorials/, an author's own in their folder.
     * @param canonical Whether it is to be one of the game's own (an admin's to say; otherwise it is the author's).
     */
    public Map<String, Object> create(Player player, String id, String title, boolean canonical, boolean admin) throws AuthoringException {
        if (id == null || !id.matches("[a-z0-9][a-z0-9-]{1,60}"))
            throw new AuthoringException("A scenario's id is made of small letters, digits and dashes: basics-02-the-ring");
        Place place = place(player, filed(id + ".hjson", ScenarioDefinition.Kind.TUTORIAL), canonical, admin);
        if (_scenarios.exists(place.owner(), place.path()))
            throw new AuthoringException("There is already a scenario at " + place.shown() + ".  Give the new one another id (or open that one, if it is the one you mean to change).");
        if (_scenarios.get(place.owner() == null ? id : place.owner() + "/" + id) != null)
            throw new AuthoringException("There is already a scenario called '" + id + "'.  Give the new one another id.");
        String text = """
                {
                	id: %s
                	version: 1
                	kind: tutorial
                	title: %s
                	author: %s
                	format: scenario
                	human: freeps
                	opponent: {
                		fallback: pass
                	}
                	coverage: strict
                	cards: {
                		freeps: {
                			frodo: 10_121
                			ring: 1_2
                		}
                		shadow: {
                			oppFrodo: 10_121
                			oppRing: 1_2
                		}
                	}
                	ringBearer: {
                		freeps: frodo
                		shadow: oppFrodo
                	}
                	ring: {
                		freeps: ring
                		shadow: oppRing
                	}
                	sites: {
                		freeps: 1_323, 1_329, 1_341, 1_348, 1_349, 1_350, 1_355, 1_358, 1_362
                		shadow: 1_323, 1_329, 1_341, 1_348, 1_349, 1_350, 1_355, 1_358, 1_362
                	}
                	rules: {
                		oneSided: true
                	}
                }
                """.formatted(id, org.hjson.JsonValue.valueOf(title == null || title.isBlank() ? id : title.trim()).toString(),
                org.hjson.JsonValue.valueOf(player.getName()).toString());
        ScenarioDefinition def;
        try {
            def = ScenarioParser.parse(text);
        } catch (ScenarioFormatException exp) {
            throw new AuthoringException(exp.getMessage());
        }
        Session session = new Session(player);
        session.owner = place.owner();
        session.path = place.path();
        synchronized (session) {
            start(session, def, true, fromTheStart());
            session.dirty = true;
            _sessions.put(player.getName(), session);
            return result(session, false, true);
        }
    }

    /**
     * A file written by hand can say things in comments.  The draft is the scenario, not its file, and what is saved
     * is the scenario written out afresh: the names of its cards and the numbers of its steps are put back as
     * comments, and any other comment is gone.  (Notes are part of the scenario, and are kept.)
     * @return How many lines of comment the file has that the file written from the draft would not have.
     */
    private int commentsLost(String source, ScenarioDefinition draft) {
        if (source == null)
            return 0;
        Set<String> kept = new HashSet<>();
        for (String line : ScenarioWriter.toHjson(draft, _library).split("\n"))
            if (line.strip().startsWith("#"))
                kept.add(line.strip());
        int lost = 0;
        for (String line : source.split("\n")) {
            String comment = line.strip();
            if ((comment.startsWith("#") || comment.startsWith("//")) && !kept.contains(comment))
                lost++;
        }
        return lost;
    }

    /**
     * @return The draft a player has open, for the Game Hall: {gameId, id, title, over, test}; null if they have
     * none.  'over' means its game cannot be gone back to as it is (resume starts another).
     */
    public Map<String, Object> current(Player player) {
        Session session = _sessions.get(player.getName());
        if (session == null)
            return null;
        Sitting sitting = session.sitting;
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("gameId", sitting.mediator.getGameId());
        info.put("id", sitting.def.id);
        info.put("title", sitting.def.title);
        info.put("over", sitting.mediator.isFinished() || sitting.mediator.isDestroyed());
        info.put("test", !sitting.author);
        info.put("dirty", session.dirty);
        return info;
    }

    /**
     * The Game Hall is asking what its player has open.  A draft whose page has been closed, with nothing in it that
     * is not in its file, is done with: there is nothing to go back to it for, and it is closed here.
     * @return As current.
     */
    public Map<String, Object> currentForHall(Player player) {
        Session session = _sessions.get(player.getName());
        if (session != null && session.left && !session.dirty) {
            close(player);
            return null;
        }
        return current(player);
    }

    public Map<String, Object> state(Player player) {
        Session session = _sessions.get(player.getName());
        if (session == null) {
            Map<String, Object> none = new LinkedHashMap<>();
            none.put("session", false);
            return none;
        }
        session.left = false;
        synchronized (session) {
            return describe(session);
        }
    }

    /** Changes the draft as its file would have it, and the names of its steps alongside. */
    private interface Patcher {
        void patch(JsonObject json, Names names) throws AuthoringException;
    }

    /**
     * Changes the draft, and has the game follow: the game in hand if it can take the change, a fresh one if not.
     * Reading the draft, changing it and having the game follow happen with the game held still, so that nothing the
     * game writes into the script meanwhile (the author answering a decision) can be lost between them.
     * @param goTo Where to be afterwards: a step for a fresh game to be brought to.  Null to stay put, in the game in
     *             hand if it can take the change and in a fresh one at the same step if not.
     */
    private Map<String, Object> change(Session session, Integer goTo, Patcher patcher) throws AuthoringException {
        return change(session, goTo, patcher, Set.of());
    }

    /**
     * @param unanswered The names of steps the change puts in that are to be given their answer by what the author
     *                   does when the game comes to them.
     */
    private Map<String, Object> change(Session session, Integer goTo, Patcher patcher, Set<String> unanswered) throws AuthoringException {
        Sitting sitting = session.sitting;
        Object[] out = new Object[2];
        String problem = sitting.mediator.scenarioLocked(() -> {
            try {
                JsonObject json = ScenarioWriter.toJson(sitting.def);
                Names names = new Names(sitting.uids(), sitting.kids());
                patcher.patch(json, names);
                ScenarioDefinition fresh = ScenarioParser.parse(json.toString());
                if (_formats.apply(fresh.format) == null)
                    return "format: there is no format called '" + fresh.format + "'";
                List<String> errors = ScenarioValidator.validate(fresh, _library);
                if (!errors.isEmpty())
                    return String.join("\n", errors);
                if (goTo == null && sitting.follow(fresh, names)) {
                    for (Step step : sitting.def.allSteps())
                        if (unanswered.contains(sitting.names.get(step)) && unanswered(step))
                            sitting.awaiting.add(step);
                    return null;
                }
                out[0] = fresh;
                Carried carried = sitting.carry(names, goTo != null ? At.step(goTo) : sitting.at());
                Set<String> waiting = new HashSet<>(carried.awaiting());
                waiting.addAll(unanswered);
                out[1] = new Carried(carried.draft(), carried.names(), waiting, carried.at(), carried.dropped());
                return null;
            } catch (AuthoringException | ScenarioFormatException exp) {
                return exp.getMessage();
            } catch (RuntimeException exp) {
                //(a step that is not there, a patch that is not one)
                return "The change could not be made to the draft as the server has it: " + exp + ".  Reload the page to see where it stands.";
            }
        });
        if (problem != null)
            throw new AuthoringException(problem);
        if (out[0] == null) {
            session.changed();
            return result(session, sitting.shown, false);
        }
        start(session, (ScenarioDefinition) out[0], true, (Carried) out[1]);
        session.dirty = true;
        return result(session, false, true);
    }

    /**
     * The whole draft, changed.  (An author's client cannot send this much in one request; see edit.)
     * @param json The scenario as its file has it (see ScenarioWriter.toJson).
     * @param goTo See change.
     */
    public Map<String, Object> draft(Player player, String json, Integer goTo) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            return change(session, goTo, (draft, names) -> {
                List<String> uids = names.uids;
                JsonObject given;
                try {
                    given = org.hjson.JsonValue.readHjson(json).asObject();
                } catch (RuntimeException exp) {
                    throw new AuthoringException("That is not a scenario: " + exp.getMessage());
                }
                for (String name : new ArrayList<>(draft.names()))
                    draft.remove(name);
                for (JsonObject.Member member : given)
                    draft.add(member.getName(), member.getValue());
                //Steps are taken to be the ones that were there, as far as there are that many
                int count = given.get("steps") != null && given.get("steps").isArray() ? given.get("steps").asArray().size() : 0;
                while (uids.size() > count)
                    uids.removeLast();
                while (uids.size() < count)
                    uids.add(null);
                //(and the steps in a block likewise: the ones it had, as far as it still has that many)
                List<List<String>> kids = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    org.hjson.JsonValue step = given.get("steps").asArray().get(i);
                    org.hjson.JsonValue under = step.isObject() ? step.asObject().get("anyOrder") : null;
                    int size = under != null && under.isArray() ? under.asArray().size() : 0;
                    List<String> had = new ArrayList<>(names.under(i));
                    while (had.size() > size)
                        had.removeLast();
                    while (had.size() < size)
                        had.add(null);
                    kids.add(had);
                }
                names.kids = kids;
            });
        }
    }

    /**
     * The author's client says what it has changed in the draft.  (It does not send the whole draft back each time:
     * a scenario's file is larger than the server lets a request be.)
     * @param patchJson What changed: {top: {name: value, ...}, topText: {name: text, ...}, steps: [{op, ...}, ...]}.
     *                  'top' replaces top-level parts of the file (anything but its steps), a null value taking the
     *                  part out.  'topText' does the same with each part given as the file would have it, in HJSON
     *                  (with or without its name in front; empty to take the part out).  'steps' is
     *                  carried out in order.  A step is said by its name, 'uid', as the state gives it (or, failing
     *                  that, by 'index', counting from 0): where steps come changes under the client as the game
     *                  writes new ones, and their names do not.  The ops: "merge" changes parts of a step ('set':
     *                  {part: value}, 'unset': [parts]) and leaves the rest as the server has it; "set" replaces
     *                  the step whole; "insert" puts 'step' in before the step called 'before' (at the end without
     *                  one), and with 'awaiting' true has it take its answer from what the author does when the game
     *                  comes to it; "delete" takes the step out; "move" takes it out and puts it in again before
     *                  'before'.
     * @param goTo See change.
     */
    public Map<String, Object> edit(Player player, String patchJson, Integer goTo) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            requireAuthor(session.sitting);
            JsonObject patch;
            try {
                patch = org.hjson.JsonValue.readHjson(patchJson).asObject();
            } catch (RuntimeException exp) {
                throw new AuthoringException("That is not a change to a scenario: " + exp.getMessage());
            }
            Set<String> unanswered = new HashSet<>();
            return change(session, goTo, (json, names) -> {
                if (patch.get("top") != null) {
                    for (JsonObject.Member member : patch.get("top").asObject()) {
                        if (member.getName().equals("steps"))
                            throw new AuthoringException("Steps are changed one at a time, not all at once.");
                        if (member.getValue().isNull())
                            json.remove(member.getName());
                        else
                            json.set(member.getName(), member.getValue());
                    }
                }
                if (patch.get("topText") != null) {
                    for (JsonObject.Member member : patch.get("topText").asObject()) {
                        String name = member.getName();
                        if (name.equals("steps"))
                            throw new AuthoringException("Steps are changed one at a time, not all at once.");
                        String text = member.getValue().isString() ? member.getValue().asString() : member.getValue().toString();
                        if (text.isBlank()) {
                            json.remove(name);
                            continue;
                        }
                        org.hjson.JsonValue value;
                        try {
                            value = org.hjson.JsonValue.readHjson(text);
                        } catch (RuntimeException exp) {
                            throw new AuthoringException(name + ": that is not HJSON: " + exp.getMessage());
                        }
                        //(copied out of a file with its name in front, "cards: { ... }": the part is what the name is given)
                        if (value.isObject() && value.asObject().size() == 1 && value.asObject().names().getFirst().equals(name)
                                && text.stripLeading().replaceAll("(?m)^\\s*(#|//).*\\n", "").stripLeading().startsWith(name))
                            value = value.asObject().get(name);
                        json.set(name, value);
                    }
                }
                if (patch.get("steps") == null)
                    return;
                List<Node> nodes = new ArrayList<>();
                int count = 0;
                for (org.hjson.JsonValue step : json.get("steps") == null ? List.<org.hjson.JsonValue>of() : json.get("steps").asArray().values()) {
                    nodes.add(Node.of(step, count < names.uids.size() ? names.uids.get(count) : null, names.under(count)));
                    count++;
                }
                for (org.hjson.JsonValue value : patch.get("steps").asArray()) {
                    JsonObject op = value.asObject();
                    String what = op.getString("op", "");
                    switch (what) {
                        case "set" -> {
                            Spot spot = named(nodes, op);
                            Node was = spot.node();
                            spot.list().set(spot.at(), Node.of(op.get("step"), was.uid, was.under()));
                        }
                        case "merge" -> {
                            Node node = named(nodes, op).node();
                            List<String> under = node.under();
                            if (op.get("set") != null)
                                for (JsonObject.Member member : op.get("set").asObject())
                                    node.json.set(member.getName(), member.getValue());
                            if (op.get("unset") != null)
                                for (org.hjson.JsonValue name : op.get("unset").asArray()) {
                                    node.json.remove(name.asString());
                                    if (name.asString().equals("anyOrder"))
                                        node.children = null;
                                }
                            typed(node.json);
                            if (node.json.get("anyOrder") != null)
                                node.take(under);
                        }
                        case "insert" -> {
                            Spot to = destination(nodes, op, "index");
                            Node node = Node.of(op.get("step"), null, null);
                            if (op.getBoolean("awaiting", false)) {
                                //(it is named now, so that it can be told which step is waiting for its answer)
                                node.uid = "s" + session.uids.incrementAndGet();
                                unanswered.add(node.uid);
                            }
                            to.list().add(to.at(), node);
                        }
                        case "delete" -> {
                            Spot spot = named(nodes, op);
                            spot.list().remove(spot.at());
                        }
                        case "move" -> {
                            Spot from = named(nodes, op);
                            Node node = from.list().remove(from.at());
                            Spot to = destination(nodes, op, "to");
                            if (to.list() != from.list()) {
                                //Into a block, or out of one.  A step of the script applies once; in a block that
                                // has to be said, and out of one it cannot be.
                                if (to.list() == nodes) {
                                    node.json.remove("once");
                                } else if (node.children != null) {
                                    throw new AuthoringException("A block cannot have another block in it.");
                                } else if (node.json.get("until") == null && !node.json.getBoolean("pause", false)) {
                                    node.json.set("once", true);
                                }
                            }
                            to.list().add(to.at(), node);
                        }
                        default -> throw new AuthoringException("'" + what + "' is not something that can be done to a step.");
                    }
                }
                org.hjson.JsonArray changed = new org.hjson.JsonArray();
                names.uids = new ArrayList<>();
                names.kids = new ArrayList<>();
                for (Node node : nodes) {
                    changed.add(node.written());
                    names.uids.add(node.uid);
                    names.kids.add(node.under() == null ? new ArrayList<>() : node.under());
                }
                json.set("steps", changed);
            }, unanswered);
        }
    }

    /**
     * A step while a change is being made to the draft: what the file would say of it, the name it goes by, and (a
     * block) the steps in it, which are kept beside what it says of itself.
     */
    private static final class Node {
        JsonObject json;
        String uid;
        /** The steps of a block; null for a step that is not one. */
        List<Node> children;

        /** @param under The names of the steps in it, as far as they have any; null if they have none. */
        static Node of(org.hjson.JsonValue value, String uid, List<String> under) throws AuthoringException {
            if (value == null || !value.isObject())
                throw new AuthoringException("A step is written as { ... }.");
            Node node = new Node();
            node.uid = uid;
            node.json = new JsonObject(value.asObject());
            typed(node.json);
            node.take(under);
            return node;
        }

        /** Takes the steps in it out of what it says of itself, to be kept beside that. */
        void take(List<String> under) throws AuthoringException {
            org.hjson.JsonValue anyOrder = json.get("anyOrder");
            json.remove("anyOrder");
            if (anyOrder == null) {
                children = null;
                return;
            }
            if (!anyOrder.isArray())
                throw new AuthoringException("anyOrder: has to be a [list] of steps");
            children = new ArrayList<>();
            for (org.hjson.JsonValue child : anyOrder.asArray())
                children.add(of(child, under != null && children.size() < under.size() ? under.get(children.size()) : null, null));
        }

        /** The names of the steps in it; null if it is not a block. */
        List<String> under() {
            if (children == null)
                return null;
            List<String> under = new ArrayList<>();
            for (Node child : children)
                under.add(child.uid);
            return under;
        }

        JsonObject written() {
            JsonObject whole = new JsonObject(json);
            if (children != null) {
                org.hjson.JsonArray anyOrder = new org.hjson.JsonArray();
                for (Node child : children)
                    anyOrder.add(child.written());
                whole.set("anyOrder", anyOrder);
            }
            return whole;
        }
    }

    /** A place among the steps: in the script itself, or in one of its blocks. */
    private record Spot(List<Node> list, int at) {
        Node node() {
            return list.get(at);
        }
    }

    private static Spot find(List<Node> nodes, String uid) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (uid.equals(node.uid))
                return new Spot(nodes, i);
            if (node.children != null)
                for (int k = 0; k < node.children.size(); k++)
                    if (uid.equals(node.children.get(k).uid))
                        return new Spot(node.children, k);
        }
        return null;
    }

    /** The step a change is to be made to: said by its name ('uid'), or, failing that, by where it comes in the script ('index'). */
    private static Spot named(List<Node> nodes, JsonObject op) throws AuthoringException {
        org.hjson.JsonValue name = op.get("uid");
        if (name != null && !name.isNull()) {
            Spot spot = find(nodes, name.asString());
            if (spot == null)
                throw new AuthoringException("That step is no longer in the script: it has changed since this page last heard of it.");
            return spot;
        }
        org.hjson.JsonValue number = op.get("index");
        if (number == null || number.isNull())
            throw new AuthoringException("The change does not say which step it is for.");
        int at = number.asInt();
        if (at < 0 || at >= nodes.size())
            throw new AuthoringException("There is no step " + (at + 1) + ".");
        return new Spot(nodes, at);
    }

    /**
     * Where a step is put: before the step called 'before' (wherever that is: in the script, or in a block); or as
     * the last step of the block called 'into'; or, failing both, where the script's steps are counted to
     * ('numbered', from 0); or at the end of the script.
     */
    private static Spot destination(List<Node> nodes, JsonObject op, String numbered) throws AuthoringException {
        org.hjson.JsonValue before = op.get("before");
        if (before != null && !before.isNull()) {
            Spot spot = find(nodes, before.asString());
            if (spot == null)
                throw new AuthoringException("That step is no longer in the script: it has changed since this page last heard of it.");
            return spot;
        }
        org.hjson.JsonValue into = op.get("into");
        if (into != null && !into.isNull()) {
            Spot block = find(nodes, into.asString());
            if (block == null || block.node().children == null)
                throw new AuthoringException("That block is no longer in the script: it has changed since this page last heard of it.");
            return new Spot(block.node().children, block.node().children.size());
        }
        org.hjson.JsonValue number = op.get(numbered);
        if (number == null || number.isNull())
            return new Spot(nodes, nodes.size());
        int at = number.asInt();
        if (at < 0 || at > nodes.size())
            throw new AuthoringException("There is no step " + (at + 1) + ".");
        return new Spot(nodes, at);
    }

    /**
     * The parts of a step that can be written out in full are typed in as the file has them.  What was typed is
     * text; where it is HJSON (it opens with a bracket, or has the part's name in front, as copied from a file) it
     * is read, and the part is what it says.
     */
    private static void typed(JsonObject step) throws AuthoringException {
        for (String name : TYPED)
            if (step.get(name) != null)
                step.set(name, typed(name, step.get(name)));
        org.hjson.JsonValue allow = step.get("allow");
        if (allow != null && allow.isObject() && allow.asObject().get("passWhen") != null) {
            JsonObject read = new JsonObject(allow.asObject());
            read.set("passWhen", typed("passWhen", read.get("passWhen")));
            step.set("allow", read);
        }
    }

    private static org.hjson.JsonValue typed(String name, org.hjson.JsonValue value) throws AuthoringException {
        if (!value.isString())
            return value;
        String text = value.asString().strip();
        String said = text.replaceAll("(?m)^\\s*(#|//).*\\n?", "").stripLeading();
        boolean named = said.matches("(?s)" + name + "\\s*:.*");
        if (!named && !said.startsWith("{") && !said.startsWith("["))
            return value;
        try {
            org.hjson.JsonValue read = org.hjson.JsonValue.readHjson(named ? "{\n" + text + "\n}" : text);
            return named ? read.asObject().get(name) : read;
        } catch (RuntimeException exp) {
            throw new AuthoringException(name + ": that is not HJSON: " + exp.getMessage());
        }
    }

    /** A fresh game, with the script played up to a step. */
    public Map<String, Object> goTo(Player player, int step) throws AuthoringException {
        return goTo(player, step, 0);
    }

    /**
     * @param sub If the step is a block: which of its steps the script is played up to, counting from 1 (the first
     *            decision that goes to it); 0 for the block's first decision, whichever of its steps takes that.
     */
    public Map<String, Object> goTo(Player player, int step, int sub) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Carried carried = carry(session.sitting, new At(Math.max(1, step), Math.max(0, sub), 0));
            start(session, carried.draft(), true, carried);
            return result(session, false, true);
        }
    }

    /**
     * The author answers for the opponent.
     * @param term The answer in the scenario's words ("play(runner1)", "pass"); null to do as little as the decision
     *             allows.
     * @param record Whether the answer is written into the script.
     */
    public Map<String, Object> answer(Player player, String term, boolean record) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            String problem = sitting.mediator.scenarioLocked(() -> {
                Ruling held = sitting.live.Held();
                if (held == null)
                    return "The opponent is not waiting to be answered for.";
                return null;
            });
            if (problem != null)
                throw new AuthoringException(problem);

            String[] failure = new String[1];
            String answer = sitting.mediator.scenarioLocked(() -> {
                Ruling held = sitting.live.Held();
                if (held == null)
                    return null;
                try {
                    String raw;
                    if (term == null || term.isBlank()) {
                        raw = AnswerCompiler.leastAnswer(held.decision());
                    } else {
                        List<Terms.Term> terms = Terms.parse(term, "answer");
                        if (terms.size() != 1)
                            throw new ScenarioFormatException("answer: one answer at a time");
                        raw = AnswerCompiler.compile(sitting.live.Table(), held.decision(),
                                ScenarioParser.parseAnswer(sitting.def, terms.getFirst(), "answer"));
                    }
                    sitting.unrecorded = record ? null : held.decision();
                    sitting.intended = raw;
                    return raw;
                } catch (ScenarioFormatException | ScenarioScriptException exp) {
                    failure[0] = exp.getMessage();
                    return null;
                }
            });
            if (answer == null)
                throw new AuthoringException(failure[0] != null ? failure[0] : "The opponent is not waiting to be answered for.");
            //(given the way all the opponent's answers are: from outside the game's lock)
            sitting.live.AuthorAnswers(answer);
            session.revision.incrementAndGet();
            return result(session, true, false);
        }
    }

    /**
     * Changes the board.
     * @param opText What to do, as a step's 'do' would say it: "move(aragorn, freeps.hand)", "wound(frodo, 2)".
     * @param inSetup True to change the board the scenario starts with (a fresh game is then brought to where this
     *                one is); false to change the board as it stands, which the step in hand (or the next one to be
     *                written) then does whenever the script is played.
     */
    public Map<String, Object> apply(Player player, String opText, boolean inSetup) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            requireAuthor(sitting);
            DoOp op;
            try {
                List<Terms.Term> terms = Terms.parse(opText, "do");
                if (terms.size() != 1)
                    throw new ScenarioFormatException("do: one change at a time");
                op = ScenarioParser.parseDo(sitting.def, terms.getFirst(), "do");
            } catch (ScenarioFormatException exp) {
                throw new AuthoringException(exp.getMessage());
            }

            if (inSetup) {
                Carried carried = carry(sitting, null);
                putInSetup(carried.draft(), op);
                start(session, carried.draft(), true, carried);
                session.dirty = true;
                return result(session, false, true);
            }

            if (op.kind() == ScenarioDefinition.DoKind.DRAW && sitting.mediator.scenarioLocked(() -> sitting.inHand() == null))
                throw new AuthoringException("Drawing is something a step does: write it in the step's Do.");
            String problem = sitting.mediator.scenarioLocked(() -> {
                if (sitting.over() || !sitting.live.Table().Arranged())
                    return "The game is over: go back to a step first.";
                Step opening = sitting.inHand();
                if (opening != null && opening.when.opening) {
                    //What the opening does, it does once it has been read: the board it is read on is the one laid out
                    reordered(opening.doOps, op);
                    opening.doOps.add(op);
                    session.notice = "Written into the opening's Do: it happens once the opening has been read.";
                    return null;
                }
                int before = sitting.live.Table().Problems().size();
                sitting.live.Table().Apply(op);
                List<String> problems = sitting.live.Table().Problems();
                if (problems.size() > before)
                    return problems.getLast();
                Step inHand = sitting.inHand();
                List<DoOp> ops = inHand != null ? inHand.doOps : sitting.pending;
                reordered(ops, op);
                ops.add(op);
                return null;
            });
            if (problem != null)
                throw new AuthoringException(problem);
            session.changed();
            return result(session, true, false);
        }
    }

    /**
     * Gives one of the scenario's cards another name, and says the new name wherever the scenario said the old one
     * (see AliasRename).  A fresh game of the draft is brought to where this one is.
     */
    public Map<String, Object> renameCard(Player player, String from, String to) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            requireAuthor(session.sitting);
            String name = to == null ? "" : to.trim();
            if (!name.matches("[A-Za-z][A-Za-z0-9_-]*"))
                throw new AuthoringException("A card's name in the scenario is made of letters, digits, dashes and underscores, starting with a letter.");
            return change(session, null, (json, names) -> {
                boolean known = false;
                JsonObject cards = json.get("cards") != null && json.get("cards").isObject() ? json.get("cards").asObject() : new JsonObject();
                for (JsonObject.Member seat : cards) {
                    if (!seat.getValue().isObject())
                        continue;
                    if (seat.getValue().asObject().get(name) != null && !name.equals(from))
                        throw new AuthoringException("The scenario already has a card called '" + name + "'.");
                    known |= from != null && seat.getValue().asObject().get(from) != null;
                }
                if (!known)
                    throw new AuthoringException("The scenario has no card called '" + from + "'.");
                if (!name.equals(from))
                    AliasRename.rename(json, from, name);
            });
        }
    }

    /**
     * Putting a hand in order one card at a time is one ordering, not one for every press: an ordering of the same
     * place that is the last thing a step does gives way to the one that follows it.
     */
    private static void reordered(List<DoOp> ops, DoOp op) {
        if (op.kind() == ScenarioDefinition.DoKind.ORDER && !ops.isEmpty()
                && ops.getLast().kind() == ScenarioDefinition.DoKind.ORDER && ops.getLast().target().equalsIgnoreCase(op.target()))
            ops.removeLast();
    }

    private static void putInSetup(ScenarioDefinition def, DoOp op) throws AuthoringException {
        ScenarioDefinition.Setup setup = def.setup;
        switch (op.kind()) {
            case ORDER -> {
                ScenarioParser.ZoneRef ref;
                try {
                    ref = ScenarioParser.parseZoneRef(def, op.target(), "do");
                } catch (ScenarioFormatException exp) {
                    throw new AuthoringException(exp.getMessage());
                }
                List<String> wanted = new ArrayList<>();
                for (String alias : op.card().split(","))
                    wanted.add(alias.trim());
                Map<SetupZone, List<String>> zones = setup.zones.computeIfAbsent(ref.seat(), seat -> new EnumMap<>(SetupZone.class));
                List<String> listed = zones.getOrDefault(ref.zone(), List.of());
                List<String> ordered = new ArrayList<>();
                if (ref.zone() == SetupZone.DECK) {
                    //The deck the scenario starts with is every card of the seat's that the setup puts nowhere else,
                    // in the order they are declared, under whatever it lists for the deck.  Listing them all for
                    // the deck is what puts them in an order of their own.
                    Set<String> elsewhere = new HashSet<>(setup.attach.keySet());
                    elsewhere.addAll(setup.stack.keySet());
                    elsewhere.addAll(def.ringBearer.values());
                    elsewhere.addAll(def.ring.values());
                    for (Map<SetupZone, List<String>> seatZones : setup.zones.values())
                        for (Map.Entry<SetupZone, List<String>> zone : seatZones.entrySet())
                            if (zone.getKey() != SetupZone.DECK)
                                elsewhere.addAll(zone.getValue());
                    for (String alias : wanted)
                        if (!elsewhere.contains(alias))
                            ordered.add(alias);
                } else {
                    for (String alias : wanted)
                        if (listed.contains(alias))
                            ordered.add(alias);
                }
                if (ordered.isEmpty())
                    throw new AuthoringException("None of those cards is in " + op.target() + " on the board the scenario starts with.");
                for (String alias : listed)
                    if (!ordered.contains(alias))
                        ordered.add(alias);
                zones.put(ref.zone(), ordered);
            }
            case MOVE -> {
                ScenarioParser.ZoneRef ref;
                try {
                    ref = ScenarioParser.parseZoneRef(def, op.target(), "do");
                } catch (ScenarioFormatException exp) {
                    throw new AuthoringException(exp.getMessage());
                }
                takeOutOfSetup(setup, op.card());
                //Whatever the setup does not place stays in its deck, in the order the cards are declared: that is
                // the bottom of the deck, under anything the setup lists for it
                if (ref.zone() != SetupZone.DECK || !ref.bottom()) {
                    List<String> zone = setup.zones.computeIfAbsent(ref.seat(), seat -> new EnumMap<>(SetupZone.class))
                            .computeIfAbsent(ref.zone(), name -> new ArrayList<>());
                    if (ref.zone() == SetupZone.DECK)
                        zone.addFirst(op.card());
                    else
                        zone.add(op.card());
                }
            }
            case ATTACH -> {
                takeOutOfSetup(setup, op.card());
                setup.attach.put(op.card(), op.target());
            }
            case STACK -> {
                takeOutOfSetup(setup, op.card());
                setup.stack.put(op.card(), op.target());
            }
            case WOUND -> setup.wounds.merge(op.card(), op.amount(), Integer::sum);
            case HEAL -> {
                int left = setup.wounds.getOrDefault(op.card(), 0) - op.amount();
                if (left > 0)
                    setup.wounds.put(op.card(), left);
                else
                    setup.wounds.remove(op.card());
            }
            case TOKENS -> setup.tokens.merge(op.card(), op.amount(), Integer::sum);
            case DRAW -> throw new AuthoringException("The starting board has no drawing: put the cards in the hand, or have a step draw them.");
            case BURDENS -> setup.burdens = Math.max(0, op.relative() ? setup.burdens + op.amount() : op.amount());
            case THREATS -> setup.threats = Math.max(0, op.relative() ? setup.threats + op.amount() : op.amount());
            case TWILIGHT -> throw new AuthoringException("The starting board has no twilight pool to set: the first turn starts it at nothing.  "
                    + "Set it on a step instead.");
        }
    }

    private static void takeOutOfSetup(ScenarioDefinition.Setup setup, String alias) {
        for (Map<SetupZone, List<String>> zones : setup.zones.values())
            for (List<String> zone : zones.values())
                zone.remove(alias);
        setup.attach.remove(alias);
        setup.stack.remove(alias);
    }

    /**
     * Writes a step for the decision that is waiting and that the script says nothing about, so that there is
     * something to hang a message on before the decision is answered.  Its answer is filled in when it is given.
     * The step goes in where the script stands: before the step it is waiting on, or, if that is a step that repeats
     * and is under way, after it (and that step's 'until' is made to end at this decision).  A step that was put in
     * there by hand and says nothing yet is used rather than a second one written.
     */
    public Map<String, Object> stepHere(Player player) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            requireAuthor(sitting);
            String problem = sitting.mediator.scenarioLocked(() -> {
                if (sitting.over())
                    return "The game is over: go back to a step first.";
                if (sitting.inHand() != null)
                    return "The decision that is waiting already has a step: step " + sitting.inHand().label() + ".";
                Ruling waiting = null;
                for (String name : List.of(sitting.live.HumanPlayer(), sitting.live.OpponentPlayer())) {
                    Ruling ruling = sitting.live.Session().Ruling(name);
                    if (waiting == null && ruling != null
                            && sitting.live.Table().userFeedback().getAwaitingDecision(name) == ruling.decision())
                        waiting = ruling;
                }
                if (waiting == null)
                    return "Nothing is waiting to be decided.";
                Sitting.Placing placing = sitting.placing(waiting.player(), waiting.decision());
                if (placing.problem() != null)
                    return placing.problem();
                try {
                    ScenarioDirector director = sitting.live.Session().Director();
                    Step current = director.currentStep();
                    if (placing.block() != null) {
                        //One more step in the block the script is at, for this decision and no other
                        Step block = placing.block();
                        JsonObject json = new JsonObject();
                        json.add("when", placing.when());
                        json.add("once", true);
                        Step step = ScenarioParser.parseStep(sitting.def, json, block.number, block, block.children.size() + 1);
                        director.addToBlock(step, false);
                        sitting.awaiting.add(step);
                        sitting.live.Session().Reconsider();
                        if (sitting.inHand() != step) {
                            director.removeFromBlock(step);
                            sitting.awaiting.remove(step);
                            return "A step could not be written for this decision.";
                        }
                        session.movedOn();
                        return null;
                    }
                    //The step the script is waiting on was put in by hand for this, and says nothing but a 'when'
                    // that does not fit: it is made to fit, in place of a second step being written in front of it
                    boolean reused = placing.repeating() == null && current != null && sitting.awaiting.contains(current) && sitting.blank(current);
                    Step step;
                    String was = null;
                    if (reused) {
                        step = current;
                        was = step.when.source;
                        JsonObject json = ScenarioWriter.step(step);
                        json.set("when", placing.when());
                        copyInto(step, sitting.reparsed(step, json));
                        director.restructure(() -> { }, step);
                    } else {
                        JsonObject json = new JsonObject();
                        json.add("when", placing.when());
                        step = ScenarioParser.parseStep(sitting.def, json, 0);
                        if (placing.repeating() != null)
                            director.insertAfterCurrent(step);
                        else
                            director.restructure(() -> sitting.def.steps.add(placing.index(), step), step);
                        sitting.awaiting.add(step);
                    }
                    sitting.live.Session().Reconsider();
                    if (sitting.inHand() != step) {
                        //It ought to fit the very decision it was written for.  If it does not, it is not left behind.
                        if (reused) {
                            JsonObject json = ScenarioWriter.step(step);
                            json.set("when", was);
                            copyInto(step, sitting.reparsed(step, json));
                        } else {
                            director.restructure(() -> sitting.def.steps.remove(step), placing.repeating());
                            sitting.awaiting.remove(step);
                        }
                        return "A step could not be written for this decision.";
                    }
                    session.movedOn();
                    if (placing.repeating() != null) {
                        sitting.giveWay(placing);
                        if (placing.repeating().block)
                            director.doneWith(placing.repeating());
                    }
                    return null;
                } catch (ScenarioFormatException exp) {
                    return exp.getMessage();
                }
            });
            if (problem != null)
                throw new AuthoringException(problem);
            session.changed();
            return result(session, true, false);
        }
    }

    /**
     * The step in hand forgets what it allows and how it is answered, and takes both from what its author does next:
     * for a step that is right in everything but what was done at it.
     */
    public Map<String, Object> retake(Player player) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            requireAuthor(sitting);
            String problem = sitting.mediator.scenarioLocked(() -> {
                Step step = sitting.over() ? null : sitting.inHand();
                if (step == null)
                    return "There is no current step to be answered again.";
                if (step.pause)
                    return "A pause is read, not answered.";
                step.allow = null;
                step.answers = new ArrayList<>();
                sitting.awaiting.add(step);
                session.movedOn();
                sitting.live.Reshow();
                return null;
            });
            if (problem != null)
                throw new AuthoringException(problem);
            session.changed();
            return result(session, true, false);
        }
    }

    public Map<String, Object> settings(Player player, Boolean recording, Boolean askOpponent, List<String> way) throws AuthoringException {
        return settings(player, recording, askOpponent, way, false);
    }

    /**
     * @param seenLeftOut True when the author has seen what was left out of the file to open it, and wants no more
     *                    said of it.
     */
    public Map<String, Object> settings(Player player, Boolean recording, Boolean askOpponent, List<String> way, boolean seenLeftOut) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            if (seenLeftOut) {
                session.leftOut = List.of();
                session.leftOutSteps = List.of();
            }
            if (recording != null)
                session.recording = recording;
            if (askOpponent != null)
                session.askOpponent = askOpponent;
            if (way != null) {
                session.way.clear();
                session.way.addAll(way);
            }
            session.revision.incrementAndGet();
            return result(session, false, false);
        }
    }

    /** The draft as the file it would be saved as. */
    public String source(Player player) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            return sitting.mediator.scenarioLocked(() -> ScenarioWriter.toHjson(sitting.def, _library));
        }
    }

    /**
     * Writes the draft to its file.  The file is kept under the folder of the scenario's kind (see filed): one that is
     * somewhere else, because its kind has been changed or because it was put there by hand, is moved as it is saved.
     * @param path For a scenario that has no file yet: somewhere other than where it was named for when it was
     *             started ("basics/basics-02.hjson").  Null to save it where it is.
     * @param canonical With a path: whether that is among the game's own scenarios.
     */
    public Map<String, Object> save(Player player, String path, boolean canonical, boolean admin) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            ScenarioDefinition.Kind kind = kindOf(session);
            Place from = new Place(session.owner, session.path);
            Place to = new Place(session.owner, filed(session.path, kind));
            if (!session.onDisk && path != null && !path.isBlank())
                to = place(player, filed(path, kind), canonical, admin);
            boolean moves = session.onDisk && !to.equals(from);
            notLocked(session, from.owner(), session.onDisk ? from.path() : null);
            if ((!session.onDisk || moves) && _scenarios.exists(to.owner(), to.path()))
                throw new AuthoringException("There is already a scenario at " + to.shown()
                        + (moves ? ", which is where a " + kind.name().toLowerCase(Locale.ROOT) + " called this goes.  Rename one of them."
                        : ".  Give this one a path of its own (or open that one, if it is the one you mean to change)."));
            if (moves) {
                try {
                    _scenarios.check(to.owner(), to.path());
                    _scenarios.move(from.owner(), from.path(), to.owner(), to.path());
                } catch (IOException exp) {
                    throw new AuthoringException("It could not be moved to " + to.shown() + ": " + exp.getMessage());
                }
                session.owner = to.owner();
                session.path = to.path();
            }
            Map<String, Object> result = write(session, to);
            if (moves)
                ((Map<String, Object>) result.get("saved")).put("movedFrom", from.shown());
            return result;
        }
    }

    /**
     * Saves the draft as another scenario: a file of its own, and (since no two scenarios have the same one) an id of
     * its own, taken from the file's name.  The scenario it was opened from is left as it was last saved, and the
     * draft is the new one from here on.
     */
    public Map<String, Object> saveAs(Player player, String path, boolean canonical, boolean admin) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            requireAuthor(session.sitting);
            Place to = place(player, filed(path, kindOf(session)), canonical, admin);
            if (_scenarios.exists(to.owner(), to.path()))
                throw new AuthoringException("There is already a scenario at " + to.shown() + ".  A copy needs a file of its own.");
            String name = to.path().substring(to.path().lastIndexOf('/') + 1, to.path().length() - ".hjson".length());
            String id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
            if (!id.matches("[a-z0-9][a-z0-9-]{1,60}"))
                throw new AuthoringException("The copy is known by its file's name, which has to have at least two letters or digits in it.");
            if (_scenarios.get(to.owner() == null ? id : to.owner() + "/" + id) != null)
                throw new AuthoringException("There is already a scenario called '" + id + "', and a copy needs a name of its own.  "
                        + "(To move this scenario's file and keep it the same scenario, rename it.)");
            Sitting sitting = session.sitting;
            sitting.mediator.scenarioLocked(() -> sitting.def.id = id);
            return write(session, to);
        }
    }

    /**
     * Moves the draft's file: another name, another folder, or from the author's own folder to the game's and back.
     * It stays the same scenario (its id is not the file's name), and what has not been saved is still not saved.
     */
    public Map<String, Object> rename(Player player, String path, boolean canonical, boolean admin) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Place to = place(player, filed(path, kindOf(session)), canonical, admin);
            Place from = new Place(session.owner, session.path);
            if (!to.equals(from)) {
                notLocked(session, from.owner(), session.onDisk ? from.path() : null);
                if (_scenarios.exists(to.owner(), to.path()))
                    throw new AuthoringException("There is already a scenario at " + to.shown() + ".");
                if (session.onDisk) {
                    try {
                        _scenarios.move(from.owner(), from.path(), to.owner(), to.path());
                    } catch (IOException exp) {
                        throw new AuthoringException("It could not be moved: " + exp.getMessage());
                    }
                }
                session.owner = to.owner();
                session.path = to.path();
                session.revision.incrementAndGet();
            }
            Map<String, Object> result = result(session, false, false);
            result.put("renamed", to.shown());
            return result;
        }
    }

    private Map<String, Object> write(Session session, Place to) throws AuthoringException {
        notLocked(session, to.owner(), to.path());
        Sitting sitting = session.sitting;
        String text = sitting.mediator.scenarioLocked(() -> ScenarioWriter.toHjson(sitting.def, _library));
        ScenarioLibrary.Entry entry;
        try {
            entry = _scenarios.save(to.owner(), to.path(), text, "written over in the authoring tool", session.player.getName(),
                    !session.player.hasType(Player.Type.ADMIN));
        } catch (IOException exp) {
            throw new AuthoringException("It could not be saved: " + exp.getMessage());
        }
        session.owner = to.owner();
        session.path = to.path();
        session.onDisk = true;
        session.dirty = false;
        session.commentsLost = 0;
        session.revision.incrementAndGet();

        Map<String, Object> saved = new LinkedHashMap<>();
        saved.put("path", to.shown());
        saved.put("id", entry.id());
        saved.put("playable", entry.playable());
        saved.put("verified", entry.verified());
        saved.put("errors", entry.errors());
        saved.put("warnings", entry.warnings());
        saved.put("report", entry.report() == null ? null : entry.report().toString());
        Map<String, Object> result = result(session, false, false);
        result.put("saved", saved);
        return result;
    }

    /**
     * @return The pictures a scenario can show that are not cards: [{name, url}], by name.  They are files in the web
     * folder's images/scenarios, put there by whoever runs the server.
     */
    public List<Map<String, Object>> images() {
        List<Map<String, Object>> found = new ArrayList<>();
        java.io.File[] files = _images == null ? null : _images.listFiles();
        if (files == null)
            return found;
        Arrays.sort(files, java.util.Comparator.comparing(file -> file.getName().toLowerCase(Locale.ROOT)));
        for (java.io.File file : files) {
            if (!file.isFile() || !file.getName().matches(ScenarioDefinition.Zoom.IMAGE_NAME))
                continue;
            Map<String, Object> image = new LinkedHashMap<>();
            image.put("name", file.getName());
            image.put("url", CardFaces.IMAGES_URL + file.getName().replace(" ", "%20"));
            found.add(image);
        }
        return found;
    }

    /** Plays the draft through with nobody at the table, the way the library does before it offers a scenario. */
    public Map<String, Object> check(Player player) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            ScenarioDefinition def = sitting.mediator.scenarioLocked(() -> {
                try {
                    return copy(sitting.def, null);
                } catch (AuthoringException exp) {
                    return null;
                }
            });
            if (def == null)
                throw new AuthoringException("The draft cannot be written out and read back.");
            ScenarioRunReport report;
            List<String> warnings;
            try {
                report = ScenarioRunner.run(def, _library, sitting.format);
                warnings = new ScenarioText(def, _library, sitting.format, _cardLookup).check(def);
            } catch (RuntimeException exp) {
                throw new AuthoringException("Playing it through threw " + exp);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("passed", report.passed());
            result.put("report", report.toString());
            result.put("warnings", warnings);
            return result;
        }
    }

    /** A player's game of the draft from a step on: what they will see, with nothing of the author's in the way. */
    public Map<String, Object> test(Player player, int step) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Carried carried = carry(session.sitting, At.step(step));
            start(session, carried.draft(), false, carried);
            return result(session, false, true);
        }
    }

    /**
     * Back to writing: from a player's game of the draft (at the step the test began), or from a game of it that is
     * over or gone (at the step it had got to).  A game for writing that is still going is left as it is.
     */
    public Map<String, Object> resume(Player player) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Sitting sitting = session.sitting;
            if (sitting.author && !sitting.mediator.isFinished() && !sitting.mediator.isDestroyed())
                return result(session, false, false);
            Carried carried = carry(sitting, session.testFrom == null ? null : At.step(session.testFrom));
            start(session, carried.draft(), true, carried);
            return result(session, false, true);
        }
    }

    /** Drops the draft.  Anything not saved is gone. */
    public void close(Player player) {
        Session session = _sessions.remove(player.getName());
        if (session == null)
            return;
        synchronized (session) {
            if (!session.sitting.mediator.isFinished())
                session.sitting.mediator.concede(player);
        }
    }

    private static void requireAuthor(Sitting sitting) throws AuthoringException {
        if (!sitting.author)
            throw new AuthoringException("This is a player's game of the draft.  Go back to writing first.");
    }

    /* ------------------------------------------------------------------ what the author's client is told */

    /**
     * @param events Whether the game in hand has been sent something its client would not think to ask for.
     * @param rebuilt Whether there is a fresh game to go to.
     */
    private Map<String, Object> result(Session session, boolean events, boolean rebuilt) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("gameId", session.sitting.mediator.getGameId());
        result.put("events", events);
        result.put("rebuilt", rebuilt);
        result.put("state", describe(session));
        return result;
    }

    private Map<String, Object> describe(Session session) {
        Sitting sitting = session.sitting;
        return sitting.mediator.scenarioLocked(() -> {
            ScenarioDefinition def = sitting.def;
            LiveScenario live = sitting.live;
            ScenarioTable table = live.Table();
            ScenarioDirector director = live.Session().Director();

            Map<String, Object> state = new LinkedHashMap<>();
            state.put("session", true);
            state.put("gameId", sitting.mediator.getGameId());
            state.put("revision", session.revision.get());
            state.put("mode", sitting.author ? "author" : "test");
            state.put("testFrom", session.testFrom);
            state.put("dirty", session.dirty);
            state.put("path", session.path == null ? null : (session.owner == null ? "" : "users/" + session.owner + "/") + session.path);
            //(the same without whose folder it is in, which is how an author says where a file goes)
            state.put("file", session.path);
            //The folder its kind puts it under, and where saving will have it (which is somewhere else when its kind
            // has been changed since it was last saved, or its file was put where it is by hand)
            state.put("kindFolder", kindFolder(def.kind));
            state.put("fileOnSave", filed(session.path, def.kind));
            //(whether the author may put a scenario among the game's own)
            state.put("mayPublish", session.player.hasType(Player.Type.ADMIN));
            state.put("onDisk", session.onDisk);
            //(whether its file is in the author's own folder: one that is can be submitted for review)
            state.put("own", session.owner != null && session.owner.equals(session.player.getName()));
            state.put("canonical", session.path != null && session.owner == null);
            state.put("canSaveAnywhere", _scenarios.hasUserFolder());
            state.put("commentsLost", session.commentsLost);
            //What was left out of the file to open it: [{where, message, was, step, uid}], 'step' being the step of
            // the file it was in (0 if none) and 'uid' that step's name in the draft (null if the whole step went)
            List<Map<String, Object>> leftOut = new ArrayList<>();
            List<String> names = sitting.uids();
            List<ScenarioSalvage.LeftOut> out = session.leftOut;
            List<String> outSteps = session.leftOutSteps;
            for (int i = 0; i < out.size(); i++) {
                ScenarioSalvage.LeftOut one = out.get(i);
                String uid = i < outSteps.size() ? outSteps.get(i) : null;
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("where", one.where());
                json.put("message", one.message());
                json.put("was", one.was());
                json.put("step", one.stepInFile());
                json.put("uid", uid != null && names.contains(uid) ? uid : null);
                leftOut.add(json);
            }
            state.put("leftOut", leftOut);
            state.put("recording", session.recording);
            state.put("askOpponent", session.askOpponent);
            state.put("way", new ArrayList<>(session.way));
            state.put("flags", new ArrayList<>(def.flags));
            state.put("notice", session.notice);
            //(where the script stopped when it played itself, for as long as that is the last thing that was said)
            boolean stopped = session.stopNotice != null && session.stopNotice.equals(session.notice);
            state.put("stopped", !stopped ? null : session.stoppedUncovered ? "uncovered" : "step");
            state.put("draft", JSON.parseObject(ScenarioWriter.toJson(def).toString()));
            //Each part of the file but its steps, as the file has it: what is copied between the file and the panel
            state.put("parts", ScenarioWriter.parts(def, _library));

            List<String> statuses = new ArrayList<>();
            for (Step step : def.steps)
                statuses.add(director.statuses().getOrDefault(step.label(), ScenarioDirector.StepStatus.UNREACHED).name().toLowerCase(Locale.ROOT));
            state.put("statuses", statuses);
            state.put("uids", sitting.uids());
            //The steps in each block: for each step in order, their names and what became of each (none, for a step
            // that is not a block)
            List<List<String>> substatuses = new ArrayList<>();
            for (Step step : def.steps) {
                List<String> under = new ArrayList<>();
                for (Step child : step.children)
                    under.add(director.statuses().getOrDefault(child.label(), ScenarioDirector.StepStatus.UNREACHED).name().toLowerCase(Locale.ROOT));
                substatuses.add(under);
            }
            state.put("kids", sitting.kids());
            state.put("substatuses", substatuses);
            List<String> unanswered = new ArrayList<>();
            for (Step step : def.allSteps())
                if (sitting.awaiting.contains(step))
                    unanswered.add(sitting.uid(step));
            state.put("awaiting", unanswered);
            //The parts of steps that are written out in full ({ ... }), as the file has them: by the step's name,
            // {part: text}.  They are typed in the same way (see typed).
            Map<String, Object> texts = new LinkedHashMap<>();
            for (Step step : def.allSteps()) {
                JsonObject written = ScenarioWriter.step(step);
                Map<String, String> parts = new LinkedHashMap<>();
                for (String name : TYPED)
                    if (written.get(name) != null && !written.get(name).isString())
                        parts.put(name, ScenarioWriter.text(written.get(name)));
                org.hjson.JsonValue allow = written.get("allow");
                if (allow != null && allow.isObject() && allow.asObject().get("passWhen") != null)
                    parts.put("passWhen", ScenarioWriter.text(allow.asObject().get("passWhen")));
                if (!parts.isEmpty())
                    texts.put(sitting.uid(step), parts);
            }
            state.put("texts", texts);

            Step inHand = sitting.inHand();
            Map<String, Object> position = new LinkedHashMap<>();
            position.put("step", sitting.position());
            //(in a block: which of its steps is in hand, counting from 1; 0 if none is, or it is no block)
            position.put("sub", inHand != null ? inHand.sub : 0);
            position.put("inHand", inHand != null);
            //(a step that repeats, has applied at least once, and has not ended: the game is at it, between its decisions)
            position.put("underWay", inHand == null && director.currentStep() != null && director.timesApplied() > 0);
            position.put("frontier", director.atFrontier() && inHand == null);
            position.put("over", sitting.over());
            position.put("result", live.Result() == null ? null : live.Result().name().toLowerCase(Locale.ROOT));
            position.put("turn", table.Arranged() ? table.gameState().getTurnNumber() : 0);
            position.put("phase", table.Arranged() ? table.GetCurrentPhase().name().toLowerCase(Locale.ROOT) : null);
            position.put("chosen", new ArrayList<>(director.flags()));
            state.put("position", position);

            Map<String, Object> waiting = new LinkedHashMap<>();
            for (String player : List.of(live.HumanPlayer(), live.OpponentPlayer())) {
                boolean human = player.equals(live.HumanPlayer());
                AwaitingDecision decision = sitting.over() ? null : table.userFeedback().getAwaitingDecision(player);
                Ruling ruling = live.Session().Ruling(player);
                Map<String, Object> described = null;
                if (decision != null && table.Arranged()) {
                    described = describeDecision(sitting, player, decision);
                    described.put("step", ruling != null && ruling.decision() == decision && ruling.step() != null ? ruling.step().number : null);
                    described.put("sub", ruling != null && ruling.decision() == decision && ruling.step() != null ? ruling.step().sub : 0);
                    described.put("held", !human && live.Held() != null && live.Held().decision() == decision);
                }
                waiting.put(human ? "human" : "opponent", described);
            }
            state.put("waiting", waiting);

            //A decision that is waiting and that no step covers, and what adding a step for it would do
            Map<String, Object> uncovered = null;
            Ruling open = sitting.author && table.Arranged() ? sitting.uncovered() : null;
            if (open != null) {
                Sitting.Placing placing = sitting.placing(open.player(), open.decision());
                uncovered = new LinkedHashMap<>();
                uncovered.put("seat", open.player().equals(live.HumanPlayer()) ? "human" : "opponent");
                uncovered.put("after", placing.index());
                uncovered.put("when", placing.when());
                uncovered.put("repeating", placing.repeating() == null ? null : placing.repeating().number);
                uncovered.put("until", placing.repeating() == null ? null : placing.repeating().until.source);
                uncovered.put("becomes", placing.until());
                uncovered.put("problem", placing.problem());
                //(a step for it would go into the block the script is at: which block, and where in it)
                uncovered.put("block", placing.block() == null ? null : placing.block().number);
                uncovered.put("sub", placing.block() == null ? 0 : placing.block().children.size() + 1);
                Step current = director.currentStep();
                uncovered.put("reuses", placing.repeating() == null && current != null && sitting.awaiting.contains(current) && sitting.blank(current)
                        ? current.number : null);
            }
            state.put("uncovered", uncovered);

            List<String> pending = new ArrayList<>();
            sitting.pending.forEach(op -> pending.add(op.source()));
            state.put("pending", pending);
            state.put("board", table.Arranged() ? describeBoard(sitting) : null);
            state.put("problems", live.Problems());
            return state;
        });
    }

    private Map<String, Object> describeBoard(Sitting sitting) {
        ScenarioTable table = sitting.live.Table();
        //Where each card comes in its hand or its pile (0 first: the top of a pile)
        Map<PhysicalCard, Integer> places = new IdentityHashMap<>();
        for (Seat seat : Seat.values())
            for (SetupZone zone : ScenarioParser.ORDERED) {
                List<? extends PhysicalCard> there = table.CardsIn(seat, zone);
                for (int i = 0; i < there.size(); i++)
                    places.put(there.get(i), i);
            }
        List<Map<String, Object>> cards = new ArrayList<>();
        for (CardDef card : sitting.def.cards.values()) {
            PhysicalCardImpl physical = table.Card(card.alias());
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("alias", card.alias());
            json.put("blueprintId", physical.getBlueprintId());
            json.put("name", GameUtils.getFullName(physical));
            json.put("type", physical.getBlueprint().getCardType().name());
            json.put("owner", card.owner().key());
            json.put("cardId", physical.getCardId());
            json.put("zone", zoneOf(physical));
            json.put("at", places.get(physical));
            json.put("wounds", table.gameState().getWounds(physical));
            Token token = physical.getBlueprint().getCulture() == null ? null : Token.findTokenForCulture(physical.getBlueprint().getCulture());
            json.put("tokens", token == null ? 0 : table.gameState().getTokenCount(physical, token));
            json.put("on", physical.getAttachedTo() != null ? table.AliasOf(physical.getAttachedTo())
                    : physical.getStackedOn() != null ? table.AliasOf(physical.getStackedOn()) : null);
            cards.add(json);
        }
        Map<String, Object> board = new LinkedHashMap<>();
        board.put("cards", cards);
        board.put("twilight", table.GetTwilight());
        board.put("burdens", table.GetBurdens());
        board.put("threats", table.GetThreats());
        return board;
    }

    private static String zoneOf(PhysicalCard card) {
        if (card.getZone() == null)
            return "nowhere";
        return switch (card.getZone()) {
            case FREE_CHARACTERS -> "characters";
            case SUPPORT -> "support";
            case SHADOW_CHARACTERS -> "minions";
            case HAND -> "hand";
            case DECK -> "deck";
            case DISCARD -> "discard";
            case DEAD -> "deadPile";
            case REMOVED -> "removed";
            case ATTACHED -> "attached";
            case STACKED -> "stacked";
            default -> card.getZone().name().toLowerCase(Locale.ROOT);
        };
    }

    /** A decision, in enough detail for the author to answer it from a panel: what it offers, in the scenario's words. */
    private Map<String, Object> describeDecision(Sitting sitting, String player, AwaitingDecision decision) {
        ScenarioTable table = sitting.live.Table();
        Map<String, String[]> params = decision.getDecisionParameters();
        AwaitingDecisionType type = decision.getDecisionType();
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("seat", table.SeatOf(player).key());
        json.put("type", AnswerCompiler.typeName(type));
        json.put("text", decision.getText().replaceAll("<[^>]*>", ""));
        json.put("where", "turn " + table.gameState().getTurnNumber() + ", " + table.GetCurrentPhase().name().toLowerCase(Locale.ROOT));
        if (params.containsKey("min") && params.get("min").length > 0)
            json.put("min", Integer.parseInt(params.get("min")[0]));
        if (params.containsKey("max") && params.get("max").length > 0)
            json.put("max", Integer.parseInt(params.get("max")[0]));

        List<Map<String, Object>> options = new ArrayList<>();
        switch (type) {
            case CARD_ACTION_CHOICE -> {
                String[] actionIds = params.get("actionId");
                String[] cardIds = params.get("cardId");
                String[] texts = params.get("actionText");
                for (int i = 0; i < actionIds.length; i++)
                    options.add(option(sitting.term(decision, actionIds[i]), cardName(table, cardIds[i]) + ": " + texts[i].replaceAll("<[^>]*>", "").trim(),
                            table.AliasOfCardId(cardIds[i]), cardIds[i]));
                json.put("pass", true);
            }
            case ACTION_CHOICE -> {
                String[] actionIds = params.get("actionId");
                String[] texts = params.get("actionText");
                for (int i = 0; i < actionIds.length; i++)
                    options.add(option("raw(" + actionIds[i] + ")", texts[i].replaceAll("<[^>]*>", "").trim(), null, null));
            }
            case CARD_SELECTION -> {
                for (String cardId : params.get("cardId"))
                    options.add(option(null, cardName(table, cardId), table.AliasOfCardId(cardId), cardId));
            }
            case ARBITRARY_CARDS -> {
                String[] tempIds = params.get("cardId");
                String[] blueprintIds = params.get("blueprintId");
                String[] selectable = params.get("selectable");
                List<? extends PhysicalCard> physical = decision instanceof ArbitraryCardsSelectionDecision arbitrary
                        && arbitrary.getPhysicalCards() != null ? new ArrayList<>(arbitrary.getPhysicalCards()) : null;
                for (int i = 0; i < tempIds.length; i++) {
                    if (!selectable[i].equals("true"))
                        continue;
                    String alias = physical != null ? table.AliasOf(physical.get(i)) : null;
                    options.add(option(null, physical != null ? GameUtils.getFullName(physical.get(i)) : blueprintName(blueprintIds[i]), alias, tempIds[i]));
                }
            }
            case MULTIPLE_CHOICE -> {
                String[] results = params.get("results");
                for (int i = 0; i < results.length; i++)
                    options.add(option(sitting.term(decision, String.valueOf(i)), results[i], null, null));
            }
            case INTEGER -> { }
            case ASSIGN_MINIONS -> {
                List<Map<String, Object>> characters = new ArrayList<>();
                for (String cardId : params.get("freeCharacters"))
                    characters.add(option(null, cardName(table, cardId), table.AliasOfCardId(cardId), cardId));
                json.put("characters", characters);
                for (String cardId : params.get("minions"))
                    options.add(option(null, cardName(table, cardId), table.AliasOfCardId(cardId), cardId));
                json.put("pass", true);
            }
        }
        json.put("options", options);
        return json;
    }

    private static Map<String, Object> option(String term, String label, String alias, String id) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("term", term);
        option.put("label", label);
        option.put("alias", alias);
        option.put("id", id);
        return option;
    }

    private static String cardName(ScenarioTable table, String cardId) {
        try {
            PhysicalCard card = table.gameState().findCardById(Integer.parseInt(cardId));
            if (card != null)
                return GameUtils.getFullName(card);
        } catch (NumberFormatException exp) {
            //Not a card on the table, then
        }
        return "card " + cardId;
    }

    private String blueprintName(String blueprintId) {
        try {
            return GameUtils.getFullName(_library.getLotroCardBlueprint(blueprintId));
        } catch (CardNotFoundException | RuntimeException exp) {
            return blueprintId;
        }
    }

    /**
     * Finds cards by name, for an author who knows what they want and not what its number is.
     * @param query Words that all have to be in the card's name, or a blueprint id.
     * @param sites Whether sites are wanted (true) or everything but sites (false).
     */
    public List<Map<String, Object>> findCards(String query, boolean sites) {
        List<Map<String, Object>> found = new ArrayList<>();
        if (query == null || query.trim().length() < 2)
            return found;
        String[] words = query.trim().toLowerCase(Locale.ROOT).split("\\s+");
        List<Map.Entry<String, LotroCardBlueprint>> matches = new ArrayList<>();
        for (Map.Entry<String, LotroCardBlueprint> card : _library.getBaseCards().entrySet()) {
            LotroCardBlueprint blueprint = card.getValue();
            if (blueprint == null || blueprint.getTitle() == null || (blueprint.getCardType() == CardType.SITE) != sites)
                continue;
            String name = GameUtils.getFullName(blueprint).toLowerCase(Locale.ROOT);
            boolean all = true;
            for (String word : words)
                if (!name.contains(word) && !card.getKey().equalsIgnoreCase(word))
                    all = false;
            if (all)
                matches.add(card);
        }
        matches.sort(Map.Entry.comparingByValue((one, other) -> GameUtils.getFullName(one).compareToIgnoreCase(GameUtils.getFullName(other))));
        for (Map.Entry<String, LotroCardBlueprint> card : matches) {
            if (found.size() >= SEARCH_LIMIT)
                break;
            LotroCardBlueprint blueprint = card.getValue();
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", card.getKey());
            json.put("name", GameUtils.getFullName(blueprint));
            json.put("type", blueprint.getCardType() == null ? null : blueprint.getCardType().name().toLowerCase(Locale.ROOT));
            json.put("culture", blueprint.getCulture() == null ? null : blueprint.getCulture().name().toLowerCase(Locale.ROOT));
            json.put("side", blueprint.getSide() == null ? null : blueprint.getSide().name().toLowerCase(Locale.ROOT));
            json.put("cost", blueprint.getTwilightCost());
            if (blueprint.getCardType() == CardType.SITE)
                json.put("site", blueprint.getSiteNumber());
            found.add(json);
        }
        return found;
    }
}
