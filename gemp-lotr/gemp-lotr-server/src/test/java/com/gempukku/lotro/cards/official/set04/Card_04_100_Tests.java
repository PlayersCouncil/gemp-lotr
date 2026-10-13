package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_100");
					// Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("gandalf", "1_72");
					// Glamdring: a hand weapon, cost 2.  "Bearer must be Gandalf."
					put("glamdring", "1_75");

					// What restores Shadowfax by legal play:
					// Mantle of the White Wizard, [gandalf] condition in the support area: "Each time you
					// play a spell, you may hinder a [gandalf] artifact to restore a Free Peoples card."
					put("mantle", "103_3");
					// Gandalf's Staff: a [gandalf] artifact, a staff and no hand weapon ("The twilight cost of
					// each [gandalf] spell is -1")
					put("staff", "2_22");
					// A New Light: [gandalf] Fellowship event, a spell, cost 1.  To play, spot a [gandalf]
					// Wizard.  It takes a minion from the Shadow player's discard pile, where there is none.
					put("light", "17_15");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Shadowfax
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Game Text: Bearer must be Gandalf. Discard any hand weapon he bears. Gandalf may not bear a hand weapon.<br>At the start of each skirmish involving Gandalf, each minion skirmishing him must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	// "Discard any hand weapon he bears" is a standing discard, apart from "Gandalf may not bear a hand weapon"
	// (Ketura on Q-0223 and Q-0229: two separate effects, and the discard is continuous).  While Shadowfax's text
	// is in force the bar keeps every hand weapon from reaching Gandalf, by a play or by a transfer, so the
	// discard is seen at two moments only: as Shadowfax is played on a Gandalf who bears one, here, and as it is
	// restored, in the two tests after.
	@Test
	public void WhenPlayedDiscardsTheHandWeaponGandalfBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("card");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring, staff);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.FreepsPlayAvailable(shadowfax));
		scn.FreepsPlayCard(shadowfax);

		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);
		// The Staff is no hand weapon: it stays on him
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder", on a hindered item: "the game text of such items does not apply, so any
	// restrictions that come from the game text of a hindered card are ignored", with Anduril, Flame of the West
	// 7_79 as the example: "Upon being restored, Anduril's game text resumes being applied, upon which Aragorn's
	// Bow would be discarded."  Shadowfax has the same two sentences, "Discard any hand weapon he bears.  Gandalf
	// may not bear a hand weapon".  A card restores it here, and a reconcile in the test after.
	@Test
	public void WhenRestoredDiscardsAHandWeaponGandalfTookUpWhileItWasHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("card");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var mantle = scn.GetFreepsCard("mantle");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax, staff);
		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCardsToHand(glamdring, light);

		scn.StartGame();

		// "Gandalf may not bear a hand weapon"
		assertFalse(scn.FreepsPlayAvailable(glamdring));

		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);

		// The spell is played, and the Mantle hinders the Staff (the one [gandalf] artifact) to restore Shadowfax
		scn.FreepsPlayCard(light);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(staff));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowfax, staff));
		scn.FreepsChooseCard(shadowfax);

		assertFalse(scn.IsHindered(shadowfax));
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);
		// The Staff is no hand weapon: it stays on him
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// The same board with the Free Peoples player's own reconcile doing the restoring, which has no card behind it
	// (Comprehensive Rules 5.0, "reconcile": "When any player reconciles (before they discard any cards from
	// hand), restore all cards owned by that player").
	@Test
	public void WhenRestoredByAReconcileDiscardsAHandWeaponGandalfTookUpWhileItWasHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("card");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax, staff);
		// The spell stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(glamdring, light);

		scn.StartGame();
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.IsHindered(shadowfax));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(shadowfax));
		assertAttachedTo(glamdring, gandalf);

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(shadowfax));
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);
		// The Staff is no hand weapon: it stays on him
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.HAND, light);
	}
}
