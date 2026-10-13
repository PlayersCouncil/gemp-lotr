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
 * A character that is killed was not discarded, and one that is discarded was not killed: a text of ANOTHER card
 * that answers the one does not answer the other.
 *
 * Comprehensive Rules 4.2, "killed": "This game term is used to describe a character that has had its vitality
 * reduced to zero or that is specifically 'killed' by the game text of another card. ...  This is different from
 * when a character is discarded from play.  Other cards may force a character in play to be discarded.  That
 * character has not been killed, but only discarded.  Cards that trigger when a character is killed will not
 * trigger when a character is discarded, and vice-versa.  Place killed Free Peoples characters in your dead pile.
 * Place killed minions in your discard pile."
 *
 * A killed minion goes to the discard pile all the same, which is how the game came to count it as discarded
 * (F-0444).  A text that says "killed or discarded" answers either, once.
 *
 * A card's OWN "when this is discarded" and a kill is OwnRemovalTriggerTests
 * (AMinionThatIsKilledWasNotDiscardedAndItsOwnDiscardTriggerIsNotSetOff); what goes to the discard pile with a
 * card that leaves play is LeavingPlayTests.
 */
public class KilledIsNotDiscardedTests
{
	protected VirtualTableScenario GetMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ceremonial Armor: [men] possession, support area.  "Each time a [men] minion is discarded
					// during the maneuver phase, you may stack it here."  Its Response answers a wound to a [men]
					// minion and has a cost: with no minion stacked here it discards the Armor, and it is declined.
					put("armor", "17_41");
					// Last Days: [men] condition, support area.  "Each time a [men] minion is killed or discarded
					// from play (except during the regroup phase), you may play a [men] minion.  Its twilight
					// cost is -2, and it is fierce and strength +2 until the regroup phase."
					put("days", "12_71");
					// Man of Bree: [men] Man, cost 2, vitality 1, site 4; strength 4 and +1 for each card in the
					// Free Peoples player's hand
					put("bree", "11_90");
					put("bree2", "11_90");
					// Corps of Harad: [men] Man, cost 4, strength 10, vitality 3, site 4; fierce only at a battleground
					put("corps", "11_73");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger, vitality 4
					// Defend It and Hope: [gondor] Maneuver event, cost 0.  "Exert a [gondor] Man to wound a
					// minion once (or twice if that minion is an Uruk-hai)."
					put("defend", "4_115");
					// What Are They?: [gondor] Maneuver event, cost 1.  "Spot a ranger to discard a roaming minion."
					put("whatAreThey", "1_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCompanionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Saruman, Agent of the Dark Lord: [uruk-hai] Wizard, vitality 4, lurker.  "Each time a
					// companion is killed, you may heal Saruman."  His Skirmish ability is not reached.
					put("saruman", "12_144");
					// Morgul Slayer: vitality 2.  "Regroup: Exert this minion to wound a companion (except the
					// Ring-bearer)."
					put("slayer", "3_93");
					// Fierce in Despair: [raider] Regroup event, cost 7.  "Spot 2 [raider] Men to discard a
					// companion (except the Ring-bearer)."
					put("despair", "7_148");
					put("axeman1", "4_224");    // Easterling Axeman: [raider] Man; nothing more below 2 burdens
					put("axeman2", "4_224");

					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AMinionThatIsKilledDoesNotSetOffATextThatAnswersAMinionDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var armor = scn.GetShadowCard("armor");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// The wound takes the last of Man of Bree's vitality: he is killed, and placed in the discard pile.  The
		// Armor's Response to the wound is declined on the way.
		scn.FreepsPlayCard(defend);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, bree);

		// He was not discarded: "Each time a [men] minion is discarded ..." is not set off.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMinionThatIsDiscardedSetsOffATextThatAnswersAMinionDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatAreThey = scn.GetFreepsCard("whatAreThey");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatAreThey);

		var armor = scn.GetShadowCard("armor");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// Man of Bree, site number 4, is roaming at site 2: the one minion to discard.
		scn.FreepsPlayCard(whatAreThey);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertSame(bree, scn.GetStackedCards(armor).get(0));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ATextThatAnswersAMinionKilledOrDiscardedAnswersAKillOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var days = scn.GetShadowCard("days");
		var bree = scn.GetShadowCard("bree");
		var bree2 = scn.GetShadowCard("bree2");
		var corps = scn.GetShadowCard("corps");
		scn.MoveCardsToSupportArea(days);
		// Two [men] minions to play, so that a second offer would have something to do.
		scn.MoveCardsToHand(corps, bree2);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SetTwilight(6);
		scn.SkipToPhase(Phase.MANEUVER);
		// 6 set + 2 companions + 2 for the site
		assertEquals(10, scn.GetTwilight());

		scn.FreepsPlayCard(defend);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(corps);
		// 4 - 2, + 2 for roaming
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertEquals(6, scn.GetTwilight());

		// The kill was one thing that happened, not a kill and a discard: there is no second offer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertInZone(Zone.HAND, bree2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ATextThatAnswersAMinionKilledOrDiscardedAnswersADiscardOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMinionScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatAreThey = scn.GetFreepsCard("whatAreThey");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatAreThey);

		var days = scn.GetShadowCard("days");
		var bree = scn.GetShadowCard("bree");
		var bree2 = scn.GetShadowCard("bree2");
		var corps = scn.GetShadowCard("corps");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(corps, bree2);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SetTwilight(6);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(10, scn.GetTwilight());

		// 1 for the event
		scn.FreepsPlayCard(whatAreThey);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(corps);
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertEquals(7, scn.GetTwilight());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(days));
		assertInZone(Zone.HAND, bree2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACompanionThatIsKilledSetsOffATextThatAnswersACompanionKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCompanionScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var saruman = scn.GetShadowCard("saruman");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(saruman, slayer);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.FreepsPass();

		// The Slayer's wound takes the last of the Dwarf Guard's vitality: he is killed, and placed in the dead pile.
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionThatIsDiscardedDoesNotSetOffATextThatAnswersACompanionKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCompanionScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var saruman = scn.GetShadowCard("saruman");
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(saruman, axeman1, axeman2);
		scn.AddWoundsToChar(saruman, 1);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// The Dwarf Guard is the one companion who is not the Ring-bearer.  He goes to the discard pile.
		scn.ShadowPlayCard(despair);
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetTwilight());

		// He was not killed: "Each time a companion is killed ..." is not set off, though Saruman could be healed.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
