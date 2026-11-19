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

public class Card_15_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("end", "15_10");
					put("end2", "15_10");
					// Gimli, Eager Hunter: [dwarven] Dwarf companion, strength 6, vitality 3, damage +1, hunter 3, no other
					// text.  A hunter companion who is neither [gondor] nor [elven].
					put("gimli", "15_5");
					put("farin", "1_11");       // Farin, Dwarven Emissary: [dwarven] Dwarf companion who is no Gimli; strength 5, +2 while skirmishing an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally; a Fellowship ability only
					// Mablung, Ranger of Ithilien: hunter [gondor] Man companion, strength 5, hunter 1; his Skirmish ability needs a roaming minion
					put("mablung", "15_63");
					// Legolas, Fleet-footed Hunter: hunter [elven] Elf companion, strength 6, hunter 1; a Regroup ability used only on purpose
					put("legolas", "15_18");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion who is no hunter; a Skirmish ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: an [elven] companion who is no hunter, no other text

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("pursuer", "4_190");    // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3
					put("pursuer2", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhateverEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Whatever End
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time Gimli wins a skirmish, you may make a hunter [gondor] companion or hunter [elven] companion <b>damage +1</b> until the regroup phase.<br><b>Skirmish:</b> Discard this condition to make a [dwarven] companion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("end");

		assertEquals("Whatever End", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenGimliWinsAHunterGondorOrElvenCompanionMayBeMadeDamagePlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var mablung = scn.GetFreepsCard("mablung");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var lorien = scn.GetFreepsCard("lorien");
		var end = scn.GetFreepsCard("end");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gimli, mablung, legolas, boromir, lorien);
		scn.MoveCardsToSupportArea(end);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{mablung, runner2});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// 9 (hunter 3) against 5: Gimli wins.
		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(end));
		scn.FreepsAcceptOptionalTrigger();

		// A hunter of either culture; not a [gondor] or [elven] companion who is no hunter, nor a hunter of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mablung, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, lorien, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
		scn.FreepsChooseCard(mablung);

		assertEquals(1, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Still so in the skirmish that follows.
		scn.FreepsResolveSkirmish(mablung);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(mablung, Keyword.DAMAGE));

		// 6 (hunter 1) against 5: Mablung wins, and the regroup phase begins.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
	}

	@Test
	public void NothingIsOfferedWhenGimliLosesOrWhenAnotherDwarvenCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var mablung = scn.GetFreepsCard("mablung");
		var legolas = scn.GetFreepsCard("legolas");
		var end = scn.GetFreepsCard("end");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, farin, mablung, legolas);
		scn.MoveCardsToSupportArea(end);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{farin, runner}, new PhysicalCardImpl[]{gimli, troop});

		// 7 against 5: Farin, a [dwarven] companion who is no Gimli, wins.
		scn.FreepsResolveSkirmish(farin);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(end));

		// 9 against 13: Gimli loses, and lives with a wound.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(end));

		assertEquals(0, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoHunterGondorOrElvenCompanionGimliWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var lorien = scn.GetFreepsCard("lorien");
		var end = scn.GetFreepsCard("end");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, boromir, lorien);
		scn.MoveCardsToSupportArea(end);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The offer would reach nobody, and is not made.
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(end));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lorien, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferWhenGimliWinsLeavesEveryHunterAsHeWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var mablung = scn.GetFreepsCard("mablung");
		var legolas = scn.GetFreepsCard("legolas");
		var end = scn.GetFreepsCard("end");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, mablung, legolas);
		scn.MoveCardsToSupportArea(end);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(end));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeGimliWinsTheOfferIsMadeAgainAndTheBonusesAddUp() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var mablung = scn.GetFreepsCard("mablung");
		var legolas = scn.GetFreepsCard("legolas");
		var end = scn.GetFreepsCard("end");
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCompanionsToTable(gimli, mablung, legolas);
		scn.MoveCardsToSupportArea(end);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, pursuer}, new PhysicalCardImpl[]{mablung, pursuer2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.PassSkirmishActions();

		// 9 against 5: Gimli wins.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(end));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(mablung);
		assertEquals(1, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
		// Gimli's damage +1: two wounds on the Pursuer, who lives.
		assertEquals(2, scn.GetWoundsOn(pursuer));

		// 6 against 5: Mablung wins, and his new damage +1 places two wounds; the second Pursuer lives too.
		scn.FreepsResolveSkirmish(mablung);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer2));

		// Both fierce Pursuers are fought again, Gimli's skirmish first, and Gimli wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, pursuer}, new PhysicalCardImpl[]{mablung, pursuer2});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertInDiscard(pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(end));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(mablung);

		// The two bonuses add up, and are both still in force in Mablung's fierce skirmish.
		assertEquals(2, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		scn.FreepsResolveSkirmish(mablung);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetKeywordCount(mablung, Keyword.DAMAGE));
	}

	@Test
	public void SkirmishAbilityDiscardsThisConditionToMakeADwarvenCompanionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var mablung = scn.GetFreepsCard("mablung");
		var end = scn.GetFreepsCard("end");
		var end2 = scn.GetFreepsCard("end2");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gimli, farin, mablung);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(end, end2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, end, end2);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		// Gimli is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{farin, runner}, new PhysicalCardImpl[]{mablung, runner2});
		scn.FreepsResolveSkirmish(farin);

		// 5, +2 while skirmishing an Orc.
		assertEquals(7, scn.GetStrength(farin));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(end));
		scn.FreepsUseCardAction(end);

		// A [dwarven] companion in the skirmish or out of it; not a [dwarven] ally, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, mablung));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertInDiscard(end);
		assertInZone(Zone.SUPPORT, end2);
		assertEquals(9, scn.GetStrength(farin));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(farin));

		// Each skirmish is a phase of its own: in Mablung's, Farin, skirmishing nobody, is his printed 5.
		scn.FreepsPass();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(mablung);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(farin));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var end = scn.GetFreepsCard("end");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(end);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(end));
		assertInZone(Zone.SUPPORT, end);
	}
}
