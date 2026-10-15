package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_93_010_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "4_26"); // Iron Axe: Dunland possession, bears on Dunland Man
					put("madman", "4_12"); // Dunlending Madman: Dunland Man minion, twilight 2
					put("hides", "4_19"); // Hides: a [dunland] possession that lies in the support area
					put("nodefense", "4_28"); // No Defense: a [dunland] support-area condition, no possession
					put("poison", "8_28"); // Spider Poison: a [gollum] support-area possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_10"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "4_26"); // Iron Axe: Dunland possession, bears on Dunland Man
					put("madman", "4_12"); // Dunlending Madman: Dunland Man minion, twilight 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_10", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_10
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: At the start of the regroup phase, you may discard your [Dunland] possession
		 * to take control of a site.
		 */
		var scn = GetShadowScenario();
		var card = scn.GetShadowCard("mod");
		assertEquals("Race Text 93_10", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void DiscardDunlandPossessionToControlSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();

		scn.SkipToSite(3);

		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);

		scn.SkipToPhase(Phase.REGROUP);

		// Start of regroup — trigger should fire
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.ATTACHED, axe);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		// Choose to discard the Dunland possession
		scn.ShadowAcceptOptionalTrigger();

		// Axe should be discarded
		assertInZone(Zone.DISCARD, axe);

		// Should have taken control of a site
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EitherDunlandPossessionMayPayAndNothingElseAndOnlyAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");
		var hides = scn.GetShadowCard("hides");
		var nodefense = scn.GetShadowCard("nodefense");
		var poison = scn.GetShadowCard("poison");
		var otherHides = scn.GetFreepsCard("hides");

		scn.StartGame();

		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);

		// One [dunland] possession on a [dunland] Man and one in the support area. Beside them a
		// [dunland] condition and a possession of another culture.
		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);
		scn.MoveCardsToSupportArea(hides, nodefense, poison);
		// The other player's own Hides, left in his support area from a turn on which he was the Shadow player
		scn.MoveCardsToSupportArea(otherHides);

		// The fellowship moves to site 4. Nothing is offered as the maneuver phase starts.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, hides));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(madman, nodefense, poison, otherHides));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(hides);

		// One possession pays, and the other stays where it was
		assertInZone(Zone.DISCARD, hides);
		assertInZone(Zone.ATTACHED, axe);
		assertInZone(Zone.SUPPORT, nodefense, poison, otherHides);
		// Both markers are past sites 1 and 2: the lowest-numbered of them is the one taken
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDunlandPossessionOfHisOwnInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var madman = scn.GetShadowCard("madman");
		var nodefense = scn.GetShadowCard("nodefense");
		var poison = scn.GetShadowCard("poison");
		var otherHides = scn.GetFreepsCard("hides");

		scn.StartGame();

		scn.SkipToSite(3);

		// A [dunland] Man, a [dunland] condition, a possession of another culture, and the other
		// player's own [dunland] possession: nothing that can pay
		scn.MoveMinionsToTable(madman);
		scn.MoveCardsToSupportArea(nodefense, poison);
		scn.MoveCardsToSupportArea(otherHides);

		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, nodefense, poison, otherHides);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CanDeclineToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");
		var otherHides = scn.GetFreepsCard("hides");

		scn.StartGame();

		scn.SkipToSite(3);

		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);
		// The other player's own Hides, left in his support area from a turn on which he was the Shadow player
		scn.MoveCardsToSupportArea(otherHides);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		// Decline
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowDeclineOptionalTrigger();

		// Axe should still be attached
		assertInZone(Zone.ATTACHED, axe);

		// No site control gained
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// "You may" is the modifier's owner and nobody else: the other player is not offered it in his place
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, otherHides);
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OwnerGatingFreepsCantTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");

		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		// Freeps owns the mod — trigger doesn't fire for Shadow
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		// Axe should still be attached
		assertInZone(Zone.ATTACHED, axe);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerOneTakesControlAsTheShadowPlayerOfPlayerTwosTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		// Player 1 owns the modifier, and these are his own Shadow cards
		var mod = scn.GetFreepsCard("mod");
		var madman = scn.GetFreepsCard("madman");
		var axe = scn.GetFreepsCard("axe");

		scn.StartGame();

		// Player 2's turn: player 1 is the Shadow player now, and his marker is at site 2
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		var site1 = scn.GetSite(1);

		// Player 2's fellowship moves to site 2: both markers are past site 1
		scn.ShadowPass();
		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);

		scn.SkipToPhaseInverted(Phase.REGROUP);
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, axe);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// Player 2, the Free Peoples player of this turn, is asked first in the regroup phase
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
