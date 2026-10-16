package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_278_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "1_278");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability only
					put("hunters", "1_267");    // Orc Hunters: [sauron] Orc, strength 9, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; adds (2) only when a weapon is played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; strength 9, damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, an Orc who is not [sauron]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthBornofFearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Strength Born of Fear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	Skirmish: Remove (3) to make a [sauron] Orc strength +1.
		*/

		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");

		assertEquals("Strength Born of Fear", strength.getBlueprint().getTitle());
		assertNull(strength.getBlueprint().getSubtitle());
		assertFalse(strength.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, strength.getBlueprint().getSide());
		assertEquals(Culture.SAURON, strength.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, strength.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(strength, Keyword.SUPPORT_AREA));
		assertEquals(1, strength.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityRemovesThreeToMakeASauronOrcStrengthPlusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var soldier = scn.GetShadowCard("soldier");
		var hunters = scn.GetShadowCard("hunters");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, hunters, chieftain, guard, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, soldier }, new PhysicalCardImpl[] { gimli, hunters });
		// The [moria] Orc and the [sauron] Uruk-hai go into Aragorn's skirmish beside the Soldier; the Chieftain is
		// left unassigned.
		scn.ShadowAssignToMinions(aragorn, troop, guard);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, soldier));
		assertTrue(scn.IsCharAssignedAgainst(gimli, hunters));
		assertFalse(scn.IsCharAssigned(chieftain));

		scn.FreepsResolveSkirmish(aragorn);
		// Exactly the (3) the ability removes.
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(strength));
		assertEquals(3, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(hunters));
		assertEquals(7, scn.GetStrength(chieftain));

		scn.ShadowUseCardAction(strength);
		// A [sauron] Orc in this skirmish, one waiting for his own, and one in none; not the [moria] Orc nor the
		// [sauron] Uruk-hai fighting in this skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, hunters, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hunters);

		assertEquals(0, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(hunters));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own (CR 4.2 "phase action"): in the next, the Hunters are back to 9.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, hunters));
		assertEquals(9, scn.GetStrength(hunters));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithTwoTwilightInThePoolAndIsWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(strength));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(strength));
	}

	@Test
	public void WithNoSauronOrcInPlayItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(strength));
		assertEquals(3, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(guard));

		// The (3) is paid, and no minion is made stronger.
		scn.ShadowUseCardAction(strength);

		assertEquals(0, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
