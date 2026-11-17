package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MinionSiteNumberModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isee", "101_46");
					put("isee2", "101_46");

					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, cost 2, strength 7, site 6; a skirmish ability that is never used
					put("soldier2", "1_271");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, site 6; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, site 4, no text
					put("marksman", "1_176");   // Goblin Marksman: a [moria] Orc, cost 3, site 4; archer is its whole text, and it never leaves the hand
					put("fear", "1_278");       // Strength Born of Fear: another [sauron] support-area condition; a skirmish ability that is never used

					put("light", "4_309");      // Light Shining Faintly: "Fellowship: Add a burden to heal a Ring-bound companion twice."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring: "Maneuver: Wear The One Ring until the regroup phase."
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void ISeeYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: I See You
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: The site number of each [Sauron] minion is -1 for each burden you can spot.
		 * Each [sauron] minion with site number of 1 or less is strength +1.
		 * When the Ring-bearer takes off The One Ring, discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("isee");

		assertEquals("I See You", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForNothingToTheSupportAreaAndSauronMinionsInPlayAndInHandLoseASiteNumberForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isee = scn.GetShadowCard("isee");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(isee, soldier, marksman);

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(soldier2, guard, troop);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertEquals(6, scn.GetMinionSiteNumber(soldier2));
		assertEquals(6, scn.GetMinionSiteNumber(guard));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.ShadowPlayAvailable(isee));
		// Both minions in hand are roaming at site 2 and cost 2 more than the pool holds: 2 + 2 and 3 + 2.
		assertFalse(scn.ShadowPlayAvailable(soldier));
		assertFalse(scn.ShadowPlayAvailable(marksman));

		scn.ShadowPlayCard(isee);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, isee);
		// 6 - 4 burdens for the Orc and the Uruk-hai of [sauron]; the [moria] Orc keeps its 4.
		assertEquals(2, scn.GetMinionSiteNumber(soldier2));
		assertEquals(2, scn.GetMinionSiteNumber(guard));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		// The [sauron] Orc in hand now has site number 2 at site 2 and pays no roaming penalty; the [moria] Orc
		// still does.
		assertTrue(scn.ShadowPlayAvailable(soldier));
		assertFalse(scn.ShadowPlayAvailable(marksman));

		scn.ShadowPlayCard(soldier);
		// 2 to play and nothing for roaming
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		// A burden fewer is a site number more.
		scn.RemoveBurdens(1);
		assertEquals(3, scn.GetMinionSiteNumber(soldier));
		assertEquals(3, scn.GetMinionSiteNumber(soldier2));
		assertEquals(3, scn.GetMinionSiteNumber(guard));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SauronMinionsWithSiteNumberOneOrLessAreStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isee = scn.GetShadowCard("isee");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var light = scn.GetFreepsCard("light");
		scn.MoveCardsToSupportArea(isee);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, troop);
		scn.FreepsRefreshDecision();
		// A [moria] Orc whose site number is 1 whatever the burdens: 4 printed - 3.
		scn.ApplyAdHocModifier(new MinionSiteNumberModifier(null, Culture.MORIA, null, -3));

		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetMinionSiteNumber(soldier));
		assertEquals(6, scn.GetMinionSiteNumber(guard));
		assertEquals(1, scn.GetMinionSiteNumber(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(13, scn.GetStrength(troop));

		scn.AddBurdens(1);
		assertEquals(5, scn.GetMinionSiteNumber(soldier));
		assertEquals(5, scn.GetMinionSiteNumber(guard));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));

		scn.AddBurdens(1);
		assertEquals(4, scn.GetMinionSiteNumber(soldier));
		assertEquals(4, scn.GetMinionSiteNumber(guard));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));

		scn.AddBurdens(1);
		assertEquals(3, scn.GetMinionSiteNumber(soldier));
		assertEquals(3, scn.GetMinionSiteNumber(guard));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));

		// 4 burdens: site number 2, one short.
		scn.AddBurdens(1);
		assertEquals(2, scn.GetMinionSiteNumber(soldier));
		assertEquals(2, scn.GetMinionSiteNumber(guard));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));

		// The fifth burden is added by play, and takes both [sauron] minions to site number 1.
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetMinionSiteNumber(soldier));
		assertEquals(1, scn.GetMinionSiteNumber(guard));
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(guard));
		// The [moria] Orc has had site number 1 all along and gains nothing.
		assertEquals(1, scn.GetMinionSiteNumber(troop));
		assertEquals(13, scn.GetStrength(troop));

		// "Or less": +1 at site number 0, and still only +1.
		scn.AddBurdens(1);
		assertEquals(0, scn.GetMinionSiteNumber(soldier));
		assertEquals(0, scn.GetMinionSiteNumber(guard));
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(guard));

		// A seventh burden would take the site number below 0.
		scn.AddBurdens(1);
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(guard));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyIsDiscardedWhenTheRingBearerTakesOffTheOneRingAndNotWhenHePutsItOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isee = scn.GetShadowCard("isee");
		var isee2 = scn.GetShadowCard("isee2");
		var fear = scn.GetShadowCard("fear");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(isee, isee2, fear);

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.RBWearingOneRing());
		assertInZone(Zone.SUPPORT, isee, isee2, fear);

		scn.FreepsUseCardAction(scn.GetRing());
		assertTrue(scn.RBWearingOneRing());
		assertInZone(Zone.SUPPORT, isee, isee2, fear);

		// The Ring is worn until the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.RBWearingOneRing());
		assertInZone(Zone.DISCARD, isee, isee2);
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StaysInPlayWhenTheRegroupPhaseBeginsWithTheRingNeverPutOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isee = scn.GetShadowCard("isee");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(isee);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.FreepsActionAvailable(scn.GetRing()));
		assertInZone(Zone.SUPPORT, isee);

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.RBWearingOneRing());
		assertInZone(Zone.SUPPORT, isee);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
