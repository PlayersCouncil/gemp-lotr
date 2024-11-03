package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_306_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("pippin", "1_306");
					put("taba", "1_317");

					put("uruk", "1_151");
					put("power", "1_136");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pippin, Friend to Frodo
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Your opponent may not discard your [shire] tales from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Friend to Frodo", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	@Test
	public void PippinDoesNotBlockFreepsUsingThereAndBackAgain() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var taba = scn.GetFreepsCard("taba");
		scn.FreepsMoveCharToTable(pippin);
		scn.FreepsAttachCardsTo(frodo, taba);

		var power = scn.GetShadowCard("power");
		scn.ShadowMoveCharToTable("uruk");
		scn.ShadowMoveCardToHand(power);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(taba));
		assertEquals(Zone.ATTACHED, taba.getZone());
		scn.FreepsUseCardAction(taba);
		assertEquals(Zone.DISCARD, taba.getZone());
	}

	@Test
	public void PippinPreventsShadowDiscardingShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var taba = scn.GetFreepsCard("taba");
		scn.FreepsMoveCharToTable(pippin);
		scn.FreepsAttachCardsTo(frodo, taba);

		var power = scn.GetShadowCard("power");
		scn.ShadowMoveCharToTable("uruk");
		scn.ShadowMoveCardToHand(power);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.ATTACHED, taba.getZone());
		scn.ShadowPlayCard(power);
		assertEquals(Zone.ATTACHED, taba.getZone());
	}
}
