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

public class Card_04_186_Tests
{

	// The Fellowship path: site 2, Ettenmoors, is Bounder's home, so he can be assigned there on turn 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("follower1", "4_186");
					put("follower2", "4_186");

					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] unbound Hobbit, strength 3, vitality 4; +2 only while bearing a weapon (none here)
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] unbound Hobbit, strength 3, vitality 4; enduring; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit, strength 3, vitality 4; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F, strength 2, vitality 2; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man, strength 8, vitality 4; a maneuver ability used only on purpose

					put("nelya", "2_84");       // Ulaire Nelya: a Nazgul, strength 10, vitality 3; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("beauty", "1_205");     // Beauty Is Fading: Maneuver: exert a Nazgul to discard a Free Peoples possession or condition, or with none, an ally or companion (except the Ring-bearer)
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text answers only its own play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukFollowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Follower
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.
		 * 	Unbound Hobbits may not be discarded.
		 * 	<b>Response:</b> If an unbound Hobbit is killed, exert this minion to add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("follower1");

		assertEquals("Uruk Follower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UnboundHobbitsCannotBeDiscardedWhileOtherCompanionsAndAHobbitAllyCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follower1 = scn.GetShadowCard("follower1");
		var nelya = scn.GetShadowCard("nelya");
		var beauty = scn.GetShadowCard("beauty");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(follower1, nelya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// No Free Peoples possession or condition is in play: the event discards an ally or companion instead.
		assertTrue(scn.ShadowPlayAvailable(beauty));
		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.ShadowPlayCard(beauty);
		assertEquals(1, scn.GetWoundsOn(nelya));
		// Unbound Hobbits of two cultures are not offered; an unbound companion who is no Hobbit, a Ring-bound
		// Hobbit and a Hobbit ally are.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merry, pippin, frodo));
		scn.ShadowChooseCard(bounder);

		assertInDiscard(bounder);
		assertInZone(Zone.FREE_CHARACTERS, merry, pippin, sam, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutAnUrukFollowerAnUnboundHobbitCanBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var beauty = scn.GetShadowCard("beauty");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(beauty);
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, pippin, aragorn));
		scn.ShadowChooseCard(merry);

		assertInDiscard(merry);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenAnUnboundHobbitIsKilledTheFollowerThatCanExertExertsToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follower1 = scn.GetShadowCard("follower1");
		var follower2 = scn.GetShadowCard("follower2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(follower1, follower2);
		// Vitality 3 with 2 wounds: the first Follower is exhausted and cannot exert.
		scn.AddWoundsToChar(follower1, 2);
		assertEquals(2, scn.GetWoundsOn(follower1));
		assertTrue(scn.IsExhausted(follower1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, follower2);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);
		assertEquals(2, scn.GetWoundsOn(follower1));
		assertEquals(0, scn.GetWoundsOn(follower2));
		assertEquals(0, scn.GetBurdens());

		// 10 against 3: Merry is overwhelmed and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(follower2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(follower1));

		scn.ShadowChooseOptionalTrigger(follower2);
		// "This minion": the Follower that responded exerts, and the other is left as it was.
		assertEquals(1, scn.GetWoundsOn(follower2));
		assertEquals(2, scn.GetWoundsOn(follower1));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// A Response may answer the same kill again while its cost can be paid (CR 4.2 "response").
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(follower2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(follower1));
		scn.ShadowChooseOptionalTrigger(follower2);
		assertEquals(2, scn.GetWoundsOn(follower2));
		assertEquals(2, scn.GetBurdens());

		// Both Followers exhausted now: nothing more is offered, and the fierce round follows.
		assertTrue(scn.IsExhausted(follower2));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsOfferedWhenAnUnboundHobbitIsKilledAndNotForOtherDeathsOrAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follower1 = scn.GetShadowCard("follower1");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var runner = scn.GetShadowCard("runner");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(follower1, troop1, troop2, troop3, runner);
		// Vitality 4 with 3 wounds: the next wound kills Aragorn.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, troop1},
				new PhysicalCardImpl[]{aragorn, troop2},
				new PhysicalCardImpl[]{bounder, troop3},
				new PhysicalCardImpl[]{merry, runner},
				new PhysicalCardImpl[]{pippin, follower1});

		// A Ring-bound Hobbit killed: not offered.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(follower1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// An unbound companion who is no Hobbit killed: not offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(follower1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// A Hobbit ally killed at his home site: not offered.
		scn.FreepsResolveSkirmish(bounder);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, bounder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(follower1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// An unbound Hobbit who loses and is wounded, not killed (5 against 3): not offered.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(follower1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// An unbound Hobbit of another culture killed (10 against 3): offered.
		assertEquals(0, scn.GetWoundsOn(follower1));
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(follower1));
	}
}
