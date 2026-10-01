package com.gempukku.lotro.scenario;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PregameSetup;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.scenario.ScenarioDefinition.CardDef;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoOp;
import com.gempukku.lotro.scenario.ScenarioDefinition.SetupZone;
import com.gempukku.lotro.virtualtable.VirtualTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A virtual table laid out the way a scenario file says: each seat gets a deck made of exactly the cards the
 * scenario names for it, the scenario's rules are in force from the start, and the first turn begins with the board
 * the scenario describes instead of with starting fellowships, an opening draw and mulligans.
 *
 * This is everything about a scenario's game that does not depend on who is answering the decisions.  A headless
 * run answers them all from the script (see ScenarioRunner); a live game answers one seat's from the script and
 * leaves the other to a person.
 */
public class ScenarioTable extends VirtualTable implements PregameSetup {
    /** Parsing card-definition JSON needs the card builder's factories, which are costly to set up and safe to share. */
    private static final LotroCardBlueprintBuilder BUILDER = new LotroCardBlueprintBuilder();
    /** Rules cards need a blueprint ID of the usual shape.  No set has this number, and no client is ever sent it. */
    private static final String RULES_BLUEPRINT_ID = "999_1";

    private final ScenarioDefinition _def;
    private final LotroCardBlueprintLibrary _library;
    private final LotroFormat _format;
    private final String _freepsPlayer;
    private final String _shadowPlayer;

    private final Map<String, PhysicalCardImpl> _aliases = new LinkedHashMap<>();
    private final Map<Seat, PhysicalCardImpl> _rulesCards = new EnumMap<>(Seat.class);
    private final Map<String, Requirement[]> _requirements = new HashMap<>();
    private final List<String> _problems = new ArrayList<>();
    private ScenarioRandom _random;

    /**
     * @param humanPlayer The name of the player in the scenario's "human" seat.
     * @param opponentPlayer The name of the player in the other seat.
     */
    public ScenarioTable(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format,
                         String humanPlayer, String opponentPlayer) {
        _def = def;
        _library = library;
        _format = format;
        _freepsPlayer = def.human == Seat.FREEPS ? humanPlayer : opponentPlayer;
        _shadowPlayer = def.human == Seat.FREEPS ? opponentPlayer : humanPlayer;
        Cards.put(_freepsPlayer, new HashMap<>());
        Cards.put(_shadowPlayer, new HashMap<>());
    }

    @Override
    public String P1() { return _freepsPlayer; }
    @Override
    public String P2() { return _shadowPlayer; }

    public ScenarioDefinition Definition() { return _def; }

    public String Player(Seat seat) { return seat == Seat.FREEPS ? _freepsPlayer : _shadowPlayer; }
    public Seat SeatOf(String player) { return player.equals(_freepsPlayer) ? Seat.FREEPS : Seat.SHADOW; }
    public String HumanPlayer() { return Player(_def.human); }
    public String OpponentPlayer() { return Player(_def.human.other()); }

    /**
     * @return The card a scenario alias refers to.
     */
    public PhysicalCardImpl Card(String alias) {
        PhysicalCardImpl card = _aliases.get(alias);
        if (card == null)
            throw new IllegalStateException("The scenario has no card called '" + alias + "' on the table");
        return card;
    }

    /**
     * @return The scenario alias of a card, or null if the scenario did not name it.
     */
    public String AliasOf(PhysicalCard card) {
        for (Map.Entry<String, PhysicalCardImpl> entry : _aliases.entrySet())
            if (entry.getValue() == card)
                return entry.getKey();
        return null;
    }

    public String AliasOfCardId(String cardId) {
        for (Map.Entry<String, PhysicalCardImpl> entry : _aliases.entrySet())
            if (String.valueOf(entry.getValue().getCardId()).equals(cardId))
                return entry.getKey();
        return null;
    }

    public ScenarioRandom Random() { return _random; }

