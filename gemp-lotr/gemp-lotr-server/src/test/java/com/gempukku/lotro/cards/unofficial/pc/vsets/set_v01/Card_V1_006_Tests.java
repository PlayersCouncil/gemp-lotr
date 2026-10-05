package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_006_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bold", "101_6");

					// Arwen, Daughter of Elrond: Elf companion, vitality 3.  Her text only matters against a Nazgul.
					put("arwen", "1_30");
					// Celeborn, Lord of Lorien: Elf ally, vitality 3.  His fellowship ability is never used here.
					put("celeborn", "1_34");
					put("lorienElf", "1_53");   // Lorien Elf: Elf companion, vitality 2, no text in play

					// Gimli, Son of Gloin: Dwarf companion.  His skirmish ability is never used here.
					put("gimli", "1_13");
					// Grimir, Dwarven Elder: Dwarf ally.  His fellowship ability is never used here.
					put("grimir", "1_17");

					// Aragorn, Ranger of the North: a Man.  His maneuver ability, which would make him defender +1,
					// is never used here.
					put("aragorn", "1_89");

					put("troop", "1_177");      // Goblin Patrol Troop: no text.  On the table so that there is combat.
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SoBoldAndSoCourteousStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: So Bold and So Courteous
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves, you may exert an Elf to make a Dwarf defender +1 until the
		 * regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bold");

		assertEquals("So Bold and So Courteous", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForFree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");
		scn.MoveCardsToHand(bold);

		scn.StartGame();

		assertInHand(bold);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		// Nothing has to be spotted: only Frodo is in play.
		assertTrue(scn.FreepsPlayAvailable(bold));

		scn.FreepsPlayCard(bold);
		// Twilight cost 0: the pool is as it was.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, bold);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingOffersToExertAnyElfWhoCanExertToMakeAnyDwarfDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var lorienElf = scn.GetFreepsCard("lorienElf");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Celeborn and Grimir are allies, so they sit in the support area.
		scn.MoveCardsToSupportArea(bold, celeborn, grimir);
		scn.MoveCompanionsToTable(arwen, lorienElf, gimli, aragorn);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(lorienElf, 1);

		scn.StartGame();

		// Nothing is offered until the fellowship moves.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(grimir, Keyword.DEFENDER));

		// Passing ends the fellowship phase, and the fellowship moves to site 2.
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bold));
		scn.FreepsAcceptOptionalTrigger();

		// The cost: any Elf who can exert, companion or ally, and nobody else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienElf, gimli, grimir, aragorn, frodo));
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(lorienElf));

		// The effect: any Dwarf, companion or ally, and nobody else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, celeborn, lorienElf, aragorn, frodo));
		scn.FreepsChooseCard(grimir);

		assertTrue(scn.HasKeyword(grimir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningExertsNoElfAndGivesNoDwarfDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(bold);
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bold));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DefenderLastsUntilTheRegroupPhaseAndASecondMoveOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(bold);
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();

		// A minion, so that the turn has an assignment phase.  Nobody is ever assigned to it.
		scn.MoveMinionsToTable(troop);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.FreepsPass();

		// One Elf and one Dwarf, so neither is asked for.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bold));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With no skirmish to fight, the assignment phase is the last one before regroup.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		// The fellowship moves a second time this turn.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bold));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Given during a regroup phase, it lasts until the next one.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");
		var lorienElf = scn.GetFreepsCard("lorienElf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(bold);
		scn.MoveCompanionsToTable(lorienElf, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(lorienElf));

		scn.FreepsPass();

		// Vitality 2: the Lorien Elf can exert once.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bold));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(lorienElf));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minions, so the turn runs straight to the regroup phase.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		scn.FreepsChooseToMove();

		// The fellowship has moved again, and the only Elf cannot exert: nothing is offered.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(lorienElf));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedWhenItsOwnersFellowshipMovesAndNotWhenTheOpponentsDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(bold);
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();

		scn.FreepsPass();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bold));
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToOtherPlayersTurn();

		// The other player's turn.  The condition, the Elf and the Dwarf are all still on the table.
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInZone(Zone.SUPPORT, bold);
		assertInPlay(arwen, gimli);
		assertEquals(0, scn.GetWoundsOn(arwen));

		// The other player's fellowship moves: this is not "the fellowship" to a card that is not active.
		scn.ShadowPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));
		// The owner of the condition is the Shadow player this turn.
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
