package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brood", "1_145");
					put("brood2", "1_145");

					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, damage +1; no other text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1; no other text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, no Uruk-hai; a regroup ability only
					put("saruman", "17_116");   // Saruman, Master of the White Hand: [uruk-hai] Wizard, no Uruk-hai; companions resistance -1, a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukBroodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Brood
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Skirmish:</b> Remove (2) to make this minion strength +1 for each other Uruk-hai you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brood");

		assertEquals("Uruk Brood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovesTwoToMakeThisMinionStrongerByAsManyOtherUrukHaiAsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brood = scn.GetShadowCard("brood");
		var brood2 = scn.GetShadowCard("brood2");
		var guard = scn.GetShadowCard("guard");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(brood, brood2, guard, savage, worker, saruman);

		// Both copies skirmish Aragorn; the other four minions are left out of every skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brood);
		scn.ShadowAssignToMinions(aragorn, brood2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(brood));
		assertEquals(9, scn.GetStrength(brood2));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(brood));

		scn.ShadowUseCardAction(brood);
		// The other copy, the Guard and the Savage are the other Uruk-hai: not the Brood itself, the Orc or the Wizard.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");
		assertEquals(12, scn.GetStrength(brood));
		assertEquals(9, scn.GetStrength(brood2));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(savage));

		// Used again, spotting fewer than there are.
		scn.FreepsPass();
		scn.ShadowUseCardAction(brood);
		assertEquals(1, scn.GetTwilight());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		assertEquals(13, scn.GetStrength(brood));
		assertEquals(9, scn.GetStrength(brood2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherUrukHaiToSpotItIsStillOfferedAndTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brood = scn.GetShadowCard("brood");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(brood, worker, saruman);

		// The Brood is the only Uruk-hai: the Orc and the Wizard beside it are not.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brood);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(brood));
		// The ability has a cost, so it is offered though it can add nothing (Q-0034).
		assertTrue(scn.ShadowActionAvailable(brood));

		scn.ShadowUseCardAction(brood);
		// With none to spot no number is asked for; the (2) is paid and the strength is as it was.
		assertEquals(0, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(brood));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedUntilThereAreTwoTwilightTokensToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brood = scn.GetShadowCard("brood");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(brood, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brood);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(brood));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(brood));
	}

	@Test
	public void UsedOutOfAnySkirmishItsBonusLastsThatSkirmishAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brood = scn.GetShadowCard("brood");
		var guard = scn.GetShadowCard("guard");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(brood, guard, savage);

		// The Brood is left unassigned; Gimli's skirmish is fought first, then Aragorn's.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, guard});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(brood));
		assertTrue(scn.ShadowActionAvailable(brood));
		scn.ShadowUseCardAction(brood);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertEquals(11, scn.GetStrength(brood));

		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(brood));

		scn.PassSkirmishActions();
		// 6 against 5: Gimli wins, and his damage +1 makes it 2 wounds; Aragorn's skirmish is next.
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(brood));
	}
}
