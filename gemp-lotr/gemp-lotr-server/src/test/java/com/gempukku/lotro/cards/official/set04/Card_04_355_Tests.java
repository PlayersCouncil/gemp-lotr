package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_355_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion, vitality 3; "Skirmish: Exert Gimli to make him strength +2."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a Maneuver ability only
					put("axe", "1_3");          // Axe Strike: [dwarven] Skirmish event, cost 0, "Make a Dwarf strength +2"
					put("sturdy", "1_315");     // Stout and Sturdy: [shire] event, cost 1, "Maneuver or Skirmish: Heal a Hobbit."
					// Savagery to Match Their Numbers: [isengard] Skirmish event, cost 0, "Make an Uruk-hai strength +2" (or
					// more with 5 companions)
					put("savagery", "1_139");
					put("slayer", "1_153");     // Uruk Slayer: Uruk-hai, vitality 2; "Skirmish: Remove (1) to make this minion strength +1 (limit +3)."
					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, vitality 3
				}},
				// Each player's adventure deck has Cavern Entrance in place of site 7; the tests that read it put the
				// Shadow player's copy on the path as the site the fellowship moves to next.
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "4_355");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CavernEntranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Cavern Entrance
		 * Unique: False
		 * Shadow Number: 7
		 * Type: Site
		 * Block: Towers
		 * Direction: Right
		 * Site Number: 7T
		 * Game Text: <b>Underground</b>. Skirmish events may not be played and skirmish special abilities may not be used.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Cavern Entrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheSiteNeitherPlayerMayPlayASkirmishEventOrUseASkirmishSpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var sturdy = scn.GetFreepsCard("sturdy");
		var frodo = scn.GetRingBearer();
		var savagery = scn.GetShadowCard("savagery");
		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axe, sturdy, savagery);

		scn.StartGame();
		// A wounded Hobbit for Stout and Sturdy to heal.
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(axe, sturdy, savagery);
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();
		assertEquals(cavern, scn.GetCurrentSite());

		scn.MoveMinionsToTable(slayer, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, slayer}, new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(gimli);

		// The Free Peoples player's skirmish events, one printed for the skirmish alone and one for the maneuver
		// phase as well, and his companion's Skirmish ability (Gimli can exert).
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(axe, sturdy);
		assertFalse(scn.FreepsPlayAvailable(axe));
		assertFalse(scn.FreepsPlayAvailable(sturdy));
		assertFalse(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();

		// The Shadow player's skirmish event, with an Uruk-hai in the skirmish, and his minion's Skirmish ability,
		// with the (1) it costs in the pool.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertInHand(savagery);
		assertFalse(scn.ShadowPlayAvailable(savagery));
		assertFalse(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void AtAnotherSiteTheSameEventsAndAbilitiesAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var sturdy = scn.GetFreepsCard("sturdy");
		var frodo = scn.GetRingBearer();
		var savagery = scn.GetShadowCard("savagery");
		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axe, sturdy, savagery);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(axe, sturdy, savagery);
		// The fellowship moves to its own site 2, Ettenmoors 1_331.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNotEquals("Cavern Entrance", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(slayer, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, slayer}, new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(axe, sturdy);
		assertTrue(scn.FreepsPlayAvailable(axe));
		assertTrue(scn.FreepsPlayAvailable(sturdy));
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertInHand(savagery);
		assertTrue(scn.ShadowPlayAvailable(savagery));
		assertTrue(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void InTheManeuverPhaseAtTheSiteAManeuverOrSkirmishEventAndAManeuverAbilityAreAllowed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sturdy = scn.GetFreepsCard("sturdy");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sturdy);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(sturdy);
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();
		assertEquals(cavern, scn.GetCurrentSite());

		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Played in the maneuver phase, an event printed "Maneuver or Skirmish" is a maneuver event (CR 4.2
		// "phase action"), and a Maneuver ability is no skirmish special ability.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(sturdy);
		assertTrue(scn.FreepsPlayAvailable(sturdy));
		scn.FreepsPlayCard(sturdy);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(sturdy);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InASkirmishAtTheSiteTheRingsResponseIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");
		var cavern = scn.GetShadowSite(7);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();
		assertEquals(cavern, scn.GetCurrentSite());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		// Frodo (3, +1 for the Ring) loses to the Uruk Savage (5) and is about to take a wound in the skirmish: the
		// Ruling Ring's Response is a special ability, but no skirmish one. Again for Damage +1's second wound.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnSkirmishEventsAndAbilitiesAreAllowedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var savagery = scn.GetShadowCard("savagery");
		var slayer = scn.GetShadowCard("slayer");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(axe, savagery);

		scn.StartGame();
		assertInHand(axe, savagery);
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();
		assertEquals(cavern, scn.GetCurrentSite());

		// No minion at the Cavern: on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNotEquals(cavern, scn.GetCurrentSite());

		scn.MoveMinionsToTable(slayer);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, slayer);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(axe);
		assertTrue(scn.FreepsPlayAvailable(axe));
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertInHand(savagery);
		assertTrue(scn.ShadowPlayAvailable(savagery));
		assertTrue(scn.ShadowActionAvailable(slayer));
	}
}
