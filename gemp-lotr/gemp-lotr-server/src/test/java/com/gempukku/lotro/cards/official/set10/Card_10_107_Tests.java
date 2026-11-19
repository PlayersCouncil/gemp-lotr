package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heart", "10_107");
					put("heart2", "10_107");
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose

					put("ted", "18_75");        // Ted Sandyman: a Hobbit minion; maneuver and regroup abilities only
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("runner", "1_178");     // Goblin Runner: vitality 1; its play trigger does not fire when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatHeartStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Great Heart
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Vitality: -1
		 * Game Text: Play this condition only during a skirmish. Bearer must be a Hobbit.<br>When this condition is played, wound a minion skirmishing bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("heart");

		assertEquals("Great Heart", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayableOnlyDuringASkirmishWhereItWoundsTheMinionSkirmishingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(heart);

		scn.StartGame();

		// The fellowship phase, where a condition is otherwise played.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(heart));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(heart));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(heart));
		scn.FreepsPlayCard(heart);
		scn.FreepsChooseCard(merry);

		assertAttachedTo(heart, merry);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BearerIsAnyHobbitAndTheMinionWoundedIsOneSkirmishingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var heart2 = scn.GetFreepsCard("heart2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(heart, heart2);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, savage1, savage2, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage1}, new PhysicalCardImpl[]{aragorn, savage2});
		// The Shadow player's leftovers: the Goblin Runner joins Merry's skirmish, Ted Sandyman Aragorn's.
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{merry, runner}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsPlayCard(heart);

		// Any Hobbit may bear it: the Ring-bearer, a [rohan] Hobbit, a Hobbit ally and the opponent's Hobbit minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// The two minions skirmishing Merry, and not those assigned to Aragorn's skirmish still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		// A wound, not an exertion: the Runner, of vitality 1, is killed.
		assertAttachedTo(heart, merry);
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage1));

		// Another copy played afterwards, on a Hobbit in no skirmish: it has no minion to wound, and the first
		// copy, played already, wounds nothing more.
		scn.ShadowPass();
		scn.FreepsPlayCard(heart2);
		scn.FreepsChooseCard(bounder);

		assertAttachedTo(heart2, bounder);
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedOnAHobbitWhoseSkirmishIsStillToComeItWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(heart);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{merry, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		scn.FreepsPlayCard(heart);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(merry);

		// Merry's minion is assigned to him and not yet skirmishing; the minion fighting now fights Aragorn.
		assertAttachedTo(heart, merry);
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
