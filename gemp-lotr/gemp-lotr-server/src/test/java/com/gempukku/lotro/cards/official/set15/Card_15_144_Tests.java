package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; his ability is used only on purpose
					// Black Land Observer: [orc] Orc, strength 8, vitality 2.  Its +3 wants a companion of strength
					// 10 or more, which no board here has; its play trigger is never met (it is cheated in).
					put("observer", "15_101");
					// Wandering Hillman: [men] Man, strength 8, vitality 3; the same +3, and a maneuver ability not used.
					put("hillman", "15_94");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				"15_144",
				// The Great Ring adds nothing while it is not worn, so Frodo reads his printed strength of 3.
				VirtualTableScenario.GreatRing
		);
	}

	protected VirtualTableScenario GetLoopScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// What Frodo bears to be 8 before his own bonus: Hobbit Sword (+2, no other text), Celebring
					// (+2; his text is about his own play) and Rohan Stable Master (+1; "considered to be mounted").
					// Laid on him, so nothing of their play or Aid is carried out.
					put("sword", "15_145");
					put("celebring", "18_7");
					put("stablemaster", "15_134");
					// Black Land Observer and Wandering Hillman: strength 8, and "While you can spot a companion of
					// strength 10 or more, this minion is strength +3."
					put("observer", "15_101");
					put("hillman", "15_94");
				}},
				VirtualTableScenario.FellowshipSites,
				"15_144",
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Frodo, Weary From the Journey
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Game Text: <b>Ring-bearer.</b> <b>Ring-bound</b>.<br>While skirmishing a minion of strength 8 or less, Frodo is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Weary From the Journey", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BEARER));
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(card.getBlueprint().canStartWithRing());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileSkirmishingAMinionOfStrength8HeIsStrengthPlus2ButNotWhileOnlyAssignedToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var observer = scn.GetShadowCard("observer");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(observer, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, observer}, new PhysicalCardImpl[]{aragorn, raiders});

		assertEquals(3, scn.GetStrength(frodo));

		// Aragorn's skirmish first: Frodo is assigned to the Observer, but not yet skirmishing it.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, raiders));
		assertEquals(3, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, observer));
		assertEquals(8, scn.GetStrength(observer));
		// 3 + 2
		assertEquals(5, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAMinionOfStrength9HeHasNoBonusThoughAMinionOfStrength8IsAssignedToAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var hillman = scn.GetShadowCard("hillman");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, raiders}, new PhysicalCardImpl[]{aragorn, hillman});

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, raiders));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(8, scn.GetStrength(hillman));
		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAMinionOfStrength9AndAMinionOfStrength8TogetherHeIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hillman = scn.GetShadowCard("hillman");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, raiders);
		// The Hillman, left over, is the Shadow player's to assign: to Frodo as well.
		scn.ShadowAssignToMinions(frodo, hillman);

		assertEquals(3, scn.GetStrength(frodo));

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, raiders));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, hillman));
		// He is skirmishing a minion of strength 8 (a [men] Man) as well as one of 9: 3 + 2.
		assertEquals(5, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AgainstBlackLandObserverInAnInvoluntaryLoopNeitherFrodoNorTheObserverGetsItsBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLoopScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("stablemaster"));
		var observer = scn.GetShadowCard("observer");

		scn.StartGame();
		scn.MoveMinionsToTable(observer);
		// 3 + 2 + 2 + 1
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(observer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, observer);
		scn.FreepsResolveSkirmish(frodo);

		// With his +2 he would be 10, which gives the Observer +3, which takes his +2 away: a loop, so neither.
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(observer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AgainstWanderingHillmanInAnInvoluntaryLoopNeitherFrodoNorTheHillmanGetsItsBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLoopScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("stablemaster"));
		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(hillman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, hillman);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(hillman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
