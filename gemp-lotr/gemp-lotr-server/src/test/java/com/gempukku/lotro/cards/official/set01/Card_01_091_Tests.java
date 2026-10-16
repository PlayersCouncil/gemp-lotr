package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_91");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ally
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Wizard companion, to bear the Stash
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally, vitality 3

					// Bilbo's Pipe: a [shire] pipe, on a Hobbit; its ability is used only on purpose
					put("bilbospipe", "1_285");
					// Old Toby: a [shire] pipeweed possession in the support area; its play offers a draw
					put("oldtoby", "1_305");
					// Wizard's Stash: a [gandalf] pipeweed possession borne by a Wizard; each companion bearing a pipe is
					// strength +1, which no test reads
					put("stash", "103_7");
					// Halfling Leaf: a [shire] pipeweed that is a condition, not a possession; to play, spot a Hobbit
					put("halfling", "17_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornsPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Aragorn's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be a [gondor] companion.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to heal X companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pipe");

		assertEquals("Aragorn's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyOnAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(aragorn, pippin, arwen);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(pipe);

		scn.StartGame();

		assertInZone(Zone.HAND, pipe);
		assertTrue(scn.FreepsPlayAvailable(pipe));

		scn.FreepsPlayCard(pipe);

		// A [gondor] Man and a [gondor] Hobbit are offered; an [elven] companion and a [gondor] ally are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(pipe, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingAPipeweedPossessionAndSpottingTwoPipesHealsTwoCompanionsOnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var orophin = scn.GetFreepsCard("orophin");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		var stash = scn.GetFreepsCard("stash");
		var halfling = scn.GetFreepsCard("halfling");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(aragorn, pipe);
		scn.AttachCardsTo(frodo, bilbospipe);
		scn.AttachCardsTo(gandalf, stash);
		scn.MoveCardsToSupportArea(oldtoby, halfling);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(orophin, 1);

		scn.StartGame();

		assertAttachedTo(pipe, aragorn);
		assertAttachedTo(bilbospipe, frodo);
		assertAttachedTo(stash, gandalf);
		assertInZone(Zone.SUPPORT, oldtoby, halfling);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);

		// Pipeweed possessions of two cultures, one in the support area and one borne, are offered; a pipeweed
		// condition and possessions that are no pipeweed are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(oldtoby, stash));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, pipe, bilbospipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stash);
		assertInZone(Zone.DISCARD, stash);
		assertInZone(Zone.SUPPORT, oldtoby, halfling);

		// Two pipes, of two cultures; Old Toby and Halfling Leaf are no pipes.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// Two different companions, each healed once; the ally is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, frodo);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(orophin));
		// The pipes are spotted, not discarded.
		assertAttachedTo(pipe, aragorn);
		assertAttachedTo(bilbospipe, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyItselfToSpotItHealsOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pipe);
		scn.MoveCardsToSupportArea(oldtoby);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		assertAttachedTo(pipe, aragorn);
		assertInZone(Zone.SUPPORT, oldtoby);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		// Old Toby, the one pipeweed possession, goes without a question.
		assertInZone(Zone.DISCARD, oldtoby);

		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(frodo);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XOfZeroMayBeChosenAndThePipeweedIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pipe);
		scn.MoveCardsToSupportArea(oldtoby);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, oldtoby);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		assertInZone(Zone.DISCARD, oldtoby);

		// The pipeweed is paid whatever X is, so X of 0 is a use like another (Gandalf's Pipe 1_74).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChoose("0");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedUntilAPipeweedPossessionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var halfling = scn.GetFreepsCard("halfling");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pipe);
		scn.AttachCardsTo(frodo, bilbospipe);
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToHand(oldtoby);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		// A pipeweed condition, two possessions that are no pipeweed, and a wounded companion to heal.
		assertInZone(Zone.SUPPORT, halfling);
		assertAttachedTo(pipe, aragorn);
		assertAttachedTo(bilbospipe, frodo);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pipe));

		// A pipeweed possession played by legal play.
		scn.FreepsPlayCard(oldtoby);
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, oldtoby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pipe));
	}
}
