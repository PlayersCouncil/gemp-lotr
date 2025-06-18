package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_080_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ottar", "51_80");
					put("gandalf", "1_72");
					put("chaff1", "51_80");
					put("chaff2", "51_80");
					put("chaff3", "51_80");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OttarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: *Ottar, Man of Laketown
		* Side: Free Peoples
		* Culture: Gandalf
		* Twilight Cost: 1
		* Type: Ally
		* Subtype: Man
		* Strength: 2
		* Vitality: 4
		* Site Number: 3
		* Game Text: To play, spot Gandalf.
		* 	<b>Fellowship:</b> Exert Ottar and discard a card from hand to draw a card.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl ottar = scn.GetFreepsCard("ottar");

		assertTrue(ottar.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ottar.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, ottar.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, ottar.getBlueprint().getCardType());
		assertEquals(Race.MAN, ottar.getBlueprint().getRace());
		assertEquals(1, ottar.getBlueprint().getTwilightCost());
		assertEquals(2, ottar.getBlueprint().getStrength());
		assertEquals(4, ottar.getBlueprint().getVitality());
		assertTrue(ottar.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void OttarRequiresGandalfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl ottar = scn.GetFreepsCard("ottar");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(ottar, gandalf);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(ottar));
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(ottar));
	}

	@Test
	public void OttarExertsAndDiscardsACardFromHandToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl ottar = scn.GetFreepsCard("ottar");
		PhysicalCardImpl chaff1 = scn.GetFreepsCard("chaff1");
		PhysicalCardImpl chaff2 = scn.GetFreepsCard("chaff2");
		scn.MoveCardsToHand(chaff1);
		scn.MoveCompanionsToTable(ottar);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(chaff2);

		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(Zone.HAND, chaff1.getZone());
		assertEquals(Zone.DECK, chaff2.getZone());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(ottar));

		scn.FreepsUseCardAction(ottar);

		assertEquals(1, scn.GetWoundsOn(ottar));
		assertEquals(Zone.DISCARD, chaff1.getZone());
		assertEquals(Zone.HAND, chaff2.getZone());
		assertEquals(1, scn.GetFreepsHandCount()); // Discarded 1, drew 1
	}

	@Test
	public void OttarRequiresCardInHandToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl ottar = scn.GetFreepsCard("ottar");
		PhysicalCardImpl chaff1 = scn.GetFreepsCard("chaff1");
		PhysicalCardImpl chaff2 = scn.GetFreepsCard("chaff2");
		scn.MoveCompanionsToTable(ottar);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(ottar));
	}
}
