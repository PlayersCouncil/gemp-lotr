package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axeman", "11_69");
					put("axeman2", "11_69");
					// Corps of Harad: another [men] Man minion of cost 4, to read beside the Axeman in hand
					put("corps", "11_73");
					// Sam, Son of Hamfast: a Ring-bound Hobbit, resistance 5; "Fellowship: Exert Sam to remove a burden"
					put("sam", "1_311");
					// Aragorn, Ranger of the North: an unbound [gondor] Man, resistance 6; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Barliman Butterbur: an ally, and so of resistance 0 (CR 4.2 "resistance"); an ability used only on purpose
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxemanofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Axeman of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you can spot a companion who has resistance 3 or less, this minion's twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("axeman");

		assertEquals("Axeman of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSamHasResistance3TheAxemanCostsTwoLessAndNotOnceHisBurdenIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		var corps = scn.GetShadowCard("corps");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(axeman, axeman2, corps);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		// 5 - 2 burdens; Frodo 10 - 2
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(axeman));
		// A second copy has its own -2 and no more: the two discounts do not reach each other.
		assertEquals(2, scn.GetTwilightCost(axeman2));
		// The other [men] minion in the same hand keeps its printed cost: the discount is the Axeman's own.
		assertEquals(4, scn.GetTwilightCost(corps));

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(4, scn.GetTwilightCost(axeman));
		assertEquals(4, scn.GetTwilightCost(axeman2));
		assertEquals(4, scn.GetTwilightCost(corps));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWhileACompanionHasResistance3ItTakesTwoLessFromThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(axeman);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(3, scn.GetResistance(sam));
		scn.FreepsPass();

		// At site 2 the Axeman (site 4) is roaming: 4 - 2 + 2 roaming. Without the discount it would want 6,
		// and with -3 it would be offered from 3.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(axeman));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(axeman));
		scn.ShadowPlayCard(axeman);

		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUnboundManAtResistance3CountsWhereAnAllyOfResistance0DoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(axeman);

		scn.StartGame();
		scn.AddBurdens(2);

		// Every companion above 3; the ally has resistance 0 but is no companion.
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(barliman));
		assertEquals(4, scn.GetTwilightCost(axeman));

		scn.AddBurdens(1);

		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(axeman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
