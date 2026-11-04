package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("meal", "18_83");
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9, vitality 2; +2 only at an underground site
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, strength 5, vitality 1; its play trigger wants an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8, vitality 2; a start-of-assignment trigger
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, strength 5; its trigger is on being played

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, no archer; a maneuver ability only
					// Garrison of Gondor: [gondor] Man, strength 8, Knight and Archer; defender only beside a fortification
					put("garrison", "8_36");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GruesomeMealStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gruesome Meal
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a companion skirmishing an [orc] minion strength -2 (or -3 if he or she is an archer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("meal");

		assertEquals("Gruesome Meal", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheCompanionSkirmishingAnOrcMinionStrengthMinus2ForThatSkirmishWhileAnArcherWaits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meal = scn.GetShadowCard("meal");
		var denizen = scn.GetShadowCard("denizen");
		var goblin = scn.GetShadowCard("goblin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn, garrison);
		scn.MoveCardsToHand(meal);
		assertInHand(meal);

		scn.StartGame();
		// One minion during the archery phase, so that the Garrison's archery wound goes on it unasked.
		scn.MoveMinionsToTable(denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(1, scn.GetWoundsOn(denizen));
		scn.MoveMinionsToTable(goblin);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{garrison, goblin});

		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(garrison));
		assertTrue(scn.ShadowPlayAvailable(meal));

		// Aragorn is the one companion skirmishing: chosen unasked.  The Garrison, an archer, is assigned to an [orc]
		// minion whose skirmish has not begun, and is untouched; and Aragorn, who is no archer, takes -2 though an
		// archer is in play.
		scn.ShadowPlayCard(meal);
		assertInDiscard(meal);
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(garrison));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The next skirmish is a phase of its own: the -2 is gone.
		scn.FreepsResolveSkirmish(garrison);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(garrison));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnArcherCompanionSkirmishingAnOrcMinionWhoIsNoOrcIsStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meal = scn.GetShadowCard("meal");
		var halftroll = scn.GetShadowCard("halftroll");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(garrison);
		scn.MoveCardsToHand(meal);
		assertInHand(meal);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(garrison, halftroll);
		scn.FreepsResolveSkirmish(garrison);
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(garrison));

		scn.ShadowPlayCard(meal);
		assertInDiscard(meal);
		assertEquals(5, scn.GetStrength(garrison));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meal = scn.GetShadowCard("meal");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(meal);
		assertInHand(meal);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(meal));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(meal));
	}

	@Test
	public void InTheSkirmishOfACompanionAgainstAnOrcOfAnotherCultureItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meal = scn.GetShadowCard("meal");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(meal);
		assertInHand(meal);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));

		// An event's target is no requirement: it is offered, and played for nothing.
		assertTrue(scn.ShadowPlayAvailable(meal));
		scn.ShadowPlayCard(meal);
		assertInDiscard(meal);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnAllySkirmishingAnOrcMinionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meal = scn.GetShadowCard("meal");
		var denizen = scn.GetShadowCard("denizen");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(meal);
		assertInHand(meal);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, denizen);
		scn.FreepsResolveSkirmish(thrarin);
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(4, scn.GetStrength(thrarin));

		// The ally is no companion.
		assertTrue(scn.ShadowPlayAvailable(meal));
		scn.ShadowPlayCard(meal);
		assertInDiscard(meal);
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
