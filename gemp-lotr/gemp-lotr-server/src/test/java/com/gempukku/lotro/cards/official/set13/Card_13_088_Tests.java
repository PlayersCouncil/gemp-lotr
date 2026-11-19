package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_088_Tests
{

	// Frodo, Son of Drogo 1_290 is the Ring-bearer: vitality 4.  The Shadow phase of turn 1 is at Ettenmoors, site 2,
	// where a site 4 minion is roaming: +2 to play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patriarch", "13_88");
					put("column", "11_72");     // Column of Easterlings: [men] Man minion, lurker; a skirmish ability used only on purpose
					put("fearsome", "11_79");   // Fearsome Dunlending: [men] Man minion, ambush; a play trigger that is not set off when cheated in
					put("soldier", "4_220");    // Desert Soldier: [raider] Man minion; a start-of-skirmish trigger only
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; "Maneuver: Exert Aragorn to make him defender +1"
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, vitality 4; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingPatriarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Dunlending Patriarch
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this, you may spot another [men] minion to make this minion twilight cost -1 for each wound on the Ring-bearer.<br>While each companion is wounded, this minion is <b>fierce</b>.
		 */

		var scn = GetScenario();

		var patriarch = scn.GetFreepsCard("patriarch");

		assertEquals("Dunlending Patriarch", patriarch.getBlueprint().getTitle());
		assertNull(patriarch.getBlueprint().getSubtitle());
		assertFalse(patriarch.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, patriarch.getBlueprint().getSide());
		assertEquals(Culture.MEN, patriarch.getBlueprint().getCulture());
		assertEquals(CardType.MINION, patriarch.getBlueprint().getCardType());
		assertEquals(Race.MAN, patriarch.getBlueprint().getRace());
		assertEquals(6, patriarch.getBlueprint().getTwilightCost());
		assertEquals(13, patriarch.getBlueprint().getStrength());
		assertEquals(3, patriarch.getBlueprint().getVitality());
		assertEquals(4, patriarch.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAnotherMenMinionItCostsOneLessForEachWoundOnTheRingBearerAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var patriarch = scn.GetShadowCard("patriarch");
		var column = scn.GetShadowCard("column");
		scn.MoveCardsToHand(patriarch);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		// Sam's wounds are no wounds on the Ring-bearer.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(column);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInHand(patriarch);
		scn.FreepsPass();

		// 6 to play - 2 for Frodo's wounds + 2 roaming = 6.  Counting Sam's wounds too it would be 3; with no
		// discount, 8.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(patriarch));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(patriarch));
		scn.ShadowPlayCard(patriarch);

		assertInZone(Zone.SHADOW_CHARACTERS, patriarch);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDiscountFollowsTheNumberOfWoundsAndAnotherMenMinionOfAnotherTitleIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var patriarch = scn.GetShadowCard("patriarch");
		var fearsome = scn.GetShadowCard("fearsome");
		scn.MoveCardsToHand(patriarch);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(fearsome);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInHand(patriarch);
		scn.FreepsPass();

		// 6 to play - 3 for Frodo's wounds + 2 roaming = 5.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(patriarch));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(patriarch));
		scn.ShadowPlayCard(patriarch);

		assertInZone(Zone.SHADOW_CHARACTERS, patriarch);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherMenMinionToSpotItCostsItsFullTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var patriarch = scn.GetShadowCard("patriarch");
		var soldier = scn.GetShadowCard("soldier");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToHand(patriarch);
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		// A Man minion of another culture, and a [men] card that is no minion.
		scn.MoveMinionsToTable(soldier);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, stragglers);
		assertInHand(patriarch);
		scn.FreepsPass();

		// 6 to play + 2 roaming = 8, with nothing taken off.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(patriarch));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(patriarch));
		scn.ShadowPlayCard(patriarch);

		assertInZone(Zone.SHADOW_CHARACTERS, patriarch);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsFierceWhileEachCompanionIsWoundedAndNotOtherwise() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var patriarch = scn.GetShadowCard("patriarch");
		var column = scn.GetShadowCard("column");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(patriarch, column);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// Aragorn is not wounded.
		assertFalse(scn.HasKeyword(patriarch, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(column, Keyword.FIERCE));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Every companion is wounded; the ally is not, and is no companion.  Only the Patriarch is fierce.
		assertTrue(scn.HasKeyword(patriarch, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(column, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotFierceWhileOnlyTheRingBearerIsUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var patriarch = scn.GetShadowCard("patriarch");

		scn.StartGame();
		scn.MoveMinionsToTable(patriarch);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(patriarch, Keyword.FIERCE));

		// Aragorn is wounded; the Ring-bearer, a companion too, is not.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(patriarch, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
