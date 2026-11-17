package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_264_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "7_264");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, not unique, strength 7; a skirmish ability used only on purpose
					// Morgul Warden: a unique [sauron] Orc, strength 9 (+1 for each other [sauron] Orc you can spot)
					put("warden", "1_259");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc; its play trigger is not met by a cheat
					// Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("sentry", "10_84");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmyofUdunStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Army of Udûn
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 9
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 19
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: <b>Shadow:</b> Discard a [sauron] Orc to add a threat.
		 * 	<b>Skirmish:</b> Remove a threat to make Army of Udûn strength +1.
		 * 	<b>Regroup:</b> Discard Army of Udûn and X other [sauron] Orcs to add X threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Army of Udûn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(19, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityDiscardsASauronOrcToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var army = scn.GetShadowCard("army");
		var soldier = scn.GetShadowCard("soldier");
		var warden = scn.GetShadowCard("warden");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.MoveMinionsToTable(army, soldier, warden, runner, sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, army, soldier, warden, runner, sentry);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		// A [sauron] Orc of any title, the Army itself among them (no "another"); not an Orc of [moria], nor a
		// [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, soldier, warden));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, sentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertInDiscard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, army, warden, runner, sentry);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NeitherTheShadowNorTheRegroupAbilityIsOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var army = scn.GetShadowCard("army");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(army, soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, army, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Both costs could be paid: the Soldier to discard, and the Army with him; no threat to remove.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(army));
	}

	@Test
	public void SkirmishAbilityRemovesAThreatToMakeTheArmyStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var army = scn.GetShadowCard("army");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		// Frodo, Aragorn and Gimli: room for the two threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(army, soldier, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, army}, new PhysicalCardImpl[]{gimli, chieftain});
		// The Soldier, left over, goes into the same skirmish as the Army.
		scn.ShadowAssignToMinions(aragorn, soldier);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(19, scn.GetStrength(army));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		assertEquals(1, scn.GetThreats());
		assertEquals(20, scn.GetStrength(army));
		assertEquals(7, scn.GetStrength(soldier));

		// No limit: once more, for the last threat.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);
		assertEquals(0, scn.GetThreats());
		assertEquals(21, scn.GetStrength(army));
		assertEquals(7, scn.GetStrength(soldier));

		// No threat left to remove: not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(army));
		scn.ShadowPass();

		// 21 and 7 against Aragorn's 8: more than double, so he is killed.  In Gimli's skirmish the Army is 19 again.
		assertInZone(Zone.DEAD, aragorn);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(19, scn.GetStrength(army));
	}

	@Test
	public void SkirmishAbilityIsOfferedWhileTheArmyIsInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var army = scn.GetShadowCard("army");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(army, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, chieftain);
		// The Army is left unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		assertEquals(0, scn.GetThreats());
		assertEquals(20, scn.GetStrength(army));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsTheArmyAndTwoOtherSauronOrcsToAddTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var army = scn.GetShadowCard("army");
		var soldier = scn.GetShadowCard("soldier");
		var warden = scn.GetShadowCard("warden");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(army, soldier, warden, runner, sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, army, soldier, warden, runner, sentry);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		// Other [sauron] Orcs of two titles; not an Orc of [moria], nor a [sauron] Uruk-hai.  X is the Shadow
		// player's, from none to the two there are.
		assertInDiscard(army);
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, warden));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, sentry));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(soldier, warden);

		assertInDiscard(army, soldier, warden);
		assertInZone(Zone.SHADOW_CHARACTERS, runner, sentry);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityWithOneOtherSauronOrcAddsOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var army = scn.GetShadowCard("army");
		var soldier = scn.GetShadowCard("soldier");
		var warden = scn.GetShadowCard("warden");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(army, soldier, warden);
		assertInZone(Zone.SHADOW_CHARACTERS, army, soldier, warden);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowUseCardAction(army);
		scn.ShadowChooseCards(warden);

		assertInDiscard(army, warden);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherSauronOrcTheRegroupAbilityDiscardsTheArmyAndAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var army = scn.GetShadowCard("army");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(army, sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, army, sentry);
		scn.FreepsPass();

		// X of zero: the Army is still discarded.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		assertInDiscard(army);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheThreatLimitTheRegroupAbilityIsStillOfferedAndItsOrcsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var army = scn.GetShadowCard("army");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// Frodo and Aragorn: two threats are as many as there may be.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(army, soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, army, soldier);
		scn.FreepsPass();

		// The discards are the cost and can be paid; the threats are the effect, which can add none.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);
		scn.ShadowChooseCards(soldier);

		assertInDiscard(army, soldier);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
