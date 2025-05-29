package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palantir", "3_67");
					put("uruk1", "1_151");
					put("uruk2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePalantirofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Palantír of Orthanc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Palantir
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.
		 * 	<b>Shadow:</b> Spot an [isengard] minions and remove (1) to reveal a card at random from the Free Peoples player's hand. Place that card on top of that player's draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("palantir");

		assertEquals("The Palantír of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		//Uncomment this once this has been converted to JSON; java cards don't support support area items with classes
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PALANTIR));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PalantirRequires1IsengardMinionToPlayAnd1ToActivate() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCardsToFreepsHand("palantir", "uruk1", "uruk2");

		var palantir = scn.GetShadowCard("palantir");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		scn.MoveCardsToHand(palantir, uruk1, uruk2);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(23, scn.GetTwilight());

		assertFalse(scn.ShadowPlayAvailable(palantir));
		scn.ShadowPlayCard(uruk1);
		assertTrue(scn.ShadowPlayAvailable(palantir));
		scn.ShadowPlayCard(palantir);
		assertTrue(scn.ShadowActionAvailable(palantir));
	}

	@Test
	public void PalantirRemoves2ToRevealCardFromFreepsHandAndPlacesOnDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCardsToFreepsHand("palantir", "uruk1", "uruk2");

		var palantir = scn.GetShadowCard("palantir");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(23, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());

		scn.ShadowUseCardAction(palantir);
		assertEquals(22, scn.GetTwilight());
		assertEquals(1, scn.ShadowGetCardChoiceCount());

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
	}
}
