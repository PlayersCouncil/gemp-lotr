package com.gempukku.lotro.at;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static org.junit.Assert.*;

public abstract class AbstractAtTest {
    public static LotroCardBlueprintLibrary _cardLibrary;
    public static LotroFormatLibrary _formatLibrary;
    public static ProductLibrary _productLibrary;

    static {
        _cardLibrary = new LotroCardBlueprintLibrary();
        _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        _productLibrary = new ProductLibrary(_cardLibrary);
    }

    public DefaultLotroGame _game;
    public DefaultUserFeedback _userFeedback;
    public static final String P1 = "player1";
    public static final String P2 = "player2";

    public PhysicalCardImpl createCard(String owner, String blueprintId) throws CardNotFoundException {
        return (PhysicalCardImpl) _game.getGameState().createPhysicalCard(owner, _cardLibrary, blueprintId);
    }

    public void addCultureTokens(PhysicalCard card, Token token, int count) {
        _game.getGameState().addTokens(card, token, count);
    }

    public int getArcheryTotal(Side side) {
        return RuleUtils.calculateArcheryTotal(_game, side);
    }

    public boolean hasKeyword(PhysicalCard card, Keyword keyword) {
        return _game.getModifiersQuerying().hasKeyword(_game, card, keyword);
    }

    public boolean hasSignet(PhysicalCard card, Signet signet) {
        return _game.getModifiersQuerying().hasSignet(_game, card, signet);
    }

    public void addModifier(Modifier modifier) {
        _game.getModifiersEnvironment().addAlwaysOnModifier(modifier);
    }

    public PhysicalCard getRingBearer(String player) {
        return _game.getGameState().getRingBearer(player);
    }

    public PhysicalCard getRing(String player) {
        return _game.getGameState().getRing(player);
    }

    public PhysicalCard getSite(int siteNumber) {
        return _game.getGameState().getSite(siteNumber);
    }

    public PhysicalCard addToZone(PhysicalCard card, Zone zone) {
        _game.getGameState().addCardToZone(_game, card, zone);
        return card;
    }

    public PhysicalCard attachTo(PhysicalCard card, PhysicalCard toCard) {
        addToZone(card, Zone.ATTACHED);
        ((PhysicalCardImpl) card).attachTo((PhysicalCardImpl) toCard);
        return card;
    }

    public PhysicalCard stackOn(PhysicalCard card, PhysicalCard toCard) {
        addToZone(card, Zone.STACKED);
        ((PhysicalCardImpl) card).stackOn((PhysicalCardImpl) toCard);
        return card;
    }

    public PhysicalCard putOnTopOfDeck(PhysicalCard card) {
        _game.getGameState().putCardOnTopOfDeck(card);
        return card;
    }

    public int getCultureTokens(PhysicalCard tomBombadilsHat, Culture culture) {
        return _game.getGameState().getTokenCount(tomBombadilsHat, Token.findTokenForCulture(culture));
    }

    public void selectArbitraryCards(String player, String[] blueprintIds) throws DecisionResultInvalidException {
        playerDecided(player, StringUtils.join(blueprintIds, ","));
    }

    public void selectCardAction(String player, PhysicalCard card) throws DecisionResultInvalidException {
        playerDecided(player, getCardActionId(player, card.getCardId()));
    }

    public void hasCardAction(String player, PhysicalCard card) {
        assertNotNull(getCardActionId(player, card));
    }

    public void assertNoCardAction(String player, PhysicalCard card) {
        assertNull(getCardActionId(player, card));
    }

    public void selectCard(String player, PhysicalCard card) throws DecisionResultInvalidException {
        playerDecided(player, String.valueOf(card.getCardId()));
    }

    public void selectYes(String player) throws DecisionResultInvalidException {
        playerDecided(player, "0");
    }

    public void selectNo(String player) throws DecisionResultInvalidException {
        playerDecided(player, "1");
    }

    public int getWounds(PhysicalCard card) {
        return _game.getGameState().getWounds(card);
    }

    public void addWounds(PhysicalCard card, int count) {
        for (int i = 0; i < count; i++) {
            _game.getGameState().addWound(card);
        }
    }

    public void removeWounds(PhysicalCard card, int count) {
        for (int i = 0; i < count; i++) {
            if (getWounds(card) > 0) {
                _game.getGameState().removeWound(card);
            }
        }
    }

