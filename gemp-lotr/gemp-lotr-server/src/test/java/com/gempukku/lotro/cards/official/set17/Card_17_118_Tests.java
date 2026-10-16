package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_118");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "17_118");
					// Four [uruk-hai] hunters of four blueprints, site 5, none with a text on being played.
					// For the hand, the only two cards there, which pay the discard of 2 without a choice:
					put("tracking", "15_171"); // Tracking Uruk: Hunter 1 and nothing else
					put("guard", "17_126");    // White Hand Guard: a skirmish ability
					// For the discard pile:
					put("butcher", "17_122");  // White Hand Butcher, cost 4: Hunter 6 and nothing else
					put("seeking", "15_169");  // Seeking Uruk: a text in fierce skirmishes only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VilePitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vile Pit
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot an [uruk-hai] hunter.<br>At the start of each Shadow phase, you may discard 2 [uruk-hai] cards from hand to play an [uruk-hai] hunter from your discard pile.<br><b>Regroup:</b> Discard this possession to return an [uruk-hai] hunter from play to your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Vile Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheHunterPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var pit = scn.GetShadowCard("pit");
		var tracking = scn.GetShadowCard("tracking");
		var guard = scn.GetShadowCard("guard");
		var butcher = scn.GetShadowCard("butcher");
		var seeking = scn.GetShadowCard("seeking");

		scn.MoveCardsToSupportArea(pit);
		scn.MoveCardsToHand(tracking, guard);
		scn.MoveCardsToDiscard(butcher, seeking);

		scn.StartGame();
		assertInHand(tracking, guard);
		assertInDiscard(butcher, seeking);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(tracking, guard);
		assertInDiscard(butcher, seeking);
		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(tracking, guard);

		// The two hunters that lay in the discard pile before the cost was paid, and neither of the two that lie
		// there now because they paid it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher, seeking));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tracking, guard));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(butcher);

		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		// 4, and 2 for roaming (a site 5 minion at site 2)
		assertEquals(17, scn.GetTwilight());
		assertInDiscard(tracking, guard, seeking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void VilePitTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
