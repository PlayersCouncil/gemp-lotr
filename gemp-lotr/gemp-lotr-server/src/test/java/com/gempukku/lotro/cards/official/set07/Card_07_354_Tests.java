package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_354_Tests
{

	// The King path, Pelennor Grassland its site 7.  Site 6 is Minas Tirith Seventh Circle, whose text heals [elven]
	// companions on a regroup move only.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf; an archery ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, a Free Peoples character who is no companion

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, vitality 2; its text answers a weapon played on it
					put("chieftain", "1_266");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "7_354");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PelennorGrasslandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pelennor Grassland
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>Plains</b>. <b>Shadow:</b> Exert a minion and spot 6 companions to add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Pelennor Grassland", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void ShadowActionExertsAMinionOfHisChoiceWithSixCompanionsToAddOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grassland = scn.GetShadowSite(7);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCompanionsToTable("boromir", "legolas", "gimli", "sam");

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		// Laid in the fellowship phase, to be on the table in the Shadow phase at the Grassland.
		scn.MoveMinionsToTable(savage, chieftain, troop);
		scn.FreepsPass();

		assertSame(grassland, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowActionAvailable(grassland));

		scn.ShadowUseCardAction(grassland);
		// Three minions of three cultures and two races can exert; no companion is offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, chieftain, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(chieftain);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithFiveCompanionsAndAnAllyUntilASixthCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grassland = scn.GetShadowSite(7);
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "sam");
		scn.MoveAlliesToTable("bounder");

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(savage, chieftain);
		scn.FreepsPass();

		assertSame(grassland, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Frodo and four companions, and an ally: five companions.
		assertFalse(scn.ShadowActionAvailable(grassland));

		scn.MoveCompanionsToTable(legolas);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grassland));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grassland = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable("aragorn", "boromir", "legolas", "gimli", "sam");

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(chieftain);
		// Exhausted: 1 wound of 2 vitality.
		scn.AddWoundsToChar(chieftain, 1);
		scn.FreepsPass();

		assertSame(grassland, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertFalse(scn.ShadowActionAvailable(grassland));

		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grassland));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
