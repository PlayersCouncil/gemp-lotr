package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palantir", "9_38");
					put("boromir", "3_122");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeeingStoneofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Seeing Stone of Orthanc
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot a [gondor] Man with 3 or more vitality (or spot a [gondor] Man and add 2 threats).
		 * <b>Regroup:</b> Add a threat or discard this artifact to remove (2) or to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("palantir");

		assertEquals("Seeing Stone of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SeeingStoneofOrthancAddsThreatsIfNoGondorManOfVit3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(palantir, boromir);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(palantir));

		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsPlayAvailable(palantir));

		scn.AddWoundsToChar(boromir, 1);
		assertEquals(2, scn.GetVitality(boromir));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(palantir));

		scn.FreepsPlayCard(palantir);
		assertEquals(2, scn.GetThreats());
	}

	@Test
	public void SeeingStoneofOrthancOffersChoiceOf2ThreatsOrSpotIfCanSpotGondorManOf3Vit() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(palantir, boromir);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(palantir));

		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsPlayAvailable(palantir));

		assertEquals(3, scn.GetVitality(boromir));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(palantir));

		scn.FreepsPlayCard(palantir);
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		scn.FreepsChooseOption("3 or more vitality");

		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.SUPPORT, palantir.getZone());
	}
}
