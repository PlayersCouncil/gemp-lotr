package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_331_Tests
{

	// The Towers sites, whose site 2 is Eastfold.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chaser1", "4_181");    // Uruk Chaser: [isengard] Uruk-hai tracker, fierce; +2 in a fierce skirmish only
					put("chaser2", "4_181");
					put("seeker", "4_195");     // Uruk Seeker: [isengard] Uruk-hai tracker, fierce; +2 against a bearer of a search card only
					// Orc Ambusher: a [sauron] tracker; a roaming discount and a Response to a regroup event only
					put("ambusher", "1_261");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai who is no tracker; damage +1 only
					// Aragorn, Ranger of the North: a card for the Free Peoples player's hand, so that he is asked to
					// reconcile after the question of another move
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EastfoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Eastfold
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2T
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Plains</b>. <b>Shadow:</b> Spot 3 [isengard] trackers to prevent the fellowship from moving again this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Eastfold", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void WithThreeIsengardTrackersTheShadowPlayerStopsTheFellowshipMovingAgainThisTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaser1 = scn.GetShadowCard("chaser1");
		var chaser2 = scn.GetShadowCard("chaser2");
		var seeker = scn.GetShadowCard("seeker");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCardsToHand(aragorn);
		// Laid in the fellowship phase at site 1, as they will stand in the Shadow phase at site 2.
		scn.MoveMinionsToTable(chaser1, chaser2, seeker);
		scn.FreepsPass();

		var eastfold = scn.GetCurrentSite();
		assertEquals("Eastfold", eastfold.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(eastfold));

		scn.ShadowUseCardAction(eastfold);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		// One move made of the two the move limit allows, and the fellowship is not asked to make the second:
		// the regroup phase has gone on to the mover's reconcile, which follows that question.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));

		// The next turn, the other player's fellowship moves to site 2 and may move again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void WithTwoIsengardTrackersItIsNotOfferedAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaser1 = scn.GetShadowCard("chaser1");
		var seeker = scn.GetShadowCard("seeker");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(chaser1, seeker, ambusher, savage);
		scn.FreepsPass();

		var eastfold = scn.GetCurrentSite();
		assertEquals("Eastfold", eastfold.getBlueprint().getTitle());
		// Two [isengard] trackers, a tracker of another culture and an [isengard] Uruk-hai who is no tracker.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(eastfold));

		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
