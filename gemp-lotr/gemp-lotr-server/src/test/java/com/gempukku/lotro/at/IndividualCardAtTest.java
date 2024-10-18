package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Token;
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
    public void bilboRingBearerWithConsortingAndMorgulBrute() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, LotroDeck> decks = new HashMap<>();
        final LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRingBearer("9_49");
        decks.put(P1, p1Deck);
        final LotroDeck p2Deck = createSimplestDeck();
        p2Deck.addCard("7_188");
        decks.put(P2, p2Deck);

        initializeGameWithDecks(decks);

        skipMulligans();

        PhysicalCard morgulBrute = _game.getGameState().getHand(P2).iterator().next();

        PhysicalCardImpl consortingWithWizards = new PhysicalCardImpl(100, "2_97", P1, _cardLibrary.getLotroCardBlueprint("2_97"));
        PhysicalCardImpl enquea = new PhysicalCardImpl(101, "1_231", P2, _cardLibrary.getLotroCardBlueprint("1_231"));

        _game.getGameState().attachCard(_game, consortingWithWizards, _game.getGameState().getRingBearer(P1));

        _game.getGameState().addTwilight(3);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, enquea, Zone.SHADOW_CHARACTERS);

        final AwaitingDecision shadowDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowDecision.getDecisionType());
        validateContents(new String[]{"" + morgulBrute.getCardId()}, ((String[]) shadowDecision.getDecisionParameters().get("cardId")));

        playerDecided(P2, "0");

        final AwaitingDecision optionalPlayDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalPlayDecision.getDecisionType());
        validateContents(new String[]{"" + morgulBrute.getCardId()}, ((String[]) optionalPlayDecision.getDecisionParameters().get("cardId")));

        assertEquals(1, _game.getGameState().getBurdens());

        // User optional trigger of Morgul Brute
        playerDecided(P2, "0");

        // This should add burden without asking FP player for choice, since Bilbo can't be wounded, because of Consorting With Wizards
        assertEquals(2, _game.getGameState().getBurdens());
    }

    @Test
    public void mumakChieftainPlayingMumakForFree() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl mumakChieftain = new PhysicalCardImpl(100, "10_45", P2, _cardLibrary.getLotroCardBlueprint("10_45"));
        PhysicalCardImpl mumak = new PhysicalCardImpl(100, "5_73", P2, _cardLibrary.getLotroCardBlueprint("5_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, mumak, Zone.DISCARD);
        _game.getGameState().addCardToZone(_game, mumakChieftain, Zone.HAND);
        _game.getGameState().setTwilight(5);

        // End fellowship phase
        playerDecided(P1, "");

        assertEquals(7, _game.getGameState().getTwilightPool());

        // Play mumak chieftain
        final AwaitingDecision shadowDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowDecision.getDecisionType());
        validateContents(new String[]{"" + mumakChieftain.getCardId()}, ((String[]) shadowDecision.getDecisionParameters().get("cardId")));
        playerDecided(P2, "0");

        assertEquals(0, _game.getGameState().getTwilightPool());

        final AwaitingDecision optionalPlayDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalPlayDecision.getDecisionType());
        validateContents(new String[]{"" + mumakChieftain.getCardId()}, ((String[]) optionalPlayDecision.getDecisionParameters().get("cardId")));
        playerDecided(P2, "0");

        assertEquals(Zone.ATTACHED, mumak.getZone());
    }

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
    public void oElberethGilthoniel() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, LotroDeck> decks = new HashMap<>();
        final LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRingBearer("9_49");
        decks.put(P1, p1Deck);

        decks.put(P2, createSimplestDeck());

        initializeGameWithDecks(decks);

        skipMulligans();

        PhysicalCardImpl oElberethGilthoniel = new PhysicalCardImpl(100, "2_108", P1, _cardLibrary.getLotroCardBlueprint("2_108"));
        PhysicalCardImpl ulaireToldea = new PhysicalCardImpl(101, "1_236", P2, _cardLibrary.getLotroCardBlueprint("1_236"));

        _game.getGameState().attachCard(_game, oElberethGilthoniel, _game.getGameState().getRingBearer(P1));

        _game.getGameState().addTwilight(3);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, ulaireToldea, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign
        playerDecided(P1, _game.getGameState().getRingBearer(P1).getCardId() + " " + ulaireToldea.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(_game.getGameState().getRingBearer(P1).getCardId()));

        AwaitingDecision playSkirmishEvent = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playSkirmishEvent.getDecisionType());
        validateContents(new String[]{"" + oElberethGilthoniel.getCardId()}, ((String[]) playSkirmishEvent.getDecisionParameters().get("cardId")));
        playerDecided(P1, "0");

        // Should not cancel skirmish (since it's a ring-bearer)
        assertEquals(Phase.SKIRMISH, _game.getGameState().getCurrentPhase());
    }

    @Test
    public void frodoCantBePlayedInStartingFellowship() throws DecisionResultInvalidException {
        Map<String, LotroDeck> decks = new HashMap<>();
        final LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRingBearer("13_156");
        p1Deck.addCard("4_301");
        decks.put(P1, p1Deck);

        decks.put(P2, createSimplestDeck());

        initializeGameWithDecks(decks);

        AwaitingDecision decision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.MULTIPLE_CHOICE, decision.getDecisionType());
    }

    @Test
    public void fordOfBruinenReduce() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        // End fellowship
        playerDecided(P1, "");

        // End shadow
        playerDecided(P2, "");

        PhysicalCardImpl ford = new PhysicalCardImpl(101, "1_338", P2, _cardLibrary.getLotroCardBlueprint("1_338"));
        ford.setSiteNumber(3);
        _game.getGameState().addCardToZone(_game, ford, Zone.ADVENTURE_PATH);

        // End regroup
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Decide to move
        playerDecided(P1, getMultipleDecisionIndex(_userFeedback.getAwaitingDecision(P1), "Yes"));

        // End shadow
        playerDecided(P2, "");

        // End regroup
        playerDecided(P1, "");
        playerDecided(P2, "");

        // P2 Turn
        PhysicalCardImpl attea = new PhysicalCardImpl(102, "1_229", P1, _cardLibrary.getLotroCardBlueprint("1_229"));
        _game.getGameState().addCardToZone(_game, attea, Zone.HAND);

        // End fellowship
        playerDecided(P2, "");

        assertEquals(8, _game.getModifiersQuerying().getTwilightCostToPlay(_game, attea, null, 0, false));
    }

    @Test
    public void sentBackAllowsPlayingCardInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl sentBack = new PhysicalCardImpl(100, "9_27", P1, _cardLibrary.getLotroCardBlueprint("9_27"));
        _game.getGameState().addCardToZone(_game, sentBack, Zone.SUPPORT);

        PhysicalCardImpl radagast1 = new PhysicalCardImpl(101, "9_26", P1, _cardLibrary.getLotroCardBlueprint("9_26"));
        _game.getGameState().addCardToZone(_game, radagast1, Zone.DEAD);

        PhysicalCardImpl radagast2 = new PhysicalCardImpl(101, "9_26", P1, _cardLibrary.getLotroCardBlueprint("9_26"));
        _game.getGameState().addCardToZone(_game, radagast2, Zone.HAND);

        skipMulligans();

        // End fellowship
        AwaitingDecision playFellowshipAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playFellowshipAction.getDecisionType());
        validateContents(new String[]{"" + sentBack.getCardId()}, ((String[]) playFellowshipAction.getDecisionParameters().get("cardId")));
        playerDecided(P1, "0");

        assertEquals(Zone.FREE_CHARACTERS, radagast2.getZone());
        assertEquals(4, _game.getGameState().getTwilightPool());
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
    public void scouringOfTheShireAndCorsairMarauder() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl corsairWarGalley = new PhysicalCardImpl(100, "8_59", P2, _cardLibrary.getLotroCardBlueprint("8_59"));
        PhysicalCardImpl corsairMarauder = new PhysicalCardImpl(101, "8_57", P2, _cardLibrary.getLotroCardBlueprint("8_57"));
        PhysicalCardImpl corsairMarauder2 = new PhysicalCardImpl(101, "8_57", P2, _cardLibrary.getLotroCardBlueprint("8_57"));
        PhysicalCardImpl scourgeOfTheShire = new PhysicalCardImpl(102, "18_112", P1, _cardLibrary.getLotroCardBlueprint("18_112"));
        PhysicalCardImpl hobbitSword = new PhysicalCardImpl(102, "1_299", P1, _cardLibrary.getLotroCardBlueprint("1_299"));

        _game.getGameState().addCardToZone(_game, scourgeOfTheShire, Zone.SUPPORT);
        _game.getGameState().attachCard(_game, hobbitSword, _game.getGameState().getRingBearer(P1));
        _game.getGameState().addCardToZone(_game, corsairWarGalley, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, corsairMarauder, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corsairMarauder2, Zone.SHADOW_CHARACTERS);

        skipMulligans();
        _game.getGameState().addTwilight(10);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addTokens(corsairWarGalley, Token.RAIDER, 1);

        AwaitingDecision playShadowAction = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playShadowAction.getDecisionType());
        playerDecided(P2, getCardActionId(playShadowAction, "Play Cor"));

        // Use Corsair Marauder's trigger
        playerDecided(P2, "0");

        // Choose Hobbit Sword to discard
        playerDecided(P2, "" + hobbitSword.getCardId());

        // Use Scourge of the Shire
        playerDecided(P1, "0");

        assertEquals(1, _game.getGameState().getTokenCount(scourgeOfTheShire, Token.SHIRE));
        assertEquals(Zone.ATTACHED, hobbitSword.getZone());
        assertEquals(1, _game.getGameState().getTokenCount(corsairWarGalley, Token.RAIDER));
    }

    @Test
    public void returnToItsMaster() throws DecisionResultInvalidException, CardNotFoundException {
        LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRing("4_1");
        LotroDeck p2Deck = createSimplestDeck();

        Map<String, LotroDeck> decks = new HashMap<>();
        decks.put(P1, p1Deck);
        decks.put(P2, p2Deck);

        initializeGameWithDecks(decks);

        skipMulligans();

        PhysicalCardImpl returnToItsMaster = new PhysicalCardImpl(102, "1_224", P2, _cardLibrary.getLotroCardBlueprint("1_224"));
        _game.getGameState().addCardToZone(_game, returnToItsMaster, Zone.HAND);

        PhysicalCardImpl nelya = new PhysicalCardImpl(102, "1_233", P2, _cardLibrary.getLotroCardBlueprint("1_233"));
        _game.getGameState().addCardToZone(_game, nelya, Zone.SHADOW_CHARACTERS);

        PhysicalCardImpl hobbitSword = new PhysicalCardImpl(102, "1_299", P1, _cardLibrary.getLotroCardBlueprint("1_299"));
        _game.getGameState().attachCard(_game, hobbitSword, _game.getGameState().getRingBearer(P1));

        // End fellowship
        playerDecided(P1, "");

        // End shadow
        playerDecided(P2, "");

        // End maneuvers phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign
        playerDecided(P1, _game.getGameState().getRingBearer(P1).getCardId() + " " + nelya.getCardId());

        // Choose skirmish to resolve
        playerDecided(P1, "" + _game.getGameState().getRingBearer(P1).getCardId());

        // Skirmish phase
        AwaitingDecision skirmishAction = _userFeedback.getAwaitingDecision(P1);
        playerDecided(P1, getCardActionId(skirmishAction, "Use The One"));

        assertTrue(_game.getGameState().isWearingRing());

        // End skirmish phase
        playerDecided(P2, "");
        playerDecided(P1, "");

        // Don't use Return to Its Master
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign
        playerDecided(P1, _game.getGameState().getRingBearer(P1).getCardId() + " " + nelya.getCardId());

        // Choose skirmish to resolve
        playerDecided(P1, "" + _game.getGameState().getRingBearer(P1).getCardId());

        // End fierce skirmish phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        AwaitingDecision playeReturnDecision = _userFeedback.getAwaitingDecision(P2);
        playerDecided(P2, getCardActionId(playeReturnDecision, "Play Return"));

        // Choose skirmish to resolve
        playerDecided(P1, "" + _game.getGameState().getRingBearer(P1).getCardId());

        assertEquals(_game.getGameState().getRingBearer(P1), _game.getGameState().getSkirmish().getFellowshipCharacter());
        assertEquals(1, _game.getGameState().getSkirmish().getShadowCharacters().size());
        assertEquals(nelya, _game.getGameState().getSkirmish().getShadowCharacters().iterator().next());
    }

    @Test
    public void moreYetToComeWorks() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl gimli = new PhysicalCardImpl(100, "1_12", P1, _cardLibrary.getLotroCardBlueprint("1_12"));
        PhysicalCardImpl moreYetToCome = new PhysicalCardImpl(101, "10_3", P1, _cardLibrary.getLotroCardBlueprint("10_3"));
        PhysicalCardImpl goblinRunner = new PhysicalCardImpl(102, "1_178", P2, _cardLibrary.getLotroCardBlueprint("1_178"));
        PhysicalCardImpl goblinRunner2 = new PhysicalCardImpl(103, "1_178", P2, _cardLibrary.getLotroCardBlueprint("1_178"));

        _game.getGameState().addCardToZone(_game, moreYetToCome, Zone.HAND);

        skipMulligans();

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, goblinRunner2, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        // End maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign Gimli to goblin runner
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        playerDecided(P2, "");

        // Choose skirmish to start
        playerDecided(P1, gimli.getCardId() + "");

        // End skirmish
        playerDecided(P1, "");
        playerDecided(P2, "");

        assertEquals(Zone.DISCARD, goblinRunner.getZone());
        AwaitingDecision playMoreYetToCome = _userFeedback.getAwaitingDecision(P1);
        playerDecided(P1, getCardActionId(playMoreYetToCome, moreYetToCome));

        assertEquals(Zone.DISCARD, goblinRunner2.getZone());
    }

    @Test
    public void treebeardEarthborn() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl treebeard = new PhysicalCardImpl(100, "4_103", P1, _cardLibrary.getLotroCardBlueprint("4_103"));
        PhysicalCardImpl merry = new PhysicalCardImpl(101, "4_311", P1, _cardLibrary.getLotroCardBlueprint("4_311"));
        PhysicalCardImpl goblinRunner = new PhysicalCardImpl(102, "1_178", P2, _cardLibrary.getLotroCardBlueprint("1_178"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, treebeard, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        // End maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign Gimli to goblin runner
        PhysicalCard frodo = _game.getGameState().getRingBearer(P1);
        playerDecided(P1, frodo.getCardId() + " " + goblinRunner.getCardId());

        // Choose skirmish to start
        playerDecided(P1, frodo.getCardId() + "");

        AwaitingDecision playSkirmishAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playSkirmishAction.getDecisionType());
        playerDecided(P1, getCardActionId(playSkirmishAction, "Use Merry"));

        AwaitingDecision treebeardDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, treebeardDecision.getDecisionType());
        playerDecided(P1, getCardActionId(treebeardDecision, "Use Tree"));

        assertEquals(Zone.STACKED, merry.getZone());
        assertEquals(treebeard, merry.getStackedOn());
    }

    @Test
    public void garradrielCorruptionAtStart() throws DecisionResultInvalidException {
        Map<String, LotroDeck> decks = new HashMap<>();
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        lotroDeck.setRingBearer("9_14");
        lotroDeck.setRing("11_1");
        lotroDeck.addSite("7_330");
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

        // Bidding
        playerDecided(P1, "5");
        playerDecided(P2, "0");

        // Seating choice
        playerDecided(P1, "0");

        _game.carryOutPendingActionsUntilDecisionNeeded();

        assertFalse(_game.isFinished());
    }

    @Test
    public void greenHillCountryWorksForFirstPlayer() throws DecisionResultInvalidException {
        Map<String, LotroDeck> decks = new HashMap<>();
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        lotroDeck.setRingBearer("10_121");
        lotroDeck.setRing("1_2");
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
        lotroDeck.addCard("1_303");
        decks.put(P1, lotroDeck);
        decks.put(P2, createSimplestDeck());

        _userFeedback = new DefaultUserFeedback();

        LotroFormatLibrary formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        LotroFormat format = formatLibrary.getFormat("movie");

        _game = new DefaultLotroGame(format, decks, _userFeedback, _cardLibrary);
        _userFeedback.setGame(_game);
        _game.startGame();

        // Bidding
        playerDecided(P1, "1");
        playerDecided(P2, "0");

        // Seating choice
        playerDecided(P1, "0");

        // Play no starting companions
        playerDecided(P1, "");

        // Mulligans
        playerDecided(P1, "0");
        playerDecided(P2, "0");

        // Fellowship phase
        AwaitingDecision playFellowshipAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playFellowshipAction.getDecisionType());
        playerDecided(P1, getCardActionId(playFellowshipAction, "Play Merry"));

        assertEquals(0, _game.getGameState().getTwilightPool());
    }

    @Test
    public void trollMonstrousFiend() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl troll = createCard(P2, "40_157");
        PhysicalCardImpl runner1 = createCard(P2, "40_169");
        PhysicalCardImpl runner2 = createCard(P2, "40_169");
        PhysicalCardImpl runner3 = createCard(P2, "40_169");
        PhysicalCardImpl runner4 = createCard(P2, "40_169");

        skipMulligans();

        _game.getGameState().addCardToZone(_game, troll, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner4, Zone.HAND);

        _game.getGameState().addTwilight(7);

        // End fellowship
        playerDecided(P1, "");

        assertEquals(9, _game.getGameState().getTwilightPool());

        final AwaitingDecision playShadowAction = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playShadowAction.getDecisionType());
        playerDecided(P2, getCardActionId(playShadowAction, "Play Cave"));

        final AwaitingDecision discardGoblins = _userFeedback.getAwaitingDecision(P2);
        assertEquals("3", discardGoblins.getDecisionParameters().get("min")[0]);
    }

    @Test
    public void exertingEhaustedFrodo() throws CardNotFoundException, DecisionResultInvalidException {
        Map<String, LotroDeck> decks = new HashMap<>();
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        lotroDeck.setRingBearer("10_121");
        lotroDeck.setRing("40_1");
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

        PhysicalCardImpl cheapMinion = createCard(P2, "40_165");
        PhysicalCardImpl randomCard1 = createCard(P1, "40_169");

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

        _game.getGameState().addTokens(frodo, Token.WOUND, 4);
        _game.getGameState().addTwilight(3);

        // Pass in fellowship
        playerDecided(P1, "");

        // Play minion
        playerDecided(P2, "0");
        playerDecided(P2, "");

        // Put on the ring
        playerDecided(P1, "0");
        assertEquals(Zone.DISCARD, randomCard1.getZone());

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
        assertEquals(4, _game.getGameState().getWounds(frodo));
    }

    @Test
    public void orcMarksmanUnique() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl marksman1 = new PhysicalCardImpl(100, "40_227", P2, _cardLibrary.getLotroCardBlueprint("40_227"));
        PhysicalCardImpl marksman2 = new PhysicalCardImpl(101, "40_227", P2, _cardLibrary.getLotroCardBlueprint("40_227"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, marksman1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, marksman2, Zone.HAND);

        _game.getGameState().addTwilight(10);

        // End fellowship
        playerDecided(P1, "");

        playerDecided(P2, "0");
        assertNull(getCardActionId(_userFeedback.getAwaitingDecision(P2), "Play Orc Mark"));
    }

    @Test
    public void frodosPipeOncePerPhase() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl frodosPipe = new PhysicalCardImpl(100, "40_250", P1, _cardLibrary.getLotroCardBlueprint("40_250"));
        PhysicalCardImpl pipeweed1 = new PhysicalCardImpl(101, "40_255", P1, _cardLibrary.getLotroCardBlueprint("40_255"));
        PhysicalCardImpl pipeweed2 = new PhysicalCardImpl(102, "40_255", P1, _cardLibrary.getLotroCardBlueprint("40_255"));

        _game.getGameState().addCardToZone(_game, frodosPipe, Zone.HAND);
        _game.getGameState().addCardToZone(_game, pipeweed1, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, pipeweed2, Zone.SUPPORT);

        skipMulligans();

        playerDecided(P1, getCardActionId(_userFeedback.getAwaitingDecision(P1), "Play Frodo's Pipe"));
        playerDecided(P1, getCardActionId(_userFeedback.getAwaitingDecision(P1), "Use Frodo's Pipe"));

        playerDecided(P1, String.valueOf(pipeweed1.getCardId()));
        playerDecided(P1, "1");

        assertNull(getCardActionId(_userFeedback.getAwaitingDecision(P1), "Use Frodo's Pipe"));
    }

    @Test
    public void blockRemovingBurdens() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl blackBreath = new PhysicalCardImpl(100, "1_207", P2, _cardLibrary.getLotroCardBlueprint("1_207"));
        PhysicalCardImpl sam = new PhysicalCardImpl(100, "1_311", P1, _cardLibrary.getLotroCardBlueprint("1_311"));

        _game.getGameState().attachCard(_game, blackBreath, _game.getGameState().getRingBearer(P1));
        _game.getGameState().addCardToZone(_game, sam, Zone.FREE_CHARACTERS);

        skipMulligans();

        final int burdensBefore = _game.getGameState().getBurdens();
        playerDecided(P1, getCardActionId(_userFeedback.getAwaitingDecision(P1), "Use Sam"));

        assertEquals(burdensBefore, _game.getGameState().getBurdens());
    }

    @Test
    public void athelasDoesNothing() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl athelas = new PhysicalCardImpl(100, "1_94", P1, _cardLibrary.getLotroCardBlueprint("1_94"));
        PhysicalCardImpl aragorn = new PhysicalCardImpl(101, "1_89", P1, _cardLibrary.getLotroCardBlueprint("1_89"));

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, athelas, aragorn);

        skipMulligans();

        // Use Athelas
        playerDecided(P1, getCardActionId(P1, "Use Athelas"));

        assertEquals(Zone.DISCARD, athelas.getZone());

        // Pass
        playerDecided(P1, "");

        playerDecided(P2, "");
    }

    @Test
    public void athelasHeals() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl athelas = new PhysicalCardImpl(100, "1_94", P1, _cardLibrary.getLotroCardBlueprint("1_94"));
        PhysicalCardImpl aragorn = new PhysicalCardImpl(101, "1_89", P1, _cardLibrary.getLotroCardBlueprint("1_89"));

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, athelas, aragorn);
        _game.getGameState().addTokens(aragorn, Token.WOUND, 1);

        skipMulligans();

        // Use Athelas
        playerDecided(P1, getCardActionId(P1, "Use Athelas"));

        assertEquals(Zone.DISCARD, athelas.getZone());
        assertEquals(0, _game.getGameState().getTokenCount(aragorn, Token.WOUND));

        // Pass
        playerDecided(P1, "");

        playerDecided(P2, "");
    }

    @Test
    public void athelasRemovesCondition() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl athelas = new PhysicalCardImpl(100, "1_94", P1, _cardLibrary.getLotroCardBlueprint("1_94"));
        PhysicalCardImpl aragorn = new PhysicalCardImpl(101, "1_89", P1, _cardLibrary.getLotroCardBlueprint("1_89"));
        PhysicalCardImpl blackBreath = new PhysicalCardImpl(100, "1_207", P2, _cardLibrary.getLotroCardBlueprint("1_207"));

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, athelas, aragorn);
        _game.getGameState().attachCard(_game, blackBreath, aragorn);

        skipMulligans();

        // Use Athelas
        playerDecided(P1, getCardActionId(P1, "Use Athelas"));

        assertEquals(Zone.DISCARD, athelas.getZone());
        assertEquals(Zone.DISCARD, blackBreath.getZone());

        // Pass
        playerDecided(P1, "");

        playerDecided(P2, "");
    }

    @Test
    public void athelasGivesChoice() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl athelas = new PhysicalCardImpl(100, "1_94", P1, _cardLibrary.getLotroCardBlueprint("1_94"));
        PhysicalCardImpl aragorn = new PhysicalCardImpl(101, "1_89", P1, _cardLibrary.getLotroCardBlueprint("1_89"));
        PhysicalCardImpl blackBreath = new PhysicalCardImpl(100, "1_207", P2, _cardLibrary.getLotroCardBlueprint("1_207"));

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, athelas, aragorn);
        _game.getGameState().attachCard(_game, blackBreath, aragorn);
        _game.getGameState().addTokens(_game.getGameState().getRingBearer(P1), Token.WOUND, 1);

        skipMulligans();

        // Use Athelas
        playerDecided(P1, getCardActionId(P1, "Use Athelas"));

        assertEquals(AwaitingDecisionType.MULTIPLE_CHOICE, _userFeedback.getAwaitingDecision(P1).getDecisionType());
    }

    @Test
    public void hobbitPartyGuest() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl hobbitPartyGuest = new PhysicalCardImpl(100, "1_297", P1, _cardLibrary.getLotroCardBlueprint("1_297"));
        PhysicalCardImpl rosie = new PhysicalCardImpl(100, "1_309", P1, _cardLibrary.getLotroCardBlueprint("1_309"));

        _game.getGameState().addCardToZone(_game, hobbitPartyGuest, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, rosie, Zone.SUPPORT);
        _game.getGameState().addTokens(rosie, Token.WOUND, 1);

        skipMulligans();

        playerDecided(P1, "0");
        assertEquals(0, _game.getGameState().getTokenCount(rosie, Token.WOUND));
    }

    @Test
    public void playStartingFellowship() throws DecisionResultInvalidException {

        Map<String, Collection<String>> additionalCardsInDeck = new HashMap<>();
        List<String> additionalCards = new LinkedList<>();
        additionalCards.add("2_121");
        additionalCards.add("3_121");
        additionalCards.add("1_306");
        additionalCardsInDeck.put(P1, additionalCards);

        Map<String, LotroDeck> decks = new HashMap<>();
        addPlayerDeck(P1, decks, additionalCardsInDeck);
        addPlayerDeck(P2, decks, additionalCardsInDeck);

        _userFeedback = new DefaultUserFeedback();

        LotroFormatLibrary formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        LotroFormat format = formatLibrary.getFormat("movie");

        _game = new DefaultLotroGame(format, decks, _userFeedback, _cardLibrary);
        _userFeedback.setGame(_game);
        _game.startGame();

        // Bidding
        playerDecided(P1, "1");
        playerDecided(P2, "0");

        // Seating choice
        playerDecided(P1, "0");

        // Play starting fellowship
        playerDecided(P1, "temp0");
        playerDecided(P1, "temp0");

        assertTrue(_userFeedback.getAwaitingDecision(P1).getText().startsWith("Do you wish to mulligan"));
    }

    @Test
    public void courteousFrodoPreventsDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, LotroDeck> decks = new HashMap<>();
        final LotroDeck p1Deck = createSimplestDeck();
        p1Deck.setRingBearer("4_301");
        decks.put(P1, p1Deck);

        decks.put(P2, createSimplestDeck());

        initializeGameWithDecks(decks);

        PhysicalCardImpl arwen = new PhysicalCardImpl(100, "7_16", P1, _cardLibrary.getLotroCardBlueprint("7_16"));
        PhysicalCardImpl legolas = new PhysicalCardImpl(100, "1_50", P1, _cardLibrary.getLotroCardBlueprint("1_50"));
        PhysicalCardImpl aragorn = new PhysicalCardImpl(100, "1_89", P1, _cardLibrary.getLotroCardBlueprint("1_89"));

        PhysicalCardImpl cardInHand = new PhysicalCardImpl(100, "1_268", P1, _cardLibrary.getLotroCardBlueprint("1_268"));
        PhysicalCardImpl inquisitor = new PhysicalCardImpl(100, "1_268", P2, _cardLibrary.getLotroCardBlueprint("1_268"));

        _game.getGameState().addCardToZone(_game, arwen, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, legolas, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, inquisitor, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand, Zone.HAND);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Play inquisitor
        playerDecided(P2, "0");
        // Use inquisitor
        playerDecided(P2, "0");

        assertEquals(Zone.HAND, cardInHand.getZone());
    }

    @Test
    public void betterThanNothingDoesNotAddBurden() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl betterThanNothing = createCard(P2, "31_19");
        PhysicalCardImpl wizardIsNeverLate = createCard(P1, "30_23");

        _game.getGameState().addCardToZone(_game, wizardIsNeverLate, Zone.HAND);
        _game.getGameState().addCardToZone(_game, betterThanNothing, Zone.SUPPORT);

        skipMulligans();
        int burdens = _game.getGameState().getBurdens();

        playerDecided(P1, "0");

        assertEquals(burdens, _game.getGameState().getBurdens());
    }

    @Test
    public void morgulBladeTransferFromSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl nelya = createCard(P2, "2_84");
        PhysicalCardImpl morgulBlade = createCard(P2, "1_216");
        PhysicalCardImpl bladeTip = createCard(P2, "1_209");

        _game.getGameState().addCardToZone(_game, nelya, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, bladeTip, Zone.SUPPORT);
        _game.getGameState().attachCard(_game, morgulBlade, nelya);

        skipMulligans();

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        // End maneuver phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign Frodo to Nelya
        PhysicalCard frodo = _game.getGameState().getRingBearer(P1);
        playerDecided(P1, frodo.getCardId() + " " + nelya.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(frodo.getCardId()));

        // P1 Passes
        playerDecided(P1, "");

        // Use Morgul Blade
        playerDecided(P2, "0");

        assertEquals(Zone.DISCARD, morgulBlade.getZone());
        assertEquals(Zone.ATTACHED, bladeTip.getZone());
    }

    @Test
    public void morgulBladeTransferFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl nelya = createCard(P2, "2_84");
        PhysicalCardImpl morgulBlade = createCard(P2, "1_216");
        PhysicalCardImpl bladeTip = createCard(P2, "1_209");

        _game.getGameState().addCardToZone(_game, nelya, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, bladeTip, Zone.DISCARD);
        _game.getGameState().attachCard(_game, morgulBlade, nelya);

        skipMulligans();

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        // End maneuver phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign Frodo to Nelya
        PhysicalCard frodo = _game.getGameState().getRingBearer(P1);
        playerDecided(P1, frodo.getCardId() + " " + nelya.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(frodo.getCardId()));

        // P1 Passes
        playerDecided(P1, "");

        // Use Morgul Blade
        playerDecided(P2, "0");

        assertEquals(Zone.DISCARD, morgulBlade.getZone());
        assertEquals(Zone.ATTACHED, bladeTip.getZone());
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
    public void putForthItsStrength() throws Exception {
        initializeSimplestGame();

        PhysicalCard putForthHisStrength = addToZone(createCard(P2, "7_205"), Zone.SUPPORT);
        PhysicalCard attea = addToZone(createCard(P2, "1_229"), Zone.SHADOW_CHARACTERS);
        for (int i = 0; i < 3; i++) {
            addToZone(createCard(P1, "1_12"), Zone.DEAD);
        }

        passUntil(Phase.FELLOWSHIP);
        addBurdens(3);
        addThreats(P1, 3);

        passUntil(Phase.SHADOW);
        selectCardAction(P2, putForthHisStrength);
        assertTrue(_game.isFinished());
        assertEquals(P2, _game.getWinnerPlayerId());
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

    @Test
    public void shuffleCardsFromPlayIntoDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard restByBlindNight = addToZone(createCard(P1, "4_54"), Zone.HAND);
        PhysicalCard stoutAndStrong = addToZone(createCard(P1, "4_57"), Zone.SUPPORT);

        passUntil(Phase.REGROUP);
        selectCardAction(P1, restByBlindNight);
        selectCard(P1, stoutAndStrong);
        assertEquals(Zone.DECK, stoutAndStrong.getZone());
    }
}
