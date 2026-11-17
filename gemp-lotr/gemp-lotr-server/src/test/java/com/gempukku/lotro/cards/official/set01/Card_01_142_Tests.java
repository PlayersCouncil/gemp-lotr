package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "1_142");
					put("voice2", "1_142");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, strength 5, vitality 3, damage +1; no other text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1; no other text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, no Uruk-hai; a regroup ability only
					put("saruman", "17_116");   // Saruman, Master of the White Hand: [uruk-hai] Wizard, no Uruk-hai; companions resistance -1

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf; an archery ability used only on purpose
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: Gandalf; a fellowship ability used only on purpose
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TraitorsVoiceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Traitor's Voice
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, spot an Uruk-hai. Plays to your support area.
		 * While the Ring-bearer is exhausted or you can spot 5 burdens, the move limit for this turn is -1 (to a minimum of 1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("voice");

		assertEquals("Traitor's Voice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(voice, savage);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An [isengard] Orc and an [uruk-hai] Wizard: neither is an Uruk-hai.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(voice);
		assertInZone(Zone.SUPPORT, voice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(voice));

		scn.MoveMinionsToTable(guard);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(voice);
		assertInZone(Zone.SUPPORT, voice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWhileTheRingBearerIsExhaustedItMakesTheMoveLimitOneAndTheTurnEndsAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		// Vitality 4: exhausted with 3 wounds.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit());

		scn.ShadowPlayCard(voice);
		assertInZone(Zone.SUPPORT, voice);
		assertEquals(1, scn.GetMoveLimit());

		// Gimli takes the Savage, so that nothing happens to the exhausted Frodo.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void OneShortOfEachHalfTheLimitIsTwoAndTheFifthBurdenMakesItOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		// Frodo (vitality 4) one wound short of exhausted; Sam, Ring-bound but not the Ring-bearer, exhausted; 4 burdens.
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 3);
		scn.AddBurdens(4);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetBurdens());
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMoveLimit());

		scn.AddBurdens(1);
		assertEquals(5, scn.GetBurdens());
		scn.ShadowPass();
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheMoveLimitIsLoweredByExactlyOneAndOnceWhenBothHalvesHold() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var frodo = scn.GetRingBearer();
		var secret = scn.GetFreepsCard("secret");
		// Gandalf, a Dwarf and an Elf for Durin's Secret
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas");
		scn.MoveCardsToHand(secret);
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.AddBurdens(5);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetBurdens());

		// The limit cannot go below 1, so from 2 a penalty of 1 and a penalty of 2 read the same: raise it first.
		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1, both halves of the "or" holding
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCopiesLeaveTheLimitAtOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var voice2 = scn.GetShadowCard("voice2");
		scn.MoveCardsToSupportArea(voice, voice2);
		assertInZone(Zone.SUPPORT, voice, voice2);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.FreepsPass();
		assertEquals(5, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, voice, voice2);
		// 2 - 1 - 1, to a minimum of 1
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WhenTheRingBearerIsHealedInTheRegroupPhaseTheLimitIsStillOneForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		scn.RemoveWoundsFromChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		// No longer exhausted, and the change holds for the whole turn (CR 4.2 "move limit")
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetMoveLimit());

		scn.ShadowPass();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The other player's turn: the penalty went with the turn it was given in
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ThePenaltyHoldsThroughASecondMoveAfterTheBurdensHaveGoneAndEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas");
		scn.MoveCardsToHand(secret);
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.FreepsPlayCard(secret);
		assertEquals(5, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1

		// The fifth burden goes in the fellowship phase, the first of the turn.
		scn.RemoveBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// One move made of two: the fellowship is asked about another, and makes it
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// The Shadow phase after the second move: the penalty is still there
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Two moves of two: the turn ends without the question of a third
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.ShadowPass();
		// The Shadow player drew at the reconcile before the second move, so he is asked to reconcile again
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The other player's turn: 2 again
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ThePenaltyIsGivenAgainOnTheNextFreePeoplesTurnAndNotOnItsOwnersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.FreepsPass();
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetMoveLimit());

		// Player 2's turn: his own Shadow card is not active, and the 5 burdens are not his
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		// Player 1's next turn: the 5 burdens are still on his Ring-bearer, and the limit is 1 again
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(5, scn.FreepsGetBurdens());
		scn.FreepsPass();
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayersOwnExhaustedRingBearerAndBurdensDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var frodo = scn.GetRingBearer();
		var shadowFrodo = scn.GetShadowRingBearer();
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		// On his own turn, player 2's Ring-bearer is exhausted and carries 5 burdens.
		scn.AddWoundsToChar(shadowFrodo, 3);
		scn.AddBurdens(5);
		assertEquals(3, scn.GetWoundsOn(shadowFrodo));
		assertEquals(5, scn.ShadowGetBurdens());

		// Player 1's turn: his own Ring-bearer has no wound and no burden.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsPass();
		assertEquals(3, scn.GetWoundsOn(shadowFrodo));
		assertEquals(5, scn.ShadowGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
	}
}