    /**
     * Things that went wrong while laying the table out or while the scenario's own instructions were carried out.
     * A well-formed scenario produces none.
     */
    public List<String> Problems() {
        List<String> all = new ArrayList<>(_problems);
        if (_random != null)
            all.addAll(_random.problems());
        return all;
    }

    /**
     * Builds the decks, starts the game, seats the players and lets the game run to its first decision: the Free
     * Peoples seat's first choice of the first turn, with the board arranged as the scenario describes.
     */
    public void Start() {
        Map<String, LotroDeck> decks = new HashMap<>();
        for (Seat seat : Seat.values()) {
            LotroDeck deck = new LotroDeck(_def.title);
            String ringBearer = _def.ringBearer.get(seat);
            String ring = _def.ring.get(seat);
            deck.setRingBearer(_def.cards.get(ringBearer).blueprintId());
            if (ring != null)
                deck.setRing(_def.cards.get(ring).blueprintId());
            for (String site : _def.sites.get(seat))
                deck.addSite(site);
            for (CardDef card : _def.cards.values())
                if (card.owner() == seat && !card.alias().equals(ringBearer) && !card.alias().equals(ring))
                    deck.addCard(card.blueprintId());
            //The format decides which version of each card is in force, the same as at a hall table
            decks.put(Player(seat), _format.applyErrata(deck));
        }

        InitializeGame(_format, decks, _library, null);

        //The Free Peoples seat outbids the other and elects to go first.  From there the game runs on its own
        // through the first site and the Ring-bearers, into arrangeBoard below, and on to the first turn.
        FreepsDecided("1");
        ShadowDecided("0");
        FreepsDecided("0");
    }

    @Override
    protected void BeforeGameStarts() {
        _random = new ScenarioRandom(this, _def);
        _gameState.setRandom(_random);
        _gameState.setPregameSetup(this);
        if (_def.oneSided)
            _gameState.setFixedFreePeoplePlayer(_freepsPlayer);

        //The person playing is asked to pass exactly where the scenario says, whatever their own settings are.  The
        // scripted opponent is only ever asked when it has something it could do.
        _game.setPlayerAutoPassSettings(HumanPlayer(), EnumSet.copyOf(_def.autoPass.isEmpty() ? EnumSet.noneOf(Phase.class) : _def.autoPass));
        _game.setPlayerAutoPassSettings(OpponentPlayer(), EnumSet.allOf(Phase.class));

        for (Seat seat : Seat.values()) {
            try {
                JSONObject definition = new JSONObject();
                definition.put("title", "Scenario Rules");
                definition.put("type", "metasite");
                String rules = _def.rules.get(seat);
                if (rules != null)
                    definition.put("effects", new JSONParser().parse(rules));
                LotroCardBlueprint blueprint = BUILDER.buildFromJson(RULES_BLUEPRINT_ID, definition);
                PhysicalCardImpl rulesCard = _gameState.createLooseCard(Player(seat), RULES_BLUEPRINT_ID, blueprint);
                rulesCard.startAffectingGame(_game);
                _rulesCards.put(seat, rulesCard);
                _game.getActionsEnvironment().addAlwaysOnActionProxy(new RulesCardTriggers(rulesCard));
            } catch (InvalidCardDefinitionException | ParseException exp) {
                throw new IllegalArgumentException("rules." + seat.key() + ": " + exp.getMessage(), exp);
            }
        }
    }

    /**
     * A rules card is in no zone, so nothing collects its triggers the way an in-play card's are collected.  This
     * does.  (Modifiers need no such help: startAffectingGame registers them.)
     */
    private static class RulesCardTriggers extends AbstractActionProxy {
        private final PhysicalCardImpl _rulesCard;

        private RulesCardTriggers(PhysicalCardImpl rulesCard) {
            _rulesCard = rulesCard;
        }

        @Override
        public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
            return _rulesCard.getBlueprint().getRequiredBeforeTriggers(game, effect, _rulesCard);
        }

