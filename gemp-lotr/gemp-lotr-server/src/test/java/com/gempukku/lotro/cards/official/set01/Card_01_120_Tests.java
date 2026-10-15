package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alive", "1_120");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2, damage +1 only
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, vitality 3; a regroup ability only
					// Different cards for the Free Peoples hand, none of them doing anything from there
					put("gimli", "1_13");
					put("merry", "1_303");
					put("pippin", "1_306");
					put("boromir", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AliveandUnspoiledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Alive and Unspoiled
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert an Uruk-hai. Plays to your support area.<br><b>Shadow:</b> Remove (3) and spot X burdens to make the Free Peoples player reveal X cards at random from hand. You may discard 1 revealed card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alive");

		assertEquals("Alive and Unspoiled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayAnUrukHaiOfAnyCultureExertsAndItGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetShadowCard("alive");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(alive);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, worker);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.ShadowPlayAvailable(alive));

		scn.ShadowPlayCard(alive);
		// Uruk-hai of two cultures; not the [isengard] Orc, who could exert as well.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertInZone(Zone.SUPPORT, alive);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetShadowCard("alive");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(alive);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		// Vitality 3 with 2 wounds: exhausted. The Worker, no Uruk-hai, could still exert.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(alive));
	}

	@Test
	public void RemovesThreeAndRevealsTwoCardsForTwoBurdensSpottedAndOneOfThemMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var alive = scn.GetShadowCard("alive");
		var shadowPippin = scn.GetShadowCard("pippin");
		scn.MoveCardsToHand(gimli, merry, shadowPippin);
		scn.MoveCardsToSupportArea(alive);
		assertInHand(gimli, merry, shadowPippin);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		// 1 + 1 for Frodo + 2 for Ettenmoors
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowActionAvailable(alive));

		scn.ShadowUseCardAction(alive);
		// X runs from 0 to the burdens there are (not to the 2 cards of the hand: the two are the same here,
		// and the next test has more cards than burdens).
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(1, scn.GetTwilight());
		// Shown to the Shadow player; the Free Peoples player, whose hand it is, is shown nothing.
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(shadowPippin, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowDecisionAvailable("discard"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, merry));
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(gimli);

		assertInDiscard(gimli);
		assertInHand(merry, shadowPippin);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyTheCardsRevealedAreOfferedForTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var alive = scn.GetShadowCard("alive");
		// Four different cards, two of them revealed at random.
		scn.MoveCardsToHand(gimli, merry, pippin, boromir);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());

		scn.ShadowUseCardAction(alive);
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		// Which cards are shown is chance; what is read is which they were.
		var gimliShown = scn.ShadowHasCardChoiceAvailable(gimli, false);
		var merryShown = scn.ShadowHasCardChoiceAvailable(merry, false);
		var pippinShown = scn.ShadowHasCardChoiceAvailable(pippin, false);
		var boromirShown = scn.ShadowHasCardChoiceAvailable(boromir, false);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseYes();

		assertEquals(gimliShown, scn.ShadowHasCardChoiceAvailable(gimli));
		assertEquals(merryShown, scn.ShadowHasCardChoiceAvailable(merry));
		assertEquals(pippinShown, scn.ShadowHasCardChoiceAvailable(pippin));
		assertEquals(boromirShown, scn.ShadowHasCardChoiceAvailable(boromir));
		assertEquals(2, scn.ShadowGetSelectableCount());
		assertTrue(scn.ShadowDecisionAvailable("discard"));
	}

	@Test
	public void OneBurdenSpottedRevealsOneCardAndTheDiscardMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var alive = scn.GetShadowCard("alive");
		scn.MoveCardsToHand(gimli, merry, pippin, boromir);
		scn.MoveCardsToSupportArea(alive);
		assertInHand(gimli, merry, pippin, boromir);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowUseCardAction(alive);
		scn.ShadowChoose("1");

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowDecisionAvailable("discard"));
		scn.ShadowChooseNo();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInHand(gimli, merry, pippin, boromir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoBurdenSpottedRemovesThreeRevealsNothingAndAsksNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var alive = scn.GetShadowCard("alive");
		scn.MoveCardsToHand(gimli, merry);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.ShadowUseCardAction(alive);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChoose("0");

		// The (3) is paid whatever X is; with X of 0 nothing is shown, and there is nothing to discard.
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowDecisionAvailable("discard"));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
	}

	@Test
	public void IsNotOfferedWithLessThanThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var alive = scn.GetShadowCard("alive");
		scn.MoveCardsToHand(gimli, merry);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(alive));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(alive));
	}
}
