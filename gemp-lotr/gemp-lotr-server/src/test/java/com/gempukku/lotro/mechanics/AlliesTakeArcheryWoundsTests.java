package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Who archery wounds are assigned to, and what happens while allies take them "instead of companions".
 *
 * Comprehensive Rules 4.2, "archery phase": the Free Peoples player assigns a number of wounds equal to the minion
 * archery total to his companions (and participating allies), one at a time; leftover wounds that cannot be assigned
 * are ignored.  "Ally": an ally participates in archery fire (is counted for the fellowship archery total if an
 * archer, and may be assigned archery wounds) only while the fellowship is at his home site, or a card allows it.
 *
 * "Instead": the stated effect is replaced with a different effect.  "If the second effect cannot happen for any
 * reason, then the original effect occurs."
 *
 * Come Down 4_146 ("Archery: Spot 2 [isengard] archers to make allies take wounds from archery fire instead of
 * companions") is the one card that turns this on.  The Comprehensive Rules' ruling on it: "This card allows all
 * allies to take wounds from archery fire."  Ketura's rulings (2026-10-08):
 *   Q-0136: a wound the allies cannot take (no ally left, or the last one killed) goes to companions, as it would
 *     have without the card.  The game used to drop it.
 *   Q-0137: the allies only take the wounds.  The card "doesn't say 'participate in archery fire'", so an archer
 *     ally away from home is not counted for the fellowship archery total.
 */
public class AlliesTakeArcheryWoundsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comedown", "4_146");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde: Elf ally, home site 3, vitality 2; has the text of site 3 while it can be spotted
					put("celeborn", "1_34");    // Celeborn: Elf ally, home site 6, vitality 3; a fellowship ability used only on purpose
					put("calaglin", "3_10");    // Calaglin: Elf ally and archer, home site 6, vitality 3; a regroup ability used only on purpose
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] archer, damage +1 and nothing else
					put("crossbow1", "4_182");  // Uruk Crossbow Troop: [isengard] archer; its when-played token does nothing when cheated in
					put("crossbow2", "4_182");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AsUsualAnArcheryWoundGoesToACompanionOrAnAllyAtHomeAndNotToAnAllyAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, celeborn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin, crossbow1);
		// The fellowship moves to site 3, Silinde's home; Celeborn's is site 6.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		scn.FreepsChooseCard(silinde);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhileAlliesTakeThemEveryAllyAtHomeOrAwayIsOfferedAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, celeborn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin, crossbow1);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(celeborn);
		// An ally is still able to take the second wound, so it too is an ally's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(celeborn);

		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWoundLeftOverWhenTheLastAllyIsKilledIsAssignedToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var crossbow2 = scn.GetShadowCard("crossbow2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow1, crossbow2);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetShadowArcheryTotal());
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		// Silinde, the one ally, is the only character the first two wounds can go to; they kill her (vitality 2).
		assertInZone(Zone.DEAD, silinde);

		// The third cannot be taken by an ally, so it is assigned as it would have been without Come Down.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoAllyLeftWhenTheArchersFireEveryWoundIsAssignedToCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow1);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		// The one ally leaves play after Come Down is used and before the archers fire.
		scn.MoveCardsToDiscard(silinde);
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnArcherAllyAwayFromHomeTakesTheWoundsAndAddsNothingToTheFellowshipArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var calaglin = scn.GetFreepsCard("calaglin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(calaglin);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow1);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Calaglin's home is site 6; the fellowship is at site 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.ShadowUseCardAction(comedown);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.PassArcheryActions();

		// Calaglin, the one ally, takes both wounds; no minion takes one, since the fellowship archery total is 0.
		assertEquals(2, scn.GetWoundsOn(calaglin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(crossbow1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnArcherAllyAtHomeIsCountedForTheFellowshipArcheryTotalBeforeAndAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(assassin, crossbow1);
		// The fellowship moves to site 6, Calaglin's home: he participates in archery fire by the rules.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.ShadowUseCardAction(comedown);
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void ComeDownIsNotOfferedWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		// With no ally to take a wound the ability would do nothing, and it has no cost (Q-0034; Q-0136).
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow1 = scn.GetShadowCard("crossbow1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow1);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
	}
}
