package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gate", "103_23");
					put("aragorn", "1_89"); // Gondor companion
					put("knight1", "8_39"); // Knight of Dol Amroth
					put("knight2", "8_39");
					put("knight3", "8_39");
					put("knight4", "8_39");

					put("hollowing1", "3_54"); // Shadow support - discards self to add twilight
					put("hollowing2", "3_54");
					put("hollowing3", "3_54");
					put("hollowing4", "3_54");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatGateofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Great Gate of Minas Tirith
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Fortification.  To play, exert a [gondor] companion (or spot 3 knights).
		* 	Each time the special ability of a Shadow support card is used, you may hinder a fortification to hinder that card.  Then you may exert X knights to hinder X other copies of that card with the same title.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gate");

		assertEquals("Great Gate of Minas Tirith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void GreatGateRequiresExertingGondorCompanionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gate);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsPlayAvailable(gate));
		scn.FreepsPlayCard(gate);

		// Aragorn exerted as extra cost
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, gate);
	}

	@Test
	public void GreatGateCanBePlayedBySpotting3KnightsWithoutExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");

		scn.MoveCompanionsToTable(knight1, knight2, knight3);
		scn.MoveCardsToHand(gate);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertEquals(0, scn.GetWoundsOn(knight3));

		assertTrue(scn.FreepsPlayAvailable(gate));
		scn.FreepsPlayCard(gate);

		// No exerts required with 3 knights
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertEquals(0, scn.GetWoundsOn(knight3));
		assertInZone(Zone.SUPPORT, gate);
	}

	@Test
	public void GreatGateResponseHindersShadowSupportAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hollowing1 = scn.GetShadowCard("hollowing1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gate, hollowing1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		assertFalse(scn.IsHindered(gate));
		scn.FreepsPass();

		int twilightBefore = scn.GetTwilight();

		// Shadow activates Hollowing (discards self to add 3 twilight)
		assertTrue(scn.ShadowActionAvailable(hollowing1));
		scn.ShadowUseCardAction(hollowing1);

		// Hollowing discards itself and adds twilight
		assertInZone(Zone.DISCARD, hollowing1);

		// Response should be offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Choose fortification to hinder (gate is only option)
		assertTrue(scn.IsHindered(gate));

		//Hollowing's effect still went through
		assertEquals(twilightBefore + 3, scn.GetTwilight());

		// Hollowing was already discarded, so hinder effect applies to it in discard
		// No other copies to hinder, so no knight exert prompt
	}

	@Test
	public void GreatGateResponseExertsKnightsToHinderOtherCopies() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");
		var hollowing1 = scn.GetShadowCard("hollowing1");
		var hollowing2 = scn.GetShadowCard("hollowing2");
		var hollowing3 = scn.GetShadowCard("hollowing3");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(knight1, knight2, knight3);
		scn.MoveCardsToSupportArea(gate, hollowing1, hollowing2, hollowing3);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		assertFalse(scn.IsHindered(hollowing2));
		assertFalse(scn.IsHindered(hollowing3));

		scn.FreepsPass();

		// Shadow activates Hollowing1
		scn.ShadowUseCardAction(hollowing1);

		// Response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Hinder gate
		// Now choose knights to exert (2 other Hollowings in play, can exert up to 2)
		scn.FreepsChooseCards(knight1, knight2);

		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertEquals(0, scn.GetWoundsOn(knight3));

		assertTrue(scn.IsHindered(hollowing2));
		assertTrue(scn.IsHindered(hollowing3));
	}

	@Test
	public void GreatGateResponseCanChooseZeroKnightsToExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var hollowing1 = scn.GetShadowCard("hollowing1");
		var hollowing2 = scn.GetShadowCard("hollowing2");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(gate, hollowing1, hollowing2);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.FreepsPass();

		// Shadow activates Hollowing1
		scn.ShadowUseCardAction(hollowing1);

		// Response
		scn.FreepsAcceptOptionalTrigger();

		// Choose 0 knights to exert
		scn.FreepsDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(knight1));

		// Hollowing2 should not be hindered
		assertFalse(scn.IsHindered(hollowing2));
	}


	@Test
	public void GreatGateResponseCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hollowing1 = scn.GetShadowCard("hollowing1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gate, hollowing1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.FreepsPass();

		// Shadow activates Hollowing1
		scn.ShadowUseCardAction(hollowing1);

		// Decline response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Gate not hindered
		assertFalse(scn.IsHindered(gate));
	}
}
