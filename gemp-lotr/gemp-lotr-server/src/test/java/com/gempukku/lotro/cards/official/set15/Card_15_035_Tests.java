package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow1", "15_35");
					put("shadow2", "15_35");

					// [gandalf] companions of three races
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("dasron", "13_29");     // Dasron, Merchant From Dorwinion: [gandalf] Man companion; his trigger wants 2 tokens to remove, and there are none
					put("onod", "17_26");       // Woodland Onod: [gandalf] Ent companion, cost 4; a skirmish ability used only on purpose
					// Cards that are not [gandalf] companions
					put("ottar", "1_80");       // Ottar, Man of Laketown: a [gandalf] ally; to play, spot Gandalf (cheated in)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("laststand", "18_21");  // Last Stand: a [gandalf] condition, with no token
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, cost 4, to read in hand beside this card

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowoftheWoodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Shadow of the Wood
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 6
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: This companion is twilight cost -1 for each [gandalf] companion you can spot.<br><b>Fellowship:</b> Place this companion in the dead pile to remove (5).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadow1");

		assertEquals("Shadow of the Wood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void IsTwilightCostMinusOneForEachGandalfCompanionSpottedAndForNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow1 = scn.GetFreepsCard("shadow1");
		var shadow2 = scn.GetFreepsCard("shadow2");
		var onod = scn.GetFreepsCard("onod");
		var avenger = scn.GetFreepsCard("avenger");
		var laststand = scn.GetFreepsCard("laststand");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("dasron"), scn.GetFreepsCard("aragorn"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("ottar"));
		scn.MoveCardsToSupportArea(laststand);
		scn.MoveCardsToHand(shadow1, shadow2, onod, avenger);

		scn.StartGame();

		assertInZone(Zone.HAND, shadow1, shadow2, onod, avenger);
		assertInZone(Zone.SUPPORT, laststand);
		// Gandalf and Dasron count; Aragorn ([gondor]), Ottar (an ally) and Last Stand (a condition) do not:
		// 6 - 2, where counting any of them would read 3 or less.
		assertEquals(4, scn.GetTwilightCost(shadow1));
		assertEquals(4, scn.GetTwilightCost(shadow2));
		// Another companion in hand is discounted by nothing.
		assertEquals(4, scn.GetTwilightCost(avenger));

		scn.FreepsPlayCard(onod);
		assertInZone(Zone.FREE_CHARACTERS, onod);
		// And an Ent: 6 - 3
		assertEquals(3, scn.GetTwilightCost(shadow1));
		assertEquals(3, scn.GetTwilightCost(shadow2));
		assertEquals(4, scn.GetTwilightCost(avenger));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(shadow1);

		assertInZone(Zone.FREE_CHARACTERS, shadow1);
		// 6 printed - 3 for Gandalf, Dasron and Woodland Onod
		assertEquals(twilight + 3, scn.GetTwilight());
		// The first copy, now in play, is a [gandalf] companion for the second: 6 - 4
		assertEquals(2, scn.GetTwilightCost(shadow2));
		assertEquals(4, scn.GetTwilightCost(avenger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityPlacesThisCompanionInTheDeadPileToRemoveFiveAndIsNoManeuverAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow1 = scn.GetFreepsCard("shadow1");
		var shadow2 = scn.GetFreepsCard("shadow2");
		scn.MoveCompanionsToTable(shadow1, shadow2);

		scn.StartGame();
		scn.SetTwilight(7);

		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(shadow1));
		assertTrue(scn.FreepsActionAvailable(shadow2));

		scn.FreepsUseCardAction(shadow1);

		// Placed in the dead pile, not the discard pile; the other copy stays in play.
		assertInZone(Zone.DEAD, shadow1);
		assertInZone(Zone.FREE_CHARACTERS, shadow2);
		assertEquals(0, scn.GetFreepsDiscardCount());
		// 7 - 5
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 left + 2 for Ettenmoors + 2 for Frodo and the second copy
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shadow2));
	}

	@Test
	public void PlacedInTheDeadPileItIsKilledSoTheThreatsBecomeWoundsAndWithThreeInThePoolThoseThreeAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var shadow1 = scn.GetFreepsCard("shadow1");
		var shadow2 = scn.GetFreepsCard("shadow2");
		scn.MoveCompanionsToTable(shadow1, shadow2);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.AddThreats(1);

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(shadow2));

		scn.FreepsUseCardAction(shadow1);

		// CR 4.2 "dead pile": a character placed there by game text has been killed, and CR 4.2 "threats": the
		// threat is removed and its wound placed on a companion of the Free Peoples player's choice.
		assertInZone(Zone.DEAD, shadow1);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, shadow2));
		scn.FreepsChooseCard(shadow2);

		assertEquals(1, scn.GetWoundsOn(shadow2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Fewer than 5 in the pool: as much as there is is removed.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
