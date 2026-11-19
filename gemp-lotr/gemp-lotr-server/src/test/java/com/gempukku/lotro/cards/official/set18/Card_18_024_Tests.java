package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ourtime", "18_24");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf who exerts; a strength bonus of his own
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a strength bonus at 3 Ents
					// Scintillating Bird: another [gandalf] Spell condition, cost 1, nothing on its play; its fellowship
					// ability is never used.
					put("bird", "17_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetManeuverPhaseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ourtime", "18_24");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf who exerted to play it
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OurTimeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Our Time
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Spell</b>. To play, exert Gandalf.<br>When you play this condition, remove (3).<br>At the start of the maneuver phase, add (4) and remove this condition from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ourtime");

		assertEquals("Our Time", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayGandalfExertsAndThePlayRemovesThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ourTime = scn.GetFreepsCard("ourtime");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(ourTime);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(ourTime));

		// Gandalf pays, with nothing asked: the Ent Avenger is a [gandalf] companion but no Gandalf.
		scn.FreepsPlayCard(ourTime);
		assertInZone(Zone.SUPPORT, ourTime);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		// 5 - 3; the condition itself costs 0.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfIsExhaustedThoughAnotherGandalfCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ourTime = scn.GetFreepsCard("ourtime");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(ourTime);
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ourTime));
	}

	@Test
	public void ThePlayRemovesWhatTwilightThereIsAndAnotherSpellPlayedAfterItRemovesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ourTime = scn.GetFreepsCard("ourtime");
		var bird = scn.GetFreepsCard("bird");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(ourTime, bird);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetTwilight());

		// Only 2 to remove: both go (CR 4.2 "effect": as much as can be done).
		scn.FreepsPlayCard(ourTime);
		assertInZone(Zone.SUPPORT, ourTime);
		assertEquals(0, scn.GetTwilight());

		// Another [gandalf] Spell condition played after it: its cost of 1 is added and nothing is removed.
		scn.FreepsPlayCard(bird);
		assertInZone(Zone.SUPPORT, bird);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseAddsFourTwilightAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetManeuverPhaseScenario();

		var ourTime = scn.GetFreepsCard("ourtime");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		// Put there, so that the (3) it removes when it is played is no part of the readings.
		scn.MoveCardsToSupportArea(ourTime);
		assertInZone(Zone.SUPPORT, ourTime);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		// The last decision before the maneuver phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, ourTime);
		int twilight = scn.GetTwilight();

		scn.ShadowPass();
		// Nobody was asked anything: the first decision of the maneuver phase finds it done.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(twilight + 4, scn.GetTwilight());
		assertInZone(Zone.REMOVED, ourTime);
		assertEquals(0, scn.GetFreepsDiscardCount());
	}
}
