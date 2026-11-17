package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flankers1", "2_61");
					put("flankers2", "2_61");
					// Goblin Marksman: [moria] Orc archer, twilight 3, site 4; no other text
					put("marksman", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinFlankersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Flankers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, you may add (3) if the fellowship has moved more than once this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flankers1");

		assertEquals("Goblin Flankers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAfterTheFellowshipsSecondMoveItMayAddThreeButNotAfterTheFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flankers1 = scn.GetShadowCard("flankers1");
		var flankers2 = scn.GetShadowCard("flankers2");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCardsToHand(flankers1, flankers2, marksman);
		assertInHand(flankers1, flankers2, marksman);
		scn.FreepsPass(); // the fellowship's first move of the turn, to site 2

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetMoveCount());
		assertEquals(20, scn.GetTwilight());

		// Moved once: nothing is offered as the first copy is played.
		scn.ShadowPlayCard(flankers1);
		assertEquals(13, scn.GetTwilight()); // 20 - 5 to play - 2 roaming at site 2
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Nobody is assigned, so the first copy is still in play when the fellowship moves again, to site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetMoveCount());
		assertEquals(20, scn.GetTwilight());

		scn.ShadowPlayCard(flankers2);
		assertEquals(13, scn.GetTwilight()); // 20 - 5 to play - 2 roaming at site 3
		// The copy just played makes the offer; the copy already in play does not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flankers2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(flankers1));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(16, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [moria] Orc played after it sets neither copy off.
		scn.ShadowPlayCard(marksman);
		assertEquals(11, scn.GetTwilight()); // 16 - 3 to play - 2 roaming
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferAfterTheSecondMoveAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flankers = scn.GetShadowCard("flankers1");

		scn.StartGame();
		scn.MoveCardsToHand(flankers);
		assertInHand(flankers);
		scn.FreepsPass(); // first move, to site 2
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove(); // second move, to site 3

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetMoveCount());

		scn.ShadowPlayCard(flankers);
		assertEquals(13, scn.GetTwilight()); // 20 - 5 to play - 2 roaming
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flankers));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
