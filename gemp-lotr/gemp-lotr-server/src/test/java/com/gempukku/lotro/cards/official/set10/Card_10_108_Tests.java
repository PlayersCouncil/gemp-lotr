package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "10_108");

					// The [shire] companions beside Frodo, and the cards that nearly are.
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability only
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability only
					put("trollslayer", "103_31"); // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only

					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					// Aragorn or Pippin, Trollslayer is the only [gondor] companion on each board, and is chosen unasked.
					// Each play takes a card from the hand.
					put("valor", "1_103");
					put("valor2", "1_103");
					put("valor3", "1_103");
					// Dwarven Armor: cards to hold in hand and nothing else (no Dwarf to bear one).
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ALightinhisMindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: A Light in his Mind
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you lose initiative, you may spot 2 [shire] companions to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("light");

		assertEquals("A Light in his Mind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachLossOfInitiativeWithTwoShireCompanionsOffersToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var filler4 = scn.GetFreepsCard("filler4");
		// Frodo, a Hobbit and the Ring-bearer, and Smeagol, Ring-bound and no Hobbit: two [shire] companions.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(light);
		scn.MoveCardsToHand(valor, valor2, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"));

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetBurdens());

		// Four cards to three: initiative is lost, and the offer is made. Declined, nothing changes.
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(light));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A card more makes four: initiative is back from the next answer on.
		scn.MoveCardsToHand(filler4);
		scn.ShadowPass();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Lost a second time in the same phase: offered again, and taken it removes one burden of two.
		scn.FreepsPlayCard(valor2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(light));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOneShireCompanionBesideAShireAllyAndAGondorHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var bounder = scn.GetFreepsCard("bounder");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var valor = scn.GetFreepsCard("valor");
		// Frodo is the one [shire] companion; the Bounder is [shire] and no companion, Pippin a Hobbit companion
		// who is not [shire].
		scn.MoveCompanionsToTable(trollslayer);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(light);
		scn.MoveCardsToHand(valor, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(light));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void AHandGoingFromThreeToTwoOrFromFiveToFourOrBackToFourOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var valor3 = scn.GetFreepsCard("valor3");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(light);
		scn.MoveCardsToHand(valor, valor2, valor3);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// Three cards to two: the Shadow has initiative already, so none is lost.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(light));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Three cards more make five: the Shadow loses initiative at the next answer, which is no loss of the
		// Free Peoples player's.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		scn.ShadowPass();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(light));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Five cards to four: initiative is kept.
		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(light));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();
		assertEquals(2, scn.GetBurdens());

		// Four cards to three on the same board: the offer is made.
		scn.FreepsPlayCard(valor3);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(light));
	}

	@Test
	public void NotOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(light);
		scn.MoveCardsToHand(valor, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(light));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetBurdens());
	}
}
