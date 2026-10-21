package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "2_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRoamingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Scout: cost 2, site number 6
					put("scout1", "2_89");
					put("scout2", "2_89");
					put("scout3", "2_89");
					// Cirith Ungol Guard: a [sauron] minion who is no Orc (an Uruk-hai), cost 3, site number 6;
					// Damage +1 is its whole text
					put("guard1", "10_81");
					put("guard2", "10_81");
					// Uruk Savage: an [isengard] minion, cost 2, site number 5; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout1", "2_89");
					put("scout2", "2_89");   // not unique: "this minion" is read beside another copy

					// Aragorn, Ranger of the North: a [gondor] ranger companion, strength 8, vitality 4; his maneuver
					// ability is used only on purpose
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: a companion who is no ranger, strength 7, Frodo signet; a skirmish
					// ability used only on purpose
					put("boromir", "1_97");

					// Pursuit Just Behind: [gondor] Regroup event, "Exert a ranger companion to wound every minion"
					put("pursuit", "1_111");
					// An Able Guide: [gondor] Regroup event, "Spot a ranger to remove (4)"
					put("guide", "1_88");
					// Swordsman of the Northern Kingdom: [gondor] Skirmish event, "Make a ranger strength +2 (or +4
					// when skirmishing a roaming minion)": it names a ranger and neither spots nor exerts one
					put("swordsman", "1_117");
					// No Mere Ranger: [gondor] Skirmish event, "Exert Aragorn to make another companion strength +2
					// (or +3 if that companion has the Aragorn signet)": it exerts a ranger, but by name
					put("nomere", "2_36");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Orc Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.<br>The roaming penalty for each [sauron] minion you play is -1.<br><b>Response:</b> If an event is played that spots or exerts a ranger, exert or discard this minion to cancel that event.
		*/

		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");

		assertEquals("Orc Scout", scout.getBlueprint().getTitle());
		assertNull(scout.getBlueprint().getSubtitle());
		assertFalse(scout.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, scout.getBlueprint().getSide());
		assertEquals(Culture.SAURON, scout.getBlueprint().getCulture());
		assertEquals(CardType.MINION, scout.getBlueprint().getCardType());
		assertEquals(Race.ORC, scout.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(scout, Keyword.TRACKER));
		assertEquals(2, scout.getBlueprint().getTwilightCost());
		assertEquals(6, scout.getBlueprint().getStrength());
		assertEquals(2, scout.getBlueprint().getVitality());
		assertEquals(6, scout.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheNextSauronMinionPaysOneLessForRoamingAndTheScoutItselfPaysThePenaltyInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var scout3 = scn.GetShadowCard("scout3");
		var guard1 = scn.GetShadowCard("guard1");
		var guard2 = scn.GetShadowCard("guard2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(scout1, scout2, scout3, guard1, guard2, savage);
		assertInHand(scout1, scout2, scout3, guard1, guard2, savage);

		scn.StartGame();
		scn.SetTwilight(27);
		scn.FreepsPass();

		// Site 2: every minion here is roaming, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 27 set + 1 companion + 2 for Ettenmoors
		assertEquals(30, scn.GetTwilight());

		scn.ShadowPlayCard(scout1);
		// 2 to play + 2 roaming: its own text is not in force while it is being played
		assertEquals(26, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scout1);

		scn.ShadowPlayCard(guard1);
		// A [sauron] Uruk-hai: 3 to play + 2 roaming - 1.  The penalty is less; the minion is no less roaming.
		assertEquals(22, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard1, Keyword.ROAMING));

		scn.ShadowPlayCard(savage);
		// An [isengard] minion: 2 to play + 2 roaming
		assertEquals(18, scn.GetTwilight());

		scn.ShadowPlayCard(scout2);
		// 2 to play + 2 roaming - 1 for the first Scout, and nothing for itself
		assertEquals(15, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scout2);

		scn.ShadowPlayCard(scout3);
		// 2 to play + 2 roaming - 2
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(guard2);
		// Three Scouts in play take the penalty of 2 to nothing and no lower: 3 to play, not 3 - 1
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard2, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronMinionWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var guard1 = scn.GetShadowCard("guard1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();

		// Site 6, the site number of the Cirith Ungol Guard: he pays no roaming penalty for the Scout to make less.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(scout1);
		scn.SetShadowHand(guard1);

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(guard1));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(guard1));

		scn.ShadowPlayCard(guard1);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(guard1, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsToCancelAnEventThatExertsARangerWhoseCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pursuit);
		assertInHand(pursuit);

		scn.StartGame();
		// With no minion in play the game goes from the Shadow phase straight to regroup; the minions are the
		// ones still standing after the skirmishes.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(scout1, scout2);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(pursuit);
		// Aragorn, the only ranger companion, exerts for the event.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Each Scout may answer it.
		assertTrue(scn.ShadowActionAvailable(scout1));
		assertTrue(scn.ShadowActionAvailable(scout2));

		scn.ShadowUseCardAction(scout1);
		// The Scout can exert and can be discarded: the Shadow player is asked which.
		assertEquals(List.of("Exert this minion", "Discard this minion"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert this minion");

		// The Scout used exerts, and the one beside it does not.  Neither is wounded by the event, which would
		// have put a wound on each; Aragorn's exertion stays paid, and the event is discarded.
		assertEquals(1, scn.GetWoundsOn(scout1));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertInPlay(scout1, scout2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedToCancelAnEventThatSpotsARanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guide = scn.GetFreepsCard("guide");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guide);
		assertInHand(guide);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(scout1, scout2);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(guide);
		assertTrue(scn.ShadowActionAvailable(scout1));

		scn.ShadowUseCardAction(scout1);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Discard this minion");

		// The Scout used is discarded, the copy beside it stays unexerted.  Cancelled: the (4) is not removed.
		// Spotting Aragorn costs him nothing.
		assertInDiscard(scout1);
		assertInPlay(scout2);
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(guide);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedScoutIsOfferedAndDiscardedUnaskedToCancelTheEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pursuit);
		assertInHand(pursuit);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(scout1, scout2);
		// Vitality 2 with 1 wound: exhausted, so it cannot exert; it can still be discarded.
		scn.AddWoundsToChar(scout1, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(scout1));
		assertEquals(0, scn.GetWoundsOn(scout2));

		scn.FreepsPlayCard(pursuit);
		assertTrue(scn.ShadowActionAvailable(scout1));
		assertTrue(scn.ShadowActionAvailable(scout2));

		scn.ShadowUseCardAction(scout1);
		// Only the discard can be done: it is done with no question, and the event is cancelled.  The other Scout
		// is not wounded by it.
		assertInDiscard(scout1);
		assertInPlay(scout2);
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnEventThatMakesARangerStrongerWithoutSpottingOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var swordsman = scn.GetFreepsCard("swordsman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(swordsman);
		assertInHand(swordsman);

		scn.StartGame();
		scn.MoveMinionsToTable(scout1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(swordsman);
		// Not offered: the event is carried out at once and the Shadow player is asked for skirmish actions.
		// 8 + 4: the Scout, of site number 6, is roaming at site 2.
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertInPlay(scout1);
		assertInDiscard(swordsman);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnEventThatExertsAragornByNameAndNotARanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var nomere = scn.GetFreepsCard("nomere");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(nomere);
		assertInHand(nomere);

		scn.StartGame();
		scn.MoveMinionsToTable(scout1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(nomere);
		// Aragorn, who is a ranger, exerts for it, and the other companion is chosen.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		scn.FreepsChooseCard(boromir);

		// The text requires Aragorn, not a ranger (CR 4.2's ruling on Orc Scout): not offered, and carried out.
		// 7 + 2: Boromir has the Frodo signet.
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertInPlay(scout1);
		assertInDiscard(nomere);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
