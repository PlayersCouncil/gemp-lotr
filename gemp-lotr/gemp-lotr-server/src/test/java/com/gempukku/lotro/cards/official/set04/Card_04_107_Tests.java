package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_107");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEntsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("windows", "4_107");
					// Treebeard, Earthborn: Ent ally, vitality 4; a response and a fellowship ability about
					// unbound Hobbits stacked on him
					put("treebeard", "4_103");
					put("avenger", "6_27");     // Ent Avenger: Ent companion, vitality 4; stronger only beside 3 Ents
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindowsinaStoneWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Windows in a Stone Wall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert an Ent. Plays on that Ent.<br><b>Response:</b> If an [isengard] minion is killed, discard this condition to reveal the top 10 cards of an opponent's draw deck. Discard 1 Shadow card and 1 Free Peoples card revealed. Your opponent reshuffles that deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Windows in a Stone Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnTheEntExertedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEntsScenario();

		var windows = scn.GetFreepsCard("windows");
		var treebeard = scn.GetFreepsCard("treebeard");
		var avenger = scn.GetFreepsCard("avenger");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(avenger, gandalf);
		scn.MoveCardsToHand(windows);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(windows));

		scn.FreepsPlayCard(windows);
		// The two Ents, an ally and a companion, and neither companion who is no Ent.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(treebeard, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));

		scn.FreepsChooseCard(treebeard);
		// The Ent who exerted is the one who bears it; nothing more is asked.
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertAttachedTo(windows, treebeard);
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertNotAttachedTo(windows, avenger);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyEntIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEntsScenario();

		var windows = scn.GetFreepsCard("windows");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(treebeard);
		// Gandalf and Frodo could exert: it is an Ent that is wanting, not anybody.
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(windows);

		scn.StartGame();
		scn.AddWoundsToChar(treebeard, 3);
		scn.FreepsRefreshDecision();

		// Vitality 4 with 3 wounds: exhausted.
		assertEquals(1, scn.GetVitality(treebeard));
		assertInZone(Zone.HAND, windows);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(windows));

		// One wound fewer and he can pay.
		scn.RemoveWoundsFromChar(treebeard, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetVitality(treebeard));
		assertTrue(scn.FreepsPlayAvailable(windows));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
