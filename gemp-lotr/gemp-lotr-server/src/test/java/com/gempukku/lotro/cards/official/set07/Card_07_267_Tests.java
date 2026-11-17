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

public class Card_07_267_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("din", "7_267");
					put("pillager", "7_275");       // Gorgoroth Pillager: besieger Orc, strength 10; its text works only while stacked on a site its owner controls
					put("troll", "8_102");          // Great Hill Troll: besieger Troll, strength 16, fierce; its trigger is for its own play at sites 5K to 9K
					put("urukbesieger", "4_180");   // Uruk Besieger: [isengard] Uruk-hai, strength 6, "Besieger" in the title and not the keyword
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc who is no besieger, strength 7; his skirmish ability is used only on purpose
					put("soldier2", "1_271");
					put("officer", "7_274");        // Gorgoroth Officer: besieger Orc, strength 12; "Skirmish: Discard 2 cards from hand to play a [sauron] Orc stacked on a site you control. That Orc is fierce and strength +6 until the regroup phase."

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; his maneuver ability is used only on purpose
					put("gimli", "1_13");           // Gimli, Son of Gloin: Dwarf companion, twilight cost 2; his skirmish ability is used only on purpose
					put("cloak", "1_42");           // Elven Cloak: [elven] possession, twilight cost 1, only ever on top of a draw deck here
					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] condition, twilight cost 0, only ever on top of a draw deck here
					put("filler1", "1_177");        // Goblin Patrol Troop, cards held in a hand or lying in a draw deck
					put("filler2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DinofArmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Din of Arms
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Choose an opponent to discard the top card of his or her draw deck. If the twilight cost of that card is less than the number of besiegers you spot, take control of a site and all besiegers are strength +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var din = scn.GetFreepsCard("din");

		assertEquals("Din of Arms", din.getBlueprint().getTitle());
		assertNull(din.getBlueprint().getSubtitle());
		assertFalse(din.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, din.getBlueprint().getSide());
		assertEquals(Culture.SAURON, din.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, din.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(din, Timeword.MANEUVER));
		assertEquals(2, din.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACardOfCostOneUnderTwoBesiegersTakesASiteAndMakesEveryBesiegerStrongerAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var din = scn.GetShadowCard("din");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		var soldier = scn.GetShadowCard("soldier");
		var cloak = scn.GetFreepsCard("cloak");
		var gimli = scn.GetFreepsCard("gimli");
		var shadowTop = scn.GetShadowCard("filler1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(din);
		scn.SetFreepsDrawDeck(cloak, gimli);
		scn.SetShadowDrawDeck(shadowTop);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(pillager, troll, urukbesieger, soldier);
		scn.FreepsRefreshDecision();
		assertInHand(din);
		assertEquals(cloak, scn.GetFreepsTopOfDeck());
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(16, scn.GetStrength(troll));
		assertTrue(scn.ShadowPlayAvailable(din));

		scn.ShadowPlayCard(din);
		// Two besiegers to spot; the Uruk Besieger has the word in his title only.
		assertEquals(2, scn.ShadowGetChoiceMax());
		// A "spot X" count comes filled in with all that can be spotted (F-0561).
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// The Free Peoples player's top card, an Elven Cloak of cost 1, is discarded; the Shadow deck is untouched.
		assertInDiscard(cloak);
		assertEquals(gimli, scn.GetFreepsTopOfDeck());
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());
		assertInDiscard(din);

		// 1 is less than 2: site 1, behind both markers, is taken, and each besieger is strength +3.
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertEquals(13, scn.GetStrength(pillager));
		assertEquals(19, scn.GetStrength(troll));
		assertEquals(6, scn.GetStrength(urukbesieger));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACardOfCostTwoUnderTwoBesiegersIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var din = scn.GetShadowCard("din");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		var cloak = scn.GetFreepsCard("cloak");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(din);
		scn.SetFreepsDrawDeck(gimli, cloak);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(pillager, troll, urukbesieger);
		scn.FreepsRefreshDecision();
		assertInHand(din);
		assertEquals(gimli, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		scn.ShadowPlayCard(din);
		// The Uruk Besieger beside them would make three, were he counted.
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// Gimli, of cost 2, is discarded; 2 is not less than 2.
		assertInDiscard(gimli);
		assertEquals(cloak, scn.GetFreepsTopOfDeck());
		assertInDiscard(din);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(16, scn.GetStrength(troll));
		assertEquals(6, scn.GetStrength(urukbesieger));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyNothingIsDiscardedAndNothingElseHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var din = scn.GetShadowCard("din");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(din);
		scn.SetFreepsDrawDeck();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(pillager, troll);
		scn.FreepsRefreshDecision();
		assertInHand(din);
		assertEquals(0, scn.GetFreepsDeckCount());
		int discardBefore = scn.GetFreepsDiscardCount();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(din);
		scn.ShadowChoose("2");

		// No card was discarded, so there is no "that card" whose cost could be less than 2.
		assertEquals(discardBefore, scn.GetFreepsDiscardCount());
		assertInDiscard(din);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(16, scn.GetStrength(troll));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var din = scn.GetShadowCard("din");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(din);
		assertInHand(din);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(pillager, troll);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.GetTwilight() >= 2);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(din));
	}

	@Test
	public void TheBesiegersAreStrengthPlus3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var din = scn.GetShadowCard("din");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(din);
		scn.SetFreepsDrawDeck(cloak);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(pillager, troll, soldier, soldier2);
		scn.FreepsRefreshDecision();
		assertInHand(din);
		assertEquals(cloak, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(din);
		scn.ShadowChoose("2");
		assertEquals(13, scn.GetStrength(pillager));
		assertEquals(19, scn.GetStrength(troll));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// The second skirmish, after one has resolved: still +3.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(pillager));
		assertEquals(19, scn.GetStrength(troll));
		scn.PassCurrentPhaseActions();

		// The Troll is fierce: the fierce assignment phase still has it.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(13, scn.GetStrength(pillager));
		assertEquals(19, scn.GetStrength(troll));

		// The regroup phase: gone.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(16, scn.GetStrength(troll));
	}

	@Test
	public void ABesiegerThatComesIntoPlayAfterTheEventIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var din = scn.GetShadowCard("din");
		var officer = scn.GetShadowCard("officer");
		var pillager = scn.GetShadowCard("pillager");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(din, scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"));
		scn.SetFreepsDrawDeck(stairs);
		scn.SetTwilight(15);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.MoveMinionsToTable(officer, soldier);
		scn.FreepsRefreshDecision();
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.STACKED, pillager);
		assertEquals(stairs, scn.GetFreepsTopOfDeck());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(din);
		// Stairs of Khazad-dum, cost 0, against the one besieger in play.
		scn.ShadowChoose("1");
		assertInDiscard(stairs);
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(15, scn.GetStrength(officer));  // 12 + 3

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(officer));
		scn.ShadowUseCardAction(officer);
		scn.ShadowChooseCards(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"));

		// The Pillager is played from the stack: the Officer's +6 and fierce, and nothing of the event's +3.
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(16, scn.GetStrength(pillager));  // 10 + 6
		assertEquals(15, scn.GetStrength(officer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
