package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_118_Tests
{

	// The King path with Pelennor Prairie as its site 4.  Nothing on the way there asks anything of the
	// fellowships laid here (Edoras Hall, King's Tent, The Dimholt).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pippin, Friend to Frodo: "Your opponent may not discard your [shire] tales from play."
					put("pippin", "1_306");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unwounded companion, who is no minion to exert

					// Free Peoples conditions.  Gondor Bowmen: a [gondor] tale in the support area; an archery ability only
					put("bowmen", "3_41");
					// Everyone Knows: a [shire] tale in the support area; it strengthens unbound Hobbits and has a skirmish ability
					put("knows", "9_50");
					// There and Back Again: a [shire] condition that a Hobbit companion bears; a maneuver ability only
					put("back", "1_317");
					// Red Book of Westmarch: a Free Peoples possession in the support area; its trigger needs Bilbo
					put("redbook", "2_113");
					// Weary: a Shadow condition, played on a companion; its trigger needs an [isengard] tracker
					put("weary", "4_212");

					put("savage", "1_151");     // Uruk Savage: vitality 3; "Damage +1." is its whole text
					put("patrol", "1_177");     // Goblin Patrol Troop: vitality 3, no text
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "10_118"); //here
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
	public void PelennorPrairieStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Pelennor Prairie
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Plains</b>. <b>Shadow:</b> Exert a minion and remove a burden to make the Free Peoples player discard one of his or her conditions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Pelennor Prairie", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void ShadowActionExertsAMinionAndRemovesABurdenToMakeTheFreePeoplesPlayerDiscardOneOfHisConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prairie = scn.GetShadowSite(4);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bowmen = scn.GetFreepsCard("bowmen");
		var back = scn.GetFreepsCard("back");
		var redbook = scn.GetFreepsCard("redbook");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(bowmen, redbook);
		scn.AttachCardsTo(frodo, back);

		var weary = scn.GetShadowCard("weary");
		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(savage, patrol);
		scn.FreepsPass();

		// The Shadow player laid the site, and it is his copy that lies there.
		assertSame(prairie, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(prairie));
		assertEquals(2, scn.GetBurdens());

		scn.ShadowUseCardAction(prairie);
		// A minion of his choosing exerts: not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, patrol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(1, scn.GetBurdens());

		// The Free Peoples player chooses, among the conditions that are his: one in his support area and one
		// that Frodo bears.  Not the Shadow player's condition on Aragorn, nor a possession of his own.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bowmen, back));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weary, redbook));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(back);
		assertInZone(Zone.DISCARD, back);
		assertInZone(Zone.SUPPORT, bowmen, redbook);
		assertAttachedTo(weary, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerIsTheOneWhoDiscardsSoPippinDoesNotKeepHisShireTale() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prairie = scn.GetShadowSite(4);

		var pippin = scn.GetFreepsCard("pippin");
		var bowmen = scn.GetFreepsCard("bowmen");
		var knows = scn.GetFreepsCard("knows");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(bowmen, knows);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertSame(prairie, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(prairie));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetBurdens());

		// The Savage is the only minion, and exerts without asking.
		scn.ShadowUseCardAction(prairie);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetBurdens());

		// Pippin stops an opponent discarding a [shire] tale.  Here its owner discards it: Everyone Knows is
		// offered beside the [gondor] tale, and goes when chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bowmen, knows));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCard(knows);
		assertInZone(Zone.DISCARD, knows);
		assertInZone(Zone.SUPPORT, bowmen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutAMinionAbleToExertOrWithoutABurdenAndIsPaidForThoughThereIsNoCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prairie = scn.GetShadowSite(4);

		var aragorn = scn.GetFreepsCard("aragorn");
		var redbook = scn.GetFreepsCard("redbook");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(redbook);

		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(savage);
		// Exhausted: 2 wounds of 3 vitality.
		scn.AddWoundsToChar(savage, 2);
		scn.FreepsPass();

		// A burden to remove, and the only minion exhausted beside companions who could exert.
		assertSame(prairie, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(prairie));

		// A second minion, able to exert.
		scn.MoveMinionsToTable(patrol);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(prairie));

		// No burden.
		scn.RemoveBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(prairie));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(prairie));

		// The Free Peoples player has no condition, only a possession: both costs are paid (the Patrol Troop is
		// the one minion able to exert) and nothing is discarded.
		scn.ShadowUseCardAction(prairie);
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, redbook);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
