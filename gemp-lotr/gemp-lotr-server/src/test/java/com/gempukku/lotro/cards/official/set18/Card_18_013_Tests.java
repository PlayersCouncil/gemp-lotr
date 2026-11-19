package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glorfindel", "18_13");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: unique [elven] Elf companion, strength 6; +3 against a Nazgûl only
					put("guardian", "4_76");    // Lórien Guardian: [elven] Elf companion, not unique, cost 2; a regroup ability only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally; his start-of-turn heal finds nobody wounded
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion

					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession, hand weapon, strength +2, on a [moria] Orc; a play trigger only
					put("scimitar2", "1_180");
					put("feral", "11_183");     // Feral Uruk: [uruk-hai] Uruk-hai, strength 9, vitality 2; +1 at a battleground, which site 2 is not
					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] possession, strength +1, vitality +1, no other text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgûl, strength 8, fierce; +1 for each companion
					// Ring of Asperity: [wraith] artifact, a ring, strength +1, on a Nazgûl; its play trigger and its
					// reconcile response are not reached.
					put("ring", "9_42");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger only
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; texts about unbound Hobbits
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlorfindelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Glorfindel, Eldarin Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Ranger</b>.<br>To play, spot 2 [elven] companions.<br>Minions skirmishing Glorfindel cannot gain strength bonuses from possessions.<br>Each time Glorfindel wins a skirmish, you may remove a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glorfindel");

		assertEquals("Glorfindel", card.getBlueprint().getTitle());
		assertEquals("Eldarin Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilTwoElvenCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var arwen = scn.GetFreepsCard("arwen");
		var guardian = scn.GetFreepsCard("guardian");
		var elrond = scn.GetFreepsCard("elrond");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(glorfindel, guardian);
		assertInZone(Zone.HAND, glorfindel);

		scn.StartGame();

		// One [elven] companion, an [elven] ally and a companion of another culture: not enough.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(glorfindel));

		// A second [elven] companion, of another title and not unique
		scn.FreepsPlayCard(guardian);
		assertInZone(Zone.FREE_CHARACTERS, guardian);
		assertTrue(scn.FreepsPlayAvailable(glorfindel));
		scn.FreepsPlayCard(glorfindel);
		assertInZone(Zone.FREE_CHARACTERS, glorfindel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MinionsSkirmishingHimLoseTheirPossessionsStrengthBonusesButNotAnArtifacts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(glorfindel, arwen);

		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var feral = scn.GetShadowCard("feral");
		var spear = scn.GetShadowCard("spear");
		var rider = scn.GetShadowCard("rider");
		var ring = scn.GetShadowCard("ring");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, feral, rider, runner);
		scn.AttachCardsTo(scout, scimitar);
		scn.AttachCardsTo(feral, spear);
		scn.AttachCardsTo(rider, ring);
		scn.AttachCardsTo(runner, scimitar2);
		assertAttachedTo(scimitar, scout);
		assertAttachedTo(spear, feral);
		assertAttachedTo(ring, rider);
		assertAttachedTo(scimitar2, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{glorfindel, scout},
				new PhysicalCardImpl[]{arwen, runner});
		scn.ShadowAssignToMinions(glorfindel, feral, rider);

		// Assigned to him, but his skirmish has not begun: every bonus counts.
		assertEquals(8, scn.GetStrength(scout));    // 6 + 2
		assertEquals(10, scn.GetStrength(feral));   // 9 + 1
		assertEquals(12, scn.GetStrength(rider));   // 8 + 3 companions + 1

		// A minion skirmishing another companion keeps its possession's bonus.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(7, scn.GetStrength(runner));   // 5 + 2
		assertEquals(8, scn.GetStrength(scout));
		assertEquals(10, scn.GetStrength(feral));

		// Arwen 6 against 7 loses, and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.FreepsResolveSkirmish(glorfindel);
		assertTrue(scn.IsCharSkirmishingAgainst(glorfindel, scout));
		// Possessions of two cultures on an Orc and an Uruk-hai: no bonus from either
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(9, scn.GetStrength(feral));
		// An artifact is no possession: the Ring's +1 stays.
		assertEquals(12, scn.GetStrength(rider));
		assertAttachedTo(scimitar, scout);
		assertAttachedTo(spear, feral);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachTimeHeWinsASkirmishHeMayRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		// Two companions, Frodo and Glorfindel: two threats
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, pursuer);
		scn.FreepsResolveSkirmish(glorfindel);
		// 9 against 5: he wins, and the Pursuer takes one wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glorfindel));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		// The fierce round: he wins a second skirmish, and is asked again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(glorfindel, pursuer);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glorfindel));
		scn.FreepsDeclineOptionalTrigger();
		// Declined: the threat stays.
		assertEquals(1, scn.GetThreats());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void NothingIsOfferedWhenHeLosesASkirmishOrAnotherCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(glorfindel, arwen);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(troop, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{glorfindel, troop},
				new PhysicalCardImpl[]{arwen, runner});

		// Arwen 6 beats the Runner 5: another companion's win
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glorfindel));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Glorfindel 9 loses to 13, under double: one wound, and he lives.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glorfindel));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedForAWinWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, runner);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glorfindel));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
