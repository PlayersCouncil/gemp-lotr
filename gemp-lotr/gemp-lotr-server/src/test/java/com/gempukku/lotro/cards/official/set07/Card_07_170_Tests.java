package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("suzerain", "7_170");
					put("soldier", "4_254");    // Southron Soldier: another [raider] Southron Man; ambush (2) and nothing else
					// Gate Picket: [sauron] Orc.  "Maneuver: Exert this minion twice to make the Free Peoples player name
					// 3 cultures. Wound each companion and ally not of a named culture."
					put("picket", "6_101");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					// Stairs of Khazad-dum: a [dwarven] tale, cost 0, support area; played only to take the hand from 4 to 3
					put("stairs", "1_24");
					// Further cards for the Free Peoples hand, never played
					put("hand1", "1_24");
					put("hand2", "1_24");
					put("hand3", "1_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuzerainofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Suzerain of Harad
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.<br>While you have initiative, the Ring-bearer cannot take wounds during the archery phase and this minion is <b>ambush</b> (8).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("suzerain");

		assertEquals("Suzerain of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileTheShadowHasInitiativeTheRingBearerCannotTakeAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var suzerain = scn.GetShadowCard("suzerain");

		scn.StartGame();
		scn.MoveMinionsToTable(suzerain);

		// The Free Peoples hand is empty: the Shadow has initiative.
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());

		// The Suzerain's archery wound: Sam, Ring-bound and no Ring-bearer, and Aragorn may take it; Frodo may not.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void WhileTheFreePeoplesPlayerHasInitiativeTheRingBearerTakesAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("stairs"), scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));

		var suzerain = scn.GetShadowCard("suzerain");

		scn.StartGame();
		scn.MoveMinionsToTable(suzerain);

		// Four cards in the Free Peoples hand: the initiative is his.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void WhileTheShadowHasInitiativeTheRingBearerStillTakesAWoundInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var suzerain = scn.GetShadowCard("suzerain");
		var picket = scn.GetShadowCard("picket");

		scn.StartGame();
		scn.MoveMinionsToTable(suzerain, picket);

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Free Peoples player names no culture, so Gate Picket wounds each companion, Frodo among them.
		scn.FreepsPass();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsAmbush8OnlyWhileTheShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		assertInHand(stairs);

		var suzerain = scn.GetShadowCard("suzerain");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		// Laid in the fellowship phase only to read the standing text (Q-0228).
		scn.MoveMinionsToTable(suzerain, soldier);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.HasKeyword(suzerain, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(suzerain, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));

		// Three cards left in hand: the Shadow has initiative.
		scn.FreepsPlayCard(stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetKeywordCount(suzerain, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
