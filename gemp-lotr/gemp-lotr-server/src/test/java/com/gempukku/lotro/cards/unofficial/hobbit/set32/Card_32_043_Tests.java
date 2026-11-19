package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_043_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The phases of
	// turn 1 after the fellowship phase are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	// Site 8 is Smaug's Den (a Shadow ability to play Smaug) and site 9 Battle of the Five Armies (a Shadow ability
	// paid with 2 doubts); neither is used.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "32_43");

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, vitality 1; its play trigger never met (cheated in)
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, no Orc; his texts reach only [gundabad] Orcs and an assignment not used

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a fellowship ability not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion; a skirmish ability not used
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal and a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrkishVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Orkish Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At site 9, each Orc is an <b>archer</b>.
		 * <b>Regroup:</b> Discard an Orc to make the Free Peoples player wound a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Orkish Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtSite9EachOrcIsAnArcherAndNotAtSite8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");

		scn.StartGame();
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(veteran, runner, sauron);
		assertFalse(scn.HasKeyword(veteran, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(runner, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sauron, Keyword.ARCHER));

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		// Orcs of two cultures, the Veteran among them; not a [sauron] minion who is no Orc.
		assertTrue(scn.HasKeyword(veteran, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(runner, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sauron, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsAnOrcToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, gloin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 3 with 2 wounds: Balin is exhausted, so a wound kills him where an exertion could not be made.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, runner, sauron);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(veteran));
		scn.ShadowUseCardAction(veteran);
		// Orcs of two cultures, the Veteran itself among them; not a [sauron] minion who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);

		// The Free Peoples player chooses the companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin, gloin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var runner = scn.GetShadowCard("runner");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, runner);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(balin));
	}
}
