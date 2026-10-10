package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * An event whose text plays a card from the discard pile is not offered unless the pool can pay for the event and
 * the card together, wherever in the event's text the play stands.
 *
 * Ketura on F-0046: playing a card from the discard pile as an effect "should block until both can be paid for;
 * rules state that effects playing from discard pile have to essentially be treated like costs."  The game asks
 * that by itself of a play that is the whole of an event's effect.  A play that an event makes only somewhere
 * ("If the fellowship is in region 3, ...") sits inside a condition, where the game does not ask beforehand; such
 * an event says it in a requirement of its own (`Playable` from the discard pile), and that requirement counts
 * the event's own twilight cost as already gone from the pool (F-0058).
 *
 * The card is Little Snuffler 13_52, a Shadow event of cost 2:
 *   "If the fellowship is in region 1, spot Gollum to wound a companion.
 *    If the fellowship is in region 2, spot Gollum to exert a companion.
 *    If the fellowship is in region 3, play a [gollum] minion from your discard pile."
 * In region 3 it used to be offered with any pool of 2 or more: it took its (2) and, the pool being short of the
 * minion, or the discard pile holding none, played nothing.
 *
 * Gollum, Dark as Darkness 9_28 is the [gollum] minion: cost 2, site number 3.  Region 3 is sites 7 to 9; the tests
 * there are at site 7, where he is not roaming, so event and minion together cost 4.
 */
public class EventThatPlaysFromADiscardPileNeedsBothCostsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snuffler", "13_52");   // Little Snuffler
					put("gollum", "9_28");      // Gollum, Dark as Darkness: [gollum] minion, cost 2, site 3; abilities used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, cost 6, site 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InRegion3WithThePoolOneShortOfTheEventAndTheMinionTogetherTheEventIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(snuffler);
		scn.MoveCardsToDiscard(gollum);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 2 for the event and 2 for Gollum: 3 pays for the event, and for Gollum, but not for both.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		assertInHand(snuffler);
		assertInDiscard(gollum);
	}

	@Test
	public void InRegion3WithThePoolPayingForBothTheMinionIsPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(snuffler);
		scn.MoveCardsToDiscard(gollum, troop);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);

		// Nothing is asked: region 3's is the one sentence that applies, and Gollum the one [gollum] minion.
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInDiscard(snuffler, troop);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion3WithNoGollumMinionInTheDiscardPileTheEventIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(snuffler);
		// A minion of another culture in the discard pile, and plenty to pay for it.
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));

		// A [gollum] minion in play is none in the discard pile.
		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));

		scn.MoveCardsToDiscard(gollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion1TheEventNeedsOnlyItsOwnCostAndGollumSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(snuffler);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No Gollum to spot: not offered, whatever the pool.
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));

		// Gollum in play, nothing in the discard pile, and a pool of exactly the event's cost: region 3's
		// sentence asks nothing of a fellowship in region 1.
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo));
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(snuffler);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
