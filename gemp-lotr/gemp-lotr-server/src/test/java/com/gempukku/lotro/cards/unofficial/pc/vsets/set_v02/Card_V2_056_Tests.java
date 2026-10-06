package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("event", "102_56");

					put("guard1", "5_83");      // Household Guard: [rohan] Man, strength 6; "Valiant" is its whole text
					put("guard2", "5_83");
					put("defender", "68_9");    // Elven Defender: valiant [elven] Elf, strength 5
					// Elite Rider: a [rohan] Man, strength 6, not valiant; stronger only while skirmishing a
					// wounded minion.
					put("rider1", "4_265");
					put("rider2", "4_265");
					// Theoden, Son of Thengel: strength 6, vitality 2, not valiant; a regroup ability that needs
					// a [rohan] ally.
					put("theoden", "4_292");
					// Theoden, Last of His House: valiant, strength 6 and +1 for each other valiant companion
					// assigned to a skirmish.  The V2 card spells the name without its accent.
					put("lasttheoden", "102_55");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, not valiant; a maneuver ability
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; strength 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally; a fellowship ability

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					put("savage3", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ToTheKingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: To the King!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot an unassigned valiant companion to have them replace an unbound companion in a skirmish
		 * 	(and make them damage +1 if that unbound companion is Theoden).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("event");

		assertEquals("To the King!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnUnassignedValiantCompanionTakesTheUnboundCompanionsPlaceInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetFreepsCard("event");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard1");
		var assignedGuard = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		var rider = scn.GetFreepsCard("rider1");
		var idleRider = scn.GetFreepsCard("rider2");
		var theoden = scn.GetFreepsCard("theoden");
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(guard, assignedGuard, defender, rider, idleRider, theoden, sam);
		scn.MoveAlliesToTable(grimir);
		// No ally and no Ring-bound companion is valiant in print, so these two are made valiant: one is
		// valiant and no companion, the other a valiant companion who is not unbound.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, grimir, null, Keyword.VALIANT));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, sam, null, Keyword.VALIANT));
		scn.MoveCardsToHand(event);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, savage3);
		scn.SkipToAssignments();
		// The Rider fights first.  A Household Guard and Theoden are assigned to skirmishes still to come.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { rider, savage1 },
				new PhysicalCardImpl[] { assignedGuard, savage2 },
				new PhysicalCardImpl[] { theoden, savage3 }
		);
		scn.FreepsResolveSkirmish(rider);

		assertTrue(scn.IsCharSkirmishingAgainst(rider, savage1));
		assertTrue(scn.IsCharAssignedAgainst(assignedGuard, savage2));
		assertTrue(scn.IsCharAssignedAgainst(theoden, savage3));
		assertFalse(scn.IsCharAssigned(guard));
		assertTrue(scn.HasKeyword(grimir, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(sam, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(idleRider, Keyword.VALIANT));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(event));

		scn.FreepsPlayCard(event);
		// Valiant companions nobody is assigned to, whatever their culture and whether Ring-bound or not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, defender, sam));
		// Not valiant; valiant and assigned to a skirmish that has not begun; valiant and an ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(idleRider, assignedGuard, grimir, rider, theoden, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, savage1));
		assertFalse(scn.IsCharSkirmishing(rider));
		assertFalse(scn.IsCharAssigned(rider));
		// The unbound companions whose skirmishes have not begun are left where they were.
		assertTrue(scn.IsCharAssignedAgainst(assignedGuard, savage2));
		assertFalse(scn.IsCharSkirmishing(assignedGuard));
		assertTrue(scn.IsCharAssignedAgainst(theoden, savage3));
		assertFalse(scn.IsCharSkirmishing(theoden));
		// The companion replaced is a Man of [rohan] and not Theoden, who is on the table and assigned
		// to another skirmish.
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.DISCARD, event);
		// The skirmish goes on where it was: the Shadow player is next to act in it.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// The Guard's 6 beats the Savage's 5, and it is the Guard's skirmish that was won: one wound.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(rider));

		scn.FreepsResolveSkirmish(assignedGuard);
		assertTrue(scn.IsCharSkirmishingAgainst(assignedGuard, savage2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyValiantCompanionIsAssignedAndIsOnceHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetFreepsCard("event");
		var defender = scn.GetFreepsCard("defender");
		var aragorn = scn.GetFreepsCard("aragorn");
		var idleRider = scn.GetFreepsCard("rider2");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(defender, aragorn, idleRider);
		scn.MoveAlliesToTable(grimir);
		// No ally is valiant in print, so Grimir is made valiant: valiant, unassigned, and no companion.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, grimir, null, Keyword.VALIANT));
		scn.MoveCardsToHand(event);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { defender, savage1 },
				new PhysicalCardImpl[] { aragorn, savage2 }
		);

		// The one valiant companion is the one fighting; an Elite Rider who is not valiant is unassigned.
		scn.FreepsResolveSkirmish(defender);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(defender, savage1));
		assertFalse(scn.IsCharAssigned(idleRider));
		assertTrue(scn.HasKeyword(grimir, Keyword.VALIANT));
		assertFalse(scn.FreepsPlayAvailable(event));

		// The Defender's 5 ties the Savage's 5 and loses: one wound, and one more for the Savage's damage +1.
		// His skirmish is over and he is assigned to nothing.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(defender));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage2));
		assertFalse(scn.IsCharAssigned(defender));
		assertTrue(scn.FreepsPlayAvailable(event));

		// One valiant companion to spot, an Elf of [elven], so no choice is put; and the companion he
		// replaces is of [gondor].
		scn.FreepsPlayCard(event);
		assertTrue(scn.IsCharSkirmishingAgainst(defender, savage2));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.HasKeyword(defender, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ReplacingTheodenMakesTheNewcomerDamagePlus1ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetFreepsCard("event");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var rider = scn.GetFreepsCard("rider1");
		scn.MoveCompanionsToTable(theoden, guard, defender, rider);
		scn.MoveCardsToHand(event);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { theoden, savage1 },
				new PhysicalCardImpl[] { rider, savage2 }
		);
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.IsCharSkirmishingAgainst(theoden, savage1));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(event));

		scn.FreepsPlayCard(event);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, defender));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden, rider));

		scn.FreepsChooseCard(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, savage1));
		assertFalse(scn.IsCharSkirmishing(theoden));
		// Damage +1 for the one who came, not for Theoden nor for the valiant companion who stayed.
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(theoden, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(defender, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// The Guard's 6 beats the Savage's 5: one wound for losing and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(theoden));

		// That skirmish phase is over, and in the next the Guard is damage +1 no longer.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.IsCharSkirmishingAgainst(rider, savage2));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ReplacingTheodenLastOfHisHouseAlsoMakesTheNewcomerDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetFreepsCard("event");
		var theoden = scn.GetFreepsCard("lasttheoden");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		scn.MoveCompanionsToTable(theoden, guard, defender);
		scn.MoveCardsToHand(event);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(theoden, savage1);

		assertTrue(scn.IsCharSkirmishingAgainst(theoden, savage1));
		assertTrue(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(defender, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(event));

		scn.FreepsPlayCard(event);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, defender));
		// This Theoden is valiant, and he is the one fighting: he cannot take his own place.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden));

		scn.FreepsChooseCard(defender);
		assertTrue(scn.IsCharSkirmishingAgainst(defender, savage1));
		assertFalse(scn.IsCharSkirmishing(theoden));
		assertEquals(1, scn.GetKeywordCount(defender, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(theoden, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MayBePlayedToNoEffectWhileARingBoundCompanionIsFighting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetFreepsCard("event");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var rider = scn.GetFreepsCard("rider1");
		scn.MoveCompanionsToTable(sam, guard, defender, rider);
		scn.MoveCardsToHand(event);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { sam, savage1 },
				new PhysicalCardImpl[] { rider, savage2 }
		);
		scn.FreepsResolveSkirmish(sam);

		// Sam is Ring-bound, and the Rider's skirmish has not begun: no unbound companion is in a skirmish.
		// There are valiant companions to spot, which is all the event asks before its "to".
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage1));
		assertTrue(scn.IsCharAssignedAgainst(rider, savage2));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(event));

		scn.FreepsPlayCard(event);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, defender));
		scn.FreepsChooseCard(guard);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage1));
		assertFalse(scn.IsCharSkirmishing(guard));
		assertFalse(scn.IsCharAssigned(guard));
		assertTrue(scn.IsCharAssignedAgainst(rider, savage2));
		assertFalse(scn.IsCharSkirmishing(rider));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
