package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_055_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("mirror", "51_55");
					put("galadriel", "1_45");
					put("allyHome3_1", "1_60");
					put("allyHome6_1", "1_56");

					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TheMirrorofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: *The Mirror of Galadriel
		* Side: Free Peoples
		* Culture: Elven
		* Twilight Cost: 2
		* Type: possession
		* Subtype: 
		* Game Text: Plays to your support area.
		* 	Each Elf ally whose home is site 6 is strength +1.
		* 	<b>Maneuver:</b> Exert Galadriel to reveal 3 cards at random from an opponent's hand. Place one on top of that player's draw deck.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mirror = scn.GetFreepsCard("mirror");

		assertTrue(mirror.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, mirror.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, mirror.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, mirror.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(mirror, Keyword.SUPPORT_AREA));
		assertEquals(2, mirror.getBlueprint().getTwilightCost());
	}

	@Test
	public void MirrorMakesSite6AlliesStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mirror = scn.GetFreepsCard("mirror");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl allyHome3_1 = scn.GetFreepsCard("allyHome3_1");
		PhysicalCardImpl allyHome6_1 = scn.GetFreepsCard("allyHome6_1");
		scn.FreepsMoveCharToTable(galadriel, allyHome3_1, allyHome6_1);
		scn.FreepsMoveCardToHand(mirror);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(allyHome3_1));
		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(allyHome6_1));
		scn.FreepsPlayCard(mirror);
		assertEquals(5, scn.GetStrength(allyHome3_1));
		assertEquals(4, scn.GetStrength(galadriel));
		assertEquals(4, scn.GetStrength(allyHome6_1));
	}

	@Test
	public void ManeuverAbilityReveals3CardFromShadowHandAndPuts1OnTopDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mirror = scn.GetFreepsCard("mirror");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		scn.FreepsMoveCharToTable(galadriel);
		scn.FreepsMoveCardToSupportArea(mirror);

		PhysicalCardImpl card1 = scn.GetShadowCard("allyHome3_1");
		PhysicalCardImpl card2 = scn.GetShadowCard("allyHome6_1");
		PhysicalCardImpl card3 = scn.GetShadowCard("galadriel");
		scn.ShadowMoveCardToHand(card1, card2, card3);
		scn.ShadowMoveCharToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(mirror));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(Zone.HAND, card1.getZone());

		scn.FreepsUseCardAction(mirror);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(card1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(Zone.DECK, card1.getZone());
	}

	@Test
	public void ManeuverAbilityReveals3RandomCardsFromShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mirror = scn.GetFreepsCard("mirror");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		scn.FreepsMoveCharToTable(galadriel);
		scn.FreepsMoveCardToSupportArea(mirror);

		PhysicalCardImpl card1 = scn.GetShadowCard("allyHome3_1");
		PhysicalCardImpl card2 = scn.GetShadowCard("allyHome6_1");
		PhysicalCardImpl card3 = scn.GetShadowCard("galadriel");
		PhysicalCardImpl card4 = scn.GetShadowCard("mirror");
		scn.ShadowMoveCardToHand(card1, card2, card3, card4);
		scn.ShadowMoveCharToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(mirror));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(Zone.HAND, card1.getZone());

		scn.FreepsUseCardAction(mirror);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
	}

}
