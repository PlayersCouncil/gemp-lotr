package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_46");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Captain, and an Uruk Savage in the discard pile.
	protected VirtualTableScenario GetCaptainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "2_46");
					put("savage", "1_151");     // Uruk Savage: Uruk-hai, cost 2, site number 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Uruk Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Shadow:</b> Remove (1) and exert Uruk Captain to play an Uruk-hai from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Uruk Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "Remove (1) and exert Uruk Captain to play an Uruk-hai from your discard pile": a play from a pile as an
	// effect is treated as a cost, so the pool must pay for the removal and the Uruk-hai together (F-0046,
	// F-0141).  The definition did not count the (1) it removes: with exactly the Uruk-hai's cost in the pool the
	// ability was offered, took the token, exerted the Captain and played nothing.
	// F-0451.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheUrukHaiTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCaptainScenario();

		var captain = scn.GetShadowCard("captain");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(captain);

		// At site 2 the Savage is roaming: 2 and 2 more.  (4) in the pool pays for him or for the removal.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(captain));
		scn.ShadowUseCardAction(captain);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(captain));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void UrukCaptainTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
