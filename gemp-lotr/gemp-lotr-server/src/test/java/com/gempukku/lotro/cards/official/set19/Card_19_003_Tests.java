package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twitching", "19_3");
					put("twitching2", "19_3");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6, damage +1; a skirmish ability never used
					put("warrior", "12_7");     // Dwarven Warrior: [dwarven] Dwarf companion, strength 6, vitality 3; his text answers his own play only
					put("farin", "1_11");       // Farin, Dwarven Emissary: [dwarven] Dwarf companion, strength 5; +2 against an Orc only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally; a fellowship ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("seeker", "4_195");     // Uruk Seeker: fierce, strength 6, vitality 2; its bonus is against a bearer of a search card only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwoBearersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twitching1", "19_3");
					put("twitching2", "19_3");
					// Gimli, Son of Gloin: a [dwarven] companion, strength 6, damage +1; a skirmish ability never used
					put("gimli", "1_13");
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion with no text once in play
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillTwitchingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Still Twitching
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a [dwarven] companion.
		 * 	Bearer gains hunter 2. Each time bearer wins a skirmish, you may transfer this condition to another [dwarven] companion.
		 * 	At the end of each turn, if bearer did not win a skirmish this turn, discard this condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("twitching");

		assertEquals("Still Twitching", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsDiscardedAtTheEndOfATurnInWhichItsBearerWonNoSkirmishAndStaysWhenHeWonOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoBearersScenario();

		// One copy on each of two [dwarven] companions: Gimli fights and wins, the Dwarf Guard fights nobody.
		var onGimli = scn.GetFreepsCard("twitching1");
		var onGuard = scn.GetFreepsCard("twitching2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, onGimli);
		scn.AttachCardsTo(guard, onGuard);
		assertAttachedTo(onGimli, gimli);
		assertAttachedTo(onGuard, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.PassSkirmishActions();

		// Gimli's 6, and the 2 of hunter 2, beats the Goblin Runner's 5. The transfer his copy then offers
		// is declined; the Guard's copy, whose bearer won nothing, offers none.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(onGimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(onGuard));
		scn.FreepsDeclineOptionalTrigger();
		assertInDiscard(runner);

		// Both are still borne when the fellowship decides not to move again.
		scn.SkipToMovementDecision();
		assertAttachedTo(onGimli, gimli);
		assertAttachedTo(onGuard, guard);

		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// The turn is over. Gimli won a skirmish in it and keeps his copy. The Guard did not, though another
		// [dwarven] companion did: his copy is in the discard pile.
		assertAttachedTo(onGimli, gimli);
		assertInDiscard(onGuard);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void CanBePlayedOnlyOnADwarvenCompanionAndGivesHimHunter2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twitching = scn.GetFreepsCard("twitching");
		var gimli = scn.GetFreepsCard("gimli");
		var warrior = scn.GetFreepsCard("warrior");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(twitching);
		scn.MoveCompanionsToTable(gimli, warrior, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertTrue(scn.FreepsPlayAvailable(twitching));

		scn.FreepsPlayCard(twitching);
		// Each [dwarven] companion may bear it; a [dwarven] ally, a [gondor] companion and the Ring-bearer may not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, warrior));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, aragorn, frodo));

		scn.FreepsChooseCard(warrior);
		assertAttachedTo(twitching, warrior);
		assertEquals(2, scn.GetKeywordCount(warrior, Keyword.HUNTER));
		// The other [dwarven] companion does not bear it and gains nothing.
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishItMayBeTransferredToAnotherDwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twitching = scn.GetFreepsCard("twitching");
		var twitching2 = scn.GetFreepsCard("twitching2");
		var gimli = scn.GetFreepsCard("gimli");
		var warrior = scn.GetFreepsCard("warrior");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(gimli, warrior, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		// A second copy on a [dwarven] companion who fights no skirmish.
		scn.AttachCardsTo(warrior, twitching);
		scn.AttachCardsTo(farin, twitching2);
		assertAttachedTo(twitching, warrior);
		assertAttachedTo(twitching2, farin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, seeker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gimli, savage }, new PhysicalCardImpl[] { warrior, seeker });

		// Gimli (6), who bears no Still Twitching, beats Uruk Savage (5): nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		// One wound for the win and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The Dwarven Warrior (6 + hunter 2 against a minion who is no hunter) beats Uruk Seeker (6), who lives
		// through it: only the copy he bears is offered.
		scn.FreepsResolveSkirmish(warrior);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(twitching));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(twitching2));

		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(twitching, warrior);
		assertAttachedTo(twitching2, farin);

		// The Seeker is fierce: the Warrior beats him a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(warrior, seeker);
		scn.FreepsResolveSkirmish(warrior);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(twitching));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(twitching2));

		scn.FreepsAcceptOptionalTrigger();
		// Another [dwarven] companion: not the bearer, not the [dwarven] ally, not the [gondor] companion, not a minion.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(warrior, grimir, aragorn, savage));

		scn.FreepsChooseCard(gimli);
		assertAttachedTo(twitching, gimli);
		assertAttachedTo(twitching2, farin);
		// The hunter 2 went with it.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheBearerIsTheOnlyDwarvenCompanionTheTransferIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twitching = scn.GetFreepsCard("twitching");
		var warrior = scn.GetFreepsCard("warrior");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		// A [dwarven] ally and a [gondor] companion beside him, neither of whom may take it.
		scn.MoveCompanionsToTable(warrior, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(warrior, twitching);
		assertAttachedTo(twitching, warrior);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior, savage);
		scn.FreepsResolveSkirmish(warrior);

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		// The Warrior (6 + 2) beats Uruk Savage (5).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(twitching, warrior);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNothingIsOfferedAndItIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twitching = scn.GetFreepsCard("twitching");
		var warrior = scn.GetFreepsCard("warrior");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(warrior, gimli);
		scn.AttachCardsTo(warrior, twitching);
		assertAttachedTo(twitching, warrior);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior, troop);
		scn.FreepsResolveSkirmish(warrior);

		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.PassSkirmishActions();

		// Goblin Patrol Troop (13) beats the Warrior (6 + 2) without overwhelming him.
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(twitching, warrior);

		scn.SkipToMovementDecision();
		assertAttachedTo(twitching, warrior);

		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// He lost the one skirmish he fought: at the end of the turn it is discarded.
		assertInDiscard(twitching);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TransferredToACompanionWhoWonNoSkirmishItIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twitching = scn.GetFreepsCard("twitching");
		var warrior = scn.GetFreepsCard("warrior");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(warrior, farin, gimli);
		scn.AttachCardsTo(warrior, twitching);
		assertAttachedTo(twitching, warrior);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior, savage);
		scn.FreepsResolveSkirmish(warrior);
		scn.PassSkirmishActions();

		// The Warrior wins; the condition goes to Farin, who fights no skirmish this turn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(twitching));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(farin);
		assertAttachedTo(twitching, farin);

		scn.SkipToMovementDecision();
		assertAttachedTo(twitching, farin);

		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// "Bearer" is the one who bears it at the end of the turn: Farin won nothing.
		assertInDiscard(twitching);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AWinOnAnEarlierTurnDoesNotKeepItAtTheEndOfALaterTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var twitching = scn.GetFreepsCard("twitching");
		var warrior = scn.GetFreepsCard("warrior");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(warrior);
		scn.AttachCardsTo(warrior, twitching);
		assertAttachedTo(twitching, warrior);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior, savage);
		scn.FreepsResolveSkirmish(warrior);
		scn.PassSkirmishActions();
		// The Warrior (6 + 2) beats Uruk Savage (5); no other [dwarven] companion, so nothing is offered.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// He won this turn: it stays at the turn's end.
		scn.SkipToOtherPlayersTurn();
		assertAttachedTo(twitching, warrior);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The other player's turn: a Free Peoples card of his opponent is not active then, and it stays.
		scn.SkipToOtherPlayersTurn();
		assertAttachedTo(twitching, warrior);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The next turn of its owner has no skirmish: the win of two turns ago does not count.
		scn.SkipToOtherPlayersTurn();
		assertInDiscard(twitching);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
