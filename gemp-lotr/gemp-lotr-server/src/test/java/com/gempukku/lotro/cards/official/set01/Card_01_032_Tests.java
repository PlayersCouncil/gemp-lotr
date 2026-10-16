package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("border", "1_32");
					put("border2", "1_32");
					put("border3", "1_32");
					// Legolas, Greenleaf: Elf companion, strength 6, himself an archer; his ability is an Archery one
					put("legolas", "1_50");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8
					// Silinde, Elf of Mirkwood: an Elf ally (home site 3), in the support area here
					put("silinde", "1_60");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf, strength 8
					// Uruk Savage: an [isengard] minion that is no archer, strength 5, vitality 3, damage +1 and nothing else
					put("savage", "1_151");
					// Goblin Marksman: a [moria] archer, strength 7, vitality 1; Archer is its whole text
					put("marksman", "1_176");
					// Archer of Harad: a [men] archer, strength 7, vitality 1; its text only adds to the archery total
					put("harad", "11_67");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BorderDefensesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Border Defenses
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Elf strength +2 (or +4 if skirmishing an archer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("border");

		assertEquals("Border Defenses", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlus2UntilTheEndOfTheSkirmishWhenHisMinionIsNoArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var border = scn.GetFreepsCard("border");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(legolas, haldir, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(border);

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The archer goes down after the archery phase, so that it is in a skirmish and not in the way.
		scn.MoveMinionsToTable(marksman);
		scn.FreepsRefreshDecision();
		assertInHand(border);

		// Not in the assignment phase, the phase before the skirmishes.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(border));

		scn.PassAssignmentActions();
		// Legolas, himself an archer, fights a minion that is no archer; the archer is in Aragorn's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, savage}, new PhysicalCardImpl[]{aragorn, marksman});
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(border));

		scn.FreepsPlayCard(border);
		// Each Elf is offered: in this skirmish, in none, and an ally; no other companion is.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, haldir, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(legolas);

		// 6 + 2
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(haldir));
		assertInDiscard(border);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Legolas (8) beats the Savage (5); in Aragorn's skirmish, which follows, he is back to 6.
		// The Savage took Legolas's archery wound on the way here.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, marksman));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesAnElfSkirmishingAnArcherStrengthPlus4AndOneWaitingForHisSkirmishPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var border = scn.GetFreepsCard("border");
		var border2 = scn.GetFreepsCard("border2");
		var border3 = scn.GetFreepsCard("border3");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCompanionsToTable(legolas, haldir, aragorn);
		scn.MoveCardsToHand(border, border2, border3);

		scn.StartGame();
		assertInHand(border, border2, border3);

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The archers, of two cultures, go down after the archery phase.
		scn.MoveMinionsToTable(marksman, harad);
		scn.PassAssignmentActions();
		// Legolas's skirmish is fought first, Haldir's after it.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, marksman}, new PhysicalCardImpl[]{haldir, harad},
				new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(haldir));
		assertInHand(border, border2, border3);

		scn.FreepsPlayCard(border);
		scn.FreepsChooseCard(legolas);
		// 6 + 4: he is skirmishing an archer.
		assertEquals(10, scn.GetStrength(legolas));

		scn.ShadowPass();
		scn.FreepsPlayCard(border2);
		scn.FreepsChooseCard(haldir);
		// 5 + 2: Haldir is assigned to an archer, but his skirmish has not begun.
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(10, scn.GetStrength(legolas));
		assertInDiscard(border, border2);

		// Legolas (10) beats the Marksman; in Haldir's own skirmish both are as printed again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.IsCharSkirmishingAgainst(haldir, harad));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsPlayCard(border3);
		scn.FreepsChooseCard(haldir);
		// 5 + 4: the [men] archer counts as the [moria] one did.
		assertEquals(9, scn.GetStrength(haldir));
		assertInDiscard(border3);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MayBePlayedWithNoElfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var border = scn.GetFreepsCard("border");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		// No Elf is in play: the only companion is Aragorn, who is none.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(border);

		scn.StartGame();
		assertInHand(border);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertEquals(8, scn.GetStrength(aragorn));
		// Nothing in the text is a cost or a requirement, so the event can be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(border));

		scn.FreepsPlayCard(border);
		assertInDiscard(border);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
