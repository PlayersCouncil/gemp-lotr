package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_297_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("work", "4_297");
					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant only
					put("guard2", "5_83");
					// Firefoot, Mearas of The Mark: a [rohan] mount, strength +1, on a [rohan] Man; its play trigger
					// is not reached by a cheat, and its maneuver trigger needs Eomer
					put("firefoot", "13_126");
					// Sturdy Shield: a [rohan] possession that is no mount (a shield), strength +1 while no opponent
					// controls a site; a regroup ability used only on purpose
					put("shield", "15_141");
					// Rohirrim Herdsman: a [rohan] Man ally, strength 4; a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Merry, Master Holbytla: a [rohan] Hobbit, no Man while fewer than 3 [rohan] companions (or
					// Eowyn) can be spotted; his abilities are used only on purpose, and nobody exerts here
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability only

					// Crebain Flock: [isengard] Crow, strength 2, vitality 3; its Shadow ability is used only on purpose
					put("crebain1", "101_25");
					put("crebain2", "101_25");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WorkfortheSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Work for the Sword
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [rohan] Man strength +2 (and <b>damage +2</b> if mounted).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("work");

		assertEquals("Work for the Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAMountedRohanManStrengthPlus2AndDamagePlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var work = scn.GetFreepsCard("work");
		var guard = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var crebain1 = scn.GetShadowCard("crebain1");
		var crebain2 = scn.GetShadowCard("crebain2");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, firefoot);
		scn.MoveCardsToHand(work);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, firefoot);
		assertInHand(work);
		scn.MoveMinionsToTable(crebain1, crebain2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, crebain1},
				new PhysicalCardImpl[]{aragorn, crebain2});
		scn.FreepsResolveSkirmish(guard);

		// 6 + 1 for Firefoot.
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(work));

		scn.FreepsPlayCard(work);
		// A [rohan] Man in this skirmish, and one in none (an ally); not the [rohan] Hobbit, nor a Man of [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInDiscard(work);
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(0, scn.GetKeywordCount(herdsman, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish; gone once the next skirmish has begun.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(crebain1));
		scn.FreepsPass();
		// The Guard wins: one wound and two more for damage +2 kill the Crebain, vitality 3.
		assertInDiscard(crebain1);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
	}

	@Test
	public void ARohanManBearingAPossessionThatIsNoMountIsMadeStrongerAndGainsNoDamage() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var work = scn.GetFreepsCard("work");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var shield = scn.GetFreepsCard("shield");
		var firefoot = scn.GetFreepsCard("firefoot");
		var crebain1 = scn.GetShadowCard("crebain1");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, shield);
		// The second Guard is mounted beside the first, who is not: the damage turns on the Man chosen.
		scn.AttachCardsTo(guard2, firefoot);
		scn.MoveCardsToHand(work);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, shield, firefoot);
		assertInHand(work);
		scn.MoveMinionsToTable(crebain1);

		// Not in the assignment phase, the Free Peoples player's last chance to act before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(work));
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(guard1, crebain1);
		scn.FreepsResolveSkirmish(guard1);

		// 6 + 1 for the Shield while no opponent controls a site; 6 + 1 for Firefoot.
		assertEquals(7, scn.GetStrength(guard1));
		assertEquals(7, scn.GetStrength(guard2));
		assertEquals(0, scn.GetKeywordCount(guard1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(work));

		scn.FreepsPlayCard(work);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2));
		scn.FreepsChooseCard(guard1);

		// The Shield is no mount: strength +2 and no damage bonus; the mounted Guard, not chosen, is untouched.
		assertInDiscard(work);
		assertEquals(9, scn.GetStrength(guard1));
		assertEquals(0, scn.GetKeywordCount(guard1, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(guard2));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanManInPlayTheEventIsStillPlayedAndChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var work = scn.GetFreepsCard("work");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var crebain1 = scn.GetShadowCard("crebain1");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(work);

		scn.StartGame();
		assertInHand(work);
		scn.MoveMinionsToTable(crebain1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, crebain1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(work));

		// The [rohan] Hobbit and the [gondor] Man are no [rohan] Men: played for nothing (CR 4.2 "effect").
		scn.FreepsPlayCard(work);

		assertInDiscard(work);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
