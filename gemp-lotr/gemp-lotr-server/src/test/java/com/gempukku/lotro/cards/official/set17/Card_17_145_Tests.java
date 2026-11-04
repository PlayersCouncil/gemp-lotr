package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unwounded companion, who is no minion to exert; a maneuver ability used only on purpose

					// Free Peoples conditions.  Gondor Bowmen: a [gondor] tale in the support area; an archery ability only
					put("bowmen", "3_41");
					// There and Back Again: a [shire] condition that a Hobbit companion bears; a maneuver ability only
					put("back", "1_317");
					// Red Book of Westmarch: a Free Peoples possession in the support area; its trigger needs Bilbo
					put("redbook", "2_113");
					// Weary: a Shadow condition, played on a companion; its trigger needs an [isengard] tracker
					put("weary", "4_212");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; "Damage +1." is its whole text
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				new HashMap<>()
				{{
					// The Fellowship-block path, with Dol Guldur in place of site 9, which no test reaches: it is put
					// on the path as site 4 (MoveCardToAdventurePath) and takes that number.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("dolguldur", "17_145");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DolGuldurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Dol Guldur
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Regroup:</b> Exert your minion and remove a threat to make the Free Peoples player discard one of his or her conditions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("dolguldur");

		assertEquals("Dol Guldur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void RegroupActionExertsAMinionAndRemovesAThreatToMakeTheFreePeoplesPlayerDiscardOneOfHisConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

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
		assertInZone(Zone.SUPPORT, bowmen, redbook);
		assertAttachedTo(back, frodo);
		assertAttachedTo(weary, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		// The Shadow player's copy, laid as site 4.
		var dolguldur = scn.GetShadowSite("dolguldur");
		scn.MoveCardToAdventurePath(dolguldur);
		scn.FreepsPass();
		assertSame(dolguldur, scn.GetCurrentSite());

		// No minion in play: no skirmishes, and the minions come in at the start of the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, patrol);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertInZone(Zone.SUPPORT, bowmen, redbook);
		assertAttachedTo(back, frodo);
		assertAttachedTo(weary, aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dolguldur));

		scn.ShadowUseCardAction(dolguldur);
		// One of his minions exerts: not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, patrol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(1, scn.GetThreats());

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
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutAMinionAbleToExertOrWithoutAThreatAndIsPaidForThoughThereIsNoCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var redbook = scn.GetFreepsCard("redbook");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(redbook);
		assertInZone(Zone.SUPPORT, redbook);

		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(3);
		var dolguldur = scn.GetShadowSite("dolguldur");
		scn.MoveCardToAdventurePath(dolguldur);
		scn.FreepsPass();
		assertSame(dolguldur, scn.GetCurrentSite());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage);
		// Exhausted: 2 wounds of 3 vitality.
		scn.AddWoundsToChar(savage, 2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, redbook);
		scn.FreepsPass();

		// A threat to remove, and the only minion exhausted beside companions who could exert.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(dolguldur));

		// A second minion, able to exert.
		scn.MoveMinionsToTable(patrol);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(dolguldur));

		// No threat.
		scn.RemoveThreats(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(dolguldur));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(dolguldur));

		// The Free Peoples player has no condition, only a possession: both costs are paid (the Patrol Troop is
		// the one minion able to exert) and nothing is discarded.
		scn.ShadowUseCardAction(dolguldur);
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, redbook);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
