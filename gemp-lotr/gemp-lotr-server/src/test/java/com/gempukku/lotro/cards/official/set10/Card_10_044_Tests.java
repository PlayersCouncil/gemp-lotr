package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vantage", "10_44");

					put("explorer", "4_250");   // Southron Explorer: a Southron Man, vitality 3; a regroup ability only
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron (fierce only at 2 burdens)
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a Southron minion who is no Man

					put("defend1", "4_115");    // Defend It and Hope: [gondor] Maneuver event, exert a [gondor] Man to wound a minion
					put("defend2", "4_115");
					put("defend3", "4_115");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4, to exert for Defend It and Hope
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man, vitality 3; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer, for an archery wound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HighVantageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: High Vantage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Until the end of the assignment phase, each time a Southron is about to take a wound, prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vantage");

		assertEquals("High Vantage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachWoundASouthronIsAboutToTakeIsPreventedAndAWoundOnAnotherMinionIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vantage = scn.GetShadowCard("vantage");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(vantage);

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var defend3 = scn.GetFreepsCard("defend3");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(defend1, defend2, defend3);
		assertInHand(vantage);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertInHand(vantage);
		assertTrue(scn.ShadowPlayAvailable(vantage));
		scn.ShadowPlayCard(vantage);
		assertInDiscard(vantage);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// The Axeman, a [raider] Man who is no Southron, takes his wound.
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(explorer, axeman));
		scn.FreepsChooseCard(axeman);
		assertEquals(1, scn.GetWoundsOn(axeman));

		scn.ShadowPass();
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(explorer);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(explorer));

		// A second wound on the same Southron is prevented as well.
		scn.ShadowPass();
		scn.FreepsPlayCard(defend3);
		scn.FreepsChooseCard(boromir);
		scn.FreepsChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASouthronWhoIsNoManIsProtectedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vantage = scn.GetShadowCard("vantage");
		var rearguard = scn.GetShadowCard("rearguard");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(vantage);
		scn.MoveCardsToSupportArea(rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		scn.StartGame();
		// The Axeman, no Southron, is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.ShadowPlayCard(vantage);
		assertInDiscard(vantage);
		scn.ShadowPass();

		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		// A Southron minion now, and still no Man.
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		assertEquals(0, scn.GetWoundsOn(rearguard));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(rearguard);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rearguard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASouthronPlayedAfterHighVantageIsProtectedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vantage = scn.GetShadowCard("vantage");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(vantage, explorer);

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);
		assertInHand(explorer);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		// 1 for High Vantage, then 2 for the Explorer and 2 for his roaming
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.ShadowPlayCard(vantage);
		assertInDiscard(vantage);
		// The Explorer comes into play only after the event was played.
		assertInHand(explorer);
		scn.ShadowPlayCard(explorer);
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(explorer, axeman));
		scn.FreepsChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThePreventionHoldsInTheArcheryPhaseAndIsGoneInTheSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vantage = scn.GetShadowCard("vantage");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(vantage);

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.ShadowPlayCard(vantage);
		assertInDiscard(vantage);

		// Legolas makes the fellowship archery total 1, and the lone minion takes that wound: prevented.
		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(explorer));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(legolas, explorer);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		// Legolas 6 against the Explorer's 5: the Explorer loses, and his wound is placed.
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vantage = scn.GetShadowCard("vantage");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(vantage);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		assertInHand(vantage);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInHand(vantage);
		assertFalse(scn.ShadowPlayAvailable(vantage));
	}
}
