package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_165_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "1_165");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; its only text answers its own play (cheated in here)
					put("balrog", "12_79");     // The Balrog, The Terror of Khazad-dûm: a [moria] minion who is no Orc; its text acts only underground
					put("soldier", "1_271");    // Orc Soldier: an Orc of another culture ([sauron]); a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CaveTrollofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Cave Troll of Moria, Scourge of the Black Pit
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. To play, spot a [moria] Orc.<br>At an underground site, Cave Troll of Moria's twilight cost is -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Cave Troll of Moria", card.getBlueprint().getTitle());
		assertEquals("Scourge of the Black Pit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAMoriaOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var balrog = scn.GetShadowCard("balrog");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveCardsToHand(troll, troop);
		// A [moria] minion who is no Orc, and an Orc who is not [moria].
		scn.MoveMinionsToTable(balrog, soldier);

		scn.FreepsPass();
		// Shadow phase at site 2: the troll would cost 10 + 2 roaming, the Patrol Troop 6 + 2.
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troll));
		assertTrue(scn.ShadowPlayAvailable(troop));

		scn.ShadowPlayCard(troop);

		// The Troop's 6 + 2 roaming leaves 12 in the pool, exactly the troll's 10 + 2.
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteHisTwilightCostIsThreeLessAndNoOtherCardsIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(troll, troop);
		scn.MoveMinionsToTable(runner);

		// Site 3, Rivendell Valley, is not underground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetTwilightCost(troll));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.FreepsPass();

		// Site 4, Balin's Tomb, is underground (and the troll's own site number: no roaming).
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilightCost(troll));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troll));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
