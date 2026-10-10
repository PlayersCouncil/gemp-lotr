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
					put("card", "18_24");
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
		 * Subtype: Support area
		 * Game Text: <b>Spell</b>. To play, exert Gandalf.<br>When you play this condition, remove (3).<br>At the start of the maneuver phase, add (4) and remove this condition from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

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
	public void AtTheStartOfTheManeuverPhaseAddsFourTwilightAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetManeuverPhaseScenario();

		var ourTime = scn.GetFreepsCard("ourtime");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		// Put there, so that the (3) it removes when it is played is no part of the readings.
		scn.MoveCardsToSupportArea(ourTime);

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
