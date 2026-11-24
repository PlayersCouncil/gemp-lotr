package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierHook;
import com.gempukku.lotro.logic.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.scenario.ScenarioDefinition.Modification;
import com.gempukku.lotro.scenario.ScenarioDefinition.ModifierTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Modifiers put on one card by hand: a board change (do: modify(aragorn, strength, +2, turn)), part of the board a
 * scenario starts with (setup.modifiers), or a sandbox player's edit.  A short list on purpose, Ketura's: a card's
 * strength, vitality, resistance or twilight cost changed by an amount, or one of a few keywords given to it; for
 * the rest of the phase, the rest of the turn, or until taken off again.
 *
 * Each is the game's own modifier, the same one a card's text would set up (StrengthModifier, AddKeywordModifier and
 * so on), affecting that one card and nothing else, with no card as its source: no card's text put it there, so
 * nothing that takes text away takes it away, and whatever stops a card getting bonuses stops this one too.  One
 * for a time is put in the game's lists for that time (until the end of the phase, of the turn), and goes when the
 * game's own do.
 *
 * The table keeps what it has put on, so that it can be listed and taken off: each with a number, counted from 1 in
 * each game, in the order they were put on.  A game rebuilt from its start (a take-back, a journal followed) puts
 * the same ones on in the same order, and so gives them the same numbers.
 */
public final class CardModifiers {
    /**
     * One thing a card can be modified in.
     * @param word Its word in the file.
     * @param label What it is called on the page.
     * @param keyword The keyword given; null for a stat.
     * @param numbered Whether it takes a number: a stat's change (signed), or a keyword's own number (damage +1).
     * @param suits The cards it means something on.
     * @param suitsWords How those are said in a mistake's message: "a character (a companion, an ally or a minion)".
     */
    public record Word(String word, String label, Keyword keyword, boolean numbered, Set<CardType> suits, String suitsWords) {
        public boolean stat() {
            return keyword == null;
        }
    }

    private static final Set<CardType> CHARACTERS = Set.of(CardType.COMPANION, CardType.ALLY, CardType.MINION);
    private static final String CHARACTER = "a character (a companion, an ally or a minion)";
    /** Every card with a twilight cost: anything that is played. */
    private static final Set<CardType> PLAYED = Set.of(CardType.COMPANION, CardType.ALLY, CardType.MINION, CardType.FOLLOWER,
            CardType.POSSESSION, CardType.ARTIFACT, CardType.CONDITION, CardType.EVENT);

    /**
     * What can be modified, in the order the page offers them.  The keywords are the ones the game reads only from
     * whether a card has them (and how many): giving one with an ordinary keyword modifier is all it takes for the
     * game's rules to treat the card as having it.  Ambush, toil and muster are read when their moment comes
     * (assigned, played, the start of the Regroup phase), so they mean something on a card that will have that
     * moment.  Left out on purpose: keywords that are a state of the game rather than of the card (roaming,
     * hindered, Ring-bound), ones that need more than the keyword (aid has a cost), and the many that are only
     * names (races, Knight, Tale, terrain), which no rule reads.
     */
    public static final List<Word> WORDS = List.of(
            new Word("strength", "Strength", null, true, CHARACTERS, CHARACTER),
            new Word("vitality", "Vitality", null, true, CHARACTERS, CHARACTER),
            new Word("resistance", "Resistance", null, true, Set.of(CardType.COMPANION), "a companion"),
            new Word("cost", "Twilight cost", null, true, PLAYED, "a card that is played (not a site or The One Ring)"),
            new Word("damage", "Damage", Keyword.DAMAGE, true, CHARACTERS, CHARACTER),
            new Word("defender", "Defender", Keyword.DEFENDER, true, CHARACTERS, CHARACTER),
            new Word("archer", "Archer", Keyword.ARCHER, false, CHARACTERS, CHARACTER),
            new Word("enduring", "Enduring", Keyword.ENDURING, false, CHARACTERS, CHARACTER),
            new Word("hunter", "Hunter", Keyword.HUNTER, true, CHARACTERS, CHARACTER),
            new Word("unhasty", "Unhasty", Keyword.UNHASTY, false, Set.of(CardType.COMPANION, CardType.ALLY), "a companion or an ally"),
            new Word("muster", "Muster", Keyword.MUSTER, false, PLAYED, "a card that is played (not a site or The One Ring)"),
            new Word("toil", "Toil", Keyword.TOIL, true, PLAYED, "a card that is played (not a site or The One Ring)"),
            new Word("fierce", "Fierce", Keyword.FIERCE, false, Set.of(CardType.MINION), "a minion"),
            new Word("lurker", "Lurker", Keyword.LURKER, false, Set.of(CardType.MINION), "a minion"),
            new Word("ambush", "Ambush", Keyword.AMBUSH, true, Set.of(CardType.MINION), "a minion"),
            new Word("relentless", "Relentless", Keyword.RELENTLESS, false, Set.of(CardType.MINION), "a minion"),
            new Word("cunning", "Cunning", Keyword.CUNNING, false, Set.of(CardType.MINION), "a minion"));

