package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("things", "2_59");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, site 4; when you play it, you may add (2)
					put("runner2", "1_178");
					// Goblin Scavengers: [moria] Orc, cost 3, site 4: "When you play this minion, you may play a weapon
					// from your discard pile on your [moria] Orc."
					put("scavengers", "1_179");
					// The Balrog, The Terror of Khazad-dum: a [moria] minion who is no Orc, cost 12, site 4; its text acts
					// only at an underground site.
					put("balrog", "12_79");
					put("armory", "1_173");     // Goblin Armory: [moria] condition, cost 0; adds (1) for each [moria] weapon played
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] hand weapon, cost 0, on a [moria] Orc; when played, may draw a card
					put("host", "1_187");       // Host of Thousands: [moria] Shadow event, cost 0: play a [moria] Orc from your discard pile
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
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Play a [moria] minion from your discard pile.
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
	public void PlaysAChosenMoriaMinionFromTheDiscardPileWhichThenOffersItsOwnWhenPlayedTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var scavengers = scn.GetShadowCard("scavengers");
		var balrog = scn.GetShadowCard("balrog");
		var armory = scn.GetShadowCard("armory");
		var scimitar = scn.GetShadowCard("scimitar");
		var host = scn.GetShadowCard("host");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(things, marksman);
		scn.MoveCardsToDiscard(scavengers, balrog, runner2, armory, scimitar, host, savage);
		// A [moria] minion in the other player's discard pile, of a title the Shadow player's pile does not hold.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("marksman"));

		assertInDiscard(scavengers, balrog, runner2, armory, scimitar, host, savage);
		scn.StartGame();
		// A [moria] Orc to bear the Scimitar.
		scn.MoveMinionsToTable(runner);
		// Enough for the dearest card in the pile: 2 for Foul Things + 12 for The Balrog + 2 roaming.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInDiscard(scavengers, balrog, runner2, armory, scimitar, host, savage);
		assertTrue(scn.ShadowPlayAvailable(things));
		scn.ShadowPlayCard(things);

		// Two [moria] minions of different races; not a [moria] condition, possession or event, a minion of another
		// culture, a card in hand or a card in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scavengers, balrog, runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, scimitar, host, savage, marksman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(scavengers);

		assertInZone(Zone.SHADOW_CHARACTERS, scavengers);
		assertInDiscard(balrog, runner2, armory, scimitar, host, savage);
		// It was played, not merely put into play: its own "when you play" text is offered, with the Scimitar in the
		// pile for it.  Foul Things is still being played meanwhile.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavengers));
		assertInZone(Zone.VOID, things);
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(things, scimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayableWithExactlyEnoughTwilightForItAndTheMinionItPlays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(runner);

		assertInDiscard(runner);
		scn.StartGame();
		// 2 set + 1 for Frodo + 2 for site 2 = 2 for Foul Things + 1 for Goblin Runner + 2 roaming
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertInDiscard(runner);
		assertTrue(scn.ShadowPlayAvailable(things));
		scn.ShadowPlayCard(things);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(things);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithTwilightOneShortOfItAndTheMinionItWouldPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(things);
		scn.MoveCardsToDiscard(runner);

		assertInDiscard(runner);
		scn.StartGame();
		// 1 set + 1 for Frodo + 2 for site 2: one short of 2 + 1 + 2 roaming
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertInDiscard(runner);
		assertFalse(scn.ShadowPlayAvailable(things));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWhenNoMoriaMinionIsInTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var things = scn.GetShadowCard("things");
		var armory = scn.GetShadowCard("armory");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		var freepsMarksman = scn.GetFreepsCard("marksman");
		scn.MoveCardsToHand(things, marksman);
		// A [moria] condition that costs nothing and an [isengard] minion; and the other player's [moria] minion in
		// his own discard pile.
		scn.MoveCardsToDiscard(armory, savage, freepsMarksman);

		assertInDiscard(armory, savage, freepsMarksman);
		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(armory, savage, freepsMarksman);
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

		assertInDiscard(runner);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInDiscard(runner);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(things));
	}
}
