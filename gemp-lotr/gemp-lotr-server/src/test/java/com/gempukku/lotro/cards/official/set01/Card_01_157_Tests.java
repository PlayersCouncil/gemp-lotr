package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "1_157");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; Damage +1 is its whole text
					put("hounding", "11_188");  // Hounding Uruk: [uruk-hai] Uruk-hai; +3 strength only while a companion is exhausted
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, no Uruk-hai; a Shadow ability used only on purpose

					// Two archer companions, so that the fellowship archery total is 2 and -1 is told from a total of 0.
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: Archer; stronger only against a Nazgul
					put("garrison", "8_36");    // Garrison of Gondor: Knight, Archer; defender +1 only with a fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Armory
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>While you can spot an Uruk-hai, the fellowship archery total is -1.
		*/

		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");

		assertEquals("Uruk-hai Armory", armory.getBlueprint().getTitle());
		assertNull(armory.getBlueprint().getSubtitle());
		assertFalse(armory.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, armory.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, armory.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, armory.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(armory, Keyword.SUPPORT_AREA));
		assertEquals(1, armory.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipArcheryTotalIsOneLessOnceAnUrukHaiIsPlayedAndNotForAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("garrison"));
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(forger);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, armory);
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		scn.FreepsPass();

		// An [isengard] Orc is no Uruk-hai: the two archers' total stands.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The minions have no archer, and their total is not touched.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureCountsAndASecondDoesNotCountAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var hounding = scn.GetShadowCard("hounding");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("garrison"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		assertInZone(Zone.SUPPORT, armory);
		scn.FreepsPass();

		// No Uruk-hai at all.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// An [uruk-hai] Uruk-hai is an Uruk-hai.
		scn.MoveMinionsToTable(hounding);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A second Uruk-hai does not make it -2.
		scn.MoveMinionsToTable(savage);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
