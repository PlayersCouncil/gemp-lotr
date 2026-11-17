package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_185_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("answers", "7_185");
					// Ulaire Nelya, Ringwraith in Twilight: a [wraith] Nazgul, strength 10; fierce and damage +1 only at 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, strength 9; "Each companion or ally who
					// bears a [wraith] condition is strength -2"
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12; fierce, and his texts read burdens only
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc, strength 5, vitality 2); stronger only at 2
					// wounds on the Ring-bearer
					put("hound", "7_192");
					// Black Steed: a mount borne by a Nazgul, strength +2 and +2 more at a plains (Ettenmoors, site 2)
					put("steed", "1_208");
					// Black Breath: a [wraith] condition, borne here by a companion; its skirmish ability is never used
					put("breath", "1_207");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; his skirmish ability is never used
					// Dwarven Armor: a possession, "Bearer must be a Dwarf"; bearer is not overwhelmed unless tripled
					put("armor", "1_8");
					put("armor2", "1_8");
					// Ring of Artifice: an artifact for a Dwarf (damage +1; strength +0 with no [dwarven] artifact in the
					// discard pile); its regroup ability is never used
					put("ring", "17_4");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("sword", "1_112");      // Ranger's Sword: strength +2 and damage +1, on Aragorn
					// Thrarin, Dwarven Smith: a Dwarf ally; "Maneuver: Exert Thrarin to allow him to participate in archery
					// fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulAnswersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Answers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Nazgûl.
		 * <b>Skirmish:</b> Remove (3) and spot 3 Nazgûl to discard a possession or condition borne by a companion a Nazgûl is skirmishing.
		*/

		var scn = GetScenario();

		var answers = scn.GetFreepsCard("answers");

		assertEquals("Morgul Answers", answers.getBlueprint().getTitle());
		assertNull(answers.getBlueprint().getSubtitle());
		assertFalse(answers.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, answers.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, answers.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, answers.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(answers, Keyword.SUPPORT_AREA));
		assertEquals(1, answers.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaWithARaiderNazgulToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(answers);
		assertInHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(answers));
		scn.ShadowPlayCard(answers);
		assertInZone(Zone.SUPPORT, answers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var hound = scn.GetShadowCard("hound");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(answers, nelya);
		assertInHand(answers, nelya);

		scn.StartGame();
		// A [wraith] minion who is no Nazgul.
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(answers));

		scn.ShadowPlayCard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertTrue(scn.ShadowPlayAvailable(answers));

		scn.ShadowPlayCard(answers);
		assertInZone(Zone.SUPPORT, answers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionRemoves3ToDiscardAPossessionOrConditionBorneByTheCompanionANazgulIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var steed = scn.GetShadowCard("steed");
		var breath = scn.GetShadowCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, armor, ring, breath);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(answers);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(breath, gimli);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, answers);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, khamul);
		scn.AttachCardsTo(nelya, steed);
		assertAttachedTo(steed, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, nelya}, new PhysicalCardImpl[]{aragorn, khamul});
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(6);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(answers));
		scn.ShadowUseCardAction(answers);
		assertEquals(3, scn.GetTwilight()); // 6 - 3

		// What Gimli bears that is a possession or a condition, of either side; not his artifact, the Nazgul's own
		// mount, or the Sword of Aragorn, whose skirmish against Khamul has not begun.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, steed, sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(breath);
		assertInDiscard(breath);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(steed, nelya);
		assertAttachedTo(sword, aragorn);

		// Nelya (10, +2 for the Steed, +2 at a plains) beats Gimli (6), who is not overwhelmed short of triple.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Khamul, a Nazgul of another culture, skirmishing Aragorn: the Sword is his to discard, the one choice.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(answers);
		assertEquals(0, scn.GetTwilight()); // 3 - 3
		assertInDiscard(sword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoCompanionANazgulIsSkirmishingTheActionIsTakenForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(thrarin, armor2);
		scn.MoveCardsToSupportArea(answers);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor2, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, khamul, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hound}, new PhysicalCardImpl[]{gimli, nelya},
				new PhysicalCardImpl[]{thrarin, lemenya});
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(6);

		// Aragorn skirmishes a [wraith] minion who is no Nazgul, and Gimli waits for his skirmish against Nelya:
		// three Nazgul are spotted, the action is offered and paid for, and there is nothing to discard.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(answers));
		scn.ShadowUseCardAction(answers);
		assertEquals(3, scn.GetTwilight()); // 6 - 3
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Lemenya skirmishes Thrarin, an ally and no companion: paid for, and nothing to discard.
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(answers));
		scn.ShadowUseCardAction(answers);
		assertEquals(0, scn.GetTwilight()); // 3 - 3
		assertAttachedTo(armor2, thrarin);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithTwoNazgulBesideAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(answers);
		assertAttachedTo(armor, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(6);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		// Two Nazgul and a [wraith] minion who is no Nazgul: not 3 Nazgul, with (6) to pay with.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(answers));
		assertAttachedTo(armor, gimli);
	}

	@Test
	public void SkirmishActionIsNotOfferedWithOnly2ToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(answers);
		assertAttachedTo(armor, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(2);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(answers));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(answers));
		assertAttachedTo(armor, gimli);
	}
}
