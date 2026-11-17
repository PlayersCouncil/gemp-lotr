package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_352_Tests
{

	// The Towers path, whose site 6 is Caves of Aglarond; the tests use the copy the fellowship stands on.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Troop of Uruk-hai: [isengard] Uruk-hai, strength 9, vitality 4, damage +1; a character skirmishing
					// it gains no strength from weapons
					put("troop", "1_143");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; whole text Damage +1
					put("patrol", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CavesofAglarondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Caves of Aglarond
		 * Unique: False
		 * Shadow Number: 3
		 * Type: Site
		 * Block: Towers
		 * Direction: Left
		 * Site Number: 6T
		 * Game Text: <b>Underground</b>. <b>Sanctuary</b>. <b>Maneuver:</b> Remove (2) and spot your Uruk-hai to make it <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Caves of Aglarond", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ManeuverAbilityRemovesTwoToMakeAChosenUrukHaiOfTheShadowPlayerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(troop, guard, patrol);
		scn.SkipToPhase(Phase.MANEUVER);

		var caves = scn.GetCurrentSite();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());

		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		// The Free Peoples player has no Uruk-hai of his own to spot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(caves));
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(patrol, Keyword.FIERCE));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(caves));

		scn.ShadowUseCardAction(caves);
		assertEquals(1, scn.GetTwilight());
		// An [isengard] Uruk-hai and a [sauron] one; not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(patrol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(patrol, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Used again with the Troop fierce already: it is still a candidate beside the Guard, and the Guard is untouched.
		scn.FreepsPass();
		assertEquals(1, scn.GetTwilight());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(caves));
		scn.ShadowUseCardAction(caves);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(patrol));
		scn.ShadowChooseCard(troop);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoUrukHaiToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.MANEUVER);

		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		scn.SetTwilight(3);
		scn.FreepsPass();

		// A minion, and the twilight to pay, but no Uruk-hai.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(caves));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithOneTwilightTokenAndIsWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(guard);
		scn.SkipToPhase(Phase.MANEUVER);

		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(caves));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(caves));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheUrukHaiIsFierceThroughTheFierceSkirmishAndNotOnceTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(guard);
		scn.SkipToPhase(Phase.MANEUVER);

		var caves = scn.GetCurrentSite();
		assertEquals("Caves of Aglarond", caves.getBlueprint().getTitle());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		// The Guard is the one Uruk-hai, so he is chosen unasked.
		scn.ShadowUseCardAction(caves);
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));

		// With the one Uruk-hai fierce already the ability is still offered.
		scn.FreepsPass();
		assertEquals(1, scn.GetTwilight());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(caves));
		scn.ShadowPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The Guard's 9 against Aragorn's 8: Aragorn loses, 1 wound and 1 for damage +1; the Guard is unhurt.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Still fierce, so a fierce round follows the skirmish.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));

		scn.PassSkirmishActions();
		// The Guard's 9 against Gimli's 6: Gimli loses, 1 wound and 1 for damage +1.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
	}
}
