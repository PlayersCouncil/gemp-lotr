package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_309_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rosie", "1_309");
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability used only on purpose
					// Merry, Friend to Sam: a [shire] Hobbit companion of another title, strength 3, vitality 4; a
					// skirmish ability only
					put("merry", "1_302");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RosieCottonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Rosie Cotton, Hobbiton Lass
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 1
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: Sam is strength +1.
		 * 	<b>Fellowship:</b> Exert Rosie Cotton to heal Sam.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rosie");

		assertEquals("Rosie Cotton", card.getBlueprint().getTitle());
		assertEquals("Hobbiton Lass", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void OnceRosieIsPlayedSamIsStrengthPlusOneAndNoOtherHobbitIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(rosie);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		int frodoStrength = scn.GetStrength(frodo);

		scn.FreepsPlayCard(rosie);
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsRosieToHealOneWoundFromSamAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(rosie);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(rosie));

		// Sam is the only card of his title there can be, so nobody is asked who is healed.
		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileRosieIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(rosie);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 1);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(rosie, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rosie));
	}
}
