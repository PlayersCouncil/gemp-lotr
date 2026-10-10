package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * An event printed with several phases is, while it is being played, an event of the phase it is played in.
 *
 * Comprehensive Rules 4.2, "phase action": "If a phase action can be played in multiple phases (for example,
 * "Maneuver or Skirmish:"), its action type is of the phase during which the action is taken."  So Sword-wall
 * 4_84 ("Skirmish or Regroup") played in the regroup phase is a regroup event and no skirmish event, and a text
 * that answers the play of "a skirmish event" does not answer it; neither does one that prices "each skirmish
 * event you play".
 *
 * The game used to read the phases printed on the event wherever it was, so every such text took a two-phase
 * event for an event of each of its phases whichever one it was played in (F-0292, Galadriel's Silver Ewer).
 * Now the play of an event records the phase it is made in (GameState.eventPlayStarted), and the definition
 * word timeword(...) reads that one phase for as long as the play lasts (Filters.timeword).  What the event
 * costs to play is read the same way before the play begins.  A card that is not being played, in a hand or a
 * pile, is what is printed on it, as it always was; so is an event printed with one phase.
 *
 * Shown with a trigger (Galadriel's Silver Ewer 18_11: "Each time you play an [elven] skirmish event, you may
 * reinforce an [elven] token"), a response (Time for Food 18_36: "If a skirmish event is played, spot Sméagol
 * to cancel that event") and a cost (Orc Skulker 12_95: "Each skirmish event you play is twilight cost -1").
 */
public class EventPrintedWithSeveralPhasesTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sword-wall: [elven] event, cost 2.  "Skirmish or Regroup: Spot 3 Elf companions to liberate
					// a site or exert a minion."
					put("swordwall", "4_84");
					// Galadriel's Silver Ewer: [elven] artifact in the support area.  "Each time you play an
					// [elven] skirmish event, you may reinforce an [elven] token."  Its other texts need
					// Galadriel as the Ring-bearer.
					put("ewer", "18_11");
					put("strands", "9_22");     // Strands of Elven Hair: [elven] possession that holds [elven] tokens; a fellowship ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; stronger only against a Nazgul
					put("elf1", "1_53");        // Lorien Elf: Elf companion, strength 4, vitality 2; no text once in play
					put("elf2", "1_53");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else

					// Flanking Attack: [raider] event, cost 0.  "Maneuver or Skirmish: Exert a Southron to add (3)."
					put("flanking", "4_234");
					// Desert Lancers: [raider] Southron, strength 11, vitality 3; a regroup ability used only on purpose
					put("lancers", "4_217");
					// Time for Food: [gollum] Free Peoples response event, cost 0.  "If a skirmish event is played,
					// spot Smeagol to cancel that event."
					put("food", "18_36");
					put("smeagol", "5_29");     // Smeagol, Slinker: his skirmish ability (add a burden) is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4; a maneuver ability used only on purpose

					// Orc Skulker: [orc] minion, strength 5, vitality 2, lurker.  "Each skirmish event you play is
					// twilight cost -1."
					put("skulker", "12_95");
					// Two-faced: [gollum] Shadow event, cost 2.  "Shadow or Maneuver or Skirmish: Restore a [gollum]
					// card.  Then you may spot a [gollum] minion and remove a threat to replace it ..."  With no
					// [gollum] card in play it does nothing.
					put("twofaced1", "103_15");
					put("twofaced2", "103_15");
					// Some Who Resisted: [gondor] event, cost 0.  "Maneuver: Spot a [gondor] companion to make the
					// twilight cost of each maneuver event +2."
					put("resisted", "3_45");

					// Stout and Sturdy: [shire] event, cost 1.  "Maneuver or Skirmish: Heal a Hobbit."
					put("stout", "1_315");
					// Bilbo, Melancholy Hobbit: [shire] companion, strength 3, vitality 4.  "At the start of each
					// skirmish involving Bilbo, you may exert him to take a [shire] skirmish event into hand from
					// your discard pile."
					put("bilbo", "12_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlayedInASkirmishItIsASkirmishEventForATrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ewer = scn.GetFreepsCard("ewer");
		var strands = scn.GetFreepsCard("strands");
		var arwen = scn.GetFreepsCard("arwen");
		var swordwall = scn.GetFreepsCard("swordwall");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, scn.GetFreepsCard("elf1"), scn.GetFreepsCard("elf2"));
		scn.MoveCardsToSupportArea(ewer, strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(swordwall);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		scn.FreepsPlayCard(swordwall);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetCultureTokensOn(strands));
		// Sword-wall's own effect: no site to liberate, so the minion exerts.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedInTheRegroupPhaseItIsNoSkirmishEventForATrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ewer = scn.GetFreepsCard("ewer");
		var strands = scn.GetFreepsCard("strands");
		var arwen = scn.GetFreepsCard("arwen");
		var swordwall = scn.GetFreepsCard("swordwall");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, scn.GetFreepsCard("elf1"), scn.GetFreepsCard("elf2"));
		scn.MoveCardsToSupportArea(ewer, strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(swordwall);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		// Arwen (6) beats the Uruk Savage (5), which takes a wound and is there in the regroup phase.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void PlayedInASkirmishItIsASkirmishEventForAResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var food = scn.GetFreepsCard("food");
		var flanking = scn.GetShadowCard("flanking");
		var lancers = scn.GetShadowCard("lancers");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(food);
		scn.MoveCardsToHand(flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(lancers);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lancers);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPlayCard(flanking);
		// Its cost is paid...
		assertEquals(1, scn.GetWoundsOn(lancers));
		// ...and the play of a skirmish event is answered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(food));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(food);
		assertInDiscard(flanking);
		// Cancelled: nothing is added.
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInTheManeuverPhaseItIsNoSkirmishEventForAResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var food = scn.GetFreepsCard("food");
		var flanking = scn.GetShadowCard("flanking");
		var lancers = scn.GetShadowCard("lancers");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(food);
		scn.MoveCardsToHand(flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(lancers);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPlayCard(flanking);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(lancers));
		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(flanking);
		assertInZone(Zone.HAND, food);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ItCostsWhatAnEventOfThePhaseItIsPlayedInCosts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var skulker = scn.GetShadowCard("skulker");
		var inManeuver = scn.GetShadowCard("twofaced1");
		var inSkirmish = scn.GetShadowCard("twofaced2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(inManeuver, inSkirmish);

		scn.StartGame();
		scn.MoveMinionsToTable(skulker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// In the maneuver phase it is a maneuver event: the Skulker takes nothing off.
		assertEquals(2, scn.GetTwilightCost(inManeuver));
		scn.ShadowPlayCard(inManeuver);
		assertEquals(8, scn.GetTwilight());
		assertInDiscard(inManeuver);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skulker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// In a skirmish it is a skirmish event: 2 - 1.
		assertEquals(1, scn.GetTwilightCost(inSkirmish));
		scn.ShadowPlayCard(inSkirmish);
		assertEquals(7, scn.GetTwilight());
		assertInDiscard(inSkirmish);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AManeuverEventsCostIsItsCostInTheManeuverPhaseAndASkirmishEventsIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var resisted = scn.GetFreepsCard("resisted");
		var skulker = scn.GetShadowCard("skulker");
		var lancers = scn.GetShadowCard("lancers");
		var flanking = scn.GetShadowCard("flanking");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(resisted);
		scn.MoveCardsToHand(flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(skulker, lancers);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);

		assertEquals(0, scn.GetTwilightCost(flanking));
		scn.FreepsPlayCard(resisted);
		assertEquals(10, scn.GetTwilight());

		// "Maneuver or Skirmish", in the maneuver phase: +2 as a maneuver event, and nothing off as a skirmish
		// event, which it is not.
		assertEquals(2, scn.GetTwilightCost(flanking));
		scn.ShadowPlayCard(flanking);
		assertEquals(1, scn.GetWoundsOn(lancers));
		// 10 - 2 + 3
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BackInTheDiscardPileItIsWhatIsPrintedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var stout = scn.GetFreepsCard("stout");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToHand(stout);

		scn.StartGame();
		scn.AddWoundsToChar(bilbo, 1);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Played as a maneuver event...
		scn.FreepsPlayCard(stout);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInDiscard(stout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, savage);
		scn.FreepsResolveSkirmish(bilbo);

		// ...and in the discard pile it is a "Maneuver or Skirmish" event again, which a text that looks for a
		// skirmish event there finds.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.HAND, stout);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
