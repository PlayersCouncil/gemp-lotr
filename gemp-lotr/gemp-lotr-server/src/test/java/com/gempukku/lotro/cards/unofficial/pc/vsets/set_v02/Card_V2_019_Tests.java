package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_019_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deathseeker", "102_19");

					// Uruk Savage: an [isengard] Uruk-hai like the Deathseeker, strength 5, vitality 3; Damage +1
					// is its whole text.
					put("savage1", "1_151");
					put("savage2", "1_151");
					put("savage3", "1_151");

					// Assault Ladder: an [isengard] machine, a condition.  "Skirmish: Spot 3 [isengard] tokens
					// here to make an Uruk-hai strength +3", which is how the tokens' culture is read.
					put("ladder", "6_58");
					// Devilry of Orthanc: an [isengard] machine, a condition; its abilities need an Uruk-hai to
					// exert in the Shadow phase or 8 tokens.
					put("devilry", "5_49");
					// Uruk Shield Wall: an [isengard] machine that is a possession.  With no tokens on it, it
					// prevents wounds to Uruk-hai that cost 0, and there are none.
					put("wall", "102_35");
					// No Business of Ours: an [isengard] condition that is no machine; it keeps the Free
					// Peoples player from looking at the Shadow hand.
					put("business", "2_44");
					// Seeking Its Master: a [sauron] condition, made a machine in the test that uses it.  Its
					// Shadow ability needs a [sauron] Orc.
					put("seeking", "1_276");

					// Gondor's Vengeance: "Regroup: Exert a ranger companion to discard a minion."
					put("vengeance", "1_106");

					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2"
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a ranger, with a maneuver ability
					put("guard1", "5_83");       // Household Guard: strength 6, vitality 3; "Valiant" is its whole text
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkDeathseekerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Berserk Deathseeker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Damage +1.
		 * This minion is strength +1 for each wound on each character in its skirmish.
		 * When this minion is killed, add 3 [isengard] tokens to an [isengard] machine.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deathseeker");

		assertEquals("Berserk Deathseeker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandForFivePlusTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deathseeker = scn.GetShadowCard("deathseeker");
		scn.MoveCardsToHand(deathseeker);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 2 for the site.
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.HAND, deathseeker);
		assertTrue(scn.ShadowPlayAvailable(deathseeker));

		scn.ShadowPlayCard(deathseeker);
		// 5 to play + 2 roaming: its site number is 5 and the fellowship is at site 2.
		assertEquals(13 - 7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, deathseeker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GrowsStrongerAsTheCompanionItIsFightingExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var deathseeker = scn.GetShadowCard("deathseeker");

		scn.StartGame();
		scn.MoveMinionsToTable(deathseeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, deathseeker);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(deathseeker));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(deathseeker));
		assertEquals(10, scn.GetStrength(deathseeker));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(11, scn.GetStrength(deathseeker));

		// A second wound on the same companion counts again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(12, scn.GetStrength(deathseeker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CountsEveryWoundOnEveryCharacterInItsOwnSkirmishItselfIncludedAndOnlyWhileThatSkirmishIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(aragorn, guard, guard2);

		var deathseeker = scn.GetShadowCard("deathseeker");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");

		scn.StartGame();
		scn.MoveMinionsToTable(deathseeker, savage1, savage2, savage3);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(guard2, 1);
		scn.AddWoundsToChar(deathseeker, 1);
		scn.AddWoundsToChar(savage1, 1);
		scn.AddWoundsToChar(savage2, 1);
		scn.AddWoundsToChar(savage3, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		// Wounded, and in no skirmish.
		assertEquals(10, scn.GetStrength(deathseeker));

		// Aragorn fights the Deathseeker and an Uruk Savage; one Household Guard fights a second Savage before
		// them, and the other Guard a third Savage after them.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, deathseeker },
				new PhysicalCardImpl[] { guard, savage2 },
				new PhysicalCardImpl[] { guard2, savage3 }
		);
		scn.ShadowAssignToMinions(aragorn, savage1);

		// The Guard's skirmish comes first.  The Deathseeker is assigned and its own skirmish has not begun:
		// the wounds on it, on Aragorn and on the two characters now fighting count for nothing.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharAssigned(deathseeker));
		assertFalse(scn.IsCharSkirmishing(deathseeker));
		assertEquals(10, scn.GetStrength(deathseeker));

		// The Guard's 6 beats the Savage's 5, which takes a second wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(deathseeker));
		assertTrue(scn.IsCharSkirmishing(savage1));
		assertFalse(scn.IsCharSkirmishing(savage2));
		// The last skirmish is still to come: its Guard and its Savage are assigned, wounded, and not fighting.
		assertTrue(scn.IsCharAssigned(guard2));
		assertTrue(scn.IsCharAssigned(savage3));
		assertFalse(scn.IsCharSkirmishing(savage3));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(savage3));
		// 10 + 2 for Aragorn's wounds + 1 for its own + 1 for the Savage's beside it.  Not the first Guard's
		// wound nor the 2 on the Savage whose skirmish is over, and not the wound on each of the two whose
		// skirmish has not begun.
		assertEquals(14, scn.GetStrength(deathseeker));
		// The Savage in the same skirmish gets nothing from it.
		assertEquals(5, scn.GetStrength(savage1));
	}

	@Test
	public void WhenKilledAddsThreeIsengardTokensToAnIsengardMachineOfItsOwnersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		var deathseeker = scn.GetShadowCard("deathseeker");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var ladder = scn.GetShadowCard("ladder");
		var devilry = scn.GetShadowCard("devilry");
		var wall = scn.GetShadowCard("wall");
		var business = scn.GetShadowCard("business");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCardsToSupportArea(ladder, devilry, wall, business, seeking);
		// No machine is printed outside [isengard], so a [sauron] condition is made one.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, seeking, null, Keyword.MACHINE));
		scn.AddTokensToCard(ladder, 1);
		scn.AddTokensToCard(devilry, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(deathseeker, savage1, savage2);
		// One wound from death, each of them.
		scn.AddWoundsToChar(deathseeker, 2);
		scn.AddWoundsToChar(savage1, 2);
		// Wounded twice and fighting an unwounded Aragorn the Deathseeker is strength 12; Aragorn's 8 + 10
		// beats that without doubling it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		assertTrue(scn.HasKeyword(seeking, Keyword.MACHINE));
		assertFalse(scn.HasKeyword(business, Keyword.MACHINE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard1, savage1 },
				new PhysicalCardImpl[] { aragorn, deathseeker },
				new PhysicalCardImpl[] { guard2, savage2 }
		);

		// Another [isengard] Uruk-hai is killed first, by the wound of a skirmish it loses: no tokens.
		scn.FreepsResolveSkirmish(guard1);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// One token on the Ladder is not the three its ability spots.
		assertFalse(scn.ShadowActionAvailable(ladder));
		scn.ShadowPass();
		assertInZone(Zone.DISCARD, savage1);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(devilry));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(deathseeker));
		assertEquals(18, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// The Deathseeker takes its third wound and is killed.  Its owner chooses among the [isengard]
		// machines, a condition or a possession, and must choose one.
		assertInZone(Zone.DISCARD, deathseeker);
		assertTrue(scn.ShadowHasCardChoiceAvailable(ladder));
		assertTrue(scn.ShadowHasCardChoiceAvailable(devilry));
		assertTrue(scn.ShadowHasCardChoiceAvailable(wall));
		assertFalse(scn.ShadowHasCardChoiceAvailable(business));
		assertFalse(scn.ShadowHasCardChoiceAvailable(seeking));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(ladder);
		assertEquals(1 + 3, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(devilry));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetCultureTokensOn(business));
		assertEquals(0, scn.GetCultureTokensOn(seeking));

		// The tokens are [isengard] ones: in the next skirmish the Ladder can spot three of them.
		scn.FreepsResolveSkirmish(guard2);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ladder));
	}

	@Test
	public void BeingDiscardedIsNotBeingKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance);

		var deathseeker = scn.GetShadowCard("deathseeker");
		var ladder = scn.GetShadowCard("ladder");
		var devilry = scn.GetShadowCard("devilry");
		scn.MoveCardsToSupportArea(ladder, devilry);

		scn.StartGame();
		scn.MoveMinionsToTable(deathseeker);

		// Nobody fights.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, deathseeker);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(vengeance));

		// Gondor's Vengeance discards the Deathseeker, the only minion; Aragorn, the only ranger, exerts for it.
		scn.FreepsPlayCard(vengeance);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, deathseeker);
		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetCultureTokensOn(devilry));
		// Nothing is asked about machines: the regroup phase goes on.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
