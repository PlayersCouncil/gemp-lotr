package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_050_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("protect", "53_50");
					put("protect2", "53_50");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3, no ability to use
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, damage +1
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3
					put("weary", "4_212");      // Weary: another [isengard] condition for a companion or ally; it does nothing unless its bearer is assigned to an [isengard] tracker

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6, vitality 2
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CanYouProtectMeFromYourselfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Can You Protect Me From Yourself?
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: Bearer must be a companion or ally. Limit 1 per bearer.
		 * Each time bearer wins a skirmish against an [isengard] minion, add a burden.
		 * At the start of the regroup phase, the Free Peoples player may exert bearer twice to discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("protect");

		assertEquals("Can You Protect Me From Yourself?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnACompanionOrAllyNotAlreadyBearingOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var protect2 = scn.GetShadowCard("protect2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(protect2);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, protect);
		// Boromir bears another [isengard] condition, which is no copy of this one.
		scn.AttachCardsTo(boromir, weary);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(protect2));

		scn.ShadowPlayCard(protect2);
		// Companions and an ally, Boromir among them for all that he bears Weary; not Aragorn, who already bears
		// a copy, nor the Shadow player's own minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, frodo, grimir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boromir);

		assertAttachedTo(protect2, boromir);
		assertNotAttachedTo(protect2, grimir);
		assertAttachedTo(weary, boromir);
		assertAttachedTo(protect, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachBearerWinningAgainstAnIsengardMinionOfEitherRaceAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var protect2 = scn.GetShadowCard("protect2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, protect);
		scn.AttachCardsTo(boromir, protect2);
		scn.MoveMinionsToTable(savage, forger);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{boromir, forger});

		assertEquals(0, scn.GetBurdens());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The burden answers the win before the skirmish's wound, with nothing to order (engine pass 54).
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		// Strength 8 against the Uruk-hai's 5.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// Strength 7 against the Orc's 6.
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("exert"));
	}

	@Test
	public void NoBurdenForANonIsengardMinionForALossOrForACompanionWithoutTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var protect2 = scn.GetShadowCard("protect2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sentry = scn.GetShadowCard("sentry");
		var raiders = scn.GetShadowCard("raiders");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, protect);
		scn.AttachCardsTo(boromir, protect2);
		scn.MoveMinionsToTable(sentry, raiders, savage);
		// Aragorn made stronger than the strength-8 Sentry.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sentry}, new PhysicalCardImpl[]{boromir, raiders},
				new PhysicalCardImpl[]{gimli, savage});

		assertEquals(0, scn.GetBurdens());
		// The bearer wins against a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetBurdens());

		// The bearer loses to an [isengard] Uruk-hai: strength 7 against 9, damage +1.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetBurdens());

		// A companion without the condition wins against an [isengard] Uruk-hai: 6 against 5, damage +1.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("exert"));
	}

	@Test
	public void WinningANormalAndAFierceSkirmishAddsABurdenEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, protect);
		scn.MoveMinionsToTable(savage);
		// The Savage made fierce, so that Aragorn fights it twice in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetBurdens());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("exert"));
	}

	@Test
	public void AtTheStartOfRegroupTheFreePeoplesPlayerMayExertEachBearerTwiceToDiscardItsCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var protect2 = scn.GetShadowCard("protect2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, protect);
		scn.AttachCardsTo(boromir, protect2);
		// No minion: the game goes from the Shadow phase to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowDecisionAvailable("exert"));
		assertTrue(scn.FreepsDecisionAvailable("exert"));
		// Each copy asks of its own bearer, one after the other.
		assertTrue(scn.FreepsDecisionAvailable("Aragorn"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(protect);
		assertAttachedTo(protect2, boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));

		assertTrue(scn.FreepsDecisionAvailable("Boromir"));
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertAttachedTo(protect2, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotAskedAtTheStartOfManeuverNorOfABearerWhoCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var protect2 = scn.GetShadowCard("protect2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, protect);
		scn.AttachCardsTo(boromir, protect2);
		// Vitality 4 with 2 wounds: 2 left, one exertion short of two.
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsDecisionAvailable("exert"));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToPhase(Phase.REGROUP);

		// Boromir, vitality 3 and unwounded, can exert twice; Aragorn cannot.
		assertTrue(scn.FreepsDecisionAvailable("Boromir"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInDiscard(protect2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertAttachedTo(protect, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
