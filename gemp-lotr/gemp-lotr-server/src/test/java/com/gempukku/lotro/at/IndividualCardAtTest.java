package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class IndividualCardAtTest extends AbstractAtTest {
    @Test
    public void musterFrodoAllowsToDiscardAndDraw() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, LotroDeck> decks = new HashMap<>();
        final LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRingBearer("11_164");
        decks.put(P1, p1Deck);
        decks.put(P2, createSimplestDeck());

        initializeGameWithDecks(decks);

        skipMulligans();

        PhysicalCardImpl mumakChieftain = new PhysicalCardImpl(100, "10_45", P1, _cardLibrary.getLotroCardBlueprint("10_45"));
        PhysicalCardImpl mumakChieftain2 = new PhysicalCardImpl(101, "10_45", P1, _cardLibrary.getLotroCardBlueprint("10_45"));

        _game.getGameState().addCardToZone(_game, mumakChieftain, Zone.HAND);
        _game.getGameState().addCardToZone(_game, mumakChieftain2, Zone.DECK);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        playerDecided(P1, "0");

        assertEquals(1, _game.getGameState().getWounds(_game.getGameState().getRingBearer(P1)));
        assertEquals(1, _game.getGameState().getHand(P1).size());

        final AwaitingDecision musterUseDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, musterUseDecision.getDecisionType());
        validateContents(new String[]{"" + _game.getGameState().getRingBearer(P1).getCardId()}, ((String[]) musterUseDecision.getDecisionParameters().get("cardId")));
        playerDecided(P1, "0");

        final AwaitingDecision musterDiscardDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, musterDiscardDecision.getDecisionType());
        validateContents(new String[]{"" + mumakChieftain.getCardId()}, ((String[]) musterDiscardDecision.getDecisionParameters().get("cardId")));
        playerDecided(P1, ((String[]) musterDiscardDecision.getDecisionParameters().get("cardId"))[0]);

        assertEquals(1, _game.getGameState().getHand(P1).size());

        assertEquals(Zone.HAND, mumakChieftain2.getZone());
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
    }

    @Test
    public void legolasBowWithToil() throws DecisionResultInvalidException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        extraCards.put(P1, Arrays.asList("11_21", "11_23", "11_17"));
        initializeSimplestGame(extraCards);

        // Play first character
        AwaitingDecision firstCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, firstCharacterDecision.getDecisionType());
        validateContents(new String[]{"11_21"}, ((String[]) firstCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(firstCharacterDecision, "11_21"));

        skipMulligans();

        final int twilightPool = _game.getGameState().getTwilightPool();

        AwaitingDecision playFirstFellowship = _userFeedback.getAwaitingDecision(P1);
        playerDecided(P1, getCardActionId(playFirstFellowship, "Play Legolas"));

        final int twilightPool2 = _game.getGameState().getTwilightPool();

        AwaitingDecision playSecondFellowship = _userFeedback.getAwaitingDecision(P1);
        playerDecided(P1, getCardActionId(playSecondFellowship, "Play Elven"));

        AwaitingDecision toilExertion = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, toilExertion.getDecisionType());
        String legolasCardId = ((String[]) toilExertion.getDecisionParameters().get("cardId"))[0];
        playerDecided(P1, legolasCardId);

        assertEquals(1, _game.getGameState().getWounds(_game.getGameState().findCardById(Integer.parseInt(legolasCardId))));

        AwaitingDecision bowHeal = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, bowHeal.getDecisionType());
        playerDecided(P1, "0");

        assertEquals(0, _game.getGameState().getWounds(_game.getGameState().findCardById(Integer.parseInt(legolasCardId))));
    }

    @Test
    public void endofTheGameGivingDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCardImpl aragorn = new PhysicalCardImpl(100, "1_89", P1, _cardLibrary.getLotroCardBlueprint("1_89"));
        PhysicalCardImpl urukHaiRaidingParty = new PhysicalCardImpl(100, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));
        PhysicalCardImpl endOfTheGame = new PhysicalCardImpl(100, "10_30", P1, _cardLibrary.getLotroCardBlueprint("10_30"));

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().addWound(aragorn);
        _game.getGameState().addWound(aragorn);
        _game.getGameState().addWound(aragorn);

        _game.getGameState().addCardToZone(_game, endOfTheGame, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End arhcery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign
        playerDecided(P1, aragorn.getCardId() + " " + urukHaiRaidingParty.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(aragorn.getCardId()));

        AwaitingDecision playSkirmishEvent = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playSkirmishEvent.getDecisionType());
        playerDecided(P1, "0");

        assertEquals(10, _game.getModifiersQuerying().getStrength(_game, aragorn));

        // End skirmish phase
        playerDecided(P2, "");
        playerDecided(P1, "");

        AwaitingDecision skirmishWinResponse = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ACTION_CHOICE, skirmishWinResponse.getDecisionType());
        playerDecided(P1, getCardActionId(skirmishWinResponse, "Required"));

        AwaitingDecision effectChoice = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.MULTIPLE_CHOICE, effectChoice.getDecisionType());

        int index = -1;
        String[] choices = ((String[]) effectChoice.getDecisionParameters().get("results"));
        for (int i = 0; i < choices.length; i++)
            if (choices[i].startsWith("Make "))
                index = i;

        playerDecided(P1, String.valueOf(index));

        assertEquals(2, _game.getGameState().getWounds(urukHaiRaidingParty));
    }

    @Test
    public void oneGoodTurnDeservesAnother() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl smeagol = new PhysicalCardImpl(100, "5_29", P1, _cardLibrary.getLotroCardBlueprint("5_29"));
        PhysicalCardImpl oneGoodTurnDeservesAnother = new PhysicalCardImpl(101, "11_49", P1, _cardLibrary.getLotroCardBlueprint("11_49"));

        _game.getGameState().addCardToZone(_game, oneGoodTurnDeservesAnother, Zone.HAND);
        _game.getGameState().addCardToZone(_game, smeagol, Zone.FREE_CHARACTERS);

        skipMulligans();

        playerDecided(P1, "0");

        assertEquals(1, _game.getGameState().getBurdens());
        assertEquals(0, _game.getGameState().getHand(P1).size());

        playerDecided(P1, "0");

        assertEquals(2, _game.getGameState().getBurdens());
        assertEquals(1, _game.getGameState().getHand(P1).size());
    }

    @Test
    public void rushOfSteeds() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl rushOfSteeds = new PhysicalCardImpl(100, "11_157", P1, _cardLibrary.getLotroCardBlueprint("11_157"));
        _game.getGameState().addCardToZone(_game, rushOfSteeds, Zone.SUPPORT);

        PhysicalCardImpl nelya = new PhysicalCardImpl(100, "1_233", P2, _cardLibrary.getLotroCardBlueprint("1_233"));
        _game.getGameState().addCardToZone(_game, nelya, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // End fellowship
        playerDecided(P1, "");

        AwaitingDecision playShadowEffect = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playShadowEffect.getDecisionType());
        playerDecided(P2, getCardActionId(playShadowEffect, "Use Úlairë "));

        AwaitingDecision rushOfSteedsOptional = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, rushOfSteedsOptional.getDecisionType());
        validateContents(new String[]{"" + rushOfSteeds.getCardId()}, ((String[]) rushOfSteedsOptional.getDecisionParameters().get("cardId")));

        playerDecided(P1, "0");

        assertEquals(P1, _game.getGameState().getSite(1).getOwner());
        assertEquals(Zone.HAND, nelya.getZone());

        playShadowEffect = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playShadowEffect.getDecisionType());
        validateContents(new String[0], (String[]) playShadowEffect.getDecisionParameters().get("cardId"));
    }

    @Test
    public void hisFirstSeriousCheck() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl gandalf = new PhysicalCardImpl(100, "1_72", P1, _cardLibrary.getLotroCardBlueprint("1_72"));
        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);

        PhysicalCardImpl hisFirstSeriousCheck = new PhysicalCardImpl(100, "3_33", P1, _cardLibrary.getLotroCardBlueprint("3_33"));
        _game.getGameState().addCardToZone(_game, hisFirstSeriousCheck, Zone.HAND);

        skipMulligans();

        // End fellowship
        playerDecided(P1, "");

        PhysicalCardImpl urukHaiRaidingParty = new PhysicalCardImpl(102, "1_158", P2, _cardLibrary.getLotroCardBlueprint("1_158"));
        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        AwaitingDecision playManeuverAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playManeuverAction.getDecisionType());
        validateContents(new String[]{hisFirstSeriousCheck.getCardId() + ""}, (String[]) playManeuverAction.getDecisionParameters().get("cardId"));
    }

    @Test
    public void burdensAreAddedFromOneRingEffect() throws CardNotFoundException, DecisionResultInvalidException {
        Map<String, LotroDeck> decks = new HashMap<>();
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        lotroDeck.setRingBearer("10_121");
        lotroDeck.setRing("9_1");
        // 7_330,7_336,8_117,7_342,7_345,7_350,8_120,10_120,7_360
        lotroDeck.addSite("1_323");
        lotroDeck.addSite("7_335");
        lotroDeck.addSite("8_117");
        lotroDeck.addSite("7_342");
        lotroDeck.addSite("7_345");
        lotroDeck.addSite("7_350");
        lotroDeck.addSite("8_120");
        lotroDeck.addSite("10_120");
        lotroDeck.addSite("7_360");
        decks.put(P1, lotroDeck);
        decks.put(P2, createSimplestDeck());

        _userFeedback = new DefaultUserFeedback();

        LotroFormatLibrary formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        LotroFormat format = formatLibrary.getFormat("movie");

        _game = new DefaultLotroGame(format, decks, _userFeedback, _cardLibrary);
        _userFeedback.setGame(_game);
        _game.startGame();

        PhysicalCardImpl cheapMinion = createCard(P2, "1_174");
        PhysicalCardImpl randomCard1 = createCard(P1, "2_65");

        // Bidding
        playerDecided(P1, "1");
        playerDecided(P2, "0");

        // Seating choice
        playerDecided(P1, "0");

        // Mulligans
        playerDecided(P1, "0");
        playerDecided(P2, "0");

        final PhysicalCard frodo = _game.getGameState().getRingBearer(P1);
        _game.getGameState().addCardToZone(_game, cheapMinion, Zone.HAND);
        _game.getGameState().addCardToZone(_game, randomCard1, Zone.HAND);

        _game.getGameState().addTwilight(3);

        // Pass in fellowship
        playerDecided(P1, "");

        // Play minion
        playerDecided(P2, "0");
        playerDecided(P2, "");

        // Put on the ring
        playerDecided(P1, "0");

        // Pass in maneuver
        playerDecided(P2, "");
        playerDecided(P1, "");

        // Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign minion to Frodo
        playerDecided(P1, frodo.getCardId() + " " + cheapMinion.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(frodo.getCardId()));

        int burdensBefore = _game.getGameState().getBurdens();

        // Pass in skirmish
        playerDecided(P1, "");
        playerDecided(P2, "");

        assertEquals(burdensBefore + 1, _game.getGameState().getBurdens());
        assertEquals(1, _game.getGameState().getWounds(frodo));
    }

    @Test
    public void sarumanOfManyColors() throws Exception {
        initializeSimplestGame();

        PhysicalCard sarumanOfManyColors = addToZone(createCard(P2, "12_54"), Zone.HAND);

        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(10);
        passUntil(Phase.SHADOW);
        assertEquals(4, getStrength(ringBearer));
        selectCardAction(P2, sarumanOfManyColors);
        playerDecided(P2, getMultipleDecisionIndex(getAwaitingDecision(P2), "Shire"));
        assertEquals(3, getStrength(ringBearer));
    }

    @Test
    public void reorderTopCardsOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard discoveries = addToZone(createCard(P1, "12_26"), Zone.HAND);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard topCard = addToZone(createCard(P1, "1_3"), Zone.DECK);
        PhysicalCard middleCard = addToZone(createCard(P1, "1_4"), Zone.DECK);
        PhysicalCard bottomCard = addToZone(createCard(P1, "1_5"), Zone.DECK);
        selectCardAction(P1, discoveries);
        pass(P1);
        playerDecided(P1, "3");
        selectArbitraryCards(P1, getArbitraryCardId(P1, "1_3"));

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals(middleCard, deck.get(0));
        assertEquals(topCard, deck.get(1));
        assertEquals(bottomCard, deck.get(2));
    }

    @Test
    public void setStrengthOverride() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard finalTriumph = addToZone(createCard(P2, "18_115"), Zone.HAND);
        PhysicalCard whiteHandExorciser = addToZone(createCard(P2, "18_125"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        setTwilightPool(4);
        playerDecided(P1, ringBearer.getCardId()+" "+whiteHandExorciser.getCardId());
        selectCard(P1, ringBearer);
        pass(P1);
        selectCardAction(P2, finalTriumph);
        assertEquals(4, RuleUtils.getFellowshipSkirmishStrength(_game));
        assertEquals(1, RuleUtils.getShadowSkirmishStrength(_game));
    }
}
