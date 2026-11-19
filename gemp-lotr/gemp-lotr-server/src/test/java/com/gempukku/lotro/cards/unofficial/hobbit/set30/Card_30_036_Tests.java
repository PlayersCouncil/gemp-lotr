package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_036_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("narzug", "30_36");
					// Thorin, Oakenshield: [dwarven] companion, strength 7 and damage +1, who beats Narzug (6) and
					// places the two wounds that kill him; a Fellowship ability only
					put("thorin", "30_19");
					// Bard, The Bowman: [esgaroth] Man ally, vitality 3, home 6 (no archer away from home); a Regroup
					// ability that wounds a minion
					put("bard", "30_22");
					// Radagast, The Brown: [gandalf] Wizard ally, vitality 4; a Fellowship ability only
					put("radagast", "31_17");
					// Granite Giant: [moria] Giant minion whose Archery ability discards an Orc from play
					put("giant", "33_42");
					// Orkish Veteran: [sauron] Orc whose Regroup ability discards an Orc from play
					put("veteran", "32_43");
					// Goblin Runner: [moria] Orc; a trigger on his own play only
					put("runner", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NarzugStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Narzug, Orkish Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Archer</b>.
		 * 	When Narzug is killed or discarded from play (except during the regroup phase), you may remove (3) to
		 * 	wound an ally twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("narzug");

		assertEquals("Narzug", card.getBlueprint().getTitle());
		assertEquals("Orkish Assassin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardedInTheArcheryPhaseRemoves3ToWoundAnAllyTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var giant = scn.GetShadowCard("giant");
		var bard = scn.GetFreepsCard("bard");
		var radagast = scn.GetFreepsCard("radagast");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveAlliesToTable(bard, radagast);
		// Bard exhausted: one wound left, and still offered, as a wound is no exertion.
		scn.AddWoundsToChar(bard, 2);
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(radagast));
		scn.MoveMinionsToTable(narzug, giant);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowUseCardAction(giant);

		// Narzug, the only Orc, is discarded for the Giant's cost.
		assertInDiscard(narzug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(narzug));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		// Allies of two cultures and races; not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bard, radagast));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo));
		scn.ShadowChooseCard(radagast);

		assertEquals(2, scn.GetWoundsOn(radagast));
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnExhaustedAllyWoundedTwiceIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var giant = scn.GetShadowCard("giant");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveAlliesToTable(bard);
		scn.AddWoundsToChar(bard, 2);
		assertEquals(2, scn.GetWoundsOn(bard));
		scn.MoveMinionsToTable(narzug, giant);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowUseCardAction(giant);
		assertInDiscard(narzug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(narzug));
		scn.ShadowAcceptOptionalTrigger();

		// Bard is the only ally, and is wounded unasked: the first wound kills him.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DEAD, bard);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndTheAlliesAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var giant = scn.GetShadowCard("giant");
		var bard = scn.GetFreepsCard("bard");
		var radagast = scn.GetFreepsCard("radagast");

		scn.StartGame();
		scn.MoveAlliesToTable(bard, radagast);
		scn.MoveMinionsToTable(narzug, giant);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(radagast));
		scn.ShadowUseCardAction(giant);
		assertInDiscard(narzug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(narzug));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var giant = scn.GetShadowCard("giant");
		var radagast = scn.GetFreepsCard("radagast");

		scn.StartGame();
		scn.MoveAlliesToTable(radagast);
		scn.MoveMinionsToTable(narzug, giant);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(radagast));
		scn.ShadowUseCardAction(giant);
		assertInDiscard(narzug);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(narzug));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoAllyTheTriggerIsStillTakenAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var giant = scn.GetShadowCard("giant");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(narzug, giant);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowUseCardAction(giant);
		assertInDiscard(narzug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(narzug));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnotherOrcDiscardedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var giant = scn.GetShadowCard("giant");
		var runner = scn.GetShadowCard("runner");
		var radagast = scn.GetFreepsCard("radagast");

		scn.StartGame();
		scn.MoveAlliesToTable(radagast);
		scn.MoveMinionsToTable(narzug, giant, runner);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowUseCardAction(giant);
		assertTrue(scn.ShadowHasCardChoiceAvailable(narzug, runner));
		scn.ShadowChooseCard(runner);

		assertInDiscard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, narzug);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void KilledInASkirmishRemoves3ToWoundAnAllyTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var thorin = scn.GetFreepsCard("thorin");
		var radagast = scn.GetFreepsCard("radagast");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(radagast);
		scn.MoveMinionsToTable(narzug);
		// Narzug is an archer: his one wound goes on Bilbo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(bilbo);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, narzug);
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(3);

		assertEquals(0, scn.GetWoundsOn(narzug));
		assertEquals(0, scn.GetWoundsOn(radagast));
		// Thorin, 7, beats Narzug, 6, and his damage +1 makes the two wounds that kill him.
		scn.PassSkirmishActions();

		assertInDiscard(narzug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(narzug));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KilledInTheRegroupPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var bard = scn.GetFreepsCard("bard");
		var radagast = scn.GetFreepsCard("radagast");

		scn.StartGame();
		scn.MoveAlliesToTable(bard, radagast);
		scn.SkipToPhase(Phase.REGROUP);
		// Narzug stands in the regroup phase as a minion that lived through the skirmishes, with one wound left.
		scn.MoveMinionsToTable(narzug);
		scn.AddWoundsToChar(narzug, 1);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(narzug));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(radagast));

		scn.FreepsUseCardAction(bard);

		assertInDiscard(narzug);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardedInTheRegroupPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narzug = scn.GetShadowCard("narzug");
		var veteran = scn.GetShadowCard("veteran");
		var radagast = scn.GetFreepsCard("radagast");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveAlliesToTable(radagast);
		scn.SkipToPhase(Phase.REGROUP);
		// Both stand in the regroup phase as minions that lived through the skirmishes.
		scn.MoveMinionsToTable(narzug, veteran);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.FreepsPass();

		scn.ShadowUseCardAction(veteran);
		assertTrue(scn.ShadowHasCardChoiceAvailable(narzug, veteran));
		scn.ShadowChooseCard(narzug);
		// The Veteran's effect: the Free Peoples player wounds a companion, Bilbo the only one.
		assertEquals(1, scn.GetWoundsOn(bilbo));

		assertInDiscard(narzug);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
