package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gorbag", "7_180");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, cost 4, site 3; her text reads [wraith] conditions on companions
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; his text needs 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl, strength 5; its text needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorbagStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorbag, Lieutenant of Cirith Ungol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, Gorbag is strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gorbag");

		assertEquals("Gorbag", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Cirith Ungol", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileANazgulCanBeSpottedHeIsStrengthPlus2AndDamagePlus1AndAnotherOrcIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gorbag = scn.GetShadowCard("gorbag");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, lemenya, hound);

		assertInPlay(gorbag, lemenya, hound);
		// 11 + 2
		assertEquals(13, scn.GetStrength(gorbag));
		assertEquals(1, scn.GetKeywordCount(gorbag, Keyword.DAMAGE));
		// The Hound, a [wraith] Orc beside the same Nazgûl, is left as printed.
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARaiderNazgulServesAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gorbag = scn.GetShadowCard("gorbag");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, khamul);

		assertInPlay(gorbag, khamul);
		assertEquals(13, scn.GetStrength(gorbag));
		assertEquals(1, scn.GetKeywordCount(gorbag, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BesideAnOrcAloneHeIsAsPrintedUntilANazgulIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gorbag = scn.GetShadowCard("gorbag");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(lemenya);
		assertInHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, hound);
		// Enough for Lemenya's 4 and her roaming penalty at site 2.
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [wraith] Orc is no Nazgûl.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(11, scn.GetStrength(gorbag));
		assertEquals(0, scn.GetKeywordCount(gorbag, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(gorbag, Keyword.DAMAGE));

		scn.ShadowPlayCard(lemenya);
		assertInPlay(lemenya);
		assertEquals(13, scn.GetStrength(gorbag));
		assertEquals(1, scn.GetKeywordCount(gorbag, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
