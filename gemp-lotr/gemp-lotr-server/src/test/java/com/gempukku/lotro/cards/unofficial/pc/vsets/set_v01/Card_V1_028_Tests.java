package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wisp", "101_28");

					// Four Sarumans, each cost 4 and site 4, with nothing that happens when they are played
					put("eye", "3_69");         // Saruman, Servant of the Eye: [isengard]; an assignment ability never used here
					put("agent", "12_144");     // Saruman, Agent of the Dark Lord: [uruk-hai], lurker; a skirmish ability and a trigger on a companion's death
					put("keeper", "3_68");      // Saruman, Keeper of Isengard: [isengard]; makes Uruk-hai fierce and can protect them from wounds
					put("still", "17_48");      // Saruman, Coldly Still: [men]; strengthens [men] minions bearing possessions, and a skirmish ability
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] minion, cost 4 like the Sarumans; Damage +1 is its whole text
					// A Saruman that is not a minion. The event says "play Saruman" and no more, so this one can be played by it too.
					put("servant", "17_38");    // Saruman, Servant of Sauron: [isengard] follower, cost 1, played to the support area; its aid cost and its trigger need a bearer

					put("flock1", "101_25");    // Crebain Flock: Crow; a Shadow ability never used here
					put("flock2", "101_25");
					put("flock3", "101_25");
					put("crows1", "101_26");    // Crows of Isengard: Crow; its trigger is on being played, and these are put on the table
					put("crows2", "101_26");
					put("savage", "1_151");     // Uruk Savage: [isengard] minion that is not a Crow; Damage +1 is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, as a Free Peoples card in the Shadow player's hand
					put("promise", "2_112");    // A Promise: a [shire] condition, likewise
					put("runner", "1_178");     // Goblin Runner: a Shadow card for the hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JustaWispofCloudStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Just a Wisp of Cloud
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spell. Weather.
		 * 	Discard a Free Peoples card from hand to play Saruman from your draw deck or discard pile; his twilight cost is -1 for each Crow you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wisp");

		assertEquals("Just a Wisp of Cloud", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAFreePeoplesCardFromHandToPlaySarumanFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var eye = scn.GetShadowCard("eye");
		var agent = scn.GetShadowCard("agent");
		var keeper = scn.GetShadowCard("keeper");
		var still = scn.GetShadowCard("still");
		var servant = scn.GetShadowCard("servant");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetShadowCard("aragorn");
		var promise = scn.GetShadowCard("promise");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(wisp, aragorn, promise, runner, still);
		// Servant of the Eye, Agent of the Dark Lord, the follower Servant of Sauron and the Raiding Party stay
		// in the draw deck; Keeper of Isengard gives the discard pile a Saruman too, and Coldly Still is a
		// Saruman in hand.
		scn.MoveCardsToDiscard(keeper);

		scn.StartGame();
		// An [isengard] minion that is not a Crow
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertEquals(5, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, wisp, aragorn, promise);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.DECK, eye, agent, servant, party);
		assertTrue(scn.ShadowPlayAvailable(wisp));
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(wisp);

		// A companion and a condition of two Free Peoples cultures can pay; the Shadow minions cannot.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, promise));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, still));
		scn.ShadowChooseCard(promise);
		assertInZone(Zone.DISCARD, promise);
		assertInZone(Zone.HAND, aragorn, runner, still);
		// The event costs nothing itself.
		assertEquals(10, scn.GetTwilight());

		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		// Sarumans of two cultures, and of two card types: two minions and a follower
		assertTrue(scn.ShadowHasCardChoiceAvailable(eye, agent, servant));
		// A minion of Saruman's culture and cost with another name, the Saruman that is in the other pile,
		// and the Saruman that is in hand
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party, keeper, still));
		scn.ShadowChooseCardBPFromSelection(agent);

		assertInZone(Zone.SHADOW_CHARACTERS, agent);
		// No Crow, and the Uruk Savage is not one: 10 - 4 for Saruman - 2 roaming (a site 4 minion played at site 2)
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DECK, eye, servant, party);
		assertInZone(Zone.DISCARD, wisp, keeper, promise);
		assertInZone(Zone.HAND, aragorn, runner, still);
		assertEquals(3, scn.GetShadowHandCount());
		// The draw deck was looked through for Saruman, so it is reshuffled once the looking is done.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysSarumanFromTheDiscardPileForTwoLessWithTwoCrows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var eye = scn.GetShadowCard("eye");
		var agent = scn.GetShadowCard("agent");
		var keeper = scn.GetShadowCard("keeper");
		var servant = scn.GetShadowCard("servant");
		var party = scn.GetShadowCard("party");
		var flock1 = scn.GetShadowCard("flock1");
		var flock2 = scn.GetShadowCard("flock2");
		var crows1 = scn.GetShadowCard("crows1");
		var aragorn = scn.GetShadowCard("aragorn");
		// A third Crow sits in hand, where it cannot be spotted.
		scn.MoveCardsToHand(wisp, aragorn, flock2);
		// Servant of the Eye, Agent of the Dark Lord, the follower Servant of Sauron and the Raiding Party are
		// in the discard pile; Keeper of Isengard stays in the draw deck, and the opponent has a Keeper of
		// Isengard in his own discard pile.
		scn.MoveCardsToDiscard(eye, agent, servant, party);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("keeper"));

		scn.StartGame();
		scn.MoveMinionsToTable(flock1, crows1);
		scn.SetTwilight(1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, keeper);
		assertInZone(Zone.HAND, wisp, aragorn);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(wisp);
		// Aragorn is the only Free Peoples card in hand, so he is discarded without asking.
		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.HAND, flock2);

		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		// Sarumans of two cultures, and of two card types: two minions and a follower
		assertTrue(scn.ShadowHasCardChoiceAvailable(eye, agent, servant));
		// A minion of Saruman's culture and cost with another name; and Keeper of Isengard, whether it is
		// the one in this player's draw deck or the one in the opponent's discard pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party, keeper));
		scn.ShadowChooseCard(eye);

		assertInZone(Zone.SHADOW_CHARACTERS, eye);
		// A Crebain Flock and a Crows of Isengard in play: 4 - 2 for Saruman + 2 roaming at site 2 is all there was
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, wisp, aragorn, agent, servant, party);
		assertInZone(Zone.DECK, keeper);
		// Saruman came from the discard pile and the draw deck was never looked through, so it is not reshuffled.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSarumanInTheDiscardPileTheDrawDeckIsSearchedAndOneCrowTakesOneOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var eye = scn.GetShadowCard("eye");
		var keeper = scn.GetShadowCard("keeper");
		var party = scn.GetShadowCard("party");
		var crows1 = scn.GetShadowCard("crows1");
		var promise = scn.GetShadowCard("promise");
		scn.MoveCardsToHand(wisp, promise);
		// Every Saruman is in the draw deck; the discard pile has only a minion that is not one.
		scn.MoveCardsToDiscard(party);

		scn.StartGame();
		scn.MoveMinionsToTable(crows1);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, wisp, promise);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(wisp);
		assertInZone(Zone.DISCARD, promise);

		// Playing from the discard pile cannot be done, so it is not offered.
		assertFalse(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(eye, keeper));
		scn.ShadowChooseCardBPFromSelection(keeper);

		assertInZone(Zone.SHADOW_CHARACTERS, keeper);
		// One Crow: 10 - (4 - 1) for Saruman - 2 roaming at site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, wisp, promise, party);
		// The draw deck was looked through for Saruman, so it is reshuffled once the looking is done.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveCrowsSarumanCostsNothingButTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var eye = scn.GetShadowCard("eye");
		var agent = scn.GetShadowCard("agent");
		var promise = scn.GetShadowCard("promise");
		scn.MoveCardsToHand(wisp, promise);
		// Two Sarumans in the discard pile, and the other three, the follower among them, out of the draw deck
		scn.MoveCardsToDiscard(eye, agent);
		scn.MoveCardsToHand(scn.GetShadowCard("keeper"), scn.GetShadowCard("still"), scn.GetShadowCard("servant"));

		scn.StartGame();
		scn.MoveMinionsToTable("flock1", "flock2", "flock3", "crows1", "crows2");
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, wisp, promise);

		scn.ShadowPlayCard(wisp);
		assertInZone(Zone.DISCARD, promise);
		// With no Saruman in the draw deck, playing from there cannot be done, so it is not offered.
		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(eye, agent));
		scn.ShadowChooseCard(agent);

		assertInZone(Zone.SHADOW_CHARACTERS, agent);
		// Five Crows: 4 - 5 is below zero, so Saruman costs 0. 10 - 0 - 2 roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.DISCARD, wisp, eye);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSarumanInTheDrawDeckOrDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var eye = scn.GetShadowCard("eye");
		var agent = scn.GetShadowCard("agent");
		var keeper = scn.GetShadowCard("keeper");
		var still = scn.GetShadowCard("still");
		var servant = scn.GetShadowCard("servant");
		var promise = scn.GetShadowCard("promise");
		var runner = scn.GetShadowCard("runner");
		// Every Saruman this player owns, the follower included, is in his hand, and the opponent has one in
		// his own discard pile.
		scn.MoveCardsToHand(wisp, promise, runner, eye, agent, keeper, still, servant);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("keeper"));

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2: enough for a Saruman
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, promise, eye, agent, keeper, still, servant);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(eye));
		assertFalse(scn.ShadowPlayAvailable(wisp));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("Q-0019: with no Saruman to play, can the event still be played for its cost alone?")
	public void CanBePlayedForItsCostAloneWithNoSarumanInTheDrawDeckOrDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var promise = scn.GetShadowCard("promise");
		// Every Saruman is in hand again. The other reading of the rules: a card may be played though it will
		// have no effect, and nothing is lost by looking for a card in the draw deck and not finding it.
		scn.MoveCardsToHand(wisp, promise);
		scn.MoveCardsToHand(scn.GetShadowCard("eye"), scn.GetShadowCard("agent"), scn.GetShadowCard("keeper"), scn.GetShadowCard("still"), scn.GetShadowCard("servant"));

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, wisp, promise);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(wisp));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenThePoolIsOneShortOfSarumansReducedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var eye = scn.GetShadowCard("eye");
		var runner = scn.GetShadowCard("runner");
		// The follower Servant of Sauron costs 1, which one Crow takes off altogether, and a follower is never
		// roaming: left in the draw deck it could be played. It is in hand, out of the event's reach.
		scn.MoveCardsToHand(wisp, runner, scn.GetShadowCard("promise"), scn.GetShadowCard("servant"));
		// Servant of the Eye is in the discard pile; the other minions named Saruman stay in the draw deck.
		scn.MoveCardsToDiscard(eye);

		scn.StartGame();
		scn.MoveMinionsToTable("flock1");
		scn.SetTwilight(1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		// One Crow: each of these Sarumans costs 4 - 1 + 2 roaming at site 2 = 5, and there is 4.
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertFalse(scn.ShadowPlayAvailable(wisp));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyShadowCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(wisp, runner, savage);
		scn.MoveCardsToDiscard(scn.GetShadowCard("eye"));

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2: enough for any Saruman
		assertEquals(10, scn.GetTwilight());
		// Two other cards in hand, both Shadow cards; the event itself is not a card that can pay for it.
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertFalse(scn.ShadowPlayAvailable(wisp));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
