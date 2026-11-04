package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("traveler", "18_130");
					put("traveler2", "18_130");

					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] Wizard, no Uruk-hai; strength 8;
					                            // companions resistance -1, a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1, no other text
					put("endless", "13_161");   // Endless Assault: an [uruk-hai] condition; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandTravelerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Traveler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> Spot 2 other [uruk-hai] minions and remove (2) to make this
		 * 	minion strength +1 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var traveler = scn.GetFreepsCard("traveler");

		assertEquals("White Hand Traveler", traveler.getBlueprint().getTitle());
		assertNull(traveler.getBlueprint().getSubtitle());
		assertFalse(traveler.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, traveler.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, traveler.getBlueprint().getCulture());
		assertEquals(CardType.MINION, traveler.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, traveler.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(traveler, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(traveler, Keyword.DAMAGE));
		assertEquals(3, traveler.getBlueprint().getTwilightCost());
		assertEquals(8, traveler.getBlueprint().getStrength());
		assertEquals(2, traveler.getBlueprint().getVitality());
		assertEquals(5, traveler.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingAnotherTravelerAndAnUrukHaiCultureWizardItRemovesTwoToMakeItselfStrongerAndDamagePlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler = scn.GetShadowCard("traveler");
		var traveler2 = scn.GetShadowCard("traveler2");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, traveler2, saruman);

		// Both Travelers skirmish Aragorn, fought first; Saruman waits on Gimli.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, traveler}, new PhysicalCardImpl[]{gimli, saruman});
		scn.ShadowAssignToMinions(aragorn, traveler2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(traveler));
		assertEquals(1, scn.GetKeywordCount(traveler, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(traveler2));
		assertEquals(1, scn.GetKeywordCount(traveler2, Keyword.DAMAGE));

		// The two others are an [uruk-hai] Uruk-hai and an [uruk-hai] Wizard.
		assertTrue(scn.ShadowActionAvailable(traveler));
		scn.ShadowUseCardAction(traveler);

		assertEquals(1, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(traveler));
		assertEquals(2, scn.GetKeywordCount(traveler, Keyword.DAMAGE));
		// "This minion": the other copy, in the same skirmish, is untouched.
		assertEquals(8, scn.GetStrength(traveler2));
		assertEquals(1, scn.GetKeywordCount(traveler2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneOtherUrukHaiCultureMinionBesideASauronUrukHaiAndAnUrukHaiConditionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler = scn.GetShadowCard("traveler");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, saruman, guard);
		scn.MoveCardsToSupportArea(endless);
		assertInZone(Zone.SUPPORT, endless);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, traveler}, new PhysicalCardImpl[]{gimli, saruman});
		scn.ShadowAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// The Traveler and Saruman are two [uruk-hai] minions, but only one is another; the Guard is a [sauron]
		// Uruk-hai and Endless Assault no minion. The (2) could be paid.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(traveler));
	}

	@Test
	public void NotOfferedUntilThereAreTwoTwilightTokensToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler = scn.GetShadowCard("traveler");
		var traveler2 = scn.GetShadowCard("traveler2");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, traveler2, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, traveler}, new PhysicalCardImpl[]{gimli, saruman});
		scn.ShadowAssignToMinions(aragorn, traveler2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(traveler));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(traveler));
	}

	@Test
	public void UsedOutOfAnySkirmishItsBonusLastsThatSkirmishAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler = scn.GetShadowCard("traveler");
		var traveler2 = scn.GetShadowCard("traveler2");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, traveler2, saruman);

		// The Traveler is left unassigned; Gimli's skirmish is fought first, then Aragorn's.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, saruman}, new PhysicalCardImpl[]{aragorn, traveler2});
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(traveler));
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(traveler));
		assertEquals(1, scn.GetKeywordCount(traveler, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(traveler));
		scn.ShadowUseCardAction(traveler);
		assertEquals(0, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(traveler));
		assertEquals(2, scn.GetKeywordCount(traveler, Keyword.DAMAGE));

		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(traveler));
		assertEquals(2, scn.GetKeywordCount(traveler, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		// Saruman's 8 against Gimli's 6: one wound on Gimli; Aragorn's skirmish is next.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(traveler));
		assertEquals(1, scn.GetKeywordCount(traveler, Keyword.DAMAGE));
	}
}
