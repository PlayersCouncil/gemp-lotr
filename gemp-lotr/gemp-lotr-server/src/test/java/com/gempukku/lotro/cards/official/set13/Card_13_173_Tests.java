package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reserve", "13_173");
					put("uruk1", "11_204");     // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; damage +1 and toil only
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard, vitality 4; his skirmish ability is never used
					put("saruman", "17_116");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, vitality 2, damage +1 only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his maneuver ability is never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: needed for Long I Fell
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer and nothing else here
					// Long I Fell: Skirmish event; the Shadow player may make the Free Peoples player wound any minion
					put("fell", "4_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukReserveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Reserve
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper>
		 * 	Each time your [uruk-hai] minion is about to be killed in a skirmish, you may place that minion on the bottom
		 * 	of your draw deck instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reserve");

		assertEquals("Uruk Reserve", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachUrukHaiMinionAboutToBeKilledInItsSkirmishMayGoToTheBottomOfTheDrawDeckInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var reserve = scn.GetShadowCard("reserve");
		var uruk1 = scn.GetShadowCard("uruk1");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(reserve, uruk1, saruman);
		// The Tyrannical Uruk has one vitality left; Saruman none wounded.
		scn.AddWoundsToChar(uruk1, 2);
		assertEquals(2, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(saruman));
		// Strengths that only arrange the board: Aragorn 18 beats the Uruk's 14 without overwhelming it, and
		// Gandalf 47 overwhelms Saruman's 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 40));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk1},
				new PhysicalCardImpl[]{gandalf, saruman}
		);
		scn.ShadowDeclineAssignments();

		// The wound for losing would kill the Uruk.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reserve));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DECK, uruk1);
		assertEquals(uruk1, scn.GetShadowBottomOfDeck());
		assertNotInZone(Zone.DISCARD, uruk1);

		// Saruman, an [uruk-hai] minion who is no Uruk-hai, is overwhelmed in the next skirmish: answered again.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reserve));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(saruman, scn.GetShadowBottomOfDeck());
		assertEquals(uruk1, scn.GetFromBottomOfShadowDeck(2));
		assertNotInZone(Zone.DISCARD, saruman);
		assertInPlay(reserve);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnswersItsOwnKillAndNotASauronUrukHaisAndDeclinedTheMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var reserve = scn.GetShadowCard("reserve");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(reserve, guard);
		// Strengths that only arrange the board: each companion overwhelms his minion.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 40));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 40));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, guard},
				new PhysicalCardImpl[]{gandalf, reserve}
		);
		scn.ShadowDeclineAssignments();

		// The Guard is a [sauron] Uruk-hai: killed, and nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(reserve));
		assertInDiscard(guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The Reserve, a lurker, is fought last: its own kill is answered, and declined it is killed.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reserve));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(reserve);
		assertNotInZone(Zone.DECK, reserve);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionKilledInTheSkirmishPhaseOutsideItsOwnSkirmishIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(fell);

		var reserve = scn.GetShadowCard("reserve");
		var uruk1 = scn.GetShadowCard("uruk1");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		assertInHand(fell);
		scn.MoveMinionsToTable(reserve, uruk1, guard);
		scn.AddWoundsToChar(uruk1, 2);
		assertEquals(2, scn.GetWoundsOn(uruk1));

		scn.SkipToAssignments();
		// The Uruk waits for its skirmish with Aragorn while Gandalf fights the Guard.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk1},
				new PhysicalCardImpl[]{gandalf, guard}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		scn.FreepsPlayCard(fell);
		// The Shadow player makes the Free Peoples player wound a minion, and he wounds the waiting Uruk.
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(uruk1);

		// "In a skirmish" is any kill while a skirmish phase is going on (Ketura, Q-0358: B).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reserve));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(uruk1, scn.GetShadowBottomOfDeck());
		assertNotInZone(Zone.DISCARD, uruk1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionKilledByAnArcheryWoundIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var reserve = scn.GetShadowCard("reserve");
		var uruk1 = scn.GetShadowCard("uruk1");

		scn.StartGame();
		scn.MoveMinionsToTable(reserve, uruk1);
		scn.AddWoundsToChar(uruk1, 2);
		assertEquals(2, scn.GetWoundsOn(uruk1));

		scn.SkipToArcheryWounds();
		// Legolas's archery wound, placed by the Shadow player, kills the Uruk in the archery phase.
		scn.ShadowChooseCard(uruk1);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(reserve));
		assertInDiscard(uruk1);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoUrukHaiMinionsAboutToBeKilledAtOnceMayBothGoToTheBottomOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var reserve = scn.GetShadowCard("reserve");
		var uruk1 = scn.GetShadowCard("uruk1");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(reserve, uruk1, saruman);
		// A strength that only arranges the board: Aragorn 48 overwhelms the Uruk and Saruman together (22).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 40));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.ShadowAssignToMinions(aragorn, saruman);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// "Each time your [uruk-hai] minion": each of the two may be saved.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reserve));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, saruman));
		assertTrue(scn.ShadowGetChoiceMax() >= 2);
		scn.ShadowChooseCards(uruk1, saruman);
		// Cards placed beneath a draw deck are asked for one at a time: the Uruk first, then Saruman beneath it.
		scn.ShadowChooseCard(uruk1);

		assertEquals(saruman, scn.GetShadowBottomOfDeck());
		assertEquals(uruk1, scn.GetFromBottomOfShadowDeck(2));
		assertNotInZone(Zone.DISCARD, uruk1);
		assertNotInZone(Zone.DISCARD, saruman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
