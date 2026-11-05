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

public class Card_04_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sas", "4_57");
					// Wish For Our Kinfolk: a [dwarven] support-area condition that holds [dwarven] tokens of its own;
					// its abilities are used only on purpose
					put("wish", "8_8");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, strength 3
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2, home 3F; a maneuver ability used only on purpose
					put("thrarin", "1_27");
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
	public void StoutandStrongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Stout and Strong
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time a Dwarf wins a skirmish, you may place a [dwarven] token on this card.
		 * <b>Skirmish:</b> Make an unbound companion strength +1 for each [dwarven] token here (limit +3). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sas");

		assertEquals("Stout and Strong", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnlyADwarfWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sas = scn.GetFreepsCard("sas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage2 = scn.GetShadowCard("savage2");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(sas);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage2, runner1);
		// Strength 8 beats Gimli's 6 without overwhelming him; he takes 2 wounds (damage +1) of his 3 and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, savage2, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage2},
				new PhysicalCardImpl[]{aragorn, runner1});

		// A companion who is no Dwarf wins: nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sas));

		// A Dwarf loses, and lives: nothing is offered.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(savage2));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sas));
		assertEquals(0, scn.GetCultureTokensOn(sas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenIsOfferedEachTimeADwarfWinsAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sas = scn.GetFreepsCard("sas");
		var wish = scn.GetFreepsCard("wish");
		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(sas, wish);
		scn.MoveCompanionsToTable(gimli);
		scn.AddTokensToCard(wish, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(wish));
		// The Pursuer is fierce, so that Gimli beats it twice in the turn.
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);

		// Gimli, 6, beats the Pursuer, 5: offered, and declined.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sas));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(sas));

		// The same Dwarf wins the fierce skirmish: offered again after the refusal, and taken.  The token goes on
		// this card alone, not on the other [dwarven] condition.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		assertInDiscard(pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sas));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(sas));
		assertEquals(2, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sas = scn.GetFreepsCard("sas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var onewhom = scn.GetFreepsCard("onewhom");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(sas);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(onewhom);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin, 4 + 2, takes part in skirmishes until the regroup phase.
		scn.FreepsPlayCard(onewhom);
		assertEquals(6, scn.GetStrength(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, runner1);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();

		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sas));
		assertEquals(0, scn.GetCultureTokensOn(sas));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(sas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAnUnboundCompanionStrengthPlus1ForEachTokenHereAndDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sas = scn.GetFreepsCard("sas");
		var wish = scn.GetFreepsCard("wish");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(sas, wish);
		scn.MoveCompanionsToTable(gimli, aragorn, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.AddTokensToCard(sas, 2);
		// [dwarven] tokens on another card are not "here": counted, they would make it +3 (the limit), not +2.
		scn.AddTokensToCard(wish, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		assertInZone(Zone.SUPPORT, sas);
		assertEquals(2, scn.GetCultureTokensOn(sas));
		assertEquals(3, scn.GetCultureTokensOn(wish));

		// Not in the Free Peoples player's assignment actions, the phase before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sas));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(sas));

		scn.FreepsUseCardAction(sas);
		// Unbound companions in the skirmish and out of it; not the Ring-bound Sam, the Ring-bearer or the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(sas);
		assertEquals(3, scn.GetCultureTokensOn(wish));

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));

		// Gimli, 6, beats the Savage, 5; in the next skirmish, Aragorn's own, the +2 is gone.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(savage1));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void WithFourTokensTheCompanionIsMadeNoMoreThanStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sas = scn.GetFreepsCard("sas");
		var gimli = scn.GetFreepsCard("gimli");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(sas);
		scn.MoveCompanionsToTable(gimli);
		scn.AddTokensToCard(sas, 4);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		assertInZone(Zone.SUPPORT, sas);
		assertEquals(4, scn.GetCultureTokensOn(sas));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage1);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));

		// Gimli is the one unbound companion, and is chosen unasked.
		scn.FreepsUseCardAction(sas);
		assertEquals(9, scn.GetStrength(gimli));
		assertInDiscard(sas);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoTokensTheAbilityIsStillOfferedAndOnlyDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sas = scn.GetFreepsCard("sas");
		var gimli = scn.GetFreepsCard("gimli");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(sas);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sas);
		assertEquals(0, scn.GetCultureTokensOn(sas));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage1);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));

		// The ability discards its own card whatever the count, so it is offered with no token here.
		assertTrue(scn.FreepsActionAvailable(sas));
		scn.FreepsUseCardAction(sas);
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(sas);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
