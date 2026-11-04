package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isildur", "13_71");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion, resistance 6; a maneuver ability used only on purpose
					put("rider", "4_286");       // Rider of Rohan: [rohan] companion of cost 2, played from hand; his skirmish ability wants a mount
					put("troop1", "1_177");      // Goblin Patrol Troop: no text; cards to hold in a hand
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("saruman", "12_144");    // Saruman, Agent of the Dark Lord: "Each time a companion is killed, you may heal Saruman"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetReconcileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isildur", "13_71");
					// Goblin Patrol Troop, a card with no text, ten times over: six for a hand that keeps
					// Isildur's resistance up, four for a draw deck deep enough to reconcile from and draw again
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
					put("card5", "1_177");
					put("card6", "1_177");
					put("card7", "1_177");
					put("card8", "1_177");
					put("card9", "1_177");
					put("card10", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCarefulStudyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isildur", "13_71");
					put("arwen", "1_30");        // Arwen, Daughter of Elrond: the Elf Careful Study spots; strength +3 only against a Nazgûl
					put("study", "7_19");        // Careful Study: Regroup event, "Spot an Elf and discard 3 cards from hand to reconcile your hand"
					// Goblin Patrol Troop, a card with no text: five for the hand, eight for the draw deck
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
					put("card5", "1_177");
					put("card6", "1_177");
					put("card7", "1_177");
					put("card8", "1_177");
					put("card9", "1_177");
					put("card10", "1_177");
					put("card11", "1_177");
					put("card12", "1_177");
					put("card13", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Isildur, Heir of Elendil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 1
		 * Game Text: <b>Knight</b>.<br>Isildur is resistance +1 for each card in your hand.<br>At the end of any phase, if Isildur has resistance 0, place him in your dead pile.<br>Each time you reconcile, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("isildur");

		assertEquals("Isildur", card.getBlueprint().getTitle());
		assertEquals("Heir of Elendil", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void ResistanceIsPlusOneForEachCardInHisOwnersHandAndNotTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(isildur, aragorn);

		scn.StartGame();
		// An empty hand: his printed 1 alone.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetResistance(isildur));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), rider,
				scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("troop3"));
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		// 1 + 3 for his owner's three cards; the Shadow player's three count for nothing.
		assertEquals(4, scn.GetResistance(isildur));
		// Another companion's resistance goes by no hand.
		assertEquals(6, scn.GetResistance(aragorn));

		// A card played from hand takes one off.
		scn.FreepsPlayCard(rider);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetResistance(isildur));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtResistance0HeStaysUntilThePhaseEndsAndIsThenPlacedInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCompanionsToTable(isildur);

		scn.StartGame();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetResistance(isildur));
		// 1 printed + 0 cards in hand - 1 burden.
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetResistance(isildur));

		// Nothing happens while the phase goes on.
		assertInZone(Zone.FREE_CHARACTERS, isildur);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship phase ends: he goes to the dead pile, not the discard pile.
		scn.FreepsPass();
		assertInZone(Zone.DEAD, isildur);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtResistance1HeStaysAndAtResistance0AtTheEndOfTheShadowPhaseHeIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isildur = scn.GetFreepsCard("isildur");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(isildur);

		scn.StartGame();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetResistance(isildur));

		// Resistance 1 as the fellowship phase ends: he stays.
		scn.FreepsPass();
		assertInZone(Zone.FREE_CHARACTERS, isildur);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Saruman, wounded, answers a companion killed.
		scn.MoveMinionsToTable(saruman);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetResistance(isildur));

		// At the end of the Shadow phase he is placed in the dead pile, and that is a kill (CR 4.2 "dead pile").
		scn.ShadowPass();
		assertInZone(Zone.DEAD, isildur);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ReconcilingOffersIsildursOwnerACardAndTakingItDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReconcileScenario();

		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"));

		scn.StartGame();
		scn.MoveCompanionsToTable(isildur);

		// No minion is played: the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();

		// The Shadow player has reconciled, and that is not Isildur's owner reconciling: nothing is offered.
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(isildur));
		scn.FreepsChooseToStay();

		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		scn.FreepsDeclineReconciliation();

		// He has reconciled, a hand of 6 made up to 8, and now Isildur makes his offer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isildur));
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(9, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInZone(Zone.FREE_CHARACTERS, isildur);

		// The turn is over: the other player's begins.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithAnEmptyDrawDeckTheDrawIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReconcileScenario();

		var isildur = scn.GetFreepsCard("isildur");
		// A hand of eight, and the two cards left over out of the draw deck.
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"),
				scn.GetFreepsCard("card7"), scn.GetFreepsCard("card8"));
		scn.MoveCardsToDiscard(scn.GetFreepsCard("card9"), scn.GetFreepsCard("card10"));

		scn.StartGame();
		scn.MoveCompanionsToTable(isildur);

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		scn.FreepsChooseToStay();

		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.FreepsDeclineReconciliation();

		// An optional draw is offered though there is no card to draw (Ketura's ruling on Q-0029).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isildur));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.FREE_CHARACTERS, isildur);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void EachReconcileOffersTheDrawAndDecliningLeavesTheHandAsReconciled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCarefulStudyScenario();

		var isildur = scn.GetFreepsCard("isildur");
		var arwen = scn.GetFreepsCard("arwen");
		var study = scn.GetFreepsCard("study");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		scn.MoveCompanionsToTable(isildur, arwen);
		scn.MoveCardsToHand(study, card1, card2, card3, scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetFreepsDeckCount());

		// Careful Study: three cards discarded, and a hand of 2 made up to 8.
		scn.FreepsPlayCard(study);
		scn.FreepsChooseCards(card1, card2, card3);
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());

		// The first reconcile of the turn: the offer, declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isildur));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// He stays, and reconciles again with eight in hand: the offer again, taken.
		scn.PassRegroupActions();
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isildur));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(9, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
