package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_025_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foreman", "102_25");

					// [isengard] Orcs, all of site 4.  The first three and the Tinker have regroup abilities that
					// are only ever used on purpose.
					put("worker", "3_62");       // Isengard Worker: cost 2, strength 5, vitality 3
					put("servant", "3_58");      // Isengard Servant: cost 3, strength 7, vitality 3
					put("retainer", "3_57");     // Isengard Retainer: cost 4, strength 8, vitality 3
					put("tinker", "6_71");       // Isengard Tinker: cost 1, strength 5, vitality 2
					put("wargrider", "5_67");    // Warg-rider: cost 1, strength 5, a warg-rider, fierce while not exhausted

					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, cost 6, site 4, no text
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, cost 1, site 4; adds (2) when played
					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai, cost 2, site 5, damage +1

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");         // Dwarf Guard: strength 4, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardForemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Isengard Foreman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, you may play an [isengard] Orc from your discard pile; it is strength +1 until the regroup phase.
		 * Regroup: Discard this minion to add two threats.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foreman");

		assertEquals("Isengard Foreman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedAnIsengardOrcMayBePlayedFromTheDiscardPileAndIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foreman = scn.GetShadowCard("foreman");
		var worker = scn.GetShadowCard("worker");
		var wargrider = scn.GetShadowCard("wargrider");
		var servant = scn.GetShadowCard("servant");
		var tinker = scn.GetShadowCard("tinker");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var theirRetainer = scn.GetFreepsCard("retainer");
		scn.MoveCardsToHand(foreman, tinker);
		// Two [isengard] Orcs in the discard pile, with an Orc of another culture and an [isengard] minion of
		// another race.  A third [isengard] Orc is in hand and a fourth in the other player's discard pile.
		scn.MoveCardsToDiscard(worker, wargrider, troop, savage, theirRetainer);

		scn.StartGame();

		scn.MoveMinionsToTable(servant);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 for Frodo + 2 for the site
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(foreman));

		scn.ShadowPlayCard(foreman);

		// 3 to play + 2 roaming at site 2
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, foreman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foreman));
		scn.ShadowAcceptOptionalTrigger();

		// The pool would pay for any of the four in the discard pile: 8 for the Goblin Patrol Troop at most.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, wargrider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, savage, tinker, theirRetainer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(worker);

		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		// 2 to play + 2 roaming
		assertEquals(14, scn.GetTwilight());
		// 5 printed + 1
		assertEquals(6, scn.GetStrength(worker));
		// Not the Foreman himself, and not an [isengard] Orc who was in play already.
		assertEquals(7, scn.GetStrength(foreman));
		assertEquals(7, scn.GetStrength(servant));
		assertInZone(Zone.DISCARD, wargrider, troop, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another minion played while the Foreman is in play sets nothing off, with an [isengard] Orc still
		// in the discard pile and the twilight to play it.
		scn.ShadowPlayCard(tinker);
		assertInZone(Zone.SHADOW_CHARACTERS, tinker);
		// 1 to play + 2 roaming
		assertEquals(11, scn.GetTwilight());
		assertEquals(5, scn.GetStrength(tinker));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foreman = scn.GetShadowCard("foreman");
		var worker = scn.GetShadowCard("worker");
		var wargrider = scn.GetShadowCard("wargrider");
		scn.MoveCardsToHand(foreman);
		scn.MoveCardsToDiscard(worker, wargrider);

		scn.StartGame();

		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(foreman);

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foreman));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DISCARD, worker, wargrider);
		assertInZone(Zone.SHADOW_CHARACTERS, foreman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenThePoolIsOneShortOfTheOnlyIsengardOrcInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foreman = scn.GetShadowCard("foreman");
		var retainer = scn.GetShadowCard("retainer");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(foreman);
		// The Isengard Retainer needs 4 + 2 roaming.  The other two could be paid for (1 + 2 and 2 + 2) and
		// are not [isengard] Orcs: one is an Orc of another culture, one an [isengard] minion of another race.
		scn.MoveCardsToDiscard(retainer, runner, savage);

		scn.StartGame();

		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the site
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(foreman);

		// 3 to play + 2 roaming leaves 5: playing a card from the discard pile has to be paid for like a cost.
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, foreman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, retainer, runner, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedWhenThePoolIsExactlyEnoughForTheOnlyIsengardOrcInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foreman = scn.GetShadowCard("foreman");
		var retainer = scn.GetShadowCard("retainer");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(foreman);
		scn.MoveCardsToDiscard(retainer, runner, savage);

		scn.StartGame();

		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the site
		assertEquals(11, scn.GetTwilight());

		scn.ShadowPlayCard(foreman);

		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(foreman));
		scn.ShadowAcceptOptionalTrigger();

		// The Retainer is the only card there that the Foreman can play, so nothing is asked.
		assertInZone(Zone.SHADOW_CHARACTERS, retainer);
		// 4 to play + 2 roaming
		assertEquals(0, scn.GetTwilight());
		// 8 printed + 1
		assertEquals(9, scn.GetStrength(retainer));
		assertInZone(Zone.DISCARD, runner, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOrcPlayedIsStrengthPlus1ThroughTheSkirmishesAndNotOnceTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var foreman = scn.GetShadowCard("foreman");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(foreman);
		scn.MoveCardsToDiscard(worker);

		scn.StartGame();

		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(foreman);
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertEquals(6, scn.GetStrength(worker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, foreman},
				new PhysicalCardImpl[]{guard, worker});

		// The Foreman's skirmish comes and goes first: 7 against Aragorn's 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(foreman));

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(worker));
		assertEquals(6, scn.GetStrength(worker));

		// 6 against the Dwarf Guard's 4: the Guard loses and is not overwhelmed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(worker));
	}

	@Test
	public void RegroupActionDiscardsTheForemanToAddTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var foreman = scn.GetShadowCard("foreman");
		var worker = scn.GetShadowCard("worker");
		// Three companions with Frodo, so there is room for three threats.
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();

		// Another [isengard] Orc stands beside the Foreman: the cost is this minion, not one of his kind.
		scn.MoveMinionsToTable(foreman, worker);

		// Nobody is assigned on this turn, so the assignment phase is the last before the regroup phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(foreman));

		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, foreman, worker);
		assertTrue(scn.ShadowActionAvailable(foreman));

		scn.ShadowUseCardAction(foreman);

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, foreman);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
