package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_001_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("great", "10_1");
					put("gimli", "8_5");

					put("troll", "8_102");
					put("assassin", "8_95");
					put("towers", "7_281");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GreatDayGreatHourStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Great Day, Great Hour
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If an opponent plays a minion, exert a Dwarf who is damage +X to exert that minion X times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("great");

		assertEquals("Great Day, Great Hour", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.RESPONSE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GreatDayGreatHourExertsMinionTwiceWithDamagePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(great);
		scn.FreepsMoveCharToTable(gimli);

		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(troll);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(troll);

		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(troll));
	}

	@Test
	public void GreatDayGreatHourExertsMinionOnceWithDamagePlus2IfOnly2Vitality() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(great);
		scn.FreepsMoveCharToTable(gimli);

		var assassin = scn.GetShadowCard("assassin");
		scn.ShadowMoveCardToHand(assassin);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(assassin);

		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(assassin));
	}

	@Test
	public void GreatDayGreatHourExertsMinionPlayedFromAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(great);
		scn.FreepsMoveCharToTable(gimli);

		var assassin = scn.GetShadowCard("assassin");
		var towers = scn.GetShadowCard("towers");
		scn.ShadowMoveCardToHand(assassin);
		scn.ShadowMoveCardToSupportArea(towers);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(Zone.HAND, assassin.getZone());
		scn.ShadowUseCardAction(towers);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(Zone.SHADOW_CHARACTERS, assassin.getZone());
		assertEquals(1, scn.GetWoundsOn(assassin));
	}
}
