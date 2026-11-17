package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_337_Tests
{

	// The King path, West Road its site 2.  Site 1 is Edoras Hall (Shadow number 0; [rohan] possessions cost 1
	// less) and site 3 The Dimholt (Shadow number 1; a fellowship ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("valor", "1_103");      // Elendil's Valor: [gondor] Maneuver event, cost 0, a [gondor] companion defender +1
					put("troop", "1_177");      // Goblin Patrol Troop: no text; a minion, and a card in the Free Peoples hand
					put("runner", "1_178");     // Goblin Runner: here only a card in the Free Peoples hand
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_337");
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
	public void WestRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: West Road
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 2
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Plains</b>. If the Shadow has initiative, the Shadow number of this site is +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("West Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void WhileTheShadowHasInitiativeTheMoveToWestRoadAddsThreeMoreAndNoOtherSiteChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowSite(2);

		scn.StartGame();
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("valor"));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(road, scn.GetCurrentSite());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		// 10 set + 1 companion + 3 and 3 for West Road
		assertEquals(17, scn.GetTwilight());
		assertEquals(6, scn.GetTwilightCost(road));
		// Edoras Hall, site 1, keeps its own number.
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileTheFreePeoplesPlayerHasInitiativeTheNumberIsThreeUntilAPlayTakesHisHandToThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowSite(2);
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable("aragorn");

		scn.StartGame();
		// Four cards in the Free Peoples hand: he has initiative.
		scn.SetFreepsHand(valor, scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("runner2"));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(road, scn.GetCurrentSite());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		// 10 set + 2 companions + 3 for West Road
		assertEquals(15, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(road));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetTwilightCost(road));

		// The event takes the hand from four cards to three, and the initiative passes to the Shadow.
		scn.FreepsPlayCard(valor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(6, scn.GetTwilightCost(road));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftWestRoadItsNumberIsThreeThoughTheShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(2);
		var road = scn.GetSite(2);
		assertSame(road, scn.GetCurrentSite());

		scn.SetFreepsHand(scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("valor"));
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(6, scn.GetTwilightCost(road));

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		// 10 set + 1 companion + 1 for The Dimholt
		assertEquals(12, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(road));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
