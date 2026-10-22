package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "8_80");
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, site 4; its text needs 2 wounds on the Ring-bearer
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, cost 4, site 3; her text reads [wraith] conditions on companions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, site 3; his text needs 3 or 5 burdens
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, site 6; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Úlairë Nertëa, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. <b>Fierce</b>.<br>The site number of each [wraith] minion is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedAtHerOwnSiteNumberAndOnceInPlayMakesEachWraithMinionSiteNumberMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(nertea, lemenya);
		assertInHand(nertea, lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, khamul, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertInHand(nertea, lemenya);
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertEquals(3, scn.GetMinionSiteNumber(khamul));
		assertEquals(6, scn.GetMinionSiteNumber(soldier));
		assertTrue(scn.HasKeyword(hound, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(khamul, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(soldier, Keyword.ROAMING));

		scn.ShadowPlayCard(nertea);
		// 4 to play + 2 roaming.  Her text does not name her, so it is not active while she is played (CR 4.2
		// "modifier"): at site 2 her site number is still 3.  Had her -1 applied she would have cost 4.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);

		// Once in play it applies to each [wraith] minion, herself included, of either race.
		assertEquals(2, scn.GetMinionSiteNumber(nertea));
		assertFalse(scn.HasKeyword(nertea, Keyword.ROAMING));
		assertEquals(3, scn.GetMinionSiteNumber(hound));
		// The [raider] Nazgûl and the [sauron] Orc are where they were.
		assertEquals(3, scn.GetMinionSiteNumber(khamul));
		assertEquals(6, scn.GetMinionSiteNumber(soldier));
		assertTrue(scn.HasKeyword(khamul, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(soldier, Keyword.ROAMING));

		// Lemenya in hand is a [wraith] minion too: site number 3 - 1 = 2, so at site 2 she is no longer roaming
		// and costs 4, not 4 + 2.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);
		assertEquals(3, scn.GetTwilight());     // lint-ok: Nertëa's -1 takes the roaming penalty off Lemenya's cost: 7 - 4
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(2, scn.GetMinionSiteNumber(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
