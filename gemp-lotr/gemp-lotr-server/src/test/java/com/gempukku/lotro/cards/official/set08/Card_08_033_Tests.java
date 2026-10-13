package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_33");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [gondor] Wraiths, neither exhausted, and a minion that overwhelms one of them.
	protected VirtualTableScenario GetTwoWraithsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("edict", "8_33");
					put("oathbreaker", "8_41");
					put("sleepless", "8_45");
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElessarsEdictStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Elessar's Edict
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a [gondor] Wraith is about to be killed, discard him and either exhaust another [gondor] Wraith or spot another exhausted [gondor] Wraith to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Elessar's Edict", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "either exhaust another [gondor] Wraith or spot another exhausted [gondor] Wraith": the definition let any
	// [gondor] Wraith be spotted, so the other Wraith need never be exhausted.
	@Test
	public void AnotherWraithWhoIsNotExhaustedMustBeExhaustedAndCannotBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWraithsScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		scn.MoveCompanionsToTable(oathbreaker, sleepless);
		scn.MoveCardsToHand(edict);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.FreepsResolveSkirmish(oathbreaker);

		// 6 against 13: Oathbreaker is overwhelmed and about to be killed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsPlayAvailable(edict));
		scn.FreepsPlayCard(edict);

		// He is discarded and not killed.  Sleepless Dead is not exhausted, so there is no "exhausted [gondor]
		// Wraith" to spot: he is exhausted, and with one thing to do nothing is asked.
		assertInZone(Zone.DISCARD, oathbreaker);
		assertEquals(2, scn.GetWoundsOn(sleepless));
		assertTrue(scn.IsExhausted(sleepless));
		assertInZone(Zone.DISCARD, edict);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ElessarsEdictTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
