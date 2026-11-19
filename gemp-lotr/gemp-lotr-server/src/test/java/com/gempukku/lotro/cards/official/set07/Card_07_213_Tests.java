package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_213_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "7_213");
					// Úlairë Cantëa, Lieutenant of Dol Guldur: [wraith] Nazgûl, strength 10, fierce; a skirmish ability
					// used only on purpose
					put("cantea", "1_230");
					// In Twilight: [wraith] condition, no minion; bearer must be a [wraith] minion, strength +2; its
					// triggers answer its play and its bearer's win
					put("twilight", "19_34");
					// Ulaire Attea, Khamul of the East: a Nazgûl minion of [raider], strength 12; his text needs burdens
					put("khamul", "103_61");
					// Morgul Hound: [wraith] Orc, cost 2, site 4, strength 5; +4 and fierce only at 2 wounds on the
					// Ring-bearer
					put("hound1", "7_192");
					put("hound2", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Lemenya, Assailing Minion
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Úlairë Lemenya is strength +1 for each other [wraith] minion you can spot.<br>While you can spot 3 [wraith] minions Úlairë Lemenya is <b>fierce</b>.<br>While you can spot 4 [wraith] minions Úlairë Lemenya is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Assailing Minion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthFierceAndDamageGrowWithTheWraithMinionsSpottedAsTheyArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var cantea = scn.GetShadowCard("cantea");
		var twilight = scn.GetShadowCard("twilight");
		var khamul = scn.GetShadowCard("khamul");
		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		scn.MoveCardsToHand(hound1, hound2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(lemenya, cantea, khamul);
		scn.AttachCardsTo(cantea, twilight);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 9 + 1: Cantëa is the one other [wraith] minion.  Khamûl (a Nazgûl, but [raider]) and In Twilight (a
		// [wraith] card, but no minion) are not counted, nor is Lemenya himself.
		assertEquals(10, scn.GetStrength(lemenya));
		// Two [wraith] minions, himself included.
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.DAMAGE));
		// 10 + 2 for In Twilight
		assertEquals(12, scn.GetStrength(cantea));
		assertEquals(12, scn.GetStrength(khamul));

		scn.ShadowPlayCard(hound1);
		// 9 + 2, and three [wraith] minions
		assertEquals(11, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(hound1));
		assertFalse(scn.HasKeyword(hound1, Keyword.FIERCE));

		scn.ShadowPlayCard(hound2);
		// 9 + 3, and four [wraith] minions
		assertEquals(12, scn.GetStrength(lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(lemenya, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		// The others are untouched.
		assertEquals(12, scn.GetStrength(cantea));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound1));
		assertFalse(scn.HasKeyword(hound1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(hound1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
