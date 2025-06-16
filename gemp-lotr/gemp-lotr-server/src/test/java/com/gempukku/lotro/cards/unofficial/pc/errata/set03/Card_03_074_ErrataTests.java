package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_074_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raider", "53_74");
					put("demands", "2_40");

					put("rosie", "1_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRaiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Uruk Raider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: Tracker. Damage +1.
		* 	While an ally is in the dead pile, this minion is strength +3 and <b>fierce</b>.
		* 	Each time an ally exerts, you may spot another [isengard] card and exert this minion to wound that ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raider");

		assertEquals("Uruk Raider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukRaiderIsStrengthPlus3AndFierceWhileAllyIsInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToHand(rosie);

		var raider = scn.GetShadowCard("raider");
		scn.MoveMinionsToTable(raider);

		scn.StartGame();

		assertEquals(Zone.HAND,  rosie.getZone());
		assertEquals(6, scn.GetStrength(raider));
		assertFalse(scn.HasKeyword(raider, Keyword.FIERCE));

		scn.MoveCardsToDeadPile(rosie);

		assertEquals(Zone.DEAD,  rosie.getZone());
		assertEquals(9, scn.GetStrength(raider));
		assertTrue(scn.HasKeyword(raider, Keyword.FIERCE));
	}

	@Test
	public void UrukRaiderCannotRespondToAllyExertionIfNoOtherIsengardCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		var raider = scn.GetShadowCard("raider");
		scn.MoveMinionsToTable(raider);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(raider));
		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(Zone.SUPPORT,  rosie.getZone());

		scn.FreepsUseCardAction(rosie);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(Zone.SUPPORT,  rosie.getZone());
		assertEquals(0, scn.GetWoundsOn(raider));
		assertEquals(1, scn.GetWoundsOn(rosie));
	}

	@Test
	public void UrukRaiderCannotRespondToAllyExertionIfSpotsOtherIsengardCardButCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		var raider = scn.GetShadowCard("raider");
		var demands = scn.GetShadowCard("demands");
		scn.MoveMinionsToTable(raider);
		scn.MoveCardsToSupportArea(demands);
		scn.AddWoundsToChar(raider, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(raider));
		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(Zone.SUPPORT,  rosie.getZone());

		scn.FreepsUseCardAction(rosie);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(Zone.SUPPORT,  rosie.getZone());
		assertEquals(1, scn.GetWoundsOn(raider));
		assertEquals(1, scn.GetWoundsOn(rosie));
	}

	@Test
	public void UrukRaiderCanSpotAnotherIsengardCardAndExertToWoundExertingAlly() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		var raider = scn.GetShadowCard("raider");
		var demands = scn.GetShadowCard("demands");
		scn.MoveMinionsToTable(raider);
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(raider));
		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(Zone.SUPPORT,  rosie.getZone());

		scn.FreepsUseCardAction(rosie);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(Zone.DEAD,  rosie.getZone());
		assertEquals(1, scn.GetWoundsOn(raider));
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void UrukRaiderTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

//		var card = scn.GetFreepsCard("card");
//		scn.MoveCardsToHand(card);
//		scn.MoveCompanionToTable(card);
//		scn.MoveCardsToSupportArea(card);
//		scn.MoveCardToDiscard(card);
//		scn.MoveCardsToTopOfDeck(card);

		var card = scn.GetShadowCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
