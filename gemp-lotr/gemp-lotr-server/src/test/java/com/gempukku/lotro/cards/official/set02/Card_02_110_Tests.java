package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "2_110");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, cost 4; a standing strength bonus only
					put("greywizard", "1_364"); // Gandalf, The Grey Wizard: cost 4; laid in the discard pile only
					put("pilgrim", "2_122");    // Gandalf, The Grey Pilgrim: cost 4; held in hand only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, cost 4; a maneuver ability never used
					// Aragorn, Defender of Rohan: the [rohan] Aragorn, cost 4; valiant, and hunter only while the
					// Ring-bearer is assigned.
					put("rohanaragorn", "17_93");
					put("entavenger", "6_27");  // Ent Avenger: a [gandalf] companion who is no Gandalf, cost 4; a bonus beside 3 Ents only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion who is no Aragorn, cost 3; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Pippin, Mr. Took
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Fellowship:</b> Play Gandalf or Aragorn; his twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Mr. Took", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipActionPlaysGandalfFromHandAtTwilightCostTwoLessAndAragornAfterHimPaysInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var greywizard = scn.GetFreepsCard("greywizard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var entavenger = scn.GetFreepsCard("entavenger");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(gandalf, aragorn, rohanaragorn, entavenger, boromir);
		scn.MoveCardsToDiscard(greywizard);

		scn.StartGame();

		assertInHand(gandalf, aragorn, rohanaragorn, entavenger, boromir);
		assertInDiscard(greywizard);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);

		// Gandalf and both Aragorns, of [gondor] and of [rohan], in hand; not a [gandalf] companion who is no Gandalf,
		// a [gondor] companion who is no Aragorn, or a Gandalf in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn, rohanaragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(entavenger, boromir, greywizard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		// 4 - 2
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// "His" twilight cost: Aragorn played from hand afterwards pays his own in full.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// 2 + 4
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysTheRohanAragornAtTwilightCostTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(gandalf, rohanaragorn);

		scn.StartGame();

		assertInHand(gandalf, rohanaragorn);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(pippin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, rohanaragorn));
		scn.FreepsChooseCard(rohanaragorn);

		assertInZone(Zone.FREE_CHARACTERS, rohanaragorn);
		assertInHand(gandalf);
		// 4 - 2
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGandalfOrAragornInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var greywizard = scn.GetFreepsCard("greywizard");
		var pilgrim = scn.GetFreepsCard("pilgrim");
		var aragorn = scn.GetFreepsCard("aragorn");
		var entavenger = scn.GetFreepsCard("entavenger");
		var boromir = scn.GetFreepsCard("boromir");
		// A Gandalf is in play already, so the Gandalf in hand cannot be played; the other Gandalf is in the
		// discard pile and Aragorn in the draw deck.
		scn.MoveCompanionsToTable(pippin, gandalf);
		scn.MoveCardsToHand(pilgrim, entavenger, boromir);
		scn.MoveCardsToDiscard(greywizard);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInHand(pilgrim, entavenger, boromir);
		assertInDiscard(greywizard);
		assertInZone(Zone.DECK, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The play is the action's whole cost (Q-0062): with nothing it could play, it is not offered.
		assertFalse(scn.FreepsPlayAvailable(pilgrim));
		assertTrue(scn.FreepsPlayAvailable(entavenger));
		assertFalse(scn.FreepsActionAvailable(pippin));
	}
}
