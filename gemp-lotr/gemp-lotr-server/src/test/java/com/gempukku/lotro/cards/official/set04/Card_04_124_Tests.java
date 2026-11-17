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

public class Card_04_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("help", "4_124");
					// Unbound [gondor] Men: Aragorn, Ranger of the North (strength 8, vitality 4; a maneuver ability used
					// only on purpose) and Boromir, Son of Denethor (strength 7, vitality 3; a skirmish ability used only
					// on purpose)
					put("aragorn", "1_89");
					put("boromir", "1_97");
					// Damrod, Ranger of Ithilien: a [gondor] Man who is Ring-bound, vitality 3; a skirmish ability used
					// only on purpose
					put("damrod", "4_114");
					// Pippin, Trollslayer: an unbound [gondor] companion who is a Hobbit, vitality 4; a skirmish ability
					// used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: a [gondor] Man who is an ally, vitality 1, made 2 by Narsil so that he can exert
					put("merchant", "7_97");
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1, "Bearer must be a [gondor] Man"
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (no Man beside fewer than 3 [rohan]
					// companions); his heal answers another [rohan] Man's exertion, and his skirmish ability is used only
					// on purpose
					put("merry", "103_85");
					// Rohirrim Herdsman: a [rohan] Man who is an ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; "when you play" does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelpinDoubtandNeedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Help in Doubt and Need
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Exert an unbound [gondor] Man to make a [rohan] companion strength +2, or exert a [rohan] companion to make an unbound [gondor] Man strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("help");

		assertEquals("Help in Doubt and Need", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAnUnboundGondorManMakesARohanCompanionStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var damrod = scn.GetFreepsCard("damrod");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir, damrod, pippin, guard, merry);
		scn.MoveAlliesToTable(merchant, herdsman);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(help);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, troop },
				new PhysicalCardImpl[] { damrod, runner }
		);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(help));
		scn.FreepsUseCardAction(help);

		// The cost: either kind may exert, so both alternatives are offered at once.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, guard, merry));
		// A Ring-bound [gondor] Man, a [gondor] Hobbit, a [gondor] Man ally, a [rohan] ally and a [shire] companion
		// are no candidate of either kind.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(damrod, pippin, merchant, herdsman, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The [rohan] companions of two races, one of them not in this skirmish; not the [rohan] ally, nor the others.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, aragorn, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(damrod);
		// A skirmish phase of its own: the +2 is gone.
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExertingARohanCompanionMakesAnUnboundGondorManStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var damrod = scn.GetFreepsCard("damrod");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir, damrod, pippin, guard, merry);
		scn.MoveAlliesToTable(merchant, herdsman);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(help);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, troop },
				new PhysicalCardImpl[] { damrod, runner }
		);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsUseCardAction(help);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Unbound [gondor] Men, one of them not in this skirmish; not the Ring-bound one, the Hobbit, the ally,
		// nor the [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(damrod, pippin, merchant, guard, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 10 against 13: Aragorn loses, 1 wound. In the next skirmish the +2 is gone.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(damrod);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheOnlyUnboundGondorManExhaustedARohanCompanionExertsForHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(help);
		// Vitality 4 with 3 wounds: exhausted, and no longer able to pay the first alternative.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(help));
		scn.FreepsUseCardAction(help);

		// Nothing is asked: the Guard is the one who can exert, and Aragorn the one unbound [gondor] Man.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionAnUnboundGondorManExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var aragorn = scn.GetFreepsCard("aragorn");
		var damrod = scn.GetFreepsCard("damrod");
		var herdsman = scn.GetFreepsCard("herdsman");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, damrod);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(help);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(damrod));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsActionAvailable(help));
		scn.FreepsUseCardAction(help);

		// Aragorn is the one who can pay; with no [rohan] companion the +2 goes to nobody.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(damrod));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoUnboundGondorManARohanCompanionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var guard = scn.GetFreepsCard("guard");
		var damrod = scn.GetFreepsCard("damrod");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard, damrod, pippin);
		scn.MoveCardsToSupportArea(help);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(damrod));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsActionAvailable(help));
		scn.FreepsUseCardAction(help);

		// The Guard is the one who can pay; with no unbound [gondor] Man the +2 goes to nobody.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(damrod));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNeitherAnUnboundGondorManNorARohanCompanionAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var damrod = scn.GetFreepsCard("damrod");
		var pippin = scn.GetFreepsCard("pippin");
		var herdsman = scn.GetFreepsCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, guard, damrod, pippin);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(help);
		// Each exhausted (vitality 4 with 3 wounds, 3 with 2); Damrod, Pippin and the Herdsman could still exert.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(help));
	}

	@Test
	public void NotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var help = scn.GetFreepsCard("help");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(help);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(help));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
	}
}
