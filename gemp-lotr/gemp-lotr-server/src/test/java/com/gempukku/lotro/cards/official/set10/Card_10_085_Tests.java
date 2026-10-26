package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flames", "10_85");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, site 6; text only on a weapon PLAYED on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, site 6; damage +1 and no other text
					put("keeper", "10_86");     // Gorgoroth Keeper: [sauron] Orc, strength 10, SITE 5; its texts need a site controlled
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					// Barren Land: a [sauron] condition, no minion.  Its move trigger is for sites 8 and 9 and its
					// ability is not used.
					put("barren", "10_79");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6; skirmish ability only
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition, cost 0; played to take the hand from four to three
					put("filler1", "1_177");    // Goblin Patrol Troop: no text; only held in the Free Peoples hand to make it up
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlamesWithinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Flames Within
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you have initiative, each of your [sauron] minions is strength +2.
		 * 	<b>Skirmish:</b> Discard this condition to make a [sauron] minion strength +2 (or +4 if it is roaming).
		 */

		var scn = GetScenario();

		var flames = scn.GetFreepsCard("flames");

		assertEquals("Flames Within", flames.getBlueprint().getTitle());
		assertNull(flames.getBlueprint().getSubtitle());
		assertTrue(flames.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, flames.getBlueprint().getSide());
		assertEquals(Culture.SAURON, flames.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, flames.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(flames, Keyword.SUPPORT_AREA));
		assertEquals(0, flames.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileTheShadowHasInitiativeEachSauronMinionIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");

		var flames = scn.GetShadowCard("flames");
		var barren = scn.GetShadowCard("barren");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(flames, barren);
		assertInZone(Zone.SUPPORT, flames);
		assertInZone(Zone.SUPPORT, barren);

		scn.StartGame();
		// Laid in the fellowship phase so that a Free Peoples play can hand the initiative over (Q-0228).
		scn.MoveMinionsToTable(chieftain, guard, hound);
		scn.SetFreepsHand(stairs, filler1, filler2, filler3);

		// Four cards: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(0, scn.GetStrength(barren));

		// Three cards: the Shadow player has it.
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		// An Orc and an Uruk-hai of the culture; not an Orc of another culture, nor a [sauron] condition.
		assertEquals(9, scn.GetStrength(chieftain));
		assertEquals(11, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(0, scn.GetStrength(barren));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Four cards again: the bonus is gone.
		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeASauronMinionThatIsNotRoamingStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var flames = scn.GetShadowCard("flames");
		var barren = scn.GetShadowCard("barren");
		var keeper = scn.GetShadowCard("keeper");
		var guard = scn.GetShadowCard("guard");
		var chieftain = scn.GetShadowCard("chieftain");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(flames, barren);
		assertInZone(Zone.SUPPORT, flames);
		assertInZone(Zone.SUPPORT, barren);

		scn.StartGame();
		scn.SkipToSite(4);
		// Four cards in the Free Peoples hand, so that the first sentence (the Shadow's initiative) is off.
		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		assertTrue(scn.FreepsHasInitiative());
		scn.MoveMinionsToTable(keeper, guard, chieftain, hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// At site 5 the Keeper (site 5) is not roaming; the Guard and the Chieftain (site 6) are.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(keeper, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));

		// The phase next door: the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flames));
		scn.ShadowPass();

		// The Keeper and the Hound are left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, guard});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(keeper));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowActionAvailable(flames));

		scn.ShadowUseCardAction(flames);
		// The text names no skirmish: the Orc in this skirmish, the Uruk-hai in the next and the Orc in none are all
		// offered; not the [wraith] Orc, nor the [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, guard, keeper));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, barren));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(keeper);

		assertInDiscard(flames);
		// Not roaming: +2, though a roaming minion stands beside it.
		assertEquals(12, scn.GetStrength(keeper));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) beats the Chieftain (7), who lives with one wound; in the next skirmish the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(10, scn.GetStrength(keeper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesARoamingSauronMinionStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var gimli = scn.GetFreepsCard("gimli");
		var flames = scn.GetShadowCard("flames");
		var keeper = scn.GetShadowCard("keeper");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(flames);
		assertInZone(Zone.SUPPORT, flames);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		assertTrue(scn.FreepsHasInitiative());
		scn.MoveMinionsToTable(keeper, guard);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(keeper, Keyword.ROAMING));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(10, scn.GetStrength(keeper));

		scn.ShadowUseCardAction(flames);
		scn.ShadowChooseCard(guard);

		assertInDiscard(flames);
		assertEquals(13, scn.GetStrength(guard));
		assertEquals(10, scn.GetStrength(keeper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoSauronMinionOnTheTableIsStillOfferedAndItsCostPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flames = scn.GetShadowCard("flames");
		var hound = scn.GetShadowCard("hound");
		var barren = scn.GetShadowCard("barren");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(flames, barren);
		assertInZone(Zone.SUPPORT, flames);
		assertInZone(Zone.SUPPORT, barren);

		scn.StartGame();
		// A minion of another culture, and a [sauron] card that is no minion: no [sauron] minion anywhere.
		scn.MoveMinionsToTable(hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(gimli, hound);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowActionAvailable(flames));

		scn.ShadowUseCardAction(flames);
		assertInDiscard(flames);
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
