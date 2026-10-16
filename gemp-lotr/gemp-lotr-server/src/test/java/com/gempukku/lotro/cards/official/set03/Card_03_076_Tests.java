package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gamble", "3_76");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
					put("troll", "1_165");        // Cave Troll of Moria: [moria] Troll, damage +1 and fierce; its other texts are about playing it
					put("wallcrawler", "1_184");  // Goblin Wallcrawler: [moria] Orc, cost 2; standing texts underground and on the archery total
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, cost 1; its trigger is on being played
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc minion; a skirmish ability only
					put("relics", "1_195");       // Relics of Moria: [moria] support-area condition; a Shadow ability used only on purpose
					put("misadventure", "1_129"); // The Misadventure of Mr. Underhill: "The twilight cost of each companion played to a site which is not a sanctuary is +2"

					// Free Peoples cards for the hand, of three different twilight costs
					put("merry", "1_303");        // Merry, From O'er the Brandywine: cost 1
					put("gimli", "1_13");         // Gimli, Son of Gloin: cost 2
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: cost 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DangerousGambleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Dangerous Gamble
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot a [moria] minion to reveal a card at random from the Free Peoples player's hand. Add (X), where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var gamble = scn.GetFreepsCard("gamble");

		assertEquals("Dangerous Gamble", gamble.getBlueprint().getTitle());
		assertNull(gamble.getBlueprint().getSubtitle());
		assertFalse(gamble.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, gamble.getBlueprint().getSide());
		assertEquals(Culture.MORIA, gamble.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, gamble.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(gamble, Timeword.SHADOW));
		assertEquals(2, gamble.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMoriaMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamble = scn.GetShadowCard("gamble");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var soldier = scn.GetShadowCard("soldier");
		var relics = scn.GetShadowCard("relics");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(gamble, wallcrawler, merry);
		assertInHand(gamble, wallcrawler, merry);

		scn.StartGame();
		// A [moria] card that is no minion, and a minion of another culture.
		scn.MoveCardsToSupportArea(relics);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(gamble));

		scn.ShadowPlayCard(wallcrawler);
		assertInZone(Zone.SHADOW_CHARACTERS, wallcrawler);
		assertTrue(scn.ShadowPlayAvailable(gamble));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RevealsACardAtRandomToTheShadowPlayerAndAddsItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamble = scn.GetShadowCard("gamble");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(gamble, runner, aragorn);
		assertInHand(gamble, runner, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(gamble));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(gamble);
		// The Free Peoples player's one card is shown to the Shadow player, his own hand is not, and the hand's
		// owner is shown nothing.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowDismissRevealedCards();

		// 2 removed to play the event, and 4 added for Aragorn.
		assertEquals(pool - 2 + 4, scn.GetTwilight());
		// The card revealed stays in the hand it was shown from.
		assertInHand(aragorn, runner);
		assertInDiscard(gamble);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingACaveTrollACardOfCostOneAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamble = scn.GetShadowCard("gamble");
		var troll = scn.GetShadowCard("troll");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(gamble, merry);
		assertInHand(gamble, merry);

		scn.StartGame();
		// The Troll, a [moria] minion who is no Orc, is the only [moria] minion (the Orc he was played beside is gone).
		scn.MoveMinionsToTable(troll);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(gamble));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(gamble);
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, false));
		scn.ShadowDismissRevealedCards();

		// 2 removed to play the event, and 1 added for Merry.
		assertEquals(pool - 2 + 1, scn.GetTwilight());
		assertInHand(merry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTwilightAddedIsTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamble = scn.GetShadowCard("gamble");
		var troop = scn.GetShadowCard("troop");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(gamble, merry, gimli, aragorn);
		assertInHand(gamble, merry, gimli, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(gamble);
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var merryShown = scn.ShadowHasCardChoiceAvailable(merry, false);
		var gimliShown = scn.ShadowHasCardChoiceAvailable(gimli, false);
		var aragornShown = scn.ShadowHasCardChoiceAvailable(aragorn, false);
		assertEquals(1, (merryShown ? 1 : 0) + (gimliShown ? 1 : 0) + (aragornShown ? 1 : 0));
		scn.ShadowDismissRevealedCards();

		// Costs 1, 2 and 4: the one shown is what is added, after the 2 the event cost.
		var shownCost = merryShown ? 1 : (gimliShown ? 2 : 4);
		assertEquals(pool - 2 + shownCost, scn.GetTwilight());
		assertInHand(merry, gimli, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyHandNothingIsRevealedAndNoTwilightIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamble = scn.GetShadowCard("gamble");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(gamble);
		assertInHand(gamble);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		// An event may be played though it will do nothing.
		assertTrue(scn.ShadowPlayAvailable(gamble));
		int pool = scn.GetTwilight();

		scn.MarkGameLog();
		scn.ShadowPlayCard(gamble);

		// Nobody is shown anything, the log says why, and nothing is added: only the 2 the event cost is gone.
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P1() + "'s hand"));
		assertEquals(pool - 2, scn.GetTwilight());
		assertInDiscard(gamble);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughACardInPlayMakesItDearerToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamble = scn.GetShadowCard("gamble");
		var troop = scn.GetShadowCard("troop");
		var misadventure = scn.GetShadowCard("misadventure");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(gamble, aragorn);
		assertInHand(gamble, aragorn);
		scn.MoveCardsToSupportArea(misadventure);
		assertInZone(Zone.SUPPORT, misadventure);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// At Ettenmoors, no sanctuary, a companion costs 2 more to play: Aragorn, printed 4, reads 6 in hand.
		assertEquals(6, scn.GetTwilightCost(aragorn));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(gamble);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		scn.ShadowDismissRevealedCards();

		// X is 4: a card in play changes another card's cost only as that card is played.
		assertEquals(pool - 2 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
