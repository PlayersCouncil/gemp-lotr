package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_108_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FOTRFrodo);
	}

	// Galadriel as Ring-bearer, with a Sam beside her who is Ring-bound and not the Ring-bearer.
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.GaladrielRB);
	}

	protected VirtualTableScenario GetScenario(String ringBearer) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elbereth", "52_108");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion to skirmish, strength 8

					put("lemenya", "1_232");    // Úlairë Lemenya: [ringwraith] Nazgûl, strength 9; text only touches bearers of [ringwraith] conditions
					put("nelya", "1_233");      // Úlairë Nelya, Lieutenant of Morgul: [ringwraith] Nazgûl, strength 10; fierce, so he skirmishes a second time; his text is a Shadow ability only used on purpose
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgûl, strength 12; fierce, so no test runs past his skirmish
					put("ambusher", "8_74");    // Morgul Ambusher: [ringwraith] Orc, not a Nazgûl, strength 8; text needs 3 threats
					put("marksman", "1_176");   // Goblin Marksman: an archer, to wound the Ring-bearer in the archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				ringBearer,
				// Isildur's Bane: the Ring-bearer may put it on when about to take any wound, an archery wound included.
				VirtualTableScenario.IsildursBaneRing
		);
	}

	@Test
	public void OElberethGilthonielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: O Elbereth! Gilthoniel!
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Strength: +1
		 * Game Text: Tale. Bearer must be the Ring-bearer.
		 * Skirmish: Discard this condition to make the Ring-bearer strength +4 if skirmishing a Nazgul or to take off The One Ring.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elbereth");

		assertEquals("O Elbereth! Gilthoniel!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnARingBearerWhoIsNotFrodoAndNotOnSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(elbereth);

		scn.StartGame();

		// 3 + 1 for Isildur's Bane
		assertEquals(4, scn.GetStrength(galadriel));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elbereth));

		// Galadriel is the only bearer there can be, so no choice is asked.
		scn.FreepsPlayCard(elbereth);

		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(elbereth, galadriel);
		assertNotAttachedTo(elbereth, sam);
		assertEquals(5, scn.GetStrength(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedInAssignmentThenMakesTheRingBearerStrengthPlus4AgainstANazgulForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var nelya = scn.GetShadowCard("nelya");
		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, ambusher);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elbereth));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, nelya},
				new PhysicalCardImpl[]{aragorn, ambusher}
		);
		scn.FreepsResolveSkirmish(frodo);

		// 3 + 1 for Isildur's Bane + 1 for this condition
		assertEquals(5, scn.GetStrength(frodo));
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// The Ring is not on, so there is nothing to take off: the +4 is what happens.
		scn.FreepsUseCardAction(elbereth);

		assertInDiscard(elbereth);
		// 3 + 1 for Isildur's Bane + 4; the condition's own +1 went with it
		assertEquals(8, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo at 8 loses to Nelya at 10; Isildur's Bane is declined and Frodo takes the wound.
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();

		// Nelya is fierce: the Ring-bearer skirmishes a Nazgûl again, in a later skirmish phase of the same turn.
		// The +4 lasted only for the phase it was used in.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, nelya);
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesTheRingBearerStrengthPlus4AgainstARaiderNazgulBesideAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var attea = scn.GetShadowCard("attea");
		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, ambusher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, attea);
		scn.ShadowAssignToMinions(frodo, ambusher);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(elbereth);

		assertInDiscard(elbereth);
		assertEquals(8, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNeitherTheRingOnNorANazgulInHisSkirmishNothingIsAskedAndNothingGained() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var lemenya = scn.GetShadowCard("lemenya");
		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, ambusher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, ambusher},
				new PhysicalCardImpl[]{aragorn, lemenya}
		);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetStrength(frodo));
		assertFalse(scn.RBWearingOneRing());
		// An ability with a cost is offered though its effect will do nothing (Q-0034).
		assertTrue(scn.FreepsActionAvailable(elbereth));

		scn.FreepsUseCardAction(elbereth);

		assertInDiscard(elbereth);
		// 3 + 1 for Isildur's Bane: the condition's +1 is gone and nothing is gained
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(8, scn.GetStrength(aragorn));
		// Neither branch can do anything, so no choice is put (F-0116).
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WearingTheRingAgainstANazgulTheRingCanBeTakenOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		// Isildur's Bane, as the archery wound comes to Frodo: he puts the Ring on.
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(elbereth);

		// Both can be done: the player chooses.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Take off");

		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WearingTheRingAgainstANazgulTheRingBearerCanBeMadeStrengthPlus4Instead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(elbereth);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("strength +4");

		assertInDiscard(elbereth);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(8, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishWithANazgulTheRingIsTakenOffAndNobodyIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, elbereth);

		var marksman = scn.GetShadowCard("marksman");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, lemenya);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(frodo);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, marksman},
				new PhysicalCardImpl[]{sam, lemenya}
		);
		// Sam, who is Ring-bound but not the Ring-bearer, is the one skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(sam);

		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// The Ring-bearer is not skirmishing the Nazgûl, so only taking off the Ring can be done: no choice.
		scn.FreepsUseCardAction(elbereth);

		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WearingTheRingAgainstAnOrcWhileANazgulWaitsTheRingIsTakenOffWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var marksman = scn.GetShadowCard("marksman");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, lemenya);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(frodo);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, marksman},
				new PhysicalCardImpl[]{aragorn, lemenya}
		);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetStrength(frodo));
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// Frodo is skirmishing an Orc, and the Nazgûl waits for Aragorn: taking off the Ring is what happens.
		scn.FreepsUseCardAction(elbereth);

		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
