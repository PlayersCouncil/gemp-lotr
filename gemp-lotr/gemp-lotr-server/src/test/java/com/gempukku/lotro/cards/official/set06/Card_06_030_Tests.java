package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "6_30");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the other companion, whom no move wounds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gandalf, Mithrandir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 10
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Damage +1</b>.<br>Each time the fellowship moves, wound Gandalf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Mithrandir", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void EachMoveOfTheTurnWoundsGandalfAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fellowship phase's move, to site 2.
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move of the same turn, to site 3: a second wound.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoWoundWhenTheFellowshipStaysAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		// The turn ends at site 2 with no second move, and Gandalf has the one wound of the one move.
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnExhaustedGandalfIsKilledByTheWoundOfTheMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();

		// Vitality 4 with 3 wounds: exhausted, so he could not exert. The move wounds him all the same.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.FreepsPass();

		assertInZone(Zone.DEAD, gandalf);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
