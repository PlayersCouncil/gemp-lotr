package com.gempukku.lotro.mechanics.keywords;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A Response that answers the toil about to be paid counts toward whether the toil card can be played (Ketura,
 * Q-0381: A).  CR 4.2 "playing a card" step 2 asks whether every cost can be paid before any is; CR 4.2
 * "response": "You may respond more than once to the same situation."
 *
 * The one such Response is Úlairë Enquëa, Black Threat 12_175 ("If you are playing a [wraith] event that has toil
 * X, exert Úlairë Enquëa to reduce that event's twilight cost by X"), so she is the card here, beside Dark Powers
 * Strengthen 11_207, a [wraith] Shadow event of cost 6 with Toil 2 ("Spot a Nazgûl to draw 3 cards"; she is the
 * Nazgûl).  She is the only [wraith] character, so each of her exertions, for the Response or for toil, takes 2
 * off; she has vitality 4.  ToilRule counts the uses of the Response in a row, each with the wounds of those
 * before it, and the toil candidates after them, so her last exertion is counted once.
 */
public class ToilResponseTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "12_175");
					put("darkpowers", "11_207");
					// Goblin Patrol Troop: [moria] minions with no text, left in the draw deck for the event to draw
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheResponseIsCountedForEveryUseThatCanBePaidAndTheToilExertionAfterThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var darkpowers = scn.GetShadowCard("darkpowers");

		scn.StartGame();
		scn.SetShadowHand(darkpowers);
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(enquea));

		// Three exertions are left in her: two Responses and the toil exertion take 6 off, the whole cost.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(darkpowers));
		int deck = scn.GetShadowDeckCount();
		assertTrue(deck >= 3);

		scn.ShadowPlayCard(darkpowers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(enquea));
		// A third Response would exhaust her, and the toil exertion owed would then have nobody to make it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();

		// 6 - 2 - 2 = 2 is still to pay from an empty pool: her toil exertion is owed, and she is the one candidate.
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(darkpowers);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheResponseIsCountedOnlyAsOftenAsSheCanStillExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var darkpowers = scn.GetShadowCard("darkpowers");

		scn.StartGame();
		scn.SetShadowHand(darkpowers);
		scn.MoveMinionsToTable(enquea);
		scn.AddWoundsToChar(enquea, 1);
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// With 1 wound two exertions are left: 4 off, and 2 still to pay from an empty pool.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(darkpowers));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(darkpowers));
	}

	@Test
	public void HerLastExertionIsCountedOnceForTheResponseAndForToil() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var darkpowers = scn.GetShadowCard("darkpowers");

		scn.StartGame();
		scn.SetShadowHand(darkpowers);
		scn.MoveMinionsToTable(enquea);
		scn.AddWoundsToChar(enquea, 2);
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(enquea));

		// One exertion is left, for the Response or for toil and not both: 2 off, and 4 to pay.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(darkpowers));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(darkpowers));
	}
}