    public void replaceSite(PhysicalCard newSite, int siteNumber) {
        PhysicalCard oldSite = _game.getGameState().getSite(siteNumber);
        _game.getGameState().removeCardsFromZone(oldSite.getOwner(), Collections.singleton(oldSite));
        oldSite.setSiteNumber(null);
        _game.getGameState().addCardToZone(_game, oldSite, Zone.ADVENTURE_DECK);

        _game.getGameState().removeCardsFromZone(newSite.getOwner(), Collections.singleton(newSite));
        newSite.setSiteNumber(siteNumber);
        _game.getGameState().addCardToZone(_game, newSite, Zone.ADVENTURE_PATH);
    }

    public int getTwilightPool() {
        return _game.getGameState().getTwilightPool();
    }

    public void setTwilightPool(int twilightPool) {
        _game.getGameState().setTwilight(twilightPool);
    }

    public void addThreats(String player, int threats) {
        _game.getGameState().addThreats(player, threats);
    }

    public int getThreats() {
        return _game.getGameState().getThreats();
    }

    public int getBurdens() {
        return _game.getGameState().getBurdens();
    }

    public void addBurdens(int count) {
        _game.getGameState().addBurdens(count);
    }

    public void removeBurdens(int count) {
        _game.getGameState().removeBurdens(count);
    }

    public Phase getPhase() {
        return _game.getGameState().getCurrentPhase();
    }

    public void pass(String player) throws DecisionResultInvalidException {
        playerDecided(player, "");
    }

    public int getStrength(PhysicalCard card) {
        return _game.getModifiersQuerying().getStrength(_game, card);
    }

    public int getResistance(PhysicalCard card) {
        return _game.getModifiersQuerying().getResistance(_game, card);
    }

    public void initializeSimplestGame() throws DecisionResultInvalidException {
        this.initializeSimplestGame(null);
    }

    public void initializeSimplestGame(Map<String, Collection<String>> additionalCardsInDeck) throws DecisionResultInvalidException {
        Map<String, LotroDeck> decks = new HashMap<>();
        addPlayerDeck(P1, decks, additionalCardsInDeck);
        addPlayerDeck(P2, decks, additionalCardsInDeck);

        initializeGameWithDecks(decks);
    }

    public void initializeGameWithDecks(Map<String, LotroDeck> decks) throws DecisionResultInvalidException {
        initializeGameWithDecks(decks, "multipath");
    }

    public void initializeGameWithDecks(Map<String, LotroDeck> decks, String formatName) throws DecisionResultInvalidException {
        _userFeedback = new DefaultUserFeedback();

        LotroFormatLibrary formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        LotroFormat format = formatLibrary.getFormat(formatName);

        _game = new DefaultLotroGame(format, decks, _userFeedback, _cardLibrary);
        _userFeedback.setGame(_game);
        _game.startGame();

        // Bidding
        playerDecided(P1, "1");
        playerDecided(P2, "0");

        // Seating choice
        playerDecided(P1, "0");
    }

    public void skipMulligans() throws DecisionResultInvalidException {
        // Mulligans
        playerDecided(P1, "0");
        playerDecided(P2, "0");
    }

    public void playerAnswersNo(String player) throws DecisionResultInvalidException {
        playerDecided(player, "1");
    }

    public void passUntil(Phase phase) throws DecisionResultInvalidException {
        while (true) {
            Phase currentPhase = _game.getGameState().getCurrentPhase();
            if (currentPhase == phase)
                break;
            switch (currentPhase) {
                case BETWEEN_TURNS -> skipMulligans();
                case FELLOWSHIP -> pass(P1);
                case SHADOW -> pass(P2);
                case MANEUVER, ARCHERY, ASSIGNMENT -> {
                    pass(P1);
                    pass(P2);
                }
            }
        }
    }

    public void assertInZone(PhysicalCard card, Zone zone) {
        assertEquals(zone, card.getZone());
    }

    public void validateContents(String[] array1, String[] array2) {
        if (array1.length != array2.length)
            fail("Array sizes differ");
        List<String> values = new ArrayList<>(Arrays.asList(array1));
        for (String s : array2) {
            if (!values.remove(s))
                fail("Arrays contents differ");
        }
    }

    public String[] toCardIdArray(PhysicalCard... cards) {
        String[] result = new String[cards.length];
        for (int i = 0; i < cards.length; i++)
            result[i] = String.valueOf(cards[i].getCardId());
        return result;
    }

    public String getArbitraryCardId(AwaitingDecision awaitingDecision, String blueprintId) {
        String[] blueprints = (String[]) awaitingDecision.getDecisionParameters().get("blueprintId");
        for (int i = 0; i < blueprints.length; i++)
            if (blueprints[i].equals(blueprintId))
                return ((String[]) awaitingDecision.getDecisionParameters().get("cardId"))[i];
        return null;
    }

