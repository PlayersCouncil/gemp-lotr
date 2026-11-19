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

public class Card_15_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "15_7");
					put("axe2", "15_7");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, 6/3, damage +1; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F; a fellowship ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, 4/2; "To play, spot a Dwarf" only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, 8/4; a maneuver ability used only on purpose
					put("bracers", "2_3");      // Dwarven Bracers: [dwarven] possession, cost 1; a response used only on purpose
					// Chamber of Records: [dwarven] support-area condition, cost 2; "When you play this, you may spot a Dwarf
					// to place 2 [dwarven] tokens here"; a fellowship ability used only on purpose
					put("chamber", "15_3");
					// Run Until Found: [dwarven] support-area condition, cost 0, that takes a [dwarven] token as it is played
					put("run", "18_2");
					// Wish For Our Kinfolk: [dwarven] support-area condition whose Fellowship ability adds [dwarven] tokens to it
					put("wish", "8_8");
					// A Path Appointed: [gondor] support-area condition whose Fellowship ability adds [gondor] tokens to it
					put("path", "8_42");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, 5/1; its play trigger does nothing when cheated in
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, 5/3; texts about unbound Hobbits only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeavyAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Heavy Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 4
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be a Dwarf.<br>This is twilight cost -1 for each [dwarven] card that has a culture token on it.<br><b>Skirmish:</b> Remove a [dwarven] token to make bearer strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Heavy Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void ADwarfCompanionOrDwarfAllyMayBearItAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(axe);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(axe, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwilightCostIsOneLessForEachDwarvenCardWithACultureTokenOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var gimli = scn.GetFreepsCard("gimli");
		var bracers = scn.GetFreepsCard("bracers");
		var chamber = scn.GetFreepsCard("chamber");
		var run = scn.GetFreepsCard("run");
		var wish = scn.GetFreepsCard("wish");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(wish, path);
		scn.MoveCardsToHand(axe, axe2, bracers, chamber, run);

		scn.StartGame();
		scn.AddTokensToCard(path, 2);
		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertInHand(axe, axe2, bracers, chamber, run);
		scn.FreepsRefreshDecision();

		// A [dwarven] card with no token, and a token on a [gondor] card: nothing off.
		assertEquals(4, scn.GetTwilightCost(axe));
		assertEquals(4, scn.GetTwilightCost(axe2));
		assertEquals(1, scn.GetTwilightCost(bracers));

		// Chamber of Records takes 2 tokens as it is played: one card, so 1 off.
		scn.FreepsPlayCard(chamber);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chamber));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertEquals(3, scn.GetTwilightCost(axe));
		assertEquals(3, scn.GetTwilightCost(axe2));

		// Run Until Found takes 1 as it is played: two cards, 2 off.  Each copy takes its own discount and no other's.
		scn.FreepsPlayCard(run);
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertEquals(2, scn.GetTwilightCost(axe));
		assertEquals(2, scn.GetTwilightCost(axe2));
		assertEquals(1, scn.GetTwilightCost(bracers));

		// 2 for the Chamber + 0 for Run Until Found, then 4 - 2 for the Axe.
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesADwarvenTokenToMakeBearerStrengthPlus1UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wish = scn.GetFreepsCard("wish");
		var run = scn.GetFreepsCard("run");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(wish, run, path);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		scn.AddTokensToCard(wish, 1);
		scn.AddTokensToCard(run, 1);
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(runner, pursuer);

		// The assignment phase is no skirmish phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(axe));
		scn.PassCurrentPhaseActions();

		// The bearer is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{guard, pursuer});
		scn.FreepsResolveSkirmish(aragorn);

		// 6 + 3 for the Axe.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(axe));
		scn.FreepsUseCardAction(axe);
		// A [dwarven] token from either card; not the [gondor] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wish, run));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path));
		scn.FreepsChooseCard(run);

		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(0, scn.GetCultureTokensOn(run));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(10, scn.GetStrength(gimli));
		// Another Dwarf, who does not bear it.
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoDwarvenTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var wish = scn.GetFreepsCard("wish");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(wish, path);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		scn.AddTokensToCard(path, 2);
		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(2, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(axe));
		assertEquals(9, scn.GetStrength(gimli));
	}
}
