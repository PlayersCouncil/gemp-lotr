package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("respite", "7_44");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] companion, vitality 4, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4; +2 only beside 3 Ents
					put("avenger", "6_27");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, here only a card in hand
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, here only a card in hand
					put("healing", "3_22");     // Master of Healing: an [elven] event, here only a card in hand

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MomentOfRespiteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Moment of Respite
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of your regroup phase, you may discard 2 cards from hand to heal Gandalf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("respite");

		assertEquals("Moment of Respite", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseTwoCardsFromHandAreDiscardedToHealGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var respite = scn.GetFreepsCard("respite");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(respite);

		scn.StartGame();
		scn.MoveCardsToHand(aragorn, sword, healing);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(avenger, 2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInHand(aragorn);
		assertInHand(sword);
		assertInHand(healing);
		assertInZone(Zone.SUPPORT, respite);
		assertEquals(2, scn.GetWoundsOn(avenger));

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(respite));
		scn.FreepsAcceptOptionalTrigger();

		// Any 2 cards from hand: a companion, a possession and an event are each offered, and exactly 2 are taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sword, healing));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sword, healing);
		assertInDiscard(sword);
		assertInDiscard(healing);
		assertInHand(aragorn);
		assertEquals(1, scn.GetFreepsHandCount());

		// One wound off Gandalf; the [gandalf] companion who is no Gandalf keeps his.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(avenger));
		assertInZone(Zone.SUPPORT, respite);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var respite = scn.GetFreepsCard("respite");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(respite);

		scn.StartGame();
		scn.MoveCardsToHand(sword);
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gandalf));

		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(respite));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInHand(sword);
	}

	@Test
	public void DecliningLeavesGandalfTheHandAndTheConditionAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var respite = scn.GetFreepsCard("respite");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(respite);

		scn.StartGame();
		scn.MoveCardsToHand(sword, healing);
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInHand(sword);
		assertInHand(healing);
		assertInZone(Zone.SUPPORT, respite);

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(respite));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInHand(sword);
		assertInHand(healing);
		assertInZone(Zone.SUPPORT, respite);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var respite = scn.GetFreepsCard("respite");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		var healing = scn.GetFreepsCard("healing");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(respite);

		scn.StartGame();
		scn.MoveCardsToHand(sword, healing);
		scn.AddWoundsToChar(gandalf, 2);
		scn.MoveMinionsToTable(troop);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// The skipper leaves a trigger at the start of the phase it stops in pending.
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(respite));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());
	}
}
