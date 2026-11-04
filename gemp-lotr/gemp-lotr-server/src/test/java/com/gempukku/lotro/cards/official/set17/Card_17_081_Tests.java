package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("maurader", "17_81");
					put("maurader2", "17_81");

					put("beckoning", "12_83");  // The Beckoning Shadow: [orc] condition, support area; corrupts only at 5 burdens with an empty Free Peoples deck
					put("effort", "15_106");    // Coordinated Effort: [orc] condition, support area; answers only a Free Peoples possession played
					put("depths", "13_120");    // Unforgiving Depths: [orc] condition, support area; muster only with 3 underground sites
					put("greatcost", "12_88");  // Great Cost: [orc] condition, support area; nothing reached in the fellowship phase
					put("clash", "17_68");      // Chaotic Clash: [orc] condition, kept in hand here
					put("gear", "17_90");       // Rider's Gear: an [orc] possession that is no condition
					put("denizen", "11_115");   // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 only at an underground site
					put("denizen2", "11_115");
					put("weary", "4_212");      // Weary: an [isengard] condition; answers only an [isengard] tracker assigned

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: to exert for Sleep, Caradhras
					put("sleep", "1_84");       // Sleep, Caradhras: Fellowship event, exert Gandalf to discard every condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishMauraderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Maurader
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each [orc] condition in your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("maurader");

		assertEquals("Orkish Maurader", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthIsPlusOneForEachOrcConditionInItsOwnersDiscardPileAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var maurader = scn.GetShadowCard("maurader");
		var maurader2 = scn.GetShadowCard("maurader2");
		var beckoning = scn.GetShadowCard("beckoning");
		var effort = scn.GetShadowCard("effort");
		var depths = scn.GetShadowCard("depths");
		var clash = scn.GetShadowCard("clash");
		var gear = scn.GetShadowCard("gear");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var weary = scn.GetShadowCard("weary");
		var hisGreatCost = scn.GetFreepsCard("greatcost");

		scn.StartGame();
		scn.MoveMinionsToTable(maurader, maurader2, denizen);
		// Not counted: an [orc] condition in play, one in hand, one in the other player's discard pile, and in this
		// player's discard pile an [orc] possession, an [orc] minion and a condition of another culture.
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(clash);
		scn.MoveCardsToDiscard(hisGreatCost, gear, denizen2, weary);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(clash);
		assertInDiscard(hisGreatCost, gear, denizen2, weary);

		assertEquals(6, scn.GetStrength(maurader));
		assertEquals(6, scn.GetStrength(maurader2));
		assertEquals(9, scn.GetStrength(denizen));

		scn.MoveCardsToDiscard(effort);
		assertInDiscard(effort);
		assertEquals(7, scn.GetStrength(maurader));
		// Each copy counts for itself; the other copy adds nothing to it.
		assertEquals(7, scn.GetStrength(maurader2));
		assertEquals(9, scn.GetStrength(denizen));

		scn.MoveCardsToDiscard(depths);
		assertInDiscard(depths);
		assertEquals(8, scn.GetStrength(maurader));
		assertEquals(8, scn.GetStrength(maurader2));
		assertEquals(9, scn.GetStrength(denizen));
	}

	@Test
	public void OrcConditionsDiscardedFromPlayMakeItStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var maurader = scn.GetShadowCard("maurader");
		var beckoning = scn.GetShadowCard("beckoning");
		var effort = scn.GetShadowCard("effort");
		var weary = scn.GetShadowCard("weary");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();
		scn.MoveMinionsToTable(maurader);
		scn.MoveCardsToSupportArea(beckoning, effort);
		scn.AttachCardsTo(gandalf, weary);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, beckoning, effort);
		assertAttachedTo(weary, gandalf);
		assertInHand(sleep);
		assertEquals(6, scn.GetStrength(maurader));

		// Every condition is discarded: two [orc] ones and an [isengard] one.
		scn.FreepsPlayCard(sleep);
		assertInDiscard(beckoning, effort, weary);
		assertEquals(8, scn.GetStrength(maurader));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
