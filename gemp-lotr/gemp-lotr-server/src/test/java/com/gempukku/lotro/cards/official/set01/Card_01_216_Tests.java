package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_216_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "1_216");
					put("blade2", "1_216");
					// Blade Tip: [wraith] condition, cost 0.  Its own response moves it from the support area to a
					// character who loses to a Nazgûl, which no test here reaches.  Two copies, so that a choice of
					// them is asked.
					put("tip", "1_209");
					put("tip2", "1_209");
					// Black Breath: another [wraith] condition that lies in the support area; its own skirmish
					// ability is never used here
					put("breath", "1_207");

					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, strength 10; fierce and damage +1 only
					// at 2 burdens or 2 wounds on the Ring-bearer, which no test here reaches
					put("nelya", "2_84");
					// Úlairë Attëa, Khamûl of the East: the [raider] Nazgûl; fierce, and nothing here is fought
					put("khamul", "103_61");
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl
					put("savage", "1_151");     // Uruk Savage: strength 5, a minion for Gimli to fight

					// Hand weapons a Nazgûl may bear, with no text that reaches a test here
					put("sword", "1_218");      // Nazgûl Sword: damage +1 only at 3 burdens
					put("nazgulblade", "12_168"); // Nazgûl Blade: strength +1 only at a battleground or forest

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6; his skirmish ability is never used
					// Bounder: a Hobbit ally whose home is site 2, where the skirmishes here are fought
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "1_216");
					// Blade Tip: [wraith] condition, cost 0.  Its own response moves it from the support area to a
					// character who loses to a Nazgûl, which no test here reaches.
					put("tip", "1_209");
					// Úlairë Nelya, Ringwraith in Twilight: Nazgûl, strength 10; fierce and damage +1 only at 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					put("savage", "1_151");     // Uruk Savage: a minion for the companion Nelya is not fighting

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion Nelya fights
					put("gimli", "1_13");       // Gimli, Son of Glóin: assigned to the Savage, his skirmish not yet begun
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a Nazgûl.<br>This weapon may be borne in addition to 1 other hand weapon.<br><b>Skirmish:</b> Discard this possession to transfer Blade Tip from your support area or discard pile to a companion bearer is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Morgul Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void DiscardedToTransferBladeTipFromTheSupportAreaToTheCompanionItsBearerIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var tip = scn.GetShadowCard("tip");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(tip);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.AttachCardsTo(nelya, blade);
		assertAttachedTo(blade, nelya);
		assertInZone(Zone.SUPPORT, tip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, nelya }, new PhysicalCardImpl[] { gimli, savage });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertAttachedTo(blade, nelya);
		assertInZone(Zone.SUPPORT, tip);
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);
		// Aragorn is the one companion Nelya is skirmishing, so nothing is asked.  Not Gimli, who is assigned
		// to another minion, and not the Ring-bearer, who is assigned to none.
		assertInDiscard(blade);
		assertAttachedTo(tip, aragorn);
		assertEquals(0, scn.GetAttachedCards(gimli).size());
		assertNotAttachedTo(tip, frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardedToTransferBladeTipFromTheDiscardPileToTheCompanionItsBearerIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var tip = scn.GetShadowCard("tip");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDiscard(tip);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.AttachCardsTo(nelya, blade);
		assertAttachedTo(blade, nelya);
		assertInDiscard(tip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, nelya }, new PhysicalCardImpl[] { gimli, savage });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertAttachedTo(blade, nelya);
		assertInDiscard(tip);
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);
		// The Blade has gone to the discard pile and Blade Tip has come out of it, onto the one companion
		// Nelya is skirmishing.
		assertInDiscard(blade);
		assertAttachedTo(tip, aragorn);
		assertEquals(0, scn.GetAttachedCards(gimli).size());
		assertNotAttachedTo(tip, frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CanBeBorneByANazgulOfEitherCultureAndNotByAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(blade);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya, khamul, creeper);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(blade));
		scn.ShadowPlayCard(blade);

		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(blade, khamul);
		assertEquals(0, scn.GetAttachedCards(nelya).size());
		assertEquals(0, scn.GetAttachedCards(creeper).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MayBeBorneBesideANazgulSwordAndNoThirdHandWeaponBesideThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var sword = scn.GetShadowCard("sword");
		var nazgulblade = scn.GetShadowCard("nazgulblade");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(blade, blade2, nazgulblade);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, sword);
		scn.SetTwilight(10);
		assertAttachedTo(sword, nelya);
		assertInZone(Zone.HAND, blade, blade2, nazgulblade);
		scn.FreepsPass();

		// Another hand weapon beside the Sword is offered only because it is this one.
		assertTrue(scn.ShadowPlayAvailable(blade));
		assertFalse(scn.ShadowPlayAvailable(nazgulblade));

		scn.ShadowPlayCard(blade);
		assertAttachedTo(blade, nelya);
		assertAttachedTo(sword, nelya);

		// Nelya is the only Nazgûl, and bears a hand weapon beside the Blade: neither a further Blade nor
		// another hand weapon may join them.
		assertInZone(Zone.HAND, blade2, nazgulblade);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(blade2));
		assertFalse(scn.ShadowPlayAvailable(nazgulblade));
	}

	@Test
	public void ANazgulBearingItMayTakeASecondBladeAndThenNoOtherHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var sword = scn.GetShadowCard("sword");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(blade2, sword);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, blade);
		scn.SetTwilight(10);
		assertAttachedTo(blade, nelya);
		assertInZone(Zone.HAND, blade2, sword);
		scn.FreepsPass();

		// Beside the Blade, one other hand weapon: a Nazgûl Sword or a second Blade.
		assertTrue(scn.ShadowPlayAvailable(sword));
		assertTrue(scn.ShadowPlayAvailable(blade2));

		scn.ShadowPlayCard(blade2);
		assertAttachedTo(blade, nelya);
		assertAttachedTo(blade2, nelya);

		assertInZone(Zone.HAND, sword);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sword));
	}

	@Test
	public void NotOfferedInTheAssignmentPhaseAndInBearersSkirmishEitherBladeTipInTheSupportAreaIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var breath = scn.GetShadowCard("breath");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(tip, tip2, breath);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, blade, blade2); // lint-ok: its own second sentence lets a second Blade stand beside it
		assertAttachedTo(blade, nelya);
		assertAttachedTo(blade2, nelya);
		assertInZone(Zone.SUPPORT, tip, tip2, breath);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(blade));
		scn.ShadowUseCardAction(blade);

		// This Blade is discarded; the other on Nelya stays.
		assertInDiscard(blade);
		assertAttachedTo(blade2, nelya);
		// Either Blade Tip, and not Black Breath, another [wraith] condition in the same support area.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tip, tip2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tip2);

		assertAttachedTo(tip2, aragorn);
		assertInZone(Zone.SUPPORT, tip, breath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FromTheDiscardPileEitherBladeTipIsOfferedAndBlackBreathIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var breath = scn.GetShadowCard("breath");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(tip, tip2, breath);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, blade);
		assertAttachedTo(blade, nelya);
		assertInDiscard(tip, tip2, breath);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInDiscard(tip, tip2, breath);
		scn.ShadowUseCardAction(blade);

		assertInDiscard(blade);
		// A choice from a pile is read by blueprint: a Blade Tip is offered, Black Breath is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tip));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath));
		// The answer goes by blueprint as well, and the first copy in the pile is the one taken.
		scn.ShadowChooseCards(tip);

		assertAttachedTo(tip, aragorn);
		assertInDiscard(tip2, breath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBladeTipInTheSupportAreaAndInTheDiscardPileThePlayerChoosesWhichPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(tip);
		scn.MoveCardsToDiscard(tip2);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, blade);
		assertAttachedTo(blade, nelya);
		assertInZone(Zone.SUPPORT, tip);
		assertInDiscard(tip2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, tip);
		assertInDiscard(tip2);
		scn.ShadowUseCardAction(blade);

		assertInDiscard(blade);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("support area"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChooseOption("discard pile");

		assertAttachedTo(tip2, aragorn);
		assertInZone(Zone.SUPPORT, tip);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherSkirmishOrAgainstAnAllyItIsDiscardedAndNothingMoreIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var tip = scn.GetShadowCard("tip");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(tip);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya, savage);
		scn.AttachCardsTo(nelya, blade, blade2); // lint-ok: its own second sentence lets a second Blade stand beside it
		assertAttachedTo(blade, nelya);
		assertAttachedTo(blade2, nelya);
		assertInZone(Zone.SUPPORT, tip);
		scn.SkipToAssignments();
		// Site 2 is the Bounder's home, so he may be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, nelya}, new PhysicalCardImpl[]{gimli, savage});

		// Gimli's skirmish first: a companion skirmishing, but not Nelya.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(blade));
		scn.ShadowUseCardAction(blade);

		assertInDiscard(blade);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.SUPPORT, tip);
		assertEquals(0, scn.GetAttachedCards(gimli).size());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli's 6 against the Savage's 5.
		scn.PassSkirmishActions();

		// Nelya's skirmish: she is skirmishing an ally, who is no companion.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, nelya));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(blade2));
		scn.ShadowUseCardAction(blade2);

		assertInDiscard(blade2);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.SUPPORT, tip);
		assertEquals(0, scn.GetAttachedCards(bounder).size());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoBladeTipToMoveItIsDiscardedAndNothingMoreIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var breath = scn.GetShadowCard("breath");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Black Breath: another [wraith] condition, the nearest thing to a Blade Tip in the support area
		scn.MoveCardsToSupportArea(breath);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, blade);
		assertAttachedTo(blade, nelya);
		assertInZone(Zone.SUPPORT, breath);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(blade));
		scn.ShadowUseCardAction(blade);

		assertInDiscard(blade);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetAttachedCards(aragorn).size());
		assertInZone(Zone.SUPPORT, breath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
