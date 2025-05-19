package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_011_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ewer", "68_11");
					put("homestead", "13_22");
					put("defiance", "1_37");

					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielsSilverEwerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Galadriel's Silver Ewer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot Galadriel or Celeborn.
		 * While Galadriel is the Ring-bearer, she is strength +1 and resistance +2.
		 * Each time you play an [elven] skirmish event, you may exert Galadriel to reinforce an [elven] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ewer");

		assertEquals("Galadriel's Silver Ewer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EwerPumpsGaladrielARB() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		scn.MoveCardsToHand(ewer);

		scn.StartGame();

		//3 base +1 from ring
		assertEquals(4, scn.GetStrength(galadriel));
		//4 base +1 from text -1 from burden
		assertEquals(3, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(ewer);

		//3 base +1 from ring +1 from ewer
		assertEquals(5, scn.GetStrength(galadriel));
		//4 base +1 from text -1 from burden + 2 from ewer
		assertEquals(5, scn.GetResistance(galadriel));
	}
}