    private static final Map<String, Word> BY_WORD = new LinkedHashMap<>();
    static {
        for (Word word : WORDS)
            BY_WORD.put(word.word(), word);
    }

    /** The thing a word in the file means; null if it is none of them. */
    public static Word word(String written) {
        return written == null ? null : BY_WORD.get(written.trim().toLowerCase(Locale.ROOT));
    }

    private static String wordsList() {
        return String.join(", ", BY_WORD.keySet());
    }

    /**
     * A modification as the file writes one: what, then its number if it takes one, then how long if not until it is
     * taken off.  do: modify(card, what, +2, turn) gives the parts after the card; setup.modifiers gives
     * "strength +2 turn" split at its spaces.
     * @param context Where in the file, for the message.
     */
    public static Modification parse(String what, List<String> rest, String context) throws ScenarioFormatException {
        Word word = word(what);
        if (word == null)
            throw new ScenarioFormatException(context + ": '" + what.trim() + "' is not something a card can be modified in.  It can be one of "
                    + wordsList() + " (cost is the twilight cost)");
        Integer amount = null;
        ModifierTime until = ModifierTime.REMOVED;
        boolean timed = false;
        for (String part : rest) {
            String written = part.trim();
            if (written.isEmpty())
                continue;
            if (timed)
                throw new ScenarioFormatException(context + ": '" + written + "' comes after how long it lasts; write " + example(word) + ", how long last");
            if (written.matches("[+-]?\\d+")) {
                if (amount != null)
                    throw new ScenarioFormatException(context + ": " + word.word() + " is given two numbers; write one: " + example(word));
                amount = Integer.parseInt(written.startsWith("+") ? written.substring(1) : written);
                continue;
            }
            until = switch (written.toLowerCase(Locale.ROOT)) {
                case "phase" -> ModifierTime.PHASE;
                case "turn" -> ModifierTime.TURN;
                default -> throw new ScenarioFormatException(context + ": '" + written + "' is neither a number nor how long the modifier lasts "
                        + "(phase: to the end of this phase; turn: to the end of this turn; nothing: until it is taken off)");
            };
            timed = true;
        }
        if (!word.numbered()) {
            if (amount != null)
                throw new ScenarioFormatException(context + ": " + word.word() + " takes no number (a card is " + word.word()
                        + " or it is not); write just " + word.word());
            return new Modification(word.word(), 0, until);
        }
        if (word.stat()) {
            if (amount == null)
                throw new ScenarioFormatException(context + ": " + word.word() + " needs the amount it is changed by, with its sign: "
                        + word.word() + " +2, " + word.word() + " -1");
            if (amount == 0)
                throw new ScenarioFormatException(context + ": " + word.word() + " changed by 0 is no change; write +n or -n");
            return new Modification(word.word(), amount, until);
        }
        //(a keyword's number: damage +1, as the card text has it; without one, 1)
        if (amount == null)
            amount = 1;
        if (amount < 1)
            throw new ScenarioFormatException(context + ": " + word.word() + " is a keyword given to the card, and its number is 1 or more ("
                    + word.word() + " +1); a keyword is not taken away by this");
        return new Modification(word.word(), amount, until);
    }

    private static String example(Word word) {
        return word.numbered() ? word.word() + (word.stat() ? " +2" : " +1") : word.word();
    }

    /**
     * Why a modification means nothing on a card of this type; null if it means something.
     */
    public static String unsuited(Modification modification, CardType type) {
        Word word = word(modification.what());
        if (word == null || type == null || word.suits().contains(type))
            return null;
        return word.label().toLowerCase(Locale.ROOT) + " is for " + word.suitsWords() + ", and this is " + article(type);
    }

    private static String article(CardType type) {
        String name = type == CardType.THE_ONE_RING ? "The One Ring" : type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return type == CardType.THE_ONE_RING ? name : ("aeiou".indexOf(name.charAt(0)) >= 0 ? "an " : "a ") + name;
    }

    /* ------------------------------------------------------------------ what a game has had put on */

    /**
     * One modifier put on by hand.
     * @param id Its number in the game, from 1.
     * @param cardId The game's id of the card it is on.
     */
    public static final class Put {
        private final int _id;
        private final int _cardId;
        private final Modification _modification;
        private final Modifier _modifier;
        private ModifierHook _hook;
        private boolean _removed;

        private Put(int id, int cardId, Modification modification, Modifier modifier) {
            _id = id;
            _cardId = cardId;
            _modification = modification;
            _modifier = modifier;
        }

        public int id() { return _id; }
        public int cardId() { return _cardId; }
        public Modification modification() { return _modification; }

