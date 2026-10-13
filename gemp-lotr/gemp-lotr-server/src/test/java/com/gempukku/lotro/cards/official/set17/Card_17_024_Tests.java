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

					put("gandalf", "1_364");     // Gandalf, The Grey Wizard: strength 7, vitality 4, resistance 6; a fellowship ability used only on purpose
					put("radagast", "9_26");     // Radagast, The Brown: the [gandalf] Wizard who is not Gandalf
					put("saruman", "32_24");     // Saruman, The White: the [gandalf] Wizard who is an ally; gives Gandalf defender +1 and has abilities used only on purpose
					put("avenger", "6_27");      // Ent Avenger: a [gandalf] companion who is no Wizard
					put("grimbeorn", "14_6");    // Grimbeorn: a [gandalf] Man, the bearer a Beorning Axe wants; heals himself as a turn starts

					put("glamdring", "1_75");    // Glamdring: possession, hand weapon, cost 2; bearer must be Gandalf
					put("glamdring2", "6_31");   // Glamdring, Lightning Brand: possession, hand weapon, cost 2; bearer must be Gandalf
					put("hobbitglamdring", "30_29"); // Glamdring (The Hobbit): [gandalf] artifact, hand weapon, bearer must be Gandalf; its trigger needs a skirmish won
					put("axe", "18_18");         // Beorning Axe: possession, hand weapon, cost 2; bearer must be a [gandalf] Man
					put("pipe", "1_74");         // Gandalf's Pipe: possession, pipe, cost 0; bearer must be Gandalf
					put("staff", "2_29");        // Wizard Staff: possession, staff, cost 2; bearer must be a Wizard
					put("bag", "18_26");         // Radagast's Herb Bag: possession with no class, cost 1; bearer must be a [gandalf] Wizard
					put("gandalfsstaff", "4_91"); // Gandalf's Staff, Walking Stick: an artifact that is no weapon, cost 2; vitality +1
					put("poison", "8_28");       // Spider Poison: a Shadow possession that a companion bears
					put("evil", "1_283");        // You Bring Great Evil: a Shadow condition that a companion bears; only matters at a sanctuary
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
		 * Strength: +1
		 * Vitality: +1
		 * Resistance: +1
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
	public void PlaysOnAGandalfWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var saruman = scn.GetFreepsCard("saruman");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, radagast, avenger);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(radagast));
		assertTrue(scn.FreepsPlayAvailable(shadowfax));
		scn.FreepsPlayCard(shadowfax);

		// Ent Avenger is a [gandalf] companion and no Wizard; Frodo is neither.  Every printed Free Peoples
		// Wizard is a [gandalf] card, so no Wizard of another culture stands here.
		// Saruman is a [gandalf] Wizard who is an ally, not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(shadowfax, gandalf);
		assertEquals(8, scn.GetStrength(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedItDiscardsEveryOtherPossessionOnItsBearerAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var glamdring = scn.GetFreepsCard("glamdring");
		var pipe = scn.GetFreepsCard("pipe");
		var gandalfsstaff = scn.GetFreepsCard("gandalfsstaff");
		var wizardstaff = scn.GetFreepsCard("staff");
		var poison = scn.GetShadowCard("poison");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCompanionsToTable(gandalf, radagast);
		// On Gandalf: a hand weapon, a possession of another class, a Shadow possession, an artifact and a condition.
		scn.AttachCardsTo(gandalf, glamdring, pipe, poison, gandalfsstaff, evil);
		scn.AttachCardsTo(radagast, wizardstaff);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(poison, gandalf);
		scn.FreepsPlayCard(shadowfax);
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(shadowfax, gandalf);
		assertInDiscard(glamdring, pipe, poison);
		// An artifact and a condition are no possessions.
		assertAttachedTo(gandalfsstaff, gandalf);
		assertAttachedTo(evil, gandalf);
		// Another companion's possession is not on the bearer.
		assertAttachedTo(wizardstaff, radagast);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Legacy Ruling 1: "This card continuously discards all possessions on its bearer. Possessions may still be
	// played on or transferred to bearer, but they will immediately be discarded once attached."
	@Test
	public void APossessionPlayedOnTheBearerLaterIsPaidForAndDiscardedAsItArrives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var wizardstaff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(wizardstaff);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(wizardstaff));
		scn.FreepsPlayCard(wizardstaff);
		// The bearer of Shadowfax is still a bearer the Wizard Staff may be played on.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		scn.FreepsChooseCard(gandalf);

		// Its cost is paid and it is gone: Gandalf has Shadowfax's +1 and not the Staff's.
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(wizardstaff);
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBearerMayNotBeGivenAHandWeaponWhileOtherItemsAndOtherBearersMay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var glamdring = scn.GetFreepsCard("glamdring2");
		var hobbitglamdring = scn.GetFreepsCard("hobbitglamdring");
		var gandalfsstaff = scn.GetFreepsCard("gandalfsstaff");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gandalf, grimbeorn);
		// Two hand weapons for Gandalf, a possession and an artifact; an artifact for him that is no weapon; and
		// a hand weapon for Grimbeorn.
		scn.MoveCardsToHand(shadowfax, glamdring, hobbitglamdring, gandalfsstaff, axe);

		scn.StartGame();

		// Without Shadowfax each of them can be played.
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		assertTrue(scn.FreepsPlayAvailable(hobbitglamdring));
		assertTrue(scn.FreepsPlayAvailable(gandalfsstaff));
		assertTrue(scn.FreepsPlayAvailable(axe));

		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);

		// Gandalf is the only bearer either Glamdring may have, and he may not bear a hand weapon now.
		assertFalse(scn.FreepsPlayAvailable(glamdring));
		assertFalse(scn.FreepsPlayAvailable(hobbitglamdring));
		assertTrue(scn.FreepsPlayAvailable(gandalfsstaff));
		assertTrue(scn.FreepsPlayAvailable(axe));

		// An artifact that is no hand weapon goes on him and stays: 2 for Shadowfax + 2 for the Staff
		scn.FreepsPlayCard(gandalfsstaff);
		assertAttachedTo(gandalfsstaff, gandalf);
		assertEquals(4, scn.GetTwilight());

		// "Bearer" is Gandalf alone: Grimbeorn takes up his Axe.  + 2 for the Axe
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, grimbeorn);
		assertEquals(6, scn.GetTwilight());

		assertInZone(Zone.HAND, glamdring, hobbitglamdring);
		assertFalse(scn.FreepsPlayAvailable(glamdring));
		assertFalse(scn.FreepsPlayAvailable(hobbitglamdring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityAddsAThreatToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);

		// A threat is the whole cost: the bearer does not exert.
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceThereAreAsManyThreatsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.AddBurdens(3);
		// Two companions, Frodo and Gandalf: a second threat can be added and a third cannot.
		scn.AddThreats(1);

		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());

		// No threat can be added, though there are burdens left to remove.
		assertFalse(scn.FreepsActionAvailable(shadowfax));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
