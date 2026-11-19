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

public class Card_04_161_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fall", "4_161");
					// Gríma, Wormtongue: an [isengard] Man minion, vitality 3; a maneuver ability never used.
					put("grima", "4_154");
					// Rohirrim Traitor: an [isengard] Man minion, strength 6; +2 while a companion bears an [isengard]
					// condition, which none does here.
					put("traitor", "6_72");
					// Saruman, Servant of the Eye: the [isengard] Saruman, a Wizard minion; may not be assigned to a
					// skirmish, and an assignment ability never used.
					put("saruman", "3_69");
					// Saruman, Rabble-rouser: the [dunland] Saruman, a Wizard minion; may not be assigned to a
					// skirmish; his move trigger has nothing to count, being laid after the move.
					put("rabblerouser", "4_33");
					// Saruman, Servant of Sauron: the [isengard] follower Saruman, no character; a skirmish-win
					// trigger only.
					put("follower", "17_38");
					// Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Dunlending Footmen: a [dunland] Man, strength 11, vitality 2; a cost modifier in skirmishes only.
					put("footmen", "6_3");
					// Uruk Chaser: an [isengard] Uruk-hai, strength 9, vitality 2, fierce; +2 in a fierce skirmish.
					put("chaser", "4_181");
					// Aragorn, Ranger of the North: an unbound [gondor] Man, strength 8; a maneuver ability never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: an unbound [dwarven] Dwarf, strength 6; a skirmish ability never used.
					put("gimli", "1_13");
					// Merry, From O'er the Brandywine: an unbound [shire] Hobbit, strength 3; +2 only while he bears
					// a weapon.
					put("merry", "1_303");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer, strength 3; a fellowship ability and
					// a response to Frodo's death.
					put("sam", "1_311");
					// Bounder: a Hobbit ally whose home is site 2, strength 2; a skirmish ability never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MenWillFallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Men Will Fall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot Saruman or an [isengard] Man to make an unbound companion strength -2.
		*/

		var scn = GetScenario();

		var fall = scn.GetFreepsCard("fall");

		assertEquals("Men Will Fall", fall.getBlueprint().getTitle());
		assertNull(fall.getBlueprint().getSubtitle());
		assertFalse(fall.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fall.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, fall.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fall.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fall, Timeword.SKIRMISH));
		assertEquals(1, fall.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGrimaSpottedMakesAnUnboundCompanionInAnotherSkirmishStrengthMinus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var grima = scn.GetShadowCard("grima");
		var savage = scn.GetShadowCard("savage");
		var chaser = scn.GetShadowCard("chaser");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, merry, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima, savage, chaser);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, chaser});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fall));
		scn.ShadowPlayCard(fall);

		// Unbound companions of three cultures and races, in this skirmish, in one still to come and in none;
		// not Sam (Ring-bound), not Frodo, not the ally, not a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo, bounder, savage, grima));
		assertEquals(6, scn.GetStrength(gimli));
		scn.ShadowChooseCard(gimli);
		assertEquals(4, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertInZone(Zone.DISCARD, fall);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still -2 at the end of this skirmish phase; gone once Gimli's own skirmish, the next phase, begins.
		scn.FreepsPass();
		assertEquals(4, scn.GetStrength(gimli));
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheRohirrimTraitorSpottedMakesTheCompanionStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var traitor = scn.GetShadowCard("traitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		scn.MoveMinionsToTable(traitor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, traitor);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fall));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowPlayCard(fall);
		// Aragorn is the one unbound companion: he is chosen unasked.
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheIsengardSarumanSpottedMakesTheCompanionStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fall));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowPlayCard(fall);
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheDunlandSarumanSpottedMakesTheCompanionStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var rabblerouser = scn.GetShadowCard("rabblerouser");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		scn.MoveMinionsToTable(rabblerouser, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fall));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowPlayCard(fall);
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheFollowerSarumanSpottedMakesTheCompanionStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var follower = scn.GetShadowCard("follower");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(follower);
		assertInZone(Zone.SUPPORT, follower);
		// His Aid is offered as the maneuver phase starts: declined, he stays in the support area.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToAssignments();
		assertInZone(Zone.SUPPORT, follower);
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fall));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowPlayCard(fall);
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSarumanAndNoIsengardMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		// An [isengard] Uruk-hai who is no Man, and a Man of [dunland].
		scn.MoveMinionsToTable(savage, footmen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, footmen});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fall));
	}

	@Test
	public void WithNoUnboundCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fall = scn.GetShadowCard("fall");
		var grima = scn.GetShadowCard("grima");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(fall);

		scn.StartGame();
		assertInZone(Zone.HAND, fall);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, grima);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		assertEquals(3, scn.GetStrength(sam));
		// Frodo 3, +1 for the Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.ShadowPlayAvailable(fall));
		scn.ShadowPlayCard(fall);
		assertInZone(Zone.DISCARD, fall);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
