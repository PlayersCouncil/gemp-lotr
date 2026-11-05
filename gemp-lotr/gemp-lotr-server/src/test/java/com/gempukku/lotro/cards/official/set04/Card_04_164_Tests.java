package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("champion", "4_164");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text

					// Aragorn, Ranger of the North: an unbound [gondor] Man, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: an unbound [dwarven] Dwarf, vitality 3; a skirmish ability never used.
					put("gimli", "1_13");
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
	public void OrthancChampionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Orthanc Champion
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Assignment:</b> Exert Orthanc Champion to assign it to an unbound companion. That companion may exert to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("champion");

		assertEquals("Orthanc Champion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilityExertsTheChampionToAssignItToAnUnboundCompanionWhoMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var champion = scn.GetShadowCard("champion");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(champion, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Site 2 is the Bounder's home, so he could be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(bounder));
		assertEquals(0, scn.GetWoundsOn(champion));
		assertTrue(scn.ShadowActionAvailable(champion));

		scn.ShadowUseCardAction(champion);
		assertEquals(1, scn.GetWoundsOn(champion));
		// Two unbound companions of two cultures and races.  Not Sam (Ring-bound), not Frodo, not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo, bounder));
		scn.ShadowChooseCard(aragorn);

		// Declined: the Champion itself is assigned to Aragorn, who does not exert; the Troop is not assigned.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, champion));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ThatCompanionMayExertToPreventTheAssignmentAndTheChampionsExertionStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var champion = scn.GetShadowCard("champion");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(champion);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(champion);
		assertEquals(1, scn.GetWoundsOn(champion));
		scn.ShadowChooseCard(gimli);

		// Gimli exerts to prevent it; Aragorn, the other unbound companion, is not exerted.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(champion));
		assertEquals(1, scn.GetWoundsOn(champion));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhileTheChampionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var champion = scn.GetShadowCard("champion");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(champion);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(champion, 1);
		assertEquals(1, scn.GetWoundsOn(champion));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.CanBeAssignedViaAction(aragorn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(champion));
	}

	@Test
	public void AnExhaustedCompanionChosenIsNotAskedAndIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var champion = scn.GetShadowCard("champion");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(champion);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(champion);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		scn.ShadowChooseCard(aragorn);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, champion));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheAbilityExertsTheChampionForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var champion = scn.GetShadowCard("champion");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(champion);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The ability has a cost that can be paid, so it is offered with nobody to assign the Champion to.
		assertEquals(0, scn.GetWoundsOn(champion));
		assertTrue(scn.ShadowActionAvailable(champion));
		scn.ShadowUseCardAction(champion);

		assertEquals(1, scn.GetWoundsOn(champion));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertFalse(scn.IsCharAssigned(champion));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
