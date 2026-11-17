package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_350_Tests
{

	// The King path, whose site 6 is Minas Tirith Seventh Circle.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; strength +3 against a Nazgul only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, vitality 3; an archery ability used only on purpose
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinasTirithSeventhCircleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Minas Tirith Seventh Circle
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. If the fellowship moves from this site during the regroup phase, heal each [elven] companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Minas Tirith Seventh Circle", card.getBlueprint().getTitle());
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
	public void ARegroupMoveFromTheSiteHealsEachElvenCompanionOnceAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas, arwen, boromir);
		scn.MoveCardsToSupportArea(orophin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(frodo, 1);

		// The move TO the Seventh Circle, in the fellowship phase, heals nobody.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Minas Tirith Seventh Circle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Two [elven] companions, one wound each; not the [gondor] Man, the [elven] ally or Frodo.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheSiteInTheRegroupPhaseOrFromItInTheFellowshipPhaseHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddWoundsToChar(legolas, 2);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// The regroup move from site 5 TO the Seventh Circle.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(legolas));

		// The next turn begins at the Seventh Circle; its sanctuary heals are declined.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(legolas));

		// The move FROM the Seventh Circle, in the fellowship phase.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
