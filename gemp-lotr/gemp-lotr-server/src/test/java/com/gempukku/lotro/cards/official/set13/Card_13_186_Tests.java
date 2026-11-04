package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_186_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Nothing but the Ring-bearer: the burdens are the only thing the site reads.
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("ruins", "18_138");         // Sirannon Ruins: River, and no other text
					put("caves", "13_186");
					// Never laid in these tests.
					put("site4", "11_234");
					put("site5", "12_187");
					put("site6", "13_185");
					put("site7", "12_185");
					put("site8", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void CavesofAglarondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Caves of Aglarond
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Underground</b>. When the fellowship moves to this site during the regroup phase, the Free Peoples player may remove 2 burdens.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("caves");

		assertEquals("Caves of Aglarond", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void MovingToTheCavesInTheRegroupPhaseOffersTheFreePeoplesPlayerToRemoveTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("caves"));

		// The Shadow player laid the Caves, his own copy: the offer is the Free Peoples player's, not the site's owner's.
		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(caves));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(caves));

		scn.FreepsAcceptOptionalTrigger();
		// 3 - 2
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("caves"));

		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(caves));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheCavesInTheFellowshipPhaseOrFromThemInTheRegroupPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("caves"));

		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(caves));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move away from the Caves: the fellowship moves, in the regroup phase, but not to this site.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));

		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(caves));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("ruins"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("caves"));

		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(caves));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
