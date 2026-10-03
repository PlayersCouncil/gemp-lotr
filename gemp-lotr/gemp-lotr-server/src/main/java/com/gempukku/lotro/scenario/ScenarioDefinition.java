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
    /** Whatever its author wanted to keep with the scenario.  Nothing reads it but people (and the authoring tool, which keeps it). */
    public String note;
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
    /**
     * Whether a click that completes a choice is sent at once, without a Done to confirm it.  It is fixed for the
     * game, whatever the person's own setting is, so that the scenario's text can say what will happen.
     */
    public boolean autoAccept = true;
    public RngDefault rngDefault = RngDefault.IDENTITY;
    public int opponentDelayMs = 900;
    /** How far a bubble sits from what it points at, where its message does not say (see Message.gap); null for the stock distance. */
    public String bubbleGap;
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
    /** What the opponent does with a decision no step covers, before it falls back on its fallback. */
    public final List<Order> orders = new ArrayList<>();
    /** Every name a choice in the script can set, in the order the script offers them. */
    public final List<String> flags = new ArrayList<>();

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
     * @param ifOffered In an 'allow': the answer is allowed whenever the decision offers it, and it is no mistake if
     *                  the decision does not (the other copy of a card that has already been played, say).
     */
    public record Answer(AnswerKind kind, List<String> cards, String text, int number,
                         Map<String, List<String>> assignments, String source, boolean ifOffered) {
        public Answer(AnswerKind kind, List<String> cards, String text, int number,
                      Map<String, List<String>> assignments, String source) {
            this(kind, cards, text, number, assignments, source, false);
        }

        public Answer offeredOrNot() {
            return new Answer(kind, cards, text, number, assignments, source, true);
        }
    }

    /**
     * A standing order: what the opponent does whenever it is asked something that no step covers and this fits.
     * Orders are tried in the order they are written, and the first whose answer the decision offers is taken.
     */
    public record Order(When when, List<Answer> answers, String source) { }

    public enum DoKind { MOVE, ATTACH, STACK, WOUND, HEAL, TWILIGHT, BURDENS, THREATS, TOKENS }

    /**
     * Something the director does to the board directly.
     * @param card The card acted on (not used by TWILIGHT, BURDENS, THREATS).
     * @param target MOVE: where to, e.g. "freeps.hand", "shadow.deck.top".  ATTACH, STACK: the other card.
     * @param amount How many.  For TWILIGHT, BURDENS and THREATS this is a new total unless relative is set.
     */
    public record DoOp(DoKind kind, String card, String target, int amount, boolean relative, String source) { }

    /** What takes a message off the screen and brings the next one. */
    public enum Advance {
        /** The Next button. */
        NEXT,
        /** The answer to the decision: the message stays up while the player answers. */
        DECISION,
        /** The player opens a card's information window (right-click, shift-click, or a swipe up). */
        INSPECT,
        /** The player closes the card information window again. */
        CLOSE
    }

    /**
     * A point inside the answering of one decision.  Some decisions take several clicks before anything is sent to
     * the game (assigning minions: pick up a minion, put it on a character, and so on, then Done).  The game sees none
     * of that; only the player's own screen does.  A message can be tied to one of these points, and it is the client
     * that decides when the point has come.
     */
    public enum MomentKind {
        /** The decision has just been put to the player.  This is when a message with no moment is shown. */
        START,
        /** The player has picked this card as part of building their answer. */
        PICKED,
        /** Assignment: this character now has every minion the allowed answer gives it. */
        ASSIGNED,
        /** What the player has built is a complete allowed answer; only confirming it remains. */
        READY
    }

    /**
     * @param card The card the moment is about (PICKED, ASSIGNED); null otherwise.
     */
    public record Moment(MomentKind kind, String card) { }

    /**
     * Part of the face of a card being shown large.
     * @param name A region every card of that kind has ("strength", "text"; see CardFaces); null if rect is given.
     * @param rect {left, top, width, height} in percent of the card image; null if name is given.
     */
    public record Focus(String name, double[] rect) { }

    /**
     * A card shown as large as the screen allows while a message is up.  Exactly one of card, site and blueprint
     * says which card.
     * @param card The alias of one of the scenario's cards.
     * @param site The number of a site on the person's adventure path.
     * @param blueprint Any card at all, by its blueprint ID.
     * @param focus The parts of its face the message is about; empty for the whole card.
     * @param magnify 1 shows the whole card; more than that shows it that many times larger, centred on the focus.
     */
    public record Zoom(String card, Integer site, String blueprint, List<Focus> focus, double magnify) { }

    public static final class Message {
        /** An author's note about the message.  Never shown to a player. */
        public String note;
        /** A heading over the text.  Most messages have none. */
        public String header;
        public String text;
        /**
         * What the bubble points at: usually one thing, which it sits beside; with several it sits beside them all
         * and has a line to each.  Empty means a bubble in the middle of the screen.
         */
        public List<String> anchor = new ArrayList<>();
        /**
         * How far the bubble sits from what it points at: a number is in hundredths of the window's shorter side (so
         * that it keeps pace with the table, which is laid out to the window), and a number followed by "px" is in
         * pixels.  Null means the scenario's own (options.bubbleGap), and failing that the stock distance.
         */
        public String gap;
        public List<String> highlight = new ArrayList<>();
        public String highlightStyle = "gold";
        public List<String> avoid = new ArrayList<>();
        public String side = "auto";
        /** Null means "decide from position": the last message of a step waits for the decision, the others for Next. */
        public Advance advance;
        /** INSPECT: the card whose information window has to be opened; null for any card. */
        public String advanceCard;
        /**
         * Buttons the message ends with, in place of Next: name -> what the button says.  Pressing one sets that
         * name for the rest of the game, for later steps to depend on ('if', 'unless').  Empty for most messages.
         */
        public Map<String, String> choices = new LinkedHashMap<>();
        /** When, within the answering of the decision, the message is shown.  Null means at the start. */
        public Moment on;
        /** A card to show large while this message is up; null for none. */
        public Zoom zoom;
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
        /** Where the step comes in the script, counting from 1.  This is how a step is known. */
        public int number;
        /** A name for the step, if its author wanted one.  It only makes reports easier to read. */
        public String id;
        /** An author's note about the step.  Never shown to a player. */
        public String note;
        public When when = new When();
        /** If present, the step keeps applying to decisions until one arrives that matches this. */
        public When until;
        /** If a decision arrives that matches this while the step is still waiting, the step is given up on. */
        public When skipWhen;
        public boolean checkpoint;
        /**
         * A step to be read, not answered: it applies to the player's decision, shows its messages, and once they
         * have been read the script moves on and the same decision goes to the next step.
         */
        public boolean pause;
        /** Names that all have to have been chosen (see Message.choices) for the step to be part of the game. */
        public List<String> onlyIf = new ArrayList<>();
        /** Names of which none may have been chosen. */
        public List<String> unless = new ArrayList<>();
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

        /** What the step is called in reports: its number, with its name if it has one. */
        public String label() {
            return id == null ? String.valueOf(number) : number + " (" + id + ")";
        }

        /** The label, plus what the step waits for, to say which step is meant to somebody reading the file. */
        public String describe() {
            return "step " + label() + (when.source.isEmpty() ? "" : " [when: " + when.source + "]");
        }
    }

    public enum OffScriptResult { RESTART, FAIL, CONTINUE }

    public static final class Outcome {
        /** Card-definition requirements, as JSON array text.  All must hold. */
        public String win;
        public String lose;
        /** What to say when the scenario is failed; null for the stock line. */
        public String loseMessage;
        public String offScriptMessage;
        public OffScriptResult offScriptResult = OffScriptResult.RESTART;
    }

    /**
     * @param label A step's label, as given by Step.label().
     */
    public Step findStep(String label) {
        for (Step step : steps)
            if (step.label().equals(label))
                return step;
        return null;
    }
}
