package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_051_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 (3/4) as the Ring-bearer; Bilbo cannot be
	// overwhelmed by Smaug unless his strength is tripled.  The skirmishes are at Erebor (site 7, a mountain, where
	// Smaug stays in play), reached by SkipToSite(6) and the move; the minions are laid in its Shadow phase.  A doubt
	// is the Hobbit game's burden.  Smaug is 17 with damage +2: a companion who loses to him and is not overwhelmed
	// takes 3 wounds.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("malice", "32_51");

					// Smaug, The Golden: [smaug] Dragon, 17/5, damage +2, fierce; discarded if not at a mountain;
					// a regroup ability, not used.
					put("smaug", "30_66");
					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire.
					put("runner", "31_30");

					// The One Ring of the Hobbit Draft Game: worn by its own Skirmish action (a doubt); while worn
					// Bilbo is +2 and each wound he is about to take in a skirmish is a doubt instead.
					put("ring", "30_48");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4, damage +1; +1 for each [dwarven] follower
					// he bears (none) and a fellowship ability.
					put("thorin", "30_19");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability.
					put("balin", "30_2");
					// Nori, Of the Blue Mountains: [dwarven] Dwarf companion, 7/3; a maneuver ability.
					put("nori", "30_15");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, 7/4; Thorin costs 2 less, and he cannot
					// be discarded beside 2 [dwarven] followers.  Mirkwood (site 5) discards every Gandalf as the
					// fellowship moves there, so he is laid after that move.
					put("gandalf", "30_26");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DragonsMaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dragon's Malice
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time Smaug wins a skirmish, you may add a doubt (or 2 doubts if a [dwarven] companion he is skirmishing is killed).<br>Smaug cannot wound Bilbo wearing the One Ring.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("malice");

		assertEquals("Dragon's Malice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeSmaugWinsASkirmishTheShadowPlayerMayAddADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(malice);
		// Thorin 9 by hand: Smaug's 17 does not overwhelm him, and he lives through 3 wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 2));
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, smaug);
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thorin));

		scn.PassSkirmishActions();

		// The loser's wounds are on before the offer: a [dwarven] companion Smaug skirmished, wounded and alive.
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void ADwarvenCompanionSmaugIsSkirmishingKilledMakesItTwoDoubtsInOneOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(malice);
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		assertEquals(0, scn.GetBurdens());

		// Balin, 7, is overwhelmed by Smaug's 17 and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, balin);

		// One "may": taken once, it adds both doubts.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));
	}

	@Test
	public void ACompanionOfAnotherCultureKilledBySmaugMakesItOneDoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(malice);
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, smaug);
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetBurdens());

		// Gandalf, 7, is overwhelmed and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gandalf);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));
	}

	@Test
	public void ADwarvenCompanionKilledInAnotherSkirmishDoesNotMakeItTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(thorin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, runner);
		scn.MoveCardsToSupportArea(malice);
		// Thorin 9 by hand, so that he lives through Smaug's 3 wounds; the Runner 15 by hand, so that it overwhelms
		// Nori's 7.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 10));
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, smaug}, new PhysicalCardImpl[]{nori, runner});
		scn.FreepsResolveSkirmish(nori);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, nori);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));
	}

	@Test
	public void NothingIsOfferedWhenSmaugLosesOrAnotherMinionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(thorin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, runner);
		scn.MoveCardsToSupportArea(malice);
		// Thorin 18 by hand beats Smaug's 17 without overwhelming him; the Runner 8 by hand beats Nori's 7.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 11));
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 3));
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, smaug}, new PhysicalCardImpl[]{nori, runner});

		scn.FreepsResolveSkirmish(nori);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(nori));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));

		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		// Smaug loses: 1 wound and 1 for Thorin's damage bonus, and he lives.
		assertEquals(2, scn.GetWoundsOn(smaug));
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(malice));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void TheDoubtIsOfferedAgainForTheFierceSkirmishAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(malice);
		// Thorin 9 and Gandalf 9 by hand: neither is overwhelmed by Smaug, and each (vitality 4) lives through his
		// 3 wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 2));
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, smaug);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		// The fierce skirmish: Smaug wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, smaug);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassFierceSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(gandalf));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SmaugCannotWoundBilboWearingTheOneRingThoughAnotherMinionStillCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var bilbo = scn.GetRingBearer();
		var ring = scn.GetFreepsCard("ring");
		scn.AttachCardsTo(bilbo, ring);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, runner);
		scn.MoveCardsToSupportArea(malice);
		// Bilbo 4 by hand, 6 with the Ring on: 18 is more than Smaug's 17, so Smaug does not overwhelm him.  The
		// Runner 7 by hand beats him without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 1));
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 2));
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(bilbo));

		// The Runner's wound is a doubt, as the Ring has it.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetBurdens());

		// The fierce skirmish, the Ring still on: Smaug beats Bilbo and wounds him not at all.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, smaug);
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.RBWearingOneRing());
		scn.PassFierceSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutDragonsMaliceSmaugsWoundsOnBilboWearingTheOneRingAreDoubts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var bilbo = scn.GetRingBearer();
		var ring = scn.GetFreepsCard("ring");
		scn.AttachCardsTo(bilbo, ring);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		// Bilbo 4 by hand, 6 with the Ring on: not overwhelmed by Smaug's 17.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 1));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, smaug);
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		scn.ShadowPass();
		scn.FreepsPass();

		// 1 wound and 2 for Smaug's damage bonus, each a doubt instead.
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(4, scn.GetBurdens());
		// Smaug is fierce: the fierce assignment phase follows.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SmaugWoundsBilboWhoIsNotWearingTheOneRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var smaug = scn.GetShadowCard("smaug");
		var bilbo = scn.GetRingBearer();
		var ring = scn.GetFreepsCard("ring");
		scn.AttachCardsTo(bilbo, ring);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(malice);
		// Bilbo 6 by hand: not overwhelmed by Smaug's 17, and he lives through 3 wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 3));
		assertInZone(Zone.SUPPORT, malice);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, smaug);
		scn.FreepsResolveSkirmish(bilbo);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(malice));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
