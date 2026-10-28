package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "7_115");
					put("ranger2", "7_115");

					// Uruk Savage: [isengard] Uruk-hai, site 5 (roaming at site 3), strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Goblin Runner: [moria] Orc, site 4 (roaming at site 3), strength 5, vitality 1; its trigger is on being played
					put("runner", "1_178");
					// Gollum, Stinker: site 3 (not roaming at site 3), strength 5 (+1 for each burden: none here); his
					// trigger needs him to win
					put("gollum", "5_25");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangerofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Ranger of Minas Tirith
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.
		 * While skirmishing a roaming minion, this companion is strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger1");

		assertEquals("Ranger of Minas Tirith", ranger.getBlueprint().getTitle());
		assertNull(ranger.getBlueprint().getSubtitle());
		assertFalse(ranger.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ranger.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, ranger.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, ranger.getBlueprint().getCardType());
		assertEquals(Race.MAN, ranger.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(ranger, Keyword.RANGER));
		assertEquals(2, ranger.getBlueprint().getTwilightCost());
		assertEquals(5, ranger.getBlueprint().getStrength());
		assertEquals(3, ranger.getBlueprint().getVitality());
		assertEquals(6, ranger.getBlueprint().getResistance());
	}

	@Test
	public void EachCopyIsStrengthPlus2AndDamagePlus1OnlyWhileItsOwnSkirmishAgainstARoamingMinionIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(ranger1, ranger2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger1, savage}, new PhysicalCardImpl[]{ranger2, runner});

		// At site 3: an Uruk-hai of site 5 and an Orc of site 4, both roaming.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertEquals(5, scn.GetStrength(ranger1));
		assertEquals(0, scn.GetKeywordCount(ranger1, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(ranger1);
		assertEquals(7, scn.GetStrength(ranger1));
		assertEquals(1, scn.GetKeywordCount(ranger1, Keyword.DAMAGE));
		// The other copy is assigned to a roaming minion, but its skirmish has not begun.
		assertEquals(5, scn.GetStrength(ranger2));
		assertEquals(0, scn.GetKeywordCount(ranger2, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(ranger2);
		assertEquals(7, scn.GetStrength(ranger2));
		assertEquals(1, scn.GetKeywordCount(ranger2, Keyword.DAMAGE));
		// The first copy's skirmish is over.
		assertEquals(5, scn.GetStrength(ranger1));
		assertEquals(0, scn.GetKeywordCount(ranger1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAMinionThatIsNotRoamingItIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger1, ranger2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger1, gollum}, new PhysicalCardImpl[]{ranger2, savage});

		// Gollum's site is 3: not roaming at site 3.  A roaming minion is in play, in another skirmish.
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.FreepsResolveSkirmish(ranger1);
		assertEquals(5, scn.GetStrength(ranger1));
		assertEquals(0, scn.GetKeywordCount(ranger1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
