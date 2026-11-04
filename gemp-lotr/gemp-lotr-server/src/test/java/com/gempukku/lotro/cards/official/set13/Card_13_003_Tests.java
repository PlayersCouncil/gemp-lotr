package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "13_3");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability that strengthens only himself
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, vitality 3; strength +2 skirmishing an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; answers a weapon played on him
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its text is about being played
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3: a minion who is no Orc
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, vitality 2: of the Orc culture and no Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAheadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "13_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeepHatredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Deep Hatred
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time the fellowship moves, add (2).<br><b>Skirmish:</b> Exert a Dwarf who is not assigned to a skirmish to wound 2 Orcs (or to wound 1 Orc twice).<br>Discard this from play if the fellowship is ahead on the adventure path.
		*/

		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");

		assertEquals("Deep Hatred", hatred.getBlueprint().getTitle());
		assertNull(hatred.getBlueprint().getSubtitle());
		assertFalse(hatred.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, hatred.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, hatred.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, hatred.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(hatred, Keyword.SUPPORT_AREA));
		assertEquals(0, hatred.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsDiscardedWhenTheFellowshipMovesAheadOfTheOtherPlayerAndStaysWhileLevelOrBehind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAheadScenario();

		// Deep Hatred is the second player's.
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, hatred);

		// The first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship is at site 1 and the first player's marker at site 2: behind.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// It moves to site 2: level with the other player, which is not ahead.
		scn.ShadowPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// It moves again in the regroup phase, to site 3: ahead.
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, hatred);
		scn.ShadowChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertInDiscard(hatred);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void IsDiscardedAsTheFirstPlayerMovesOnFromSite1WhereBothPlayersBegin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAheadScenario();

		var hatred = scn.GetFreepsCard("hatred");
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();

		// Both players' markers are at site 1: level.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// The first player moves to site 2 while the other is still at site 1.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertInDiscard(hatred);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveAddsTwoAndTheMoveAheadOfTheOtherPlayerDiscardsItOnceItsTwilightIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and again in the regroup phase, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 2, behind the other player's marker at site 3.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.MoveCardsToSupportArea(hatred);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, hatred);

		// The move to Rivendell Valley: 0 for the site + 2 companions + 2.  Level with the other player.
		int before = scn.GetTwilight();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(before + 4, scn.GetTwilight());
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// The regroup move to Balin's Tomb: 3 for the site + 2 companions + 2 again.  Ahead of the other
		// player, and discarded once the move's twilight is added.
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, hatred);
		before = scn.GetTwilight();
		scn.FreepsChooseToMove();
		assertEquals(before + 7, scn.GetTwilight());
		assertEquals(4, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and again in the regroup phase, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 2, behind the other player's marker at site 3.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.MoveCardsToSupportArea(hatred);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, hatred);
		scn.FreepsPass();
		assertEquals(3, scn.FreepsGetSiteNumber());

		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, hatred);
		int before = scn.GetTwilight();
		scn.FreepsChooseToStay();
		assertEquals(before, scn.GetTwilight());
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);
		scn.PassReconcile(VirtualTableScenario.P1);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void SkirmishAbilityExertsADwarfWhoIsNotAssignedToWoundTwoOrcsOnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and again in the regroup phase, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 2, behind the other player's marker at site 3.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(troop, soldier, chieftain, runner, savage, halftroll);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, hatred);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{farin, soldier});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetSiteNumber(VirtualTableScenario.P1));
		assertInZone(Zone.SUPPORT, hatred);
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hatred));
		scn.FreepsUseCardAction(hatred);
		// Gimli and Grimir are Dwarves in no skirmish; Farin is assigned, Aragorn assigned and no Dwarf, Frodo
		// unassigned and no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(farin, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Orcs able to take two wounds: either half can be done in full.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Wound 2 Orcs");
		// Orcs of the [moria] and [sauron] cultures, in the skirmish and out of it; not the Uruk-hai, and not the
		// Half-troll of the [orc] culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier, chieftain, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, halftroll));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(chieftain, runner);
		// A wound, not an exertion: the Runner of 1 vitality is killed.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WoundOneOrcTwiceIsAimedAtAnOrcAbleToTakeBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and again in the regroup phase, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 2, behind the other player's marker at site 3.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(troop, soldier, runner, savage, halftroll);
		scn.FreepsRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(gimli));
		// Gimli is the only Dwarf in no skirmish, so he is the one to exert.
		scn.FreepsUseCardAction(hatred);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Wound 1 Orc twice");
		// The half chosen has to be done in full: not the Runner, who has 1 vitality.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, savage, halftroll));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneOrcInPlayItIsWoundedTwiceWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and again in the regroup phase, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 2, behind the other player's marker at site 3.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(soldier, savage, halftroll);
		scn.FreepsRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsUseCardAction(hatred);
		// With one Orc there are not two to wound, so wounding the one twice is all that can be done in full.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, soldier);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheOnlyDwarfInNoSkirmishIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and again in the regroup phase, to site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 2, behind the other player's marker at site 3.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.MoveCardsToSupportArea(hatred);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(troop, soldier);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, hatred);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{farin, soldier});
		scn.FreepsResolveSkirmish(aragorn);

		// Gimli, 2 wounds of 3, cannot exert; Farin could, but is assigned.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hatred));

		// A Dwarf in no skirmish who can exert.
		scn.MoveAlliesToTable(grimir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hatred));
	}

	@Test
	public void PlayedWhileTheFellowshipIsAheadItIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetFreepsCard("hatred");

		scn.StartGame();
		// Turn 1: the first player moves to site 2, and again in the regroup phase, to site 3.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToOtherPlayersTurn();
		// Turn 2: the second player moves from site 1 to site 2, and no further.
		scn.ShadowPass();
		scn.SkipToOtherPlayersTurn();

		// Turn 3: the first player's fellowship is at site 3, ahead of the other player's marker at site 2.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		scn.MoveCardsToHand(hatred);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.HAND, hatred);
		scn.FreepsPlayCard(hatred);
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
