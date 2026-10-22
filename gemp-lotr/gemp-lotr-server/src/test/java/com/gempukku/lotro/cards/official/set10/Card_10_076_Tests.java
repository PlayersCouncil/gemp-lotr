package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauder1", "10_76");
					put("marauder2", "10_76");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, site 6, strength 9; "Damage +1." is its whole text
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, site 6, strength 7; answers only a weapon played on him
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, site 4, strength 7; a skirmish ability used only on purpose
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, site 5, strength 9; "Damage +1." is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AdvanceMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Advance Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert this minion to make a roaming [sauron] minion strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauder1");

		assertEquals("Advance Marauder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsThisMinionToMakeARoamingSauronMinionStrengthPlus3ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder1 = scn.GetShadowCard("marauder1");
		var marauder2 = scn.GetShadowCard("marauder2");
		var guard = scn.GetShadowCard("guard");
		var chieftain = scn.GetShadowCard("chieftain");
		var lights = scn.GetShadowCard("lights");
		var party = scn.GetShadowCard("party");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(marauder1, marauder2, guard, chieftain, lights, party);
		// The fellowship moves to site 4.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, marauder1}, new PhysicalCardImpl[]{gimli, guard});
		// Both Marauders in one skirmish; the Chieftain, Corpse Lights and the Raiding Party left unassigned.
		scn.ShadowAssignToMinions(aragorn, marauder2);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(marauder1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(party, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lights, Keyword.ROAMING));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(marauder1));
		assertEquals(0, scn.GetWoundsOn(marauder2));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.ShadowActionAvailable(marauder1));

		scn.ShadowUseCardAction(marauder1);

		// This Marauder exerts, and not the other in the same skirmish.
		assertEquals(1, scn.GetWoundsOn(marauder1));
		assertEquals(0, scn.GetWoundsOn(marauder2));
		// Any roaming [sauron] minion: this one, the other in his skirmish, an Uruk-hai waiting for his own
		// skirmish, an Orc in none.  Not a [sauron] minion that is not roaming, nor a roaming [isengard] one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(marauder1, marauder2, guard, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lights, party, aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(12, scn.GetStrength(guard));
		assertEquals(9, scn.GetStrength(marauder1));
		assertEquals(9, scn.GetStrength(marauder2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 18: Aragorn is overwhelmed.  The +3 lasted that skirmish only, and is gone in the Guard's own.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder1 = scn.GetShadowCard("marauder1");
		var marauder2 = scn.GetShadowCard("marauder2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(marauder1, marauder2);
		// Vitality 3 with 2 wounds: this Marauder cannot exert.  The other can.
		scn.AddWoundsToChar(marauder1, 2);
		assertEquals(2, scn.GetWoundsOn(marauder1));
		assertEquals(0, scn.GetWoundsOn(marauder2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, marauder1);
		scn.ShadowAssignToMinions(aragorn, marauder2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marauder1));
		assertTrue(scn.ShadowActionAvailable(marauder2));
	}

	@Test
	public void WithNoRoamingSauronMinionTheAbilityIsStillOfferedAndItsExertionMakesNoOneStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder1 = scn.GetShadowCard("marauder1");
		var lights = scn.GetShadowCard("lights");
		var party = scn.GetShadowCard("party");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(marauder1);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(lights, party);
		scn.SetTwilight(10);
		// The fellowship moves to site 6, where no minion here is roaming, and the Marauder is played there.
		scn.FreepsPass();
		scn.ShadowPlayCard(marauder1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, marauder1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(marauder1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lights, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(party, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(marauder1));
		assertTrue(scn.ShadowActionAvailable(marauder1));

		scn.ShadowUseCardAction(marauder1);

		// The exertion is paid; there is no roaming [sauron] minion to strengthen, and nothing is asked.
		assertEquals(1, scn.GetWoundsOn(marauder1));
		assertEquals(9, scn.GetStrength(marauder1));
		assertEquals(7, scn.GetStrength(lights));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
