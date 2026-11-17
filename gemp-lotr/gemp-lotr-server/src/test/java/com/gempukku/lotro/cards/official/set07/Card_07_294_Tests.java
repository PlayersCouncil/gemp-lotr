package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_294_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "7_294");
					put("chieftain", "1_266");  // Orc Chieftain: another [sauron] Orc; text on a weapon PLAYED on him only
					put("stairs", "1_24");      // Stairs of Khazad-dûm: [dwarven] condition of cost 0, no requirement; played to take the hand from 4 to 3
					put("sword", "1_299");      // Hobbit Sword: a card for the Free Peoples hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a card for the Free Peoples hand
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a card for the Free Peoples hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcArcherTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Archer Troop
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 6
		 * Game Text: <b>Archer</b>.<br>If you have initiative, the minion archery total is +1 and Orc Archer Troop is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Orc Archer Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithTheShadowInitiativeTheMinionArcheryTotalIsPlusOneAndOnlyTheTroopIsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		scn.MoveCardsToHand(stairs, sword, aragorn, boromir);

		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, chieftain);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		// 1 for the Troop's own archer keyword, and no more.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));

		// A card played from four to three: the Shadow has initiative.
		scn.FreepsPlayCard(stairs);

		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		// 1 for the archer keyword + 1.  The fellowship archery total is untouched.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		// The Troop alone: another [sauron] Orc beside it is not made fierce.
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
