package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_336_Tests
{

	// The King path, Rohirrim Camp its site 2.  Site 1 is Edoras Hall ([rohan] possessions cost 1 less) and site 3
	// The Dimholt (Shadow number 1; a fellowship ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site number 5; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, cost 2, site number 6; its text answers a weapon played on it
					put("chieftain", "1_266");
					put("troop", "1_177");      // Goblin Patrol Troop: no text; here only a card in the Free Peoples hand

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("valor", "1_103");      // Elendil's Valor: [gondor] Maneuver event, cost 0, a [gondor] companion defender +1
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_336");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimCampStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rohirrim Camp
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Plains</b>. If the Shadow has initiative, minions are not roaming.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Rohirrim Camp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void WhileTheShadowHasInitiativeMinionsOfEitherCultureAndRacePlayedAtTheCampAreNotRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowSite(2);
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("savage"), scn.GetFreepsCard("chieftain"), scn.GetFreepsCard("troop"));
		scn.SetShadowHand(savage, chieftain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(camp, scn.GetCurrentSite());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		// 10 set + 1 companion + 2 for the Camp
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Uruk-hai of site number 5: 2 and no roaming penalty.
		scn.ShadowPlayCard(savage);
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.HasKeyword(savage, Keyword.ROAMING));

		// A [sauron] Orc of site number 6: 2 and no roaming penalty.
		scn.ShadowPlayCard(chieftain);
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileTheFreePeoplesPlayerHasInitiativeAMinionIsRoamingUntilAPlayTakesHisHandToThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowSite(2);
		var savage = scn.GetShadowCard("savage");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable("aragorn");

		scn.StartGame();
		// Four cards in the Free Peoples hand: he has initiative.
		scn.SetFreepsHand(valor, scn.GetFreepsCard("savage"), scn.GetFreepsCard("chieftain"), scn.GetFreepsCard("troop"));
		scn.SetShadowHand(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(camp, scn.GetCurrentSite());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		// 10 set + 2 companions + 2 for the Camp
		assertEquals(14, scn.GetTwilight());

		// 2 + 2 roaming.
		scn.ShadowPlayCard(savage);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		// The event takes the hand from four cards to three, and the initiative passes to the Shadow.
		scn.FreepsPlayCard(valor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheCampMinionsAreRoamingAgainThoughTheShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowSite(2);
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		assertSame(camp, scn.GetCurrentSite());

		scn.SetFreepsHand(scn.GetFreepsCard("savage"), scn.GetFreepsCard("chieftain"), scn.GetFreepsCard("troop"));
		// Laid in the fellowship phase, to stand in play through the move that leaves the Camp.
		scn.MoveMinionsToTable(savage);
		scn.SetShadowHand(chieftain);
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		// 10 set + 1 companion + 1 for The Dimholt
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		// 2 + 2 roaming.
		scn.ShadowPlayCard(chieftain);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
