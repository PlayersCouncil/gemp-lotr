package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_036_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("feet", "51_36");
					put("arwen", "1_30");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("chaff1", "1_3");
					put("chaff2", "1_3");
					put("chaff3", "1_3");
					put("chaff4", "1_3");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void CurseTheirFoulFeetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Curse Their Foul Feet!
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 2
		* Type: event
		* Subtype: Fellowship
		* Game Text: Exert an Elf to reveal 4 cards at random from an opponent's hand.  Make the fellowship archery total +X until the regroup phase, where X is the number of Orcs revealed.  That player may discard X cards from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");

		assertFalse(feet.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, feet.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, feet.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, feet.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(feet, Timeword.FELLOWSHIP));
		assertEquals(2, feet.getBlueprint().getTwilightCost());
	}

	@Test
	public void FoulFeetExertsAnElfToReveal4CardsFromShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCardToHand(feet, arwen);

		scn.ShadowMoveCardToHand("runner1","runner2","runner3","runner4",
				"chaff1","chaff2","chaff3","chaff4" );

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(feet));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(feet));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(4, scn.GetFreepsCardChoiceCount());
	}

	@Test
	public void FoulFeetRevealing4CardsResultsInPlus4Archery() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCardToHand(feet);
		scn.FreepsMoveCharToTable(arwen);

		scn.ShadowMoveCardToHand("runner1","runner2","runner3","runner4" );

		scn.StartGame();

		scn.FreepsPlayCard(feet);

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		scn.ShadowChooseNo();

		assertEquals(4, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void FoulFeetRevealing0CardsResultsInPlus0Archery() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCardToHand(feet);
		scn.FreepsMoveCharToTable(arwen);

		scn.ShadowMoveCardToHand("chaff1","chaff2","chaff3","chaff4" );

		scn.StartGame();

		scn.FreepsPlayCard(feet);

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void FoulFeetLetsShadowDiscardXCardsToPreventArchery() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCardToHand(feet);
		scn.FreepsMoveCharToTable(arwen);

		scn.ShadowMoveCardToHand("runner1","runner2","runner3","runner4");

		scn.StartGame();

		scn.FreepsPlayCard(feet);

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());

		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
