package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_249_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeking1", "7_249");
					put("seeking2", "7_249");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					// Merry, Master Holbytla: [rohan] Hobbit companion, strength 3; a Man only beside 3 [rohan] companions or
					// Eowyn, and his other texts answer an exertion or are used only on purpose
					put("merry", "103_85");
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, strength 3; an archery ability that needs a villager
					// Well Stored: another [rohan] condition in the support area; at the start of the fellowship phase it may
					// heal a villager, and there is none
					put("stored", "4_296");
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer", "4_190");
					// Uruk Seeker: [isengard] Uruk-hai, fierce, strength 6, vitality 2; stronger only against a bearer of a
					// search card, and there is none
					put("seeker", "4_195");
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play text is not reached when cheated in
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, for a [rohan] Man ally at his home site 3T, where he can be assigned to a skirmish.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeking", "7_249");
					// Rohirrim Herdsman: [rohan] Man ally, strength 4, vitality 2, home 3T; a play requirement passed over by
					// the cheat and a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeekingNewFoesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Seeking New Foes
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Skirmishes are resolved in an order decided by the first Shadow player.
		 * 	Each time a [rohan] Man wins a skirmish, place a [rohan] token here. Each [rohan] Man is strength +1 for each token here.
		 * 	Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seeking1");

		assertEquals("Seeking New Foes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileInPlayTheShadowPlayerDecidesTheOrderOfSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetFreepsCard("seeking1");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard, boromir);
		scn.MoveCardsToSupportArea(seeking);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, seeking);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, runner1},
				new PhysicalCardImpl[]{boromir, runner2}
		);

		// The Shadow player is asked which skirmish comes first, and the Free Peoples player is not.
		assertFalse(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, boromir));
		scn.ShadowResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(boromir));
		assertFalse(scn.IsCharSkirmishing(guard));
		scn.PassSkirmishActions();

		// Boromir (7) beat his Runner (5, vitality 1); the Guard's has not been fought yet.
		assertInDiscard(runner2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);

		// The second skirmish is the Shadow player's to choose as well.
		assertFalse(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		scn.ShadowResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		scn.PassSkirmishActions();
		// The Guard's win sets off the token beside the loser's wound: the Free Peoples player orders the two.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertInDiscard(runner1, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutItTheFreePeoplesPlayerDecidesTheOrderOfSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetFreepsCard("seeking1");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard, boromir);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, guard, boromir);
		assertNotInZone(Zone.SUPPORT, seeking);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, runner1},
				new PhysicalCardImpl[]{boromir, runner2}
		);

		// The same board with no Seeking New Foes in play: the order is the Free Peoples player's (CR 4.2 "skirmish phase(s)").
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertFalse(scn.ShadowDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, boromir));
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachWinOfARohanManPlacesATokenOnEachCopyAndEachTokenMakesEveryRohanManStrongerByOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking1 = scn.GetFreepsCard("seeking1");
		var seeking2 = scn.GetFreepsCard("seeking2");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var aldor = scn.GetFreepsCard("aldor");
		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");
		var runner = scn.GetShadowCard("runner1");
		// Two [rohan] companions, the Guard and Merry: Merry is no Man.
		scn.MoveCompanionsToTable(guard, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToSupportArea(seeking1, seeking2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, seeking1, seeking2);
		// Merry is given the strength to beat a Runner: 3 + 3 against 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, merry, null, 3));
		scn.MoveMinionsToTable(pursuer, seeker, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, pursuer},
				new PhysicalCardImpl[]{boromir, seeker},
				new PhysicalCardImpl[]{merry, runner}
		);

		assertEquals(0, scn.GetCultureTokensOn(seeking1));
		assertEquals(0, scn.GetCultureTokensOn(seeking2));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(aldor));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(merry));

		// A [gondor] Man wins (7 against 6): no token.
		scn.ShadowResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertEquals(0, scn.GetCultureTokensOn(seeking1));
		assertEquals(0, scn.GetCultureTokensOn(seeking2));

		// A [rohan] Hobbit who is no Man wins (6 against 5): no token.
		scn.ShadowResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(0, scn.GetCultureTokensOn(seeking1));
		assertEquals(0, scn.GetCultureTokensOn(seeking2));

		// A [rohan] Man companion wins (6 against 5): a token on each copy.
		scn.ShadowResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// The win sets off each copy beside the loser's wound: the Free Peoples player orders them.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetCultureTokensOn(seeking1));
		assertEquals(1, scn.GetCultureTokensOn(seeking2));
		// Each copy counts its own token: +1 and +1.  Boromir and Merry are no [rohan] Men.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(aldor));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(merry));

		// The fierce round: the Guard wins again against the Pursuer (8 against 5), and each copy has a second token.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, pursuer},
				new PhysicalCardImpl[]{boromir, seeker}
		);
		scn.ShadowResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(2, scn.GetCultureTokensOn(seeking1));
		assertEquals(2, scn.GetCultureTokensOn(seeking2));

		// Read during Boromir's fierce skirmish, with both copies still in play: 6 + 2 + 2, and 3 + 2 + 2.
		scn.ShadowResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(aldor));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(merry));
		assertInZone(Zone.SUPPORT, seeking1, seeking2);
	}

	@Test
	public void ARohanManAllyWhoWinsASkirmishPlacesAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var seeking = scn.GetFreepsCard("seeking");
		var herdsman = scn.GetFreepsCard("herdsman");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.SkipToSite(2);
		// Laid after the first turns, whose regroup phases would have discarded it.
		scn.MoveCardsToSupportArea(seeking);
		assertInZone(Zone.SUPPORT, seeking);
		// The Herdsman is given the strength to beat a Runner: 4 + 2 against 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, herdsman, null, 2));
		scn.MoveMinionsToTable(runner1, runner2);
		// The fellowship moves to Barrows of Edoras, site 3T, the Herdsman's home.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{herdsman, runner1},
				new PhysicalCardImpl[]{boromir, runner2}
		);

		assertEquals(0, scn.GetCultureTokensOn(seeking));
		scn.ShadowResolveSkirmish(herdsman);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInDiscard(runner1);

		// Read during Boromir's skirmish, with the condition still in play.
		scn.ShadowResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(seeking));
		assertInZone(Zone.SUPPORT, seeking);
	}

	@Test
	public void ARohanManWhoLosesPlacesNoTokenAndTheConditionGoesAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetFreepsCard("seeking1");
		var stored = scn.GetFreepsCard("stored");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard, boromir);
		scn.MoveCardsToSupportArea(seeking, stored);
		// One token, as an earlier win would have placed it.
		scn.AddTokensToCard(seeking, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, seeking, stored);
		assertEquals(1, scn.GetCultureTokensOn(seeking));
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, troop},
				new PhysicalCardImpl[]{boromir, runner}
		);
		assertEquals(7, scn.GetStrength(guard));

		// The Guard (7) loses to the Troop (13), under double his strength: one wound, and he lives.
		scn.ShadowResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.FREE_CHARACTERS, guard);

		// During the next skirmish, the phase before regroup: no new token, and the bonus still on.
		scn.ShadowResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(seeking));
		assertInZone(Zone.SUPPORT, seeking);
		assertEquals(7, scn.GetStrength(guard));
		scn.PassSkirmishActions();

		// The regroup phase: the condition is discarded and its bonus gone; the other condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(seeking);
		assertInZone(Zone.SUPPORT, stored);
		assertEquals(6, scn.GetStrength(guard));
	}
}
