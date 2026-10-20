package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soa", "4_82");
					// Final Count: an [elven] support-area condition that takes [elven] tokens of its own when Legolas wins;
					// its strength bonus needs [dwarven] tokens on My Axe Is Notched, which is not in play
					put("finalcount", "4_69");

					put("legolas", "11_21");    // Legolas, Companion of the Ring: Elf companion, strength 6, vitality 3; +2 only at a battleground or forest, which no site of this path is
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability only
					// Golradir, Councilor of Imladris: an Elf ally, strength 4, vitality 3, home 3F; a skirmish ability used only on purpose
					put("golradir", "3_20");
					// One Whom Men Would Follow: exert Aragorn to make an ally strength +2 and take part in skirmishes until the regroup phase
					put("onewhom", "1_109");

					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and no other text
					put("savage2", "1_151");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does nothing when cheated in
					put("pursuer", "4_190");    // Uruk Pursuer: strength 5, vitality 3, printed fierce; its bar and its response reach no Hobbit here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofArmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Strength of Arms
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time an Elf wins a skirmish, you may place an [elven] token on this card.
		 * <b>Skirmish:</b> Make a minion skirmishing an Elf strength -1 for each [elven] token here (limit -3). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soa");

		assertEquals("Strength of Arms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnlyAnElfWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, runner1);
		// Strength 8 beats Arwen's 6 without overwhelming her; she takes 2 wounds of her 3 and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, savage2, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{legolas, savage1},
				new PhysicalCardImpl[]{arwen, savage2},
				new PhysicalCardImpl[]{aragorn, runner1});

		// A companion who is no Elf wins: nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(soa));

		// An Elf loses, and lives: nothing is offered.
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(8, scn.GetStrength(savage2));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(soa));

		// An Elf wins.
		assertEquals(0, scn.GetWoundsOn(savage1));
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(soa));
		assertEquals(0, scn.GetCultureTokensOn(soa));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(soa));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenIsOfferedEachTimeAnElfWinsAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(legolas, arwen);

		scn.StartGame();
		// The Pursuer is fierce, so that Legolas beats it twice in the turn without killing it.
		scn.MoveMinionsToTable(pursuer, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{legolas, pursuer},
				new PhysicalCardImpl[]{arwen, runner1});

		// Arwen, 6, beats the Runner, 5: offered, and declined.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(soa));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(soa));

		// Legolas, 6, beats the Pursuer, 5: offered again after the refusal, and taken.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(soa));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(soa));

		// The same Elf wins the fierce skirmish: a second token.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(legolas, pursuer);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(soa));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(soa));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfAllyWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var aragorn = scn.GetFreepsCard("aragorn");
		var golradir = scn.GetFreepsCard("golradir");
		var onewhom = scn.GetFreepsCard("onewhom");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(onewhom);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		// Golradir, 4 + 2, takes part in skirmishes until the regroup phase.
		scn.FreepsPlayCard(onewhom);
		assertEquals(6, scn.GetStrength(golradir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(golradir, runner1);
		scn.FreepsResolveSkirmish(golradir);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(soa));
		assertEquals(0, scn.GetCultureTokensOn(soa));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(soa));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAMinionSkirmishingAnElfStrengthMinus1ForEachTokenHereAndDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var finalcount = scn.GetFreepsCard("finalcount");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(soa, finalcount);
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AddTokensToCard(soa, 2);
		// [elven] tokens on another card are not "here": counted, they would make it -3 (the limit), not -2.
		scn.AddTokensToCard(finalcount, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, runner1);
		assertInZone(Zone.SUPPORT, soa);
		assertEquals(2, scn.GetCultureTokensOn(soa));
		assertEquals(3, scn.GetCultureTokensOn(finalcount));

		// Not in the Free Peoples player's assignment actions, the phase before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(soa));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{legolas, savage1},
				new PhysicalCardImpl[]{arwen, savage2});
		// The Runner, left over, is put on Legolas by the Shadow player: two minions in one Elf's skirmish.
		scn.ShadowAssignToMinions(legolas, runner1);

		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsActionAvailable(soa));

		scn.FreepsUseCardAction(soa);
		// Both minions skirmishing Legolas, of two cultures and races; not the Savage assigned to Arwen, whose
		// skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, runner1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage1);

		assertEquals(3, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage2));
		assertInDiscard(soa);
		assertEquals(3, scn.GetCultureTokensOn(finalcount));

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(savage1));

		// Legolas, 6, loses to 3 + 5 and lives (2 wounds of 3); in the next skirmish the -2 is gone.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
	}

	@Test
	public void WithFourTokensTheMinionIsMadeNoMoreThanStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var legolas = scn.GetFreepsCard("legolas");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(legolas);
		scn.AddTokensToCard(soa, 4);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		assertInZone(Zone.SUPPORT, soa);
		assertEquals(4, scn.GetCultureTokensOn(soa));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, pursuer);
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(5, scn.GetStrength(pursuer));

		// The one minion skirmishing an Elf is chosen unasked.
		scn.FreepsUseCardAction(soa);
		assertEquals(2, scn.GetStrength(pursuer));
		assertInDiscard(soa);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoTokensTheAbilityIsStillOfferedAndOnlyDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var legolas = scn.GetFreepsCard("legolas");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, soa);
		assertEquals(0, scn.GetCultureTokensOn(soa));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage1);
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(5, scn.GetStrength(savage1));

		// The ability discards its own card whatever the count, so it is offered with no token here.
		assertTrue(scn.FreepsActionAvailable(soa));
		scn.FreepsUseCardAction(soa);
		assertEquals(5, scn.GetStrength(savage1));
		assertInDiscard(soa);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingACompanionWhoIsNoElfIsNotReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AddTokensToCard(soa, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, soa);
		assertEquals(2, scn.GetCultureTokensOn(soa));
		scn.MoveMinionsToTable(savage1, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{legolas, savage1});

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage1));

		// The Runner is the only minion in this skirmish, against a Man: it is not reached, nor is the Savage
		// waiting for Legolas; the condition is discarded all the same.
		assertTrue(scn.FreepsActionAvailable(soa));
		scn.FreepsUseCardAction(soa);
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage1));
		assertInDiscard(soa);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAnElfAllyIsMadeStrengthMinus1ForOneToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soa = scn.GetFreepsCard("soa");
		var aragorn = scn.GetFreepsCard("aragorn");
		var golradir = scn.GetFreepsCard("golradir");
		var onewhom = scn.GetFreepsCard("onewhom");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(soa);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(onewhom);
		scn.AddTokensToCard(soa, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, soa);
		assertEquals(1, scn.GetCultureTokensOn(soa));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(onewhom);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(golradir, savage1);
		scn.FreepsResolveSkirmish(golradir);
		assertEquals(5, scn.GetStrength(savage1));

		scn.FreepsUseCardAction(soa);
		assertEquals(4, scn.GetStrength(savage1));
		assertInDiscard(soa);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
