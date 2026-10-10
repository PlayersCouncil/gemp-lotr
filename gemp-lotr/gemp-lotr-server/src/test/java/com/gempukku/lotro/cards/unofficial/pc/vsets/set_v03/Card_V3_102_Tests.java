package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

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

public class Card_V3_102_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "103_102");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text at all
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its only text is for when it is played

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit, strength 3, vitality 4
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, home site 3, strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Mouth of Sauron, Emissary of Mordor
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Lurker.
		 * 	Each time a companion loses a skirmish, the Free Peoples player must add a threat or a burden.
		 * 	Skirmish: Exert this minion to make a wounded companion strength -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Emissary of Mordor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandForThreeAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(mouth);

		scn.StartGame();
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.HAND, mouth);
		assertTrue(scn.ShadowPlayAvailable(mouth));

		scn.ShadowPlayCard(mouth);

		assertInZone(Zone.SHADOW_CHARACTERS, mouth);
		// 11 - 3 to play - 2 roaming (a site 5 minion played at site 2)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachSkirmishACompanionLosesMakesTheFreePeoplesPlayerAddAThreatOrABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, troop);
		// The Troop made fierce, so that Aragorn loses to it twice in the turn. The Mouth fights nobody.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 8 against 13: Aragorn loses and is not overwhelmed.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		// It is the Free Peoples player who chooses, between the two and nothing else.
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChoose("Add a threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RingBoundCompanionOverwhelmedByTheMouthHimselfSetsItOffToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, mouth);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, sam);

		// 3 against 9: Sam is overwhelmed and killed, and has lost his skirmish all the same.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));

		scn.FreepsChoose("Add a burden");

		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CompanionWinningHisSkirmishAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 8 against 5: it is the Runner who loses.
		scn.FreepsPass();
		scn.ShadowPass();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AllyLosingASkirmishAtHisHomeSiteAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		// The fellowship moves to site 3, Elrond's home, where he may be assigned to a skirmish.
		scn.FreepsPass();
		scn.MoveMinionsToTable(mouth, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(elrond);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(elrond));

		// 8 against 13: Elrond loses, and he is an ally.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheBurdenIsAddedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, troop);
		// Two companions, Frodo and Aragorn, so a third threat cannot be added.
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheRingBearerHinderedTheThreatIsAddedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, troop);
		// No burden can be added while the Ring-bearer is hindered.
		scn.HinderCard(frodo);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsHindered(frodo));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNeitherAThreatNorABurdenPossibleNothingIsAskedOrAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, troop);
		// A hindered companion still counts toward the limit on threats: two companions, two threats.
		scn.AddThreats(2);
		scn.HinderCard(frodo);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPass();

		// The trigger does not happen at all: the skirmish's wound has nothing beside it to be put in order
		// with (on the other boards the Free Peoples player orders the two), and no choice of a threat or a
		// burden follows it.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsChoiceAvailable("Add a threat"));
		assertFalse(scn.FreepsChoiceAvailable("Add a burden"));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishActionExertsTheMouthToWeakenAWoundedCompanionForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, sam, guard);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, runner);
		// Wounded: Aragorn, Sam, Frodo and the ally Elrond. Not wounded: the Dwarf Guard.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(elrond, 1);
		scn.SkipToAssignments();
		// The Mouth is a lurker: his skirmish with Sam waits until Aragorn's is over.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{sam, mouth});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(sam));
		assertFalse(scn.IsCharSkirmishing(mouth));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));
		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(0, scn.GetWoundsOn(mouth));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);

		assertEquals(1, scn.GetWoundsOn(mouth));
		// A wounded companion, in the skirmish or out of it; the Mouth has just become a wounded minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, elrond, mouth));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(sam);

		assertEquals(2, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 5: Aragorn wins. Sam is weaker until this skirmish is over, and no longer.
		scn.FreepsPass();
		assertEquals(2, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishing(sam));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionAddsUpAndIsNotOfferedOnceTheMouthIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, runner);
		// Aragorn is the only wounded companion, so he is chosen without a question.
		scn.AddWoundsToChar(aragorn, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(mouth));

		scn.FreepsPass();
		scn.ShadowUseCardAction(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(7, scn.GetStrength(aragorn));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		assertEquals(2, scn.GetWoundsOn(mouth));
		assertEquals(6, scn.GetStrength(aragorn));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mouth));
	}

	@Test
	public void SkirmishActionWithNoWoundedCompanionIsStillOfferedAndOnlyExertsTheMouth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(0, scn.GetWoundsOn(mouth));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);

		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
