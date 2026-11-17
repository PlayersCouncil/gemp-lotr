package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("task", "11_175");

					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit companion, resistance 6; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: an unbound [gondor] Hobbit companion, resistance 6; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit companion who is not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who cannot be unbound; a skirmish ability used only on purpose

					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion; abilities used only on purpose
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai minion, strength 5; damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ATaskNowtoBeDoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: A Task Now to Be Done
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot X Hobbits to make an unbound Hobbit resistance +X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("task");

		assertEquals("A Task Now to Be Done", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndInASkirmishSpotsHobbitsToMakeAnUnboundHobbitResistanceHigher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var task = scn.GetFreepsCard("task");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(task);
		assertInHand(task);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, savage);

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(task));
		scn.PassAssignmentActions();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{merry, ted});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(merry));
		assertTrue(scn.FreepsPlayAvailable(task));
		scn.FreepsPlayCard(task);

		// Six Hobbits: Frodo, Sam, Merry, Pippin, the ally Bounder and the minion Ted Sandyman; not Aragorn or the
		// Uruk. The number comes filled in with all six (F-0561), and fewer may be named.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(6), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// An unbound Hobbit of either culture, in this skirmish or none; not the Ring-bound Sam or Frodo, the ally,
		// the Hobbit minion or the companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, ted, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertInDiscard(task);
		// Two named of the six: 6 + 2, not + 6.
		assertEquals(8, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AllHobbitsSpottedRaiseResistanceBySixUntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var task = scn.GetFreepsCard("task");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(task);
		assertInHand(task);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{merry, ted});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(6, scn.GetResistance(merry));
		scn.FreepsPlayCard(task);
		scn.FreepsChoose("6");
		scn.FreepsChooseCard(merry);

		// All six named: 6 + 6. Merry's own skirmish is still to come.
		assertEquals(12, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasted for the skirmish phase it was played in; the next skirmish is a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, ted));
		assertEquals(6, scn.GetResistance(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var task = scn.GetFreepsCard("task");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(task);
		assertInHand(task);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.FreepsPlayAvailable(task));
		scn.FreepsPlayCard(task);
		// Frodo and Sam may be spotted, though neither is unbound.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		assertInDiscard(task);
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
