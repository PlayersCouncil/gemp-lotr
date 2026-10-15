package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_280_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lieutenant", "1_280");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("foe", "1_247");        // Enheartened Foe: [sauron] Response event, "If a [sauron] Orc wins a skirmish, make that Orc fierce until the regroup phase"

					// Five cards of five titles for the Free Peoples hand, none of them played.
					put("guard", "1_7");        // Dwarf Guard
					put("gimli", "1_13");       // Gimli, Son of Glóin
					put("boromir", "1_96");     // Boromir, Lord of Gondor
					put("merry", "1_303");      // Merry, From O'er the Brandywine
					put("sam", "1_311");        // Sam, Son of Hamfast
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerLieutenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Tower Lieutenant
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each time this minion wins a skirmish, the Free Peoples player discards 2 cards at random from hand.
		*/

		var scn = GetScenario();

		var lieutenant = scn.GetFreepsCard("lieutenant");

		assertEquals("Tower Lieutenant", lieutenant.getBlueprint().getTitle());
		assertNull(lieutenant.getBlueprint().getSubtitle());
		assertTrue(lieutenant.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lieutenant.getBlueprint().getSide());
		assertEquals(Culture.SAURON, lieutenant.getBlueprint().getCulture());
		assertEquals(CardType.MINION, lieutenant.getBlueprint().getCardType());
		assertEquals(Race.ORC, lieutenant.getBlueprint().getRace());
		assertEquals(4, lieutenant.getBlueprint().getTwilightCost());
		assertEquals(10, lieutenant.getBlueprint().getStrength());
		assertEquals(3, lieutenant.getBlueprint().getVitality());
		assertEquals(6, lieutenant.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishTheFreePeoplesPlayerDiscardsTwoCardsAtRandomFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard, gimli, boromir, merry);

		var lieutenant = scn.GetShadowCard("lieutenant");
		var shadowGuard = scn.GetShadowCard("guard");
		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCardsToHand(shadowGuard, shadowGimli);

		scn.StartGame();
		assertInZone(Zone.HAND, guard, gimli, boromir, merry, shadowGuard, shadowGimli);
		scn.MoveMinionsToTable(lieutenant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lieutenant);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		int discard = scn.GetFreepsDiscardCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetShadowHandCount());
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Lieutenant, 10, beats Aragorn, 8: two of the four cards go, and nobody is asked which.
		scn.PassCurrentPhaseActions();
		// The wound for losing and the Lieutenant's trigger come at the same moment.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		// The two came out of the hand: none of the four is anywhere but the hand or the discard pile.
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertNotInZone(Zone.DECK, guard, gimli, boromir, merry);
		// Not the Shadow player's hand.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, shadowGuard, shadowGimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ItsOwnLossDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard, gimli, boromir, merry);

		var lieutenant = scn.GetShadowCard("lieutenant");

		scn.StartGame();
		assertInZone(Zone.HAND, guard, gimli, boromir, merry);
		scn.MoveMinionsToTable(lieutenant);
		// Aragorn at 12 beats the Lieutenant, 10, without overwhelming him: one wound of his three.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lieutenant);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetWoundsOn(lieutenant));

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertInZone(Zone.SHADOW_CHARACTERS, lieutenant);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, guard, gimli, boromir, merry);
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsAnsweredAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard, gimli, boromir, merry, sam);

		var lieutenant = scn.GetShadowCard("lieutenant");
		var foe = scn.GetShadowCard("foe");
		scn.MoveCardsToHand(foe);

		scn.StartGame();
		assertInZone(Zone.HAND, guard, gimli, boromir, merry, sam, foe);
		scn.MoveMinionsToTable(lieutenant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lieutenant);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetFreepsHandCount());
		int discard = scn.GetFreepsDiscardCount();

		// The first win: two of five go.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		// Enheartened Foe makes him fierce, for a second skirmish in the turn.
		assertTrue(scn.ShadowPlayAvailable(foe));
		scn.ShadowPlayCard(foe);
		assertTrue(scn.HasKeyword(lieutenant, Keyword.FIERCE));

		// The second win, in the fierce round: two of the three left go.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.InFierceRound());
		scn.FreepsAssignToMinions(aragorn, lieutenant);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discard + 4, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
