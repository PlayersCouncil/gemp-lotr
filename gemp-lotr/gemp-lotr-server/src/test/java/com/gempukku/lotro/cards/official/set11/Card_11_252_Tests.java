package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_252_Tests
{

	// The Fellowship path, with Osgiliath Reclaimed in each deck in place of site 9 (which no test reaches); a test
	// lays it on the path as the site after the fellowship's.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("osgiliath", "11_252");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OsgiliathReclaimedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Osgiliath Reclaimed
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. When the fellowship moves from this site, the Free Peoples player may spot 3 burdens to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("osgiliath");

		assertEquals("Osgiliath Reclaimed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void MovingThereAsksNothingAndMovingOnWithThreeBurdensTheFreePeoplesPlayerMayRemoveOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The Shadow player's copy, as a site 2 is on the first player's first turn.
		var osgiliath = scn.GetShadowSite("osgiliath");

		scn.StartGame();
		scn.MoveCardToAdventurePath(osgiliath);
		scn.AddBurdens(3);

		// The move to it: nothing is asked.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(osgiliath, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move from it: the Free Peoples player is offered the removal, and the Shadow player, whose
		// site it is, is not.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(osgiliath));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheBurdensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetShadowSite("osgiliath");

		scn.StartGame();
		scn.MoveCardToAdventurePath(osgiliath);
		scn.AddBurdens(3);
		scn.FreepsPass();

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(osgiliath));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoBurdensOfHisOwnAndThreeOnTheOtherPlayersRingBearerNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetShadowSite("osgiliath");

		scn.StartGame();
		// The second player's burdens are laid on his own turn.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(3);
		scn.SkipToOtherPlayersTurn();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveCardToAdventurePath(osgiliath);
		scn.AddBurdens(2);
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.ShadowGetBurdens());

		scn.FreepsPass();
		assertSame(osgiliath, scn.GetCurrentSite());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
