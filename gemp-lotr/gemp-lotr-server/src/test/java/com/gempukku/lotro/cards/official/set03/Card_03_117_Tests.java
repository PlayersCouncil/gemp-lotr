package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_117_Tests
{

	// The Gates are site 8, after Anduin Wilderland at site 7; site 9 is Emyn Muil, whose Maneuver ability is the
	// Shadow player's and is used only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a [gondor] companion with a Maneuver special ability
					put("aragorn", "1_89");
					// Elendil's Valor: [gondor] Maneuver event, "Make a [gondor] companion defender +1 until the regroup phase"
					put("valor", "1_103");
					// Stout and Sturdy: [shire] event, "Maneuver or Skirmish: Heal a Hobbit"
					put("stout", "1_315");
					// It Wants to be Found: [wraith] Maneuver event, "Spot a twilight Nazgûl to add a burden", cost 2
					put("found", "2_78");
					// Úlairë Nelya: a twilight Nazgûl, strength 10, vitality 3, site 2; fierce and damage +1 only at
					// 2 burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_354");
					put("site8", "3_117");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatesofArgonathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Gates of Argonath
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Right
		 * Game Text: <b>River</b>. Maneuver events may not be played.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gates of Argonath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtTheGatesNeitherPlayerMayPlayAManeuverEventButAManeuverAbilityIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn);

		var found = scn.GetShadowCard("found");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetFreepsHand(valor, stout);
		scn.SetShadowHand(found);
		// A wounded Hobbit for Stout and Sturdy to heal.
		scn.AddWoundsToChar(frodo, 1);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertEquals("Gates of Argonath", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// A Maneuver event, and an event printed "Maneuver or Skirmish" in the maneuver phase
		assertFalse(scn.FreepsPlayAvailable(valor));
		assertFalse(scn.FreepsPlayAvailable(stout));
		// A Maneuver special ability is no event.
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		// The Shadow player's Maneuver event, with its twilight Nazgûl and the twilight to pay for it
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 1 for each of 2 companions + 8 for the Gates; It Wants to be Found costs 2
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(found));
		assertInZone(Zone.HAND, valor, stout);
		assertInZone(Zone.HAND, found);
	}

	@Test
	public void AnEventOfManeuverOrSkirmishIsPlayedInASkirmishAtTheGates() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetFreepsHand(stout);
		scn.FreepsPass();

		assertEquals("Gates of Argonath", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(nelya);
		scn.AddWoundsToChar(frodo, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Played in a skirmish it is a Skirmish event (CR 4.2 "phase action"; F-0292).
		assertTrue(scn.FreepsPlayAvailable(stout));
		scn.FreepsPlayCard(stout);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, stout);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheGatesManeuverEventsArePlayedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(aragorn);

		var gates = scn.GetShadowSite(8);
		var found = scn.GetShadowCard("found");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetFreepsHand(valor);
		scn.SetShadowHand(found);
		scn.FreepsPass();

		assertSame(gates, scn.GetCurrentSite());
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(valor));

		// Aragorn loses to Nelya (8 to 10) and takes a wound; the fellowship then moves on to site 9 in the regroup
		// phase, and Nelya is still in play there.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertSame(gates, scn.GetSite(8));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(valor));
		// Aragorn is the only [gondor] companion: chosen unasked.
		scn.FreepsPlayCard(valor);
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertInZone(Zone.DISCARD, valor);

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(found));
		scn.ShadowPlayCard(found);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, found);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
