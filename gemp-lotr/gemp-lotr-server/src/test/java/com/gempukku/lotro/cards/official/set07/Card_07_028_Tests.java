package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("between", "7_28");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; stronger only against a Nazgul
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, vitality 3; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, vitality 4; a maneuver ability only

					put("healing", "3_22");     // Master of Healing: an [elven] event, here only a card in hand
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, here only a card in hand

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowBetweenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Shadow Between
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an Elf.
		 * At the start of your regroup phase, you may discard this condition or a card from hand to heal an Elf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("between");

		assertEquals("Shadow Between", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(between, arwen);

		scn.StartGame();

		// A Man and a Hobbit, and no Elf.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(between));

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertTrue(scn.FreepsPlayAvailable(between));

		scn.FreepsPlayCard(between);
		assertInZone(Zone.SUPPORT, between);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithAnElfAllyTheOnlyElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var golradir = scn.GetFreepsCard("golradir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(between);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(between));
		scn.FreepsPlayCard(between);
		assertInZone(Zone.SUPPORT, between);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseACardFromHandIsDiscardedToHealAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var healing = scn.GetFreepsCard("healing");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToSupportArea(between);

		scn.StartGame();
		scn.MoveCardsToHand(healing, sword);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(golradir, 2);
		scn.AddWoundsToChar(aragorn, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(between));
		scn.FreepsAcceptOptionalTrigger();

		// The price is the player's choice: this condition, or a card from hand.
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a card from hand"));
		scn.FreepsChoose("Discard a card from hand");

		// Any card from hand: an [elven] event and a [shire] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(healing, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);
		assertInDiscard(sword);
		assertInHand(healing);

		// A wounded Elf companion or Elf ally; not the wounded Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, golradir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		// One wound healed, from Arwen alone; the condition stays.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, between);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThisConditionMayBeDiscardedInsteadOfACardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToSupportArea(between);

		scn.StartGame();
		scn.MoveCardsToHand(healing);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(golradir, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(between));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("Discard this condition");
		assertInDiscard(between);

		scn.FreepsChooseCard(golradir);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertInHand(healing);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyHandThisConditionIsDiscardedWithNoChoiceOfPrice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToSupportArea(between);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(golradir, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(between));
		scn.FreepsAcceptOptionalTrigger();

		// No card in hand: the one price that can be paid is paid, and the heal is asked straight away.
		assertFalse(scn.FreepsChoiceAvailable("Discard a card from hand"));
		assertInDiscard(between);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, golradir));
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheElfTheHandAndTheConditionAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var arwen = scn.GetFreepsCard("arwen");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(between);

		scn.StartGame();
		scn.MoveCardsToHand(healing);
		scn.AddWoundsToChar(arwen, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(between));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInHand(healing);
		assertInZone(Zone.SUPPORT, between);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var between = scn.GetFreepsCard("between");
		var arwen = scn.GetFreepsCard("arwen");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(between);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToHand(healing);
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveMinionsToTable(troop);

		// The skipper leaves a trigger at the start of the phase it stops in pending.
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(between));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SUPPORT, between);
	}
}
