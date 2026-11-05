package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alive1", "4_143");
					put("alive2", "4_143");
					// Uruk Pursuer: an [isengard] tracker, vitality 3, fierce; its texts are about unbound Hobbits
					// killed or discarded.
					put("pursuer", "4_190");
					// Uruk Chaser: an [isengard] tracker, vitality 2, fierce; stronger only in a fierce skirmish.
					put("chaser", "4_181");
					// Orc Ambusher: a [sauron] tracker, vitality 2; a roaming discount and a response to a Free
					// Peoples regroup event.
					put("ambusher", "1_261");
					// Uruk Savage: an [isengard] Uruk-hai who is no tracker, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Aragorn, Ranger of the North: an unbound [gondor] Man, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: an unbound [dwarven] Dwarf, vitality 3; a skirmish ability never used.
					put("gimli", "1_13");
					// Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4.
					put("merry", "1_303");
					// Pippin, Trollslayer: an unbound [gondor] Hobbit, vitality 4; a skirmish ability never used.
					put("pippin", "103_31");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability never used.
					put("sam", "1_311");
					// Bounder: a Hobbit ally whose home is site 2, where these assignment phases are; a skirmish
					// ability never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BroughtBackAliveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Brought Back Alive
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Search</b>.<br><b>Assignment:</b> Exert an [isengard] tracker to assign it to an unbound companion. That companion may exert to prevent this (unless that companion is a Hobbit).
		*/

		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive1");

		assertEquals("Brought Back Alive", alive.getBlueprint().getTitle());
		assertNull(alive.getBlueprint().getSubtitle());
		assertFalse(alive.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, alive.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, alive.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, alive.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(alive, Timeword.ASSIGNMENT));
		assertTrue(scn.HasKeyword(alive, Keyword.SEARCH));
		assertEquals(0, alive.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardTrackerToAssignItToAnUnboundCompanionWhoMayExertToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive1 = scn.GetShadowCard("alive1");
		var alive2 = scn.GetShadowCard("alive2");
		var pursuer = scn.GetShadowCard("pursuer");
		var chaser = scn.GetShadowCard("chaser");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, merry, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(alive1, alive2);

		scn.StartGame();
		assertInZone(Zone.HAND, alive1, alive2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, chaser, ambusher, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Site 2 is the Bounder's home, so he could be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(bounder));
		assertTrue(scn.ShadowPlayAvailable(alive1));
		scn.ShadowPlayCard(alive1);

		// Two [isengard] trackers of different titles; not a [sauron] tracker, nor an [isengard] Uruk-hai who is
		// no tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, chaser));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ambusher, savage));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.ShadowChooseCard(pursuer);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(chaser));

		// Three unbound companions; not Sam (Ring-bound), not Frodo, not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo, bounder));
		scn.ShadowChooseCard(gimli);

		// Gimli exerts to prevent it: nobody is assigned, and the Pursuer's exertion stands.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(pursuer));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertInZone(Zone.DISCARD, alive1);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The second copy, declined: the tracker exerted is assigned to Aragorn, who does not exert; the other
		// tracker is not.
		scn.FreepsPass();
		scn.ShadowPlayCard(alive2);
		scn.ShadowChooseCard(pursuer);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(aragorn, pursuer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(chaser));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AHobbitChosenIsNotAskedToExertAndIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive1 = scn.GetShadowCard("alive1");
		var alive2 = scn.GetShadowCard("alive2");
		var pursuer = scn.GetShadowCard("pursuer");
		var chaser = scn.GetShadowCard("chaser");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, merry, pippin);
		scn.MoveCardsToHand(alive1, alive2);

		scn.StartGame();
		assertInZone(Zone.HAND, alive1, alive2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, chaser);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// A [shire] Hobbit: nothing is asked, and he is assigned unwounded.
		scn.ShadowPlayCard(alive1);
		scn.ShadowChooseCard(pursuer);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry, pippin));
		scn.ShadowChooseCard(merry);
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(merry, pursuer));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// A [gondor] Hobbit: the same.
		scn.FreepsPass();
		scn.ShadowPlayCard(alive2);
		scn.ShadowChooseCard(chaser);
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, pippin));
		scn.ShadowChooseCard(pippin);
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(pippin, chaser));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionChosenIsNotAskedAndIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive1 = scn.GetShadowCard("alive1");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(alive1);

		scn.StartGame();
		assertInZone(Zone.HAND, alive1);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowPlayCard(alive1);
		// The one tracker exerts unasked.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		scn.ShadowChooseCard(aragorn);
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, pursuer));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheTrackerExertsForNothingAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive1 = scn.GetShadowCard("alive1");
		var pursuer = scn.GetShadowCard("pursuer");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(alive1);

		scn.StartGame();
		assertInZone(Zone.HAND, alive1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowPlayAvailable(alive1));
		scn.ShadowPlayCard(alive1);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertFalse(scn.IsCharAssigned(pursuer));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertInZone(Zone.DISCARD, alive1);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyIsengardTrackerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive1 = scn.GetShadowCard("alive1");
		var pursuer = scn.GetShadowCard("pursuer");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(alive1);

		scn.StartGame();
		assertInZone(Zone.HAND, alive1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, ambusher, savage);
		// Vitality 3 with 2 wounds: the Pursuer cannot exert.  The [sauron] tracker and the Uruk-hai could.
		scn.AddWoundsToChar(pursuer, 2);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(alive1));
	}
}
