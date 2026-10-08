package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_248_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forces1", "51_248");
					put("forces2", "51_248");

					put("swordsman1", "3_98");  // Orc Swordsman: [sauron] Orc, vitality 3; strength +2 only while bearing a weapon
					put("swordsman2", "3_98");
					put("soldier1", "1_271");   // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("soldier2", "1_271");
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, so a [sauron] minion who is not an Orc; vitality 2; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForcesofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Forces of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Exert a [sauron] minion to add (1) for each [sauron] Orc you can spot (limit (3)).
		 */

		var scn = GetScenario();

		var forces = scn.GetFreepsCard("forces1");

		assertEquals("Forces of Mordor", forces.getBlueprint().getTitle());
		assertNull(forces.getBlueprint().getSubtitle());
		assertFalse(forces.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, forces.getBlueprint().getSide());
		assertEquals(Culture.SAURON, forces.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, forces.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(forces, Timeword.SHADOW));
		assertEquals(0, forces.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionToAddOneForEachSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces1");
		var swordsman = scn.GetShadowCard("swordsman1");
		var soldier = scn.GetShadowCard("soldier1");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman, soldier, sentry, troop);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertTrue(scn.ShadowPlayAvailable(forces));

		scn.ShadowPlayCard(forces);
		// A [sauron] Orc and a [sauron] Uruk-hai may exert; a [moria] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swordsman, soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(swordsman);

		assertEquals(1, scn.GetWoundsOn(swordsman));
		// The 2 [sauron] Orcs count; the Uruk-hai and the [moria] Orc do not.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, forces);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertingAnUrukHaiAddsOneForTheOneSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces1");
		var swordsman = scn.GetShadowCard("swordsman1");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman, sentry, troop);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(forces);
		scn.ShadowChooseCard(sentry);

		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSauronOrcItMayStillBePlayedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces1");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, troop);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		// An event is offered whatever its effect will do.
		assertTrue(scn.ShadowPlayAvailable(forces));

		scn.ShadowPlayCard(forces);
		// The Uruk-hai is the only minion that may exert, so is chosen without asking.
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.DISCARD, forces);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FourSauronOrcsAddOnlyThreeAndASecondCopyAddsThreeMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces1 = scn.GetShadowCard("forces1");
		var forces2 = scn.GetShadowCard("forces2");
		var swordsman1 = scn.GetShadowCard("swordsman1");
		var swordsman2 = scn.GetShadowCard("swordsman2");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(forces1, forces2);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman1, swordsman2, soldier1, soldier2);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(forces1);
		scn.ShadowChooseCard(swordsman1);
		assertEquals(1, scn.GetWoundsOn(swordsman1));
		// 4 [sauron] Orcs, limit 3.
		assertEquals(twilight + 3, scn.GetTwilight());

		// The limit is that copy's: another copy in the same phase has its own.
		assertTrue(scn.ShadowPlayAvailable(forces2));
		scn.ShadowPlayCard(forces2);
		scn.ShadowChooseCard(swordsman2);
		assertEquals(1, scn.GetWoundsOn(swordsman2));
		assertEquals(twilight + 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forces = scn.GetShadowCard("forces1");
		var swordsman = scn.GetShadowCard("swordsman1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman, troop);
		// Vitality 3 with 2 wounds: exhausted.  The [moria] Orc beside him could exert.
		scn.AddWoundsToChar(swordsman, 2);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(forces));

		scn.RemoveWoundsFromChar(swordsman, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(forces));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
