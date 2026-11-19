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

public class Card_15_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("path", "15_65");
					put("path2", "15_65");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a Maneuver ability only
					put("aragorn", "1_89");
					// Aragorn, Defender of Rohan: the [rohan] Aragorn, strength 8; his text needs the Ring-bearer assigned
					put("rohanaragorn", "17_93");
					// Gimli, Eager Hunter: hunter [dwarven] Dwarf companion, strength 6, damage +1, hunter 3, no other text
					put("gimli", "15_5");
					// Legolas, Fleet-footed Hunter: hunter [elven] Elf companion, strength 6, hunter 1; a Regroup ability used only on purpose
					put("legolas", "15_18");
					put("farin", "1_11");       // Farin, Dwarven Emissary: a [dwarven] companion who is no hunter; +2 while skirmishing an Orc
					put("lorien", "1_53");      // Lórien Elf: an [elven] companion who is no hunter, no other text
					// Mablung, Ranger of Ithilien: a hunter companion of another culture ([gondor]); his Skirmish ability is used only on purpose
					put("mablung", "15_63");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, no Aragorn; a Skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; Knight is his whole text
					// Gondorian Merchant: the [gondor] Man ally, strength 6; a Maneuver ability used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a Skirmish ability used only on purpose
					put("pippin", "103_31");
					// Elite Rider: a Man companion of another culture ([rohan]), strength 6; +2 only while skirmishing a wounded minion
					put("rider", "4_265");

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
	public void NoQuickerPathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: No Quicker Path
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time Aragorn wins a skirmish, you may make a hunter [dwarven] companion or hunter [elven] companion strength +1 until the regroup phase.
		 * 	<b>Skirmish:</b> Discard this condition to make a [gondor] Man strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("path");

		assertEquals("No Quicker Path", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAragornWinsAHunterDwarvenOrElvenCompanionMayBeMadeStrengthPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var farin = scn.GetFreepsCard("farin");
		var lorien = scn.GetFreepsCard("lorien");
		var mablung = scn.GetFreepsCard("mablung");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas, farin, lorien, mablung);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{gimli, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 8 against 5: Aragorn wins.
		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(path));
		scn.FreepsAcceptOptionalTrigger();

		// A hunter of either culture; not a [dwarven] or [elven] companion who is no hunter, nor a hunter of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(farin, lorien, mablung, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsChooseCard(gimli);

		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));

		// Still so in the skirmish that follows: 6 + 1 + 3 (hunter, against the Runner, who is no hunter).
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(gimli));

		// Gimli wins, and the regroup phase begins.
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void NothingIsOfferedWhenAragornLosesOrWhenAnotherGondorCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, legolas);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner}, new PhysicalCardImpl[]{aragorn, troop});

		// 7 against 5: Boromir, a [gondor] Man who is no Aragorn, wins.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(path));

		// 8 against 13: Aragorn loses, and lives with a wound.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(path));

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoHunterDwarvenOrElvenCompanionAragornWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var lorien = scn.GetFreepsCard("lorien");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, farin, lorien);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The offer would reach nobody, and is not made.
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(path));
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(4, scn.GetStrength(lorien));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheRohanAragornWinsTheOfferMayBeDeclinedLeavingEveryHunterAsHeWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 8 against 5: the [rohan] Aragorn wins, and is an Aragorn as much as any.
		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(path));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeAragornWinsTheOfferIsMadeAgainAndTheBonusesAddUp() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var path = scn.GetFreepsCard("path");
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer}, new PhysicalCardImpl[]{legolas, pursuer2});
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.PassSkirmishActions();

		// 8 against 5: Aragorn wins, and the Pursuer lives with a wound.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(path));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(6, scn.GetStrength(legolas));
		scn.FreepsChooseCard(legolas);
		assertEquals(7, scn.GetStrength(legolas));

		// 7 + 1 (hunter) against 5: Legolas wins, and the second Pursuer lives with a wound.
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer2));

		// Both fierce Pursuers are fought again, Aragorn's skirmish first, and Aragorn wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer}, new PhysicalCardImpl[]{legolas, pursuer2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(path));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);

		// The two bonuses add up, and are both still in force in Legolas' fierce skirmish: 6 + 1 + 1, and hunter 1 there.
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(legolas));
	}

	@Test
	public void SkirmishAbilityDiscardsThisConditionToMakeAGondorManStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var path = scn.GetFreepsCard("path");
		var path2 = scn.GetFreepsCard("path2");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir, knight, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(path, path2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, path, path2);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		// The Gondorian Knight is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner}, new PhysicalCardImpl[]{rider, runner2});
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);

		// A [gondor] Man in the skirmish or out of it, companion or ally; not a [gondor] companion who is no Man,
		// nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertInDiscard(path);
		assertInZone(Zone.SUPPORT, path2);
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(boromir));

		// Each skirmish is a phase of its own: in the Rider's, Boromir is his printed 7.
		scn.FreepsPass();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(path);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(path));
		assertInZone(Zone.SUPPORT, path);
	}
}
