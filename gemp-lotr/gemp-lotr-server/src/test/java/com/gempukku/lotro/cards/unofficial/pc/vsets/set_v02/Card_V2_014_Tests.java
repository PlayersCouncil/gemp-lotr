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

public class Card_V2_014_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("remember", "102_14");

					// Gandalf, The Grey Wizard: strength 7, not valiant; a fellowship ability.
					put("gandalf", "1_364");
					// Gandalf, Lathspell: strength 7. "Each mounted companion gains valiant", so on Shadowfax he
					// is valiant himself: no Gandalf is valiant in print.  His other text answers minions'
					// special abilities, and no minion here has one.
					put("lathspell", "102_13");
					// Shadowfax, Greatheart: a mount for Gandalf that changes no number; a skirmish ability.
					put("shadowfax", "8_21");

					// Theoden, Son of Thengel: strength 6, not valiant; a regroup ability that needs a [rohan] ally.
					put("theoden", "4_292");
					// Theoden, Tall and Proud: strength 7, valiant; a trigger when he is killed.
					put("tallproud", "8_92");

					put("guard1", "5_83");       // Household Guard: [rohan] Man, strength 6; "Valiant" is its whole text
					put("guard2", "5_83");
					put("defender", "68_9");     // Elven Defender: valiant [elven] Elf, strength 5
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, not valiant; a maneuver ability
					// Elite Rider: a [rohan] Man like Theoden, strength 6, not valiant; stronger only while
					// skirmishing a wounded minion.
					put("rider", "4_265");
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound, strength 3, not valiant
					put("grimir", "1_17");       // Grimir, Dwarven Elder: an ally, strength 3, not valiant
					// Radagast, The Brown: a [gandalf] Wizard who is not Gandalf.  The move limit is +1.
					put("radagast", "9_26");

					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, no text
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RememberYourOldStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Remember Your Old Strength
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Gandalf to make Theoden and each valiant companion strength +1 until the regroup
		 * phase (or strength +2 if every unbound companion is valiant).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("remember");

		assertEquals("Remember Your Old Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToMakeTheodenAndEachValiantCompanionStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var remember = scn.GetFreepsCard("remember");
		var gandalf = scn.GetFreepsCard("gandalf");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gandalf, theoden, guard, defender, aragorn, rider, sam);
		// No ally is valiant in print, so Grimir is made valiant: valiant, and not a companion.
		scn.MoveAlliesToTable(grimir);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, grimir, null, Keyword.VALIANT));
		// Nor is any Ring-bound companion, so Sam is made valiant: a valiant companion who is not unbound.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, sam, null, Keyword.VALIANT));
		scn.MoveCardsToHand(remember);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(grimir, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(defender, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(sam, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(gandalf, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));

		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(3, scn.GetStrength(sam));
		// Frodo's 3 + 1 for the Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(13, scn.GetStrength(troop));

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(remember));

		scn.FreepsPlayCard(remember);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, remember);
		// Gandalf exerts, and nobody else.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Theoden by his name, and the valiant companions: two who are unbound and Sam, who is Ring-bound.
		// Aragorn, the Elite Rider and Gandalf are unbound companions who are not valiant, so it is +1 and
		// not +2.
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));
		assertEquals(4, scn.GetStrength(sam));
		// Nobody else: not a [rohan] Man who is neither Theoden nor valiant.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(13, scn.GetStrength(troop));

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileGandalfIsExhaustedThoughAnotherWizardCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var remember = scn.GetFreepsCard("remember");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(gandalf, radagast, aragorn, guard);
		scn.MoveCardsToHand(remember);
		// Vitality 4 with 3 wounds.
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(radagast));
		assertInZone(Zone.HAND, remember);
		assertFalse(scn.FreepsPlayAvailable(remember));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// One wound is taken off Gandalf.  Aragorn's ability is used only to have the Free Peoples player
		// asked afresh.
		scn.RemoveWoundsFromChar(gandalf, 1);
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();
		assertTrue(scn.FreepsPlayAvailable(remember));

		scn.FreepsPlayCard(remember);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesAndEndsAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var remember = scn.GetFreepsCard("remember");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		scn.MoveCompanionsToTable(gandalf, guard, defender);
		scn.MoveCardsToHand(remember);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));

		scn.FreepsPlayCard(remember);
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { defender, runner1 },
				new PhysicalCardImpl[] { guard, runner2 }
		);

		// The Elven Defender's 5 + 1 beats a Runner's 5, which his 5 alone would not.
		scn.FreepsResolveSkirmish(defender);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);
		assertEquals(0, scn.GetWoundsOn(defender));

		// One skirmish phase has ended, and the Household Guard fights the next still at 7.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(defender));

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(defender));
	}

	@Test
	public void WithEveryUnboundCompanionValiantTheBonusIsPlus2AndStaysPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var remember = scn.GetFreepsCard("remember");
		var lathspell = scn.GetFreepsCard("lathspell");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var tallproud = scn.GetFreepsCard("tallproud");
		var guard = scn.GetFreepsCard("guard1");
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(lathspell, tallproud, guard, sam);
		scn.AttachCardsTo(lathspell, shadowfax);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(remember);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// The three unbound companions are valiant: Gandalf by the mount he bears.
		assertTrue(scn.HasKeyword(lathspell, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(tallproud, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		// The two Ring-bound companions and the ally are not, and are not asked to be.
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(sam, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(grimir, Keyword.VALIANT));

		assertEquals(7, scn.GetStrength(lathspell));
		assertEquals(7, scn.GetStrength(tallproud));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(grimir));

		scn.FreepsPlayCard(remember);
		assertEquals(1, scn.GetWoundsOn(lathspell));
		// Gandalf is a valiant companion too.
		assertEquals(9, scn.GetStrength(lathspell));
		assertEquals(9, scn.GetStrength(tallproud));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Aragorn, an unbound companion who is not valiant, arrives afterwards.  What the event gave was
		// settled when it was played.
		scn.MoveCompanionsToTable(aragorn);
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertEquals(9, scn.GetStrength(lathspell));
		assertEquals(9, scn.GetStrength(tallproud));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void WithGandalfTheOneUnboundCompanionNotValiantTheBonusIsPlus1AndStaysPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var remember = scn.GetFreepsCard("remember");
		var lathspell = scn.GetFreepsCard("lathspell");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var tallproud = scn.GetFreepsCard("tallproud");
		var guard = scn.GetFreepsCard("guard1");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(lathspell, tallproud, guard, sam);
		scn.MoveCardsToHand(remember);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// Without a mount Gandalf is not valiant, and he is an unbound companion.
		assertFalse(scn.HasKeyword(lathspell, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(tallproud, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));

		scn.FreepsPlayCard(remember);
		assertEquals(1, scn.GetWoundsOn(lathspell));
		assertEquals(8, scn.GetStrength(tallproud));
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(lathspell));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Shadowfax is put on Gandalf afterwards.  He is valiant now, and so is every unbound companion, but
		// he was not when the event was played: he gets nothing, and the others' +1 does not become +2.
		scn.AttachCardsTo(lathspell, shadowfax);
		assertTrue(scn.HasKeyword(lathspell, Keyword.VALIANT));
		assertEquals(7, scn.GetStrength(lathspell));
		assertEquals(8, scn.GetStrength(tallproud));
		assertEquals(7, scn.GetStrength(guard));
	}

	@Test
	public void WithAValiantGandalfAndOneUnboundCompanionOfAnotherCultureNotValiantTheBonusIsPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var remember = scn.GetFreepsCard("remember");
		var lathspell = scn.GetFreepsCard("lathspell");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var tallproud = scn.GetFreepsCard("tallproud");
		var guard = scn.GetFreepsCard("guard1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(lathspell, tallproud, guard, aragorn);
		scn.AttachCardsTo(lathspell, shadowfax);
		scn.MoveCardsToHand(remember);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// Everybody unbound is valiant but Aragorn.
		assertTrue(scn.HasKeyword(lathspell, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(tallproud, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));

		scn.FreepsPlayCard(remember);
		assertEquals(1, scn.GetWoundsOn(lathspell));
		assertEquals(8, scn.GetStrength(lathspell));
		assertEquals(8, scn.GetStrength(tallproud));
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTheodenHimselfTheOneUnboundCompanionNotValiantTheBonusIsPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var remember = scn.GetFreepsCard("remember");
		var lathspell = scn.GetFreepsCard("lathspell");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(lathspell, theoden, guard);
		scn.AttachCardsTo(lathspell, shadowfax);
		scn.MoveCardsToHand(remember);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// Everybody unbound is valiant but Theoden, whom the event names: he is an unbound companion like
		// any other.
		assertTrue(scn.HasKeyword(lathspell, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertEquals(7, scn.GetStrength(lathspell));
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsPlayCard(remember);
		assertEquals(1, scn.GetWoundsOn(lathspell));
		assertEquals(8, scn.GetStrength(lathspell));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheodenAndAValiantCompanionWhoArriveAfterItIsPlayedAreNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var remember = scn.GetFreepsCard("remember");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, guard1, aragorn);
		scn.MoveCardsToHand(remember);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(remember);
		assertEquals(7, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.MoveCompanionsToTable(guard2, theoden);
		assertTrue(scn.HasKeyword(guard2, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(guard1));
	}
}
