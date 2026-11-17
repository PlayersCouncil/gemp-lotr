package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_231_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fey", "7_231");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] Man ALLY, strength 3; an archery ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it, never here
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FeyHeSeemedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fey He Seemed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] Man strength +2 (or +3 and damage +1 if skirmishing a wounded minion).
		 */

		var scn = GetScenario();

		var fey = scn.GetFreepsCard("fey");

		assertEquals("Fey He Seemed", fey.getBlueprint().getTitle());
		assertNull(fey.getBlueprint().getSubtitle());
		assertFalse(fey.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, fey.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, fey.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fey.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fey, Timeword.SKIRMISH));
		assertEquals(1, fey.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARohanManSkirmishingAWoundedMinionIsStrengthPlus3AndDamagePlus1ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fey = scn.GetFreepsCard("fey");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(fey);
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, chieftain);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInHand(fey);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, savage },
				new PhysicalCardImpl[] { aragorn, chieftain }
		);
		scn.FreepsResolveSkirmish(guard);

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(aldor));
		assertTrue(scn.FreepsPlayAvailable(fey));

		scn.FreepsPlayCard(fey);

		// A [rohan] Man, a companion or an ally, the ally in no skirmish; not a [rohan] Hobbit, nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInDiscard(fey);
		assertEquals(9, scn.GetStrength(guard));    // 6 + 3
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(aldor));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
	}

	@Test
	public void ARohanManAssignedToAWoundedMinionWhoseSkirmishHasNotBegunIsOnlyStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fey = scn.GetFreepsCard("fey");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(fey);
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInHand(fey);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, savage },
				new PhysicalCardImpl[] { aragorn, troop }
		);
		// Aragorn's skirmish, against a wounded minion; the Guard's wounded minion waits.
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// The Guard is the only [rohan] Man, so he is chosen unasked.
		scn.FreepsPlayCard(fey);

		assertInDiscard(fey);
		assertEquals(8, scn.GetStrength(guard));    // 6 + 2
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanManSkirmishingAnUnwoundedMinionIsOnlyStrengthPlus2ThoughHeIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fey = scn.GetFreepsCard("fey");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(fey);
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, chieftain);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInHand(fey);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, chieftain },
				new PhysicalCardImpl[] { aragorn, savage }
		);
		// The Guard's skirmish, against the unwounded Orc; the wounded Uruk-hai is in Aragorn's, still to come.
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// The Guard is the only [rohan] Man, so he is chosen unasked.
		scn.FreepsPlayCard(fey);

		assertInDiscard(fey);
		assertEquals(8, scn.GetStrength(guard));    // 6 + 2
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanManOnTheTableTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fey = scn.GetFreepsCard("fey");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(fey);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInHand(fey);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// A [rohan] Hobbit (no Man with one [rohan] companion) and a [gondor] Man, the latter skirmishing a wounded minion.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(fey));

		scn.FreepsPlayCard(fey);

		assertInDiscard(fey);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fey = scn.GetFreepsCard("fey");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(fey);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInHand(fey);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fey));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fey));
	}
}
