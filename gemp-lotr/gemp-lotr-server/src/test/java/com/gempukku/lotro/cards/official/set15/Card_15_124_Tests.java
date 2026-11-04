package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "15_124");

					// Éomer, Keeper of Oaths: [rohan] Man companion, strength 8, vitality 3, resistance 6; his only
					// text answers his own play, and he is cheated in
					put("eomer", "8_87");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion who is no Éomer, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; "Fellowship: Exert Guma to heal a [rohan] Man"
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Éowyn) can be spotted
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so exhausted as it comes
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomersSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Éomer's Spear, Trusty Weapon
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	If bearer is Éomer and he is unwounded, he is <b>damage +1</b> and resistance +1.
		 * 	<b>Skirmish:</b> If bearer is Éomer, discard this possession to wound a minion he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Éomer's Spear", card.getBlueprint().getTitle());
		assertEquals("Trusty Weapon", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndNotOnARohanHobbitOrAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		// Two [rohan] companions only, so Merry is no Man.
		scn.MoveCompanionsToTable(eomer, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(spear);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.FreepsPlayAvailable(spear));
		scn.FreepsPlayCard(spear);

		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);

		assertAttachedTo(spear, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByEomerItGivesDamagePlus1AndResistancePlus1OnlyWhileHeIsUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(eomer, spear);

		scn.StartGame();
		scn.AddWoundsToChar(eomer, 1);
		scn.FreepsRefreshDecision();

		// Wounded: nothing.
		assertAttachedTo(spear, eomer);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(eomer));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(guard));

		// Guma heals him: unwounded, he is damage +1 and resistance 6 + 1. The Guard, unwounded but bearing
		// nothing, is untouched.
		scn.FreepsUseCardAction(guma);
		// Guma's own exertion leaves him wounded too, so the heal asks which.
		scn.FreepsChooseCard(eomer);
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertEquals(7, scn.GetResistance(eomer));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherRohanManItGivesNothingAndItsAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(guard, spear);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		// Both unwounded, Éomer in play beside him: the bearer is no Éomer, so neither gets anything.
		assertAttachedTo(spear, guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(eomer));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(spear));
		assertAttachedTo(spear, guard);
	}

	@Test
	public void SkirmishAbilityDiscardsTheSpearToWoundAMinionEomerIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, spear);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(spear, eomer);
		scn.MoveMinionsToTable(runner, savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eomer, runner },
				new PhysicalCardImpl[] { guard, troop }
		);
		// The Savage left over goes to Éomer as well.
		scn.ShadowAssignToMinions(eomer, savage);
		scn.FreepsResolveSkirmish(eomer);

		assertAttachedTo(spear, eomer);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(spear));
		scn.FreepsUseCardAction(spear);

		assertInDiscard(spear);
		// Both minions skirmishing him, of two cultures and races; not the Troop, waiting for the Guard's skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// The Runner is exhausted: the wound kills it, as an exertion could not.
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishTheSpearIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, spear);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(spear, eomer);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eomer, savage },
				new PhysicalCardImpl[] { guard, troop }
		);
		// The Guard's skirmish first: the Savage is assigned to Éomer and waits, the Troop fights the Guard.
		scn.FreepsResolveSkirmish(guard);

		assertAttachedTo(spear, eomer);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(spear));
		scn.FreepsUseCardAction(spear);

		assertInDiscard(spear);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
