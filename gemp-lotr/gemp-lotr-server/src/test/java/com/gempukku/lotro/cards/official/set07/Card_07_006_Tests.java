package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "7_6");

					put("guard", "1_7");        // Dwarf Guard: another Dwarf companion, strength 4, no text in play
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition, cost 0, no requirement; strength only against a [moria] minion
					put("streaming", "8_78");   // Streaming to the Field: for each [wraith] Orc spotted, the Free Peoples player needs a card more for initiative
					put("whelp", "7_202");      // Morgul Whelp: a [wraith] Orc; stronger only while the Ring-bearer is wounded

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");      // Goblin Patrol Troop: no text
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gimli, Faithful Companion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Damage +1</b>.<br>While you have initiative, Gimli is strength +2.
		*/

		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", gimli.getBlueprint().getTitle());
		assertEquals("Faithful Companion", gimli.getBlueprint().getSubtitle());
		assertTrue(gimli.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gimli.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, gimli.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, gimli.getBlueprint().getCardType());
		assertEquals(Race.DWARF, gimli.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(2, gimli.getBlueprint().getTwilightCost());
		assertEquals(6, gimli.getBlueprint().getStrength());
		assertEquals(3, gimli.getBlueprint().getVitality());
		assertEquals(6, gimli.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, gimli.getBlueprint().getSignet());
	}

	@Test
	public void IsStrengthPlus2WhileTheFreePeoplesPlayerHasInitiativeAndNotOtherwise() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		var card4 = scn.GetFreepsCard("card4");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));

		scn.StartGame();

		// Four cards in hand: the Free Peoples player has initiative.  6 + 2.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		// Down to 3 by a legal play: the Shadow has initiative.
		scn.FreepsPlayCard(stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		// A fourth card again, and initiative with it.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GoesByInitiativeAndNotByFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		var streaming = scn.GetShadowCard("streaming");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCardsToSupportArea(streaming);

		scn.StartGame();

		// Streaming to the Field with no [wraith] Orc to spot asks nothing more.
		assertInZone(Zone.SUPPORT, streaming);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(gimli));

		// One [wraith] Orc: initiative takes a fifth card.  The same four cards, and the Shadow has initiative.
		scn.MoveMinionsToTable(whelp);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
