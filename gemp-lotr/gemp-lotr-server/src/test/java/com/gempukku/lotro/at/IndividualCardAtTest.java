package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class IndividualCardAtTest extends AbstractAtTest {
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
}
