package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_043_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rearguard", "103_43");
					put("rearguard2", "103_43");
					put("rearguard3", "103_43");
					put("scout", "4_252");      // Southron Scout: [raider] Man, Southron, ambush (2), strength 3; keywords only
					put("scout2", "4_252");
					put("soldier", "4_254");    // Southron Soldier: [raider] Man, Southron, ambush (2), strength 4; keywords only
					put("soldier2", "4_254");
					put("attacker", "7_141");   // Easterling Attacker: [raider] Man and no Southron; a skirmish ability never used here
					put("attacker2", "7_141");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text at all
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskRearguardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Rearguard
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Artifact
		 * Subtype: Support area
		 * Strength: 5
		 * Vitality: 5
		 * Game Text: Regroup: Stack a Southron Man here.
		 * 	Maneuver: Remove (5) to make this artifact a <b>fierce</b> mounted Southron minion until the end of the turn
		 * 	that is strength +3 and <b>ambush (1)</b> for each Southron stacked on it.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rearguard");

		assertEquals("Bladetusk Rearguard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		// The strength of 5 and vitality of 5 are printed on the card, but it has neither until its Maneuver
		// ability has made it a minion. They are read in the tests of that ability.
	}

	@Test
	public void RegroupActionStacksASouthronManFromPlayOnTheCopyUsedUntilNoneIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var rearguard2 = scn.GetShadowCard("rearguard2");
		var rearguard3 = scn.GetShadowCard("rearguard3");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var scoutInHand = scn.GetShadowCard("scout2");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToSupportArea(rearguard, rearguard2, rearguard3);
		scn.MoveCardsToHand(scoutInHand);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, soldier, attacker);
		// No card is printed a Southron and not a Man. A third copy is one for the turn: its own Maneuver ability
		// makes it a Southron minion until the end of the turn.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsType(rearguard3, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard3, Keyword.SOUTHRON));
		assertEquals(0, scn.GetStackedCards(rearguard).size());
		assertEquals(0, scn.GetStackedCards(rearguard2).size());
		assertTrue(scn.ShadowActionAvailable(rearguard));
		assertTrue(scn.ShadowActionAvailable(rearguard2));

		scn.ShadowUseCardAction(rearguard);

		// The two Southron Men in play; not the Man who is no Southron, the Southron minion who is no Man, or the
		// Southron Man in hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attacker, rearguard3, scoutInHand));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(scout);

		assertInZone(Zone.STACKED, scout);
		assertEquals(1, scn.GetStackedCards(rearguard).size());
		assertTrue(scn.GetStackedCards(rearguard).contains(scout));
		assertEquals(0, scn.GetStackedCards(rearguard2).size());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, attacker, rearguard3);
		assertInZone(Zone.HAND, scoutInHand);

		// The other copy takes the one Southron Man left, who is chosen without asking.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rearguard));
		assertTrue(scn.ShadowActionAvailable(rearguard2));
		scn.ShadowUseCardAction(rearguard2);

		assertInZone(Zone.STACKED, soldier);
		assertEquals(1, scn.GetStackedCards(rearguard).size());
		assertEquals(1, scn.GetStackedCards(rearguard2).size());
		assertTrue(scn.GetStackedCards(rearguard2).contains(soldier));

		// With no Southron Man in play there is nothing to stack, and no copy offers its action: the Man who is
		// no Southron and the Southron who is no Man are still there.
		scn.FreepsPass();
		assertInZone(Zone.SHADOW_CHARACTERS, attacker, rearguard3);
		assertFalse(scn.ShadowActionAvailable(rearguard));
		assertFalse(scn.ShadowActionAvailable(rearguard2));
		assertFalse(scn.ShadowActionAvailable(rearguard3));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithFourInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(rearguard);
		scn.StackCardsOn(rearguard, scout);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(rearguard));
		assertFalse(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionRemovesFiveAndMakesItAFierceMountedSouthronMinionStrongerForEachSouthronStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var rearguard2 = scn.GetShadowCard("rearguard2");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var attacker = scn.GetShadowCard("attacker");
		var attacker2 = scn.GetShadowCard("attacker2");
		var soldier2 = scn.GetShadowCard("soldier2");
		var scoutInPlay = scn.GetShadowCard("scout2");
		scn.MoveCardsToSupportArea(rearguard, rearguard2);
		// On the one used: two Southron Men and two Men who are no Southrons, so that two Southrons are stacked
		// there among four Men and four [raider] cards.
		// On the other copy: a Southron, who is not stacked on the one used. In play: another, who is not stacked.
		scn.StackCardsOn(rearguard, scout, soldier2, attacker, attacker2);
		scn.StackCardsOn(rearguard2, soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutInPlay);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, rearguard, rearguard2);
		assertTrue(scn.IsType(rearguard, CardType.ARTIFACT));
		assertFalse(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.HasKeyword(rearguard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertFalse(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.HasKeyword(rearguard, Keyword.AMBUSH));
		assertTrue(scn.ShadowActionAvailable(rearguard));

		scn.ShadowUseCardAction(rearguard);

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		// 5 + 3 for the Scout + 3 for the Soldier, and nothing for either Easterling, for the other copy's stack
		// or for the Southron in play. Counting Men, or [raider] cards, or the cards stacked, would make it 17.
		assertEquals(11, scn.GetStrength(rearguard));
		assertEquals(5, scn.GetVitality(rearguard));
		assertEquals(2, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));
		assertEquals(4, scn.GetStackedCards(rearguard).size());

		// The other copy is as it was.
		assertInZone(Zone.SUPPORT, rearguard2);
		assertFalse(scn.IsType(rearguard2, CardType.MINION));
		assertFalse(scn.HasKeyword(rearguard2, Keyword.FIERCE));
		assertEquals(1, scn.GetStackedCards(rearguard2).size());

		// With (5) still in the pool the copy in the support area offers its action; the one that is already
		// that minion does not offer it again.
		scn.FreepsPass();
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(rearguard2));
		assertFalse(scn.ShadowActionAvailable(rearguard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNothingStackedItIsStrengthFiveVitalityFiveAndEachSouthronManItThenStacksAddsThreeAndAmbushOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetStackedCards(rearguard).size());
		assertFalse(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.ShadowActionAvailable(rearguard));

		scn.ShadowUseCardAction(rearguard);

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard, Keyword.FIERCE));
		assertEquals(5, scn.GetStrength(rearguard));
		assertEquals(5, scn.GetVitality(rearguard));
		assertFalse(scn.HasKeyword(rearguard, Keyword.AMBUSH));

		// In the regroup phase of the same turn it is still that minion, and what it stacks counts at once.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertEquals(5, scn.GetStrength(rearguard));

		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, soldier));
		scn.ShadowChooseCard(scout);

		assertEquals(1, scn.GetStackedCards(rearguard).size());
		// 5 + 3
		assertEquals(8, scn.GetStrength(rearguard));
		assertEquals(1, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));

		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);

		assertEquals(2, scn.GetStackedCards(rearguard).size());
		// 5 + 3 + 3
		assertEquals(11, scn.GetStrength(rearguard));
		assertEquals(2, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));
		assertEquals(5, scn.GetVitality(rearguard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StaysThatMinionThroughASecondMoveAndIsDiscardedWithItsStackWhenTheTurnsMinionsAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rearguard = scn.GetShadowCard("rearguard");
		var rearguard2 = scn.GetShadowCard("rearguard2");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(rearguard, rearguard2);
		scn.StackCardsOn(rearguard, scout);
		scn.StackCardsOn(rearguard2, soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);

		assertTrue(scn.IsType(rearguard, CardType.MINION));
		// 5 + 3
		assertEquals(8, scn.GetStrength(rearguard));

		// At the end of the regroup phase, and after the fellowship has moved again, it is still that minion.
		scn.SkipToMovementDecision();
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertEquals(8, scn.GetStrength(rearguard));

		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertEquals(8, scn.GetStrength(rearguard));
		assertEquals(5, scn.GetVitality(rearguard));
		assertEquals(1, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));

		// The turn's last act is to discard every minion, and it is one: it goes with the Troop, and its stack
		// with it. The copy that was never made a minion stays where it is, stack and all.
		scn.SkipToOtherPlayersTurn();
		assertInDiscard(rearguard, scout, troop);
		assertInZone(Zone.SUPPORT, rearguard2);
		assertInZone(Zone.STACKED, soldier);
		assertEquals(1, scn.GetStackedCards(rearguard2).size());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
