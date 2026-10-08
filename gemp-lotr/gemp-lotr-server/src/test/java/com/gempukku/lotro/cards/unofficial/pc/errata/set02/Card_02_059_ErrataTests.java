package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_059_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("things", "52_59");
					put("things2", "52_59");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, site 4; when you play it, you may add (2)
					put("runner2", "1_178");
					put("armory", "1_173");     // Goblin Armory: [moria] condition, cost 0; adds (1) for each [moria] weapon played
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] hand weapon, cost 0, on a [moria] Orc; when played, may draw a card
					put("host", "1_187");       // Host of Thousands: [moria] Shadow event, cost 0: play a [moria] Orc from your discard pile
					put("relentless", "1_194"); // Relentless: [moria] Response event
					put("drums", "1_168");      // Drums in the Deep: [moria] Skirmish event
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, Damage +1 is its whole text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoulThingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Foul Things
		 * Unique: false
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Play a [moria] card from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("things");

		assertEquals("Foul Things", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAMoriaMinionFromTheDiscardPileWhichThenOffersItsOwnWhenPlayedTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(things));
		scn.ShadowPlayCard(things);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// 2 for Foul Things + 1 for Goblin Runner + 2 roaming (site 4 minion at site 2), and + 2 from the Runner's own
		// "when you play" trigger: it was played, not merely put into play.  (The game adds the Runner's (2) without
		// asking, a known departure.)
		assertEquals(twilight - 5 + 2, scn.GetTwilight());
		assertInDiscard(things);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MoriaCardsOfEachTypeThatCanBePlayedNowAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var armory = scn.GetShadowCard("armory");
		var scimitar = scn.GetShadowCard("scimitar");
		var host = scn.GetShadowCard("host");
		var relentless = scn.GetShadowCard("relentless");
		var drums = scn.GetShadowCard("drums");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(things, marksman);
		scn.MoveCardsToDiscard(runner2, armory, scimitar, host, relentless, drums, savage);
		// A [moria] minion in the other player's discard pile.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("marksman"));

		scn.StartGame();
		// A [moria] Orc to bear the Scimitar.
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(things);

		// A minion, a condition, a possession and a Shadow event; not a Response event, an event of another phase, a
		// card of another culture, a card in hand or a card in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner2, armory, scimitar, host));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(relentless, drums, savage, marksman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertInDiscard(things, runner2, scimitar, host);
		// 2 for Foul Things + 0 for Goblin Armory
		assertEquals(twilight - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithExactlyEnoughTwilightForItAndTheMinionItIsPlayedBesideAnotherFoulThings() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var things2 = scn.GetShadowCard("things2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(things2, runner);

		scn.StartGame();
		// 2 set + 1 for Frodo + 2 for site 2 = 2 for Foul Things + 1 for Goblin Runner + 2 roaming
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(things));
		scn.ShadowPlayCard(things);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(things, things2);
		// All 5 spent, then 2 added by the Runner's own trigger.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithTwilightOneShortOfItAndTheMinionItWouldPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		// 1 set + 1 for Frodo + 2 for site 2: one short of 2 + 1 + 2 roaming
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(things));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWhenNothingInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var relentless = scn.GetShadowCard("relentless");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(things, marksman);
		// A [moria] Response event and an [isengard] minion.
		scn.MoveCardsToDiscard(relentless, savage);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(things));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(things));
	}

	@Test
	@Ignore("Q-0126: another Foul Things is a [moria] card in the discard pile, but the definition leaves it out by title")
	public void AnotherFoulThingsInTheDiscardPileMayBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var things2 = scn.GetShadowCard("things2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(things2, runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(things);

		assertTrue(scn.ShadowHasCardChoiceAvailable(things2, runner));
		scn.ShadowChooseCardBPFromSelection(things2);
		// The second Foul Things then plays the one [moria] card left in the discard pile.

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(things, things2);
		// 2 + 2 for the two Foul Things + 1 for Goblin Runner + 2 roaming, and + 2 from the Runner's trigger
		assertEquals(twilight - 7 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
