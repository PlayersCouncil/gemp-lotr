package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that is hindered on a bearer who is not.
 *
 * Comprehensive Rules 5.0, "hinder": "If a borne card is being hindered but not the card it is borne by, all stat
 * bonuses it provides are temporarily revoked from its bearer.  This means that death can occur if a card granting
 * a vitality bonus is hindered on an exhausted character, or corruption can occur if a resistance-boosting card is
 * hindered."  Its game text is gone with them ("The entire game text of hindered cards does not apply").
 *
 * What stays is its item class.  The same entry, on "passive play restriction": "If the hindered card is an item
 * (possession or artifact), it continues to restrict its bearer from bearing any other cards of the same item
 * class via play or transfer. ...  However, note that the game text of such items does not apply, so any
 * restrictions that come from the game text of a hindered card are ignored.  Example: If Aragorn bears Anduril,
 * Flame of the West, he cannot bear Aragorn's Bow due to Anduril's game text.  If Anduril is hindered, a Ranger's
 * Sword cannot be played on Aragorn due to passive item class restrictions.  But because Anduril's game text does
 * not apply, Aragorn's Bow can now be played.  Upon being restored, Anduril's game text resumes being applied,
 * upon which Aragorn's Bow would be discarded."
 *
 * Where hindering the item is only how the board is laid, the rig does it.  In the test of a death it is the act,
 * and a card does it.
 */
public class HinderedBorneCardTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("aragorn", "1_89");
					// Anduril, Flame of the West: artifact, hand weapon, strength +2, vitality +1.  "Bearer must
					// be Aragorn.  Discard other weapons he bears.  He is damage +1 and cannot bear other
					// weapons."  Its own ability needs site 2 or 5.
					put("anduril", "7_79");
					// Ranger's Sword: hand weapon, cost 1.  "Bearer must be Aragorn.  He is damage +1."
					put("sword", "1_112");
					// Aragorn's Bow: ranged weapon, cost 1.  "Bearer must be Aragorn.  He is an archer."  Its
					// archery ability is never used: no test here has an archery phase with the Bow in play.
					put("bow", "1_90");

					// Aragorn, Captain of Gondor: Knight; strength 8, vitality 4, resistance 6.  His offer at
					// the start of the fellowship phase is why he is put down after it.  Never on a table with
					// the other Aragorn.
					put("captain", "7_81");
					// Gondorian Steed: mount, strength +1, resistance +1.  "Bearer must be a knight.  Bearer is
					// damage +1."  Its other text answers a move from a mountain.
					put("steed", "12_49");

					// Grima, Wormtongue (the erratum): "Maneuver: Exert Grima to spot an unbound companion.
					// Hinder a weapon borne by that companion (or all Free Peoples cards borne by them if they
					// bear 3 or more cards)."  Strength 4, vitality 3.
					put("grima", "54_154");
					// Arwen, Daughter of Elrond, and Gwemegil (hand weapon, strength +2, "Bearer must be Arwen"):
					// a second companion Grima could choose
					put("arwen", "1_30");
					put("gwemegil", "1_47");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedItemGivesItsBearerNoBonusAndNoKeyword() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var anduril = scn.GetFreepsCard("anduril");
		var steed = scn.GetFreepsCard("steed");

		scn.StartGame();
		scn.MoveCompanionsToTable(captain);
		scn.AttachCardsTo(captain, anduril, steed);

		// 8 + 2 for Anduril + 1 for the Steed
		assertEquals(11, scn.GetStrength(captain));
		// 4 + 1 for Anduril
		assertEquals(5, scn.GetVitality(captain));
		// 6 + 1 for the Steed, and no burdens
		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetResistance(captain));
		// Damage +1 from each
		assertEquals(2, scn.GetKeywordCount(captain, Keyword.DAMAGE));

		scn.HinderCard(anduril);
		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(steed));
		assertFalse(scn.IsHindered(captain));
		// The Steed's are left
		assertEquals(9, scn.GetStrength(captain));
		assertEquals(4, scn.GetVitality(captain));
		assertEquals(7, scn.GetResistance(captain));
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));

		scn.HinderCard(steed);
		assertTrue(scn.IsHindered(steed));
		assertFalse(scn.IsHindered(captain));
		assertEquals(8, scn.GetStrength(captain));
		assertEquals(4, scn.GetVitality(captain));
		assertEquals(6, scn.GetResistance(captain));
		assertFalse(scn.HasKeyword(captain, Keyword.DAMAGE));
		// Both are still his to bear
		assertAttachedTo(anduril, captain);
		assertAttachedTo(steed, captain);

		// "Temporarily revoked"
		scn.RestoreCard(anduril, steed);
		assertEquals(11, scn.GetStrength(captain));
		assertEquals(5, scn.GetVitality(captain));
		assertEquals(7, scn.GetResistance(captain));
		assertEquals(2, scn.GetKeywordCount(captain, Keyword.DAMAGE));
	}

	@Test
	public void ABearerKeptAliveByAnItemsVitalityIsKilledWhenTheItemIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(arwen, gwemegil);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		// Four wounds on a vitality of 4 + 1
		scn.AddWoundsToChar(aragorn, 4);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetVitality(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, arwen));
		scn.ShadowChooseCard(aragorn);

		// Anduril is the one weapon he bears and is hindered with nothing more asked.  His vitality is 4 again
		// under 4 wounds: he is killed, and what he bore is discarded.
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, anduril);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertAttachedTo(gwemegil, arwen);
		assertFalse(scn.IsHindered(gwemegil));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderedItemStillBarsAnotherOfItsClassWhileItsOwnTextBarsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sword, bow);

		scn.StartGame();

		// The control: with no Anduril, either weapon can be played on him
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.FreepsPlayAvailable(bow));

		// Anduril: a hand weapon, as the Sword is, whose text keeps every other weapon off him
		scn.AttachCardsTo(aragorn, anduril);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsPlayAvailable(sword));
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.HinderCard(anduril);
		scn.FreepsRefreshDecision();
		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(aragorn));
		// Still a hand weapon on him; no longer a text that bars the Bow
		assertFalse(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.FreepsPlayAvailable(bow));

		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(anduril, aragorn);
		assertTrue(scn.IsHindered(anduril));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// The last sentence of the rules' example.  The restoring is the Free Peoples player's own reconcile, which has
	// no card behind it.  Where a card restores Anduril the Bow is discarded (F-0360; Card_07_079_Tests reads it).
	@Test
	@Ignore("F-0361: a card's 'restored' trigger does not answer a reconcile's restore, so Anduril 7_79 restored by its player's reconcile leaves on Aragorn the Bow he took up while it was hindered")
	public void AWeaponTakenUpWhileAnItemThatBarsItWasHinderedIsDiscardedWhenThatItemIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		// The Sword stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(sword, bow);

		scn.StartGame();
		scn.HinderCard(anduril);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);
		assertTrue(scn.IsHindered(anduril));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(anduril));
		assertAttachedTo(bow, aragorn);

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(anduril));
		assertAttachedTo(anduril, aragorn);
		assertInZone(Zone.DISCARD, bow);
		assertInZone(Zone.HAND, sword);
	}
}
