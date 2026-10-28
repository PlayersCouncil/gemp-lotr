package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrage", "12_82");
					put("denizen", "11_115");   // Denizen of Khazad-dûm: [orc] Orc, strength 9, vitality 2; +2 only at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, strength 8, vitality 2; plays an [orc] Orc
					                            // from the discard pile at the start of an assignment phase, if companions outnumber minions
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its other text is about unbound Hobbits

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; needs a Dwarf to play, so cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarrageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Barrage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Toil 2</b>. <helper>(For each [orc] character you exert when playing this, its twilight cost is -2.)</helper><br>Make each of your [orc] minions strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var barrage = scn.GetFreepsCard("barrage");

		assertEquals("Barrage", barrage.getBlueprint().getTitle());
		assertNull(barrage.getBlueprint().getSubtitle());
		assertFalse(barrage.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, barrage.getBlueprint().getSide());
		assertEquals(Culture.ORC, barrage.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, barrage.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(barrage, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(barrage, Keyword.TOIL));
		assertTrue(scn.HasTimeword(barrage, Timeword.SKIRMISH));
		assertEquals(5, barrage.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachOrcCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrage = scn.GetShadowCard("barrage");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(barrage);
		// Three minions to two companions: the Half-troll's own trigger stays out of the way.
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		assertInHand(barrage);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 5 - 2 - 2 is the least it can cost, with both [orc] minions exerted; the [moria] Troop does not count.
		// Toil 1 would want 3, and toil 3 (or the Troop counted) would let 0 do.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(barrage));
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(barrage));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(barrage);
		// The pool is 2 short of its 5: one exertion is owed, and the second is the Shadow player's to make or not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(halftroll);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 3 - (5 - 2)
		assertEquals(0, scn.GetTwilight());

		// The exerted Half-troll is made stronger with the rest.
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(halftroll));
		assertInDiscard(barrage);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesEachOfYourOrcMinionsStrengthPlus2WhetherOrNotItIsInTheSkirmishAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrage = scn.GetShadowCard("barrage");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(barrage);
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		assertInHand(barrage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		// Frodo 1_290 is 3, and 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		scn.ShadowPlayCard(barrage);
		// The pool pays in full: toil still asks, from 0, and nobody is exerted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));

		// The [orc] Orc in the skirmish and the [orc] Half-troll in none are each 2 stronger;
		// the [moria] Orc and the companions are not.
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(halftroll));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertInDiscard(barrage);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrage = scn.GetShadowCard("barrage");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetShadowHand(barrage);
		scn.MoveMinionsToTable(denizen, halftroll);
		assertInHand(barrage);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// The pool pays its full 5 without toil.
		assertTrue(scn.GetTwilight() >= 5);
		assertFalse(scn.ShadowPlayAvailable(barrage));
		assertInHand(barrage);
	}

	@Test
	public void TheBonusLastsThroughTheNextSkirmishAndIsGoneOnceTheRegroupPhaseBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrage = scn.GetShadowCard("barrage");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SetShadowHand(barrage);
		// Three minions to three companions: the Half-troll's own trigger stays out of the way.
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		assertInHand(barrage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, denizen},
				new PhysicalCardImpl[]{gimli, halftroll});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(barrage);
		scn.ShadowDeclineChoosing();
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(halftroll));
		scn.PassSkirmishActions();

		// The next skirmish, a phase of its own: the Half-troll is still 8 + 2.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(halftroll));
		assertEquals(11, scn.GetStrength(denizen));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(9, scn.GetStrength(denizen));
	}

	@Test
	public void AnOrcMinionPlayedAfterwardsIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrage = scn.GetShadowCard("barrage");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);

		scn.StartGame();
		scn.SetShadowHand(barrage);
		scn.MoveCardsToDiscard(denizen);
		scn.MoveMinionsToTable(halftroll, pursuer);
		assertInHand(barrage);
		assertInDiscard(denizen);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Four companions to two minions: the skip declines the Half-troll's trigger at this first assignment phase.
		assertInDiscard(denizen);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, halftroll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(barrage);
		scn.ShadowDeclineChoosing();
		// 10 against Aragorn's 8: the Half-troll lives through its skirmish.
		assertEquals(10, scn.GetStrength(halftroll));
		scn.PassSkirmishActions();

		// The fierce Uruk Pursuer brings a fierce assignment phase, at whose start the Half-troll plays the Denizen.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(halftroll));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		// The Denizen came into play after the event: 9 as printed, while the Half-troll is still 8 + 2.
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(halftroll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
