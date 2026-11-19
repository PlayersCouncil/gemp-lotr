package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "15_71");
					put("ranger2", "15_71");     // a second copy: the Ranger is not unique
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose

					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; Damage +1 is its whole text
					// Goblin Man: [isengard] Orc of site 2, so not roaming at site 2; discarded only underground, and a
					// Shadow ability used only on purpose.
					put("goblinman", "2_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnyieldingRangerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Unyielding Ranger
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		 * 		This companion is strength +1 for each roaming minion you can spot.
		*/

		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");

		assertEquals("Unyielding Ranger", ranger.getBlueprint().getTitle());
		assertNull(ranger.getBlueprint().getSubtitle());
		assertFalse(ranger.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ranger.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, ranger.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, ranger.getBlueprint().getCardType());
		assertEquals(Race.MAN, ranger.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(ranger, Keyword.RANGER));
		assertTrue(scn.HasKeyword(ranger, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(ranger, Keyword.HUNTER));
		assertEquals(3, ranger.getBlueprint().getTwilightCost());
		assertEquals(6, ranger.getBlueprint().getStrength());
		assertEquals(3, ranger.getBlueprint().getVitality());
		assertEquals(6, ranger.getBlueprint().getResistance());
	}

	@Test
	public void ARoamingMinionPlayedMakesHimStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(ranger);

		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(ranger));
		assertInHand(troop);

		// To Ettenmoors, site 2: the Troop, of site 4, roams there.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 6 to play + 2 roaming.
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetStrength(ranger));

		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(7, scn.GetStrength(ranger));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyCountsEveryRoamingMinionOnceAndNoMinionThatIsNotRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(ranger, ranger2, aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetStrength(ranger));
		assertEquals(6, scn.GetStrength(ranger2));
		assertEquals(8, scn.GetStrength(aragorn));

		// A minion of site 2 at site 2 is no roaming minion.
		scn.MoveMinionsToTable(goblinman);
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertEquals(6, scn.GetStrength(ranger));
		assertEquals(6, scn.GetStrength(ranger2));

		// A [moria] Orc of site 4: one roaming minion.
		scn.MoveMinionsToTable(troop);
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(7, scn.GetStrength(ranger));
		assertEquals(7, scn.GetStrength(ranger2));

		// An [isengard] Uruk-hai of site 5: two roaming minions of three minions in play.  Each copy counts them
		// for itself alone (+2, not +4), and another companion is not made stronger.
		scn.MoveMinionsToTable(savage);
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(8, scn.GetStrength(ranger));
		assertEquals(8, scn.GetStrength(ranger2));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
