package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "17_93");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, not unique; valiant and no other text
					put("merry", "103_85");      // Merry, Master Holbytla: the [rohan] companion who is a Hobbit; his skirmish ability is used only on purpose
					put("herdsman", "7_247");    // Rohirrim Herdsman: a [rohan] Man who is an ally; a regroup ability only
					put("gimli", "1_13");        // Gimli, Son of Glóin: a [dwarven] companion; a skirmish ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability only

					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1; its play trigger does not fire when cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo loses to a Goblin Runner and goes on
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Aragorn, Defender of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>. While the Ring-bearer is assigned to a skirmish, each [rohan] companion gains <b>hunter 1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Defender of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileTheRingBearerIsAssignedEachRohanCompanionIsHunter1UntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		// Two [rohan] companions only: Merry stays a Hobbit and no Man.
		scn.MoveCompanionsToTable(aragorn, merry, gimli);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		// Nobody is assigned yet.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.HUNTER));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{aragorn, runner2});

		// The [rohan] companions, Aragorn himself (a Man) and Merry (a Hobbit), gain hunter 1; the [rohan] ally, the
		// [dwarven] companion and the [shire] Ring-bearer do not.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(herdsman, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.HUNTER));

		// The Ring-bearer's own skirmish first: he is still assigned while it is fought.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.HUNTER));
		scn.PassSkirmishActions();

		// Once his skirmish is over he is no longer assigned (CR 4.2 "skirmish phase(s)").
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerAssignedGivesNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, guard, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{aragorn, runner2});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharAssigned(sam));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.HUNTER));
	}
}