        /** What the card's information says of it: "Strength +2", "Fierce", "Damage +1". */
        public String text() {
            Word word = word(_modification.what());
            if (_modification.amount() == 0)
                return word.label();
            return word.label() + " " + (_modification.amount() > 0 ? "+" : "") + _modification.amount();
        }

        /** As the page is told of it (see ScenarioAuthoring.describeBoard). */
        public Map<String, Object> json() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", _id);
            json.put("what", _modification.what());
            json.put("amount", _modification.amount() == 0 ? null : _modification.amount());
            json.put("until", _modification.until().name().toLowerCase(Locale.ROOT));
            json.put("text", text());
            //(as a step's Do would say it, the card left out: "strength +2 turn")
            json.put("written", _modification.text());
            return json;
        }
    }

    private final List<Put> _put = new ArrayList<>();
    private int _next = 1;

    /**
     * Puts a modifier on a card.
     * @param phase The phase a modifier for the phase lasts to the end of: the phase the game is in, or for the board
     *              a scenario starts with, the phase it starts in.
     * @throws IllegalArgumentException if it means nothing on that card, with words that say why.
     */
    public Put put(LotroGame game, PhysicalCard card, Modification modification, Phase phase) {
        String unsuited = unsuited(modification, card.getBlueprint().getCardType());
        if (unsuited != null)
            throw new IllegalArgumentException(unsuited);
        Put[] made = new Put[1];
        //Taken off by hand, a modifier for a time is no longer in force (the game's lists for that time have no way to
        // take one out early); one until taken off is taken out of the game altogether
        Condition notRemoved = ignored -> !made[0]._removed;
        Modifier modifier = modifier(card, modification, notRemoved);
        made[0] = new Put(_next++, card.getCardId(), modification, modifier);
        ModifiersEnvironment modifiers = game.getModifiersEnvironment();
        switch (modification.until()) {
            case PHASE -> modifiers.addUntilEndOfPhaseModifier(modifier, phase);
            case TURN -> modifiers.addUntilEndOfTurnModifier(modifier);
            case REMOVED -> made[0]._hook = modifiers.addAlwaysOnModifier(modifier);
        }
        _put.add(made[0]);
        return made[0];
    }

    private static Modifier modifier(PhysicalCard card, Modification modification, Condition condition) {
        Word word = word(modification.what());
        int amount = modification.amount();
        if (word.keyword() != null)
            return new AddKeywordModifier(null, card, condition, word.keyword(), Math.max(1, amount));
        return switch (word.word()) {
            case "strength" -> new StrengthModifier(null, card, condition, amount);
            case "vitality" -> new VitalityModifier(null, card, condition, amount);
            case "resistance" -> new ResistanceModifier(null, card, condition, amount);
            case "cost" -> new TwilightCostModifier(null, card, condition, new ConstantEvaluator(amount));
            default -> throw new IllegalStateException("No modifier for " + word.word());
        };
    }

    /**
     * Takes one off.
     * @return Whether there was one of that number still in force.
     */
    public boolean remove(LotroGame game, int id) {
        for (Put put : inForce(game))
            if (put._id == id) {
                take(put);
                return true;
            }
        return false;
    }

    /**
     * Takes off every one on a card, or every one of a kind.
     * @param what A word (strength, fierce); null for all of them.
     * @return How many were taken off.
     */
    public int removeFrom(LotroGame game, PhysicalCard card, String what) {
        int taken = 0;
        for (Put put : inForce(game))
            if (put._cardId == card.getCardId() && (what == null || put._modification.what().equals(what))) {
                take(put);
                taken++;
            }
        return taken;
    }

    private void take(Put put) {
        put._removed = true;
        if (put._hook != null)
            put._hook.stop();
        _put.remove(put);
    }

    /** The ones on a card that are still in force, in the order they were put on. */
    public List<Put> on(LotroGame game, PhysicalCard card) {
        List<Put> on = new ArrayList<>();
        for (Put put : inForce(game))
            if (put._cardId == card.getCardId())
                on.add(put);
        return on;
    }

    /**
     * Every one still in force, in the order they were put on.  One for a time whose time is up has been taken out of
     * the game's lists by the game, and is dropped from here as it is noticed.
     */
    public List<Put> inForce(LotroGame game) {
        List<Put> live = new ArrayList<>();
        for (Put put : List.copyOf(_put)) {
            if (put._modification.until() == ModifierTime.REMOVED || stillHeld(game, put)) {
                live.add(put);
            } else {
                put._removed = true;
                _put.remove(put);
            }
        }
        return live;
    }

    /** Whether the game still has a modifier for a time among its modifiers. */
    private static boolean stillHeld(LotroGame game, Put put) {
        PhysicalCard card = game.getGameState().findCardById(put._cardId);
        return card != null && game.getModifiersQuerying().getModifiersAffecting(game, card).contains(put._modifier);
    }
}
