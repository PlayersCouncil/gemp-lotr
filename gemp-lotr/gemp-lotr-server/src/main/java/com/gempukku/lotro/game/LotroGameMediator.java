package com.gempukku.lotro.game;

import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LotroGameMediator {
    private static final Logger LOG = LogManager.getLogger(LotroGameMediator.class);

    private final Map<String, GameCommunicationChannel> _communicationChannels = Collections.synchronizedMap(new HashMap<>());
    private final DefaultUserFeedback _userFeedback;
    private final DefaultLotroGame _lotroGame;
    private final Map<String, Integer> _playerClocks = new HashMap<>();
    private final Map<String, Long> _decisionQuerySentTimes = new HashMap<>();
    private final Set<String> _playersPlaying = new HashSet<>();
    private final Map<String, LotroDeck> _playerDecks = new HashMap<>();

    private final String _gameId;

    private GameTimer _timeSettings;
    private final boolean _allowSpectators;
    private final boolean _cancellable;
    private final boolean _showInGameHall;

    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
    private int _channelNextIndex = 0;
    private volatile boolean _destroyed;

    public LotroGameMediator(String gameId, LotroFormat lotroFormat, LotroGameParticipant[] participants, LotroCardBlueprintLibrary library,
                             GameTimer gameTimer, boolean allowSpectators, boolean cancellable, boolean showInGameHall,
                             String tournamentName) {
        _gameId = gameId;
        _timeSettings = gameTimer;
        _allowSpectators = allowSpectators;
        _cancellable = cancellable;
        this._showInGameHall = showInGameHall;
        if (participants.length < 1)
            throw new IllegalArgumentException("Game can't have less than one participant");

        for (LotroGameParticipant participant : participants) {
            String participantId = participant.getPlayerId();
            _playerDecks.put(participantId, participant.getDeck());
            _playerClocks.put(participantId, 0);
            _playersPlaying.add(participantId);
        }

        _userFeedback = new DefaultUserFeedback();
        _lotroGame = new DefaultLotroGame(lotroFormat, _playerDecks, _userFeedback, library, _timeSettings.toString(), _allowSpectators,
                tournamentName);
        _userFeedback.setGame(_lotroGame);
    }

    public boolean isVisibleToUser(String username) {
        return !_showInGameHall || _playersPlaying.contains(username);
    }

    public boolean isDestroyed() {
        return _destroyed;
    }

    public void destroy() {
        _destroyed = true;
    }

    public String getGameId() {
        return _gameId;
    }

    public DefaultLotroGame getGame() { return _lotroGame; }

    public boolean isAllowSpectators() {
        return _allowSpectators;
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        if (_playersPlaying.contains(playerId)) {
            _lotroGame.setPlayerAutoPassSettings(playerId, phases);
        }
    }

    public void sendMessageToPlayers(String message) {
        _lotroGame.getGameState().sendMessage(message);
    }

    public void addGameStateListener(String playerId, GameStateListener listener) {
        _lotroGame.addGameStateListener(playerId, listener);
    }

    public void removeGameStateListener(GameStateListener listener) {
        _lotroGame.removeGameStateListener(listener);
    }

    public void addGameResultListener(GameResultListener listener) {
        _lotroGame.addGameResultListener(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _lotroGame.removeGameResultListener(listener);
    }

    public String getWinner() {
        return _lotroGame.getWinnerPlayerId();
    }

    public List<String> getPlayersPlaying() {
        return new LinkedList<>(_playersPlaying);
    }

    public String getGameStatus() {
        if (_lotroGame.isCancelled())
            return "Cancelled";
        if (_lotroGame.isFinished())
            return "Finished";
        final Phase currentPhase = _lotroGame.getGameState().getCurrentPhase();
        if (currentPhase == Phase.PLAY_STARTING_FELLOWSHIP || currentPhase == Phase.PUT_RING_BEARER)
            return "Preparation";
        return "At sites: " + getPlayerPositions();
    }

    public boolean isFinished() {
        return _lotroGame.isFinished();
    }

    public String produceCardInfo(Player player, int cardId) {
        _readLock.lock();
        try {
            PhysicalCard card = _lotroGame.getGameState().findCardById(cardId);
            if (card == null || card.getZone() == null)
                return null;

            if (card.getZone().isInPlay() || card.getZone() == Zone.HAND) {
                StringBuilder sb = new StringBuilder();

                if (card.getZone() == Zone.HAND)
                    sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
                else if (!Filters.hasActive(_lotroGame, card))
                    sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");

                sb.append("<b>Affecting card:</b>");
                Collection<Modifier> modifiers = _lotroGame.getModifiersQuerying().getModifiersAffecting(_lotroGame, card);
                for (Modifier modifier : modifiers) {
                    PhysicalCard source = modifier.getSource();
                    if (source != null) {
                        sb.append("<br><b>")
                                .append(GameUtils.getCardLink(source))
                                .append(":</b> ")
                                .append(modifier.getText(_lotroGame, card));
                    }
                    else {
                        sb.append("<br><b><i>System</i>:</b> ")
                                .append(modifier.getText(_lotroGame, card));
                    }
                }
                if (modifiers.size() == 0) {
                    sb.append("<br><i>nothing</i>");
                }

                if (card.getZone().isInPlay() && card.getBlueprint().getCardType() == CardType.SITE) {
                    sb.append("<br><b>Owner:</b> ")
                            .append(card.getOwner());
                }

                Map<Token, Integer> map = _lotroGame.getGameState().getTokens(card);
                if (map != null && map.size() > 0) {
                    sb.append("<br><b>Tokens:</b>");
                    for (Map.Entry<Token, Integer> tokenIntegerEntry : map.entrySet()) {
                        sb.append("<br>")
                                .append(tokenIntegerEntry.getKey().toString())
                                .append(": ")
                                .append(tokenIntegerEntry.getValue());
                    }
                }

                List<PhysicalCard> stackedCards = _lotroGame.getGameState().getStackedCards(card);
                if (stackedCards != null && stackedCards.size() > 0) {
                    sb.append("<br><b>Stacked cards:</b>")
                            .append("<br>")
                            .append(GameUtils.getAppendedNames(stackedCards));
                }

                final String extraDisplayableInformation = card.getBlueprint().getDisplayableInformation(card);
                if (extraDisplayableInformation != null) {
                    sb.append("<br><b>Extra information:</b>")
                            .append("<br>")
                            .append(extraDisplayableInformation);
                }

                sb.append("<br><br><b>Effective stats:</b>");
                try {
                    PhysicalCard target = card.getAttachedTo();
                    int twilightCost = _lotroGame.getModifiersQuerying().getTwilightCostToPlay(_lotroGame, card, target, 0, false);
                    sb.append("<br><b>Twilight cost:</b> ")
                            .append(twilightCost);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int strength = _lotroGame.getModifiersQuerying().getStrength(_lotroGame, card);
                    sb.append("<br><b>Strength:</b> ")
                            .append(strength);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int vitality = _lotroGame.getModifiersQuerying().getVitality(_lotroGame, card);
                    sb.append("<br><b>Vitality:</b> ")
                            .append(vitality);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int resistance = _lotroGame.getModifiersQuerying().getResistance(_lotroGame, card);
                    sb.append("<br><b>Resistance:</b> ")
                            .append(resistance);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int siteNumber = _lotroGame.getModifiersQuerying().getMinionSiteNumber(_lotroGame, card);
                    sb.append("<br><b>Site number:</b> ")
                            .append(siteNumber);
                } catch (UnsupportedOperationException ignored) {
                }

                StringBuilder keywords = new StringBuilder();
                for (Keyword keyword : Keyword.values()) {
                    if (keyword.isInfoDisplayable()) {
                        if (keyword.isMultiples()) {
                            int count = _lotroGame.getModifiersQuerying().getKeywordCount(_lotroGame, card, keyword);
                            if (count > 0)
                                keywords.append(keyword.getHumanReadable())
                                        .append(" +")
                                        .append(count)
                                        .append(", ");
                        } else {
                            if (_lotroGame.getModifiersQuerying().hasKeyword(_lotroGame, card, keyword))
                                keywords.append(keyword.getHumanReadable())
                                        .append(", ");
                        }
                    }
                }
                if (keywords.length() > 0) {
                    sb.append("<br><b>Keywords:</b> ")
                            .append(keywords.substring(0, keywords.length() - 2));
                }

                if(TrulyAwfulTengwarHackMap.containsKey(card.getBlueprintId())) {
                    sb.append("<br><br><b>Tengwar Translation: </b><br>")
                            .append(TrulyAwfulTengwarHackMap.get(card.getBlueprintId()));
                }

                return sb.toString();
            } else {
                return null;
            }
        } finally {
            _readLock.unlock();
        }
    }

    public static final HashMap<String, String> TrulyAwfulTengwarHackMap = new HashMap<>() {{
        put("1_1T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_One_Ring,_Isildur's_Bane_(1R1)\">•The One Ring, Isildur's Bane (1R1)</a><br><br><b>Response:</b> If bearer is about to take a wound, he wears The One Ring until the regroup phase. <br>While wearing The One Ring, each time the Ring-bearer is about to take a wound, add two burdens instead.</i>");
        put("1_13T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gimli,_Son_of_Gloin_(1R13)\">•Gimli, Son of Gloin (1R13)</a><br><br><b>Damage +1</b>. <br><b>Skirmish:</b> Exert Gimli to make him strength +2.</i>");
        put("1_14T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gimli's_Battle_Axe_(1R14)\">•Gimli's Battle Axe (1R14)</a><br><br>Bearer must be Gimli. <br>He is <b>damage +1</b>. <br>Each time Gimli wins a skirmish, you may wound an Orc.</i>");
        put("1_30T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Arwen,_Daughter_of_Elrond_(1R30)\">•Arwen, Daughter of Elrond (1R30)</a><br><br><b>Ranger.</b> <br>While skirmishing a Nazgûl, Arwen is strength +3.</i>");
        put("1_50T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Legolas,_Greenleaf_(1R50)\">•Legolas, Greenleaf (1R50)</a><br><br><b>Archer.</b> <br><b>Archery:</b> Exert Legolas to wound a minion; Legolas does not add to the fellowship archery total.</i>");
        put("1_72T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gandalf,_Friend_of_the_Shirefolk_(1R72)\">•Gandalf, Friend of the Shirefolk (1R72)</a><br><br>Gandalf is strength +1 for each of these races you can spot in the fellowship: Hobbit, Dwarf, Elf, and Man.</i>");
        put("1_83T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Servant_of_the_Secret_Fire_(1R83)\">Servant of the Secret Fire (1R83)</a><br><br><b>Spell.</b> <br><b>Skirmish:</b> Spot Gandalf to make a minion strength -3.</i>");
        put("1_89T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Aragorn,_Ranger_of_the_North_(1R89)\">•Aragorn, Ranger of the North (1R89)</a><br><br><b>Ranger.</b> <br><b>Maneuver:</b> Exert Aragorn to make him <b>defender +1</b> until the regroup phase.</i>");
        put("1_96T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Boromir,_Lord_of_Gondor_(1R96)\">•Boromir, Lord of Gondor (1R96)</a><br><br><b>Ranger.</b> <br>Boromir is not overwhelmed unless his strength is tripled.</i>");
        put("1_114T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Saga_of_Elendil_(1R114)\">•The Saga of Elendil (1R114)</a><br><br><b>Tale.</b> Bearer must be a [GONDOR] companion. <br><b>Skirmish:</b> Discard this condition to make bearer strength +2.</i>");
        put("1_127T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Lurtz,_Servant_of_Isengard_(1R127)\">•Lurtz, Servant of Isengard (1R127)</a><br><br><b>Archer.</b> <b>Damage +1</b>. <br><b>Maneuver:</b> Spot another Uruk-hai to make Lurtz <b>fierce</b> until the regroup phase.</i>");
        put("1_165T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Cave_Troll_of_Moria,_Scourge_of_the_Black_Pit_(1R165)\">•Cave Troll of Moria, Scourge of the Black Pit (1R165)</a><br><br><b>Damage +1.</b> <b>Fierce.</b> To play, spot a [MORIA] Orc. <br>At an underground site, Cave Troll of Moria’s twilight cost is -3.</i>");
        put("1_231T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Ulaire_Enquea,_Lieutenant_of_Morgul_(1U231)\">•Ulaire Enquea, Lieutenant of Morgul (1U231)</a><br><br><b>Fierce.</b> <br><b>Maneuver:</b> Spot 6 companions (or 5 burdens) and exert Úlairë Enquëa to wound a companion (except the Ring-bearer).</i>");
        put("1_237T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Witch-king,_Lord_of_Angmar_(1R237)\">•The Witch-king, Lord of Angmar (1R237)</a><br><br><b>Fierce.</b> <br>For each other Nazgûl you can spot, The Witch-king is strength +2.</i>");
        put("1_256T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Morgul_Hunter_(1R256)\">Morgul Hunter (1R256)</a><br><br>For each companion you can spot, this minion is strength +1.</i>");
        put("2_52T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Balrog,_Flame_of_Udun_(2R52)\">•The Balrog, Flame of Udun (2R52)</a><br><br><b>Damage +1.</b> <b>Fierce.</b> To play, spot a [MORIA] Orc. <br>Discard The Balrog if not underground. <br><b>Shadow:</b> Exert The Balrog and remove (2) to play a [MORIA] Orc from your discard pile.</i>");
        put("2_102T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Frodo,_Reluctant_Adventurer_(2C102)\">•Frodo, Reluctant Adventurer (2C102)</a><br><br><b>Ring-bound.</b> <b>Ring-bearer</b> (resistance 10). <br>The cost of each artifact, possession, and [SHIRE] tale played on Frodo is -1.</i>");
        put("2_105T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Mithril-coat_(2R105)\">•Mithril-coat (2R105)</a><br><br>Bearer must be Frodo. <br>The minion archery total is -1. <br>Each minion skirmishing Frodo does not gain strength bonuses from weapons and loses all damage bonuses.</i>");
        put("4_1T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_One_Ring,_Answer_To_All_Riddles_(4R1)\">•The One Ring, Answer To All Riddles (4R1)</a><br><br>While wearing The One Ring, the Ring-bearer is strength +2, and each time he is about to take a wound in a skirmish, add a burden instead.<br><b>Skirmish:</b> Add a burden to wear The One Ring until the regroup phase.</i>");
        put("4_19T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Hides_(4R19)\">Hides (4R19)</a><br><br>When you play this possession, you may draw a card. <br><b>Response: </b>If a [DUNLAND] Man is about to take a wound, remove (2) or discard this possession to prevent that wound.</i>");
        put("4_73T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Legolas,_Dauntless_Hunter_(4R73)\">•Legolas, Dauntless Hunter (4R73)</a><br><br><b>Archer.</b> <br>The twilight cost of each Shadow event and Shadow condition is +1 for each unbound Hobbit you can spot.</i>");
        put("4_90T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gandalf,_The_White_Wizard_(4C90)\">•Gandalf, The White Wizard (4C90)</a><br><br>While you can spot 3 twilight tokens, Gandalf is strength +3.</i>");
        put("4_100T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Shadowfax_(4R100)\">•Shadowfax (4R100)</a><br><br>Bearer must be Gandalf. Discard any hand weapon he bears.<br>Gandalf may not bear a hand weapon.<br>At the start of each skirmish involving Gandalf, each minion skirmishing him must exert.</i>");
        put("4_103T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Treebeard,_Earthborn_(4R103)\">•Treebeard, Earthborn (4R103)</a><br><br><b>Unhasty.</b><br><b>Response:</b> If an unbound Hobbit is about to be discarded, stack him here instead. <br><b>Fellowship:</b> Exert Treebeard and add (1) to play an unbound Hobbit stacked here.</i>");
        put("4_154T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Grima,_Wormtongue_(4R154)\">•Grima, Wormtongue (4R154)</a><br><br><b>Maneuver:</b> Exert Gríma and spot an unbound companion bearing 3 or more cards to return each Free Peoples card that companion bears to its owner’s hand.</i>");
        put("4_173T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Saruman,_Black_Traitor_(4R173)\">•Saruman, Black Traitor (4R173)</a><br><br>Saruman may not take wounds during the archery phase and may not be assigned to a skirmish.<br>When you play Saruman, you may discard a condition.<br><b>Shadow:</b> Exert Saruman to play Saruman’s Staff from your discard pile.</i>");
        put("4_176T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Ugluk,_Servant_of_Saruman_(4R176)\">•Ugluk, Servant of Saruman (4R176)</a><br><br><b>Tracker.</b> <b>Fierce.</b><br>The roaming penalty for each [ISENGARD] tracker you play is -2.<br>While you can spot 2 [ISENGARD] trackers, Uglúk is strength +3.<br>While you can spot 3 [ISENGARD] trackers, Uglúk is <b>damage +1.</b></i>");
        put("4_219T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Desert_Lord_(4R219)\">•Desert Lord (4R219)</a><br><br><b>Southron.</b> <b>Archer.</b> <br><b>Archery:</b> Exert Desert Lord to exert a companion (except the Ring-bearer); Desert Lord does not add to the minion archery total.</i>");
        put("4_225T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Easterling_Captain_(4R225)\">•Easterling Captain (4R225)</a><br><br><b>Easterling.</b> <b>Fierce.</b><br><b>Skirmish:</b> Spot 2 burdens and remove (2) to make an Easterling strength +2. <br><b>Skirmish:</b> Spot 4 burdens and remove (2) to make an Easterling strength +3. <br><b>Skirmish:</b> Spot 6 burdens and remove (2) to make an Easterling strength +4.</i>");
        put("4_289T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Simbelmyne_(4R289)\">Simbelmyne (4R289)</a><br><br><b>Fellowship:</b> Spot 2 [ROHAN] Men (or 1 <b>valiant</b> [ROHAN] Man) to play a [ROHAN] character or [ROHAN] possession from your draw deck.</i>");
        put("4_301T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Frodo,_Courteous_Halfling_(4R301)\">•Frodo, Courteous Halfling (4R301)</a><br><br><b>Ring-bearer (resistance 10).</b><br>While you can spot 3 unbound companions, Shadow cards may not discard cards from your hand or from the top of your draw deck.</i>");
        put("4_364T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Aragorn,_Wingfoot_(4P364)\">•Aragorn, Wingfoot (4P364)</a><br><br><b>Ranger.</b><br>Each time the fellowship moves, you may wound a minion for each unbound Hobbit you spot.</i>");
        put("5_25T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gollum,_Stinker_(5R25)\">•Gollum, Stinker (5R25)</a><br><br>Gollum is strength +1 for each burden. Each time Gollum wins a skirmish, you may add a burden.</i>");
        put("5_29T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Smeagol,_Slinker_(5R29)\">•Smeagol, Slinker (5R29)</a><br><br><b>Ring-bound.</b> To play, add a burden. <br><b>Skirmish:</b> Add a burden to make Sméagol strength +2 and take no wounds.</i>");
        put("5_100T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Grishnakh,_Orc_Captain_(5R100)\">•Grishnakh, Orc Captain (5R100)</a><br><br><b>Tracker.</b><br>The site number of each [SAURON] Orc is -3. <br><b>Shadow:</b> Exert Grishnákh twice and spot another [SAURON] Orc to draw 3 cards. The Free Peoples player may add 2 burdens to prevent this.</i>");
        put("5_116T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Sting,_Baggins_Heirloom_(5R116)\">•Sting, Baggins Heirloom (5R116)</a><br><br>Bearer must be Frodo.<br><b>Skirmish:</b> Exert Frodo to make Sméagol strength +2 or Gollum strength -2.</i>");
        put("6_88T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Ulaire_Toldea,_Winged_Sentry_(6R88)\">•Ulaire Toldea, Winged Sentry (6R88)</a><br><br><b>Fierce.</b> Each time Úlairë Toldëa wins a skirmish, the Free Peoples player must exert a companion or add a burden.</i>");
        put("7_2T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_One_Ring,_Such_a_Weight_to_Carry_(7R2)\">•The One Ring, Such a Weight to Carry (7R2)</a><br><br><b>Maneuver:</b> Add a burden to wear The One Ring until the regroup phase. <br>While wearing The One Ring, each time the Ring-bearer is about to take a wound, add a burden instead.</i>");
        put("7_79T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Anduril,_Flame_of_the_West_(7R79)\">•Anduril, Flame of the West (7R79)</a><br><br>Bearer must be Aragorn. Discard other weapons he bears. He is <b>damage +1</b> and cannot bear other weapons. <br><b>Fellowship</b> or <b>Regroup:</b> If the fellowship is at any site 2 or any site 5, play the fellowship’s next site (replacing opponent’s site if necessary).</i>");
        put("7_211T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Ulaire_Cantea,_Faster_Than_Winds_(7R211)\">•Ulaire Cantea, Faster Than Winds (7R211)</a><br><br><b>Fierce.</b> <br>When you play Úlairë Cantëa, add a threat for each companion over 4. <br><b>Maneuver:</b> Remove 2 threats and spot another [WRAITH] minion to discard a possession.</i>");
        put("7_221T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Witch-king,_Morgul_King_(7R221)\">•The Witch-king, Morgul King (7R221)</a><br><br><b>Fierce.</b> <br>When you play The Witch-king, you may remove a threat to take a [WRAITH] card into hand from your discard pile. <br>The Ring-bearer cannot take threat wounds.</i>");
        put("7_227T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Eomer,_Skilled_Tactician_(7R227)\">•Eomer, Skilled Tactician (7R227)</a><br><br><b>Valiant.</b> While you can spot a [ROHAN] Man, Éomer’s twilight cost is –1. <br><b>Fellowship:</b> Play a [ROHAN] companion to take a [ROHAN] possession or [ROHAN] skirmish event into hand from your discard pile.</i>");
        put("7_321T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Merry,_Swordthain_(7R321)\">•Merry, Swordthain (7R321)</a><br><br><b>Skirmish:</b> If Merry is not assigned to a skirmish, return him to your hand to play up to 2 [ROHAN] possessions from your discard pile.</i>");
        put("7_324T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Pippin,_Wearer_of_Black_and_Silver_(7R324)\">•Pippin, Wearer of Black and Silver (7R324)</a><br><br><b>Skirmish:</b> If Pippin is not assigned to a skirmish, return him to your hand to wound a roaming minion twice.</i>");
        put("8_15T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gandalf,_Leader_of_Men_(8R15)\">•Gandalf, Leader of Men (8R15)</a><br><br>When Gandalf is in your starting fellowship, his twilight cost is -2.</i>");
        put("8_25T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Shelob,_Eater_of_Light_(8R25)\">•Shelob, Eater of Light (8R25)</a><br><br><b>Fierce.</b> <br>When you play Shelob, you may play a [GOLLUM] possession from your draw deck. <br>Shelob is strength +3 for each minion stacked on a [GOLLUM] possession.</i>");
        put("8_38T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/King_of_the_Dead,_Oathbreaker_(8R38)\">•King of the Dead, Oathbreaker (8R38)</a><br><br><b>Enduring.</b> To play, spot a [GONDOR] Wraith and add 2 threats. <br><b>Response:</b> If Aragorn is about to take a wound in a skirmish, exert King of the Dead to prevent that wound.</i>");
        put("8_51T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Castamir_of_Umbar_(8R51)\">•Castamir of Umbar (8R51)</a><br><br><b>Corsair.</b> <b>Enduring.</b> <b>Fierce.</b> <br><b>Shadow:</b> Exert Castamir of Umbar and play a corsair to add 2 [RAIDER] tokens to a card that already has a [RAIDER] token on it.</i>");
        put("8_57T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Corsair_Marauder_(8R57)\">Corsair Marauder (8R57)</a><br><br><b>Corsair.</b> <br>When you play this minion, if you can spot another corsair, you may discard a possession to add 2 [RAIDER] tokens to a card that already has a [RAIDER] token on it.</i>");
        put("8_103T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Grond,_Hammer_of_the_Underworld_(8R103)\">•Grond, Hammer of the Underworld (8R103)</a><br><br><b>Engine.</b> <b>Shadow:</b> Play a [SAURON] minion to add a [SAURON] token here. <br><b>Regroup:</b> Remove X [SAURON] tokens here to discard a Free Peoples card (except a companion) with a twilight cost of X. Discard a [SAURON] minion or this possession.</i>");
        put("10_6T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Arwen,_Queen_of_Elves_and_Men_(10R6)\">•Arwen, Queen of Elves and Men (10R6)</a><br><br>Each minion skirmishing Arwen is strength –2 for each wounded minion you can spot.</i>");
        put("10_9T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Elrond,_Venerable_Lord_(10R9)\">•Elrond, Venerable Lord (10R9)</a><br><br>To play, spot an Elf. <br>At the start of each skirmish involving Elrond, you may reveal the top card of your draw deck. If it is an [ELVEN] card, you may heal another Elf.</i>");
        put("10_25T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Aragorn,_Elessar_Telcontar_(10R25)\">•Aragorn, Elessar Telcontar (10R25)</a><br><br>When you play Aragorn, you may heal another companion. At the start of each fellowship phase, you may exert a companion of one culture to heal a companion of another culture.</i>");
        put("10_88T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gothmog,_Lieutenant_of_Morgul_(10R88)\">•Gothmog, Lieutenant of Morgul (10R88)</a><br><br><b>Besieger.</b> <br>When you play Gothmog, the Free Peoples player must wound a companion for each site you control. <br>While you control a site, Gothmog is <b>fierce.</b></i>");
        put("10_122T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Sam,_Great_Elf_Warrior_(10P122)\">•Sam, Great Elf Warrior (10P122)</a><br><br><b>Ring-bound.</b> <br>Sam is strength +1 for each [SHIRE] companion you can spot. <br><b>Response:</b> If Frodo dies, make Sam the Ring-bearer (resistance 5).</i>");
        put("11_1T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_One_Ring,_The_Ring_of_Rings_(11R1)\">•The One Ring, The Ring of Rings (11R1)</a><br><br><b>Response:</b> If the Ring-bearer is about to take a wound, he or she wears The One Ring until the regroup phase. While the Ring-bearer is wearing The One Ring, each time he or she is about to take a wound, add a burden instead.</i>");
        put("11_33T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gandalf,_Leader_of_the_Company_(11S33)\">•Gandalf, Leader of the Company (11S33)</a><br><br>While Gandalf is in region 1, each other companion is strength +2. While Gandalf is in region 2, each companion is strength +1. While Gandalf is in region 3, he is strength +2.</i>");
        put("11_54T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Aragorn,_Strider_(11R54)\">•Aragorn, Strider (11R54)</a><br><br><b>Ranger.</b> Each time the fellowship moves, add (1).</i>");
        put("11_57T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Boromir,_Hero_of_Osgiliath_(11R57)\">•Boromir, Hero of Osgiliath (11R57)</a><br><br><b>Knight.</b> Each time Boromir wins a skirmish, you may heal a [GONDOR] companion.</i>");
        put("11_123T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Goblin_Hordes_(11R123)\">Goblin Hordes (11R123)</a><br><br>To play, spot an [ORC] minion. <br>Each time the fellowship moves from an underground site, you may take an [ORC] minion from your discard pile into hand.</i>");
        put("11_226T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Witch-king,_Captain_of_the_Nine_Riders_(11R226)\">•The Witch-king, Captain of the Nine Riders (11R226)</a><br><br><b>Fierce.</b> <b>Toil 2.</b> (For each [WRAITH] character you exert when playing this, its twilight cost is –2.) <b>Muster.</b> (At the start of the regroup phase, you may discard a card from hand to draw a card.)</i>");
        put("12_17T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Elrond,_Witness_to_History_(12R17)\">•Elrond, Witness to History (12R17)</a><br><br>To play, spot an Elf. <br><b>Skirmish:</b> If Elrond is skirmishing a minion, exert him to place an [ELVEN] card from your discard pile on top of your draw deck.</i>");
        put("12_35T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Watch_and_Wait_(12R35)\">Watch and Wait (12R35)</a><br><br>To play, spot a [GANDALF] companion. <br>Bearer must be a companion (except the Ring-bearer). Limit 1 per bearer. <br>Each time bearer wins a skirmish, you may remove a burden.</i>");
        put("12_54T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Saruman,_Of_Many_Colours_(12R54)\">•Saruman, Of Many Colours (12R54)</a><br><br><b>Damage +1. Fierce. Lurker.</b> <i>(Skirmishes involving lurker minions must be resolved after any others.)</i> When you play Saruman, name a culture. Each companion of the named culture is strength –1.</i>");
        put("12_73T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Mouth_of_Sauron,_Messenger_of_Mordor_(12S73)\">•The Mouth of Sauron, Messenger of Mordor (12S73)</a><br><br><b>Maneuver:</b> Exert The Mouth of Sauron to play a [MEN] condition or [MEN] possession from your draw deck.</i>");
        put("12_79T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/The_Balrog,_The_Terror_of_Khazad-dum_(12R79)\">•The Balrog, The Terror of Khazad-dum (12R79)</a><br><br><b>Damage +1.</b> <br>While The Balrog is at an underground site, it is <b>fierce</b> and cannot take wounds or be exerted.</i>");
        put("12_174T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Ulaire_Cantea,_Black_Assassin_(12R174)\">•Ulaire Cantea, Black Assassin (12R174)</a><br><br><b>Fierce. <br>Assignment:</b> Assign Úlairë Cantëa to a companion who has resistance 0. <br><b>Skirmish:</b> Spot 6 companions and another [WRAITH] card to kill a companion Úlairë Cantëa is skirmishing.</i>");
        put("13_5T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Gimli,_Lord_of_the_Glittering_Caves_(13R5)\">•Gimli, Lord of the Glittering Caves (13R5)</a><br><br><b>Damage +1.</b><br>Gimli is strength +1 for each minion assigned to a skirmish.<br><b>Assignment:</b> Exert Gimli and spot a lurker minion to make that minion lose <b>lurker</b> until the regroup phase.</i>");
        put("13_46T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Deagol,_Fateful_Finder_(13R46)\">•Deagol, Fateful Finder (13R46)</a><br><br>To play, spot Sméagol.<br><b>Aid –</b> Add a burden.<br><b>Skirmish:</b> If bearer is not assigned to a skirmish, discard this from play to play an artifact or possession from your draw deck on bearer.</i>");
        put("13_112T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Orc_Crusher_(13R112)\">•Orc Crusher (13R112)</a><br><br>To play, spot an [ORC] Orc.<br>This minion is twilight cost –1 for each burden.<br>Each time a Ring-bound companion loses a skirmish involving an [ORC] minion, you may reveal this card from hand to add a burden.</i>");
        put("13_137T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Theoden,_The_Renowned_(13R137)\">•Theoden, The Renowned (13R137)</a><br><br>While you can spot Éowyn, Théoden is <b>defender +1</b>.<br>While you can spot Éomer, Théoden is <b>damage +1</b>.<br>While you can spot Théodred, the move limit is +1.</i>");
        put("13_156T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Sam,_Bearer_of_Great_Need_(13R156)\">•Sam, Bearer of Great Need (13R156)</a><br><br><b>Ring-bound.</b><br>Sam is resistance +1 for each Hobbit you can spot.<br><b>Regroup:</b> Exert Sam and transfer a follower he is bearing to your support area to discard a minion from play.</i>");
        put("13_174T", "<i><a target=\"_blank\" href=\"https://wiki.lotrtcgpc.net/wiki/Uruk_Rogue_(13R174)\">Uruk Rogue (13R174)</a><br><br><b>Damage +1.<br>Skirmish:</b> If no other minions are assigned to a skirmish, you may exert this minion twice to make it <b>fierce</b> and strength +4 until the regroup phase.</i>");

    }};

    public void startGame() {
        _writeLock.lock();
        try {
            _lotroGame.startGame();
            startClocksForUsersPendingDecision();
        } finally {
            _writeLock.unlock();
        }
    }

    public void cleanup() {
        _writeLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            Map<String, GameCommunicationChannel> channelsCopy = new HashMap<>(_communicationChannels);
            for (Map.Entry<String, GameCommunicationChannel> playerChannels : channelsCopy.entrySet()) {
                String playerId = playerChannels.getKey();
                // Channel is stale (user no longer connected to game, to save memory, we remove the channel
                // User can always reconnect and establish a new channel
                GameCommunicationChannel channel = playerChannels.getValue();
                if (currentTime > channel.getLastAccessed() + _timeSettings.maxSecondsPerDecision() * 1000L) {
                    _lotroGame.removeGameStateListener(channel);
                    _communicationChannels.remove(playerId);
                }
            }

            if (_lotroGame != null && _lotroGame.getWinnerPlayerId() == null) {
                for (Map.Entry<String, Long> playerDecision : new HashMap<>(_decisionQuerySentTimes).entrySet()) {
                    String player = playerDecision.getKey();
                    long decisionSent = playerDecision.getValue();
                    if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * 1000L) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _lotroGame.playerLost(player, "Player decision timed-out");
                    }
                }

                for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                    String player = playerClock.getKey();
                    if (_timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(player) < 0) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _lotroGame.playerLost(player, "Player run out of time");
                    }
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public void concede(Player player) {
        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_lotroGame.getWinnerPlayerId() == null && _playersPlaying.contains(playerId)) {
                addTimeSpentOnDecisionToUserClock(playerId);
                _lotroGame.playerLost(playerId, "Concession");
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public void cancel(Player player) {
        _lotroGame.getGameState().sendWarning(player.getName(), "You can't cancel this game");

        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_playersPlaying.contains(playerId))
                _lotroGame.requestCancel(playerId);
        } finally {
            _writeLock.unlock();
        }
    }

    public synchronized void playerAnswered(Player player, int channelNumber, int decisionId, String answer) throws SubscriptionConflictException, SubscriptionExpiredException {
        String playerName = player.getName();
        _writeLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(playerName);
                    if (awaitingDecision != null) {
                        if (awaitingDecision.getAwaitingDecisionId() == decisionId && !_lotroGame.isFinished()) {
                            try {
                                _userFeedback.participantDecided(playerName);
                                awaitingDecision.decisionMade(answer);

                                // Decision successfully made, add the time to user clock
                                addTimeSpentOnDecisionToUserClock(playerName);

                                _lotroGame.carryOutPendingActionsUntilDecisionNeeded();
                                startClocksForUsersPendingDecision();

                            } catch (DecisionResultInvalidException decisionResultInvalidException) {
                                // Participant provided wrong answer - send a warning message, and ask again for the same decision
                                _lotroGame.getGameState().sendWarning(playerName, decisionResultInvalidException.getWarningMessage());
                                _userFeedback.sendAwaitingDecision(playerName, awaitingDecision);
                            } catch (RuntimeException runtimeException) {
                                LOG.error("Error processing game decision", runtimeException);
                                _lotroGame.cancelGame();
                            }
                        }
                    }
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public GameCommunicationChannel getCommunicationChannel(Player player, int channelNumber) throws PrivateInformationException, SubscriptionConflictException, SubscriptionExpiredException {
        String playerName = player.getName();
        if (!player.hasType(Player.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    return communicationChannel;
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _readLock.unlock();
        }
    }

    public void processVisitor(GameCommunicationChannel communicationChannel, int channelNumber, String playerName, ParticipantCommunicationVisitor visitor) {
        _readLock.lock();
        try {
            visitor.visitChannelNumber(channelNumber);
            for (GameEvent gameEvent : communicationChannel.consumeGameEvents())
                visitor.visitGameEvent(gameEvent);

            Map<String, Integer> secondsLeft = new HashMap<>();
            for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                String playerClockName = playerClock.getKey();
                secondsLeft.put(playerClockName, _timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(playerClockName));

                if (_decisionQuerySentTimes.containsKey(playerClockName))
                    secondsLeft.put("decisionClock", getCurrentUserPendingTime(playerClockName));
            }
            visitor.visitClock(secondsLeft);
        } finally {
            _readLock.unlock();
        }
    }

    public void signupUserForGame(Player player, ParticipantCommunicationVisitor visitor) throws PrivateInformationException {
        String playerName = player.getName();
        if (!player.hasType(Player.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            int number = _channelNextIndex;
            _channelNextIndex++;

            GameCommunicationChannel participantCommunicationChannel = new GameCommunicationChannel(playerName, number, _lotroGame.getFormat());
            _communicationChannels.put(playerName, participantCommunicationChannel);

            _lotroGame.addGameStateListener(playerName, participantCommunicationChannel);

            visitor.visitChannelNumber(number);

            for (GameEvent gameEvent : participantCommunicationChannel.consumeGameEvents())
                visitor.visitGameEvent(gameEvent);

            Map<String, Integer> secondsLeft = new HashMap<>();
            for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                String playerId = playerClock.getKey();
                secondsLeft.put(playerId, _timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(playerId));

                if (_decisionQuerySentTimes.containsKey(playerId))
                    secondsLeft.put("decisionClock", getCurrentUserPendingTime(playerId));
            }

            visitor.visitClock(secondsLeft);
        } finally {
            _readLock.unlock();
        }
    }

    private void startClocksForUsersPendingDecision() {
        long currentTime = System.currentTimeMillis();
        Set<String> users = _userFeedback.getUsersPendingDecision();
        for (String user : users)
            _decisionQuerySentTimes.put(user, currentTime);
    }

    private void addTimeSpentOnDecisionToUserClock(String participantId) {
        Long queryTime = _decisionQuerySentTimes.remove(participantId);
        if (queryTime != null) {
            long currentTime = System.currentTimeMillis();
            long diffSec = (currentTime - queryTime) / 1000;
            _playerClocks.put(participantId, _playerClocks.get(participantId) + (int) diffSec);
        }
    }

    public int getCurrentUserPendingTime(String participantId) {
        if (!_decisionQuerySentTimes.containsKey(participantId))
            return 0;
        long queryTime = _decisionQuerySentTimes.get(participantId);
        long currentTime = System.currentTimeMillis();
        return (int) ((currentTime - queryTime) / 1000);
    }

    public GameTimer getTimeSettings() {
        return _timeSettings;
    }

    public Map<String, Integer> getPlayerClocks() { return Collections.unmodifiableMap(_playerClocks); }

    public String getPlayerPositions() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String player : _playersPlaying) {
            stringBuilder.append(_lotroGame.getGameState().getPlayerPosition(player) + ", ");
        }
        if (stringBuilder.length() > 0)
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());

        return stringBuilder.toString();
    }
}
