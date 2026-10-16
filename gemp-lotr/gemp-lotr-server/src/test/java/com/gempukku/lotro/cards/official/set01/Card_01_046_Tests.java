package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_046_Tests
{

	// Rivers at sites 2 and 3: Buckleberry Ferry and Ford of Bruinen; Bag End at site 1 is none.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boats", "1_46");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, vitality 3; an archery ability only
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, cost 2, vitality 2; has the text of site 3
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, who is no ally
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, who is no Elf
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_330");      // Buckleberry Ferry: river; no assignment or skirmish phases while only Hobbits are in the fellowship
					put("site3", "1_338");      // Ford of Bruinen: river, sanctuary; the first Nazgul played there each turn is -5
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GiftofBoatsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gift of Boats
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert an Elf ally. Plays to your support area.<br>When the fellowship moves from a river during the fellowship phase, the move limit for this turn is +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boats");

		assertEquals("Gift of Boats", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayAnElfAllyOfThePlayersChoiceExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boats = scn.GetFreepsCard("boats");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin, silinde, barliman);
		scn.MoveCardsToHand(boats);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.FreepsPlayAvailable(boats));

		// Two Elf allies of two titles; an Elf who is a companion, and an ally who is no Elf, are not offered.
		scn.FreepsPlayCard(boats);
		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInZone(Zone.SUPPORT, boats);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileNoElfAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boats = scn.GetFreepsCard("boats");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin, barliman);
		// Orophin, vitality 3, is exhausted; an Elf companion and a Man ally who could exert stand beside him.
		scn.AddWoundsToChar(orophin, 2);
		scn.MoveCardsToHand(boats, silinde);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(orophin));
		assertFalse(scn.FreepsPlayAvailable(boats));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(silinde);
		assertTrue(scn.FreepsPlayAvailable(boats));

		// Silinde is the only Elf ally who can exert.
		scn.FreepsPlayCard(boats);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(2, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInZone(Zone.SUPPORT, boats);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingFromARiverInTheFellowshipPhaseRaisesTheMoveLimitForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boats = scn.GetFreepsCard("boats");
		scn.MoveCardsToSupportArea(boats);

		scn.StartGame();
		scn.SkipToSite(2);

		assertInZone(Zone.SUPPORT, boats);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 2 -> 3, from Buckleberry Ferry
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Still in force in the Shadow phase after the second move.
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// A third move is offered and made.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 4 -> 5
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Gone with the turn: 2 on the other player's turn and on the next of the Free Peoples player's.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMoveFromASiteThatIsNoRiverOrARegroupMoveFromARiverLeavesTheMoveLimitAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boats = scn.GetFreepsCard("boats");
		scn.MoveCardsToSupportArea(boats);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, boats);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();          // move 1 -> 2, from Bag End, to a river
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3, from Buckleberry Ferry, in the regroup phase
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// Two moves of two: no third is offered, and the turn ends.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void MovingFromTheFordInTheFellowshipPhaseRaisesTheMoveLimitAsMovingFromTheFerryDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boats = scn.GetFreepsCard("boats");
		scn.MoveCardsToSupportArea(boats);

		scn.StartGame();
		scn.SkipToSite(3);

		// The Ford of Bruinen: a river like the Ferry, and a sanctuary besides.
		assertInZone(Zone.SUPPORT, boats);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 3 -> 4, from the Ford
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
