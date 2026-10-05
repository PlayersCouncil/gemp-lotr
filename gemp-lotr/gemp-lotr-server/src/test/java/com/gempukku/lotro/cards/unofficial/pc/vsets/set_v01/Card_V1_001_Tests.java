package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_001_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hosp", "101_1");
					put("hosp2", "101_1");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6, vitality 3, damage +1
					// Farin, Dwarven Emissary: Dwarf, strength 5, vitality 3, no damage bonus.  He is strength +2
					// while skirmishing an Orc, and never skirmishes one here.
					put("farin", "1_11");

					// Dwarven Armor: possession.  "Bearer may not be overwhelmed unless his or her strength is
					// tripled."  No bonus to anything.
					put("armor", "1_8");
					put("armor2", "1_8");
					put("axe", "2_10");         // Hand Axe: possession, strength +1; its archery ability is never used
					// Gimli's Pipe: possession, no bonus to anything; its fellowship ability is never used.
					put("pipe", "3_2");
					// Ring of Retribution: artifact, strength +1 and vitality +1.  Its response is to bearer
					// winning a skirmish, which he never does here.
					put("ring", "9_9");

					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1, no other text
					put("raiders2", "1_158");
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1, archer, no other text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					// Morgul Slayer: Orc, strength 7, vitality 2.  Its whole text is "Regroup: Exert this minion to
					// wound a companion (except the Ring-bearer)."
					put("slayer", "3_93");
					put("slayer2", "3_93");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same table with a Gimli as Ring-bearer, which makes him a Ring-bound Dwarf.
	protected VirtualTableScenario GetGimliRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hosp", "101_1");

					put("farin", "1_11");       // Farin, Dwarven Emissary: unique Dwarf companion
					put("guard", "1_7");        // Dwarf Guard: non-unique Dwarf companion, no text in play
					put("guard2", "1_7");
					// Grimir, Dwarven Elder: Dwarf ally.  His fellowship ability is never used.
					put("grimir", "1_17");
					put("elf", "1_53");         // Lorien Elf: Elf companion, no text in play

					put("armor1", "1_8");       // Dwarven Armor: possession
					put("armor2", "1_8");
					put("armor3", "1_8");
					put("armor4", "1_8");
					put("armor5", "1_8");
					put("armor6", "1_8");
					put("axe1", "2_10");        // Hand Axe: possession
					put("axe2", "2_10");
					put("axe3", "2_10");
					put("axe4", "2_10");
					put("axe5", "2_10");
					put("ring", "9_9");         // Ring of Retribution: artifact
					// Endurance of Dwarves: a condition a Dwarf bears.  Its skirmish ability is never used.
					put("endurance", "2_4");

					put("raiders", "1_158");    // Uruk-hai Raiding Party: only here so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HospitalityoftheDwarvesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Hospitality of the Dwarves
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Maneuver: Spot an unbound Dwarf bearing 2 or more items and hinder this condition to choose
		 * one: make that Dwarf damage +1 until the regroup phase, or make that Dwarf take no more than 1 wound
		 * until the regroup phase. If at an underground site, do both.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hosp");

		assertEquals("Hospitality of the Dwarves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		scn.MoveCardsToHand(hosp);

		scn.StartGame();

		assertInHand(hosp);
		assertEquals(0, scn.GetTwilight());
		// Nothing has to be spotted to play it: only Frodo is in play.
		assertTrue(scn.FreepsPlayAvailable(hosp));

		scn.FreepsPlayCard(hosp);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, hosp);
		assertFalse(scn.IsHindered(hosp));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionHindersItselfToMakeTheSpottedDwarfDamagePlusOneUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		scn.MoveCardsToSupportArea(hosp);
		// Gimli bears nothing.  He is here to fight a skirmish before Farin's.
		scn.MoveCompanionsToTable(farin, gimli);
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor"), scn.GetFreepsCard("axe"));

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, raiders2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2, Ettenmoors, is a plains.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.IsHindered(hosp));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(hosp));

		// Farin is the only unbound Dwarf bearing 2 items, so he is spotted without asking.
		scn.FreepsUseCardAction(hosp);
		assertTrue(scn.IsHindered(hosp));
		assertTrue(scn.FreepsChoiceAvailable("damage +1"));
		assertTrue(scn.FreepsChoiceAvailable("no more than 1 wound"));

		scn.FreepsChoose("damage +1");
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { gimli, raiders2 },
				new PhysicalCardImpl[] { farin, raiders }
		);
		scn.ShadowDeclineAssignments();

		// Gimli's skirmish is fought first, so that one skirmish phase has already ended when Farin fights.
		// His 6 loses to a Raiding Party's 9 for 2 wounds, and he lives.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(farin);
		// The last decision before the regroup phase.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(farin));

		// Farin, 5 + 1 for the Hand Axe, loses to the Raiding Party's 9 without being overwhelmed.
		scn.PassSkirmishActions();
		// Only the chosen half was done: nothing limits his wounds, so he takes 1 for losing and 1 for the
		// Raiding Party's damage +1.
		assertEquals(2, scn.GetWoundsOn(farin));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		// Nothing has restored the condition: that waits for its owner to reconcile.
		assertTrue(scn.IsHindered(hosp));
	}

	@Test
	public void ManeuverActionCanInsteadLimitTheSpottedDwarfToOneWoundUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		scn.MoveCardsToSupportArea(hosp);
		scn.MoveCompanionsToTable(gimli, farin);
		// The Ring of Retribution makes Gimli strength 7 and vitality 4, so that he can lose a skirmish to the
		// Raiding Party's 9, take two more wounds in the regroup phase, and live.
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("ring"));
		// Farin bears 2 items as well, so either Dwarf could be spotted.  His 5 + 1 for the Hand Axe loses
		// to a Raiding Party's 9 without being overwhelmed.
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor2"), scn.GetFreepsCard("axe"));

		scn.StartGame();
		// The Slayers are left unassigned, and wait for the regroup phase.
		scn.MoveMinionsToTable(raiders, raiders2, slayer, slayer2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(hosp));

		scn.FreepsUseCardAction(hosp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsChoiceAvailable("damage +1"));
		assertTrue(scn.FreepsChoiceAvailable("no more than 1 wound"));

		scn.FreepsChoose("no more than 1 wound");
		assertTrue(scn.IsHindered(hosp));
		// Only the chosen half was done: his damage bonus is still the one he prints.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { farin, raiders2 },
				new PhysicalCardImpl[] { gimli, raiders }
		);
		scn.ShadowDeclineAssignments();

		// Farin's skirmish is fought first.  He is not the Dwarf who was spotted, so nothing limits his
		// wounds: 1 for losing and 1 for the Raiding Party's damage +1.
		scn.FreepsResolveSkirmish(farin);
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli's skirmish is the second: a skirmish phase has ended since the ability was used, and the
		// regroup phase has not come.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// 1 for losing; the Raiding Party's damage +1 would be a second wound, which he does not take.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The regroup phase has begun, and with it the limit is gone: each Morgul Slayer exerts to wound a
		// companion, and both wounds land on Gimli in this one phase.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, farin));
		scn.ShadowChooseCard(gimli);
		// 1 from the skirmish + 1
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		// A Dwarf still limited to 1 wound this phase could not be chosen for the second.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, farin));
		scn.ShadowChooseCard(gimli);
		// Vitality 4 with the Ring of Retribution, so he lives.
		assertEquals(3, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherDwarfsWoundsAreNotLimitedAndDoNotCountAsTheSpottedDwarfsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCardsToSupportArea(hosp);
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("ring"));
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor2"), scn.GetFreepsCard("axe"));

		scn.StartGame();
		// Three archers, so that both Dwarves are wounded in the one archery phase.
		scn.MoveMinionsToTable("marksman", "marksman2", "marksman3");
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(hosp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		scn.FreepsChoose("no more than 1 wound");

		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));

		// The first archery wound goes on Farin.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(farin);
		assertEquals(1, scn.GetWoundsOn(farin));

		// Farin's wound was not Gimli's: Gimli can still be given his one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli can now take no more, so the third cannot be assigned to him.  Farin, who was not spotted,
		// can take a second.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin));
		scn.FreepsChooseCard(farin);
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ExertingDoesNotCountAsTheOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var gimli = scn.GetFreepsCard("gimli");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(hosp);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("pipe"));

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(hosp);
		scn.FreepsChoose("no more than 1 wound");

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		// Gimli's own "Skirmish: Exert Gimli to make him strength +2."
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));

		// His 8 still loses to the Raiding Party's 9.  The exertion was not a wound, so he takes the 1 wound
		// he is allowed, and not the second for damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteBothAreDoneWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		scn.MoveCardsToSupportArea(hosp);
		scn.MoveCompanionsToTable(gimli, farin);
		// The Ring of Retribution makes Gimli strength 7 and vitality 4, so that he can lose a skirmish to the
		// Raiding Party's 9, take two more wounds in the regroup phase, and live.
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("ring"));
		// Farin bears 2 items as well, so either Dwarf could be spotted.  His 5 + 1 for the Hand Axe loses
		// to a Raiding Party's 9 without being overwhelmed.
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor2"), scn.GetFreepsCard("axe"));

		scn.StartGame();
		scn.SkipToSite(3);
		// The Slayers are left unassigned, and wait for the regroup phase.
		scn.MoveMinionsToTable(raiders, raiders2, slayer, slayer2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 4, Balin's Tomb, is underground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.IsHindered(hosp));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(hosp));

		scn.FreepsUseCardAction(hosp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		// No choice between the two is asked: the Shadow player is already being asked for a maneuver action.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.IsHindered(hosp));
		// His own damage +1 and the one he was given.  The Dwarf who was not spotted is given nothing.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { farin, raiders2 },
				new PhysicalCardImpl[] { gimli, raiders }
		);
		scn.ShadowDeclineAssignments();

		// Farin's skirmish is fought first.  Nothing limits his wounds: 1 for losing and 1 for the Raiding
		// Party's damage +1.
		scn.FreepsResolveSkirmish(farin);
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli's skirmish is the second: a skirmish phase has ended since the ability was used.  This is
		// the last decision before the regroup phase, and both halves are still in force.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// 1 for losing, and the Raiding Party's damage +1 adds no second wound.
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The regroup phase has begun, and with it the limit is gone: each Morgul Slayer exerts to wound a
		// companion, and both wounds land on Gimli in this one phase.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, farin));
		scn.ShadowChooseCard(gimli);
		// 1 from the skirmish + 1
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		// A Dwarf still limited to 1 wound this phase could not be chosen for the second.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, farin));
		scn.ShadowChooseCard(gimli);
		// Vitality 4 with the Ring of Retribution, so he lives.
		assertEquals(3, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteTheWoundLimitIsOnTheSpottedDwarfAloneAndRenewsEachPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(hosp);
		scn.MoveCompanionsToTable(gimli, farin);
		// The Ring of Retribution makes Gimli strength 7 and vitality 4, so that he can be wounded in two
		// phases and live.
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("ring"));
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor2"), scn.GetFreepsCard("axe"));

		scn.StartGame();
		scn.SkipToSite(3);
		// Three archers, so that both Dwarves are wounded in the one archery phase, and a Raiding Party for
		// Gimli to lose a skirmish to afterwards.
		scn.MoveMinionsToTable("marksman", "marksman2", "marksman3", "raiders");
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 4, Balin's Tomb, is underground.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));

		scn.FreepsUseCardAction(hosp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));

		// The first archery wound goes on Farin.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(farin);
		assertEquals(1, scn.GetWoundsOn(farin));

		// Farin's wound was not Gimli's: Gimli can still be given his one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli can now take no more, so the third cannot be assigned to him.  Farin, who was not spotted,
		// can take a second.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin));
		scn.FreepsChooseCard(farin);
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// A skirmish phase is a new phase, and the regroup phase has not come: he can take 1 wound again,
		// and again no more than 1.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, raiders);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// His 7 loses to the Raiding Party's 9: 1 already + 1 for losing, and the Raiding Party's damage +1
		// would be a second wound in this phase, which he does not take.  A limit of 1 wound in all would
		// have left him on 1, and no limit would have put him on 3.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UsingOneCopyHindersOnlyThatCopyAndItIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var hosp2 = scn.GetFreepsCard("hosp2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(hosp, hosp2);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("pipe"));

		scn.StartGame();
		scn.MoveMinionsToTable("raiders");
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(hosp));
		assertFalse(scn.IsHindered(hosp2));
		assertTrue(scn.FreepsActionAvailable(hosp));
		assertTrue(scn.FreepsActionAvailable(hosp2));

		scn.FreepsUseCardAction(hosp);
		scn.FreepsChoose("damage +1");
		assertTrue(scn.IsHindered(hosp));
		assertFalse(scn.IsHindered(hosp2));

		// Gimli still bears his 2 items, but the copy that was used is hindered and cannot be hindered again.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hosp));
		assertTrue(scn.FreepsActionAvailable(hosp2));
	}

	@Test
	public void OnlyAnUnboundDwarfBearingTwoOrMoreItemsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var farin = scn.GetFreepsCard("farin");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var grimir = scn.GetFreepsCard("grimir");
		var elf = scn.GetFreepsCard("elf");
		var ringBearer = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(hosp, grimir);
		scn.MoveCompanionsToTable(farin, guard, guard2, elf);

		// Two who qualify: one with 3 possessions, one with exactly 2 items of which one is an artifact.
		scn.AttachCardsTo(guard, scn.GetFreepsCard("armor1"), scn.GetFreepsCard("axe1"), scn.GetFreepsCard("axe2"));
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor2"), scn.GetFreepsCard("ring"));
		// Four who each miss by one thing.  A Dwarf with 2 cards on him of which only 1 is an item:
		scn.AttachCardsTo(guard2, scn.GetFreepsCard("armor3"), scn.GetFreepsCard("endurance"));
		// a Dwarf who is an ally, and so is not unbound:
		scn.AttachCardsTo(grimir, scn.GetFreepsCard("armor4"), scn.GetFreepsCard("axe3"));
		// an unbound companion who is not a Dwarf:
		scn.AttachCardsTo(elf, scn.GetFreepsCard("armor5"), scn.GetFreepsCard("axe4"));
		// and a Dwarf who is the Ring-bearer, and so is Ring-bound.
		scn.AttachCardsTo(ringBearer, scn.GetFreepsCard("armor6"), scn.GetFreepsCard("axe5"));

		scn.StartGame();
		scn.MoveMinionsToTable("raiders");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsRace(ringBearer, Race.DWARF));
		assertTrue(scn.HasKeyword(ringBearer, Keyword.RING_BOUND));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(hosp));

		scn.FreepsUseCardAction(hosp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, grimir, elf, ringBearer));

		scn.FreepsChooseCard(guard);
		scn.FreepsChoose("damage +1");
		// The Dwarf who was spotted gets it, and the other who could have been does not.
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNoUnboundDwarfBearsTwoItems() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var farin = scn.GetFreepsCard("farin");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var elf = scn.GetFreepsCard("elf");
		var ringBearer = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(hosp, grimir);
		scn.MoveCompanionsToTable(farin, guard, elf);

		// An unbound Dwarf with 1 item, and another with 1 item and a condition.
		scn.AttachCardsTo(farin, scn.GetFreepsCard("armor1"));
		scn.AttachCardsTo(guard, scn.GetFreepsCard("armor2"), scn.GetFreepsCard("endurance"));
		// 2 items each on a Dwarf ally, on a companion who is not a Dwarf, and on a Ring-bound Dwarf.
		scn.AttachCardsTo(grimir, scn.GetFreepsCard("armor3"), scn.GetFreepsCard("axe1"));
		scn.AttachCardsTo(elf, scn.GetFreepsCard("armor4"), scn.GetFreepsCard("axe2"));
		scn.AttachCardsTo(ringBearer, scn.GetFreepsCard("armor5"), scn.GetFreepsCard("axe3"));

		scn.StartGame();
		scn.MoveMinionsToTable("raiders");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsHindered(hosp));
		assertFalse(scn.FreepsActionAvailable(hosp));
	}

	@Test
	public void WoundLimitIsOneWoundInEachPhaseUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosp = scn.GetFreepsCard("hosp");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(hosp);
		scn.MoveCompanionsToTable(gimli, farin);
		// The Ring of Retribution makes Gimli strength 7 and vitality 4, so that he can be wounded in two
		// phases and live.  Farin bears nothing: he is here to take the archery wound that Gimli cannot.
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("ring"));

		scn.StartGame();
		// Two archers and a Raiding Party: enough to wound Gimli twice in the archery phase and twice more
		// in a skirmish, if nothing stopped it.
		scn.MoveMinionsToTable("marksman", "marksman2", "raiders");
		scn.SkipToPhase(Phase.MANEUVER);

		// Gimli is the only unbound Dwarf bearing 2 items, so he is spotted without asking.
		scn.FreepsUseCardAction(hosp);
		scn.FreepsChoose("no more than 1 wound");

		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));

		// The archery phase.  The first wound goes on Gimli.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// That was his one wound for this phase, so the second cannot be assigned to him.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin));
		scn.FreepsChooseCard(farin);
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// A skirmish phase is a new phase, and the regroup phase has not come: he can take 1 wound again,
		// and again no more than 1.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// His 7 loses to the Raiding Party's 9: 1 already + 1 for losing, and the Raiding Party's damage +1
		// would be a second wound in this phase, which he does not take.  A limit of 1 wound in all would
		// have left him on 1, and no limit would have put him on 3.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
