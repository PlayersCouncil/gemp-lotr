package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_74");
					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk: stronger for each race in the fellowship, nothing else
					put("avenger", "6_27");         // Ent Avenger: a [gandalf] companion who is no Gandalf; stronger only beside 3 Ents
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: bears the second pipe; a maneuver ability never used here
					put("aragornspipe", "1_91");    // Aragorn's Pipe: a [gondor] pipe; its own fellowship ability is never used
					put("bilbospipe", "1_285");     // Bilbo's Pipe: a [shire] pipe, on Frodo; its own fellowship ability is never used
					put("toby", "1_305");           // Old Toby: [shire] pipeweed possession, support area; when played, may draw a card
					put("stash", "103_7");          // Wizard's Stash: [gandalf] pipeweed possession borne by a Wizard; companions bearing a pipe are +1; a regroup ability never used here
					put("halfling", "17_106");      // Halfling Leaf: [shire] pipeweed CONDITION, support area; a skirmish ability never used here
					put("book", "2_113");           // Red Book of Westmarch: a [shire] possession in the support area that is no pipeweed; answers a tale played beside Bilbo
					put("sword", "1_299");          // Hobbit Sword: a possession borne by Frodo that is no pipeweed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gandalf's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be Gandalf.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to remove X burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pipe");

		assertEquals("Gandalf's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAPipeweedAndSpotsXPipesToRemoveXBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var toby = scn.GetFreepsCard("toby");
		var stash = scn.GetFreepsCard("stash");
		var halfling = scn.GetFreepsCard("halfling");
		var book = scn.GetFreepsCard("book");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		// Three pipes of three cultures on three bearers
		scn.AttachCardsTo(gandalf, pipe, stash);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.AttachCardsTo(frodo, bilbospipe, sword);
		// Two pipeweed possessions of two cultures, one in the support area and one borne; a pipeweed that is a
		// condition; possessions that are no pipeweed
		scn.MoveCardsToSupportArea(toby, halfling, book);

		scn.StartGame();
		// Three burdens and three pipes: "X burdens" is neither every burden nor one.
		scn.AddBurdens(3);

		assertEquals(3, scn.GetBurdens());
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(stash, gandalf);
		assertAttachedTo(aragornspipe, aragorn);
		assertAttachedTo(bilbospipe, frodo);
		assertInZone(Zone.SUPPORT, toby, halfling, book);
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		// The pipeweed to discard: a pipeweed possession of either culture, wherever it lies; not a pipeweed that
		// is a condition, a possession that is no pipeweed, or a pipe.
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, stash));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, book, sword, pipe, aragornspipe, bilbospipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(toby);
		assertInZone(Zone.DISCARD, toby);
		assertInZone(Zone.SUPPORT, halfling, book);
		assertAttachedTo(stash, gandalf);

		// X may be as many as the three pipes in play: not 2 (the pipeweeds left), not 1 (the pipeweed
		// possessions left, or the [gandalf] pipes).
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(aragornspipe, aragorn);
		assertAttachedTo(bilbospipe, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again, with the one pipeweed possession left: it goes unasked, and X of 1 removes one burden.
		assertTrue(scn.FreepsActionAvailable(pipe));
		scn.FreepsUseCardAction(pipe);
		assertInZone(Zone.DISCARD, stash);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, halfling, book);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoPipeweedPossessionInPlayAndOfferedOnceOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var book = scn.GetFreepsCard("book");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, pipe);
		// A pipeweed that is no possession and a possession that is no pipeweed
		scn.MoveCardsToSupportArea(halfling, book);
		scn.MoveCardsToHand(toby);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, halfling, book);
		assertFalse(scn.FreepsActionAvailable(pipe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(toby);
		// Old Toby's own draw, declined
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsActionAvailable(pipe));

		// Old Toby is the only pipeweed possession, and the pipe the only pipe: X of 1.
		scn.FreepsUseCardAction(pipe);
		assertInZone(Zone.DISCARD, toby);
		assertInZone(Zone.SUPPORT, halfling, book);
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingNoPipesStillDiscardsThePipeweedAndRemovesNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, pipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetBurdens());
		assertAttachedTo(pipe, gandalf);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsActionAvailable(pipe));

		// The pipeweed is paid for whatever X is, so X may be none of the pipes (CR 4.2 "spot": a player need not
		// spot all he could).
		scn.FreepsUseCardAction(pipe);
		assertInZone(Zone.DISCARD, toby);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");
		assertEquals(2, scn.GetBurdens());
		assertAttachedTo(pipe, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedOnAGandalfCompanionWhoIsNotGandalfAndIsPlayedOnGandalfOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(pipe, gandalf);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertFalse(scn.FreepsPlayAvailable(pipe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(pipe));

		// Gandalf is the one bearer there can be: the pipe goes on him unasked.
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
