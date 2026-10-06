package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("matriarch", "103_42");

					// Desert Warrior: Southron, a [raider] Man, costs 2. At the start of its own skirmishes it
					// may remove (3) to wound; it is never in a skirmish here.
					put("warrior1", "4_222");
					put("warrior2", "4_222");
					put("warrior3", "4_222");
					put("warrior4", "4_222");
					put("explorer", "4_250");     // Southron Explorer: Southron, a [raider] Man, costs 2; a regroup ability never used here
					put("explorer2", "4_250");
					put("scout", "4_252");        // Southron Scout: Southron, a [raider] Man, ambush (2) and nothing else
					put("infantry", "4_227");     // Easterling Infantry: a [raider] Man who is no Southron; its text needs 3 burdens
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("savage", "4_17");        // Dunlending Savage: a Man who is [dunland]; no text
					put("troop", "1_177");        // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskMatriarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Matriarch
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Artifact
		 * Subtype: Support area
		 * Strength: 6
		 * Vitality: 6
		 * Game Text: Each time a Southron is played, you may stack a [raider] Man from play or hand here.
		 * 	Maneuver: Remove (6) to make this artifact a <b>fierce</b> mounted Southron minion until the end of
		 * 	the turn that is strength +3 and <b>ambush (1)</b> for each Southron stacked on her. She adds 1 to
		 * 	the Shadow archery total for each Southron stacked on her (limit 6).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("matriarch");

		assertEquals("Bladetusk Matriarch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		// The strength and vitality printed on the card are those of the minion she becomes. The artifact
		// itself has neither; they are read where the Maneuver ability is tested.
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForFiveToTheSupportAreaAndEachSouthronPlayedOffersARaiderManFromPlayToStackOnHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var khamul = scn.GetShadowCard("khamul");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(matriarch, infantry, warrior1, explorer);

		scn.StartGame();
		scn.SetTwilight(27);
		scn.FreepsPass();

		// 27 set + 1 companion + 2 for the site
		assertEquals(30, scn.GetTwilight());
		scn.ShadowPlayCard(matriarch);
		// An artifact pays no roaming penalty.
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, matriarch);
		assertFalse(scn.IsType(matriarch, CardType.MINION));

		scn.MoveMinionsToTable(warrior2, khamul, savage);

		// A [raider] Man who is no Southron is played: nothing is offered.
		scn.ShadowPlayCard(infantry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(warrior1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(matriarch));
		scn.ShadowAcceptOptionalTrigger();
		// The other Southron is still in hand, so the game asks which place the Man comes from.
		scn.ShadowChoose("play");

		// [raider] Men in play: the Southron just played, another Southron, the Easterling. Not the [raider]
		// Nazgul, and not the [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior1, warrior2, infantry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(khamul));
		assertFalse(scn.ShadowHasCardChoiceAvailable(savage));
		assertFalse(scn.ShadowHasCardChoiceAvailable(explorer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(infantry);
		assertInZone(Zone.STACKED, infantry);
		assertEquals(1, scn.GetStackedCards(matriarch).size());
		assertTrue(scn.GetStackedCards(matriarch).contains(infantry));
		assertInZone(Zone.SHADOW_CHARACTERS, warrior1, warrior2);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Southron is played: offered again, and this time declined.
		scn.ShadowPlayCard(explorer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(matriarch));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetStackedCards(matriarch).size());
		assertInZone(Zone.SHADOW_CHARACTERS, explorer, warrior1, warrior2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRaiderManMayComeFromHandInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var warrior1 = scn.GetShadowCard("warrior1");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		var infantry = scn.GetShadowCard("infantry");
		var khamul = scn.GetShadowCard("khamul");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCardsToHand(warrior1, scout, infantry, khamul, savage);
		// A [raider] Man in the discard pile: neither in play nor in hand
		scn.MoveCardsToDiscard(explorer);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetStackedCards(matriarch).size());
		scn.ShadowPlayCard(warrior1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(matriarch));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("hand");

		// [raider] Men in hand: a Southron and an Easterling. Not the [raider] Nazgul or the [dunland] Man in
		// hand, not the Southron in play, not the one in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, infantry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(khamul));
		assertFalse(scn.ShadowHasCardChoiceAvailable(savage));
		assertFalse(scn.ShadowHasCardChoiceAvailable(warrior1));
		assertFalse(scn.ShadowHasCardChoiceAvailable(explorer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.GetShadowHandCount());

		scn.ShadowChooseCard(scout);
		assertInZone(Zone.STACKED, scout);
		assertEquals(1, scn.GetStackedCards(matriarch).size());
		assertTrue(scn.GetStackedCards(matriarch).contains(scout));
		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(infantry);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior1);
		assertInDiscard(explorer);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesSixToBecomeAFierceMountedSouthronMinionStrongerForEachSouthronStackedOnHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var warrior1 = scn.GetShadowCard("warrior1");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(matriarch);
		// 2 Southrons, and an Easterling: a [raider] Man her own text can put there
		scn.StackCardsOn(matriarch, warrior1, explorer, infantry);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, matriarch);
		assertFalse(scn.IsType(matriarch, CardType.MINION));
		assertFalse(scn.HasKeyword(matriarch, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(matriarch, Keyword.MOUNTED));
		assertFalse(scn.HasKeyword(matriarch, Keyword.SOUTHRON));
		assertEquals(0, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowActionAvailable(matriarch));

		scn.ShadowUseCardAction(matriarch);
		assertEquals(0, scn.GetTwilight());

		assertInZone(Zone.SHADOW_CHARACTERS, matriarch);
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		assertTrue(scn.HasKeyword(matriarch, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(matriarch, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(matriarch, Keyword.SOUTHRON));
		// 6 + 3 for each of the 2 Southrons; nothing for the Easterling
		assertEquals(12, scn.GetStrength(matriarch));
		assertEquals(6, scn.GetVitality(matriarch));
		assertEquals(2, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(3, scn.GetStackedCards(matriarch).size());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithFiveInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(matriarch));
	}

	@Test
	public void AddsOneToTheShadowArcheryTotalForEachSouthronStackedOnHerUpToSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var warrior3 = scn.GetShadowCard("warrior3");
		var warrior4 = scn.GetShadowCard("warrior4");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var scout = scn.GetShadowCard("scout");
		var infantry = scn.GetShadowCard("infantry");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(matriarch);
		// 7 Southrons and an Easterling
		scn.StackCardsOn(matriarch, warrior1, warrior2, warrior3, warrior4, explorer, explorer2, scout, infantry);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(6);
		scn.FreepsPass();

		// While she is an artifact she adds nothing, with a minion in play and Southrons stacked on her.
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowUseCardAction(matriarch);
		// 7 Southrons, limit 6
		assertEquals(6, scn.GetShadowArcheryTotal());
		// Strength and ambush have no limit: 6 + 3 for each of 7
		assertEquals(27, scn.GetStrength(matriarch));
		assertEquals(7, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(6, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNothingStackedSheIsStrengthSixStaysAMinionThroughASecondMoveAndIsDiscardedWhenTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(6);
		scn.FreepsPass();

		scn.ShadowUseCardAction(matriarch);
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		assertEquals(6, scn.GetStrength(matriarch));
		assertEquals(6, scn.GetVitality(matriarch));
		assertEquals(0, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(0, scn.GetShadowArcheryTotal());

		// The regroup phase of the first move
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		assertTrue(scn.HasKeyword(matriarch, Keyword.FIERCE));

		// The second move: the same turn, and she is a minion still.
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, matriarch);
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		assertTrue(scn.HasKeyword(matriarch, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(matriarch, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(matriarch, Keyword.SOUTHRON));
		assertEquals(6, scn.GetStrength(matriarch));
		assertEquals(6, scn.GetVitality(matriarch));

		// The turn ends, and every minion is discarded as it does.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		assertInDiscard(matriarch);
		assertInDiscard(troop);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ASouthronStackedOnHerAfterASecondMoveStillAddsToHerStrengthAmbushAndArchery() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var warrior1 = scn.GetShadowCard("warrior1");
		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(matriarch);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(6);
		scn.FreepsPass();

		scn.ShadowUseCardAction(matriarch);
		assertTrue(scn.IsType(matriarch, CardType.MINION));

		scn.SkipToMovementDecision();
		// The reconcile on the way has drawn the Shadow player a hand. A Southron to play is put in it, and
		// a second [raider] Man, so that whatever was drawn both places hold one and the game asks which.
		scn.MoveCardsToHand(warrior1, scout);
		scn.SetTwilight(2);
		scn.FreepsChooseToMove();

		// The Shadow phase of the second move, the same turn
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 2 companions + 0 for the site
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetStackedCards(matriarch).size());
		assertEquals(6, scn.GetStrength(matriarch));
		assertEquals(0, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(0, scn.GetShadowArcheryTotal());

		// 2 to play + 2 roaming
		scn.ShadowPlayCard(warrior1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(matriarch));
		scn.ShadowAcceptOptionalTrigger();
		// The Southron just played is the only [raider] Man in play, so it is the one stacked.
		scn.ShadowChoose("play");

		assertEquals(1, scn.GetStackedCards(matriarch).size());
		assertTrue(scn.GetStackedCards(matriarch).contains(warrior1));
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		// 6 + 3 for the one Southron now stacked on her
		assertEquals(9, scn.GetStrength(matriarch));
		assertEquals(1, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("Q-0077: may the Maneuver ability be used again while the Matriarch is a minion?")
	public void ManeuverActionCanBeUsedAgainWhileSheIsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var matriarch = scn.GetShadowCard("matriarch");
		var warrior1 = scn.GetShadowCard("warrior1");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(matriarch);
		scn.StackCardsOn(matriarch, warrior1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(12);
		scn.FreepsPass();

		scn.ShadowUseCardAction(matriarch);
		assertEquals(6, scn.GetTwilight());
		// 6 + 3 for the one Southron
		assertEquals(9, scn.GetStrength(matriarch));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(matriarch));
		scn.ShadowUseCardAction(matriarch);

		// The cost is paid again, and she is the minion she already was.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsType(matriarch, CardType.MINION));
		assertEquals(9, scn.GetStrength(matriarch));
		assertEquals(6, scn.GetVitality(matriarch));
		assertEquals(1, scn.GetKeywordCount(matriarch, Keyword.AMBUSH));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
