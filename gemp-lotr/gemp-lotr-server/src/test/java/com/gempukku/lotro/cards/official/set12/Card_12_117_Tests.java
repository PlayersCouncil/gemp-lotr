package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_12_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leofric", "12_117");
					put("boromir", "1_97");      // Boromir, Son of Denethor: [gondor] Man, strength 7
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("sword", "4_297");       // Work for the Sword: [rohan] Skirmish event, a [rohan] Man strength +2
					put("villagers", "4_273");   // Fight for the Villagers: [rohan] Skirmish event, a [rohan] Man strength +2
					put("eorlingas", "4_275");   // Forth Eorlingas!: [rohan] Maneuver event
					put("unyielding", "10_74");  // Unyielding: [rohan] Response event
					put("axestrike", "1_3");     // Axe Strike: [dwarven] Skirmish event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeofricStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Léofric, Defender of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> If Léofric is in a skirmish, exert him to play a [rohan] skirmish event from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leofric");

		assertEquals("Léofric", card.getBlueprint().getTitle());
		assertEquals("Defender of the Mark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void InHisSkirmishExertsToPlayARohanSkirmishEventFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leofric = scn.GetFreepsCard("leofric");
		var sword = scn.GetFreepsCard("sword");
		var villagers = scn.GetFreepsCard("villagers");
		var eorlingas = scn.GetFreepsCard("eorlingas");
		var unyielding = scn.GetFreepsCard("unyielding");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(leofric);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		assertInZone(Zone.DECK, sword, villagers, eorlingas, unyielding, axestrike);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(leofric, runner1);
		scn.FreepsResolveSkirmish(leofric);

		assertEquals(0, scn.GetWoundsOn(leofric));
		assertEquals(6, scn.GetStrength(leofric));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(leofric));
		scn.FreepsUseCardAction(leofric);
		assertEquals(1, scn.GetWoundsOn(leofric));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, villagers));
		// Another phase's [rohan] event, a [rohan] Response event and another culture's Skirmish event.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eorlingas, unyielding, axestrike));
		scn.FreepsChooseCardBPFromSelection(sword);

		// Léofric is the only [rohan] Man: Work for the Sword goes to him unasked.
		assertEquals(8, scn.GetStrength(leofric));
		assertInZone(Zone.DISCARD, sword);
		assertInZone(Zone.DECK, villagers, eorlingas, unyielding, axestrike);
		// The deck is shuffled as the event leaves it, and again after the search (CR 4.2 "draw deck").
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanSkirmishEventInTheDeckHeExertsAndPlaysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leofric = scn.GetFreepsCard("leofric");
		var eorlingas = scn.GetFreepsCard("eorlingas");
		var unyielding = scn.GetFreepsCard("unyielding");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(leofric);
		// Every [rohan] Skirmish event out of the deck.
		scn.MoveCardsToFreepsDiscard("sword", "villagers");

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		assertInZone(Zone.DECK, eorlingas, unyielding, axestrike);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(leofric, runner1);
		scn.FreepsResolveSkirmish(leofric);

		assertEquals(0, scn.GetWoundsOn(leofric));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(leofric));
		scn.FreepsUseCardAction(leofric);
		assertEquals(1, scn.GetWoundsOn(leofric));

		assertTrue(scn.FreepsHasCardChoiceAvailable(eorlingas, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(6, scn.GetStrength(leofric));
		assertInZone(Zone.DECK, eorlingas, unyielding, axestrike);
		// The search alone shuffles once.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileHeWaitsForHisSkirmishDuringAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leofric = scn.GetFreepsCard("leofric");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(leofric, boromir);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		assertInZone(Zone.DECK, sword);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{leofric, runner1}, new PhysicalCardImpl[]{boromir, runner2});
		scn.FreepsResolveSkirmish(boromir);

		// Assigned, but his skirmish has not begun.
		assertTrue(scn.IsCharAssignedAgainst(leofric, runner1));
		assertEquals(0, scn.GetWoundsOn(leofric));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leofric));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(leofric);
		assertTrue(scn.FreepsActionAvailable(leofric));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedInHisSkirmishWhileExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leofric = scn.GetFreepsCard("leofric");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(leofric);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		// Vitality 3 with 2 wounds.
		scn.AddWoundsToChar(leofric, 2);
		assertEquals(2, scn.GetWoundsOn(leofric));
		assertInZone(Zone.DECK, sword);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(leofric, runner1);
		scn.FreepsResolveSkirmish(leofric);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leofric));
	}
}
