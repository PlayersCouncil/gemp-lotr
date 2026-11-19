package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chief", "17_49");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, cost 3; a maneuver ability only
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); "Each [men] minion bearing a
					// possession is strength +2" and a skirmish ability, neither reached here.
					put("saruman", "17_48");
					put("oath", "17_66");       // Wildman's Oath: a [men] possession in the support area, no minion
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Stampeding Savage: "Maneuver: Discard this minion and X minions stacked on a [men] possession to
					// play a [men] minion from your discard pile. That minion is twilight cost -X."
					put("savage", "17_53");
					put("pandemonium", "17_46"); // Pandemonium: "Play a [men] minion stacked on a [men] possession as if from hand."
					put("armor", "17_41");      // Ceremonial Armor: a [men] possession in the support area, to stack on
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampedingChiefStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Stampeding Chief
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 17
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>.<br>If you play Stampeding Chief from your hand, remove (3) and spot another [men] minion or discard Stampeding Chief.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("chief");

		assertEquals("Stampeding Chief", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandBesideAMenManItMayRemoveThreeAndStay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(chief);
		assertInHand(chief);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.FreepsPass();
		// 8 to play + 2 roaming at site 2 = 10, leaving exactly 3.
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();
		assertEquals(13, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(chief));
		scn.ShadowPlayCard(chief);

		// Both can be done in full, so the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Discard Stampeding Chief"));
		scn.ShadowChoose("Remove (3)");

		// 13 - 10 to play - 3 removed
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chief, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromHandBesideAMenWizardItMayBeDiscardedInsteadWithItsCostStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(chief);
		assertInHand(chief);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.FreepsPass();
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(chief);

		// Saruman, a [men] Wizard, is "another [men] minion" as well as a Man is.
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Discard Stampeding Chief"));
		scn.ShadowChoose("Discard Stampeding Chief");

		// 13 - 10 to play: the cost stays paid and nothing is removed.
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(chief);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoLeftInThePoolItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(chief);
		assertInHand(chief);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.FreepsPass();
		// 10 to play leaves 2: (3) cannot be removed.
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertEquals(12, scn.GetTwilight());

		scn.ShadowPlayCard(chief);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(chief);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherMenMinionItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");
		var oath = scn.GetShadowCard("oath");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(chief);
		scn.MoveCardsToSupportArea(oath);
		assertInHand(chief);
		assertInZone(Zone.SUPPORT, oath);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.FreepsPass();
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(chief);

		// A [men] possession and a [dunland] Man are no "other [men] minion", and the Chief is not another.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(chief);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromTheDiscardPileItStaysWithNothingToRemoveOrSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToDiscard(chief);
		assertInDiscard(chief);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// Exactly the 10 the Chief costs at site 2 (X is 0): nothing would be left to remove.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(savage));
		scn.ShadowUseCardAction(savage);
		// The Chief, the one card the pile offers, is played unasked.

		// The Savage discarded himself, so no other [men] minion is in play; were the text answered, the Chief
		// would be discarded.
		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, chief);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PlayedFromAPossessionAsIfFromHandItStaysWithNothingToRemoveOrSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");
		var pandemonium = scn.GetShadowCard("pandemonium");
		var armor = scn.GetShadowCard("armor");
		scn.MoveCardsToHand(pandemonium);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, chief);
		assertInHand(pandemonium);
		assertInZone(Zone.STACKED, chief);

		scn.StartGame();
		scn.FreepsPass();
		// Pandemonium costs 0; the Chief 10 at site 2: nothing would be left to remove.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The Chief is the one minion stacked, so it is played unasked.
		scn.ShadowPlayCard(pandemonium);

		// Played off the Armor, not from the hand: nothing is asked and the Chief stays, with no other
		// [men] minion in play.
		assertInZone(Zone.SHADOW_CHARACTERS, chief);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
