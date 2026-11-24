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
    public enum Fallback { FAIL, PASS, BOT, SANDBOX }
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
     * options.autoPass written for each role ({ freeps: [...], shadow: [...] }): the phases the person is passed
     * through while they are the Free Peoples player (their own turn), and while they are the Shadow player (the
     * other seat's turn, in a two-sided game).  Null when autoPass is written as one list, which holds whatever role
     * the person is playing (autoPass above).
     */
    public Map<com.gempukku.lotro.common.Side, Set<Phase>> autoPassByRole;

    /** The phases the person is passed through while playing a role: autoPassByRole's, or the one list. */
    public Set<Phase> autoPassFor(com.gempukku.lotro.common.Side role) {
        if (autoPassByRole == null)
            return autoPass;
        return autoPassByRole.getOrDefault(role, EnumSet.noneOf(Phase.class));
    }
    /**
     * Whether a click that completes a choice is sent at once, without a Done to confirm it.  It is fixed for the
     * game, whatever the person's own setting is, so that the scenario's text can say what will happen.
     */
    public boolean autoAccept = true;
    /**
     * Whether the look through the person's own deck that comes with searching it is skipped (the person's own
     * "auto-dismiss deck reveal" setting, fixed for the game).  Null: on for a tutorial, off for anything else.
     */
    public Boolean autoDismissDeckReveal;
    /**
     * The animation speed the game is shown at, fixed for the game: the steps of the Settings tab's slider, from -4
     * (16 times as slow) through 0 (as the game comes) to 4 (16 times as fast).  Null: the person's own setting.
     */
    public Integer animationSpeed;

    /** Whether the look through one's own deck is skipped, as the file says or as the scenario's kind has it. */
    public boolean dismissesDeckReveal() {
        return autoDismissDeckReveal != null ? autoDismissDeckReveal : kind == Kind.TUTORIAL;
    }
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
    /**
     * Each site's name in the scenario, by seat, in the same order as sites: "site3" for the player's own site 3 and
     * "site3opp" for the opponent's, unless the file names it otherwise.
     */
    public final Map<Seat, List<String>> siteAliases = new EnumMap<>(Seat.class);
    /** Every site by its name in the scenario (a site is named, and picked, as a card is). */
    public final Map<String, CardDef> siteCards = new LinkedHashMap<>();

    /** The name a site has when the file does not give it one. */
    public String defaultSiteAlias(Seat seat, int number) {
        return "site" + number + (seat == human ? "" : "opp");
    }

    /** Card-definition "effects" for each seat's rules card, as JSON array text.  Absent means no rules. */
    public final Map<Seat, String> rules = new EnumMap<>(Seat.class);
    /** The Free Peoples seat takes every turn; the Shadow seat never gets a turn of its own. */
    public boolean oneSided = false;
    /**
     * In a one-sided game (rules.markers: together): the Shadow seat's marker, which never moves of itself there,
     * keeps level with the fellowship's, so that the sites behind them both can be controlled.  False: it stays
     * where it is put (site 1, setup.startAt.opponentSite, or do: marker(...)), as it always has.
     */
    public boolean markersTogether = false;

    /**
     * Whose characters the opponent's leftovers (and the sandbox's assigning) go to: the fellowship it assigns minions
     * against.  In a one-sided game that is the Free Peoples seat's, whoever sits there (as it always was); in a
     * two-sided game it is the person's, the opponent assigning as the Shadow player on the person's turns.
     */
    public Seat leftoversSeat() {
        return oneSided ? Seat.FREEPS : human;
    }

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
        /**
         * setup.opening: game.  The game's own opening is played, as decisions the script answers (the bid, the
         * choice of going first, the starting fellowships, the mulligans), in place of the board being laid out: the
         * decks are the declared cards in the declared order (with setup.<seat>.deck on top), and nothing else of
         * the setup is said.  False (opening: board, the default): the board is prepared as the setup says.
         */
        public boolean realOpening = false;

        /*
         * Where the story has got to when the game begins (setup.startAt, and setup.twilight).  A game that begins
         * anywhere but the start of turn 1 at site 1 is carried on from there, not begun: see lateStart.
         */
        /** The site the fellowship whose turn it is (the Free Peoples seat's) is at. */
        public int site = 1;
        /** The Shadow seat's marker; null for the default (see opponentSite(def)). */
        public Integer opponentSite;
        /** What is in the twilight pool when play begins. */
        public int twilight = 0;
        /** How many times the fellowship has moved this turn already; null for the default (see moves()). */
        public Integer moves;
        /** Free Peoples character -> the minions it is assigned to skirmish, for a start at the assignments or skirmishes. */
        public final Map<String, List<String>> skirmishes = new LinkedHashMap<>();
        /** The sites each seat controls when the game begins: site numbers or sites' names, as written. */
        public final Map<Seat, List<String>> controlled = new EnumMap<>(Seat.class);
        /**
         * The sites on the adventure path when the game begins, by name, site 1 first, where it matters whose they are
         * (a card that cares who played a site).  Empty, and past its end: the Free Peoples seat's own sites.
         */
        public final List<String> path = new ArrayList<>();

        /** Where the Shadow seat's marker is when the game begins. */
        public int opponentSite(ScenarioDefinition def) {
            if (opponentSite != null)
                return opponentSite;
            return def.oneSided && def.markersTogether ? site : 1;
        }

        /** How many moves the fellowship has made this turn when the game begins. */
        public int moves() {
            return moves != null ? moves : defaultMoves();
        }

        /** One move (to the site it is at) once the Fellowship phase is over; none before. */
        public int defaultMoves() {
            return phase == Phase.FELLOWSHIP ? 0 : 1;
        }

        /**
         * Whether the game begins further on than the very start: on a later turn or site, past the start of the
         * Fellowship phase, with twilight in the pool or skirmishes assigned.  Such a game is carried on from where it
         * is (ScenarioTable.carryOn), and nothing the story has already been through happens again: no start of turn
         * (so no sanctuary healing), no moving to the site the fellowship is at.
         */
        public boolean lateStart() {
            return turn != 1 || phase != Phase.FELLOWSHIP || site != 1 || twilight != 0 || moves() != defaultMoves()
                    || !skirmishes.isEmpty();
        }
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
        /**
         * The role the seat being asked plays at that moment, written as(freeps) or as(shadow): the Free Peoples
         * player is the one whose turn it is.  (A seat is a chair: in a two-sided game each seat plays both roles,
         * one on its own turns and the other on the other seat's.)  Null for either.
         */
        public com.gempukku.lotro.common.Side role;
        /** Which skirmish of the turn is being fought, counting from 1. */
        public Integer skirmish;
        /** A card that has to be among the decision's options. */
        public String offers;
        /**
         * A card that has to be in the skirmish being fought: the character on either side of it.  Written
         * inSkirmish(card); fighting(card), as it once was, is read as that.
         */
        public String fighting;
        /**
         * A Free Peoples character whose skirmish is the last one finished this turn: the first decision after that
         * skirmish, whatever it is (choosing the next skirmish, the next skirmish's own, a fierce assignment, or the
         * first of the Regroup phase).  In a block, a step that waits for it is offered the decision before the
         * block's other steps, and before the block's end.
         */
        public String after;
        /** Card-definition requirements, as JSON array text. */
        public String require;
        /**
         * "opening": not a decision of the game's at all, but the moment before the first turn, with the board laid
         * out and nothing yet begun.  Only a scenario's first step can wait for it (see ScenarioTable.arrangeBoard).
         */
        public boolean opening;
        /** How it was written, for reports. */
        public String source = "";
        /** What the decision has to be for (see DecisionKinds): nextSkirmish, archeryWound and the like. */
        public List<DecisionKinds.Kind> kinds = new ArrayList<>();
        /** What it must not be for: not(nextSkirmish). */
        public List<DecisionKinds.Kind> notKinds = new ArrayList<>();

        public boolean isEmpty() {
            return seat == null && decision == null && text == null && phase == null && turn == null && role == null
                    && skirmish == null && offers == null && fighting == null && after == null && require == null && !opening
                    && kinds.isEmpty() && notKinds.isEmpty();
        }

        /** Whether a decision that is (or is not, for null) of a kind fits what this says of kinds. */
        public boolean fitsKind(DecisionKinds.Kind kind) {
            for (DecisionKinds.Kind wanted : kinds)
                if (wanted != kind)
                    return false;
            return kind == null || !notKinds.contains(kind);
        }
    }

    public enum AnswerKind { PASS, PLAY, USE, CHOOSE, OPTION, NUMBER, ASSIGN, RAW }

    /**
     * An answer to a decision, written with card aliases rather than whatever IDs the cards have at the time.
     * @param cards PLAY and USE: the one card.  CHOOSE: the chosen cards.
     * @param text USE: a fragment of the action's text, when the card offers more than one.  OPTION: the option's
     *             text, or "#n" for the n-th option counting from 1.  RAW: the literal answer.
     * @param assignments ASSIGN: Free Peoples character -> the minions assigned to it.
     * In an 'allow', an answer is allowed whenever the decision offers it, and it is no mistake if the decision does
     * not (the other copy of a card that has already been played, say): only a step that allows nothing the decision
     * offers is one.
     */
    public record Answer(AnswerKind kind, List<String> cards, String text, int number,
                         Map<String, List<String>> assignments, String source, List<Pick> picks) {
        public Answer(AnswerKind kind, List<String> cards, String text, int number,
                      Map<String, List<String>> assignments, String source) {
            this(kind, cards, text, number, assignments, source, List.of());
        }
    }

    /**
     * One card a choose(...) picks.  It is a card by its name in the scenario, or whichever card is skirmishing
     * ("skirmishing", or "skirmishing(aragorn)": in the skirmish against that character), narrowed by card filters
     * as cards write them ("title(Orthanc Warrior)", "not(exhausted)"), and then, of the cards on offer that are
     * left, by which one it is counting from 1 (0: whichever comes first).
     * @param alias The card, by its name in the scenario; null if it is picked by its skirmish.
     * @param skirmishing Whether it is picked by being in the skirmish being fought.
     * @param against With skirmishing: the character it is skirmishing against; null for either side.
     * @param filter Card filters it has to pass, with a comma between them, names in the scenario as alias(...);
     *               null for none.
     */
    public record Pick(String alias, boolean skirmishing, String against, String filter, int ordinal, String source) {
        public boolean plain() {
            return alias != null && !skirmishing && filter == null && ordinal == 0;
        }

        /** The card filter that says it all, names in the scenario as alias(...). */
        public String asFilter() {
            List<String> parts = new java.util.ArrayList<>();
            if (alias != null)
                parts.add("alias(" + alias + ")");
            if (skirmishing)
                parts.add(against == null ? "inSkirmish" : "inSkirmishAgainst(alias(" + against + "))");
            if (filter != null)
                parts.add(filter);
            return String.join(",", parts);
        }
    }

    /**
     * A standing order: what the opponent does whenever it is asked something that no step covers and this fits.
     * Orders are tried in the order they are written, and the first whose answer the decision offers is taken.
     */
    public record Order(When when, List<Answer> answers, String source, boolean once, String note, String id) {
        public Order(When when, List<Answer> answers, String source) {
            this(when, answers, source, false, null, null);
        }

        /** What the order is called in reports: its name if it has one, otherwise its place among the orders (from 1). */
        public String label(int place) {
            return id == null ? "order " + place : "order " + place + " (" + id + ")";
        }
    }

    public enum DoKind { MOVE, ATTACH, STACK, WOUND, HEAL, TWILIGHT, BURDENS, THREATS, TOKENS, DRAW, ORDER,
        /** A seat's marker put on a site: target the seat, amount the site's number. */
        MARKER,
        /** A site controlled by a seat: card the site's name (or null), amount its number (or 0), target the seat. */
        CONTROL,
        /** A controlled site controlled by nobody again: card the site's name (or null), amount its number (or 0). */
        LIBERATE }

    /**
     * Something the director does to the board directly.
     * @param card The card acted on (not used by TWILIGHT, BURDENS, THREATS, DRAW).  ORDER: the cards, in the order
     *             they are to come, with ", " between them.
     * @param target MOVE: where to, e.g. "freeps.hand", "shadow.deck.top".  ATTACH, STACK: the other card.  DRAW: the
     *               seat that draws.  ORDER: the hand or pile, e.g. "freeps.hand".
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
     * A card (or a picture) shown as large as the screen allows while a message is up.  Exactly one of card, site,
     * blueprint and image says what.
     * @param card The alias of one of the scenario's cards.
     * @param site The number of a site on the person's adventure path.
     * @param blueprint Any card at all, by its blueprint ID.
     * @param image A picture that is not a card: the name of a file in the web folder's images/scenarios.
     * @param focus The parts of its face the message is about; empty for the whole of it.
     * @param magnify 1 shows the whole of it; more than that shows it that many times larger, centred on the focus
     *                (on its middle if there is none).
     */
    public record Zoom(String card, Integer site, String blueprint, String image, List<Focus> focus, double magnify) {
        /** What a picture's file can be called: nothing that leads out of the folder it is kept in. */
        public static final String IMAGE_NAME = "[A-Za-z0-9_-][A-Za-z0-9_. -]{0,80}\\.(?i:png|jpe?g|gif|webp)";
    }

    public static final class Message {
        /** An author's note about the message.  Never shown to a player. */
        public String note;
        /** A heading over the text.  Most messages have none. */
        public String header;
        /**
         * Said the first time its step applies, and not again when the step applies to more decisions (a step that
         * repeats, or one of a block's that is not 'once').  False: said every time the step applies.
         */
        public boolean once = true;
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
        /**
         * Card-definition requirements (JSON array text) that have to hold for the message to be said; null for
         * always.  They are looked at when the step is put up (see ScenarioDirector.render), which for a message
         * tied to a moment ('on') is the same as when its moment comes: nothing in the game changes while the
         * player builds their answer.  A 'once' message is said the first time its step applies with them holding.
         */
        public String require;
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

        /**
         * Set for a block ("anyOrder"): a stretch of the game whose decisions come in an order the script cannot
         * know (which skirmish is fought first, how many times each side acts in it).  A block has no decision of
         * its own.  It has steps, and while it is the step the script is at, each decision that arrives goes to the
         * first of its steps that fits, wherever that step comes; it is over when a decision arrives that fits its
         * 'until', which is then the next step's.  Its own 'when', if it has one, is something every decision its
         * steps take has to fit as well.
         */
        public boolean block;
        /**
         * A block that is the skirmishes of a turn ("skirmishes: true"): the same as any block to the game; the
         * authoring tool shows its steps by the skirmish they are in (fighting(x)), and after it (after(x)).
         */
        public boolean skirmishes;
        /** A block's steps, in the order they are tried. */
        public List<Step> children = new ArrayList<>();
        /** The block a step is in; null for a step of the script itself. */
        public Step parent;
        /** Where a step comes in its block, counting from 1; 0 for a step of the script itself. */
        public int sub;
        /**
         * In a block: the step applies to one decision and is then done with.  Without it a block's step applies to
         * every decision that fits it (and shows its messages at the first).
         */
        public boolean once;
        /**
         * Answers the step's 'allow' has to refuse, for the headless runner to prove (see Refusal).  A live game
         * takes no notice of them.
         */
        public List<Refusal> refuses = new ArrayList<>();

        public boolean repeats() {
            return until != null;
        }

        /** Whether the step can apply to more than one decision: it repeats, or it is a block's and not 'once'. */
        public boolean recurs() {
            return until != null || (parent != null && !once && !pause);
        }

        /** What the step is called in reports: its number, with its name if it has one.  A block's steps are 12.1, 12.2. */
        public String label() {
            String place = parent == null ? String.valueOf(number) : parent.number + "." + sub;
            return id == null ? place : place + " (" + id + ")";
        }

        /** Where the step is in the file, as the parser says where things are: "step 12", "step 12.anyOrder[2]". */
        public String context() {
            return parent == null ? "step " + label() : "step " + parent.number + ".anyOrder[" + sub + "]";
        }

        /** The label, plus what the step waits for, to say which step is meant to somebody reading the file. */
        public String describe() {
            return "step " + label() + (when.source.isEmpty() ? "" : " [when: " + when.source + "]");
        }
    }

    /**
     * An answer that a step's 'allow' must refuse: "refuses: choose(boromir)".  The runner checks, at the step's
     * decision, that the game offers the answer and that the step does not allow it (the page would not let the
     * player give it).
     * @param at Which of the step's decisions it is about, counting from 1 ("refuses: 2: choose(boromir)", for the
     *           second arrow of an archery step that repeats); 0 for every decision of the step, of which the game
     *           has to offer it at one at least.
     * @param source The line as it was written, its "2: " included.
     */
    public record Refusal(int at, Answer answer, String source) { }

    public enum OffScriptResult { RESTART, FAIL, CONTINUE }

    public static final class Outcome {
        /** Card-definition requirements, as JSON array text.  All must hold. */
        public String win;
        public String lose;
        /** What to say when the scenario is failed; null for the stock line. */
        public String loseMessage;
        public String offScriptMessage;
        public OffScriptResult offScriptResult = OffScriptResult.RESTART;
        /** Closing messages said when 'win' comes true, as a step's end.messages are. */
        public List<Message> winMessages = new ArrayList<>();
        /** Closing messages said when 'lose' comes true (loseMessage, if there is one, is said after them, as it always was). */
        public List<Message> loseMessages = new ArrayList<>();
        /**
         * How the scenario ends when the game itself ends with the person winning it (the Ring-bearer they are hunting
         * killed or corrupted, their fellowship surviving its last site): what it counts as, and what is said.  Null
         * for as before: complete, and nothing said.
         */
        public End gameWon;
        /**
         * The same when the game ends with the person losing it (their Ring-bearer killed or corrupted, the other
         * fellowship surviving).  Null for as before: failed, with loseMessage.
         */
        public End gameLost;
    }

    /* ---- the opponent's answering by rule (assign by rule, leftovers, the sandbox) ---- */

    /**
     * In assign(...): "every minion still unassigned", written rest or all, in place of a character's minions.  (The
     * sentinels begin with a character no card's name can have, so that a card called "rest" is still that card.)
     */
    public static final String REST = "~rest";
    /** In assign(...) and opponent.leftovers: whichever character is the Free Peoples player's Ring-bearer. */
    public static final String RING_BEARER = "~ringBearer";

    /**
     * opponent.leftovers: the character the opponent assigns every minion still unassigned to, after whatever a step
     * or one of its orders assigns, and when nothing answers its assignment at all: a card's name, or RING_BEARER.
     * Null for none (the old way: what is not assigned stays unassigned).
     */
    public String leftovers;
    /**
     * opponent.plays, for the sandbox fallback: the cards it may play, in the order it tries them (minions first
     * whatever the order).  Empty for every Shadow card it has.
     */
    public final List<String> sandboxPlays = new ArrayList<>();

    /**
     * @param label A step's label, as given by Step.label().
     */
    public Step findStep(String label) {
        for (Step step : allSteps())
            if (step.label().equals(label))
                return step;
        return null;
    }

    /** Every step: the steps of the script in order, each block followed by the steps in it. */
    public List<Step> allSteps() {
        List<Step> all = new ArrayList<>();
        for (Step step : steps) {
            all.add(step);
            all.addAll(step.children);
        }
        return all;
    }

    /* ---- sandboxes ---- */

    /**
     * options.tools: what the person playing a sandbox may change while it is stopped, by the names of
     * ScenarioSandbox.TOOLS.  Null for all of them.
     */
    public List<String> tools;

    /**
     * What the person playing may change in edit mode, as the page is told it: "sandbox" for a sandbox, "none"
     * otherwise.  (A puzzle's level is the puzzle's own.)
     */
    public String level() {
        return kind == Kind.SANDBOX ? "sandbox" : "none";
    }

    /** The tools in force: options.tools, or all of them. */
    public List<String> toolsInForce() {
        return tools != null ? tools : ScenarioSandbox.TOOLS;
    }

    /**
     * The coverage the scenario is played with.  Nothing a person does in a sandbox is off its script, however few
     * steps it has: a sandbox is always guided, whatever its file says.
     */
    public Coverage coverageInForce() {
        return kind == Kind.SANDBOX ? Coverage.GUIDED : coverage;
    }

    /**
     * The opponent's fallback in force.  A sandbox's opponent never leaves the script by being asked something no
     * step covers: 'fail' (the default) is taken as 'pass' there.
     */
    public Fallback fallbackInForce() {
        return kind == Kind.SANDBOX && fallback == Fallback.FAIL ? Fallback.PASS : fallback;
    }
}
