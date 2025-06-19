package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ewer", "18_11");
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
		 * While Galadriel is the Ring-bearer, she is strength +2 and resistance +2.
		 * Each time you play an [elven] skirmish event, you may reinforce an [elven] token.
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
		//3 base +1 from text -1 from burden
		assertEquals(3, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(ewer);

		//3 base +1 from ring +2 from ewer
		assertEquals(6, scn.GetStrength(galadriel));
		//3 base +1 from text -1 from burden + 2 from ewer
		assertEquals(5, scn.GetResistance(galadriel));
	}

	@Test
	public void EwerReinforcesAfterPlayingSkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var homestead = scn.GetFreepsCard("homestead");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCardsToSupportArea(ewer, homestead);
		scn.MoveCardsToHand(defiance);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.AddTokensToCard(homestead, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(galadriel, savage);
		scn.FreepsResolveSkirmish(galadriel);

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(homestead));
	}
}
