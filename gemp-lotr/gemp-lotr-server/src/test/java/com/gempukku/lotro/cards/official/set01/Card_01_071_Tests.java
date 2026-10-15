package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("secret", "1_71");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; stronger only beside 3 Ents
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability never used here
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, cost 1; a maneuver ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability never used here
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally, cost 2; an archery ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DurinsSecretStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Durin's Secret
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot Gandalf, a Dwarf, and an Elf to make the move limit for this turn +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("secret");

		assertEquals("Durin's Secret", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfADwarfAndAnElfMakesTheMoveLimitPlusOneForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gandalf, gimli, legolas);
		scn.MoveCardsToHand(secret);

		scn.StartGame();

		assertInHand(secret);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsPlayAvailable(secret));

		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 1 -> 2
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Still in force in the Shadow phase after the second move.
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// A third move is offered and made.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Gone with the turn: 2 on the other player's turn and on the next of the Free Peoples player's.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAGandalfCompanionWhoIsNotGandalfUntilGandalfIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(avenger, gimli, legolas);
		scn.MoveCardsToHand(secret, gandalf);

		scn.StartGame();

		// A Dwarf and an Elf, and a [gandalf] companion who is not Gandalf.
		assertInZone(Zone.FREE_CHARACTERS, avenger, gimli, legolas);
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsPlayAvailable(secret));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(secret));
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutADwarfUntilADwarfAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gandalf, legolas);
		scn.MoveCardsToHand(secret, thrarin);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsPlayAvailable(secret));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Dwarf who is an ally is spotted as well as one who is a companion.
		scn.FreepsPlayCard(thrarin);
		assertTrue(scn.FreepsPlayAvailable(secret));
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnElfUntilAnElfAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToHand(secret, orophin);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsPlayAvailable(secret));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf who is an ally is spotted as well as one who is a companion.
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.FreepsPlayAvailable(secret));
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
