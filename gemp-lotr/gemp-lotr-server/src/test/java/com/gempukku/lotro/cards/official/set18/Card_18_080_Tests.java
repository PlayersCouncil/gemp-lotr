package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_80");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gothmog, and a Mountain Orc in the discard pile.
	protected VirtualTableScenario GetGothmogScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "18_80");
					// Mountain Orc: [orc] Orc, cost 3, site number 4, vitality 2; its text answers an assignment
					put("orc", "11_129");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GothmogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gothmog, Morgul Leader
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each of the fellowship and minion archery totals is +3.<br><b>Regroup:</b> Remove (2) to play an [orc] minion from your discard pile. That minion comes into play exhausted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gothmog", card.getBlueprint().getTitle());
		assertEquals("Morgul Leader", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "Remove (2) to play an [orc] minion from your discard pile": a play from a pile as an effect is treated as
	// a cost, so the pool must pay for the removal and the minion together (F-0046, F-0141).  The definition did
	// not count the (2) it removes: with the minion's cost in the pool and no more the ability was offered, took
	// the tokens and played nothing.
	// F-0453.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheOrcMinionTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGothmogScenario();

		var gothmog = scn.GetShadowCard("gothmog");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToDiscard(orc);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		// Gothmog is put down in the regroup phase: before it his archery bonus would be in the way.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(gothmog);

		// At site 2 the Mountain Orc is roaming: 3 and 2 more.  (6) in the pool pays for him, or for the removal
		// and four tokens beside.
		scn.SetTwilight(6);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gothmog));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gothmog));
		scn.ShadowUseCardAction(gothmog);

		// "That minion comes into play exhausted."
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertEquals(1, scn.GetWoundsOn(orc));
		assertTrue(scn.IsExhausted(orc));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GothmogTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());
	}
}
