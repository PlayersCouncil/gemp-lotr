package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything a scenario file says, after parsing.  This is plain data: nothing here knows how to run a game.
 * See ScenarioParser for the file format and ScenarioTable / ScenarioDirector for what is done with it.
 */
public final class ScenarioDefinition {
    public enum Kind { TUTORIAL, PUZZLE, SANDBOX }
    public enum Coverage { STRICT, GUIDED }
    public enum Fallback { FAIL, PASS, BOT }
    public enum RngDefault { IDENTITY, RANDOM }
    public enum RunnerLevel { SETUP, PREFIX, SOLUTION, FULL }

    public String id;
    public int version = 1;
    public Kind kind = Kind.TUTORIAL;
    public String title;
    public String summary;
    public String author;
    public List<String> tags = new ArrayList<>();
    /** Supplies the rules flags and errata mapping.  Deck legality is not checked. */
    public String format;
    /** Which seat the person playing the scenario sits in; the scripted opponent takes the other. */
    public Seat human = Seat.FREEPS;
    /** What the opponent is called on screen.  Null means "named after the kind of scenario". */
    public String opponentName;
    public Fallback fallback = Fallback.FAIL;
    public Coverage coverage = Coverage.STRICT;

    /**
     * The phases in which the person playing is not asked to pass when they have nothing they could play.  (The
     * scripted opponent is never asked: it only gets a choice of actions when it has one to make.)
     */
    public Set<Phase> autoPass = EnumSet.noneOf(Phase.class);
    public boolean autoAccept = false;
    public RngDefault rngDefault = RngDefault.IDENTITY;
    public int opponentDelayMs = 900;
    /** How much of the scenario the headless runner is required to prove.  Null means "as much as its coverage allows". */
    public RunnerLevel runnerLevel;

    /** Every named card, in the order the file declares them (which is also the order they sit in their deck). */
    public final Map<String, CardDef> cards = new LinkedHashMap<>();
    public final Map<Seat, String> ringBearer = new EnumMap<>(Seat.class);
    public final Map<Seat, String> ring = new EnumMap<>(Seat.class);
    /** Site blueprints for each seat's adventure deck, site 1 first. */
    public final Map<Seat, List<String>> sites = new EnumMap<>(Seat.class);

    /** Card-definition "effects" for each seat's rules card, as JSON array text.  Absent means no rules. */
    public final Map<Seat, String> rules = new EnumMap<>(Seat.class);
    /** The Free Peoples seat takes every turn; the Shadow seat never gets a turn of its own. */
    public boolean oneSided = false;

    public final Setup setup = new Setup();
    public final List<RngEntry> rng = new ArrayList<>();
    public final List<Step> steps = new ArrayList<>();
    public final Outcome outcome = new Outcome();

    public record CardDef(String alias, String blueprintId, Seat owner) { }

    public String opponentDisplayName() {
        if (opponentName != null)
            return opponentName;
        return switch (kind) {
            case TUTORIAL -> "Tutorial";
            case PUZZLE -> "Puzzle";
            case SANDBOX -> "Sandbox";
        };
    }

    /** Where a card can be put when the board is first arranged. */
    public enum SetupZone { CHARACTERS, SUPPORT, MINIONS, HAND, DECK, DISCARD, DEAD_PILE, REMOVED }

    public static final class Setup {
        public int turn = 1;
        public Phase phase = Phase.FELLOWSHIP;
        public final Map<Seat, Map<SetupZone, List<String>>> zones = new EnumMap<>(Seat.class);
        /** Card alias -> the alias of the card that bears it. */
        public final Map<String, String> attach = new LinkedHashMap<>();
        /** Card alias -> the alias of the card it is stacked on. */
        public final Map<String, String> stack = new LinkedHashMap<>();
        public final Map<String, Integer> wounds = new LinkedHashMap<>();
        /** Culture tokens of the card's own culture. */
        public final Map<String, Integer> tokens = new LinkedHashMap<>();
        public int burdens = 0;
        public int threats = 0;
    }

    public enum RngKind { SHUFFLE, PICK }

    /**
     * One decided random outcome.
     * @param zone "deck", "hand" or "adventureDeck"
     * @param result For a shuffle, the cards that end up on top, topmost first.  For a pick, the cards picked.
     */
    public record RngEntry(RngKind kind, Seat seat, String zone, List<String> result) { }

