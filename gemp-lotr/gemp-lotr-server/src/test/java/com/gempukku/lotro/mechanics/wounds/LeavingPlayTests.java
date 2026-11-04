package com.gempukku.lotro.mechanics.wounds;

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
 * What a card bears, and what is stacked on it, goes to the discard pile when the card leaves play.
 *
 * Comprehensive Rules 4.2, "leaving play": "When a card (other than a site) leaves play for any reason, any cards
 * played on that card (or borne by or stacked on that card) are discarded.  Any tokens on that card are removed."
 * And "stack": "Stacked cards are not in play and are not active."
 *
 * So a borne card that goes with its bearer was discarded from play, and a text that answers a discard from play
 * answers it: Ketura on Q-0081, any discard from play is answered, a discard with its bearer included.  (Only an
 * answer that would PREVENT such a discard is not offered, Q-0023: the saved card would have nowhere to go.)  The
 * same holds one step further down, for a card borne by a borne card.  A card stacked on the card that leaves is
 * put in its owner's discard pile too, but it was never in play, and its own "when this is discarded" is not set
 * off.
 *
 * A bearer who is killed, with a card that answers what he bore, is in OwnRemovalTriggerTests
 * (ACardDiscardedWithItsBearerByNoCardDoesNotMeetAConditionOnWhatDiscardedIt); here the bearer is discarded.
 * The hindered mark of a card that leaves play is HinderedCardLeavingPlayTests.
 */
public class LeavingPlayTests
{
	protected VirtualTableScenario GetBorneScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard1", "5_83");      // Household Guard: [rohan] Man, not unique; Valiant is its whole text
					put("guard2", "5_83");
					// Rider's Spear: [rohan] possession, a hand weapon on a [rohan] Man; damage +1 only if he is mounted
					put("spear", "4_288");
					// Sword Rack: [rohan] condition, support area.  "Response: If a [rohan] possession is discarded
					// from play and no more than one possession is stacked here, stack it here."
					put("rack", "11_158");

					// Fierce in Despair: [raider] Regroup event, cost 7.  "Spot 2 [raider] Men to discard a
					// companion (except the Ring-bearer)."
					put("despair", "7_148");
					put("axeman1", "4_224");    // Easterling Axeman: [raider] Man; nothing more below 2 burdens
					put("axeman2", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBorneByABorneCardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3; +2 only with a weapon
					// Alatar, Final Envoy: [gandalf] follower.  Borne, his one text needs a [gandalf] companion
					put("alatar", "13_28");
					// Whisper in the Dark: [men] Shadow condition.  "Bearer must be a follower."  Its trigger needs
					// a [men] Man
					put("whisper", "18_77");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Teeth of Mordor: [sauron] condition, support area.  "Regroup: Spot a [sauron] Orc and remove
					// (3) to stack that minion here."  How a Mordor Guard comes to be stacked on it
					put("teeth", "5_110");
					// Mordor Guard: [sauron] Orc.  "When this minion is discarded, remove 3 threats."
					put("stacked", "7_287");
					put("inplay", "7_287");

					// Stand Against Darkness: Maneuver event, cost 1.  "Exert an Elf to discard a [sauron] minion,
					// a [sauron] condition, or a [sauron] possession."
					put("stand1", "1_63");
					put("stand2", "1_63");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf, vitality 3; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhatACardBearsIsDiscardedFromPlayWithItWhenItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBorneScenario();

		var bearer = scn.GetFreepsCard("guard1");
		var other = scn.GetFreepsCard("guard2");
		var spear = scn.GetFreepsCard("spear");
		var rack = scn.GetFreepsCard("rack");
		scn.MoveCompanionsToTable(bearer, other);
		scn.AttachCardsTo(bearer, spear);
		scn.MoveCardsToSupportArea(rack);

		var despair = scn.GetShadowCard("despair");
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("axeman1"), scn.GetShadowCard("axeman2"));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertAttachedTo(spear, bearer);
		assertEquals(0, scn.GetStackedCards(rack).size());
		int discards = scn.GetFreepsDiscardCount();

		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(bearer, other));
		scn.ShadowChooseCard(bearer);

		// The Guard was discarded, not killed, and his spear went to the discard pile with him
		assertInZone(Zone.DISCARD, bearer, spear);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(discards + 2, scn.GetFreepsDiscardCount());
		assertInZone(Zone.FREE_CHARACTERS, other);

		// The spear was discarded from play: the Sword Rack, which answers that, is offered to its owner.  (What
		// the Rack then does with the spear is Card_11_158_Tests'.)
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, bearer, spear);
		assertEquals(0, scn.GetStackedCards(rack).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhatABorneCardBearsGoesWithThemBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBorneByABorneCardScenario();

		var merry = scn.GetFreepsCard("merry");
		var alatar = scn.GetFreepsCard("alatar");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, alatar);

		// The Shadow player's condition, on the Free Peoples player's follower
		var whisper = scn.GetShadowCard("whisper");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(alatar, whisper);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		assertAttachedTo(alatar, merry);
		assertAttachedTo(whisper, alatar);
		int freepsDiscards = scn.GetFreepsDiscardCount();
		int shadowDiscards = scn.GetShadowDiscardCount();

		scn.PassSkirmishActions();

		// Merry, 3 against 13, is overwhelmed and killed.  The follower he bore goes to its owner's discard
		// pile, and the condition on the follower to the other player's: neither is left on the table.
		assertInZone(Zone.DEAD, merry);
		assertInZone(Zone.DISCARD, alatar, whisper);
		assertEquals(0, scn.GetAttachedCards(alatar).size());
		assertEquals(freepsDiscards + 1, scn.GetFreepsDiscardCount());
		assertEquals(shadowDiscards + 1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardStackedOnACardThatLeavesPlayGoesToTheDiscardPileAndWasNotDiscardedFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var stand1 = scn.GetFreepsCard("stand1");
		var stand2 = scn.GetFreepsCard("stand2");
		scn.MoveCompanionsToTable(legolas, scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(stand1, stand2);

		var teeth = scn.GetShadowCard("teeth");
		var stacked = scn.GetShadowCard("stacked");
		var inplay = scn.GetShadowCard("inplay");
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, stacked);

		scn.StartGame();
		// Four companions, so four threats.
		scn.AddThreats(4);
		scn.MoveMinionsToTable(inplay);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.STACKED, stacked);
		assertTrue(scn.GetStackedCards(teeth).contains(stacked));
		assertEquals(4, scn.GetThreats());
		int shadowDiscards = scn.GetShadowDiscardCount();

		scn.FreepsPlayCard(stand1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(teeth, inplay));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stacked));
		scn.FreepsChooseCard(teeth);

		// The condition is discarded and the Guard stacked on it is put in its owner's discard pile with it.
		// That Guard was not in play and was not discarded from play: its own text removes no threat.
		assertInZone(Zone.DISCARD, teeth, stacked);
		assertEquals(shadowDiscards + 2, scn.GetShadowDiscardCount());
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, inplay);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(stand2);

		// The control: the same card discarded from play by the same event removes its 3 threats, 4 - 3.
		assertInZone(Zone.DISCARD, inplay);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