    public String getCardActionId(AwaitingDecision awaitingDecision, String actionTextStart) {
        String[] actionTexts = (String[]) awaitingDecision.getDecisionParameters().get("actionText");
        for (int i = 0; i < actionTexts.length; i++)
            if (actionTexts[i].startsWith(actionTextStart))
                return ((String[]) awaitingDecision.getDecisionParameters().get("actionId"))[i];
        return null;
    }

    public String getCardActionId(AwaitingDecision awaitingDecision, PhysicalCard card) {
        String[] cardIds = (String[]) awaitingDecision.getDecisionParameters().get("cardId");
        for (int i = 0; i < cardIds.length; i++)
            if (cardIds[i].equals(String.valueOf(card.getCardId())))
                return ((String[]) awaitingDecision.getDecisionParameters().get("actionId"))[i];
        return null;
    }

    public String getCardActionId(String playerId, PhysicalCard carD) {
        return getCardActionId(playerId, carD.getCardId());
    }

    public String getCardActionId(String playerId, int cardId) {
        AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(playerId);
        String[] cardIds = (String[]) awaitingDecision.getDecisionParameters().get("cardId");
        for (int i = 0; i < cardIds.length; i++)
            if (cardIds[i].equals(String.valueOf(cardId)))
                return ((String[]) awaitingDecision.getDecisionParameters().get("actionId"))[i];
        return null;
    }

    public String getCardActionId(String playerId, String actionTextStart) {
        return getCardActionId(_userFeedback.getAwaitingDecision(playerId), actionTextStart);
    }

    public String getCardActionIdContains(AwaitingDecision awaitingDecision, String actionTextContains) {
        String[] actionTexts = (String[]) awaitingDecision.getDecisionParameters().get("actionText");
        for (int i = 0; i < actionTexts.length; i++)
            if (actionTexts[i].contains(actionTextContains))
                return ((String[]) awaitingDecision.getDecisionParameters().get("actionId"))[i];
        return null;
    }

    public String getMultipleDecisionIndex(AwaitingDecision awaitingDecision, String result) {
        String[] actionTexts = (String[]) awaitingDecision.getDecisionParameters().get("results");
        for (int i = 0; i < actionTexts.length; i++)
            if (actionTexts[i].equals(result))
                return String.valueOf(i);
        return null;
    }

    public void addPlayerDeck(String player, Map<String, LotroDeck> decks, Map<String, Collection<String>> additionalCardsInDeck) {
        LotroDeck deck = createSimplestDeck();
        if (additionalCardsInDeck != null) {
            Collection<String> extraCards = additionalCardsInDeck.get(player);
            if (extraCards != null)
                for (String extraCard : extraCards)
                    deck.addCard(extraCard);
        }
        decks.put(player, deck);
    }

    public void moveCardToZone(PhysicalCardImpl card, Zone zone) {
        _game.getGameState().addCardToZone(_game, card, zone);
    }

    public void playerDecided(String player, String answer) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(player);
        _userFeedback.participantDecided(player);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            _userFeedback.sendAwaitingDecision(player, decision);
            throw exp;
        }
        _game.carryOutPendingActionsUntilDecisionNeeded();
    }

    public AwaitingDecision getAwaitingDecision(String player) {
        return _userFeedback.getAwaitingDecision(player);
    }

    public void carryOutEffectInPhaseActionByPlayer(String playerId, Effect effect) throws DecisionResultInvalidException {
        SystemQueueAction action = new SystemQueueAction();
        action.appendEffect(effect);
        carryOutEffectInPhaseActionByPlayer(playerId, action);
    }

    public void carryOutEffectInPhaseActionByPlayer(String playerId, Action action) throws DecisionResultInvalidException {
        CardActionSelectionDecision awaitingDecision = (CardActionSelectionDecision) _userFeedback.getAwaitingDecision(playerId);
        awaitingDecision.addAction(action);

        playerDecided(playerId, "0");
    }

    public LotroDeck createSimplestDeck() {
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        // 10_121,1_2
        lotroDeck.setRingBearer("10_121");
        lotroDeck.setRing("1_2");
        // 7_330,7_336,8_117,7_342,7_345,7_350,8_120,10_120,7_360
        lotroDeck.addSite("7_330");
        lotroDeck.addSite("7_335");
        lotroDeck.addSite("8_117");
        lotroDeck.addSite("7_342");
        lotroDeck.addSite("7_345");
        lotroDeck.addSite("7_350");
        lotroDeck.addSite("8_120");
        lotroDeck.addSite("10_120");
        lotroDeck.addSite("7_360");
        return lotroDeck;
    }
}
