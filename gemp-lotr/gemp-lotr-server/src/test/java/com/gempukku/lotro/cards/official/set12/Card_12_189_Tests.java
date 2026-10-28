package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_189_Tests
{

	// A Shadows-block path: the game starts at Sirannon Ruins, the Shadow player lays Hobbiton Market as the site
	// after it, and Nurn after that, reached by the regroup move.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul minion, vitality 3; strength +1 for each companion
					put("prowler", "11_136");   // Prowling Orc: [orc] Orc minion, a lurker; a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");     // Sirannon Ruins: River, no other text
					put("site2", "12_189");
					put("site3", "17_148");     // Nurn: Plains; its text is a controlled site's
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "11_230");
					put("site6", "12_185");
					put("site7", "13_185");
					put("site8", "12_188");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The same path with Nurn second and Hobbiton Market third, reached by the regroup move.
	protected VirtualTableScenario GetMoveToScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul minion, vitality 3; strength +1 for each companion
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");     // Sirannon Ruins: River, no other text
					put("site2", "17_148");     // Nurn: Plains; its text is a controlled site's
					put("site3", "12_189");
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "11_230");
					put("site6", "12_185");
					put("site7", "13_185");
					put("site8", "12_188");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void HobbitonMarketStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Hobbiton Market
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Dwelling</b>. When the fellowship moves from this site, wound each minion that is not a lurker.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site2");

		assertEquals("Hobbiton Market", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
	}

	@Test
	public void MovingFromItWoundsEachMinionThatIsNotALurkerAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troop = scn.GetShadowCard("troop");
		var rider = scn.GetShadowCard("rider");
		var prowler = scn.GetShadowCard("prowler");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		assertEquals("Hobbiton Market", scn.GetCurrentSite().getBlueprint().getTitle());

		// The minions that lived through the skirmishes, laid as the regroup phase starts (Q-0228).
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop, rider, prowler);
		// The Rider is exhausted: a wound kills him.
		scn.AddWoundsToChar(rider, 2);
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(prowler));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsRefreshDecision();

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		assertEquals("Nurn", scn.GetCurrentSite().getBlueprint().getTitle());

		// The [moria] Orc and the [wraith] Nazgul are wounded, the Rider killed; the lurker and the companion are not.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, rider);
		assertEquals(0, scn.GetWoundsOn(prowler));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToItFromAnotherSiteWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMoveToScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troop = scn.GetShadowCard("troop");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		assertEquals("Nurn", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop, rider);
		scn.AddWoundsToChar(rider, 2);
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsRefreshDecision();

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		assertEquals("Hobbiton Market", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
