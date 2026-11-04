package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("means", "13_121");

					put("denizen", "11_115");    // Denizen of Khazad-dûm: an [orc] Orc, strength 9; +2 only at an underground site
					put("moria", "11_116");      // Denizen of Moria: an [orc] Orc of another title, strength 7; +3 only at an underground site
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; its start-of-assignment trigger
					// needs more companions than minions, never so here
					put("halftroll", "14_12");
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, strength 13, no text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Glóin: a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhateverMeansStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Whatever Means
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [orc] Orc strength +2 (or +6 if you remove 2 burdens).
		*/

		var scn = GetScenario();

		var means = scn.GetFreepsCard("means");

		assertEquals("Whatever Means", means.getBlueprint().getTitle());
		assertNull(means.getBlueprint().getSubtitle());
		assertFalse(means.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, means.getBlueprint().getSide());
		assertEquals(Culture.ORC, means.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, means.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(means, Timeword.SKIRMISH));
		assertEquals(0, means.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnOrcCultureOrcStrengthPlus2WithNoBurdenToRemoveAndNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var means = scn.GetShadowCard("means");
		var denizen = scn.GetShadowCard("denizen");
		var moria = scn.GetShadowCard("moria");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(means);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, moria, halftroll, troop);
		scn.SkipToAssignments();
		// The Denizen, the Half-troll and the Troop on Aragorn; Denizen of Moria waits for Gimli's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, moria});
		scn.ShadowAssignToMinions(aragorn, halftroll, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(7, scn.GetStrength(moria));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));

		assertTrue(scn.ShadowPlayAvailable(means));
		scn.ShadowPlayCard(means);
		assertFalse(scn.ShadowDecisionAvailable("remove 2 burdens"));

		// An [orc] Orc in the skirmish and one waiting for another; not an [orc] Half-troll, nor a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, moria));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(7, scn.GetStrength(moria));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(means);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovingTwoBurdensMakesItPlus6UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var means = scn.GetShadowCard("means");
		var denizen = scn.GetShadowCard("denizen");
		var moria = scn.GetShadowCard("moria");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(means);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(denizen, moria, halftroll, troop);
		scn.SkipToAssignments();
		// The Denizen, the Half-troll and the Troop on Aragorn; Denizen of Moria waits for Gimli's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, moria});
		scn.ShadowAssignToMinions(aragorn, halftroll, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(moria));

		scn.ShadowPlayCard(means);
		assertTrue(scn.ShadowDecisionAvailable("remove 2 burdens"));
		scn.ShadowChooseYes();
		assertEquals(1, scn.GetBurdens());
		scn.ShadowChooseCard(moria);

		// 7 + 6, in Aragorn's skirmish, though Denizen of Moria is in none yet.
		assertEquals(13, scn.GetStrength(moria));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Gimli's, it is back to 7.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(moria));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoBurdensTheRemovalMayBeDeclinedForPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var means = scn.GetShadowCard("means");
		var denizen = scn.GetShadowCard("denizen");
		var moria = scn.GetShadowCard("moria");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(means);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(denizen, moria, halftroll, troop);
		scn.SkipToAssignments();
		// The Denizen, the Half-troll and the Troop on Aragorn; Denizen of Moria waits for Gimli's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, moria});
		scn.ShadowAssignToMinions(aragorn, halftroll, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(denizen));

		scn.ShadowPlayCard(means);
		assertTrue(scn.ShadowDecisionAvailable("remove 2 burdens"));
		scn.ShadowChooseNo();
		scn.ShadowChooseCard(denizen);

		assertEquals(2, scn.GetBurdens());
		assertEquals(11, scn.GetStrength(denizen));
		assertInDiscard(means);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneBurdenItIsPlus2AndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var means = scn.GetShadowCard("means");
		var denizen = scn.GetShadowCard("denizen");
		var moria = scn.GetShadowCard("moria");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(means);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(denizen, moria, halftroll, troop);
		scn.SkipToAssignments();
		// The Denizen, the Half-troll and the Troop on Aragorn; Denizen of Moria waits for Gimli's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, moria});
		scn.ShadowAssignToMinions(aragorn, halftroll, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(denizen));

		scn.ShadowPlayCard(means);
		assertFalse(scn.ShadowDecisionAvailable("remove 2 burdens"));
		scn.ShadowChooseCard(denizen);

		assertEquals(1, scn.GetBurdens());
		assertEquals(11, scn.GetStrength(denizen));
		assertInDiscard(means);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoOrcCultureOrcToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var means = scn.GetShadowCard("means");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(means);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, halftroll);
		scn.ShadowAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));

		// The target is no requirement: the event is played, and nobody changes strength.
		assertTrue(scn.ShadowPlayAvailable(means));
		scn.ShadowPlayCard(means);
		assertInDiscard(means);
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var means = scn.GetShadowCard("means");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(means);

		scn.StartGame();
		assertInHand(means);
		scn.MoveMinionsToTable(denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInHand(means);
		assertFalse(scn.ShadowPlayAvailable(means));
	}
}
