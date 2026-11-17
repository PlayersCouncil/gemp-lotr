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

public class Card_10_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brave", "10_105");
					put("brave2", "10_105");
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3, vitality 4,
					                            // enduring (strength +2 for each wound)
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion, strength 3, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who may exert to skirmish, strength 4, vitality 2

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("savage3", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BraveandLoyalStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Brave and Loyal
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If a Hobbit companion is not assigned to a skirmish, exert that Hobbit to have him or her replace a companion (except the Ring-bearer) in a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brave");

		assertEquals("Brave and Loyal", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnUnassignedHobbitCompanionExertsToTakeTheCompanionsPlaceInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brave = scn.GetFreepsCard("brave");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bounder = scn.GetFreepsCard("bounder");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(merry, sam, aragorn, gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(brave);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{sam, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(brave));
		scn.FreepsPlayCard(brave);

		// The unassigned Hobbit companions, the Ring-bearer among them; not Sam, whose skirmish is still to come,
		// nor the Hobbit ally, nor the unassigned companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, bounder, gandalf, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertInDiscard(brave);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		// The skirmish still to come is untouched.
		assertTrue(scn.IsCharAssignedAgainst(sam, savage2));

		scn.PassSkirmishActions();

		// 3 + 2 (enduring, one wound) = 5 against 5: Merry loses, and takes 1 wound + 1 for damage +1.
		// Aragorn, out of the skirmish, takes nothing.
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseNorWhileEveryHobbitCompanionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brave = scn.GetFreepsCard("brave");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bounder = scn.GetFreepsCard("bounder");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		scn.MoveCompanionsToTable(merry, aragorn, gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(brave);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, savage3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Every Hobbit is unassigned, but this is no skirmish.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(brave));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{frodo, savage2},
				new PhysicalCardImpl[]{merry, savage3});
		scn.FreepsResolveSkirmish(aragorn);

		// Both Hobbit companions are assigned; the Hobbit ally and Gandalf are not, and could exert.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsPlayAvailable(brave));
	}

	@Test
	public void NotPlayableWhenTheOnlyUnassignedHobbitCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brave = scn.GetFreepsCard("brave");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		scn.MoveCompanionsToTable(merry, sam, aragorn, gandalf);
		scn.MoveCardsToHand(brave);
		// Vitality 4 with 3 wounds: Merry cannot exert.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(savage1, savage2, savage3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{frodo, savage2},
				new PhysicalCardImpl[]{sam, savage3});
		scn.FreepsResolveSkirmish(aragorn);

		// Sam and Frodo could exert, but are assigned; Gandalf could, but is no Hobbit.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsPlayAvailable(brave));
	}

	@Test
	public void WithAnAllyOrTheRingBearerInTheSkirmishTheHobbitExertsAndNobodyIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brave = scn.GetFreepsCard("brave");
		var brave2 = scn.GetFreepsCard("brave2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var thrarin = scn.GetFreepsCard("thrarin");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(brave, brave2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage1}, new PhysicalCardImpl[]{thrarin, savage2});
		scn.FreepsResolveSkirmish(thrarin);

		// An ally in the skirmish: no companion to replace.
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsPlayCard(brave);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, savage2));
		assertFalse(scn.IsCharSkirmishing(merry));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);

		// The Ring-bearer in the skirmish: he is excepted.
		scn.FreepsPlayCard(brave2);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage1));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
