package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "6_80");
					put("legion2", "6_80");

					put("elf1", "1_53");        // Lorien Elf: companions to count, with Frodo; no text once in play
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("elf4", "1_53");
					put("elf5", "1_53");
					put("elf6", "1_53");
					put("elf7", "1_53");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, not a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronArcherLegionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Southron Archer Legion
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.<br><b>Archery:</b> Remove (3) to add 1 to the minion archery total for each companion over 4 (limit +4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legion");

		assertEquals("Southron Archer Legion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ArcheryAbilityRemoves3ToAdd1ToTheMinionArcheryTotalForEachCompanionOver4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3", "elf4", "elf5");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(legion);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Six companions and an ally: the ally is no companion, so 2 over 4, not 3.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowActionAvailable(legion));
		scn.ShadowUseCardAction(legion);

		assertEquals(2, scn.GetTwilight());
		// 1 for the Legion as an archer + 2
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithFourCompanionsTheCostIsPaidForNothingAndWithFiveItAdds1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var elf4 = scn.GetFreepsCard("elf4");
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(legion);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(6);
		scn.FreepsPass();

		// Four companions and an ally: none over 4.  The ability has a cost, so it is offered and paid for nothing.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowActionAvailable(legion));
		scn.ShadowUseCardAction(legion);
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetShadowArcheryTotal());

		// A fifth companion: one over 4.
		scn.MoveCompanionsToTable(elf4);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(legion));
		scn.ShadowUseCardAction(legion);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThan3InThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3", "elf4", "elf5");

		scn.StartGame();
		scn.MoveMinionsToTable(legion);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(legion));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(legion));
	}

	@Test
	public void EachCopyAddsNoMoreThan4InAPhaseAndIsStillPaidForAtItsLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var legion2 = scn.GetShadowCard("legion2");
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3", "elf4", "elf5", "elf6");

		scn.StartGame();
		scn.MoveMinionsToTable(legion, legion2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(12);
		scn.FreepsPass();

		// Seven companions: 3 over 4.  Two Legions, each an archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.ShadowUseCardAction(legion);
		assertEquals(9, scn.GetTwilight());
		assertEquals(5, scn.GetShadowArcheryTotal());

		// The second use reaches this copy's +4 for the phase: +1, not +3.
		scn.FreepsPass();
		scn.ShadowUseCardAction(legion);
		assertEquals(6, scn.GetTwilight());
		assertEquals(6, scn.GetShadowArcheryTotal());

		// At its limit it is still offered, and the (3) is paid for nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(legion));
		scn.ShadowUseCardAction(legion);
		assertEquals(3, scn.GetTwilight());
		assertEquals(6, scn.GetShadowArcheryTotal());

		// The other copy has a limit of its own.
		scn.FreepsPass();
		scn.ShadowUseCardAction(legion2);
		assertEquals(0, scn.GetTwilight());
		assertEquals(9, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheAdditionEndsWithTheArcheryPhaseAndTheLimitIsCountedAfreshInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var elf3 = scn.GetFreepsCard("elf3");
		var elf4 = scn.GetFreepsCard("elf4");
		var elf5 = scn.GetFreepsCard("elf5");
		var elf6 = scn.GetFreepsCard("elf6");
		var elf7 = scn.GetFreepsCard("elf7");
		scn.MoveCompanionsToTable(elf1, elf2, elf3, elf4, elf5, elf6, elf7);

		scn.StartGame();
		scn.MoveMinionsToTable(legion);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// Eight companions: 4 over 4, the whole of the limit at once.
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.ShadowUseCardAction(legion);
		assertEquals(5, scn.GetShadowArcheryTotal());
		scn.PassArcheryActions();

		// Five archery wounds, one on each of five Elves.
		scn.FreepsChooseCard(elf1);
		scn.FreepsChooseCard(elf2);
		scn.FreepsChooseCard(elf3);
		scn.FreepsChooseCard(elf4);
		scn.FreepsChooseCard(elf5);
		assertEquals(1, scn.GetWoundsOn(elf5));
		// The archery phase is over, and the +4 with it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetShadowArcheryTotal());

		// The Legion skirmishes an unwounded Elf, who is overwhelmed: seven companions are left.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(elf6, legion);
		scn.FreepsResolveSkirmish(elf6);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, elf6);

		// A second move: the Legion is still in play for a second archery phase this turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowActionAvailable(legion));
		scn.ShadowUseCardAction(legion);
		// Seven companions: 3 over 4, all of it, the +4 of the first archery phase not counted against it.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
