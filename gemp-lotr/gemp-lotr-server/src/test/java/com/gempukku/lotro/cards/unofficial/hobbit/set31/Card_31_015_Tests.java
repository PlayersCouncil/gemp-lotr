package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_015_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwaihir", "31_15");
					put("balin", "30_2");       // Balin, Brother of Dwalin: a [dwarven] companion; a fellowship ability only
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: another companion; a maneuver ability used only on purpose

					// Cards for the hand. Gandalf, The Grey: a [gandalf] character. Glamdring: a [gandalf] artifact.
					// Take up arms. Fight!: a [gandalf] event. Battle Fury: a [dwarven] event.
					put("gandalf", "30_27");
					put("glamdring", "30_29");
					put("takeup", "30_31");
					put("fury", "30_3");

					// Orkish Marauder: a [gundabad] Orc, on Warg 31_27 (a [gundabad] mount; a regroup ability only)
					put("marauder", "30_38");
					put("warg", "31_27");
					// Goblin Footman: a [moria] Orc, on Vicious Warg 32_32 (a mount for any Orc; a play trigger not
					// reached), or bearing Goblin Scimitar 31_31 (a hand weapon, no mount; a play trigger not reached)
					put("footman", "30_42");
					put("viciouswarg", "32_32");
					put("scimitar", "31_31");
					// Úlairë Nelya, Revived: a Nazgûl, no Orc, on Fell Beast 6_83 (a mount for a Nazgûl; bearer fierce)
					put("nelya", "32_63");
					put("fellbeast", "6_83");
					put("runner", "31_30");     // Goblin Runner: a plain [moria] Orc
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	// A table in the Multipath format, where a skirmish involving the Ring-bearer may not be cancelled: the
	// Fellowship path, Frodo 1_290 as the Ring-bearer and The Ruling Ring.
	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwaihir", "31_15");
					put("marauder", "30_38");   // Orkish Marauder: a [gundabad] Orc
					put("warg", "31_27");       // Warg: a [gundabad] mount
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void GwaihirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Gwaihir, Lord of the Eagles
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Eagle
		 * Strength: +1
		 * Aid: Discard a [gandalf] card from hand.
		 * Game Text: <b>Aid</b> – Discard a [gandalf] card from hand.
		 * 	<b>Skirmish:</b> Transfer Gwaihir to your support area to cancel a skirmish involving bearer and a mounted Orc.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gwaihir");

		assertEquals("Gwaihir", card.getBlueprint().getTitle());
		assertEquals("Lord of the Eagles", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.EAGLE, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidDiscardsAGandalfCardFromHandToTransferGwaihir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var takeup = scn.GetFreepsCard("takeup");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(gwaihir);

		scn.StartGame();
		scn.MoveCardsToHand(gandalf, glamdring, takeup, fury);
		scn.MoveMinionsToTable("runner");
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(gandalf, glamdring, takeup, fury);
		assertInZone(Zone.SUPPORT, gwaihir);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir, "Aid"));
		scn.FreepsAcceptOptionalTrigger();

		// A [gandalf] character, item and event may pay; a [dwarven] card may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, glamdring, takeup));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fury));
		scn.FreepsChooseCard(takeup);
		assertInDiscard(takeup);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(gandalf, glamdring, fury);

		scn.FreepsChooseCard(balin);
		assertAttachedTo(gwaihir, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWithNoGandalfCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(gwaihir);

		scn.StartGame();
		scn.MoveCardsToHand(fury);
		scn.MoveMinionsToTable("runner");
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, gwaihir);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gwaihir, "Aid"));
		assertInZone(Zone.SUPPORT, gwaihir);
		assertInHand(fury);
		assertEquals(1, scn.GetFreepsHandCount());
	}

	@Test
	public void SkirmishAbilityTransfersGwaihirToCancelBearersSkirmishWithAMountedGundabadOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var marauder = scn.GetShadowCard("marauder");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		// The hand is empty, so Gwaihir's Aid is not offered at the start of the maneuver phase.
		scn.AttachCardsTo(balin, gwaihir);
		scn.MoveMinionsToTable(marauder);
		scn.AttachCardsTo(marauder, warg);
		assertAttachedTo(gwaihir, balin);
		assertAttachedTo(warg, marauder);

		// Not in the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gwaihir));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(balin, marauder);
		scn.FreepsResolveSkirmish(balin);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(marauder));
		assertTrue(scn.IsCharSkirmishing(balin));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertFalse(scn.IsCharSkirmishing(balin));
		assertFalse(scn.IsCharSkirmishing(marauder));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(marauder));
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		// The Warg's fierce brings the fierce round on, the only skirmish having been cancelled.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnOrcBearingAHandWeaponThatIsNoMountIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var footman = scn.GetShadowCard("footman");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, gwaihir);
		scn.MoveMinionsToTable(footman);
		scn.AttachCardsTo(footman, scimitar);
		assertAttachedTo(gwaihir, balin);
		assertAttachedTo(scimitar, footman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, footman);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishing(balin));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		// The transfer is paid, and the skirmish goes on.
		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMountedNazgulIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var nelya = scn.GetShadowCard("nelya");
		var fellbeast = scn.GetShadowCard("fellbeast");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.AttachCardsTo(balin, gwaihir);
		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, fellbeast);
		assertAttachedTo(gwaihir, balin);
		assertAttachedTo(fellbeast, nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, nelya);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishing(balin));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnotherCompanionsSkirmishWithAMountedOrcIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var marauder = scn.GetShadowCard("marauder");
		var warg = scn.GetShadowCard("warg");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);

		scn.StartGame();
		scn.AttachCardsTo(balin, gwaihir);
		scn.MoveMinionsToTable(marauder, runner);
		scn.AttachCardsTo(marauder, warg);
		assertAttachedTo(gwaihir, balin);
		assertAttachedTo(warg, marauder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{dwalin, marauder},
				new PhysicalCardImpl[]{balin, runner});
		// Dwalin's skirmish first, while Balin, the bearer, waits for his own.
		scn.FreepsResolveSkirmish(dwalin);
		assertTrue(scn.IsCharSkirmishing(dwalin));
		assertTrue(scn.IsCharAssigned(balin));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.IsCharSkirmishingAgainst(dwalin, marauder));
		assertTrue(scn.IsCharAssigned(balin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBearersSkirmishWithAnUnmountedOrcIsNotCancelledWhileAMountedOrcWaitsForAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var marauder = scn.GetShadowCard("marauder");
		var warg = scn.GetShadowCard("warg");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);

		scn.StartGame();
		scn.AttachCardsTo(balin, gwaihir);
		scn.MoveMinionsToTable(marauder, runner);
		scn.AttachCardsTo(marauder, warg);
		assertAttachedTo(gwaihir, balin);
		assertAttachedTo(warg, marauder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{dwalin, marauder},
				new PhysicalCardImpl[]{balin, runner});
		// Balin's skirmish first: the mounted Orc is in play and assigned, but waits for Dwalin's skirmish.
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, runner));
		assertTrue(scn.IsCharAssigned(dwalin));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, runner));
		assertTrue(scn.IsCharAssigned(dwalin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void RingBearersSkirmishWithAMountedMoriaOrcIsCancelledAtTheHobbitTable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var bilbo = scn.GetRingBearer();
		var footman = scn.GetShadowCard("footman");
		var viciouswarg = scn.GetShadowCard("viciouswarg");

		scn.StartGame();
		scn.AttachCardsTo(bilbo, gwaihir);
		scn.MoveMinionsToTable(footman);
		scn.AttachCardsTo(footman, viciouswarg);
		assertAttachedTo(gwaihir, bilbo);
		assertAttachedTo(viciouswarg, footman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, footman);
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.IsCharSkirmishing(bilbo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		// The Hobbit Draft Game lets a skirmish involving the Ring-bearer be cancelled.
		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertFalse(scn.IsCharSkirmishing(bilbo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RingBearersSkirmishIsNotCancelledInTheMultipathFormat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var frodo = scn.GetRingBearer();
		var marauder = scn.GetShadowCard("marauder");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.AttachCardsTo(frodo, gwaihir);
		scn.MoveMinionsToTable(marauder);
		scn.AttachCardsTo(marauder, warg);
		assertAttachedTo(gwaihir, frodo);
		assertAttachedTo(warg, marauder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, marauder);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.FreepsActionAvailable(gwaihir));

		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, marauder));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
