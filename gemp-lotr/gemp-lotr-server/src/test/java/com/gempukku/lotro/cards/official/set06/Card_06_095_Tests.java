package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hrethel", "6_95");
					// Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					put("guard", "5_83");
					// Aldor, Soldier of Edoras: [rohan] Man ally, home 3T/6T; an archery ability used only on purpose
					put("aldor", "4_262");
					// Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose
					put("boromir", "1_97");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, and no Man unless 3 [rohan] companions or
					// Eowyn can be spotted; his trigger answers a [rohan] Man exerting, his skirmish ability is used
					// only on purpose
					put("merry", "103_85");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Footman of Dunland: [men] Man, strength 7, vitality 3; the site gains plains
					put("footman", "11_82");
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its other text answers only
					// unbound Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HrethelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Hrethel, Rider of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot a [rohan] Man.
		 * 	<b>Skirmish:</b> Heal a minion skirmishing Hrethel to make Hrethel strength +3 (limit +3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hrethel");

		assertEquals("Hrethel", card.getBlueprint().getTitle());
		assertEquals("Rider of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilARohanManCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(boromir, merry);
		scn.MoveCardsToHand(hrethel, guard);

		scn.StartGame();

		// Boromir is a Man of another culture; Merry, beside fewer than 3 [rohan] companions and no Eowyn, is a
		// [rohan] Hobbit and no Man.
		assertInZone(Zone.HAND, hrethel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hrethel));

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.FreepsPlayAvailable(hrethel));

		scn.FreepsPlayCard(hrethel);
		assertInZone(Zone.FREE_CHARACTERS, hrethel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManAllyIsEnoughToSpotToPlayHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var aldor = scn.GetFreepsCard("aldor");
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(hrethel);

		scn.StartGame();

		// The ally is the only [rohan] Man there is.
		assertInZone(Zone.HAND, hrethel);
		assertTrue(scn.FreepsPlayAvailable(hrethel));

		scn.FreepsPlayCard(hrethel);
		assertInZone(Zone.FREE_CHARACTERS, hrethel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityHealsAMinionSkirmishingHrethelToMakeHimStrongerAndNoOtherMinionCanPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var guard = scn.GetFreepsCard("guard");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCompanionsToTable(hrethel, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, footman);
		scn.AddWoundsToChar(savage1, 1);
		scn.AddWoundsToChar(savage2, 1);
		scn.AddWoundsToChar(footman, 1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(footman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{hrethel, savage1},
				new PhysicalCardImpl[]{guard, savage2}
		);
		scn.ShadowAssignToMinions(hrethel, footman);
		scn.FreepsResolveSkirmish(guard);

		// In the Guard's skirmish, the wounded Savage fights the Guard and not Hrethel, and the two wounded
		// minions assigned to Hrethel are not skirmishing yet: no minion can be healed, and the ability is not
		// offered.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hrethel));

		scn.PassSkirmishActions();
		// The Guard (6) beats the Savage (5), which takes a second wound and lives.
		assertEquals(2, scn.GetWoundsOn(savage2));
		scn.FreepsResolveSkirmish(hrethel);

		assertEquals(6, scn.GetStrength(hrethel));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hrethel));

		scn.FreepsUseCardAction(hrethel);
		// Both minions skirmishing him; not the Savage whose skirmish against the Guard is over.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		// One wound healed, and 6 + 3 = 9.
		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertEquals(9, scn.GetStrength(hrethel));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileNoMinionSkirmishingHimIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(hrethel);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(hrethel, savage1);

		// Nothing to heal: the cost cannot be paid.
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(6, scn.GetStrength(hrethel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hrethel));
	}

	@Test
	public void NoMoreThanPlus3InOneSkirmishThoughTheHealIsStillPaidAndTheLimitIsNewInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(hrethel);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.AddWoundsToChar(pursuer, 2);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(hrethel, pursuer);

		assertEquals(6, scn.GetStrength(hrethel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(hrethel);

		// The Pursuer is the one minion skirmishing him, and is healed unasked.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(9, scn.GetStrength(hrethel));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// At the limit the ability is still offered, and its heal is paid for nothing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hrethel));
		scn.FreepsUseCardAction(hrethel);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		// Still 9 at the next decision of the same skirmish.
		assertEquals(9, scn.GetStrength(hrethel));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Hrethel (9) beats the Pursuer (5) without overwhelming it; it takes a wound and lives, and is fierce.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(hrethel, pursuer);

		// The bonus lasted for that skirmish phase only; the fierce skirmish is a phase of its own, with a limit of
		// its own.
		assertEquals(6, scn.GetStrength(hrethel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hrethel));
		scn.FreepsUseCardAction(hrethel);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(9, scn.GetStrength(hrethel));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
