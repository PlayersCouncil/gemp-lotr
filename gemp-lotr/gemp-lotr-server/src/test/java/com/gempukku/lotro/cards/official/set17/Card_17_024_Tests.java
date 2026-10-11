package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "17_24");
					put("glamdring", "1_75");
					put("glamdring2", "6_31");
					put("pipe", "1_74");
					put("staff", "2_29");
					put("gandalf", "1_364");
					put("radagast", "9_26");     // Radagast, The Brown: the [gandalf] Wizard who is not Gandalf
					put("bag", "18_26");         // Radagast's Herb Bag: possession with no class, cost 1; bearer must be a [gandalf] Wizard
					put("hobbitglamdring", "30_29"); // Glamdring (The Hobbit): [gandalf] artifact, hand weapon, bearer must be Gandalf; its trigger needs a skirmish won
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Shadowfax, Greatest of the Mearas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 1
		 * Vitality: 1
		 * Resistance: 1
		 * Game Text: Bearer must be a [gandalf] Wizard. Discard all other possessions on bearer and bearer may not bear a hand weapon.<br><b>Fellowship:</b> Add a threat to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Greatest of the Mearas", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void ShadowfaxPlaysOnaGandalfWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(shadowfax);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.RemoveBurdens(1);

		assertEquals(Zone.HAND, shadowfax.getZone());
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetVitality(gandalf));
		assertEquals(6, scn.GetResistance(gandalf));

		scn.FreepsPlayCard(shadowfax);
		assertEquals(Zone.ATTACHED, shadowfax.getZone());
		assertEquals(gandalf, shadowfax.getAttachedTo());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(5, scn.GetVitality(gandalf));
		assertEquals(7, scn.GetResistance(gandalf));
	}

	/**
	 * Legacy Ruling #1 decided that GOTM is to be read as having a perpetual discard effect on
	 * all possessions.  Weapons remain unable to be borne by bearer.
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_1">Legacy Ruling #1 on the wiki</a>
	 */
	@Test
	public void ShadowfaxDiscardsPossessionsOnBearerBothBeforeAndAfterPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring2 = scn.GetFreepsCard("glamdring2");
		var pipe = scn.GetFreepsCard("pipe");
		var staff = scn.GetFreepsCard("staff");

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(shadowfax, staff, glamdring2);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, pipe, glamdring);

		scn.StartGame();

		assertEquals(Zone.HAND, shadowfax.getZone());
		assertEquals(Zone.ATTACHED, glamdring.getZone());
		assertEquals(Zone.ATTACHED, pipe.getZone());
		scn.FreepsPlayCard(shadowfax);
		assertEquals(Zone.ATTACHED, shadowfax.getZone());
		assertEquals(Zone.DISCARD, glamdring.getZone());
		assertEquals(Zone.DISCARD, pipe.getZone());

		assertEquals(Zone.HAND, staff.getZone());
		scn.FreepsPlayCard(staff);
		assertEquals(Zone.DISCARD, staff.getZone());

		assertEquals(Zone.HAND, glamdring2.getZone());
		assertFalse(scn.FreepsPlayAvailable(glamdring2));
	}

	@Test
	public void ShadowfaxFellowshipAbilityAddsAThreatToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}

	// Legacy Ruling 1: "Possessions may still be played on or transferred to bearer, but they will immediately be
	// discarded once attached."  The play is in the test above; this is the transfer.
	@Test
	public void APossessionTransferredToTheBearerIsDiscardedAsItArrives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var bag = scn.GetFreepsCard("bag");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(radagast, bag);

		scn.StartGame();

		assertAttachedTo(bag, radagast);
		// Gandalf is the only other Wizard who could bear the Herb Bag
		assertTrue(scn.FreepsTransferAvailable(bag));
		scn.FreepsTransferCard(bag);

		// 1 for the transfer
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, bag);
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Shadowfax is hindered its text does not apply, and a possession
	// played on its bearer stays.  When it is restored the standing discard is in force again and the possession
	// is discarded (Ketura on Q-0223).  The restore here is the Free Peoples player's own reconcile.
	@Test
	public void APossessionTheBearerTookUpWhileItWasHinderedIsDiscardedWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pipe = scn.GetFreepsCard("pipe");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		// The Wizard Staff stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(pipe, staff);

		scn.StartGame();
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, gandalf);
		assertTrue(scn.IsHindered(shadowfax));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(shadowfax));
		assertAttachedTo(pipe, gandalf);

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(shadowfax));
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, pipe);
		assertInZone(Zone.HAND, staff);
	}

	// Ketura on Q-0223: the discard and the bar are two effects that name different things.  "An artifact hand
	// weapon that was on bearer before GOTM was played would therefore not be discarded: the discard effect only
	// hits possessions, and 'cannot bear' only prevents new bearings."  The only artifact hand weapon a [gandalf]
	// Wizard can bear is Glamdring of The Hobbit sets (F-0427).
	@Test
	public void AnArtifactHandWeaponBorneBeforeItIsPlayedIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("hobbitglamdring");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(gandalf);
		// An artifact that is a hand weapon, and a possession
		scn.AttachCardsTo(gandalf, glamdring, pipe);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		scn.FreepsPlayCard(shadowfax);

		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, pipe);
		assertAttachedTo(glamdring, gandalf);
		// 7, + 1 for Shadowfax, + 2 for Glamdring
		assertEquals(10, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
