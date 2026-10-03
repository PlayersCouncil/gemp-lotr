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

        /**
         * What a fresh game of the draft takes over from this one.
         * @param uids The names of the steps of the draft the fresh game is of, in order (null where a step is new).
         */
        Carried carry(List<String> uids, int position) throws AuthoringException {
            Set<String> unanswered = new HashSet<>();
            for (Step step : awaiting)
                unanswered.add(uid(step));
            List<String> dropped = new ArrayList<>();
            pending.forEach(op -> dropped.add(op.source()));
            return new Carried(null, new ArrayList<>(uids), unanswered, position, dropped);
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
                        copyInto(step, ScenarioParser.parseStep(def, json, step.number));
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
                json.add(human ? "allow" : "answer", term(ruling.decision(), answer));
                Step written = ScenarioParser.parseStep(def, json, 0);
                written.doOps.addAll(pending);
                pending.clear();
                if (placing.repeating() != null) {
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

        /** What a decision's step waits for, as tightly as can be said without knowing what the decision will offer. */
        String whenFor(String player, AwaitingDecision decision) {
            ScenarioTable table = live.Table();
            StringBuilder when = new StringBuilder();
            when.append(table.SeatOf(player).key()).append(", ").append(AnswerCompiler.typeName(decision.getDecisionType()));
            Phase phase = table.GetCurrentPhase();
            String name = phase.name().toLowerCase(Locale.ROOT);
            if (Phase.findPhase(name) == phase)
                when.append(", phase(").append(name).append(")");
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
         */
        record Placing(int index, Step repeating, String until, String when, String problem) { }

        Placing placing(String player, AwaitingDecision decision) {
            ScenarioDirector director = live.Session().Director();
            String when = whenFor(player, decision);
            Step current = director.currentStep();
            if (current == null || director.timesApplied() == 0)
                return new Placing(position() - 1, null, null, when, null);
            try {
                return new Placing(current.number, current, givingWay(current, player, decision), when, null);
            } catch (AuthoringException exp) {
                return new Placing(current.number, current, null, when, exp.getMessage());
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
            String cannot = repeating.describe() + " repeats and is under way, and ";
            if (until.require != null || until.source.startsWith("{"))
                throw new AuthoringException(cannot + "its 'until' is written out with requirements.  Change it by hand so that it fits this decision; "
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
                throw new AuthoringException(cannot + "nothing simple tells this decision from the ones it applies to.  Change its 'until' by hand "
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
            copyInto(repeating, ScenarioParser.parseStep(def, json, repeating.number));
            session.notice = "Step " + repeating.label() + " repeats, and was still under way.  Its 'until' was \"" + was + "\" and is now \""
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
        boolean follow(ScenarioDefinition fresh, List<String> uids) {
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
            for (int i = 0; i < kept; i++)
                if (!without(ScenarioWriter.step(def.steps.get(i)), SHOWN).equals(without(ScenarioWriter.step(fresh.steps.get(i)), SHOWN)))
                    return false;
            //A decision that is already in front of somebody cannot be taken back to be read over
            boolean somethingWaiting = live.Session().Ruling(live.HumanPlayer()) != null || live.Session().Ruling(live.OpponentPlayer()) != null;
            if (inHand == null && somethingWaiting) {
                //(the first step still to come, or any that may be passed over on the way to it)
                for (int i = kept; i < fresh.steps.size(); i++) {
                    Step next = fresh.steps.get(i);
                    if (next.pause)
                        return false;
                    if (next.onlyIf.isEmpty() && next.unless.isEmpty())
                        break;
                }
            }

            Set<String> unanswered = new HashSet<>();
            for (Step step : awaiting)
                unanswered.add(uid(step));
            director.restructure(() -> {
                for (int i = 0; i < kept; i++)
                    copyInto(def.steps.get(i), fresh.steps.get(i));
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
            awaiting.retainAll(def.steps.subList(0, kept));
            for (int i = kept; i < def.steps.size(); i++) {
                if (i >= uids.size() || uids.get(i) == null)
                    continue;
                Step step = def.steps.get(i);
                names.put(step, uids.get(i));
                //(the steps after the ones in use are new copies of themselves)
                if (unanswered.contains(uids.get(i)) && !step.pause && step.allow == null && step.answers.isEmpty())
                    awaiting.add(step);
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

    private static void copyInto(Step target, Step source) {
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
     * What a fresh game takes over from the one before it.
     * @param draft The draft the fresh game is of; null if whoever asked has it already.
     * @param uids The names of the draft's steps, in order; null where a step is new.
     * @param awaiting The names of the steps that are waiting to be given their answer.
     * @param position The step to play the script up to.
     * @param dropped Changes to the board that no step carried yet: the fresh game does not have them.
     */
    private record Carried(ScenarioDefinition draft, List<String> uids, Set<String> awaiting, int position, List<String> dropped) { }

    /** A copy of the draft as the game in hand has it, and what goes with it, taken while that game is held still. */
    private Carried carry(Sitting sitting, Integer position) throws AuthoringException {
        AuthoringException[] failure = new AuthoringException[1];
        Carried carried = sitting.mediator.scenarioLocked(() -> {
            try {
                Carried rest = sitting.carry(sitting.uids(), position != null ? position : sitting.position());
                return new Carried(copy(sitting.def, null), rest.uids(), rest.awaiting(), rest.position(), rest.dropped());
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
            throw new AuthoringException("You have no scenario open for writing.  Open one from the Game Hall.");
        session.left = false;
        return session;
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

        int runTo = carried.position();
        Sitting sitting = new Sitting(session, def, format, author);
        if (carried.uids() != null) {
            for (int i = 0; i < def.steps.size() && i < carried.uids().size(); i++) {
                Step step = def.steps.get(i);
                String uid = carried.uids().get(i);
                if (uid == null)
                    continue;
                sitting.names.put(step, uid);
                if (carried.awaiting().contains(uid) && !step.pause && step.allow == null && step.answers.isEmpty())
                    sitting.awaiting.add(step);
            }
        }
        try {
            LiveScenario live = new LiveScenario(def, _library, format, session.player.getName(),
                    new ScenarioText(def, _library, format, _cardLookup), _timer);
            if (author)
                live.SetAuthor(sitting);
            else
                live.SetDraftTest();
            live.RunTo(runTo, session::choice);
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
        return new Carried(null, null, Set.of(), 1, List.of());
    }

    /* ------------------------------------------------------------------ what an author can ask for */

    /**
     * Opens one of the library's scenarios for writing.  Whatever draft the author had open is dropped.
     */
    public Map<String, Object> open(Player player, String id, boolean admin) throws AuthoringException {
        ScenarioLibrary.Entry entry = _scenarios.get(id);
        if (entry == null || entry.def() == null)
            throw new AuthoringException("There is no scenario called '" + id + "' that can be read.");
        if (!admin && (entry.owner() == null || !entry.owner().equals(player.getName())))
            throw new AuthoringException("That scenario is not yours to change.");
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
        boolean ownFolder = _scenarios.hasUserFolder() && !(canonical && admin);
        Place place = place(player, (ownFolder ? "" : "tutorials/") + id + ".hjson", canonical, admin);
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

    /** Changes the draft as its file would have it, and the names of its steps alongside (one for each step, in order). */
    private interface Patcher {
        void patch(JsonObject json, List<String> uids) throws AuthoringException;
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
                List<String> uids = sitting.uids();
                patcher.patch(json, uids);
                ScenarioDefinition fresh = ScenarioParser.parse(json.toString());
                if (_formats.apply(fresh.format) == null)
                    return "format: there is no format called '" + fresh.format + "'";
                List<String> errors = ScenarioValidator.validate(fresh, _library);
                if (!errors.isEmpty())
                    return String.join("\n", errors);
                if (goTo == null && sitting.follow(fresh, uids)) {
                    for (Step step : sitting.def.steps)
                        if (unanswered.contains(sitting.names.get(step)) && !step.pause && step.allow == null && step.answers.isEmpty())
                            sitting.awaiting.add(step);
                    return null;
                }
                out[0] = fresh;
                Carried carried = sitting.carry(uids, goTo != null ? goTo : sitting.position());
                Set<String> waiting = new HashSet<>(carried.awaiting());
                waiting.addAll(unanswered);
                out[1] = new Carried(carried.draft(), carried.uids(), waiting, carried.position(), carried.dropped());
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
            return change(session, goTo, (draft, uids) -> {
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
            return change(session, goTo, (json, uids) -> {
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
                List<org.hjson.JsonValue> steps = new ArrayList<>(json.get("steps") == null ? List.of() : json.get("steps").asArray().values());
                for (org.hjson.JsonValue value : patch.get("steps").asArray()) {
                    JsonObject op = value.asObject();
                    String what = op.getString("op", "");
                    switch (what) {
                        case "set" -> steps.set(place(op, "uid", "index", uids, false), op.get("step"));
                        case "merge" -> {
                            int at = place(op, "uid", "index", uids, false);
                            JsonObject step = new JsonObject(steps.get(at).asObject());
                            if (op.get("set") != null)
                                for (JsonObject.Member member : op.get("set").asObject())
                                    step.set(member.getName(), member.getValue());
                            if (op.get("unset") != null)
                                for (org.hjson.JsonValue name : op.get("unset").asArray())
                                    step.remove(name.asString());
                            steps.set(at, step);
                        }
                        case "insert" -> {
                            int at = place(op, "before", "index", uids, true);
                            steps.add(at, op.get("step"));
                            if (op.getBoolean("awaiting", false)) {
                                //(it is named now, so that it can be told which step is waiting for its answer)
                                String uid = "s" + session.uids.incrementAndGet();
                                uids.add(at, uid);
                                unanswered.add(uid);
                            } else {
                                uids.add(at, null);
                            }
                        }
                        case "delete" -> {
                            int at = place(op, "uid", "index", uids, false);
                            steps.remove(at);
                            uids.remove(at);
                        }
                        case "move" -> {
                            int from = place(op, "uid", "index", uids, false);
                            org.hjson.JsonValue step = steps.remove(from);
                            String uid = uids.remove(from);
                            int to = place(op, "before", "to", uids, true);
                            steps.add(to, step);
                            uids.add(to, uid);
                        }
                        default -> throw new AuthoringException("'" + what + "' is not something that can be done to a step.");
                    }
                }
                org.hjson.JsonArray changed = new org.hjson.JsonArray();
                steps.forEach(changed::add);
                json.set("steps", changed);
            }, unanswered);
        }
    }

    /**
     * Where in the script a change is to be made.
     * @param named The part of the change that says it by a step's name.
     * @param numbered The part that says it by where the step comes, for a change that names no step.
     * @param orEnd Whether saying neither means the end of the script (true), or is a mistake.
     */
    private static int place(JsonObject op, String named, String numbered, List<String> uids, boolean orEnd) throws AuthoringException {
        org.hjson.JsonValue name = op.get(named);
        if (name != null && !name.isNull()) {
            int at = uids.indexOf(name.asString());
            if (at < 0)
                throw new AuthoringException("That step is no longer in the script: it has changed since this page last heard of it.");
            return at;
        }
        org.hjson.JsonValue number = op.get(numbered);
        if (number == null || number.isNull()) {
            if (orEnd)
                return uids.size();
            throw new AuthoringException("The change does not say which step it is for.");
        }
        int at = number.asInt();
        if (at < 0 || at > uids.size() || (!orEnd && at == uids.size()))
            throw new AuthoringException("There is no step " + (at + 1) + ".");
        return at;
    }

    /** A fresh game, with the script played up to a step. */
    public Map<String, Object> goTo(Player player, int step) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Carried carried = carry(session.sitting, Math.max(1, step));
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
                if (inHand != null)
                    inHand.doOps.add(op);
                else
                    sitting.pending.add(op);
                return null;
            });
            if (problem != null)
                throw new AuthoringException(problem);
            session.changed();
            return result(session, true, false);
        }
    }

    private static void putInSetup(ScenarioDefinition def, DoOp op) throws AuthoringException {
        ScenarioDefinition.Setup setup = def.setup;
        switch (op.kind()) {
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
                        copyInto(step, ScenarioParser.parseStep(sitting.def, json, step.number));
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
                            copyInto(step, ScenarioParser.parseStep(sitting.def, json, step.number));
                        } else {
                            director.restructure(() -> sitting.def.steps.remove(step), placing.repeating());
                            sitting.awaiting.remove(step);
                        }
                        return "A step could not be written for this decision.";
                    }
                    session.movedOn();
                    if (placing.repeating() != null)
                        sitting.giveWay(placing);
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
                    return "No step is in hand to be answered again.";
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
        Session session = session(player);
        synchronized (session) {
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
     * Writes the draft to its file.
     * @param path For a scenario that has no file yet: somewhere other than where it was named for when it was
     *             started ("tutorials/basics-02.hjson").  Null to save it where it is.
     * @param canonical With a path: whether that is among the game's own scenarios.
     */
    public Map<String, Object> save(Player player, String path, boolean canonical, boolean admin) throws AuthoringException {
        Session session = session(player);
        synchronized (session) {
            Place to = new Place(session.owner, session.path);
            if (!session.onDisk && path != null && !path.isBlank())
                to = place(player, path, canonical, admin);
            if (!session.onDisk && _scenarios.exists(to.owner(), to.path()))
                throw new AuthoringException("There is already a scenario at " + to.shown()
                        + ".  Give this one a path of its own (or open that one, if it is the one you mean to change).");
            return write(session, to);
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
            Place to = place(player, path, canonical, admin);
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
            Place to = place(player, path, canonical, admin);
            Place from = new Place(session.owner, session.path);
            if (!to.equals(from)) {
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
        Sitting sitting = session.sitting;
        String text = sitting.mediator.scenarioLocked(() -> ScenarioWriter.toHjson(sitting.def, _library));
        ScenarioLibrary.Entry entry;
        try {
            entry = _scenarios.save(to.owner(), to.path(), text);
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
            Carried carried = carry(session.sitting, Math.max(1, step));
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
            Carried carried = carry(sitting, session.testFrom);
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
            state.put("onDisk", session.onDisk);
            state.put("canonical", session.path != null && session.owner == null);
            state.put("canSaveAnywhere", _scenarios.hasUserFolder());
            state.put("commentsLost", session.commentsLost);
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
            List<String> unanswered = new ArrayList<>();
            for (Step step : def.steps)
                if (sitting.awaiting.contains(step))
                    unanswered.add(sitting.uid(step));
            state.put("awaiting", unanswered);

            Step inHand = sitting.inHand();
            Map<String, Object> position = new LinkedHashMap<>();
            position.put("step", sitting.position());
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
