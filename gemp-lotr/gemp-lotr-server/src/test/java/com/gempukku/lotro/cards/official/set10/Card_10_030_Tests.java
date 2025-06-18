package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

// Wielder of the Flame
public class Card_10_030_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("endgame", "10_30");
                    put("aragorn", "1_89");

                    put("troop", "1_143");
                }}
		);
	}

	@Test
	public void EndoftheGameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: End of the Game
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
         * Game Text: Make an exhausted [GONDOR] companion strength +2. If that companion wins this skirmish, heal that
         *   companion or make him or her damage +1.
		*/

		var scn = GetScenario();

        var card = scn.GetFreepsCard("endgame");

		assertEquals("End of the Game", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

    @Test
    public void DoesNotWorkIfNoExhaustedGondorCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");
        scn.MoveMinionsToTable(troop);

		scn.StartGame();

        scn.AddWoundsToChar(frodo, 3);

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, troop);
        scn.FreepsResolveSkirmish(aragorn);

        //As Aragorn isn't exhausted (tho Frodo is), the event does nothing.
        assertTrue(scn.FreepsPlayAvailable(endgame));
        assertEquals(8, scn.GetStrength(aragorn));
        scn.FreepsPlayCard(endgame);
        assertEquals(8, scn.GetStrength(aragorn));
        //Passing skirmish actions
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();
        //no other decisions occur from the event
        assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
    }

    @Test
    public void AddsStrengthToExhaustedGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");
        scn.MoveMinionsToTable(troop);

        scn.StartGame();

        scn.AddWoundsToChar(aragorn, 3);

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, troop);
        scn.FreepsResolveSkirmish(aragorn);

        assertTrue(scn.FreepsPlayAvailable(endgame));

        assertEquals(8, scn.GetStrength(aragorn));
        scn.FreepsPlayCard(endgame);
        assertEquals(10, scn.GetStrength(aragorn));
    }

    @Test
    public void WinningSkirmishCanHealCompanion() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");
        scn.MoveMinionsToTable(troop);

        scn.StartGame();

        scn.AddWoundsToChar(aragorn, 3);

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, troop);
        scn.FreepsResolveSkirmish(aragorn);

        assertTrue(scn.FreepsPlayAvailable(endgame));

        assertEquals(8, scn.GetStrength(aragorn));
        scn.FreepsPlayCard(endgame);
        assertEquals(10, scn.GetStrength(aragorn));
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.FreepsActionAvailable("Required trigger from"));
        scn.FreepsResolveActionOrder("Required trigger from");
        scn.FreepsChooseOption("Heal");
        assertEquals(2, scn.GetWoundsOn(aragorn));
        assertEquals(1, scn.GetWoundsOn(troop));
    }

    @Test
    public void WinningSkirmishCanAddDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");
        scn.MoveMinionsToTable(troop);

        scn.StartGame();

        scn.AddWoundsToChar(aragorn, 3);

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, troop);
        scn.FreepsResolveSkirmish(aragorn);

        assertTrue(scn.FreepsPlayAvailable(endgame));

        assertEquals(8, scn.GetStrength(aragorn));
        scn.FreepsPlayCard(endgame);
        assertEquals(10, scn.GetStrength(aragorn));
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.FreepsActionAvailable("Required trigger from"));
        scn.FreepsResolveActionOrder("Required trigger from");
        scn.FreepsChooseOption("damage +1");
        assertEquals(3, scn.GetWoundsOn(aragorn));
        assertEquals(2, scn.GetWoundsOn(troop));
	}
}
