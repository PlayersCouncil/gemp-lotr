package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_212_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weary", "4_212");
					put("weary2", "4_212");
					put("weary3", "4_212");
					put("weary4", "4_212");
					// Rest While You Can: another [isengard] search condition that plays on a companion; its text answers a kill only
					put("rest", "4_171");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion to assign who bears nothing
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally; a fellowship ability used only on purpose
					put("heir", "4_109");       // Aragorn, Heir of Elendil: defender +1 is his whole text

					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] tracker, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("footsoldier", "4_187");// Uruk Foot Soldier: [isengard] tracker, fierce; strength +3 and damage +1 only in a fierce skirmish
					put("ambusher", "1_261");   // Orc Ambusher: [sauron] tracker; its texts lower a roaming penalty and answer a regroup event
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no tracker; damage +1 only

					// The End Comes: "Assignment: Spot 5 [moria] minions to make the Free Peoples player assign the
					// Ring-bearer to a skirmish"; the Troop and four Runners are the five (cheated in, so no Runner's (2))
					put("endcomes", "1_169");
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner1", "1_178");    // Goblin Runner: its only text answers its own play
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WearyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Weary
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Search</b>. Plays on a companion or ally. Limit 1 per character.<br>Each time the Free Peoples player assigns bearer to skirmish an [isengard] tracker, bearer must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weary");

		assertEquals("Weary", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnACompanionOrAnAllyAndNotOnAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(weary);
		assertInHand(weary);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(weary);
		assertTrue(scn.ShadowPlayAvailable(weary));
		scn.ShadowPlayCard(weary);
		// Companions of two cultures and races, and an ally; not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo, maggot));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(maggot);

		assertAttachedTo(weary, maggot);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OneWearyToACharacterAndWithEveryCharacterBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var weary2 = scn.GetShadowCard("weary2");
		var weary3 = scn.GetShadowCard("weary3");
		var weary4 = scn.GetShadowCard("weary4");
		var rest = scn.GetShadowCard("rest");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(weary2, weary3, weary4);
		assertInHand(weary4);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, weary);
		scn.AttachCardsTo(frodo, rest);
		assertAttachedTo(weary, aragorn);
		assertAttachedTo(rest, frodo);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(weary2);
		// Not Aragorn, who bears one; Frodo, who bears another [isengard] condition, and Maggot, who bears nothing, are.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, maggot));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(frodo);
		assertAttachedTo(weary2, frodo);

		// Maggot is the one character left without one, and takes the third unasked.
		scn.ShadowPlayCard(weary3);
		assertAttachedTo(weary3, maggot);

		// Every companion and ally now bears one: the fourth cannot be played.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(weary4);
		assertFalse(scn.ShadowPlayAvailable(weary4));
	}

	@Test
	public void EachBearerTheFreePeoplesPlayerAssignsToAnIsengardTrackerExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var weary2 = scn.GetShadowCard("weary2");
		var weary3 = scn.GetShadowCard("weary3");
		var weary4 = scn.GetShadowCard("weary4");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var pursuer = scn.GetShadowCard("pursuer");
		var footsoldier = scn.GetShadowCard("footsoldier");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, weary);
		scn.AttachCardsTo(frodo, weary2);
		scn.AttachCardsTo(gimli, weary3);
		scn.AttachCardsTo(boromir, weary4);
		scn.MoveMinionsToTable(pursuer, footsoldier, ambusher, savage);
		scn.SkipToAssignments();

		assertAttachedTo(weary, aragorn);
		assertAttachedTo(weary2, frodo);
		assertAttachedTo(weary3, gimli);
		assertAttachedTo(weary4, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, pursuer},
				new PhysicalCardImpl[]{frodo, footsoldier},
				new PhysicalCardImpl[]{gimli, ambusher},
				new PhysicalCardImpl[]{boromir, savage});

		// Two [isengard] trackers: each bearer assigned to one exerts.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		// A tracker of another culture, and an [isengard] minion who is no tracker: no exertion.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheShadowPlayersAssignmentOfBearerAndAnotherCompanionAssignedToATrackerDoNotMakeBearerExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var pursuer = scn.GetShadowCard("pursuer");
		var footsoldier = scn.GetShadowCard("footsoldier");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, weary);
		scn.MoveMinionsToTable(pursuer, footsoldier);
		scn.SkipToAssignments();

		assertAttachedTo(weary, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		// The Free Peoples player assigns a companion who bears no Weary to an [isengard] tracker.
		scn.FreepsAssignToMinions(sam, pursuer);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		// The leftover tracker is the Shadow player's to assign, and he assigns it to the bearer.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(aragorn, footsoldier);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, footsoldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void BearerAssignedToAFierceTrackerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, weary);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();

		assertAttachedTo(weary, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsAssignToMinions(aragorn, pursuer);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Aragorn's 8 beats the Pursuer's 5, which takes a wound and lives (vitality 3).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Pursuer is fierce: assigned to it again, Aragorn exerts again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void MadeByAShadowCardToAssignBearerTheFreePeoplesPlayerWhoPicksAnIsengardTrackerMakesHimExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var endcomes = scn.GetShadowCard("endcomes");
		var frodo = scn.GetRingBearer();
		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(endcomes);

		scn.StartGame();
		scn.AttachCardsTo(frodo, weary);
		scn.MoveMinionsToTable(pursuer, troop, scn.GetShadowCard("runner1"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertAttachedTo(weary, frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's; the Free Peoples player picks the minion, so the assignment is his (Q-0171).
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, troop));
		scn.FreepsChooseCard(pursuer);

		assertTrue(scn.IsCharAssignedAgainst(frodo, pursuer));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ADefenderBearerAssignedToTwoIsengardTrackersInOneAnswerExertsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weary = scn.GetShadowCard("weary");
		var heir = scn.GetFreepsCard("heir");
		var pursuer = scn.GetShadowCard("pursuer");
		var footsoldier = scn.GetShadowCard("footsoldier");
		scn.MoveCompanionsToTable(heir);

		scn.StartGame();
		scn.AttachCardsTo(heir, weary);
		scn.MoveMinionsToTable(pursuer, footsoldier);
		scn.SkipToAssignments();

		assertAttachedTo(weary, heir);
		assertEquals(0, scn.GetWoundsOn(heir));
		scn.FreepsAssignToMinions(heir, pursuer, footsoldier);

		assertTrue(scn.IsCharAssignedAgainst(heir, pursuer));
		assertTrue(scn.IsCharAssignedAgainst(heir, footsoldier));
		// One assignment of bearer, to one skirmish: one exertion (reading A of Q-0323).
		assertEquals(1, scn.GetWoundsOn(heir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
