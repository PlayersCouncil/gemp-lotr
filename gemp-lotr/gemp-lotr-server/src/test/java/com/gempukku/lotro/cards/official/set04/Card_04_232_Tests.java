package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_232_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer1", "4_232");
					put("archer2", "4_232");   // a second copy: not unique

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3; a skirmish ability only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EliteArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Elite Archer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.<br><b>Archery:</b> Exert this minion to exert a companion (except the Ring-bearer); this minion does not add to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer1");

		assertEquals("Elite Archer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ArcheryAbilityExertsThisMinionToExertACompanionAndItAloneDropsOutOfTheArcheryTotalThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer1 = scn.GetShadowCard("archer1");
		var archer2 = scn.GetShadowCard("archer2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(archer1, archer2);
		// Vitality 3 with 2 wounds: Gimli is exhausted and cannot exert.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Two archers: both copies.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(archer1));
		assertEquals(0, scn.GetWoundsOn(archer2));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(archer1));

		scn.ShadowUseCardAction(archer1);
		// The copy used exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(archer1));
		assertEquals(0, scn.GetWoundsOn(archer2));
		// Companions of two cultures, one of them Ring-bound; not the Ring-bearer, an ally, an exhausted companion
		// or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, celeborn, gimli, archer2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		// The copy used no longer adds to it; the other copy, an archer of the same title, still does.
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.PassArcheryActions();
		// One archery wound to place, and only one.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The effect lasted the archery phase: in the assignment phase the copy used is counted again.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetShadowArcheryTotal());
	}

	@Test
	public void WithOnlyTheRingBearerAndAnExhaustedCompanionTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer1 = scn.GetShadowCard("archer1");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(archer1);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(archer1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowActionAvailable(archer1));

		scn.ShadowUseCardAction(archer1);
		// The cost is paid; nobody can be exerted, and nobody is asked for.
		assertEquals(1, scn.GetWoundsOn(archer1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer1 = scn.GetShadowCard("archer1");
		var archer2 = scn.GetShadowCard("archer2");
		scn.MoveCompanionsToTable("sam", "aragorn");

		scn.StartGame();
		scn.MoveMinionsToTable(archer1, archer2);
		// Vitality 2 with 1 wound: exhausted, with two companions it could exert.
		scn.AddWoundsToChar(archer1, 1);
		assertEquals(1, scn.GetWoundsOn(archer1));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(archer1));
		assertEquals(0, scn.GetWoundsOn(archer2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(archer1));
		// The other copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(archer2));

		scn.RemoveWoundsFromChar(archer1, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(archer1));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
