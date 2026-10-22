package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haste", "10_109");
					put("haste2", "10_109");

					put("sam", "1_311");           // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability never used here
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; a skirmish ability never used here
					put("trollslayer", "103_31");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally; a skirmish ability never used here
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here

					put("savage", "1_151");        // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
					put("troop", "1_177");         // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MakeHasteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Make Haste
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Stealth</b>. Spot a [shire] companion and return him or her to your hand to make a Shadow player choose to remove (2) or discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haste");

		assertEquals("Make Haste", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ReturnsAShireCompanionToHandAndTheShadowPlayerChoosesToRemoveTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, smeagol, trollslayer);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(haste);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertInPlay(sam, smeagol);
		assertTrue(scn.FreepsPlayAvailable(haste));
		scn.FreepsPlayCard(haste);

		// [shire] companions, a Hobbit and one who is no Hobbit.  Not the Ring-bearer, who cannot be returned to
		// hand (CR 4.2 "Ring-bearer"), a Hobbit companion of [gondor], nor a [shire] Hobbit ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, trollslayer, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertInHand(sam);
		assertInPlay(smeagol, frodo, trollslayer, bounder);

		// The Shadow player, and not the Free Peoples player, chooses between the two.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.ShadowChoiceAvailable("Discard a minion"));
		scn.ShadowChoose("Remove 2 twilight");

		// 5 - 2
		assertEquals(3, scn.GetTwilight());
		assertInPlay(savage, troop);
		assertInDiscard(haste);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayChooseToDiscardAMinionOfHisChoiceInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(haste);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertInPlay(smeagol);
		scn.FreepsPlayCard(haste);
		// The one [shire] companion who can be returned goes with nothing asked.
		assertInHand(smeagol);

		scn.ShadowChoose("Discard a minion");

		// The Shadow player picks the minion: minions of two cultures and two races.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(troop);

		assertInDiscard(troop);
		assertInPlay(savage);
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(haste);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithLessThanTwoTwilightTheMinionIsDiscardedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var haste2 = scn.GetFreepsCard("haste2");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToHand(haste, haste2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertInPlay(savage);
		scn.FreepsPlayCard(haste);
		scn.FreepsChooseCard(sam);

		// (2) cannot be removed in full from a pool of 1, so the minion is discarded: no choice is asked.
		assertFalse(scn.ShadowDecisionAvailable("Remove 2 twilight"));
		assertInDiscard(savage);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// With no minion left and still only 1 in the pool, neither can be done in full: both are offered, and
		// as much of the one chosen as can be done is done (CR 4.2 "effect").
		scn.ShadowPass();
		scn.FreepsPlayCard(haste2);
		assertInHand(smeagol);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Remove 2 twilight");

		assertEquals(0, scn.GetTwilight());
		assertInDiscard(haste, haste2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionLeftTwoTwilightAreRemovedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var haste2 = scn.GetFreepsCard("haste2");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToHand(haste, haste2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		scn.FreepsPlayCard(haste);
		scn.FreepsChooseCard(sam);
		scn.ShadowChoose("Discard a minion");
		// The only minion goes with nothing more asked.
		assertInDiscard(savage);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// No minion is left to discard, so the (2) is removed with no choice asked.
		scn.ShadowPass();
		scn.FreepsPlayCard(haste2);
		assertInHand(smeagol);
		assertFalse(scn.ShadowDecisionAvailable("Discard a minion"));
		// 5 - 2
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(haste, haste2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyShireCompanionIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var frodo = scn.GetRingBearer();
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(trollslayer, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(haste);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Frodo is a [shire] companion but cannot be returned to hand; a [gondor] Hobbit companion, a [shire]
		// Hobbit ally and a Man are beside him.
		assertInPlay(frodo, trollslayer, bounder, aragorn);
		assertInHand(haste);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(haste));
	}
}
