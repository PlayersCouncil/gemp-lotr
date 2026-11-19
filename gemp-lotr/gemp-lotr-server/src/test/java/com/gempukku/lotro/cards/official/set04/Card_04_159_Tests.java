package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_159_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("riddles1", "4_159");
					put("riddles2", "4_159");
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
					// Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: a [dwarven] Dwarf companion, vitality 3; a skirmish ability never used.
					put("gimli", "1_13");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability never used.
					put("sam", "1_311");
					// Bounder: a Hobbit ally whose home is site 2; a skirmish ability never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ManyRiddlesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Many Riddles
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Strength: -1
		 * Game Text: <b>Search</b>. To play, exert an [isengard] tracker. Plays on a companion (except the Ring- bearer).<br><b>Assignment:</b> Exert an [isengard] tracker and remove (2) to assign that tracker to bearer. Bearer may exert to prevent this.
		*/

		var scn = GetScenario();

		var riddles = scn.GetFreepsCard("riddles1");

		assertEquals("Many Riddles", riddles.getBlueprint().getTitle());
		assertNull(riddles.getBlueprint().getSubtitle());
		assertFalse(riddles.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, riddles.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, riddles.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, riddles.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(riddles, Keyword.SEARCH));
		assertEquals(1, riddles.getBlueprint().getTwilightCost());
		assertEquals(-1, riddles.getBlueprint().getStrength());
	}

	@Test
	public void PlayedByExertingAnIsengardTrackerOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles1 = scn.GetShadowCard("riddles1");
		var pursuer = scn.GetShadowCard("pursuer");
		var chaser = scn.GetShadowCard("chaser");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(riddles1);

		scn.StartGame();
		assertInZone(Zone.HAND, riddles1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, chaser, ambusher, savage);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(riddles1));
		scn.ShadowPlayCard(riddles1);

		// Companions of three cultures and races, Sam among them (Ring-bound, but not the Ring-bearer); not
		// Frodo, the Ring-bearer, nor the ally, nor a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, bounder, savage));
		scn.ShadowChooseCard(sam);

		// Two [isengard] trackers of different titles; not a [sauron] tracker, nor an [isengard] Uruk-hai who is
		// no tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, chaser));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ambusher, savage));
		assertEquals(0, scn.GetWoundsOn(chaser));
		scn.ShadowChooseCard(chaser);
		assertEquals(1, scn.GetWoundsOn(chaser));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(savage));

		assertAttachedTo(riddles1, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyIsengardTrackerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles1 = scn.GetShadowCard("riddles1");
		var pursuer = scn.GetShadowCard("pursuer");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(riddles1);

		scn.StartGame();
		assertInZone(Zone.HAND, riddles1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, ambusher, savage);
		// Vitality 3 with 2 wounds: the Pursuer cannot exert.  The [sauron] tracker and the Uruk-hai could.
		scn.AddWoundsToChar(pursuer, 2);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(riddles1));
	}

	@Test
	public void ExertsAnIsengardTrackerAndRemoves2ToAssignItToBearerAndASecondUseWithThatTrackerAssignedDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles1 = scn.GetShadowCard("riddles1");
		var riddles2 = scn.GetShadowCard("riddles2");
		var pursuer = scn.GetShadowCard("pursuer");
		var chaser = scn.GetShadowCard("chaser");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, chaser, ambusher, savage);
		scn.AttachCardsTo(gimli, riddles1);
		scn.AttachCardsTo(aragorn, riddles2);
		assertAttachedTo(riddles1, gimli);
		assertAttachedTo(riddles2, aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(riddles1));
		scn.ShadowUseCardAction(riddles1);
		// The tracker to exert: two [isengard] trackers of different titles; not a [sauron] tracker, nor an
		// [isengard] Uruk-hai who is no tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, chaser));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ambusher, savage));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.ShadowChooseCard(pursuer);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(chaser));
		assertEquals(4, scn.GetTwilight());

		// Gimli, the bearer, declines to exert: the tracker exerted is assigned to him, and to nobody else.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsCharAssignedAgainst(gimli, pursuer));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(chaser));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The other copy, on Aragorn, with the Pursuer already assigned: the costs are paid and nothing more
		// happens; Aragorn is not asked to exert.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(riddles2));
		scn.ShadowUseCardAction(riddles2);
		scn.ShadowChooseCard(pursuer);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(gimli, pursuer));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BearerExertsToPreventTheAssignmentAndTheCostsStayPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles1 = scn.GetShadowCard("riddles1");
		var chaser = scn.GetShadowCard("chaser");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(chaser);
		scn.AttachCardsTo(aragorn, riddles1);
		assertAttachedTo(riddles1, aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(riddles1);
		// The one tracker exerts unasked.
		assertEquals(1, scn.GetWoundsOn(chaser));
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(chaser));
		assertEquals(1, scn.GetWoundsOn(chaser));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnExhaustedBearerIsNotAskedAndIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles1 = scn.GetShadowCard("riddles1");
		var chaser = scn.GetShadowCard("chaser");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.MoveMinionsToTable(chaser);
		scn.AttachCardsTo(aragorn, riddles1);
		assertAttachedTo(riddles1, aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(riddles1);
		assertEquals(1, scn.GetWoundsOn(chaser));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, chaser));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithLessThan2TwilightOrWithTheOnlyIsengardTrackerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles1 = scn.GetShadowCard("riddles1");
		var pursuer = scn.GetShadowCard("pursuer");
		var ambusher = scn.GetShadowCard("ambusher");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, ambusher, savage);
		scn.AttachCardsTo(aragorn, riddles1);
		assertAttachedTo(riddles1, aragorn);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// (1) in the pool, the tracker fresh: not offered.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(riddles1));

		// (2) in the pool: offered.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(riddles1));

		// Vitality 3 with 2 wounds: the Pursuer cannot exert; the [sauron] tracker and the Uruk-hai could.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.AddWoundsToChar(pursuer, 2);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(riddles1));
	}
}
