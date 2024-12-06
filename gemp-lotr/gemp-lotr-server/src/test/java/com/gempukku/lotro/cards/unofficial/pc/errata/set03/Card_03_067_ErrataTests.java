package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_067_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<String, String>()
				{{
					put("palantir", "53_67");
					put("uruk1", "1_151");
					put("uruk2", "1_151");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ThePalantirofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 3
		* Title: *The Palantir of Orthanc
		* Side: Free Peoples
		* Culture: Isengard
		* Twilight Cost: 0
		* Type: artifact
		* Subtype: Palantir
		* Game Text: To play, spot an [isengard] minion. Plays to your support area.
		* 	<b>Shadow:</b> Spot 2 [isengard] minions and remove (2) to reveal a card at random from the Free Peoples player's hand. Place that card on top of that player's draw deck.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl palantir = scn.GetFreepsCard("palantir");

		assertTrue(palantir.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, palantir.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, palantir.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, palantir.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(palantir, Keyword.SUPPORT_AREA));
		assertTrue(palantir.getBlueprint().getPossessionClasses().contains(PossessionClass.PALANTIR));
		assertEquals(0, palantir.getBlueprint().getTwilightCost());
	}

	@Test
	public void PalantirRequires1IsengardMinionToPlayBut2ToActivate() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		scn.FreepsMoveCardToHand("palantir", "uruk1", "uruk2");

		PhysicalCardImpl palantir = scn.GetShadowCard("palantir");
		PhysicalCardImpl uruk1 = scn.GetShadowCard("uruk1");
		PhysicalCardImpl uruk2 = scn.GetShadowCard("uruk2");
		scn.ShadowMoveCardToHand(palantir, uruk1, uruk2);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(23, scn.GetTwilight());

		assertFalse(scn.ShadowPlayAvailable(palantir));
		scn.ShadowPlayCard(uruk1);
		assertTrue(scn.ShadowPlayAvailable(palantir));
		scn.ShadowPlayCard(palantir);
		assertFalse(scn.ShadowActionAvailable(palantir));
		scn.ShadowPlayCard(uruk2);
		assertTrue(scn.ShadowActionAvailable(palantir));
	}

	@Test
	public void PalantirRemoves2ToRevealCardFromFreepsHandAndPlacesOnDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		scn.FreepsMoveCardToHand("palantir", "uruk1", "uruk2");

		PhysicalCardImpl palantir = scn.GetShadowCard("palantir");
		PhysicalCardImpl uruk1 = scn.GetShadowCard("uruk1");
		PhysicalCardImpl uruk2 = scn.GetShadowCard("uruk2");
		scn.ShadowMoveCharToTable(uruk1, uruk2);
		scn.ShadowMoveCardToSupportArea(palantir);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(23, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());

		scn.ShadowUseCardAction(palantir);
		assertEquals(21, scn.GetTwilight());
		assertEquals(1, scn.GetShadowCardChoiceCount());

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
	}
}
