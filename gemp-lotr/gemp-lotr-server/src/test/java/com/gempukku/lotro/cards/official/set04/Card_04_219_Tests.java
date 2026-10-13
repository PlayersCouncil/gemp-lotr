package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_219_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lord", "4_219");

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer; Archer is its whole text
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: an ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertLordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Desert Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.<br><b>Archery:</b> Exert Desert Lord to exert a companion (except the Ring-bearer); Desert Lord does not add to the minion archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lord");

		assertEquals("Desert Lord", card.getBlueprint().getTitle());
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
	public void ArcheryAbilityExertsHimToExertACompanionAndHeDoesNotAddToTheArcheryTotalThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetShadowCard("lord");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(lord, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Two archers: Desert Lord and the Marksman.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(lord));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lord));

		scn.ShadowUseCardAction(lord);
		assertEquals(1, scn.GetWoundsOn(lord));
		// Companions of two cultures, one of them Ring-bound; not the Ring-bearer, nor an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, celeborn, marksman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		// He no longer adds to it; the Marksman still does.
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.PassArcheryActions();
		// One archery wound to place, and only one.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The effect lasted the archery phase: in the assignment phase he is counted again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileDesertLordIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetShadowCard("lord");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable("sam", "aragorn");

		scn.StartGame();
		scn.MoveMinionsToTable(lord, marksman);
		// Vitality 3 with 2 wounds: exhausted, with two companions he could exert.
		scn.AddWoundsToChar(lord, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(lord));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lord));

		scn.RemoveWoundsFromChar(lord, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(lord));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
