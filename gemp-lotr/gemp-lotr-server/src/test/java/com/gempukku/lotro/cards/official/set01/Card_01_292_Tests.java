package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_292_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_292");
					// Bilbo's Pipe: a second [shire] pipe, on a Hobbit; its own fellowship ability is never used
					put("bilbospipe", "1_285");
					// Aragorn's Pipe: a [gondor] pipe on a [gondor] companion; its own fellowship ability is never used
					put("aragornspipe", "1_91");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here
					// Saruman's Hospitality: a [gandalf] pipeweed possession, borne by an unbound companion bearing a
					// pipe; a move trigger only (it needs pipes of 3 cultures)
					put("hospitality", "103_4");
					put("toby", "1_305");          // Old Toby: [shire] pipeweed possession, support area; when played, may draw a card
					put("halfling", "17_106");     // Halfling Leaf: [shire] pipeweed CONDITION, support area; a skirmish ability
					put("sword", "1_299");         // Hobbit Sword: a [shire] possession that is neither pipe nor pipeweed
					// Sam, Son of Hamfast: Hobbit companion; his fellowship ability (exert to remove a burden) is never used
					put("sam", "1_311");
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; a skirmish ability never used here
					put("trollslayer", "103_31");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheGaffersPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Gaffer's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be a Hobbit.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to remove (X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pipe");

		assertEquals("The Gaffer's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAHobbitCompanionOrAllyAndNotOnACompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(trollslayer, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(pipe);

		scn.StartGame();

		assertInHand(pipe);
		assertTrue(scn.FreepsPlayAvailable(pipe));
		scn.FreepsPlayCard(pipe);

		// The Ring-bearer, a Hobbit who is a [gondor] card, and a Hobbit ally
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, trollslayer, bounder));
		// A [shire] companion who is no Hobbit
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(bounder);

		assertAttachedTo(pipe, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsAPipeweedPossessionAndSpotsXPipesToRemoveXTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hospitality = scn.GetFreepsCard("hospitality");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, sam);
		// Three pipes, two of them [shire] and one [gondor]
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(sam, bilbospipe, sword);
		scn.AttachCardsTo(aragorn, aragornspipe, hospitality);
		scn.MoveCardsToSupportArea(toby, halfling);

		scn.StartGame();
		scn.SetTwilight(5);

		assertAttachedTo(pipe, frodo);
		assertAttachedTo(bilbospipe, sam);
		assertAttachedTo(sword, sam);
		assertAttachedTo(aragornspipe, aragorn);
		assertAttachedTo(hospitality, aragorn);
		assertInZone(Zone.SUPPORT, toby, halfling);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);

		// A pipeweed possession of each of two cultures, wherever it lies; not a pipeweed condition, the pipes, or a
		// possession that is no pipeweed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, hospitality));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, pipe, bilbospipe, aragornspipe, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(hospitality);
		assertInDiscard(hospitality);
		assertInZone(Zone.SUPPORT, toby, halfling);

		// X: up to the 3 pipes. Not 2 (the [shire] pipes, or the pipeweeds left), not 5 (the pool, or the possessions).
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(3, scn.GetTwilight());
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(bilbospipe, sam);
		assertAttachedTo(aragornspipe, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again, with Old Toby the one pipeweed possession left: he goes unasked, and X of 1 removes one.
		assertTrue(scn.FreepsActionAvailable(pipe));
		scn.FreepsUseCardAction(pipe);
		assertInDiscard(toby);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, halfling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingNoPipesStillDiscardsThePipeweedAndRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.SetTwilight(3);

		assertAttachedTo(pipe, frodo);
		assertInZone(Zone.SUPPORT, toby);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsUseCardAction(pipe);
		assertInDiscard(toby);

		// A player need not spot all he could (CR 4.2 "spot"): none, or the one pipe.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		assertEquals(3, scn.GetTwilight());
		assertAttachedTo(pipe, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyTwilightPoolThePipeweedIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();

		assertAttachedTo(pipe, frodo);
		assertInZone(Zone.SUPPORT, toby);
		assertEquals(0, scn.GetTwilight());
		// An action with a cost is offered whenever the cost can be paid (Q-0034).
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		assertInDiscard(toby);
		scn.FreepsChoose("1");

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAPipeweedPossessionAndOfferedOnceOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		// A pipeweed that is no possession
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToHand(toby);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertAttachedTo(pipe, frodo);
		assertInZone(Zone.SUPPORT, halfling);
		assertInHand(toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pipe));

		scn.FreepsPlayCard(toby);
		// Old Toby's own draw is declined.
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pipe));
	}
}
