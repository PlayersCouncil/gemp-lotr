package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "15_117");
					put("desolation", "15_107");  // Desolation Orc: an [orc] Orc whose only text is an extra cost to play (cheated in)
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability never used
					put("boromir", "1_97");       // Boromir, Son of Denethor: a [gondor] Man, vitality 3; a skirmish ability never used
					put("gimli", "1_13");         // Gimli, Son of Glóin: a [dwarven] Dwarf; a skirmish ability never used
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard; his text is about his strength
					put("legolas", "1_50");       // Legolas, Greenleaf: an [elven] Elf; an archer, so laid only where no archery phase comes
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("thrarin", "1_27");       // Thrarin, Dwarven Smith: an ally; a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Tower Troll
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: For each companion over 5, this minion is <b>damage +1</b>.<br><b>Assignment:</b> Exert this minion twice to assign it to a companion (except the Ring-bearer). If you cannot spot 6 companions, the Free Peoples player may exert a companion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Tower Troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus1ForEachCompanionOverFiveAndNoAllyCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		var legolas = scn.GetFreepsCard("legolas");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(legolas, gandalf);

		var troll = scn.GetShadowCard("troll");
		var desolation = scn.GetShadowCard("desolation");

		scn.StartGame();
		// Laid in the fellowship phase to read the count as companions are played (Q-0228).
		scn.MoveMinionsToTable(troll, desolation);
		scn.FreepsRefreshDecision();

		// Five companions (Frodo, Aragorn, Boromir, Gimli, Sam) and an ally: none over 5.
		assertEquals(0, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(desolation, Keyword.DAMAGE));

		scn.FreepsPlayCard(legolas);
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(desolation, Keyword.DAMAGE));

		scn.FreepsPlayCard(gandalf);
		assertEquals(2, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(desolation, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSixCompanionsItExertsTwiceToAssignItselfToAChosenCompanionAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, gandalf, sam);
		scn.MoveAlliesToTable(thrarin);

		var troll = scn.GetShadowCard("troll");
		var desolation = scn.GetShadowCard("desolation");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, desolation);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(desolation));
		assertTrue(scn.ShadowActionAvailable(troll));

		scn.ShadowUseCardAction(troll);
		// Both exertions are paid by the Troll alone.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(desolation));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn, boromir, gimli, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, thrarin, troll, desolation));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		// Six companions can be spotted: the Free Peoples player is offered no prevention.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troll));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(desolation));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithFiveCompanionsTheFreePeoplesPlayerMayExertACompanionToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);
		scn.MoveAlliesToTable(thrarin);

		var troll = scn.GetShadowCard("troll");
		var desolation = scn.GetShadowCard("desolation");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, desolation);
		// Vitality 3 with 2 wounds: Boromir cannot exert.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(troll));

		scn.ShadowUseCardAction(troll);
		assertEquals(2, scn.GetWoundsOn(troll));
		// An exhausted companion may still be assigned; the Ring-bearer and the ally may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn, boromir, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, thrarin, troll, desolation));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		// Five companions and an ally: six companions cannot be spotted.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, thrarin, troll, desolation));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Prevented: nobody is assigned, and the Troll's exertions stay paid.
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(troll));
		assertEquals(2, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithFiveCompanionsTheFreePeoplesPlayerMayDeclineAndTheTrollIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(troll);
		scn.ShadowChooseCard(boromir);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(boromir, troll));
		assertEquals(2, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedThePreventionIsNotAskedAndTheTrollIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		// Vitality 4 with 3 wounds each: neither companion can exert.  Thrarin, an ally, could, but is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(troll);
		// Aragorn is the only companion who may be assigned, and is chosen unasked.
		assertEquals(2, scn.GetWoundsOn(troll));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troll));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerAndAnAllyItsExertionsArePaidForNothingAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowActionAvailable(troll));

		scn.ShadowUseCardAction(troll);
		assertEquals(2, scn.GetWoundsOn(troll));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertFalse(scn.IsCharAssigned(troll));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(thrarin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhenTheTrollCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		// Vitality 3 with 1 wound: it can exert once, not twice.
		scn.AddWoundsToChar(troll, 1);
		assertEquals(1, scn.GetWoundsOn(troll));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
		assertEquals(1, scn.GetWoundsOn(troll));
	}
}
