package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_346_Tests
{

	// The King path, with Minas Tirith Fifth Circle as its site 6.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4; a fellowship ability used only on purpose
					// Gandalf's Staff: vitality +1 on Gandalf, so that he lives with 4 wounds; a spell discount and a
					// skirmish ability, neither used here
					put("staff", "2_22");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
				}},
				new HashMap<>(VirtualTableScenario.KingSites)
				{{
					put("site6", "7_346");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinasTirithFifthCircleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Minas Tirith Fifth Circle
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. If the fellowship moves from this site during the regroup phase, heal Gandalf three times.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Minas Tirith Fifth Circle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void ARegroupMoveFromTheSiteHealsGandalfThreeTimesAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var avenger = scn.GetFreepsCard("avenger");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(gandalf, avenger, knight);
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.SkipToSite(5);
		// Vitality 4 + 1 for the Staff: alive with 4 wounds.
		scn.AddWoundsToChar(gandalf, 4);
		scn.AddWoundsToChar(avenger, 2);
		scn.AddWoundsToChar(knight, 1);
		scn.AddWoundsToChar(frodo, 1);

		// The move TO the Fifth Circle, in the fellowship phase.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Minas Tirith Fifth Circle", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Three heals: 4 to 1, where two would leave 2 and four would leave none.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheSiteInTheRegroupPhaseOrFromItInTheFellowshipPhaseHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddWoundsToChar(gandalf, 4);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// The regroup move from site 5 TO the Fifth Circle.
		scn.SkipToMovementDecision();
		assertEquals(4, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetWoundsOn(gandalf));

		// The next turn begins at the Fifth Circle; its sanctuary heals are declined.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetWoundsOn(gandalf));

		// The move FROM the Fifth Circle, in the fellowship phase.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