    /**
     * What a decision has to look like for a step to apply to it.  Every field is optional; all given fields must hold.
     */
    public static final class When {
        public Seat seat;
        public AwaitingDecisionType decision;
        /** Case-insensitive fragment of the decision's text. */
        public String text;
        public Phase phase;
        public Integer turn;
        /** Which skirmish of the turn is being fought, counting from 1. */
        public Integer skirmish;
        /** A card that has to be among the decision's options. */
        public String offers;
        /** Card-definition requirements, as JSON array text. */
        public String require;
        /** How it was written, for reports. */
        public String source = "";

        public boolean isEmpty() {
            return seat == null && decision == null && text == null && phase == null && turn == null
                    && skirmish == null && offers == null && require == null;
        }
    }

    public enum AnswerKind { PASS, PLAY, USE, CHOOSE, OPTION, NUMBER, ASSIGN, RAW }

    /**
     * An answer to a decision, written with card aliases rather than whatever IDs the cards have at the time.
     * @param cards PLAY and USE: the one card.  CHOOSE: the chosen cards.
     * @param text USE: a fragment of the action's text, when the card offers more than one.  OPTION: the option's
     *             text, or "#n" for the n-th option counting from 1.  RAW: the literal answer.
     * @param assignments ASSIGN: Free Peoples character -> the minions assigned to it.
     */
    public record Answer(AnswerKind kind, List<String> cards, String text, int number,
                         Map<String, List<String>> assignments, String source) { }

    public enum DoKind { MOVE, ATTACH, STACK, WOUND, HEAL, TWILIGHT, BURDENS, THREATS, TOKENS }

    /**
     * Something the director does to the board directly.
     * @param card The card acted on (not used by TWILIGHT, BURDENS, THREATS).
     * @param target MOVE: where to, e.g. "freeps.hand", "shadow.deck.top".  ATTACH, STACK: the other card.
     * @param amount How many.  For TWILIGHT, BURDENS and THREATS this is a new total unless relative is set.
     */
    public record DoOp(DoKind kind, String card, String target, int amount, boolean relative, String source) { }

    public enum Advance { NEXT, DECISION }

    public static final class Message {
        public String title;
        public String text;
        /** What the bubble points at.  Null means a bubble in the middle of the screen. */
        public String anchor;
        public List<String> highlight = new ArrayList<>();
        public String highlightStyle = "gold";
        public List<String> avoid = new ArrayList<>();
        public String side = "auto";
        /** Null means "decide from position": the last message of a step waits for the decision, the others for Next. */
        public Advance advance;
    }

    public static final class Allow {
        /** The answers the player may give.  Anything else the game offers is shown as "not now". */
        public List<Answer> answers = new ArrayList<>();
        /** Card-definition requirements (JSON array text) that must hold before Pass is allowed. */
        public String passWhen;
        /** What to say when the player picks something legal that is not allowed.  Null means the stock line. */
        public String otherwise;
        /** Card alias -> what to say when the player picks that particular card instead. */
        public Map<String, String> hints = new LinkedHashMap<>();
    }

    public enum EndResult { COMPLETE, FAIL }

    public static final class End {
        public EndResult result = EndResult.COMPLETE;
        public List<Message> messages = new ArrayList<>();
    }

    public static final class Step {
        public String id;
        public When when = new When();
        /** If present, the step keeps applying to decisions until one arrives that matches this. */
        public When until;
        /** If a decision arrives that matches this while the step is still waiting, the step is given up on. */
        public When skipWhen;
        public boolean checkpoint;
        public List<DoOp> doOps = new ArrayList<>();
        public List<Message> messages = new ArrayList<>();
        /** Null means the player may answer however they like. */
        public Allow allow;
        /** The scripted answer(s): what the opponent does, or what the runner does on the player's behalf. */
        public List<Answer> answers = new ArrayList<>();
        public End end;

        public boolean repeats() {
            return until != null;
        }
    }

    public enum OffScriptResult { RESTART, FAIL, CONTINUE }

    public static final class Outcome {
        /** Card-definition requirements, as JSON array text.  All must hold. */
        public String win;
        public String lose;
        public String offScriptMessage;
        public OffScriptResult offScriptResult = OffScriptResult.RESTART;
    }

    public Step findStep(String stepId) {
        for (Step step : steps)
            if (step.id.equals(stepId))
                return step;
        return null;
    }
}
