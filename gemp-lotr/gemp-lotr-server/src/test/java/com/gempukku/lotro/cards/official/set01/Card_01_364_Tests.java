package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_364_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_364");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, Gandalf signet, cost 4; his ability is not used
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, Gandalf signet, cost 2; a skirmish ability only
					put("pippin", "1_307");     // Pippin, Hobbit of Some Intelligence: Gandalf signet; left in the discard pile, never played
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man like Aragorn, but the Frodo signet; a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion with no signet at all; "To play, spot a Dwarf"
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, so that the Guard can be played; his fellowship ability is not used
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gandalf, The Grey Wizard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Fellowship:</b> Exert Gandalf to play a companion who has the Gandalf signet. The twilight cost of that companion is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("The Grey Wizard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipAbilityExertsGandalfToPlayACompanionWithTheGandalfSignetForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(aragorn, gimli, boromir, guard);
		scn.MoveCardsToDiscard(pippin);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetTwilight());
		assertInHand(aragorn, gimli, boromir, guard);
		assertInDiscard(pippin);
		// The Guard can be played from hand, as the others can: only its missing signet keeps it from the ability.
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsUseCardAction(gandalf);

		// Companions with the Gandalf signet in hand, of two cultures. Not one of another signet or none, nor one
		// with the signet that is not in hand.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, guard, pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// 4 for Aragorn - 2
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Only that companion's cost is changed: Gimli played from hand afterwards pays his 2.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted, so he cannot exert.
		scn.AddWoundsToChar(gandalf, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInHand(aragorn);
		assertFalse(scn.FreepsActionAvailable(gandalf));
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoCompanionOfTheGandalfSignetInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(boromir, guard);
		scn.MoveCardsToDiscard(pippin);

		scn.StartGame();

		// Gandalf could exert, but the play the cost buys has nothing in hand to play.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(boromir, guard);
		assertInDiscard(pippin);
		assertFalse(scn.FreepsActionAvailable(gandalf));
		assertTrue(scn.FreepsPlayAvailable(boromir));
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(aragorn);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(aragorn);
		assertFalse(scn.FreepsActionAvailable(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
