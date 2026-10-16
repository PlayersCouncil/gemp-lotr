package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_188_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Frodo, Son of Drogo is the Ring-bearer (resistance 10) and Sam the other Ring-bound companion;
					// the rest are unbound companions of resistance 6.
					put("sam", "1_311");            // Sam, Son of Hamfast: resistance 5; a fellowship ability used only on purpose
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("legolas", "1_50");         // Legolas, Greenleaf: an archery ability used only on purpose
					put("boromir", "1_97");         // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("gimli", "1_13");           // Gimli, Son of Gloin: cost 2; a skirmish ability used only on purpose
					put("maggot", "1_288");         // Farmer Maggot, Chaser of Rascals: an ally, who is no companion

					put("troop1", "1_177");         // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("arrows", "1_141");         // Their Arrows Enrage: a Shadow condition, no minion; archers -1 strength only
				}},
				new HashMap<>()
				{{
					put("site1", "12_188");         // Hill of Sight: the game starts there
					put("site2", "13_185");         // Abandoned Mine Shaft: its text needs a regroup move from it outside region 1
					// Never laid in these tests.
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void HillofSightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Hill of Sight
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. <b>Forest</b>. While you can spot 3 minions (or 6 companions), each unbound
		 * 	companion is resistance -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Hill of Sight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void WithAThirdMinionEachUnboundCompanionIsResistanceMinusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var maggot = scn.GetFreepsCard("maggot");
		// Four companions and an ally: far short of six.
		scn.MoveCompanionsToTable(sam, aragorn, legolas);
		scn.MoveAlliesToTable(maggot);

		var arrows = scn.GetShadowCard("arrows");
		scn.MoveCardsToSupportArea(arrows);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Hill of Sight", scn.GetCurrentSite().getBlueprint().getTitle());

		// Two minions, and a Shadow condition that is no minion.
		scn.MoveMinionsToTable("troop1", "troop2");
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(legolas));

		scn.MoveMinionsToTable("troop3");
		// Aragorn ([gondor] Man) and Legolas ([elven] Elf) are unbound; Frodo and Sam are Ring-bound.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithASixthCompanionPlayedEachUnboundCompanionIsResistanceMinusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var maggot = scn.GetFreepsCard("maggot");
		// Five companions and an ally, who nearly counts.
		scn.MoveCompanionsToTable(sam, aragorn, legolas, boromir);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(gimli);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Hill of Sight", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(legolas));
		assertEquals(6, scn.GetResistance(boromir));

		scn.FreepsPlayCard(gimli);

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(legolas));
		assertEquals(4, scn.GetResistance(boromir));
		assertEquals(4, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheHillUnboundCompanionsHaveTheirFullResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, legolas, boromir, gimli);

		scn.StartGame(scn.GetFreepsSite("site1"));
		var hill = scn.GetCurrentSite();
		assertEquals("Hill of Sight", hill.getBlueprint().getTitle());
		assertEquals(4, scn.GetResistance(aragorn));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		assertEquals("Abandoned Mine Shaft", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.ADVENTURE_PATH, hill);

		// Six companions and three minions, with the Hill behind the fellowship.
		scn.MoveMinionsToTable("troop1", "troop2", "troop3");
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(legolas));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
