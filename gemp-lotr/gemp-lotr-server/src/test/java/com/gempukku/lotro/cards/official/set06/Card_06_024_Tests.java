package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boomed", "6_24");

					put("quickbeam", "6_33");   // Quickbeam, Bregalad: Ent companion, strength 8; only a cost discount of his own
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: Ent ally, strength 12; a Fellowship ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion who is no Ent
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit
					put("pippin", "103_31");    // Pippin, Trollslayer: unbound [gondor] Hobbit; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a Hobbit ally, never unbound; a skirmish ability used only on purpose

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoomedandTrumpetedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Boomed and Trumpeted
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Ent strength +X and <b>damage +X</b>, where X is the number of unbound Hobbits you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boomed");

		assertEquals("Boomed and Trumpeted", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnEntStrengthAndDamagePlus2ForTwoUnboundHobbitsUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boomed = scn.GetFreepsCard("boomed");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(quickbeam, gandalf, merry, pippin, sam);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(boomed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{quickbeam, runner1}, new PhysicalCardImpl[]{gandalf, runner2});
		scn.FreepsResolveSkirmish(quickbeam);

		assertEquals(8, scn.GetStrength(quickbeam));
		assertEquals(0, scn.GetKeywordCount(quickbeam, Keyword.DAMAGE));
		assertEquals(12, scn.GetStrength(treebeard));
		assertTrue(scn.FreepsPlayAvailable(boomed));
		scn.FreepsPlayCard(boomed);

		// An Ent in the skirmish and an Ent ally who is in none; no one who is not an Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(quickbeam, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, merry, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(quickbeam);

		// X = 2: Merry and Pippin. Frodo and Sam are Ring-bound, Bounder is an ally.
		assertEquals(10, scn.GetStrength(quickbeam));
		assertEquals(2, scn.GetKeywordCount(quickbeam, Keyword.DAMAGE));
		assertEquals(12, scn.GetStrength(treebeard));
		assertInDiscard(boomed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The runner dies in the first skirmish; in the next one the bonus is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(8, scn.GetStrength(quickbeam));
		assertEquals(0, scn.GetKeywordCount(quickbeam, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneUnboundHobbitAnEntAllyInNoSkirmishIsMadeStrengthAndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boomed = scn.GetFreepsCard("boomed");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(quickbeam, merry, sam);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(boomed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(quickbeam, runner1);
		scn.FreepsResolveSkirmish(quickbeam);

		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(quickbeam));
		scn.FreepsPlayCard(boomed);
		scn.FreepsChooseCard(treebeard);

		// X = 1: Merry alone.
		assertEquals(13, scn.GetStrength(treebeard));
		assertEquals(1, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(quickbeam));
		assertEquals(0, scn.GetKeywordCount(quickbeam, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boomed = scn.GetFreepsCard("boomed");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(quickbeam, merry);
		scn.MoveCardsToHand(boomed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(boomed));
	}

	@Test
	public void WithNoEntInPlayItIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boomed = scn.GetFreepsCard("boomed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(boomed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);

		// 6, +1 for the Hobbit companion Merry
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(boomed));
		scn.FreepsPlayCard(boomed);

		assertInDiscard(boomed);
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
