package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shepherd", "17_54");
					put("shepherd2", "17_54");
					// Stampeding Chief: unique [men] Man, cost 8, site 4, damage +1, fierce; "If you play Stampeding
					// Chief from your hand, remove (3) and spot another [men] minion or discard Stampeding Chief."
					put("chief", "17_49");
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, cost 2; a regroup ability only
					// Ted Sandyman, Chief's Men's Ally: unique [men] Hobbit, cost 1; a maneuver ability that liberates a
					// site of his player's (none is controlled here) and a regroup ability
					put("ted", "18_75");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, cost 3; a maneuver ability never used
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, cost 2, no text
					put("squad", "11_99");      // Squad of Haradrim: [men] Man, cost 3, to bear the Dagger; a regroup ability only
					put("dagger", "18_69");     // Henchman's Dagger: a [men] hand weapon, cost 0, no other text
					// Man of Bree: [men] Man, cost 2; strength +1 for each card in the Free Peoples hand
					put("bree", "11_90");
					// Fleet of Corsair Ships: a [men] possession in the support area; a play trigger and a regroup
					// ability, neither reached here
					put("fleet", "18_66");
					// Easterling Runner: [men] Man of another title, left in the draw deck
					put("runner", "13_90");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampedingShepherdStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Stampeding Shepherd
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Fierce</b>.<br><b>Maneuver:</b> Discard this minion and a [men] minion with a twilight cost 2 or less from play to play Stampeding Chief from your draw deck. It is twilight cost -8.
		*/

		var scn = GetScenario();

		var shepherd = scn.GetShadowCard("shepherd");

		assertEquals("Stampeding Shepherd", shepherd.getBlueprint().getTitle());
		assertNull(shepherd.getBlueprint().getSubtitle());
		assertFalse(shepherd.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shepherd.getBlueprint().getSide());
		assertEquals(Culture.MEN, shepherd.getBlueprint().getCulture());
		assertEquals(CardType.MINION, shepherd.getBlueprint().getCardType());
		assertEquals(Race.MAN, shepherd.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(shepherd, Keyword.FIERCE));
		assertEquals(5, shepherd.getBlueprint().getTwilightCost());
		assertEquals(11, shepherd.getBlueprint().getStrength());
		assertEquals(2, shepherd.getBlueprint().getVitality());
		assertEquals(4, shepherd.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsItselfAndACheapMenMinionToPlayStampedingChiefFromTheDrawDeckEightCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd = scn.GetShadowCard("shepherd");
		var shepherd2 = scn.GetShadowCard("shepherd2");
		var chief = scn.GetShadowCard("chief");
		var hillsman = scn.GetShadowCard("hillsman");
		var ted = scn.GetShadowCard("ted");
		var warrior = scn.GetShadowCard("warrior");
		var brigand = scn.GetShadowCard("brigand");
		var squad = scn.GetShadowCard("squad");
		var dagger = scn.GetShadowCard("dagger");
		var bree = scn.GetShadowCard("bree");
		var fleet = scn.GetShadowCard("fleet");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(shepherd, shepherd2, hillsman, ted, warrior, brigand, squad);
		scn.AttachCardsTo(squad, dagger);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, bree);
		assertInZone(Zone.SHADOW_CHARACTERS, shepherd, shepherd2, hillsman, ted, warrior, brigand, squad);
		assertInZone(Zone.ATTACHED, dagger);
		assertInZone(Zone.STACKED, bree);
		assertInZone(Zone.DECK, chief, runner);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(shepherd));
		scn.ShadowUseCardAction(shepherd);

		// A [men] minion of cost 2 or 1 in play; not one of cost 3, a [dunland] one of cost 2, a [men] possession of
		// cost 0, a [men] minion of cost 2 stacked (not in play), or the other Shepherd.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warrior, brigand, dagger, bree, shepherd2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertInDiscard(shepherd, ted);
		assertInZone(Zone.SHADOW_CHARACTERS, shepherd2, hillsman, warrior, brigand, squad);

		// The deck is shown first, each card listed as not selectable; then the Chief may be chosen and a [men]
		// minion of another title is not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chief, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(chief));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(chief);

		assertInZone(Zone.SHADOW_CHARACTERS, chief);
		// 8 printed + 2 roaming at site 2 - 8
		assertEquals(8, scn.GetTwilight());
		// Shuffled as the Chief leaves the deck looked through, and again once the look is over
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DECK, runner);
		// Played from the draw deck, not from hand: the Chief's own text is not answered and he stays.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOtherMenMinionOfCostTwoOrLessInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd = scn.GetShadowCard("shepherd");
		var shepherd2 = scn.GetShadowCard("shepherd2");
		var chief = scn.GetShadowCard("chief");
		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var brigand = scn.GetShadowCard("brigand");
		var squad = scn.GetShadowCard("squad");
		var dagger = scn.GetShadowCard("dagger");
		var bree = scn.GetShadowCard("bree");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.MoveMinionsToTable(shepherd, shepherd2, warrior, brigand, squad);
		scn.AttachCardsTo(squad, dagger);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, bree);
		assertInZone(Zone.SHADOW_CHARACTERS, shepherd, shepherd2, warrior, brigand, squad);
		assertInZone(Zone.ATTACHED, dagger);
		assertInZone(Zone.STACKED, bree);
		assertInZone(Zone.DECK, chief, hillsman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowActionAvailable(shepherd));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.MoveMinionsToTable(hillsman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(shepherd));
	}

	@Test
	public void WithNoChiefInTheDrawDeckTheCostIsPaidAndTheDeckIsShownAndShuffledForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd = scn.GetShadowCard("shepherd");
		var chief = scn.GetShadowCard("chief");
		var hillsman = scn.GetShadowCard("hillsman");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(shepherd, hillsman);
		scn.MoveCardsToDiscard(chief);
		assertInZone(Zone.SHADOW_CHARACTERS, shepherd, hillsman);
		assertInDiscard(chief);
		assertInZone(Zone.DECK, runner);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shuffles = scn.GetShadowDeckShuffleCount();

		// A play from the draw deck carries no penalty when nothing is found (Q-0019): it is offered.
		assertTrue(scn.ShadowActionAvailable(shepherd));
		scn.ShadowUseCardAction(shepherd);

		// The Hillsman, the one minion that can be discarded, goes unasked.
		assertInDiscard(shepherd, hillsman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.ShadowDismissRevealedCards();

		assertInDiscard(chief);
		assertEquals(10, scn.GetTwilight());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
