package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: FreepsRefreshDecision and ShadowRefreshDecision, which have a pending choice of phase
 * actions worked out afresh after the rig has rearranged the board under it; and the cheats that do the same of
 * their own accord as they finish (SetHand, SetDrawDeck, MoveCardsToDiscard and their twins).
 */
public class DecisionRefreshTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: companion, cost 4
					put("gimli", "1_13");           // Gimli, Son of Gloin: companion, cost 2
					put("guard", "1_7");            // Dwarf Guard: companion
					put("armor", "1_8");            // Dwarven Armor: possession, cost 0; bearer must be a Dwarf
					put("runner", "1_178");         // Goblin Runner: cost 1; adds (2) when played
					put("troop", "1_177");          // Goblin Patrol Troop: cost 6, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardCheatedIntoHandIsOfferedOnceTheChoiceOfPhaseActionsIsRefreshed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));

		// The engine asks once: the choice that is pending was worked out before Aragorn reached the hand.
		scn.MoveCardsToHand(aragorn);
		assertFalse(scn.FreepsPlayAvailable(aragorn));

		scn.FreepsRefreshDecision();

		// The same decision, with what the board allows now.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertEquals(0, scn.GetTwilight());

		// And it is answered like any other: the action taken is the one named.
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.HAND, gimli);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnActionTheBoardNoLongerAllowsIsGoneOnceTheChoiceIsRefreshed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(runner, troop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertTrue(scn.ShadowPlayAvailable(troop));

		// Enough left for the Runner (1 + 2 roaming) and not for the Troop (6 + 2 roaming).
		scn.SetTwilight(3);
		assertTrue(scn.ShadowPlayAvailable(troop));

		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertFalse(scn.ShadowPlayAvailable(troop));

		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// 3 - 1 to play - 2 roaming, + the Runner's own (2)
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RefreshingIsRefusedUnlessThatPlayerIsAtAChoiceOfPhaseActions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// The Free Peoples player is at the fellowship actions and the Shadow player is being asked nothing.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertThrows(AssertionError.class, scn::ShadowRefreshDecision);

		// Which Dwarf is to bear the Armor is a choice of cards, not a choice of phase actions.
		scn.FreepsPlayCard(armor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertThrows(AssertionError.class, scn::FreepsRefreshDecision);
		// The refusal answered nothing: the choice is still there to be made.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertThrows(AssertionError.class, scn::FreepsRefreshDecision);
	}

	@Test
	public void ACardCheatedFromHandIntoTheDiscardPileIsNoLongerOfferedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(aragorn, gimli);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(gimli));

		// No refresh is called: the cheat has the pending choice worked out afresh itself.
		scn.MoveCardsToDiscard(gimli);

		assertInZone(Zone.DISCARD, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gimli)); // lint-ok: this cheat refreshes the choice itself (engine pass 40), which is what is tested
		assertTrue(scn.FreepsPlayAvailable(aragorn));
	}

	@Test
	public void SettingAHandOrADrawDeckAtAChoiceOfPhaseActionsChangesWhatItOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsPlayAvailable(aragorn));

		// Gimli goes back on the deck, and Aragorn is dealt in.
		scn.SetFreepsHand(aragorn);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.DECK, gimli);
		assertFalse(scn.FreepsPlayAvailable(gimli)); // lint-ok: this cheat refreshes the choice itself (engine pass 40), which is what is tested
		assertTrue(scn.FreepsPlayAvailable(aragorn));

		// Aragorn is taken out of the hand into the draw deck.
		scn.SetFreepsDrawDeck(aragorn, gimli);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.DECK, aragorn);
		assertFalse(scn.FreepsPlayAvailable(aragorn)); // lint-ok: this cheat refreshes the choice itself (engine pass 40), which is what is tested
		assertFalse(scn.FreepsPlayAvailable(gimli)); // lint-ok: this cheat refreshes the choice itself (engine pass 40), which is what is tested
		assertEquals(0, scn.GetFreepsHandCount());
	}

	@Test
	public void SettingTheShadowHandAtHisChoiceOfShadowActionsChangesWhatItOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(troop));
		assertFalse(scn.ShadowPlayAvailable(runner));

		scn.SetShadowHand(runner);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DECK, troop);
		assertFalse(scn.ShadowPlayAvailable(troop)); // lint-ok: this cheat refreshes the choice itself (engine pass 40), which is what is tested
		assertTrue(scn.ShadowPlayAvailable(runner));
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCheatsLeaveAPendingDecisionThatIsNoChoiceOfPhaseActionsAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// Which Dwarf is to bear the Armor is a choice of cards; the Shadow player is asked nothing.
		scn.FreepsPlayCard(armor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));

		scn.SetFreepsHand(aragorn);
		scn.SetFreepsDrawDeck();
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(scn.GetShadowCard("troop"));

		// Nothing was answered and nothing refused: the choice is still there to be made.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(aragorn));
	}
}
