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

public class Card_01_273_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oppression", "1_273");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; adds (2) only when a weapon is played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; "Skirmish: Exert this minion to wound a character he is skirmishing"
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)"
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("scout", "15_116");     // Scouting Orc: [orc] Orc, strength 8, vitality 2; "Skirmish: Exert this minion to wound a companion it is skirmishing."
					put("ungolguard", "10_81"); // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc, strength 9; Damage +1 only

					put("dwarfguard", "1_7");   // Dwarf Guard: Dwarf companion, strength 4, vitality 2; its text is about playing it
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: Hobbit companion, strength 3, vitality 4; a skirmish ability only while unassigned
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oppression", "1_273");
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7; a regroup ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: Hobbit companion, strength 3, vitality 4; a skirmish ability only while unassigned
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The One Ring, Answer To All Riddles: "Skirmish: Add a burden to wear The One Ring until the regroup phase."
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void TheRingsOppressionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Ring's Oppression
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a [sauron] Orc kills a companion, add 1 burden (or 3 if the Ring-bearer wears The One Ring).
		*/

		var scn = GetScenario();

		var oppression = scn.GetFreepsCard("oppression");

		assertEquals("The Ring's Oppression", oppression.getBlueprint().getTitle());
		assertNull(oppression.getBlueprint().getSubtitle());
		assertFalse(oppression.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, oppression.getBlueprint().getSide());
		assertEquals(Culture.SAURON, oppression.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, oppression.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(oppression, Timeword.RESPONSE));
		assertEquals(3, oppression.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionKilledByLosingASkirmishThatInvolvesASauronOrcAddsOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oppression = scn.GetShadowCard("oppression");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(dwarfguard);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		// Dwarf Guard has one vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(dwarfguard, 1);
		scn.MoveMinionsToTable(chieftain, troop);
		assertEquals(1, scn.GetWoundsOn(dwarfguard));
		assertInHand(oppression);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwarfguard, chieftain);
		// The [moria] Orc is put in the same skirmish by the Shadow player.
		scn.ShadowAssignToMinions(dwarfguard, troop);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(dwarfguard);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());

		// 7 + 13 against 4: Dwarf Guard loses a skirmish involving a [sauron] Orc, and dies of the wound.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);
		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);

		// The Ring is not worn: 1 burden.
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(oppression);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileTheRingBearerWearsTheRingACompanionOverwhelmedByASauronOrcAddsThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var oppression = scn.GetShadowCard("oppression");
		var slayer = scn.GetShadowCard("slayer");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		assertInHand(oppression);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, slayer);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(merry);

		// The Ring-bearer, in no skirmish, puts The One Ring on by its own ability.
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(scn.GetRing());
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		// 7 against 3: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);

		// 1 for the Ring + 3, the Ring being worn.
		assertEquals(4, scn.GetBurdens());
		assertInDiscard(oppression);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAKillInAnotherSkirmishNorForACompanionWhoLivesThroughTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oppression = scn.GetShadowCard("oppression");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(dwarfguard, gimli);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		assertInHand(oppression);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dwarfguard, troop}, new PhysicalCardImpl[]{gimli, chieftain});
		scn.SetTwilight(3);

		// 13 against 4: the [moria] Orc overwhelms Dwarf Guard while the [sauron] Orc waits for Gimli's skirmish.
		scn.FreepsResolveSkirmish(dwarfguard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);
		// The game goes on to the next skirmish: the Shadow player, with (3) in the pool, was asked nothing.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertInHand(oppression);
		assertEquals(0, scn.GetBurdens());

		// 7 against 6: Gimli loses to the [sauron] Orc, takes the wound and lives.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(oppression);
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void NotOfferedWhenASauronUrukHaiKillsACompanionNorWhenASauronOrcKillsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oppression = scn.GetShadowCard("oppression");
		var chieftain = scn.GetShadowCard("chieftain");
		var ungolguard = scn.GetShadowCard("ungolguard");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(dwarfguard);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, ungolguard);
		assertInHand(oppression);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dwarfguard, ungolguard}, new PhysicalCardImpl[]{thrarin, chieftain});
		scn.SetTwilight(3);

		// 9 against 4: the [sauron] Uruk-hai overwhelms Dwarf Guard.
		scn.FreepsResolveSkirmish(dwarfguard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertInHand(oppression);

		// 7 against 4: the [sauron] Orc beats the ally, and the wound kills him.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(oppression);
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void ACompanionKilledByASauronOrcsOwnSkirmishAbilityAddsOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oppression = scn.GetShadowCard("oppression");
		var soldier = scn.GetShadowCard("soldier");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		// Gimli has one vitality left.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(soldier);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInHand(oppression);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, soldier);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());

		// The Orc Soldier's own game text kills Gimli (CR 4.2 Section Three, The Ring's Oppression).
		scn.ShadowUseCardAction(soldier);
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(oppression);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledInTheOrcsSkirmishByAnotherMinionsWoundDoesNotAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oppression = scn.GetShadowCard("oppression");
		var chieftain = scn.GetShadowCard("chieftain");
		var scout = scn.GetShadowCard("scout");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(dwarfguard);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		// Dwarf Guard has one vitality left.
		scn.AddWoundsToChar(dwarfguard, 1);
		assertEquals(1, scn.GetWoundsOn(dwarfguard));
		scn.MoveMinionsToTable(chieftain, scout);
		assertInHand(oppression);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwarfguard, chieftain);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(dwarfguard, scout);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(dwarfguard);

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The [orc] Orc's own wound kills Dwarf Guard, before he has lost to the [sauron] Orc: its text did not kill him, nor did his losing.
		assertInZone(Zone.DEAD, dwarfguard);
		// The Shadow player, with (3) in the pool, is asked nothing: the Free Peoples player has the regroup phase.
		assertEquals(3, scn.GetTwilight());
		assertInHand(oppression);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledByASauronOrcsRegroupAbilityOutsideAnySkirmishAddsOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oppression = scn.GetShadowCard("oppression");
		var slayer = scn.GetShadowCard("slayer");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(dwarfguard, aragorn);
		scn.MoveCardsToHand(oppression);

		scn.StartGame();
		// Dwarf Guard has one vitality left.
		scn.AddWoundsToChar(dwarfguard, 1);
		assertEquals(1, scn.GetWoundsOn(dwarfguard));
		assertInHand(oppression);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(dwarfguard, aragorn));
		scn.ShadowChooseCard(dwarfguard);

		// Morgul Slayer's own game text kills Dwarf Guard (CR 4.2 Section Three, The Ring's Oppression).
		assertInZone(Zone.DEAD, dwarfguard);
		assertTrue(scn.ShadowPlayAvailable(oppression));
		scn.ShadowPlayCard(oppression);

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(oppression);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