        @Override
        public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
            return _rulesCard.getBlueprint().getRequiredAfterTriggers(game, effectResult, _rulesCard);
        }
    }

    /**
     * Called by the game in place of the usual opening, once every card exists.
     */
    @Override
    public void arrangeBoard(LotroGame game) {
        for (Seat seat : Seat.values())
            bindAliases(seat);

        for (Seat seat : Seat.values()) {
            Map<SetupZone, List<String>> zones = _def.setup.zones.getOrDefault(seat, Map.of());
            for (Map.Entry<SetupZone, List<String>> entry : zones.entrySet()) {
                if (entry.getKey() == SetupZone.DECK)
                    continue;
                for (String alias : entry.getValue())
                    place(Card(alias), seat, entry.getKey(), false);
            }
            //Cards left in the deck are already in the order the scenario declares them.  The ones it lists for the
            // deck go on top of those, first listed topmost.
            List<String> deck = zones.getOrDefault(SetupZone.DECK, List.of());
            for (String alias : deck.reversed())
                MoveCardsToTopOfDeck(Card(alias));
        }

        for (Map.Entry<String, String> entry : _def.setup.attach.entrySet())
            AttachCardsTo(Card(entry.getValue()), Card(entry.getKey()));
        for (Map.Entry<String, String> entry : _def.setup.stack.entrySet())
            StackCardsOn(Card(entry.getValue()), Card(entry.getKey()));
        for (Map.Entry<String, Integer> entry : _def.setup.wounds.entrySet())
            AddWoundsToChar(Card(entry.getKey()), entry.getValue());
        for (Map.Entry<String, Integer> entry : _def.setup.tokens.entrySet())
            AddTokensToCard(Card(entry.getKey()), entry.getValue());

        //The burden that was bid for the first turn does not count; the scenario says how many there are.
        RemoveBurdens(GetBurdens());
        AddBurdens(_def.setup.burdens);
        if (_def.setup.threats > 0)
            AddThreats(_def.setup.threats);
    }

    private void bindAliases(Seat seat) {
        String player = Player(seat);
        String ringBearer = _def.ringBearer.get(seat);
        String ring = _def.ring.get(seat);
        bind(player, ringBearer, (PhysicalCardImpl) _gameState.getRingBearer(player));
        if (ring != null)
            bind(player, ring, (PhysicalCardImpl) _gameState.getRing(player));

        //Copies of the same card are told apart by position: the first copy in the deck is the first alias declared
        for (PhysicalCard card : _gameState.getDeck(player)) {
            String match = null;
            for (CardDef candidate : _def.cards.values()) {
                if (candidate.owner() == seat && !_aliases.containsKey(candidate.alias())
                        && !candidate.alias().equals(ringBearer) && !candidate.alias().equals(ring)
                        && _format.applyErrata(candidate.blueprintId()).equals(card.getBlueprintId())) {
                    match = candidate.alias();
                    break;
                }
            }
            if (match == null)
                _problems.add("A " + card.getBlueprintId() + " in the " + seat.key() + " deck matches no card alias");
            else
                bind(player, match, (PhysicalCardImpl) card);
        }
    }

    private void bind(String player, String alias, PhysicalCardImpl card) {
        _aliases.put(alias, card);
        Cards.get(player).put(alias, card);
        _gameState.labelCard(alias, card);
    }

    private void place(PhysicalCardImpl card, Seat seat, SetupZone zone, boolean bottom) {
        switch (zone) {
            case CHARACTERS -> {
                //A Ring-bearer is already on the table, with the Ring if there is one
                if (card.getZone() != Zone.FREE_CHARACTERS)
                    MoveCardToZone(card, Zone.FREE_CHARACTERS);
            }
            case SUPPORT -> MoveCardToZone(card, Zone.SUPPORT);
            case MINIONS -> MoveCardToZone(card, Zone.SHADOW_CHARACTERS);
            case HAND -> MoveCardToZone(card, Zone.HAND);
            case DISCARD -> MoveCardToZone(card, Zone.DISCARD);
            case DEAD_PILE -> MoveCardToZone(card, Zone.DEAD);
            case REMOVED -> MoveCardToZone(card, Zone.REMOVED);
            case DECK -> {
                if (bottom)
                    MoveCardsToBottomOfDeck(card);
                else
                    MoveCardsToTopOfDeck(card);
            }
        }
        if (card.getOwner() != null && !card.getOwner().equals(Player(seat)))
            _problems.add("'" + AliasOf(card) + "' was put in a " + seat.key() + " zone but belongs to the other seat");
    }

    /**
     * Carries out one of the scenario's direct changes to the board.  Only do this while the game is waiting on a
     * decision, and bear in mind that the decision it is waiting on was worked out before the change.
     */
    public void Apply(DoOp op) {
        try {
            switch (op.kind()) {
                case MOVE -> {
                    ScenarioParser.ZoneRef ref = ScenarioParser.parseZoneRef(_def, op.target(), "do");
                    place(Card(op.card()), ref.seat(), ref.zone(), ref.bottom());
                }
                case ATTACH -> AttachCardsTo(Card(op.target()), Card(op.card()));
                case STACK -> StackCardsOn(Card(op.target()), Card(op.card()));
                case WOUND -> AddWoundsToChar(Card(op.card()), op.amount());
                case HEAL -> RemoveWoundsFromChar(Card(op.card()), Math.min(op.amount(), GetWoundsOn(Card(op.card()))));
                case TOKENS -> AddTokensToCard(Card(op.card()), op.amount());
                case TWILIGHT -> SetTwilight(Math.max(0, op.relative() ? GetTwilight() + op.amount() : op.amount()));
                case BURDENS -> setTotal(GetBurdens(), op, this::AddBurdens, this::RemoveBurdens);
                case THREATS -> setTotal(GetThreats(), op, this::AddThreats, this::RemoveThreats);
            }
        } catch (ScenarioFormatException exp) {
            _problems.add(exp.getMessage());
        }
    }

    private static void setTotal(int current, DoOp op, java.util.function.IntConsumer add, java.util.function.IntConsumer remove) {
        int wanted = Math.max(0, op.relative() ? current + op.amount() : op.amount());
        if (wanted > current)
            add.accept(wanted - current);
        else if (wanted < current)
            remove.accept(current - wanted);
    }

    /**
     * Checks card-definition requirements (the same ones cards use for "requires") against the game as it stands.
     * @param requirementsJson A JSON array of requirement objects; null or empty always holds.
     * @param player Who "you" refers to in the requirements.
     */
    public boolean Holds(String requirementsJson, String player) {
        if (requirementsJson == null)
            return true;
        Requirement[] requirements = _requirements.computeIfAbsent(requirementsJson, ScenarioTable::buildRequirements);
        ActionContext context = new DefaultActionContext(player, _game, _rulesCards.get(SeatOf(player)), null, null);
        for (Requirement requirement : requirements)
            if (!requirement.accepts(context))
                return false;
        return true;
    }

    /**
     * @throws IllegalArgumentException if the JSON does not describe requirements the card builder understands.
     */
    public static Requirement[] buildRequirements(String requirementsJson) {
        try {
            JSONArray array = (JSONArray) new JSONParser().parse(requirementsJson);
            JSONObject[] objects = new JSONObject[array.size()];
            for (int i = 0; i < objects.length; i++)
                objects[i] = (JSONObject) array.get(i);
            return BUILDER.getRequirementFactory().getRequirements(objects, BUILDER);
        } catch (InvalidCardDefinitionException | ParseException exp) {
            throw new IllegalArgumentException(exp.getMessage(), exp);
        }
    }

    /**
     * @throws IllegalArgumentException if the JSON does not describe effects the card builder understands.
     */
    public static void checkRules(String effectsJson) {
        try {
            JSONObject definition = new JSONObject();
            definition.put("title", "Scenario Rules");
            definition.put("type", "metasite");
            definition.put("effects", new JSONParser().parse(effectsJson));
            BUILDER.buildFromJson(RULES_BLUEPRINT_ID, definition);
        } catch (InvalidCardDefinitionException | ParseException exp) {
            throw new IllegalArgumentException(exp.getMessage(), exp);
        }
    }
}
