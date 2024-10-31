package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TriggersAtTest extends AbstractAtTest {
    @Test
    public void fpCharWinsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = new PhysicalCardImpl(100, "5_7", P1, _cardLibrary.getLotroCardBlueprint("5_7"));
        PhysicalCardImpl stoutAndStrong = new PhysicalCardImpl(101, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl goblinRunner = new PhysicalCardImpl(102, "1_178", P2, _cardLibrary.getLotroCardBlueprint("1_178"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, stoutAndStrong, Zone.SUPPORT);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        assertEquals(Phase.SHADOW, _game.getGameState().getCurrentPhase());
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

        // Assign
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));

        // End skirmish phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        AwaitingDecision chooseTriggersDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, chooseTriggersDecision.getDecisionType());
        validateContents(new String[]{"" + gimli.getCardId(), "" + stoutAndStrong.getCardId()}, (String[]) chooseTriggersDecision.getDecisionParameters().get("cardId"));
    }

    @Test
    public void musterWorkingWithOtherOptionalStartOfRegroupTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl dervorin = new PhysicalCardImpl(100, "7_88", P1, _cardLibrary.getLotroCardBlueprint("7_88"));
        PhysicalCardImpl boromir = new PhysicalCardImpl(101, "1_96", P1, _cardLibrary.getLotroCardBlueprint("1_96"));
        PhysicalCardImpl cardInHand1 = new PhysicalCardImpl(102, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand2 = new PhysicalCardImpl(103, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand3 = new PhysicalCardImpl(104, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand4 = new PhysicalCardImpl(105, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, dervorin, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new AddKeywordModifier(dervorin, dervorin, null, Keyword.MUSTER));

        _game.getGameState().addCardToZone(_game, cardInHand1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand4, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        final AwaitingDecision optionalStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{"" + dervorin.getCardId(), "" + dervorin.getCardId()}, (String[]) optionalStartOfRegroupDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, getCardActionId(optionalStartOfRegroupDecision, "Optional "));

        final AwaitingDecision optionalSecondStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalSecondStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{"" + dervorin.getCardId()}, (String[]) optionalSecondStartOfRegroupDecision.getDecisionParameters().get("cardId"));
    }

    @Test
    public void userOfMusterAllowsUseOfOtherOptionalStartOfRegroupTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl dervorin = new PhysicalCardImpl(100, "7_88", P1, _cardLibrary.getLotroCardBlueprint("7_88"));
        PhysicalCardImpl boromir = new PhysicalCardImpl(101, "1_96", P1, _cardLibrary.getLotroCardBlueprint("1_96"));
        PhysicalCardImpl cardInHand1 = new PhysicalCardImpl(102, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand2 = new PhysicalCardImpl(103, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand3 = new PhysicalCardImpl(104, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand4 = new PhysicalCardImpl(105, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand5 = new PhysicalCardImpl(106, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, dervorin, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new AddKeywordModifier(dervorin, dervorin, null, Keyword.MUSTER));

        _game.getGameState().addCardToZone(_game, cardInHand1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand4, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand5, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        final AwaitingDecision optionalStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{"" + dervorin.getCardId()}, (String[]) optionalStartOfRegroupDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, getCardActionId(optionalStartOfRegroupDecision, "Use "));

        playerDecided(P1, String.valueOf(cardInHand1.getCardId()));

        final AwaitingDecision optionalSecondStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalSecondStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{"" + dervorin.getCardId()}, (String[]) optionalSecondStartOfRegroupDecision.getDecisionParameters().get("cardId"));
        assertTrue(((String[]) optionalSecondStartOfRegroupDecision.getDecisionParameters().get("actionText"))[0].startsWith("Optional "));
    }

    @Test
    public void userOfMusterDisablesUseOfOtherOptionalStartOfRegroupTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl dervorin = new PhysicalCardImpl(100, "7_88", P1, _cardLibrary.getLotroCardBlueprint("7_88"));
        PhysicalCardImpl boromir = new PhysicalCardImpl(101, "1_96", P1, _cardLibrary.getLotroCardBlueprint("1_96"));
        PhysicalCardImpl cardInHand1 = new PhysicalCardImpl(102, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand2 = new PhysicalCardImpl(103, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand3 = new PhysicalCardImpl(104, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand4 = new PhysicalCardImpl(105, "4_57", P1, _cardLibrary.getLotroCardBlueprint("4_57"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, dervorin, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new AddKeywordModifier(dervorin, dervorin, null, Keyword.MUSTER));

        _game.getGameState().addCardToZone(_game, cardInHand1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand4, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        final AwaitingDecision optionalStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{"" + dervorin.getCardId(), "" + dervorin.getCardId()}, (String[]) optionalStartOfRegroupDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, getCardActionId(optionalStartOfRegroupDecision, "Use "));
        playerDecided(P1, String.valueOf(cardInHand1.getCardId()));

        final AwaitingDecision regroupPhaseActionDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, regroupPhaseActionDecision.getDecisionType());
        validateContents(new String[]{}, (String[]) regroupPhaseActionDecision.getDecisionParameters().get("cardId"));

        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
    }

    @Test
    public void musterForShadowSideTriggersCorrectly() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl musterWitchKing = new PhysicalCardImpl(100, "11_226", P2, _cardLibrary.getLotroCardBlueprint("11_226"));
        PhysicalCardImpl musterWitchKing2 = new PhysicalCardImpl(101, "11_226", P2, _cardLibrary.getLotroCardBlueprint("11_226"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, musterWitchKing, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, musterWitchKing2, Zone.HAND);

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

        // Assign
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End fierce assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Fierce assign
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Start regroup
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
    }

    @Test
    public void replaceSiteNotPossibleWithMountDoom() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl gandalf = new PhysicalCardImpl(100, "1_72", P1, _cardLibrary.getLotroCardBlueprint("1_72"));
        PhysicalCardImpl traveledLeader = new PhysicalCardImpl(101, "12_34", P1, _cardLibrary.getLotroCardBlueprint("12_34"));
        PhysicalCardImpl mountDoom = new PhysicalCardImpl(102, "15_193", P2, _cardLibrary.getLotroCardBlueprint("15_193"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, traveledLeader, Zone.HAND);
        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().removeCardsFromZone(P2, Collections.singleton(_game.getGameState().getSite(2)));
        mountDoom.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, mountDoom, Zone.ADVENTURE_PATH);

        // End shadow phase
        playerDecided(P2, "");

        // End regroup phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, "1");

        // Reconcile
        playerDecided(P1, "");

        playerDecided(P2, "");
        playerDecided(P1, "");
        playerDecided(P2, "");
        playerDecided(P1, "");

        // Reconcile
        playerDecided(P1, "");

        playerDecided(P2, "1");

        playerDecided(P1, "");
        playerDecided(P2, "");

        final AwaitingDecision regroupActionDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, regroupActionDecision.getDecisionType());
        validateContents(new String[]{"" + traveledLeader.getCardId()}, (String[]) regroupActionDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, "0");

        PhysicalCard siteOne = _game.getGameState().getSite(1);

        playerDecided(P1, "" + siteOne.getCardId());

        assertEquals(siteOne, _game.getGameState().getSite(1));
    }

    @Test
    public void startOfSkirmishInvolving() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard hopelessGollum = addToZone(createCard(P2, "15_43"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);

        playerDecided(P1, gimli.getCardId() + " " + hopelessGollum.getCardId());

        assertEquals(0, getStrength(hopelessGollum));
        selectCard(P1, gimli);
        assertEquals(4, getStrength(hopelessGollum));
    }

    @Test
    public void skirmishAboutToEnd() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard attea = addToZone(createCard(P2, "1_229"), Zone.SHADOW_CHARACTERS);
        PhysicalCard loathsome = addToZone(createCard(P2, "7_182"), Zone.HAND);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + attea.getCardId());

        selectCard(P1, gimli);
        pass(P1);
        pass(P2);

        selectCardAction(P2, loathsome);
        assertEquals(Zone.DISCARD, loathsome.getZone());
    }

    @Test
    public void revealedCardFromHand() throws Exception {
        initializeSimplestGame();

        PhysicalCard eyeOfBaradDur = addToZone(createCard(P2, "5_96"), Zone.HAND);
        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard glamdring = attachTo(createCard(P1, "1_75"), gandalf);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, glamdring);
        pass(P1);

        assertEquals(1, getBurdens());
        selectCardAction(P2, eyeOfBaradDur);
        assertEquals(3, getBurdens());
    }

    @Test
    public void playerDrawsCard() throws Exception {
        initializeSimplestGame();

        PhysicalCard verilyICome = attachTo(createCard(P2, "2_94"), _game.getGameState().getRingBearer(P1));
        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);

        putOnTopOfDeck(createCard(P1, "1_40"));

        assertEquals(1, getBurdens());
        selectCardAction(P1, elrond);
        assertEquals(2, getBurdens());
    }

    @Test
    public void beforeToil() throws Exception {
        initializeSimplestGame();

        PhysicalCard enqueaBlackThirst = addToZone(createCard(P2, "12_175"), Zone.SHADOW_CHARACTERS);
        PhysicalCard minasMorgulAnswers = addToZone(createCard(P2, "12_167"), Zone.HAND);

        passUntil(Phase.SHADOW);
        assertEquals(2, getTwilightPool());
        selectCardAction(P2, minasMorgulAnswers);
        selectCardAction(P2, enqueaBlackThirst);
        pass(P2);
        selectCard(P2, enqueaBlackThirst);
        assertEquals(2, getTwilightPool());
        assertEquals(2, getWounds(enqueaBlackThirst));
        assertEquals(Zone.DISCARD, minasMorgulAnswers.getZone());
    }

    @Test
    public void usesSpecialAbility() throws Exception {
        initializeSimplestGame();

        PhysicalCard hauntingHerSteps = addToZone(createCard(P2, "4_155"), Zone.HAND);
        PhysicalCard grimaWormtongue = addToZone(createCard(P2, "4_154"), Zone.SHADOW_CHARACTERS);

        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);

        PhysicalCard cardInDeck = putOnTopOfDeck(createCard(P1, "1_40"));

        selectCardAction(P1, elrond);
        selectCardAction(P2, hauntingHerSteps);

        assertEquals(1, getWounds(grimaWormtongue));
        assertEquals(Zone.DECK, cardInDeck.getZone());
    }

    @Test
    public void takesWound() throws Exception {
        initializeSimplestGame();

        PhysicalCard promiseKeeping = addToZone(createCard(P2, "8_24"), Zone.SUPPORT);
        PhysicalCard gollum = addToZone(createCard(P2, "7_58"), Zone.SHADOW_CHARACTERS);

        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + gollum.getCardId());
        selectCard(P1, ringBearer);
        pass(P1);
        pass(P2);

        // Don't use Ring
        pass(P1);
        assertEquals(2, getWounds(ringBearer));
    }

    @Test
    public void takesOffRing() throws Exception {
        initializeSimplestGame();

        PhysicalCard iSeeYou = addToZone(createCard(P2, "101_46"), Zone.SUPPORT);
        PhysicalCard gollum = addToZone(createCard(P2, "7_58"), Zone.SHADOW_CHARACTERS);

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard ring = getRing(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + gollum.getCardId());
        selectCard(P1, ringBearer);
        pass(P1);
        pass(P2);
        assertEquals(Zone.SUPPORT, iSeeYou.getZone());
        selectCardAction(P1, ring);
        assertEquals(Zone.DISCARD, iSeeYou.getZone());
    }

    @Test
    public void siteLiberated() throws Exception {
        initializeSimplestGame();

        PhysicalCard drivenIntoTheHills = addToZone(createCard(P2, "102_1"), Zone.SUPPORT);
        PhysicalCard theodenSonOfThengel = addToZone(createCard(P1, "4_292"), Zone.FREE_CHARACTERS);
        PhysicalCard guma = addToZone(createCard(P1, "4_277"), Zone.SUPPORT);

        passUntil(Phase.SHADOW);
        _game.getGameState().takeControlOfCard(P2, _game, _game.getGameState().getSite(1), Zone.SUPPORT);
        _game.getGameState().setPlayerPosition(P2, 2);

        passUntil(Phase.REGROUP);
        assertEquals(Zone.SUPPORT, drivenIntoTheHills.getZone());
        selectCardAction(P1, theodenSonOfThengel);
        assertEquals(Zone.DISCARD, drivenIntoTheHills.getZone());
    }

    @Test
    public void siteControlled() throws Exception {
        initializeSimplestGame();

        PhysicalCard youDoNotKnowPain = addToZone(createCard(P2, "17_138"), Zone.SUPPORT);
        PhysicalCard whiteHandIntruder = addToZone(createCard(P2, "17_127"), Zone.SHADOW_CHARACTERS);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        for (int i = 0; i < 2; i++) {
            addToZone(createCard(P2, "17_119"), Zone.SHADOW_CHARACTERS);
        }

        passUntil(Phase.SHADOW);
        _game.getGameState().setPlayerPosition(P2, 2);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + whiteHandIntruder.getCardId());
        pass(P2);
        selectCard(P1, gimli);

        pass(P1);
        pass(P2);
        selectCardAction(P2, whiteHandIntruder);
        assertEquals(2, getWounds(gimli));
        removeWounds(gimli, 1);
        assertEquals(1, getWounds(gimli));
        selectCardAction(P2, youDoNotKnowPain);
        assertEquals(2, getWounds(gimli));
    }

    @Test
    public void replacesSite() throws Exception {
        initializeSimplestGame();

        PhysicalCard riddermarkTactician = addToZone(createCard(P1, "13_133"), Zone.FREE_CHARACTERS);
        PhysicalCard theoden = addToZone(createCard(P1, "13_137"), Zone.FREE_CHARACTERS);
        PhysicalCard nelya = addToZone(createCard(P2, "11_222"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.SHADOW);
        replaceSite(_game.getGameState().getAdventureDeck(P1).get(0), 2);
        selectCardAction(P2, nelya);
        hasCardAction(P1, riddermarkTactician);
    }

    @Test
    public void removesBurden() throws Exception {
        initializeSimplestGame();

        PhysicalCard theShireCountryside = addToZone(createCard(P1, "3_113"), Zone.SUPPORT);
        PhysicalCard elfSong = addToZone(createCard(P1, "1_39"), Zone.HAND);
        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, elfSong);
        hasCardAction(P1, theShireCountryside);
    }

    @Test
    public void reconciles() throws Exception {
        initializeSimplestGame();

        PhysicalCard isildur = addToZone(createCard(P1, "13_71"), Zone.FREE_CHARACTERS);

        for (int i = 0; i < 5; i++) {
            addToZone(createCard(P1, "13_71"), Zone.HAND);
        }

        passUntil(Phase.REGROUP);
        pass(P1);
        pass(P2);
        assertEquals(Zone.FREE_CHARACTERS, isildur.getZone());
        playerAnswersNo(P1);
        pass(P1);
        hasCardAction(P1, isildur);
    }

    @Test
    public void putsOnRing() throws Exception {
        initializeSimplestGame();

        PhysicalCard theTwilightWorld = addToZone(createCard(P2, "1_228"), Zone.HAND);
        PhysicalCard nelya = addToZone(createCard(P2, "11_222"), Zone.SHADOW_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard ring = getRing(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        pass(P2);
        selectCard(P1, ringBearer);
        pass(P1);
        pass(P2);
        selectCardAction(P1, ring);
        assertEquals(1, getBurdens());
        selectCardAction(P2, theTwilightWorld);
        assertEquals(4, getBurdens());
        assertEquals(1, getWounds(nelya));
    }

    @Test
    public void movesTo() throws Exception {
        Map<String, LotroDeck> decks = new HashMap<>();
        decks.put(P1, createSpecialMovesToDeck());
        decks.put(P2, createSpecialMovesToDeck());
        initializeGameWithDecks(decks);

        passUntil(Phase.SHADOW);
        assertEquals(1, getWounds(getRingBearer(P1)));
    }

    public LotroDeck createSpecialMovesToDeck() {
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        lotroDeck.setRingBearer("10_121");
        lotroDeck.setRing("1_2");
        lotroDeck.addSite("7_330");
        lotroDeck.addSite("1_332");
        lotroDeck.addSite("8_117");
        lotroDeck.addSite("7_342");
        lotroDeck.addSite("7_345");
        lotroDeck.addSite("7_350");
        lotroDeck.addSite("8_120");
        lotroDeck.addSite("10_120");
        lotroDeck.addSite("7_360");
        return lotroDeck;
    }

    @Test
    public void movesFrom() throws Exception {
        initializeSimplestGame();

        PhysicalCard councilCourtyard = addToZone(createCard(P1, "1_337"), Zone.ADVENTURE_DECK);

        passUntil(Phase.FELLOWSHIP);
        replaceSite(councilCourtyard, 1);
        setTwilightPool(2);

        passUntil(Phase.SHADOW);
        assertEquals(2, getTwilightPool());
    }

    @Test
    public void losesInitiative() throws Exception {
        initializeSimplestGame();

        PhysicalCard glimpseOfFate = addToZone(createCard(P1, "10_12"), Zone.SUPPORT);
        PhysicalCard legolas1 = addToZone(createCard(P1, "1_50"), Zone.HAND);
        PhysicalCard legolas2 = addToZone(createCard(P1, "1_50"), Zone.HAND);
        PhysicalCard legolas3 = addToZone(createCard(P1, "1_50"), Zone.HAND);
        PhysicalCard legolas4 = addToZone(createCard(P1, "1_50"), Zone.HAND);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(5, getStrength(goblinRunner));
        selectCardAction(P1, legolas1);
        assertEquals(1, getStrength(goblinRunner));
    }

    @Test
    public void heals() throws Exception {
        initializeSimplestGame();

        PhysicalCard frodo = getRingBearer(P1);

        PhysicalCard lingeringShadow = attachTo(createCard(P2, "12_166"), frodo);
        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        addWounds(frodo, 1);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(1, getBurdens());
        selectCardAction(P1, theGaffer);
        assertEquals(0, getWounds(frodo));
        assertEquals(2, getBurdens());
    }

    @Test
    public void fpStartedAssigning() throws Exception {
        initializeSimplestGame();

        PhysicalCard nerteaDarkHorseman = addToZone(createCard(P2, "69_38"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        assertNotNull(getMultipleDecisionIndex(getAwaitingDecision(P1), "Yes"));
    }

    @Test
    public void fpDecidedToStay() throws Exception {
        initializeSimplestGame();

        PhysicalCard veryNiceFriends = addToZone(createCard(P1, "7_76"), Zone.HAND);
        PhysicalCard smeagol = addToZone(createCard(P1, "7_71"), Zone.DISCARD);
        PhysicalCard legolas1 = addToZone(createCard(P1, "1_50"), Zone.HAND);

        passUntil(Phase.REGROUP);
        selectCardAction(P1, veryNiceFriends);
        pass(P2);
        pass(P1);
        selectNo(P1);
        assertEquals(Zone.DISCARD, legolas1.getZone());
    }

    @Test
    public void exerted() throws Exception {
        initializeSimplestGame();

        PhysicalCard demandsOfSackvilleBagginses = addToZone(createCard(P2, "2_40"), Zone.SUPPORT);
        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(0, getTwilightPool());
        selectCardAction(P1, theGaffer);
        assertEquals(1, getTwilightPool());
    }

    @Test
    public void endOfTurn() throws Exception {
        initializeSimplestGame();

        PhysicalCard sarumansChill = addToZone(createCard(P2, "1_134"), Zone.SUPPORT);

        passUntil(Phase.REGROUP);
        pass(P1);
        pass(P2);
        selectNo(P1);
        assertEquals(Zone.DISCARD, sarumansChill.getZone());
    }

    @Test
    public void discardedFromPlay() throws Exception {
        initializeSimplestGame();

        PhysicalCard swordRack = addToZone(createCard(P1, "11_158"), Zone.SUPPORT);
        PhysicalCard beyondTheHeightOfMen = addToZone(createCard(P2, "2_39"), Zone.HAND);
        PhysicalCard urukScout = addToZone(createCard(P2, "2_47"), Zone.SHADOW_CHARACTERS);

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard heavyChain = attachTo(createCard(P1, "4_278"), ringBearer);

        passUntil(Phase.MANEUVER);
        pass(P1);
        assertEquals(Zone.ATTACHED, heavyChain.getZone());
        selectCardAction(P2, beyondTheHeightOfMen);
        assertEquals(Zone.DISCARD, heavyChain.getZone());
        selectCardAction(P1, swordRack);
        assertEquals(Zone.STACKED, heavyChain.getZone());
    }

    @Test
    public void beforeThreatWounds() throws Exception {
        initializeSimplestGame();

        PhysicalCard mordorAssassin = addToZone(createCard(P2, "7_284"), Zone.SHADOW_CHARACTERS);
        PhysicalCard attea = addToZone(createCard(P2, "1_229"), Zone.SHADOW_CHARACTERS);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        addThreats(P1, 2);
        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + attea.getCardId());
        playerDecided(P2, gimli.getCardId() + " " + mordorAssassin.getCardId());
        selectCard(P1, gimli);
        pass(P1);
        pass(P2);
        selectCardAction(P2, mordorAssassin);
        assertTrue(_game.getGameState().getAssignments().stream().anyMatch(assignment ->
                assignment.getFellowshipCharacter() == ringBearer && assignment.getShadowCharacters().stream().anyMatch(shadowCharacter -> shadowCharacter == mordorAssassin)));
    }

    @Test
    public void assignedToSkirmish() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard desperateDefenseOfTheRing = attachTo(createCard(P2, "1_244"), ringBearer);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        assertEquals(1, getBurdens());
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        assertEquals(2, getBurdens());
    }

    @Test
    public void assignedAgainst() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard unferth = addToZone(createCard(P2, "4_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        assertEquals(0, getWounds(ringBearer));
        playerDecided(P1, ringBearer.getCardId() + " " + unferth.getCardId());
        assertEquals(1, getWounds(ringBearer));
    }

    @Test
    public void afterAllSkirmishes() throws Exception {
        initializeSimplestGame();

        PhysicalCard witchKing = addToZone(createCard(P2, "12_183"), Zone.SHADOW_CHARACTERS);
        PhysicalCard witchKingsBeast = attachTo(createCard(P2, "12_184"), witchKing);

        passUntil(Phase.ASSIGNMENT);
        // Normal Assignment actions
        pass(P1);
        pass(P2);
        // Normal Assignments
        pass(P1);
        pass(P2);
        // Fierce Assignment actions
        pass(P1);
        pass(P2);
        // Fierce Assignments
        pass(P1);
        pass(P2);
        hasCardAction(P2, witchKingsBeast);
    }

    @Test
    public void addsThreat() throws Exception {
        initializeSimplestGame();

        PhysicalCard tomBombadilsHat = addToZone(createCard(P1, "0_60"), Zone.SUPPORT);
        PhysicalCard warTowers = addToZone(createCard(P2, "7_173"), Zone.HAND);
        PhysicalCard desertNomad = addToZone(createCard(P2, "7_132"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        pass(P1);
        selectCardAction(P2, warTowers);
        assertEquals(1, getCultureTokens(tomBombadilsHat, Culture.SHIRE));
    }

    @Test
    public void addsBurden() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard tomBombadilsHat = addToZone(createCard(P1, "0_60"), Zone.SUPPORT);
        PhysicalCard desperateDefenseOfTheRing = attachTo(createCard(P2, "1_244"), ringBearer);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        assertEquals(1, getCultureTokens(tomBombadilsHat, Culture.SHIRE));
    }


    @Test
    public void aboutToTakeControlOfSite() throws Exception {
        initializeSimplestGame();

        PhysicalCard whiteHandIntruder = addToZone(createCard(P2, "17_127"), Zone.SHADOW_CHARACTERS);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        for (int i = 0; i < 2; i++) {
            addToZone(createCard(P2, "17_119"), Zone.SHADOW_CHARACTERS);
        }
        PhysicalCard ceorl = addToZone(createCard(P1, "4_264"), Zone.SUPPORT);
        PhysicalCard guma = addToZone(createCard(P1, "4_277"), Zone.SUPPORT);

        passUntil(Phase.SHADOW);
        _game.getGameState().setPlayerPosition(P2, 2);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + whiteHandIntruder.getCardId());
        pass(P2);
        selectCard(P1, gimli);

        pass(P1);
        pass(P2);
        selectCardAction(P2, whiteHandIntruder);
        hasCardAction(P1, ceorl);
    }

    @Test
    public void aboutToHeal() throws Exception {
        initializeSimplestGame();

        PhysicalCard frodo = getRingBearer(P1);

        PhysicalCard extraordinaryResilience = addToZone(createCard(P1, "1_287"), Zone.HAND);
        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        addWounds(frodo, 1);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(1, getBurdens());
        selectCardAction(P1, theGaffer);
        selectCardAction(P1, extraordinaryResilience);
        assertEquals(1, getWounds(frodo));
        assertEquals(0, getBurdens());
    }

    @Test
    public void aboutToExert() throws Exception {
        initializeSimplestGame();

        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard glamdring = attachTo(createCard(P1, "1_75"), gandalf);
        PhysicalCard strengthOfSpirit = addToZone(createCard(P1, "1_85"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, glamdring);
        selectCardAction(P1, strengthOfSpirit);
        assertEquals(0, getWounds(gandalf));
    }

    @Test
    public void aboutToDrawCard() throws Exception {
        initializeSimplestGame();

        PhysicalCard mirrorDangerousGuide = addToZone(createCard(P1, "15_22"), Zone.SUPPORT);
        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard elrondCopy = putOnTopOfDeck(createCard(P1, "1_40"));
        selectCardAction(P1, elrond);
        hasCardAction(P1, mirrorDangerousGuide);
    }

    @Test
    public void aboutToBeOverwhelmed() throws Exception {
        initializeSimplestGame();

        PhysicalCard attea1 = addToZone(createCard(P2, "1_229"), Zone.SHADOW_CHARACTERS);
        PhysicalCard attea2 = addToZone(createCard(P2, "1_229"), Zone.SHADOW_CHARACTERS);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard footmansArmor = attachTo(createCard(P1, "7_93"), gimli);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard footmansArmorInHand = addToZone(createCard(P1, "7_93"), Zone.HAND);
        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + attea1.getCardId());
        playerDecided(P2, gimli.getCardId() + " " + attea2.getCardId());
        selectCard(P1, gimli);
        pass(P1);
        pass(P2);
        hasCardAction(P1, footmansArmor);
    }

    @Test
    public void aboutToAddTwilight() throws Exception {
        initializeSimplestGame();

        PhysicalCard threeMonstrousTrolls = addToZone(createCard(P1, "3_114"), Zone.SUPPORT);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.HAND);
        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(1);

        passUntil(Phase.SHADOW);
        selectCardAction(P2, goblinRunner);
        assertEquals(0, getTwilightPool());
        selectCardAction(P1, threeMonstrousTrolls);
        assertEquals(1, getWounds(theGaffer));
        assertEquals(0, getTwilightPool());
        assertEquals(Zone.SHADOW_CHARACTERS, goblinRunner.getZone());
    }

    @Test
    public void aboutToAddBurden() throws Exception {
        initializeSimplestGame();

        PhysicalCard melilotBrandybuck = addToZone(createCard(P1, "3_110"), Zone.SUPPORT);
        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard desperateDefenseOfTheRing = attachTo(createCard(P2, "1_244"), ringBearer);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        assertEquals(1, getBurdens());
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        selectCardAction(P1, melilotBrandybuck);
        assertEquals(1, getBurdens());
        assertEquals(1, getWounds(melilotBrandybuck));
    }
}
